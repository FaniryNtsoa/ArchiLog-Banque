package com.banque.situationbancaire.entity;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité représentant l'historique des frais de tenue de compte prélevés
 */
@Entity
@Table(name = "frais_tenue_historique")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraisTenueHistorique implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_frais")
    private Long idFrais;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compte", nullable = false)
    @NotNull(message = "Le compte est obligatoire")
    @ToString.Exclude
    private CompteCourant compte;

    @Column(name = "montant_frais", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant des frais est obligatoire")
    @DecimalMin(value = "0.0", message = "Le montant doit être positif")
    private BigDecimal montantFrais;

    @Column(name = "periode_debut", nullable = false)
    @NotNull(message = "La période de début est obligatoire")
    private LocalDate periodeDebut;

    @Column(name = "periode_fin", nullable = false)
    @NotNull(message = "La période de fin est obligatoire")
    private LocalDate periodeFin;

    @Column(name = "date_prelevement", nullable = false)
    @Builder.Default
    private LocalDateTime datePrelevement = LocalDateTime.now();
}
