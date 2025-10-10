package com.banque.situationbancaire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO pour une opération bancaire
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperationDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long idMouvement;
    private String numeroCompte;
    private String typeOperation;
    private BigDecimal montant;
    private BigDecimal soldeAvant;
    private BigDecimal soldeApres;
    private LocalDateTime dateOperation;
    private String reference;
    private String libelle;
}
