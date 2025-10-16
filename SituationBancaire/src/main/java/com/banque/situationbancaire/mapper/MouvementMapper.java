package com.banque.situationbancaire.mapper;

import com.banque.situationbancaire.dto.MouvementDTO;
import com.banque.situationbancaire.entity.Mouvement;

/**
 * Mapper pour convertir entre Mouvement et MouvementDTO
 */
public class MouvementMapper {
    
    /**
     * Convertit une entité Mouvement vers un MouvementDTO
     */
    public static MouvementDTO toDTO(Mouvement mouvement) {
        if (mouvement == null) {
            return null;
        }
        
        MouvementDTO dto = new MouvementDTO();
        dto.setIdMouvement(mouvement.getIdMouvement());
        dto.setMontant(mouvement.getMontant());
        dto.setDateOperation(mouvement.getDateOperation());
        dto.setDateValeur(mouvement.getDateOperation()); // Même valeur pour l'instant
        dto.setSoldeAvant(mouvement.getSoldeAvantOperation());
        dto.setSoldeApres(mouvement.getSoldeApresOperation());
        dto.setReference(mouvement.getReference());
        dto.setLibelle(mouvement.getLibelleOperation());
        
        // Informations du compte
        if (mouvement.getCompte() != null) {
            dto.setNumeroCompte(mouvement.getCompte().getNumeroCompte());
        }
        
        // Informations du type d'opération
        if (mouvement.getTypeOperation() != null) {
            dto.setTypeOperation(mouvement.getTypeOperation().getLibelleOperation());
            if (mouvement.getTypeOperation().getNature() != null) {
                dto.setNatureOperation(mouvement.getTypeOperation().getNature().name());
            }
            if (mouvement.getTypeOperation().getCategorie() != null) {
                dto.setCategorieOperation(mouvement.getTypeOperation().getCategorie().name());
            }
        }
        
        // Informations sur le virement si applicable
        if (mouvement.getVirementEmis() != null) {
            // Récupération des infos du virement depuis le mouvement de crédit associé
            if (mouvement.getVirementEmis().getMouvementCredit() != null && 
                mouvement.getVirementEmis().getMouvementCredit().getCompte() != null) {
                dto.setNumeroCompteBeneficiaire(mouvement.getVirementEmis().getMouvementCredit().getCompte().getNumeroCompte());
                if (mouvement.getVirementEmis().getMouvementCredit().getCompte().getClient() != null) {
                    dto.setNomBeneficiaire(
                        mouvement.getVirementEmis().getMouvementCredit().getCompte().getClient().getNom() + " " +
                        mouvement.getVirementEmis().getMouvementCredit().getCompte().getClient().getPrenom()
                    );
                }
            }
        } else if (mouvement.getVirementRecu() != null) {
            // Récupération des infos du virement depuis le mouvement de débit associé
            if (mouvement.getVirementRecu().getMouvementDebit() != null && 
                mouvement.getVirementRecu().getMouvementDebit().getCompte() != null) {
                dto.setNumeroCompteBeneficiaire(mouvement.getVirementRecu().getMouvementDebit().getCompte().getNumeroCompte());
                if (mouvement.getVirementRecu().getMouvementDebit().getCompte().getClient() != null) {
                    dto.setNomBeneficiaire(
                        mouvement.getVirementRecu().getMouvementDebit().getCompte().getClient().getNom() + " " +
                        mouvement.getVirementRecu().getMouvementDebit().getCompte().getClient().getPrenom()
                    );
                }
            }
        }
        
        dto.setStatut("EXECUTE"); // Par défaut, les mouvements en base sont exécutés
        
        return dto;
    }
    
    /**
     * Convertit un MouvementDTO vers une entité Mouvement
     * Note: Cette méthode ne définit pas les relations (compte, typeOperation)
     */
    public static Mouvement toEntity(MouvementDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Mouvement mouvement = new Mouvement();
        mouvement.setIdMouvement(dto.getIdMouvement());
        mouvement.setMontant(dto.getMontant());
        mouvement.setDateOperation(dto.getDateOperation());
        mouvement.setSoldeAvantOperation(dto.getSoldeAvant());
        mouvement.setSoldeApresOperation(dto.getSoldeApres());
        mouvement.setReference(dto.getReference());
        mouvement.setLibelleOperation(dto.getLibelle());
        
        return mouvement;
    }
}