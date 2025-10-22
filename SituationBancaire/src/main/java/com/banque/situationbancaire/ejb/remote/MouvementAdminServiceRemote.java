package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.dto.MouvementDTO;
import com.banque.situationbancaire.session.UserSessionBean;

import jakarta.ejb.Remote;
import java.util.List;
import java.util.Optional;

/**
 * Interface Remote pour le service de gestion des mouvements (admin)
 */
@Remote
public interface MouvementAdminServiceRemote {
    
    MouvementDTO create(MouvementDTO mouvementDTO, UserSessionBean userSession);
    
    Optional<MouvementDTO> findById(Long id, UserSessionBean userSession);
    
    List<MouvementDTO> findAll(UserSessionBean userSession);
    
    MouvementDTO update(MouvementDTO mouvementDTO, UserSessionBean userSession);
    
    void delete(Long id, UserSessionBean userSession);
}
