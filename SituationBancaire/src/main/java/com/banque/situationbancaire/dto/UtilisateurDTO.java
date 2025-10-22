package com.banque.situationbancaire.dto;

import lombok.*;
import java.io.Serializable;

/**
 * DTO pour l'Utilisateur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UtilisateurDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Integer idUtilisateur;
    private String loginUtilisateur;
    private String motDePasse; // Ne pas renvoyer en production
    private Integer idDirection;
    private Integer niveauDirection;
    private Integer roleUtilisateur;
}
