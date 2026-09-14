package com.cypherb.divergences.repository;

import com.cypherb.divergences.model.Entradas;
import com.cypherb.divergences.model.Monedas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntradasRepository extends JpaRepository<Entradas, Long>, JpaSpecificationExecutor {
    List<Entradas> findByMonedaId(Long monedaId);
    List<Entradas> findByTipo(Entradas.TipoEntrada tipo);
    Optional findByMonedaAndTipoAndFechaCreacion(Monedas moneda, Entradas.TipoEntrada tipo, LocalDate fechaCreacion);
}