package com.banque.situationbancaire.dto;

import lombok.*;
import java.io.Serializable;

/**
 * DTO pour la requête de connexion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String login;
    private String motDePasse;
}
