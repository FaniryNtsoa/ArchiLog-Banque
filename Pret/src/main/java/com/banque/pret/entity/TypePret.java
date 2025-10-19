package com.banque.pret.entity;

import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant un type de prêt
 */
@Entity
@Table(name = "type_pret")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypePret implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_type_pret")
    private Long idTypePret;

    @Column(name = "code_type", unique = true, nullable = false, length = 20)
    @NotBlank(message = "Le code type est obligatoire")
    private String codeType;

    @Column(name = "libelle", nullable = false, length = 100)
    @NotBlank(message = "Le libellé est obligatoire")
    private String libelle;

    @Column(name = "taux_interet_annuel", nullable = false, precision = 6, scale = 4)
    @NotNull(message = "Le taux d'intérêt est obligatoire")
    @DecimalMin(value = "0.0", message = "Le taux d'intérêt doit être positif")
    private BigDecimal tauxInteretAnnuel;

    @Column(name = "duree_min", nullable = false)
    @NotNull(message = "La durée minimale est obligatoire")
    @Min(value = 1, message = "La durée minimale doit être au moins 1 mois")
    private Integer dureeMin;

    @Column(name = "duree_max", nullable = false)
    @NotNull(message = "La durée maximale est obligatoire")
    private Integer dureeMax;

    @Column(name = "montant_min", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant minimal est obligatoire")
    @DecimalMin(value = "0.0", message = "Le montant minimal doit être positif")
    private BigDecimal montantMin;

    @Column(name = "montant_max", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant maximal est obligatoire")
    private BigDecimal montantMax;

    @Column(name = "frais_dossier", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal fraisDossier = BigDecimal.ZERO;

    @Column(name = "penalite_retard_taux", precision = 5, scale = 4)
    @Builder.Default
    private BigDecimal penaliteRetardTaux = BigDecimal.ZERO;

    @Column(name = "delai_tolerance_jours")
    @Builder.Default
    private Integer delaiToleranceJours = 5;

    @Column(name = "actif", nullable = false)
    @Builder.Default
    private Boolean actif = true;

    @Column(name = "date_creation")
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @OneToMany(mappedBy = "typePret", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Pret> prets = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
    }
}
