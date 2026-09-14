package com.cypherb.divergences.controller;

import com.cypherb.divergences.model.Usuario;
import com.cypherb.divergences.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public List getAllUsuarios() {
        return usuarioService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity getUsuarioById(@PathVariable Long id) {
        return (ResponseEntity) usuarioService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity getUsuarioByChatId(@PathVariable Long chatId) {
        return (ResponseEntity) usuarioService.findByChatId(chatId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity createOrUpdateUsuario(@RequestBody Usuario usuario) {
        Usuario guardado = usuarioService.saveOrUpdateByChatId(usuario);
        return ResponseEntity.ok(guardado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity deleteUsuario(@PathVariable Long id) {
        if (usuarioService.findById(id).isPresent()) {
            usuarioService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}