package com.cypherb.divergences.repository;

import com.cypherb.divergences.model.Monedas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MonedasRepository extends JpaRepository<Monedas, Long> {
    Optional<Monedas> findBySimbolo(String simbolo);
    Optional<Monedas> findByParCompleto(String parCompleto);
}