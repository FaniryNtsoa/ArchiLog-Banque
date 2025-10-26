package com.banque.situationbancaire.dto;

import lombok.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour les virements
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VirementDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long idVirement;
    private Long idCompteDebit;
    private Long idCompteCredit;
    private String numeroCompteDebit;
    private String numeroCompteCredit;
    
    // Anciens noms pour compatibilité
    private String numeroCompteDebiteur;
    private String numeroCompteCrediteur;
    private BigDecimal montant;
    private String libelle;
    private String reference;
    private LocalDateTime dateVirement;
    private LocalDateTime dateExecution;
    private String statut;
    private String motifRejet;
    private Integer idAdministrateur;
    
    // Informations additionnelles pour l'affichage
    private String nomDebiteur;
    private String nomCrediteur;
}