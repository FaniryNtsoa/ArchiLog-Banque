package com.banque.pret.ejb.impl;

import com.banque.pret.dto.EcheanceDTO;
import com.banque.pret.dto.RemboursementDTO;
import com.banque.pret.ejb.remote.EcheanceServiceRemote;
import com.banque.pret.entity.Echeance;
import com.banque.pret.entity.Remboursement;
import com.banque.pret.entity.enums.StatutEcheance;
import com.banque.pret.mapper.EcheanceMapper;
import com.banque.pret.mapper.RemboursementMapper;
import com.banque.pret.repository.EcheanceRepository;
import com.banque.pret.repository.RemboursementRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implémentation du service échéance et remboursement
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
        LOGGER.info("Récupération du tableau d'amortissement pour le prêt: " + idPret);
        
        List<Echeance> echeances = echeanceRepository.findByPretId(idPret);
        
        return echeances.stream()
                .map(EcheanceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public EcheanceDTO rechercherEcheanceParId(Long idEcheance) {
        LOGGER.info("Recherche de l'échéance par ID: " + idEcheance);
        
        Optional<Echeance> echeance = echeanceRepository.findById(idEcheance);
        return echeance.map(EcheanceMapper::toDTO).orElse(null);
    }

    @Override
    public RemboursementDTO enregistrerRemboursement(RemboursementDTO remboursementDTO) {
        LOGGER.info("Enregistrement d'un remboursement pour l'échéance: " + remboursementDTO.getIdEcheance());

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

        // Vérifier que l'échéance n'est pas déjà payée
        if (echeance.getStatut() == StatutEcheance.PAYE || 
            echeance.getStatut() == StatutEcheance.PAYE_AVEC_RETARD) {
            throw new IllegalStateException("Cette échéance a déjà été payée");
        }

        // Création du remboursement
        Remboursement remboursement = RemboursementMapper.toEntity(remboursementDTO);
        remboursement.setEcheance(echeance);

        // Sauvegarde du remboursement
        Remboursement remboursementCree = remboursementRepository.save(remboursement);

        // Mise à jour du statut de l'échéance
        LocalDate aujourdhui = LocalDate.now();
        if (aujourdhui.isAfter(echeance.getDateEcheance())) {
            echeance.setStatut(StatutEcheance.PAYE_AVEC_RETARD);
        } else {
            echeance.setStatut(StatutEcheance.PAYE);
        }
        echeance.setDatePaiement(aujourdhui);

        echeanceRepository.update(echeance);

        LOGGER.info("Remboursement enregistré avec succès");

        return RemboursementMapper.toDTO(remboursementCree);
    }

    @Override
    public List<RemboursementDTO> listerRemboursementsParEcheance(Long idEcheance) {
        LOGGER.info("Récupération des remboursements de l'échéance: " + idEcheance);
        
        List<Remboursement> remboursements = remboursementRepository.findByEcheanceId(idEcheance);
        
        return remboursements.stream()
                .map(RemboursementMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<RemboursementDTO> listerRemboursementsParPret(Long idPret) {
        LOGGER.info("Récupération des remboursements du prêt: " + idPret);
        
        List<Remboursement> remboursements = remboursementRepository.findByPretId(idPret);
        
        return remboursements.stream()
                .map(RemboursementMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EcheanceDTO> listerEcheancesImpayees(Long idPret) {
        LOGGER.info("Récupération des échéances impayées du prêt: " + idPret);
        
        List<Echeance> echeancesAVenir = echeanceRepository.findByPretIdAndStatut(idPret, StatutEcheance.A_VENIR);
        List<Echeance> echeancesEnRetard = echeanceRepository.findByPretIdAndStatut(idPret, StatutEcheance.EN_RETARD);
        List<Echeance> echeancesAujourdhui = echeanceRepository.findByPretIdAndStatut(idPret, StatutEcheance.ECHEANCE_AUJOURDHUI);
        
        List<Echeance> toutesEcheances = new java.util.ArrayList<>();
        toutesEcheances.addAll(echeancesAVenir);
        toutesEcheances.addAll(echeancesEnRetard);
        toutesEcheances.addAll(echeancesAujourdhui);
        
        return toutesEcheances.stream()
                .map(EcheanceMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<EcheanceDTO> listerEcheancesEnRetard() {
        LOGGER.info("Récupération de toutes les échéances en retard");
        
        List<Echeance> echeances = echeanceRepository.findEcheancesEnRetard();
        
        return echeances.stream()
                .map(EcheanceMapper::toDTO)
                .collect(Collectors.toList());
    }
}
