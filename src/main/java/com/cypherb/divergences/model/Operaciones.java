package com.cypherb.divergences.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Operación real de futuros abierta o cerrada en el exchange.
 *
 * Se usa también como cuerpo de las peticiones POST (abrir) y PUT (cerrar).
 * Los campos calculados (cantidad, PnL, ROI...) los rellena el servidor.
 *
 * No se usa @Data para no generar equals/hashCode con la relación a Monedas.
 */
@Entity
@Table(
        name = "operaciones",
        indexes = {
                @Index(name = "idx_operaciones_estado", columnList = "estado"),
                @Index(name = "idx_operaciones_moneda", columnList = "moneda_id"),
                @Index(name = "idx_operaciones_fecha_apertura", columnList = "fecha_apertura")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "moneda")
public class Operaciones {

    public enum Direccion { LONG, SHORT }

    public enum Estado { ABIERTA, TAKE_PROFIT, STOP_LOSS, LIQUIDADA, CERRADA_MANUAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EAGER para poder devolver la entidad en JSON con su moneda
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "moneda_id", nullable = false)
    private Monedas moneda;

    @Enumerated(EnumType.STRING)
    @Column(name = "direccion", nullable = false, length = 5)
    @Builder.Default
    private Direccion direccion = Direccion.LONG;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private Estado estado = Estado.ABIERTA;

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @Column(name = "precio_entrada", nullable = false, precision = 24, scale = 10)
    private BigDecimal precioEntrada;

    @Column(name = "precio_salida", precision = 24, scale = 10)
    private BigDecimal precioSalida;

    @Column(name = "apalancamiento", nullable = false)
    private Integer apalancamiento;

    // Importe invertido: margen aportado, no la exposición total
    @Column(name = "margen", nullable = false, precision = 18, scale = 2)
    private BigDecimal margen;

    // Unidades de la moneda: margen * apalancamiento / precio de entrada
    @Column(name = "cantidad", nullable = false, precision = 30, scale = 10)
    private BigDecimal cantidad;

    // Precio al que se cierra con beneficio
    @Column(name = "take_profit", nullable = false, precision = 24, scale = 10)
    private BigDecimal takeProfit;

    // Precio al que se cierra con pérdida; null si no hay stop-loss
    @Column(name = "stop_loss", precision = 24, scale = 10)
    private BigDecimal stopLoss;

    @Column(name = "precio_liquidacion", precision = 24, scale = 10)
    private BigDecimal precioLiquidacion;

    @Column(name = "comisiones", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal comisiones = BigDecimal.ZERO;

    // Positivo si se paga, negativo si se cobra
    @Column(name = "funding", nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal funding = BigDecimal.ZERO;

    @Column(name = "pnl_bruto", precision = 18, scale = 2)
    private BigDecimal pnlBruto;

    @Column(name = "pnl_neto", precision = 18, scale = 2)
    private BigDecimal pnlNeto;

    // Rentabilidad sobre el margen, en %
    @Column(name = "roi", precision = 10, scale = 2)
    private BigDecimal roi;

    @Column(name = "notas", length = 500)
    private String notas;

    @CreationTimestamp
    @Column(name = "creado_en", nullable = false, updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    /** Dinero total movido en el exchange. */
    @Transient
    public BigDecimal getExposicion() {
        if (margen == null || apalancamiento == null) {
            return null;
        }
        return margen.multiply(BigDecimal.valueOf(apalancamiento));
    }
}