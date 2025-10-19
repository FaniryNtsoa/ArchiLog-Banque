package com.banque.pret.entity;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité représentant les paramètres de pénalités (pour extension future)
 */
@Entity
@Table(name = "parametres_penalites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametresPenalites implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_parametre")
    private Long idParametre;

    @Column(name = "type_penalite", nullable = false, length = 50)
    @NotBlank(message = "Le type de pénalité est obligatoire")
    private String typePenalite;

    @Column(name = "taux_penalite", nullable = false, precision = 5, scale = 4)
    @NotNull(message = "Le taux de pénalité est obligatoire")
    private BigDecimal tauxPenalite;

    @Column(name = "delai_tolerance_jours", nullable = false)
    @NotNull(message = "Le délai de tolérance est obligatoire")
    private Integer delaiToleranceJours;

    @Column(name = "montant_min_penalite", precision = 10, scale = 2)
    private BigDecimal montantMinPenalite;

    @Column(name = "montant_max_penalite", precision = 10, scale = 2)
    private BigDecimal montantMaxPenalite;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private Boolean actif = false;

    @Column(name = "date_debut")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "date_creation")
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
