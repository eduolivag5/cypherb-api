package com.cypherb.divergences.dto;

import com.cypherb.divergences.model.Monedas;

public record SuscripcionResponseDTO(
        Long id,
        Long usuarioId,
        Long chatId,              // <-- Añadido el chatId aquí
        MonedaResponseDTO moneda
) {
    // DTO interno plano para evitar problemas de proxies de Hibernate
    public record MonedaResponseDTO(
            String simbolo,
            String parCompleto
    ) {
        public MonedaResponseDTO(Monedas monedaEntity) {
            this(
                    monedaEntity != null ? monedaEntity.getSimbolo() : null,
                    monedaEntity != null ? monedaEntity.getParCompleto() : null
            );
        }
    }

    public SuscripcionResponseDTO(Long id, Long usuarioId, Long chatId, Monedas monedaEntity) {
        this(id, usuarioId, chatId, monedaEntity != null ? new MonedaResponseDTO(monedaEntity) : null);
    }
}