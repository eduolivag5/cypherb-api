package com.cypherb.divergences.service;

import com.cypherb.divergences.model.Monedas;
import com.cypherb.divergences.model.Operaciones;
import com.cypherb.divergences.model.Operaciones.Direccion;
import com.cypherb.divergences.model.Operaciones.Estado;
import com.cypherb.divergences.repository.MonedasRepository;
import com.cypherb.divergences.repository.OperacionesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OperacionesService {

    private static final int APALANCAMIENTO_MAXIMO = 125;
    // Margen de mantenimiento aproximado de Binance para posiciones pequeñas
    private static final BigDecimal MARGEN_MANTENIMIENTO = new BigDecimal("0.004");
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private static final MathContext PRECISION = MathContext.DECIMAL64;

    private final OperacionesRepository operacionesRepository;
    private final MonedasRepository monedasRepository;

    /**
     * Abre una operación. Del cuerpo se usan: moneda.id, direccion, fechaApertura,
     * precioEntrada, apalancamiento, margen, takeProfit, stopLoss,
     * precioLiquidacion, comisiones y notas. El resto lo calcula el servidor.
     */
    @Transactional
    public Operaciones abrir(Operaciones datos) {
        if (datos.getMoneda() == null || datos.getMoneda().getId() == null) {
            throw peticionIncorrecta("moneda.id es obligatorio");
        }
        exigirPositivo(datos.getPrecioEntrada(), "precioEntrada");
        exigirPositivo(datos.getMargen(), "margen");
        exigirPositivo(datos.getTakeProfit(), "takeProfit");
        Integer apalancamiento = datos.getApalancamiento();
        if (apalancamiento == null || apalancamiento < 1 || apalancamiento > APALANCAMIENTO_MAXIMO) {
            throw peticionIncorrecta("apalancamiento debe estar entre 1 y " + APALANCAMIENTO_MAXIMO);
        }

        Long monedaId = datos.getMoneda().getId();
        Monedas moneda = monedasRepository.findById(monedaId)
                .orElseThrow(() -> noEncontrado("No existe la moneda con id " + monedaId));

        Direccion direccion = datos.getDireccion() != null ? datos.getDireccion() : Direccion.LONG;
        BigDecimal precioLiquidacion = datos.getPrecioLiquidacion() != null
                ? datos.getPrecioLiquidacion()
                : estimarPrecioLiquidacion(datos.getPrecioEntrada(), apalancamiento, direccion);

        validarNiveles(direccion, datos.getPrecioEntrada(), datos.getTakeProfit(),
                datos.getStopLoss(), precioLiquidacion);

        BigDecimal cantidad = datos.getMargen()
                .multiply(BigDecimal.valueOf(apalancamiento))
                .divide(datos.getPrecioEntrada(), 10, RoundingMode.HALF_UP);

        // Se construye una entidad nueva para ignorar campos que no debe fijar el cliente
        Operaciones operacion = Operaciones.builder()
                .moneda(moneda)
                .direccion(direccion)
                .estado(Estado.ABIERTA)
                .fechaApertura(datos.getFechaApertura() != null ? datos.getFechaApertura() : LocalDateTime.now())
                .precioEntrada(datos.getPrecioEntrada())
                .apalancamiento(apalancamiento)
                .margen(datos.getMargen())
                .cantidad(cantidad)
                .takeProfit(datos.getTakeProfit())
                .stopLoss(datos.getStopLoss())
                .precioLiquidacion(precioLiquidacion)
                .comisiones(cero(datos.getComisiones()))
                .funding(BigDecimal.ZERO)
                .notas(datos.getNotas())
                .build();

        return operacionesRepository.save(operacion);
    }

    /**
     * Cierra una operación abierta. Del cuerpo se usan: estado, fechaCierre,
     * precioSalida, comisiones (la de cierre, se suma a la de apertura),
     * funding (total pagado) y notas.
     */
    @Transactional
    public Operaciones cerrar(Long id, Operaciones datos) {
        Operaciones operacion = buscar(id);
        if (operacion.getEstado() != Estado.ABIERTA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La operación " + id + " ya está cerrada (" + operacion.getEstado() + ")");
        }

        Estado estado = datos.getEstado() != null && datos.getEstado() != Estado.ABIERTA
                ? datos.getEstado()
                : Estado.CERRADA_MANUAL;

        LocalDateTime fechaCierre = datos.getFechaCierre() != null ? datos.getFechaCierre() : LocalDateTime.now();
        if (fechaCierre.isBefore(operacion.getFechaApertura())) {
            throw peticionIncorrecta("fechaCierre no puede ser anterior a la fecha de apertura");
        }

        BigDecimal precioSalida = datos.getPrecioSalida();
        if (precioSalida == null && estado == Estado.LIQUIDADA) {
            precioSalida = operacion.getPrecioLiquidacion();
        }
        exigirPositivo(precioSalida, "precioSalida");

        operacion.setEstado(estado);
        operacion.setFechaCierre(fechaCierre);
        operacion.setPrecioSalida(precioSalida);
        operacion.setComisiones(operacion.getComisiones().add(cero(datos.getComisiones())));
        if (datos.getFunding() != null) {
            operacion.setFunding(datos.getFunding());
        }
        if (datos.getNotas() != null) {
            operacion.setNotas(datos.getNotas());
        }

        calcularResultado(operacion);
        return operacion;
    }

    @Transactional(readOnly = true)
    public List<Operaciones> listar(Estado estado) {
        return estado != null
                ? operacionesRepository.findByEstadoOrderByFechaAperturaDesc(estado)
                : operacionesRepository.findAllByOrderByFechaAperturaDesc();
    }

    @Transactional(readOnly = true)
    public Operaciones obtener(Long id) {
        return buscar(id);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!operacionesRepository.existsById(id)) {
            throw noEncontrado("No existe la operación con id " + id);
        }
        operacionesRepository.deleteById(id);
    }

    // ------------------------------------------------------------------ cálculos

    /** PnL bruto, neto y ROI de una operación ya cerrada. */
    private void calcularResultado(Operaciones operacion) {
        BigDecimal pnlBruto;
        if (operacion.getEstado() == Estado.LIQUIDADA) {
            // En margen aislado se pierde exactamente el margen aportado
            pnlBruto = operacion.getMargen().negate();
        } else {
            BigDecimal diferencia = operacion.getPrecioSalida().subtract(operacion.getPrecioEntrada());
            if (operacion.getDireccion() == Direccion.SHORT) {
                diferencia = diferencia.negate();
            }
            pnlBruto = operacion.getCantidad().multiply(diferencia, PRECISION);
        }

        BigDecimal pnlNeto = pnlBruto
                .subtract(operacion.getComisiones())
                .subtract(operacion.getFunding());
        BigDecimal roi = pnlNeto.multiply(CIEN).divide(operacion.getMargen(), 2, RoundingMode.HALF_UP);

        operacion.setPnlBruto(pnlBruto.setScale(2, RoundingMode.HALF_UP));
        operacion.setPnlNeto(pnlNeto.setScale(2, RoundingMode.HALF_UP));
        operacion.setRoi(roi);
    }

    /** Estimación para margen aislado; el valor exacto lo da Binance. */
    private BigDecimal estimarPrecioLiquidacion(BigDecimal entrada, int apalancamiento, Direccion direccion) {
        BigDecimal inversa = BigDecimal.ONE.divide(BigDecimal.valueOf(apalancamiento), PRECISION);
        BigDecimal resultado = direccion == Direccion.LONG
                ? entrada.multiply(BigDecimal.ONE.subtract(inversa))
                .divide(BigDecimal.ONE.subtract(MARGEN_MANTENIMIENTO), PRECISION)
                : entrada.multiply(BigDecimal.ONE.add(inversa))
                .divide(BigDecimal.ONE.add(MARGEN_MANTENIMIENTO), PRECISION);
        return resultado.setScale(10, RoundingMode.HALF_UP);
    }

    private void validarNiveles(Direccion direccion, BigDecimal entrada, BigDecimal takeProfit,
                                BigDecimal stopLoss, BigDecimal liquidacion) {
        boolean esLong = direccion == Direccion.LONG;

        if (esLong ? takeProfit.compareTo(entrada) <= 0 : takeProfit.compareTo(entrada) >= 0) {
            throw peticionIncorrecta(esLong
                    ? "En un LONG el takeProfit debe ser mayor que el precio de entrada"
                    : "En un SHORT el takeProfit debe ser menor que el precio de entrada");
        }
        if (stopLoss == null) {
            return;
        }
        exigirPositivo(stopLoss, "stopLoss");
        if (esLong ? stopLoss.compareTo(entrada) >= 0 : stopLoss.compareTo(entrada) <= 0) {
            throw peticionIncorrecta(esLong
                    ? "En un LONG el stopLoss debe ser menor que el precio de entrada"
                    : "En un SHORT el stopLoss debe ser mayor que el precio de entrada");
        }
        if (liquidacion != null
                && (esLong ? stopLoss.compareTo(liquidacion) <= 0 : stopLoss.compareTo(liquidacion) >= 0)) {
            throw peticionIncorrecta("El stopLoss saltaría después de la liquidación (" + liquidacion + ")");
        }
    }

    // ------------------------------------------------------------------- ayudas

    private Operaciones buscar(Long id) {
        return operacionesRepository.findById(id)
                .orElseThrow(() -> noEncontrado("No existe la operación con id " + id));
    }

    private BigDecimal cero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private void exigirPositivo(BigDecimal valor, String campo) {
        if (valor == null || valor.signum() <= 0) {
            throw peticionIncorrecta(campo + " es obligatorio y debe ser mayor que 0");
        }
    }

    private ResponseStatusException peticionIncorrecta(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }

    private ResponseStatusException noEncontrado(String mensaje) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, mensaje);
    }
}