package com.banque.pret.entity;

import com.banque.pret.entity.enums.StatutEcheance;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entité représentant une échéance de prêt
 */
@Entity
@Table(name = "echeance", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"id_pret", "numero_echeance"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Echeance implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_echeance")
    private Long idEcheance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pret", nullable = false)
    @NotNull(message = "Le prêt est obligatoire")
    @ToString.Exclude
    private Pret pret;

    @Column(name = "numero_echeance", nullable = false)
    @NotNull(message = "Le numéro d'échéance est obligatoire")
    @Min(value = 1, message = "Le numéro d'échéance doit être au moins 1")
    private Integer numeroEcheance;

    @Column(name = "montant_echeance", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant de l'échéance est obligatoire")
    private BigDecimal montantEcheance;

    @Column(name = "capital", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le capital est obligatoire")
    private BigDecimal capital;

    @Column(name = "interet", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "L'intérêt est obligatoire")
    private BigDecimal interet;

    @Column(name = "capital_restant", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le capital restant est obligatoire")
    private BigDecimal capitalRestant;

    @Column(name = "date_echeance", nullable = false)
    @NotNull(message = "La date d'échéance est obligatoire")
    private LocalDate dateEcheance;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    @Builder.Default
    private StatutEcheance statut = StatutEcheance.A_VENIR;

    @Column(name = "penalite_appliquee", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal penaliteAppliquee = BigDecimal.ZERO;

    @Column(name = "jours_retard")
    @Builder.Default
    private Integer joursRetard = 0;

    @Column(name = "date_calcul_penalite")
    private LocalDate dateCalculPenalite;

    @OneToMany(mappedBy = "echeance", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @Builder.Default
    private List<Remboursement> remboursements = new ArrayList<>();
}
