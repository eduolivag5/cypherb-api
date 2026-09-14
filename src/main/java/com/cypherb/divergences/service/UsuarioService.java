package com.cypherb.divergences.service;

import com.cypherb.divergences.model.Usuario;
import com.cypherb.divergences.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public List findAll() {
        return usuarioRepository.findAll();
    }

    public Optional findById(Long id) {
        return usuarioRepository.findById(id);
    }

    public Optional findByChatId(Long chatId) {
        return usuarioRepository.findByChatId(chatId);
    }

    public Usuario saveOrUpdateByChatId(Usuario usuarioRequest) {
        return usuarioRepository.findByChatId(usuarioRequest.getChatId())
                .map(usuarioExistente -> {
                    usuarioExistente.setUsername(usuarioRequest.getUsername());
                    usuarioExistente.setFirstName(usuarioRequest.getFirstName());
                    usuarioExistente.setLastName(usuarioRequest.getLastName());
                    return usuarioRepository.save(usuarioExistente);
                })
                .orElseGet(() -> usuarioRepository.save(usuarioRequest));
    }

    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }
}