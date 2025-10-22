package com.banque.situationbancaire.session;

import com.banque.situationbancaire.entity.ActionRole;
import com.banque.situationbancaire.entity.Direction;
import com.banque.situationbancaire.entity.Utilisateur;
import com.banque.situationbancaire.ejb.remote.UserSessionBeanRemote;
import lombok.Getter;
import lombok.Setter;

import jakarta.ejb.Stateful;
import jakarta.ejb.Remove;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Session Bean Stateful pour stocker les informations de session utilisateur
 * Cette session est créée lors de la connexion et détruite lors de la déconnexion
 */
@Stateful
@Getter
@Setter
public class UserSessionBean implements UserSessionBeanRemote, Serializable {
    
    private static final long serialVersionUID = 1L;

    // Identifiant unique de session
    private String sessionId;
    
    // Utilisateur connecté
    private Utilisateur utilisateur;
    
    // Liste des directions (si nécessaire pour vérification)
    private List<Direction> directions;
    
    // Liste des permissions (actions autorisées pour le rôle de l'utilisateur)
    private List<ActionRole> actionsAutorisees;
    
    /**
     * Initialise la session utilisateur
     */
    public void initSession(Utilisateur user, List<Direction> dirs, List<ActionRole> actions) {
        this.sessionId = UUID.randomUUID().toString();
        this.utilisateur = user;
        this.directions = dirs != null ? new ArrayList<>(dirs) : new ArrayList<>();
        this.actionsAutorisees = actions != null ? new ArrayList<>(actions) : new ArrayList<>();
    }

    /**
     * Vérifie si l'utilisateur a la permission d'effectuer une action sur une table
     */
    public boolean hasPermission(String nomTable, String action) {
        if (actionsAutorisees == null || actionsAutorisees.isEmpty()) {
            return false;
        }
        
        return actionsAutorisees.stream()
                .anyMatch(ar -> 
                    ar.getNomTable().equalsIgnoreCase(nomTable) && 
                    ar.getActionAutorisee().equalsIgnoreCase(action) &&
                    ar.getRoleRequis().equals(utilisateur.getRoleUtilisateur())
                );
    }

    /**
     * Vérifie si l'utilisateur appartient à une direction spécifique
     */
    public boolean isInDirection(Integer idDirection) {
        if (utilisateur == null || utilisateur.getDirection() == null) {
            return false;
        }
        return utilisateur.getDirection().getIdDirection().equals(idDirection);
    }

    /**
     * Récupère le rôle de l'utilisateur
     */
    public Integer getUserRole() {
        return utilisateur != null ? utilisateur.getRoleUtilisateur() : null;
    }

    /**
     * Récupère l'ID de la direction de l'utilisateur
     */
    public Integer getUserDirectionId() {
        if (utilisateur != null && utilisateur.getDirection() != null) {
            return utilisateur.getDirection().getIdDirection();
        }
        return null;
    }

    /**
     * Méthode appelée lors de la déconnexion pour détruire la session
     */
    @Remove
    public void destroy() {
        this.utilisateur = null;
        this.directions = null;
        this.actionsAutorisees = null;
        this.sessionId = null;
    }

    /**
     * Vérifie si la session est valide
     */
    public boolean isValid() {
        return this.utilisateur != null && this.sessionId != null;
    }
}
