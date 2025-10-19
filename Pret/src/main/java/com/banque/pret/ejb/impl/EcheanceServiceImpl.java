package com.banque.pret.ejb.impl;

import com.banque.pret.dto.EcheanceDTO;
import com.banque.pret.dto.RemboursementDTO;
import com.banque.pret.ejb.remote.EcheanceServiceRemote;
import com.banque.pret.entity.Echeance;
import com.banque.pret.entity.Pret;
import com.banque.pret.entity.Remboursement;
import com.banque.pret.entity.enums.StatutEcheance;
import com.banque.pret.entity.enums.StatutPret;
import com.banque.pret.entity.enums.TypePaiement;
import com.banque.pret.mapper.EcheanceMapper;
import com.banque.pret.mapper.RemboursementMapper;
import com.banque.pret.repository.EcheanceRepository;
import com.banque.pret.repository.PretRepository;
import com.banque.pret.repository.RemboursementRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * PHASE 5 : Implémentation du service échéance et remboursement
 */
@Stateless
public class EcheanceServiceImpl implements EcheanceServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(EcheanceServiceImpl.class.getName());
    private static final int DELAI_TOLERANCE = 5; // 5 jours de délai de tolérance

    @Inject
    private EcheanceRepository echeanceRepository;

    @Inject
    private RemboursementRepository remboursementRepository;

    @Inject
    private PretRepository pretRepository;

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
        LOGGER.info("💰 PHASE 5 : GESTION DES REMBOURSEMENTS - Montant: " + remboursementDTO.getMontantPaye());

        try {
            // Validation
            if (remboursementDTO.getIdPret() == null || remboursementDTO.getMontantPaye() == null) {
                throw new IllegalArgumentException("Le prêt et le montant sont obligatoires");
            }

            if (remboursementDTO.getMontantPaye().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant doit être supérieur à zéro");
            }

            // Récupération du prêt
            Optional<Pret> pretOpt = pretRepository.findById(remboursementDTO.getIdPret());
            if (pretOpt.isEmpty()) {
                throw new IllegalArgumentException("Prêt non trouvé");
            }
            Pret pret = pretOpt.get();

            // RÈGLE : Récupérer toutes les échéances impayées du prêt, triées par date
            List<Echeance> echeancesImpayees = echeanceRepository.findByPretId(remboursementDTO.getIdPret())
                    .stream()
                    .filter(e -> e.getStatut() != StatutEcheance.PAYE
                            && e.getStatut() != StatutEcheance.PAYE_AVEC_RETARD)
                    .sorted(Comparator.comparing(Echeance::getNumeroEcheance))
                    .collect(Collectors.toList());

            if (echeancesImpayees.isEmpty()) {
                throw new IllegalStateException("❌ Aucune échéance impayée trouvée pour ce prêt");
            }

            BigDecimal montantRestant = remboursementDTO.getMontantPaye();
            LocalDate dateRemboursement = LocalDate.now();
            List<Echeance> echeancesTraitees = new ArrayList<>();
            List<Remboursement> remboursementsCrees = new ArrayList<>();

            LOGGER.info("📋 Traitement de " + echeancesImpayees.size() + " échéances impayées");

            for (Echeance echeance : echeancesImpayees) {
                if (montantRestant.compareTo(BigDecimal.ZERO) <= 0) {
                    break; // Plus de montant à distribuer
                }

                BigDecimal montantEcheance = echeance.getMontantEcheance();
                BigDecimal montantAPayer = montantRestant.min(montantEcheance);

                // RÈGLE : Calcul des pénalités en cas de retard
                BigDecimal penaliteAppliquee = BigDecimal.ZERO;
                long joursRetard = ChronoUnit.DAYS.between(echeance.getDateEcheance(), dateRemboursement);

                // Pénalités désactivées pour l'instant
                /*
                 * if (joursRetard > DELAI_TOLERANCE) {
                 * penaliteAppliquee = montantEcheance
                 * .multiply(BigDecimal.valueOf(0.0005))
                 * .multiply(BigDecimal.valueOf(joursRetard - DELAI_TOLERANCE))
                 * .setScale(2, RoundingMode.HALF_UP);
                 * }
                 */

                // Génération d'un numéro de transaction sécurisé
                String numeroTransaction = "TXN-" + System.currentTimeMillis() + "-" +
                        ThreadLocalRandom.current().nextInt(1000, 9999);

                TypePaiement typePaiement = remboursementDTO.getTypePaiement();
                if (typePaiement == null) {
                    typePaiement = TypePaiement.ESPECES; // Valeur par défaut
                    LOGGER.info("⚠️ Type de paiement non spécifié, utilisation de la valeur par défaut: VIREMENT");
                }

                // Création du remboursement
                Remboursement remboursement = Remboursement.builder()
                        .echeance(echeance)
                        .montant(montantAPayer)
                        .montantEcheance(montantAPayer)
                        .montantPenalite(BigDecimal.ZERO)
                        .datePaiement(LocalDateTime.now())
                        .idCompte(remboursementDTO.getIdCompte())
                        .typePaiement(typePaiement)
                        .numeroTransaction(numeroTransaction) // Généré automatiquement pour éviter les doublons
                        .build();

                // SAUVEGARDE SÉCURISÉE avec gestion d'erreur
                try {
                    LOGGER.info("💾 Sauvegarde du remboursement - Échéance: " + echeance.getNumeroEcheance() +
                            ", Montant: " + montantAPayer);

                    Remboursement remboursementCree = remboursementRepository.save(remboursement);
                    remboursementsCrees.add(remboursementCree);

                    LOGGER.info("✅ Remboursement sauvegardé avec ID: " + remboursementCree.getIdRemboursement());

                } catch (Exception e) {
                    LOGGER.severe("❌ ERREUR lors de la sauvegarde du remboursement: " + e.getMessage());
                    LOGGER.severe("❌ Détails de l'erreur: " + e.getClass().getName());

                    // Convertir l'exception technique en exception métier
                    if (e.getMessage() != null && e.getMessage().toLowerCase().contains("constraint")) {
                        throw new IllegalStateException(
                                "Erreur de contrainte base de données. Vérifiez l'intégrité des données.");
                    } else if (e.getMessage() != null && e.getMessage().toLowerCase().contains("foreign")) {
                        throw new IllegalStateException(
                                "Référence invalide. L'échéance n'existe pas ou a été supprimée.");
                    } else {
                        throw new IllegalStateException("Erreur technique lors de l'enregistrement: " + e.getMessage());
                    }
                }

                // RÈGLE : Mise à jour du statut de l'échéance
                if (montantAPayer.compareTo(montantEcheance) >= 0) {
                    // Paiement complet
                    if (joursRetard > DELAI_TOLERANCE) {
                        echeance.setStatut(StatutEcheance.PAYE_AVEC_RETARD);
                        LOGGER.info("✅ Échéance " + echeance.getNumeroEcheance() + " payée avec retard (" + joursRetard
                                + " jours)");
                    } else {
                        echeance.setStatut(StatutEcheance.PAYE);
                        LOGGER.info("✅ Échéance " + echeance.getNumeroEcheance() + " payée à temps");
                    }
                    echeance.setDatePaiement(dateRemboursement);
                    echeance.setPenaliteAppliquee(BigDecimal.ZERO);
                    echeance.setJoursRetard((int) Math.max(0, joursRetard));

                    echeancesTraitees.add(echeance);
                    montantRestant = montantRestant.subtract(montantEcheance);
                } else {
                    // Paiement partiel
                    LOGGER.info("⚠️ Paiement partiel de " + montantAPayer + " sur l'échéance " +
                            echeance.getNumeroEcheance() + " (reste: " + montantEcheance.subtract(montantAPayer) + ")");
                    montantRestant = BigDecimal.ZERO; // On s'arrête là
                }

                // Mise à jour sécurisée de l'échéance
                try {
                    echeanceRepository.update(echeance);
                    LOGGER.info("📝 Statut échéance " + echeance.getNumeroEcheance() + " mis à jour: "
                            + echeance.getStatut());
                } catch (Exception e) {
                    LOGGER.severe("❌ ERREUR lors de la mise à jour de l'échéance: " + e.getMessage());
                    throw new IllegalStateException("Erreur lors de la mise à jour du statut de l'échéance");
                }
            }

            // Vérifier s'il reste du montant non utilisé
            if (montantRestant.compareTo(BigDecimal.ZERO) > 0) {
                LOGGER.info("💰 Montant non utilisé: " + montantRestant + " - Toutes les échéances sont payées");
            }

            // RÈGLE : Vérifier si toutes les échéances sont payées => Prêt TERMINE
            verifierEtatPret(pret);

            LOGGER.info("🎉 PHASE 5 terminée - " + echeancesTraitees.size() +
                    " échéances traitées, " + remboursementsCrees.size() + " remboursements créés");

            // Retourner le premier remboursement créé
            if (remboursementsCrees.isEmpty()) {
                throw new IllegalStateException("Aucun remboursement n'a pu être créé");
            }

            return RemboursementMapper.toDTO(remboursementsCrees.get(0));

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Relancer les exceptions métier avec un message clair
            LOGGER.warning("⚠️ Erreur métier lors du remboursement: " + e.getMessage());
            throw e;

        } catch (Exception e) {
            // Capturer toutes les autres exceptions techniques
            LOGGER.severe("💥 ERREUR TECHNIQUE inattendue lors du remboursement: " + e.getMessage());
            LOGGER.severe("💥 Type d'erreur: " + e.getClass().getName());

            // Journalisation complète pour le débogage
            if (e.getCause() != null) {
                LOGGER.severe("💥 Cause: " + e.getCause().getMessage());
            }

            throw new IllegalStateException(
                    "Une erreur technique est survenue lors du traitement de votre remboursement. Veuillez réessayer.");
        }
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
        List<Echeance> echeancesAujourdhui = echeanceRepository.findByPretIdAndStatut(idPret,
                StatutEcheance.ECHEANCE_AUJOURDHUI);

        List<Echeance> toutesEcheances = new ArrayList<>();
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

    /**
     * PHASE 5 : Vérification mensuelle des échéances et mise à jour des statuts
     * Cette méthode doit être appelée quotidiennement par un scheduler
     */
    public void verifierEcheancesMensuelles() {
        LOGGER.info("PHASE 5 : Vérification mensuelle des échéances");

        LocalDate aujourdhui = LocalDate.now();

        // Récupération de toutes les échéances non payées
        List<Echeance> toutesEcheances = echeanceRepository.findAll()
                .stream()
                .filter(e -> e.getStatut() == StatutEcheance.A_VENIR ||
                        e.getStatut() == StatutEcheance.ECHEANCE_AUJOURDHUI ||
                        e.getStatut() == StatutEcheance.EN_RETARD)
                .collect(Collectors.toList());

        for (Echeance echeance : toutesEcheances) {
            LocalDate dateEcheance = echeance.getDateEcheance();
            long joursRetard = ChronoUnit.DAYS.between(dateEcheance, aujourdhui);

            if (aujourdhui.isEqual(dateEcheance)) {
                // RÈGLE : Date d'échéance = aujourd'hui
                echeance.setStatut(StatutEcheance.ECHEANCE_AUJOURDHUI);
                echeanceRepository.update(echeance);
                LOGGER.info("📅 Échéance " + echeance.getPret().getNumeroPret() +
                        "-" + echeance.getNumeroEcheance() + " échue aujourd'hui");

            } else if (joursRetard > DELAI_TOLERANCE && echeance.getStatut() != StatutEcheance.EN_RETARD) {
                // RÈGLE : Paiement partiel/aucun => EN_RETARD après délai de tolérance
                echeance.setStatut(StatutEcheance.EN_RETARD);
                echeance.setJoursRetard((int) joursRetard);
                echeanceRepository.update(echeance);

                // Mettre à jour le statut du prêt
                Pret pret = echeance.getPret();
                if (pret.getStatut() == StatutPret.EN_COURS) {
                    pret.setStatut(StatutPret.EN_RETARD);
                    pretRepository.update(pret);
                }

                LOGGER.warning("⚠️ Échéance " + echeance.getPret().getNumeroPret() +
                        "-" + echeance.getNumeroEcheance() + " en retard de " + joursRetard + " jours");
            }
        }
    }

    /**
     * PHASE 5 : Vérifie l'état du prêt et met à jour son statut si toutes les
     * échéances sont payées
     */
    private void verifierEtatPret(Pret pret) {
        List<Echeance> echeances = echeanceRepository.findByPretId(pret.getIdPret());

        boolean toutesPayees = echeances.stream()
                .allMatch(e -> e.getStatut() == StatutEcheance.PAYE ||
                        e.getStatut() == StatutEcheance.PAYE_AVEC_RETARD);

        if (toutesPayees) {
            // RÈGLE : Toutes les échéances payées => Prêt TERMINE
            pret.setStatut(StatutPret.TERMINE);
            pretRepository.update(pret);
            LOGGER.info("🎉 Prêt " + pret.getNumeroPret() + " TERMINÉ - Toutes les échéances sont payées");
        } else {
            // Vérifier s'il y a des retards
            boolean aDesRetards = echeances.stream()
                    .anyMatch(e -> e.getStatut() == StatutEcheance.EN_RETARD);

            if (aDesRetards && pret.getStatut() != StatutPret.EN_RETARD) {
                pret.setStatut(StatutPret.EN_RETARD);
                pretRepository.update(pret);
                LOGGER.warning("⚠️ Prêt " + pret.getNumeroPret() + " EN_RETARD");
            } else if (!aDesRetards && pret.getStatut() == StatutPret.EN_RETARD) {
                pret.setStatut(StatutPret.EN_COURS);
                pretRepository.update(pret);
                LOGGER.info("✅ Prêt " + pret.getNumeroPret() + " de nouveau EN_COURS");
            }
        }
    }
}
