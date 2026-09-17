package com.cypherb.divergences.repository;

import com.cypherb.divergences.model.Suscripcion;
import com.cypherb.divergences.model.Monedas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SuscripcionRepository extends JpaRepository<Suscripcion, Long> {

    // Añadido <Suscripcion>
    List<Suscripcion> findByUsuarioId(Long usuarioId);

    // Añadido <Suscripcion>
    List<Suscripcion> findByMoneda(Monedas moneda);

    // Añadido <Suscripcion>
    Optional<Suscripcion> findByUsuarioIdAndMoneda(Long usuarioId, Monedas moneda);

    boolean existsByUsuarioIdAndMoneda(Long usuarioId, Monedas moneda);
}