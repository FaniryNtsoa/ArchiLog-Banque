package com.banque.pret.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour l'entité Echeance
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EcheanceDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long idEcheance;
    private Long idPret;
    private Integer numeroEcheance;
    private BigDecimal montantEcheance;
    private BigDecimal capital;
    private BigDecimal interet;
    private BigDecimal capitalRestant;
    private LocalDate dateEcheance;
    private LocalDate datePaiement;
    private String statut;  // String au lieu de l'enum
    private BigDecimal penaliteAppliquee;
    private Integer joursRetard;
    private LocalDate dateCalculPenalite;
}
