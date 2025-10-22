package com.banque.situationbancaire.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les mouvements bancaires
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouvementDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idMouvement;
    private Long idCompte;
    private String numeroCompte;
    private Long idTypeOperation;
    private BigDecimal montant;
    private BigDecimal soldeAvantOperation;
    private BigDecimal soldeApresOperation;
    private LocalDateTime dateOperation;
    private String reference;
    private String libelleOperation;
    
    // Anciens champs pour compatibilité
    private String typeOperation;
    private String natureOperation;
    private String categorieOperation;
    private String libelle;
    private LocalDateTime dateValeur;
    private BigDecimal soldeAvant;
    private BigDecimal soldeApres;
    private String numeroCompteBeneficiaire;
    private String nomBeneficiaire;
    private String statut;
}