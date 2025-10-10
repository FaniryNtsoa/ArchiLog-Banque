package com.banque.situationbancaire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour un compte courant
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompteCourantDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long idCompte;
    private Long idClient;
    private String numeroCompte;
    private String libelleCompte;
    private String typeCompte;
    private String devise;
    private String statut;
    private LocalDate dateOuverture;
    private BigDecimal soldeActuel;
}
