package com.banque.pret.ejb.impl;

import com.banque.pret.dto.EcheanceDTO;
import com.banque.pret.dto.RemboursementDTO;
import com.banque.pret.ejb.remote.EcheanceServiceRemote;
import com.banque.pret.entity.*;
import com.banque.pret.entity.enums.StatutEcheance;
import com.banque.pret.mapper.EcheanceMapper;
import com.banque.pret.mapper.RemboursementMapper;
import com.banque.pret.repository.*;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implémentation du service de gestion des échéances et remboursements
 */
@Stateless
public class EcheanceServiceImpl implements EcheanceServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(EcheanceServiceImpl.class.getName());

    @Inject
    private EcheanceRepository echeanceRepository;

    @Inject
    private RemboursementRepository remboursementRepository;

    @Override
    public List<EcheanceDTO> obtenirTableauAmortissement(Long idPret) {
        LOGGER.info("Récupération du tableau d'amortissement pour le prêt ID: " + idPret);
        
        List<Echeance> echeances = echeanceRepository.findByPretId(idPret);
        return echeances.stream()
                .map(EcheanceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EcheanceDTO rechercherEcheanceParId(Long idEcheance) {
        LOGGER.info("Recherche de l'échéance ID: " + idEcheance);
        
        Optional<Echeance> echeance = echeanceRepository.findById(idEcheance);
        return echeance.map(EcheanceMapper::toDTO).orElse(null);
    }

    @Override
    public RemboursementDTO enregistrerRemboursement(RemboursementDTO remboursementDTO) {
        LOGGER.info("Enregistrement d'un remboursement pour l'échéance ID: " + remboursementDTO.getIdEcheance() + 
                   " par l'administrateur ID: " + remboursementDTO.getIdAdministrateur());

        // Validation
        if (remboursementDTO.getIdEcheance() == null) {
            throw new IllegalArgumentException("L'échéance est obligatoire");
        }

        // Récupération de l'échéance
        Optional<Echeance> echeanceOpt = echeanceRepository.findById(remboursementDTO.getIdEcheance());
        if (echeanceOpt.isEmpty()) {
            throw new IllegalArgumentException("Échéance non trouvée");
        }
        Echeance echeance = echeanceOpt.get();

        // Validation du montant
        if (remboursementDTO.getMontant() == null || 
            remboursementDTO.getMontant().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }

        // Création du remboursement avec traçabilité admin
        Remboursement remboursement = Remboursement.builder()
                .echeance(echeance)
                .idCompte(remboursementDTO.getIdCompte())
                .montant(remboursementDTO.getMontant())
                .montantEcheance(remboursementDTO.getMontantEcheance())
                .montantPenalite(remboursementDTO.getMontantPenalite() != null ? 
                    remboursementDTO.getMontantPenalite() : java.math.BigDecimal.ZERO)
                .numeroTransaction(remboursementDTO.getNumeroTransaction())
                .datePaiement(remboursementDTO.getDateOperation() != null ? 
                    remboursementDTO.getDateOperation() : LocalDateTime.now())
                .typePaiement(remboursementDTO.getTypePaiement())
                .idAdministrateur(remboursementDTO.getIdAdministrateur()) // TRAÇABILITÉ ADMIN
                .build();

        // Sauvegarde
        Remboursement remboursementCree = remboursementRepository.save(remboursement);

        // Mise à jour du statut de l'échéance si payée entièrement
        if (remboursement.getMontant().compareTo(echeance.getMontantEcheance()) >= 0) {
            echeance.setStatut(StatutEcheance.PAYE);
            echeance.setDatePaiement(LocalDate.now());
            echeanceRepository.update(echeance);
        }

        LOGGER.info("✅ Remboursement enregistré avec succès par l'admin ID: " + remboursementDTO.getIdAdministrateur());
        
        return RemboursementMapper.toDTO(remboursementCree);
    }

    @Override
    public List<RemboursementDTO> listerRemboursementsParEcheance(Long idEcheance) {
        LOGGER.info("Liste des remboursements pour l'échéance ID: " + idEcheance);
        
        List<Remboursement> remboursements = remboursementRepository.findByEcheanceId(idEcheance);
        return remboursements.stream()
                .map(RemboursementMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RemboursementDTO> listerRemboursementsParPret(Long idPret) {
        LOGGER.info("Liste des remboursements pour le prêt ID: " + idPret);
        
        List<Remboursement> remboursements = remboursementRepository.findByPretId(idPret);
        return remboursements.stream()
                .map(RemboursementMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EcheanceDTO> listerEcheancesImpayees(Long idPret) {
        LOGGER.info("Liste des échéances impayées pour le prêt ID: " + idPret);
        
        List<Echeance> echeances = echeanceRepository.findByPretIdAndStatut(idPret, StatutEcheance.A_VENIR);
        List<Echeance> echeancesRetard = echeanceRepository.findByPretIdAndStatut(idPret, StatutEcheance.EN_RETARD);
        echeances.addAll(echeancesRetard);
        
        return echeances.stream()
                .map(EcheanceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EcheanceDTO> listerEcheancesEnRetard() {
        LOGGER.info("Liste de toutes les échéances en retard");
        
        List<Echeance> echeances = echeanceRepository.findEcheancesEnRetard();
        return echeances.stream()
                .map(EcheanceMapper::toDTO)
                .collect(Collectors.toList());
    }
}