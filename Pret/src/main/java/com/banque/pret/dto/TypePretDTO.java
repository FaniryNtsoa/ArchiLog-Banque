package com.banque.pret.dto;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour l'entité TypePret
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypePretDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private Long idTypePret;
    private String codeType;
    private String libelle;
    private BigDecimal tauxInteretAnnuel;
    private Integer dureeMin;
    private Integer dureeMax;
    private BigDecimal montantMin;
    private BigDecimal montantMax;
    private BigDecimal fraisDossier;
    private BigDecimal penaliteRetardTaux;
    private Integer delaiToleranceJours;
    private Boolean actif;
    private LocalDateTime dateCreation;
}
