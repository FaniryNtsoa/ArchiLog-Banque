package com.banque.situationbancaire.service;

import com.banque.situationbancaire.ejb.remote.UserSessionBeanRemote;
import jakarta.ejb.Stateless;

/**
 * Service de vérification des autorisations
 */
@Stateless
public class AuthorizationService {

    /**
     * Vérifie si l'utilisateur a la permission d'effectuer une action
     */
    public boolean checkPermission(UserSessionBeanRemote userSession, String nomTable, String action) {
        if (userSession == null || !userSession.isValid()) {
            return false;
        }
        return userSession.hasPermission(nomTable, action);
    }

    /**
     * Vérifie si l'utilisateur est dans une direction spécifique
     */
    public boolean checkDirection(UserSessionBeanRemote userSession, Integer idDirection) {
        if (userSession == null || !userSession.isValid()) {
            return false;
        }
        return userSession.isInDirection(idDirection);
    }

    /**
     * Vérifie si l'utilisateur a un rôle spécifique
     */
    public boolean hasRole(UserSessionBeanRemote userSession, Integer role) {
        if (userSession == null || !userSession.isValid()) {
            return false;
        }
        return userSession.getUserRole().equals(role);
    }
}
