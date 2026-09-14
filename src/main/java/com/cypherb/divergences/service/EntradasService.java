package com.cypherb.divergences.service;

import com.cypherb.divergences.model.Entradas;
import com.cypherb.divergences.model.Monedas;
import com.cypherb.divergences.repository.EntradasRepository;
import com.cypherb.divergences.repository.MonedasRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EntradasService {

    private final EntradasRepository entradasRepository;
    private final MonedasRepository monedasRepository;

    @Transactional
    public Entradas crearEntrada(String simbolo, BigDecimal precio, Entradas.TipoEntrada tipo, LocalDate fechaCreacion) {
        Monedas moneda = monedasRepository.findBySimbolo(simbolo.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Moneda no encontrada con símbolo: " + simbolo));

        LocalDate fecha = fechaCreacion != null ? fechaCreacion : LocalDate.now();

        return (Entradas) entradasRepository.findByMonedaAndTipoAndFechaCreacion(moneda, tipo, fecha)
                .orElseGet(() -> {
                    Entradas nuevaEntrada = Entradas.builder()
                            .precio(precio)
                            .tipo(tipo)
                            .moneda(moneda)
                            .fechaCreacion(fecha)
                            .build();
                    return entradasRepository.save(nuevaEntrada);
                });
    }

    @Transactional(readOnly = true)
    public List listarEntradasFiltradas(
            String simbolo,
            Entradas.TipoEntrada tipo,
            BigDecimal precioMin,
            BigDecimal precioMax,
            LocalDate fechaInicio,
            LocalDate fechaFin
    ) {
        Specification spec = (root, query, cb) -> {
            List predicates = new ArrayList<>();

            if (simbolo != null && !simbolo.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("moneda").get("simbolo")), simbolo.toUpperCase()));
            }
            if (tipo != null) {
                predicates.add(cb.equal(root.get("tipo"), tipo));
            }
            if (precioMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("precio"), precioMin));
            }
            if (precioMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("precio"), precioMax));
            }
            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaFin));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }

            jakarta.persistence.criteria.Predicate finalPredicate = (jakarta.persistence.criteria.Predicate) predicates.get(0);
            for (int i = 1; i < predicates.size(); i++) {
                finalPredicate = cb.and(finalPredicate, (jakarta.persistence.criteria.Predicate) predicates.get(i));
            }

            return finalPredicate;
        };

        return entradasRepository.findAll(spec);
    }
}