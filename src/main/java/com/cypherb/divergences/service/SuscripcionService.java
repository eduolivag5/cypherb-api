package com.cypherb.divergences.service;

import com.cypherb.divergences.model.Monedas;
import com.cypherb.divergences.model.Suscripcion;
import com.cypherb.divergences.repository.MonedasRepository;
import com.cypherb.divergences.repository.SuscripcionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final MonedasRepository monedasRepository;

    @Transactional
    public Suscripcion crearSuscripcion(Long usuarioId, String simbolo) {
        Monedas moneda = monedasRepository.findBySimbolo(simbolo.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Moneda no encontrada con símbolo: " + simbolo));

        if (suscripcionRepository.existsByUsuarioIdAndMoneda(usuarioId, moneda)) {
            throw new RuntimeException("El usuario ya está suscrito a la moneda: " + simbolo);
        }

        Suscripcion nuevaSuscripcion = Suscripcion.builder()
                .usuarioId(usuarioId)
                .moneda(moneda)
                .build();

        return suscripcionRepository.save(nuevaSuscripcion);
    }

    @Transactional(readOnly = true)
    public List obtenerPorUsuario(Long usuarioId) {
        return suscripcionRepository.findByUsuarioId(usuarioId);
    }

    @Transactional(readOnly = true)
    public List obtenerTodas() {
        return suscripcionRepository.findAll();
    }

    @Transactional
    public void eliminarSuscripcion(Long usuarioId, String simbolo) throws Throwable {
        Monedas moneda = monedasRepository.findBySimbolo(simbolo.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Moneda no encontrada con símbolo: " + simbolo));

        Suscripcion suscripcion = (Suscripcion) suscripcionRepository.findByUsuarioIdAndMoneda(usuarioId, moneda)
                .orElseThrow(() -> new RuntimeException("No existe suscripción para este usuario y moneda"));

        suscripcionRepository.delete(suscripcion);
    }
}