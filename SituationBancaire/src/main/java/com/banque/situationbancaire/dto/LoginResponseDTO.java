package com.banque.situationbancaire.dto;

import lombok.*;
import java.io.Serializable;
import java.util.List;

/**
 * DTO pour la réponse de connexion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean success;
    private String message;
    private UtilisateurDTO utilisateur;
    private String sessionId; // Identifiant de session unique
}
