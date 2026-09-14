package com.cypherb.divergences.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "monedas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Monedas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "simbolo", nullable = false, unique = true)
    private String simbolo; // Ejemplo: BTC

    @Column(name = "par_completo", nullable = false, unique = true)
    private String parCompleto; // Ejemplo: BTCUSDT
}