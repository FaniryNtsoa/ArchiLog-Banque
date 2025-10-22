package com.banque.situationbancaire.service;

import com.banque.situationbancaire.session.UserSessionBean;
import jakarta.ejb.Stateless;

/**
 * Service de vérification des autorisations
 */
@Stateless
public class AuthorizationService {

    /**
     * Vérifie si l'utilisateur a la permission d'effectuer une action
     */
    public boolean checkPermission(UserSessionBean userSession, String nomTable, String action) {
        if (userSession == null || !userSession.isValid()) {
            return false;
        }
        return userSession.hasPermission(nomTable, action);
    }

    /**
     * Vérifie si l'utilisateur est dans une direction spécifique
     */
    public boolean checkDirection(UserSessionBean userSession, Integer idDirection) {
        if (userSession == null || !userSession.isValid()) {
            return false;
        }
        return userSession.isInDirection(idDirection);
    }

    /**
     * Vérifie si l'utilisateur a un rôle spécifique
     */
    public boolean hasRole(UserSessionBean userSession, Integer role) {
        if (userSession == null || !userSession.isValid()) {
            return false;
        }
        return userSession.getUserRole().equals(role);
    }
}
