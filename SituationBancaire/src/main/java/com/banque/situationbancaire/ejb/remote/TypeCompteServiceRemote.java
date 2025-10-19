package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.dto.TypeCompteDTO;
import jakarta.ejb.Remote;
import java.util.List;

/**
 * Interface remote pour le service de gestion des types de compte
 */
@Remote
public interface TypeCompteServiceRemote {
    
    /**
     * Liste tous les types de compte disponibles
     * @return La liste de tous les types de compte avec leurs paramètres
     */
    List<TypeCompteDTO> listerTousLesTypesCompte();
    
    /**
     * Recherche un type de compte par son ID
     * @param idTypeCompte L'ID du type de compte
     * @return Le type de compte trouvé ou null
     */
    TypeCompteDTO rechercherTypeCompteParId(Long idTypeCompte);
    
    /**
     * Recherche un type de compte par son code
     * @param codeType Le code du type de compte (ex: STANDARD, PREMIUM, etc.)
     * @return Le type de compte trouvé ou null
     */
    TypeCompteDTO rechercherTypeCompteParCode(String codeType);
    
    /**
     * Vérifie si un type de compte existe par son code
     * @param codeType Le code du type de compte
     * @return true si le type existe, false sinon
     */
    boolean existeParCode(String codeType);
}