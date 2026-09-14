package com.cypherb.divergences.service;

import com.cypherb.divergences.model.Monedas;
import com.cypherb.divergences.repository.MonedasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonedasService {

    private final MonedasRepository monedasRepository;

    public List findAll() {
        return monedasRepository.findAll();
    }

    public Optional findById(Long id) {
        return monedasRepository.findById(id);
    }

    @Transactional
    public Monedas guardarOObtener(Monedas moneda) {
        // Buscar por símbolo o por par completo
        return monedasRepository.findBySimbolo(moneda.getSimbolo().toUpperCase())
                .orElseGet(() -> monedasRepository.findByParCompleto(moneda.getParCompleto().toUpperCase())
                        .orElseGet(() -> monedasRepository.save(moneda)));
    }

    public void deleteById(Long id) {
        monedasRepository.deleteById(id);
    }
}