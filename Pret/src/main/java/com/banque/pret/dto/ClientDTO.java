package com.banque.pret.dto;

import com.banque.pret.entity.enums.SituationFamiliale;
import com.banque.pret.entity.enums.StatutClient;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO pour l'entité Client
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
    private SituationFamiliale situationFamiliale;
    private String motDePasse;
    private StatutClient statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
}
