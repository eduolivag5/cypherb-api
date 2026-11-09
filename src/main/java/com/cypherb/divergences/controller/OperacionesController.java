package com.cypherb.divergences.controller;

import com.cypherb.divergences.dto.RespuestaListadoDTO;
import com.cypherb.divergences.model.Operaciones;
import com.cypherb.divergences.service.OperacionesService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/operaciones")
@RequiredArgsConstructor
public class OperacionesController {

    private final OperacionesService operacionesService;

    /** Abre una operación. */
    @PostMapping
    public ResponseEntity<Operaciones> abrir(@RequestBody Operaciones operacion) {
        return ResponseEntity.status(HttpStatus.CREATED).body(operacionesService.abrir(operacion));
    }

    /** Cierra una operación abierta y calcula su resultado. */
    @PutMapping("/{id}")
    public Operaciones cerrar(@PathVariable Long id, @RequestBody Operaciones operacion) {
        return operacionesService.cerrar(id, operacion);
    }

    /** Lista todas las operaciones, o solo las de un estado (?estado=ABIERTA). */
    @GetMapping
    public RespuestaListadoDTO<Operaciones> listar(@RequestParam(required = false) Operaciones.Estado estado) {
        return RespuestaListadoDTO.de(operacionesService.listar(estado));
    }

    @GetMapping("/{id}")
    public Operaciones obtener(@PathVariable Long id) {
        return operacionesService.obtener(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        operacionesService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}