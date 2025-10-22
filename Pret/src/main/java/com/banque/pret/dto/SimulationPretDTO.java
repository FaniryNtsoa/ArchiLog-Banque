package com.banque.pret.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * DTO pour la simulation de prêt
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationPretDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    // Paramètres de la simulation
    private Long idTypePret; // Ajouté pour récupérer les plafonds
    private BigDecimal montantDemande;
    private Integer dureeMois;
    private BigDecimal tauxInteretAnnuel;
    private BigDecimal fraisDossier;
    private BigDecimal revenuMensuel; // Pour vérifier les 33%
    
    // ===== TRAÇABILITÉ ADMIN =====
    private Long idAdministrateur;
    
    // Résultats de la simulation
    private BigDecimal mensualite;
    private BigDecimal montantTotalDu;
    private BigDecimal coutTotalCredit;
    private BigDecimal totalInterets;
    private BigDecimal totalFrais;
    
    // Tableau d'amortissement
    private List<EcheanceDTO> tableauAmortissement;
}
