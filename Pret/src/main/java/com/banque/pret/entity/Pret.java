package com.banque.pret.entity;

import com.banque.pret.entity.enums.StatutPret;
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
 * Entité représentant un prêt bancaire
 */
@Entity
@Table(name = "pret")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pret implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pret")
    private Long idPret;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_client", nullable = false)
    @NotNull(message = "Le client est obligatoire")
    @ToString.Exclude
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_type_pret", nullable = false)
    @NotNull(message = "Le type de prêt est obligatoire")
    @ToString.Exclude
    private TypePret typePret;

    @Column(name = "numero_pret", unique = true, nullable = false, length = 30)
    private String numeroPret;

    @Column(name = "montant_demande", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant demandé est obligatoire")
    @DecimalMin(value = "0.0", message = "Le montant demandé doit être positif")
    private BigDecimal montantDemande;

    @Column(name = "montant_accorde", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant accordé est obligatoire")
    private BigDecimal montantAccorde;

    @Column(name = "duree_mois", nullable = false)
    @NotNull(message = "La durée est obligatoire")
    @Min(value = 1, message = "La durée doit être au moins 1 mois")
    private Integer dureeMois;

    @Column(name = "taux_interet_annuel", nullable = false, precision = 6, scale = 4)
    @NotNull(message = "Le taux d'intérêt est obligatoire")
    private BigDecimal tauxInteretAnnuel;

    @Column(name = "montant_total_du", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant total dû est obligatoire")
    private BigDecimal montantTotalDu;

    @Column(name = "mensualite", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "La mensualité est obligatoire")
    private BigDecimal mensualite;

    @Column(name = "total_penalites", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal totalPenalites = BigDecimal.ZERO;

    @Column(name = "date_demande", nullable = false)
    @Builder.Default
    private LocalDate dateDemande = LocalDate.now();

    @Column(name = "date_approbation")
    private LocalDate dateApprobation;

    @Column(name = "date_premiere_echeance", nullable = false)
    @NotNull(message = "La date de première échéance est obligatoire")
    private LocalDate datePremiereEcheance;

    @Column(name = "date_derniere_echeance", nullable = false)
    @NotNull(message = "La date de dernière échéance est obligatoire")
    private LocalDate dateDerniereEcheance;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    @Builder.Default
    private StatutPret statut = StatutPret.EN_ATTENTE;

    @Column(name = "motif_refus", columnDefinition = "TEXT")
    private String motifRefus;

    @Column(name = "date_creation")
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    // ===== TRAÇABILITÉ ADMIN =====
    @Column(name = "id_administrateur")
    private Long idAdministrateur;

    @OneToMany(mappedBy = "pret", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @OrderBy("numeroEcheance ASC")
    @Builder.Default
    private List<Echeance> echeances = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.numeroPret == null || this.numeroPret.isEmpty()) {
            this.numeroPret = generateNumeroPret();
        }
        this.dateCreation = LocalDateTime.now();
    }

    /**
     * Génère un numéro de prêt automatiquement
     * Format: PRET + timestamp + 4 chiffres aléatoires
     */
    private String generateNumeroPret() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 9999);
        return String.format("PRET%d%04d", timestamp, random);
    }
}
