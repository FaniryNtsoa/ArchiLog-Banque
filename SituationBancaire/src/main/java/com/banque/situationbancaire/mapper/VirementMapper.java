package com.banque.situationbancaire.mapper;

import com.banque.situationbancaire.dto.VirementDTO;
import com.banque.situationbancaire.entity.Virement;

/**
 * Mapper pour convertir entre Virement et VirementDTO
 */
public class VirementMapper {
    
    /**
     * Convertit une entité Virement vers un VirementDTO
     */
    public static VirementDTO toDTO(Virement virement) {
        if (virement == null) {
            return null;
        }
        
        VirementDTO dto = new VirementDTO();
        dto.setIdVirement(virement.getIdVirement());
        dto.setMontant(virement.getMontant());
        dto.setDateVirement(virement.getDateVirement());
        dto.setDateExecution(virement.getDateVirement()); // Même date pour l'instant
        dto.setStatut("EXECUTE"); // Par défaut
        
        // Récupération des informations depuis les mouvements
        if (virement.getMouvementDebit() != null) {
            dto.setLibelle(virement.getMouvementDebit().getLibelleOperation());
            dto.setReference(virement.getMouvementDebit().getReference());
            
            if (virement.getMouvementDebit().getCompte() != null) {
                dto.setNumeroCompteDebiteur(virement.getMouvementDebit().getCompte().getNumeroCompte());
                if (virement.getMouvementDebit().getCompte().getClient() != null) {
                    dto.setNomDebiteur(
                        virement.getMouvementDebit().getCompte().getClient().getNom() + " " +
                        virement.getMouvementDebit().getCompte().getClient().getPrenom()
                    );
                }
            }
        }
        
        if (virement.getMouvementCredit() != null && virement.getMouvementCredit().getCompte() != null) {
            dto.setNumeroCompteCrediteur(virement.getMouvementCredit().getCompte().getNumeroCompte());
            if (virement.getMouvementCredit().getCompte().getClient() != null) {
                dto.setNomCrediteur(
                    virement.getMouvementCredit().getCompte().getClient().getNom() + " " +
                    virement.getMouvementCredit().getCompte().getClient().getPrenom()
                );
            }
        }
        
        return dto;
    }
    
    /**
     * Convertit un VirementDTO vers une entité Virement
     * Note: Cette méthode ne définit pas les relations (mouvements)
     */
    public static Virement toEntity(VirementDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Virement virement = new Virement();
        virement.setIdVirement(dto.getIdVirement());
        virement.setMontant(dto.getMontant());
        virement.setDateVirement(dto.getDateVirement());
        
        return virement;
    }
}