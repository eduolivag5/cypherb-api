package com.cypherb.divergences.controller;

import com.cypherb.divergences.model.Monedas;
import com.cypherb.divergences.service.MonedasService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monedas")
@RequiredArgsConstructor
public class MonedasController {

    private final MonedasService monedasService;

    @GetMapping
    public List getAllMonedas() {
        return monedasService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity getMonedaById(@PathVariable Long id) {
        return (ResponseEntity) monedasService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity createMoneda(@RequestBody Monedas moneda) {
        Monedas resultado = monedasService.guardarOObtener(moneda);
        return ResponseEntity.ok(resultado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteMoneda(@PathVariable Long id) {
        if (monedasService.findById(id).isPresent()) {
            monedasService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}