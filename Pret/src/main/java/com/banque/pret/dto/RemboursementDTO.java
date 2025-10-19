package com.banque.pret.dto;

import com.banque.pret.entity.enums.TypePaiement;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour l'entité Remboursement
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemboursementDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long idRemboursement;
    private Long idEcheance;
    private Long idPret; // AJOUTÉ pour PHASE 5
    private Long idCompte;
    private BigDecimal montant;
    private BigDecimal montantPaye; // AJOUTÉ pour PHASE 5
    private BigDecimal montantEcheance;
    private BigDecimal montantPenalite;
    private BigDecimal penalites; // Alias pour montantPenalite
    private LocalDateTime datePaiement;
    private TypePaiement typePaiement=TypePaiement.ESPECES;
    private String numeroTransaction; // AJOUTÉ pour PHASE 5
}
