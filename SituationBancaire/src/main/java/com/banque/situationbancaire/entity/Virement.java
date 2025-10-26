package com.banque.situationbancaire.entity;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant un virement entre deux comptes
 */
@Entity
@Table(name = "virement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Virement implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_virement")
    private Long idVirement;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mouvement_debit", nullable = false)
    @NotNull(message = "Le mouvement de débit est obligatoire")
    @ToString.Exclude
    private Mouvement mouvementDebit;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mouvement_credit", nullable = false)
    @NotNull(message = "Le mouvement de crédit est obligatoire")
    @ToString.Exclude
    private Mouvement mouvementCredit;

    @Column(name = "montant", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le montant doit être positif")
    private BigDecimal montant;

    @Column(name = "date_virement", nullable = false)
    @Builder.Default
    private LocalDateTime dateVirement = LocalDateTime.now();

    @Column(name = "id_administrateur")
    private Integer idAdministrateur;
}
