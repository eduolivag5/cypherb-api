package com.cypherb.divergences.controller;

import com.cypherb.divergences.model.Entradas;
import com.cypherb.divergences.service.EntradasService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/entradas")
@RequiredArgsConstructor
public class EntradasController {

    private final EntradasService entradasService;

    public record EntradaRequestDTO(
            String simbolo,
            BigDecimal precio,
            Entradas.TipoEntrada tipo,
            LocalDate fechaCreacion
    ) {}

    @GetMapping
    public ResponseEntity obtenerEntradas(
            @RequestParam(required = false) String simbolo,
            @RequestParam(required = false) Entradas.TipoEntrada tipo,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) LocalDate fechaInicio,
            @RequestParam(required = false) LocalDate fechaFin
    ) {
        List lista = entradasService.listarEntradasFiltradas(
                simbolo, tipo, precioMin, precioMax, fechaInicio, fechaFin
        );
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    public ResponseEntity crearEntrada(@RequestBody @Valid EntradaRequestDTO dto) {
        Entradas entrada = entradasService.crearEntrada(
                dto.simbolo(),
                dto.precio(),
                dto.tipo(),
                dto.fechaCreacion()
        );
        return ResponseEntity.ok(entrada);
    }
}