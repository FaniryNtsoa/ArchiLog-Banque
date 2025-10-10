package com.banque.situationbancaire.entity;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entité représentant le calcul journalier des intérêts pour un compte
 */
@Entity
@Table(name = "interet_journalier")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InteretJournalier implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_interet")
    private Long idInteret;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compte", nullable = false)
    @NotNull(message = "Le compte est obligatoire")
    @ToString.Exclude
    private CompteCourant compte;

    @Column(name = "date_jour", nullable = false)
    @NotNull(message = "La date est obligatoire")
    private LocalDate dateJour;

    @Column(name = "montant_base", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant de base est obligatoire")
    private BigDecimal montantBase;

    @Column(name = "taux_applique", nullable = false, precision = 5, scale = 4)
    @NotNull(message = "Le taux appliqué est obligatoire")
    private BigDecimal tauxApplique;

    @Column(name = "interet_jour", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "L'intérêt du jour est obligatoire")
    private BigDecimal interetJour;
}
