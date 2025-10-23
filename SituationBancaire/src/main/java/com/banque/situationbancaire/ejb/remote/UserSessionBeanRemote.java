package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.dto.UtilisateurDTO;
// import com.banque.situationbancaire.entity.ActionRole;
// import com.banque.situationbancaire.entity.Direction;
// import com.banque.situationbancaire.entity.Utilisateur;

import jakarta.ejb.Remote;
import java.util.List;

/**
 * Interface Remote pour UserSessionBean
 */
@Remote
public interface UserSessionBeanRemote {
    
    // void initSession(Utilisateur user, List<Direction> dirs, List<ActionRole> actions);
    
    boolean hasPermission(String nomTable, String action);
    
    boolean isInDirection(Integer idDirection);
    
    Integer getUserRole();
    
    Integer getUserDirectionId();
    
    void destroy();
    
    boolean isValid();
    
    String getSessionId();
    
    // Utilisateur getUtilisateur();
    
    UtilisateurDTO authentifierUtilisateur(String email, String motDePasse);
}
