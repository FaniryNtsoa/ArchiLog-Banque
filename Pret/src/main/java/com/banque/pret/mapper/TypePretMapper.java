package com.banque.pret.mapper;

import com.banque.pret.dto.TypePretDTO;
import com.banque.pret.entity.TypePret;

/**
 * Mapper pour convertir entre TypePret et TypePretDTO
 */
public class TypePretMapper {

    /**
     * Convertit une entité TypePret en TypePretDTO
     */
    public static TypePretDTO toDTO(TypePret typePret) {
        if (typePret == null) {
            return null;
        }

        return TypePretDTO.builder()
                .idTypePret(typePret.getIdTypePret())
                .codeType(typePret.getCodeType())
                .libelle(typePret.getLibelle())
                .tauxInteretAnnuel(typePret.getTauxInteretAnnuel())
                .dureeMin(typePret.getDureeMin())
                .dureeMax(typePret.getDureeMax())
                .montantMin(typePret.getMontantMin())
                .montantMax(typePret.getMontantMax())
                .fraisDossier(typePret.getFraisDossier())
                .penaliteRetardTaux(typePret.getPenaliteRetardTaux())
                .delaiToleranceJours(typePret.getDelaiToleranceJours())
                .actif(typePret.getActif())
                .dateCreation(typePret.getDateCreation())
                .build();
    }

    /**
     * Convertit un TypePretDTO en entité TypePret
     */
    public static TypePret toEntity(TypePretDTO dto) {
        if (dto == null) {
            return null;
        }

        return TypePret.builder()
                .idTypePret(dto.getIdTypePret())
                .codeType(dto.getCodeType())
                .libelle(dto.getLibelle())
                .tauxInteretAnnuel(dto.getTauxInteretAnnuel())
                .dureeMin(dto.getDureeMin())
                .dureeMax(dto.getDureeMax())
                .montantMin(dto.getMontantMin())
                .montantMax(dto.getMontantMax())
                .fraisDossier(dto.getFraisDossier())
                .penaliteRetardTaux(dto.getPenaliteRetardTaux())
                .delaiToleranceJours(dto.getDelaiToleranceJours())
                .actif(dto.getActif())
                .dateCreation(dto.getDateCreation())
                .build();
    }

    /**
     * Met à jour une entité TypePret existante avec les données d'un DTO
     */
    public static void updateEntity(TypePret typePret, TypePretDTO dto) {
        if (typePret == null || dto == null) {
            return;
        }

        typePret.setCodeType(dto.getCodeType());
        typePret.setLibelle(dto.getLibelle());
        typePret.setTauxInteretAnnuel(dto.getTauxInteretAnnuel());
        typePret.setDureeMin(dto.getDureeMin());
        typePret.setDureeMax(dto.getDureeMax());
        typePret.setMontantMin(dto.getMontantMin());
        typePret.setMontantMax(dto.getMontantMax());
        typePret.setFraisDossier(dto.getFraisDossier());
        typePret.setPenaliteRetardTaux(dto.getPenaliteRetardTaux());
        typePret.setDelaiToleranceJours(dto.getDelaiToleranceJours());
        typePret.setActif(dto.getActif());
    }
}
