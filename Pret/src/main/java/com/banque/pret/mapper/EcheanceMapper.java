package com.banque.pret.mapper;

import com.banque.pret.dto.EcheanceDTO;
import com.banque.pret.entity.Echeance;
import com.banque.pret.entity.enums.StatutEcheance;

/**
 * Mapper pour convertir entre Echeance et EcheanceDTO
 */
public class EcheanceMapper {

    /**
     * Convertit une entité Echeance en EcheanceDTO
     */
    public static EcheanceDTO toDTO(Echeance echeance) {
        if (echeance == null) {
            return null;
        }

        return EcheanceDTO.builder()
                .idEcheance(echeance.getIdEcheance())
                .idPret(echeance.getPret() != null ? echeance.getPret().getIdPret() : null)
                .numeroEcheance(echeance.getNumeroEcheance())
                .montantEcheance(echeance.getMontantEcheance())
                .capital(echeance.getCapital())
                .interet(echeance.getInteret())
                .capitalRestant(echeance.getCapitalRestant())
                .dateEcheance(echeance.getDateEcheance())
                .datePaiement(echeance.getDatePaiement())
                .statut(echeance.getStatut() != null ? echeance.getStatut().name() : null)
                .penaliteAppliquee(echeance.getPenaliteAppliquee())
                .joursRetard(echeance.getJoursRetard())
                .dateCalculPenalite(echeance.getDateCalculPenalite())
                .build();
    }

    /**
     * Convertit un EcheanceDTO en entité Echeance (sans les relations)
     */
    public static Echeance toEntity(EcheanceDTO dto) {
        if (dto == null) {
            return null;
        }

        return Echeance.builder()
                .idEcheance(dto.getIdEcheance())
                .numeroEcheance(dto.getNumeroEcheance())
                .montantEcheance(dto.getMontantEcheance())
                .capital(dto.getCapital())
                .interet(dto.getInteret())
                .capitalRestant(dto.getCapitalRestant())
                .dateEcheance(dto.getDateEcheance())
                .datePaiement(dto.getDatePaiement())
                .statut(dto.getStatut() != null ? StatutEcheance.valueOf(dto.getStatut()) : null)
                .penaliteAppliquee(dto.getPenaliteAppliquee())
                .joursRetard(dto.getJoursRetard())
                .dateCalculPenalite(dto.getDateCalculPenalite())
                .build();
    }
}
