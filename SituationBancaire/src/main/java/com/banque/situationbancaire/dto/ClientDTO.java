package com.banque.situationbancaire.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour la création d'un client
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long idClient;
    private String numeroClient;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String numCin;
    private String email;
    private String telephone;
    private String adresse;
    private String codePostal;
    private String ville;
    private String profession;
    private BigDecimal revenuMensuel;
    private BigDecimal soldeInitial;
    private String situationFamiliale;
    private String statut;
}
