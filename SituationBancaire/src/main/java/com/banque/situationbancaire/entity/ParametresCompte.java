package com.banque.situationbancaire.entity;

import com.banque.situationbancaire.entity.enums.PeriodiciteFreais;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant les paramètres d'un compte (avec historique)
 */
@Entity
@Table(name = "parametres_compte")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametresCompte implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametre")
    private Long idParametre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_taux_decouvert", nullable = false)
    @NotNull(message = "Le taux de découvert est obligatoire")
    @ToString.Exclude
    private TauxInteretDecouvert tauxDecouvert;

    @Column(name = "plafond_retrait_journalier", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le plafond de retrait journalier est obligatoire")
    @DecimalMin(value = "0.0", message = "Le plafond de retrait doit être positif")
    private BigDecimal plafondRetraitJournalier;

    @Column(name = "plafond_virement_journalier", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le plafond de virement journalier est obligatoire")
    @DecimalMin(value = "0.0", message = "Le plafond de virement doit être positif")
    private BigDecimal plafondVirementJournalier;

    @Column(name = "montant_decouvert_autorise", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal montantDecouvertAutorise = BigDecimal.ZERO;

    @Column(name = "frais_tenue_compte", nullable = false, precision = 10, scale = 2)
    @NotNull(message = "Les frais de tenue de compte sont obligatoires")
    @DecimalMin(value = "0.0", message = "Les frais doivent être positifs")
    private BigDecimal fraisTenueCompte;

    @Enumerated(EnumType.STRING)
    @Column(name = "frais_tenue_compte_periodicite", nullable = false)
    @Builder.Default
    private PeriodiciteFreais fraisTenueComptePeriodicite = PeriodiciteFreais.MENSUEL;

    @Column(name = "date_debut", nullable = false)
    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "date_creation", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @OneToMany(mappedBy = "parametreActuel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<TypeCompte> typesCompte = new ArrayList<>();
}
