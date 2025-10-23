package com.banque.situationbancaire.session;

import com.banque.situationbancaire.entity.ActionRole;
import com.banque.situationbancaire.entity.Direction;
import com.banque.situationbancaire.entity.Utilisateur;
import com.banque.situationbancaire.mapper.UtilisateurMapper;
import com.banque.situationbancaire.repository.UtilisateurRepository;
import com.banque.situationbancaire.dto.UtilisateurDTO;
import com.banque.situationbancaire.ejb.impl.ClientServiceImpl;
import com.banque.situationbancaire.ejb.remote.UserSessionBeanRemote;
import lombok.Getter;
import lombok.Setter;

import jakarta.ejb.Stateful;
import jakarta.inject.Inject;
import jakarta.ejb.Remove;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Session Bean Stateful pour stocker les informations de session utilisateur
 * Cette session est créée lors de la connexion et détruite lors de la déconnexion
 */
@Stateful
@Getter
@Setter
public class UserSessionBean implements UserSessionBeanRemote, Serializable {
    
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logger.getLogger(ClientServiceImpl.class.getName());

    @Inject
    private UtilisateurRepository utilisateurRepository;

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
    // public void initSession(Utilisateur user, List<Direction> dirs, List<ActionRole> actions) {
    //     this.sessionId = UUID.randomUUID().toString();
    //     this.utilisateur = user;
    //     this.directions = dirs != null ? new ArrayList<>(dirs) : new ArrayList<>();
    //     this.actionsAutorisees = actions != null ? new ArrayList<>(actions) : new ArrayList<>();
    // }

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

    public UtilisateurDTO authentifierUtilisateur(String email, String motDePasse) {
        // Cette méthode serait implémentée pour authentifier l'utilisateur
        // et retourner un UtilisateurDTO. Pour l'instant, elle retourne null.
        LOGGER.info("Authentification de l'utilisateur avec email : " + email);
        // Logique d'authentification à implémenter ici
        Optional<Utilisateur> userOpt = utilisateurRepository.findByLogin(email);
        if(userOpt.isEmpty()) {
            return null; // Utilisateur non trouvé
        }
        Utilisateur user = userOpt.get();
        if(!user.getMotDePasse().equals(hashPassword(motDePasse))) {
            return null; // Mot de passe incorrect
        }
        return UtilisateurMapper.toDTO(user);
    }

    private String hashPassword(String motDePasse) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(motDePasse.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }
}
