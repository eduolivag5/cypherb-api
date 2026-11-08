package com.cypherb.divergences.repository;

import com.cypherb.divergences.model.Operaciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OperacionesRepository extends JpaRepository<Operaciones, Long> {

    List<Operaciones> findAllByOrderByFechaAperturaDesc();

    List<Operaciones> findByEstadoOrderByFechaAperturaDesc(Operaciones.Estado estado);
}