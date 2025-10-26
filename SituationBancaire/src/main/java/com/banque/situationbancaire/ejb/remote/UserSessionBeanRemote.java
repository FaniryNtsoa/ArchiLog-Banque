package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.dto.UtilisateurDTO;
import com.banque.situationbancaire.dto.DirectionDTO;
import com.banque.situationbancaire.dto.ActionRoleDTO;

import jakarta.ejb.Remote;
import java.util.List;

/**
 * Interface Remote pour UserSessionBean
 */
@Remote
public interface UserSessionBeanRemote {
    
    void initSession(UtilisateurDTO user, List<DirectionDTO> dirs, List<ActionRoleDTO> actions);
    
    boolean hasPermission(String nomTable, String action);
    
    boolean isInDirection(Integer idDirection);
    
    Integer getUserRole();
    
    Integer getUserDirectionId();
    
    void destroy();
    
    boolean isValid();
    
    String getSessionId();
    
    UtilisateurDTO getUtilisateur();
    
    List<ActionRoleDTO> getActionsAutorisees();
}
