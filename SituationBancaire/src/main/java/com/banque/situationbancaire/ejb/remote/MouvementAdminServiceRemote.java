package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.dto.MouvementDTO;

import jakarta.ejb.Remote;
import java.util.List;
import java.util.Optional;

/**
 * Interface Remote pour le service de gestion des mouvements (admin)
 */
@Remote
public interface MouvementAdminServiceRemote {
    
    MouvementDTO create(MouvementDTO mouvementDTO, UserSessionBeanRemote userSession);
    
    Optional<MouvementDTO> findById(Long id, UserSessionBeanRemote userSession);
    
    List<MouvementDTO> findAll(UserSessionBeanRemote userSession);
    
    MouvementDTO update(MouvementDTO mouvementDTO, UserSessionBeanRemote userSession);
    
    void delete(Long id, UserSessionBeanRemote userSession);
}
