package com.banque.situationbancaire.mapper;

import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.dto.MouvementDTO;
import com.banque.situationbancaire.entity.CompteCourant;
import com.banque.situationbancaire.entity.Mouvement;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper pour convertir entre CompteCourant et CompteCourantDTO
 */
public class CompteCourantMapper {
    
    /**
     * Convertit une entité CompteCourant vers un CompteCourantDTO
     */
    public static CompteCourantDTO toDTO(CompteCourant compte) {
        if (compte == null) {
            return null;
        }
        
        CompteCourantDTO dto = new CompteCourantDTO();
        dto.setIdCompte(compte.getIdCompte());
        dto.setNumeroCompte(compte.getNumeroCompte());
        
        // Calcul du solde à partir du solde initial et des mouvements
        dto.setSolde(calculateSoldeActuel(compte));
        dto.setDecouvertAutorise(BigDecimal.ZERO); // À implémenter selon les paramètres
        dto.setStatut(compte.getStatut() != null ? compte.getStatut().name() : null);
        
        // Conversion des dates
        dto.setDateOuverture(compte.getDateOuverture() != null ? 
            compte.getDateOuverture().atStartOfDay() : null);
        dto.setDateFermeture(compte.getDateFermeture() != null ? 
            compte.getDateFermeture().atStartOfDay() : null);
        
        // Informations du client
        if (compte.getClient() != null) {
            dto.setIdClient(compte.getClient().getIdClient());
            dto.setNumeroClient(compte.getClient().getNumeroClient());
            dto.setNomClient(compte.getClient().getNom());
            dto.setPrenomClient(compte.getClient().getPrenom());
        }
        
        // Type de compte
        if (compte.getTypeCompte() != null) {
            dto.setTypeCompte(compte.getTypeCompte().getLibelle());
        }
        
        // Conversion des mouvements si nécessaire
        if (compte.getMouvements() != null && !compte.getMouvements().isEmpty()) {
            List<MouvementDTO> mouvementsDTO = compte.getMouvements().stream()
                .map(MouvementMapper::toDTO)
                .collect(Collectors.toList());
            dto.setMouvements(mouvementsDTO);
        }
        
        return dto;
    }
    
    /**
     * Convertit un CompteCourantDTO vers une entité CompteCourant
     * Note: Cette méthode ne définit pas les relations (client, typeCompte)
     */
    public static CompteCourant toEntity(CompteCourantDTO dto) {
        if (dto == null) {
            return null;
        }
        
        CompteCourant compte = new CompteCourant();
        compte.setIdCompte(dto.getIdCompte());
        compte.setNumeroCompte(dto.getNumeroCompte());
        compte.setSoldeInitial(dto.getSolde());
        
        // Conversion du statut
        if (dto.getStatut() != null) {
            try {
                compte.setStatut(com.banque.situationbancaire.entity.enums.StatutCompte.valueOf(dto.getStatut()));
            } catch (IllegalArgumentException e) {
                compte.setStatut(com.banque.situationbancaire.entity.enums.StatutCompte.OUVERT);
            }
        }
        
        // Conversion des dates
        if (dto.getDateOuverture() != null) {
            compte.setDateOuverture(dto.getDateOuverture().toLocalDate());
        }
        if (dto.getDateFermeture() != null) {
            compte.setDateFermeture(dto.getDateFermeture().toLocalDate());
        }
        
        return compte;
    }
    
    /**
     * Calcule le solde actuel d'un compte
     */
    private static BigDecimal calculateSoldeActuel(CompteCourant compte) {
        if (compte.getSoldeInitial() == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal solde = compte.getSoldeInitial();
        
        if (compte.getMouvements() != null) {
            for (Mouvement mouvement : compte.getMouvements()) {
                if (mouvement.getMontant() != null) {
                    // Les mouvements créditeurs sont positifs, débiteurs sont négatifs
                    solde = solde.add(mouvement.getMontant());
                }
            }
        }
        
        return solde;
    }
}