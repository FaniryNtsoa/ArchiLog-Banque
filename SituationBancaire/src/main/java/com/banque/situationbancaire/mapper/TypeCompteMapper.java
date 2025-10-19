package com.banque.situationbancaire.mapper;

import com.banque.situationbancaire.dto.TypeCompteDTO;
import com.banque.situationbancaire.entity.TypeCompte;

/**
 * Mapper pour convertir entre TypeCompte et TypeCompteDTO
 */
public class TypeCompteMapper {
    
    /**
     * Convertit une entité TypeCompte vers un TypeCompteDTO
     */
    public static TypeCompteDTO toDTO(TypeCompte typeCompte) {
        if (typeCompte == null) {
            return null;
        }
        
        TypeCompteDTO dto = new TypeCompteDTO();
        dto.setIdTypeCompte(typeCompte.getIdTypeCompte());
        dto.setCodeType(typeCompte.getCodeType());
        dto.setLibelle(typeCompte.getLibelle());
        dto.setDescription(typeCompte.getDescription());
        
        // Ajouter les informations des paramètres si disponibles
        if (typeCompte.getParametreActuel() != null) {
            dto.setIdParametre(typeCompte.getParametreActuel().getIdParametre());
            dto.setPlafondRetraitJournalier(typeCompte.getParametreActuel().getPlafondRetraitJournalier());
            dto.setPlafondVirementJournalier(typeCompte.getParametreActuel().getPlafondVirementJournalier());
            dto.setMontantDecouvertAutorise(typeCompte.getParametreActuel().getMontantDecouvertAutorise());
            dto.setFraisTenueCompte(typeCompte.getParametreActuel().getFraisTenueCompte());
            dto.setFraisTenueComptePeriodicite(
                typeCompte.getParametreActuel().getFraisTenueComptePeriodicite() != null ?
                typeCompte.getParametreActuel().getFraisTenueComptePeriodicite().name() : null
            );
            
            // Ajouter les informations du taux de découvert si disponibles
            if (typeCompte.getParametreActuel().getTauxDecouvert() != null) {
                dto.setTauxAnnuel(typeCompte.getParametreActuel().getTauxDecouvert().getTauxAnnuel());
                dto.setDescriptionTaux(typeCompte.getParametreActuel().getTauxDecouvert().getDescription());
            }
        }
        
        return dto;
    }
    
    /**
     * Convertit un TypeCompteDTO vers une entité TypeCompte (partiellement)
     * Note: Cette méthode ne convertit que les champs de base, 
     * les paramètres doivent être gérés séparément
     */
    public static TypeCompte toEntity(TypeCompteDTO dto) {
        if (dto == null) {
            return null;
        }
        
        TypeCompte typeCompte = new TypeCompte();
        typeCompte.setIdTypeCompte(dto.getIdTypeCompte());
        typeCompte.setCodeType(dto.getCodeType());
        typeCompte.setLibelle(dto.getLibelle());
        typeCompte.setDescription(dto.getDescription());
        
        return typeCompte;
    }
}