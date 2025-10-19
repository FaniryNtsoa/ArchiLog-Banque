package com.banque.pret.ejb.impl;

import com.banque.pret.dto.EcheanceDTO;
import com.banque.pret.dto.PretDTO;
import com.banque.pret.dto.SimulationPretDTO;
import com.banque.pret.ejb.remote.PretServiceRemote;
import com.banque.pret.entity.*;
import com.banque.pret.entity.enums.StatutEcheance;
import com.banque.pret.entity.enums.StatutPret;
import com.banque.pret.mapper.EcheanceMapper;
import com.banque.pret.mapper.PretMapper;
import com.banque.pret.repository.*;
import com.banque.pret.util.CalculPretUtil;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implémentation du service prêt avec simulation et création
 */
@Stateless
public class PretServiceImpl implements PretServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(PretServiceImpl.class.getName());

    @Inject
    private PretRepository pretRepository;

    @Inject
    private ClientRepository clientRepository;

    @Inject
    private TypePretRepository typePretRepository;

    @Inject
    private EcheanceRepository echeanceRepository;

    @Override
    public SimulationPretDTO simulerPret(SimulationPretDTO simulationDTO) {
        LOGGER.info("Simulation de prêt - Montant: " + simulationDTO.getMontantDemande() + 
                   ", Durée: " + simulationDTO.getDureeMois() + " mois");

        // Validation des paramètres
        if (simulationDTO.getMontantDemande() == null || simulationDTO.getDureeMois() == null || 
            simulationDTO.getTauxInteretAnnuel() == null) {
            throw new IllegalArgumentException("Paramètres de simulation incomplets");
        }

        // Calcul de la mensualité
        BigDecimal mensualite = CalculPretUtil.calculerMensualite(
            simulationDTO.getMontantDemande(),
            simulationDTO.getTauxInteretAnnuel(),
            simulationDTO.getDureeMois()
        );

        // Calcul du montant total dû
        BigDecimal montantTotalDu = CalculPretUtil.calculerMontantTotalDu(
            mensualite, 
            simulationDTO.getDureeMois()
        );

        // Génération du tableau d'amortissement
        LocalDate datePremiereEcheance = LocalDate.now().plusMonths(1);
        List<EcheanceDTO> tableauAmortissement = CalculPretUtil.genererTableauAmortissement(
            simulationDTO.getMontantDemande(),
            simulationDTO.getTauxInteretAnnuel(),
            simulationDTO.getDureeMois(),
            datePremiereEcheance
        );

        // Calcul du total des intérêts
        BigDecimal totalInterets = CalculPretUtil.calculerTotalInterets(tableauAmortissement);

        // Calcul du coût total du crédit
        BigDecimal frais = simulationDTO.getFraisDossier() != null ? 
                          simulationDTO.getFraisDossier() : BigDecimal.ZERO;
        BigDecimal coutTotalCredit = CalculPretUtil.calculerCoutTotalCredit(
            montantTotalDu,
            simulationDTO.getMontantDemande(),
            frais
        );

        // Construction de la réponse
        SimulationPretDTO resultat = SimulationPretDTO.builder()
                .montantDemande(simulationDTO.getMontantDemande())
                .dureeMois(simulationDTO.getDureeMois())
                .tauxInteretAnnuel(simulationDTO.getTauxInteretAnnuel())
                .fraisDossier(frais)
                .mensualite(mensualite)
                .montantTotalDu(montantTotalDu)
                .coutTotalCredit(coutTotalCredit)
                .totalInterets(totalInterets)
                .totalFrais(frais)
                .tableauAmortissement(tableauAmortissement)
                .build();

        LOGGER.info("Simulation terminée - Mensualité: " + mensualite + 
                   ", Coût total: " + coutTotalCredit);

        return resultat;
    }

    @Override
    public PretDTO creerDemandePret(PretDTO pretDTO) {
        LOGGER.info("Création d'une demande de prêt pour le client: " + pretDTO.getIdClient());

        // Validation
        if (pretDTO.getIdClient() == null || pretDTO.getIdTypePret() == null) {
            throw new IllegalArgumentException("Client et type de prêt obligatoires");
        }

        // Récupération du client
        Optional<Client> clientOpt = clientRepository.findById(pretDTO.getIdClient());
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Client non trouvé");
        }
        Client client = clientOpt.get();

        // Récupération du type de prêt
        Optional<TypePret> typePretOpt = typePretRepository.findById(pretDTO.getIdTypePret());
        if (typePretOpt.isEmpty()) {
            throw new IllegalArgumentException("Type de prêt non trouvé");
        }
        TypePret typePret = typePretOpt.get();

        // Vérification de l'éligibilité
        verifierEligibilite(pretDTO, typePret, client);

        // Calcul de la mensualité et du montant total
        BigDecimal mensualite = CalculPretUtil.calculerMensualite(
            pretDTO.getMontantDemande(),
            typePret.getTauxInteretAnnuel(),
            pretDTO.getDureeMois()
        );

        BigDecimal montantTotalDu = CalculPretUtil.calculerMontantTotalDu(
            mensualite,
            pretDTO.getDureeMois()
        );

        // Calcul des dates
        LocalDate datePremiereEcheance = LocalDate.now().plusMonths(1);
        LocalDate dateDerniereEcheance = datePremiereEcheance.plusMonths(pretDTO.getDureeMois() - 1);

        // Création de l'entité Prêt
        Pret pret = Pret.builder()
                .client(client)
                .typePret(typePret)
                .montantDemande(pretDTO.getMontantDemande())
                .montantAccorde(pretDTO.getMontantDemande()) // Sera ajusté lors de l'approbation
                .dureeMois(pretDTO.getDureeMois())
                .tauxInteretAnnuel(typePret.getTauxInteretAnnuel())
                .mensualite(mensualite)
                .montantTotalDu(montantTotalDu)
                .totalPenalites(BigDecimal.ZERO)
                .dateDemande(LocalDate.now())
                .datePremiereEcheance(datePremiereEcheance)
                .dateDerniereEcheance(dateDerniereEcheance)
                .statut(StatutPret.EN_ATTENTE)
                .build();

        // Sauvegarde
        Pret pretCree = pretRepository.save(pret);

        LOGGER.info("Demande de prêt créée avec succès: " + pretCree.getNumeroPret());

        return PretMapper.toDTO(pretCree);
    }

    @Override
    public PretDTO approuverPret(Long idPret) {
        LOGGER.info("Approbation du prêt: " + idPret);

        Optional<Pret> pretOpt = pretRepository.findById(idPret);
        if (pretOpt.isEmpty()) {
            throw new IllegalArgumentException("Prêt non trouvé");
        }

        Pret pret = pretOpt.get();

        if (pret.getStatut() != StatutPret.EN_ATTENTE) {
            throw new IllegalStateException("Seuls les prêts en attente peuvent être approuvés");
        }

        // Mise à jour du statut
        pret.setStatut(StatutPret.APPROUVE);
        pret.setDateApprobation(LocalDate.now());

        // Génération du tableau d'amortissement
        genererTableauAmortissement(pret);

        // Mise à jour du statut à EN_COURS après génération des échéances
        pret.setStatut(StatutPret.EN_COURS);

        Pret pretApprouve = pretRepository.update(pret);

        LOGGER.info("Prêt approuvé et tableau d'amortissement généré: " + pretApprouve.getNumeroPret());

        return PretMapper.toDTO(pretApprouve);
    }

    @Override
    public PretDTO refuserPret(Long idPret, String motifRefus) {
        LOGGER.info("Refus du prêt: " + idPret);

        Optional<Pret> pretOpt = pretRepository.findById(idPret);
        if (pretOpt.isEmpty()) {
            throw new IllegalArgumentException("Prêt non trouvé");
        }

        Pret pret = pretOpt.get();

        if (pret.getStatut() != StatutPret.EN_ATTENTE) {
            throw new IllegalStateException("Seuls les prêts en attente peuvent être refusés");
        }

        pret.setStatut(StatutPret.REFUSE);
        pret.setMotifRefus(motifRefus);

        Pret pretRefuse = pretRepository.update(pret);

        LOGGER.info("Prêt refusé: " + pretRefuse.getNumeroPret());

        return PretMapper.toDTO(pretRefuse);
    }

    @Override
    public PretDTO rechercherPretParId(Long idPret) {
        LOGGER.info("Recherche du prêt par ID: " + idPret);
        Optional<Pret> pret = pretRepository.findById(idPret);
        return pret.map(PretMapper::toDTO).orElse(null);
    }

    @Override
    public PretDTO rechercherPretParNumero(String numeroPret) {
        LOGGER.info("Recherche du prêt par numéro: " + numeroPret);
        Optional<Pret> pret = pretRepository.findByNumeroPret(numeroPret);
        return pret.map(PretMapper::toDTO).orElse(null);
    }

    @Override
    public List<PretDTO> listerTousLesPrets() {
        LOGGER.info("Récupération de tous les prêts");
        List<Pret> prets = pretRepository.findAll();
        return prets.stream()
                .map(PretMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PretDTO> listerPretsParClient(Long idClient) {
        LOGGER.info("Récupération des prêts du client: " + idClient);
        List<Pret> prets = pretRepository.findByClientId(idClient);
        return prets.stream()
                .map(PretMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PretDTO> listerPretsParStatut(StatutPret statut) {
        LOGGER.info("Récupération des prêts avec statut: " + statut);
        List<Pret> prets = pretRepository.findByStatut(statut);
        return prets.stream()
                .map(PretMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PretDTO> listerPretsParClientEtStatut(Long idClient, StatutPret statut) {
        LOGGER.info("Récupération des prêts du client " + idClient + " avec statut: " + statut);
        List<Pret> prets = pretRepository.findByClientIdAndStatut(idClient, statut);
        return prets.stream()
                .map(PretMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void supprimerPret(Long idPret) {
        LOGGER.info("Suppression du prêt: " + idPret);

        Optional<Pret> pretOpt = pretRepository.findById(idPret);
        if (pretOpt.isEmpty()) {
            throw new IllegalArgumentException("Prêt non trouvé");
        }

        Pret pret = pretOpt.get();

        // Vérifier que le prêt n'est pas en cours
        if (pret.getStatut() == StatutPret.EN_COURS || pret.getStatut() == StatutPret.EN_RETARD) {
            throw new IllegalStateException("Impossible de supprimer un prêt en cours");
        }

        pretRepository.deleteById(idPret);
    }

    /**
     * Vérifie l'éligibilité d'un client pour un prêt
     */
    private void verifierEligibilite(PretDTO pretDTO, TypePret typePret, Client client) {
        // Vérification du montant
        if (!CalculPretUtil.estMontantValide(pretDTO.getMontantDemande(), 
                                             typePret.getMontantMin(), 
                                             typePret.getMontantMax())) {
            throw new IllegalArgumentException(
                String.format("Le montant doit être entre %s et %s", 
                             typePret.getMontantMin(), typePret.getMontantMax())
            );
        }

        // Vérification de la durée
        if (!CalculPretUtil.estDureeValide(pretDTO.getDureeMois(), 
                                          typePret.getDureeMin(), 
                                          typePret.getDureeMax())) {
            throw new IllegalArgumentException(
                String.format("La durée doit être entre %d et %d mois", 
                             typePret.getDureeMin(), typePret.getDureeMax())
            );
        }

        // Autres vérifications d'éligibilité peuvent être ajoutées ici
        // Par exemple: vérification du revenu, de l'endettement, etc.
    }

    /**
     * Génère le tableau d'amortissement pour un prêt approuvé
     */
    private void genererTableauAmortissement(Pret pret) {
        LOGGER.info("Génération du tableau d'amortissement pour le prêt: " + pret.getNumeroPret());

        List<EcheanceDTO> tableauDTO = CalculPretUtil.genererTableauAmortissement(
            pret.getMontantAccorde(),
            pret.getTauxInteretAnnuel(),
            pret.getDureeMois(),
            pret.getDatePremiereEcheance()
        );

        // Conversion et sauvegarde des échéances
        for (EcheanceDTO echeanceDTO : tableauDTO) {
            Echeance echeance = EcheanceMapper.toEntity(echeanceDTO);
            echeance.setPret(pret);
            echeance.setStatut(StatutEcheance.A_VENIR);
            echeanceRepository.save(echeance);
        }

        LOGGER.info("Tableau d'amortissement généré: " + tableauDTO.size() + " échéances");
    }
}
