package com.banque.situationbancaire.entity;

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
 * Entité représentant les taux d'intérêt pour les découverts
 */
@Entity
@Table(name = "taux_interet_decouvert")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TauxInteretDecouvert implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_taux")
    private Long idTaux;

    @Column(name = "taux_annuel", nullable = false, precision = 5, scale = 4)
    @NotNull(message = "Le taux annuel est obligatoire")
    @DecimalMin(value = "0.0", message = "Le taux doit être positif")
    private BigDecimal tauxAnnuel;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "date_debut", nullable = false)
    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "date_creation", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @OneToMany(mappedBy = "tauxDecouvert", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<ParametresCompte> parametres = new ArrayList<>();
}
