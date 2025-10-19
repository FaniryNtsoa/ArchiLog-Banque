package com.banque.pret.ejb.remote;

import com.banque.pret.dto.TypePretDTO;
import jakarta.ejb.Remote;
import java.util.List;

/**
 * Interface remote pour le service de gestion des types de prêts
 */
@Remote
public interface TypePretServiceRemote {
    
    /**
     * Crée un nouveau type de prêt
     * @param typePretDTO Le type de prêt à créer
     * @return Le type de prêt créé avec son ID
     */
    TypePretDTO creerTypePret(TypePretDTO typePretDTO);
    
    /**
     * Recherche un type de prêt par son ID
     * @param idTypePret L'ID du type de prêt
     * @return Le type de prêt trouvé ou null
     */
    TypePretDTO rechercherTypePretParId(Long idTypePret);
    
    /**
     * Recherche un type de prêt par son code
     * @param codeType Le code du type de prêt
     * @return Le type de prêt trouvé ou null
     */
    TypePretDTO rechercherTypePretParCode(String codeType);
    
    /**
     * Met à jour un type de prêt
     * @param typePretDTO Le type de prêt à mettre à jour
     * @return Le type de prêt mis à jour
     */
    TypePretDTO modifierTypePret(TypePretDTO typePretDTO);
    
    /**
     * Liste tous les types de prêts
     * @return La liste de tous les types de prêts
     */
    List<TypePretDTO> listerTousLesTypesPrets();
    
    /**
     * Liste tous les types de prêts actifs
     * @return La liste des types de prêts actifs
     */
    List<TypePretDTO> listerTypesPretsActifs();
    
    /**
     * Supprime un type de prêt
     * @param idTypePret L'ID du type de prêt à supprimer
     */
    void supprimerTypePret(Long idTypePret);
}
