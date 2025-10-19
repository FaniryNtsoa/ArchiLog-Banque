package com.banque.situationbancaire.entity;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant un mouvement bancaire
 */
@Entity
@Table(name = "mouvement", indexes = {
    @Index(name = "idx_mouvement_compte_date", columnList = "id_compte,date_operation"),
    @Index(name = "idx_mouvement_reference", columnList = "reference")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mouvement implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mouvement")
    private Long idMouvement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compte", nullable = false)
    @NotNull(message = "Le compte est obligatoire")
    @ToString.Exclude
    private CompteCourant compte;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_operation", nullable = false)
    @NotNull(message = "Le type d'opération est obligatoire")
    @ToString.Exclude
    private TypeOperation typeOperation;

    @Column(name = "montant", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant est obligatoire")
    private BigDecimal montant;

    @Column(name = "solde_avant_operation", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le solde avant opération est obligatoire")
    private BigDecimal soldeAvantOperation;

    @Column(name = "solde_apres_operation", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le solde après opération est obligatoire")
    private BigDecimal soldeApresOperation;

    @Column(name = "date_operation", nullable = false)
    @Builder.Default
    private LocalDateTime dateOperation = LocalDateTime.now();

    @Column(name = "reference", length = 100)
    private String reference;

    @Column(name = "libelle_operation", length = 255)
    private String libelleOperation;

    @OneToOne(mappedBy = "mouvementDebit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Virement virementEmis;

    @OneToOne(mappedBy = "mouvementCredit", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Virement virementRecu;
}
