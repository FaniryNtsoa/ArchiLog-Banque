package com.banque.pret.entity;

import com.banque.pret.entity.enums.TypePaiement;
import lombok.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entité représentant un remboursement d'échéance
 */
@Entity
@Table(name = "remboursement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Remboursement implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_remboursement")
    private Long idRemboursement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_echeance", nullable = false)
    @NotNull(message = "L'échéance est obligatoire")
    @ToString.Exclude
    private Echeance echeance;

    @Column(name = "id_compte")
    private Long idCompte;

    @Column(name = "montant", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.0", message = "Le montant doit être positif")
    private BigDecimal montant;

    @Column(name = "montant_echeance", nullable = false, precision = 15, scale = 2)
    @NotNull(message = "Le montant de l'échéance est obligatoire")
    private BigDecimal montantEcheance;

    @Column(name = "montant_penalite", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montantPenalite = BigDecimal.ZERO;

    @Column(name = "numero_transaction", length = 100)
    private String numeroTransaction;

    @Column(name = "date_paiement")
    @Builder.Default
    private LocalDateTime datePaiement = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "type_paiement", nullable = false)
    @Builder.Default
    private TypePaiement typePaiement = TypePaiement.VIREMENT;

    // ===== TRAÇABILITÉ ADMIN =====
    @Column(name = "id_administrateur")
    private Long idAdministrateur;

    @PrePersist
    protected void onCreate() {
        this.datePaiement = LocalDateTime.now();
    }
}
