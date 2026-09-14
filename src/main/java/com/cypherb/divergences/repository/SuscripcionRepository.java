package com.cypherb.divergences.repository;

import com.cypherb.divergences.model.Suscripcion;
import com.cypherb.divergences.model.Monedas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    List findByUsuarioId(Long usuarioId);

    List findByMoneda(Monedas moneda);

    Optional findByUsuarioIdAndMoneda(Long usuarioId, Monedas moneda);

    boolean existsByUsuarioIdAndMoneda(Long usuarioId, Monedas moneda);
}