package com.cypherb.divergences.controller;

import com.cypherb.divergences.dto.SuscripcionRequestDTO;
import com.cypherb.divergences.model.Suscripcion;
import com.cypherb.divergences.service.SuscripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/suscripciones")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @GetMapping
    public ResponseEntity listarSuscripciones(
            @RequestParam(required = false) Long usuarioId
    ) {
        if (usuarioId != null) {
            return ResponseEntity.ok(suscripcionService.obtenerPorUsuario(usuarioId));
        }
        return ResponseEntity.ok(suscripcionService.obtenerTodas());
    }

    // POST: Crear una nueva suscripción
    @PostMapping
    public ResponseEntity crearSuscripcion(@RequestBody @Valid SuscripcionRequestDTO dto) {
        Suscripcion nueva = suscripcionService.crearSuscripcion(dto.usuarioId(), dto.simbolo());
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    // DELETE: Borrar una suscripción indicando usuarioId y simbolo en parámetros o body
    @DeleteMapping
    public ResponseEntity eliminarSuscripcion(
            @RequestParam Long usuarioId,
            @RequestParam String simbolo
    ) throws Throwable {
        suscripcionService.eliminarSuscripcion(usuarioId, simbolo);
        return ResponseEntity.noContent().build();
    }
}