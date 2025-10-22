package com.banque.pret.mapper;

import com.banque.pret.dto.PretDTO;
import com.banque.pret.entity.Pret;
import com.banque.pret.entity.enums.StatutPret;

/**
 * Mapper pour convertir entre Pret et PretDTO
 */
public class PretMapper {

    /**
     * Convertit une entité Pret en PretDTO
     */
    public static PretDTO toDTO(Pret pret) {
        if (pret == null) {
            return null;
        }

        PretDTO.PretDTOBuilder builder = PretDTO.builder()
                .idPret(pret.getIdPret())
                .idClient(pret.getClient() != null ? pret.getClient().getIdClient() : null)
                .idTypePret(pret.getTypePret() != null ? pret.getTypePret().getIdTypePret() : null)
                .numeroPret(pret.getNumeroPret())
                .montantDemande(pret.getMontantDemande())
                .montantAccorde(pret.getMontantAccorde())
                .dureeMois(pret.getDureeMois())
                .tauxInteretAnnuel(pret.getTauxInteretAnnuel())
                .montantTotalDu(pret.getMontantTotalDu())
                .mensualite(pret.getMensualite())
                .totalPenalites(pret.getTotalPenalites())
                .dateDemande(pret.getDateDemande())
                .dateApprobation(pret.getDateApprobation())
                .datePremiereEcheance(pret.getDatePremiereEcheance())
                .dateDerniereEcheance(pret.getDateDerniereEcheance())
                .statut(pret.getStatut() != null ? pret.getStatut().name() : null)
                .motifRefus(pret.getMotifRefus())
                .dateCreation(pret.getDateCreation())
                .idAdministrateur(pret.getIdAdministrateur()); // TRAÇABILITÉ ADMIN

        // Ajouter les informations du client si disponibles
        if (pret.getClient() != null) {
            builder.nomClient(pret.getClient().getNom())
                   .prenomClient(pret.getClient().getPrenom())
                   .emailClient(pret.getClient().getEmail());
        }

        // Ajouter les informations du type de prêt si disponibles
        if (pret.getTypePret() != null) {
            builder.libelleTypePret(pret.getTypePret().getLibelle());
        }

        return builder.build();
    }

    /**
     * Convertit un PretDTO en entité Pret (sans les relations)
     */
    public static Pret toEntity(PretDTO dto) {
        if (dto == null) {
            return null;
        }

        return Pret.builder()
                .idPret(dto.getIdPret())
                .numeroPret(dto.getNumeroPret())
                .montantDemande(dto.getMontantDemande())
                .montantAccorde(dto.getMontantAccorde())
                .dureeMois(dto.getDureeMois())
                .tauxInteretAnnuel(dto.getTauxInteretAnnuel())
                .montantTotalDu(dto.getMontantTotalDu())
                .mensualite(dto.getMensualite())
                .totalPenalites(dto.getTotalPenalites())
                .dateDemande(dto.getDateDemande())
                .dateApprobation(dto.getDateApprobation())
                .datePremiereEcheance(dto.getDatePremiereEcheance())
                .dateDerniereEcheance(dto.getDateDerniereEcheance())
                .statut(dto.getStatut() != null ? StatutPret.valueOf(dto.getStatut()) : null)
                .motifRefus(dto.getMotifRefus())
                .dateCreation(dto.getDateCreation())
                .idAdministrateur(dto.getIdAdministrateur()) // TRAÇABILITÉ ADMIN
                .build();
    }
}
