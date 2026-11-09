package com.cypherb.divergences.dto;

import java.util.List;

/**
 * Respuesta genérica para listados: el total de elementos y los elementos.
 * Se puede reutilizar en cualquier controller: RespuestaListado<Monedas>, etc.
 */
public record RespuestaListadoDTO<T>(long total, List<T> records) {

    public static <T> RespuestaListadoDTO<T> de(List<T> records) {
        return new RespuestaListadoDTO<>(records.size(), records);
    }
}