package com.banque.centralisateur.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO générique pour les réponses du centralisateur
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean succes;
    private String message;
    private Object donnees;
    private String codeErreur;

    public ReponseDTO(boolean succes, String message) {
        this.succes = succes;
        this.message = message;
    }

    public ReponseDTO(boolean succes, String message, Object donnees) {
        this.succes = succes;
        this.message = message;
        this.donnees = donnees;
    }

    public static ReponseDTO succes(String message, Object donnees) {
        return new ReponseDTO(true, message, donnees);
    }

    public static ReponseDTO succes(String message) {
        return new ReponseDTO(true, message);
    }

    public static ReponseDTO erreur(String message, String codeErreur) {
        ReponseDTO reponse = new ReponseDTO(false, message);
        reponse.setCodeErreur(codeErreur);
        return reponse;
    }
}
