package com.banque.situationbancaire.entity;

import com.banque.situationbancaire.entity.enums.StatutDecouvert;
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
 * Entité représentant un découvert bancaire et sa gestion
 */
@Entity
@Table(name = "decouvert")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Decouvert implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_decouvert")
    private Long idDecouvert;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_compte", nullable = false)
    @NotNull(message = "Le compte est obligatoire")
    @ToString.Exclude
    private CompteCourant compte;

    @Column(name = "montant_decouvert", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant du découvert est obligatoire")
    @DecimalMin(value = "0.0", message = "Le montant doit être positif")
    private BigDecimal montantDecouvert;

    @Column(name = "montant_autorise", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant autorisé est obligatoire")
    @DecimalMin(value = "0.0", message = "Le montant autorisé doit être positif")
    private BigDecimal montantAutorise;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    @Builder.Default
    private StatutDecouvert statut = StatutDecouvert.ACTIF;

    @Column(name = "date_debut", nullable = false)
    @NotNull(message = "La date de début est obligatoire")
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "date_creation", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "date_modification")
    @Builder.Default
    private LocalDateTime dateModification = LocalDateTime.now();

    @Column(name = "motif_fermeture", length = 255)
    private String motifFermeture;

    @OneToMany(mappedBy = "decouvert", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<InteretJournalier> interetsJournaliers = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    /**
     * Calcule le total des intérêts accumulés
     * @return Le total des intérêts journaliers
     */
    public BigDecimal calculerTotalInterets() {
        return interetsJournaliers.stream()
                .map(InteretJournalier::getInteretJour)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Vérifie si le découvert est encore dans les limites autorisées
     * @return true si le découvert est dans les limites, false sinon
     */
    public boolean estDansLesLimites() {
        return montantDecouvert.compareTo(montantAutorise) <= 0;
    }

    /**
     * Calcule le montant dépassant l'autorisation de découvert
     * @return Le montant de dépassement, 0 si pas de dépassement
     */
    public BigDecimal calculerDepassement() {
        if (montantDecouvert.compareTo(montantAutorise) > 0) {
            return montantDecouvert.subtract(montantAutorise);
        }
        return BigDecimal.ZERO;
    }
}