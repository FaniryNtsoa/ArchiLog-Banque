package com.banque.pret.mapper;

import com.banque.pret.dto.RemboursementDTO;
import com.banque.pret.entity.Remboursement;

/**
 * Mapper pour convertir entre Remboursement et RemboursementDTO
 */
public class RemboursementMapper {

    /**
     * Convertit une entité Remboursement en RemboursementDTO
     */
    public static RemboursementDTO toDTO(Remboursement remboursement) {
        if (remboursement == null) {
            return null;
        }

        return RemboursementDTO.builder()
                .idRemboursement(remboursement.getIdRemboursement())
                .idEcheance(remboursement.getEcheance() != null ? remboursement.getEcheance().getIdEcheance() : null)
                .idCompte(remboursement.getIdCompte())
                .montant(remboursement.getMontant())
                .montantEcheance(remboursement.getMontantEcheance())
                .montantPenalite(remboursement.getMontantPenalite())
                .datePaiement(remboursement.getDatePaiement())
                .typePaiement(remboursement.getTypePaiement())
                .build();
    }

    /**
     * Convertit un RemboursementDTO en entité Remboursement (sans les relations)
     */
    public static Remboursement toEntity(RemboursementDTO dto) {
        if (dto == null) {
            return null;
        }

        return Remboursement.builder()
                .idRemboursement(dto.getIdRemboursement())
                .idCompte(dto.getIdCompte())
                .montant(dto.getMontant())
                .montantEcheance(dto.getMontantEcheance())
                .montantPenalite(dto.getMontantPenalite())
                .datePaiement(dto.getDatePaiement())
                .typePaiement(dto.getTypePaiement())
                .build();
    }
}
