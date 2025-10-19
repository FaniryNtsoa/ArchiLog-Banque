package com.banque.pret.dto;

import com.banque.pret.entity.enums.StatutPret;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour l'entité Pret
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PretDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long idPret;
    private Long idClient;
    private Long idTypePret;
    private String numeroPret;
    private BigDecimal montantDemande;
    private BigDecimal montantAccorde;
    private Integer dureeMois;
    private BigDecimal tauxInteretAnnuel;
    private BigDecimal montantTotalDu;
    private BigDecimal mensualite;
    private BigDecimal totalPenalites;
    private LocalDate dateDemande;
    private LocalDate dateApprobation;
    private LocalDate datePremiereEcheance;
    private LocalDate dateDerniereEcheance;
    private StatutPret statut;
    private String motifRefus;
    private LocalDateTime dateCreation;
    
    // Informations du client (pour affichage)
    private String nomClient;
    private String prenomClient;
    private String emailClient;
    
    // Informations du type de prêt (pour affichage)
    private String libelleTypePret;
}
