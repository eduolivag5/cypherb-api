package com.cypherb.divergences.service;

import com.cypherb.divergences.dto.SuscripcionResponseDTO;
import com.cypherb.divergences.model.Monedas;
import com.cypherb.divergences.model.Suscripcion;
import com.cypherb.divergences.model.Usuario; // Asegúrate de importar tu entidad Usuario
import com.cypherb.divergences.repository.MonedasRepository;
import com.cypherb.divergences.repository.SuscripcionRepository;
import com.cypherb.divergences.repository.UsuarioRepository; // Asegúrate de importar tu repositorio de usuarios
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final MonedasRepository monedasRepository;
    private final UsuarioRepository usuarioRepository; // <-- Inyectamos el repositorio de usuarios

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
    public List<SuscripcionResponseDTO> obtenerTodas() {
        return suscripcionRepository.findAll().stream()
                .map(s -> {
                    // Buscamos el chatId del usuario correspondiente
                    Long chatId = usuarioRepository.findById(s.getUsuarioId())
                            .map(Usuario::getChatId) // Asume que tu entidad Usuario tiene getChatId()
                            .orElse(null);

                    return new SuscripcionResponseDTO(s.getId(), s.getUsuarioId(), chatId, s.getMoneda());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SuscripcionResponseDTO> obtenerPorUsuario(Long usuarioId) {
        // Opcional: si ya sabemos el usuarioId, podríamos buscar el chatId una sola vez fuera del stream,
        // pero hacerlo así mantiene la consistencia del mapper.
        Long chatId = usuarioRepository.findById(usuarioId)
                .map(Usuario::getChatId)
                .orElse(null);

        return suscripcionRepository.findByUsuarioId(usuarioId).stream()
                .map(s -> new SuscripcionResponseDTO(s.getId(), s.getUsuarioId(), chatId, s.getMoneda()))
                .toList();
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