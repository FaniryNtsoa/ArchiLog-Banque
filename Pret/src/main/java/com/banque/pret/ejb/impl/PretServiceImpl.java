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
import java.math.RoundingMode;
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
        LOGGER.info(" PHASE 1 : SIMULATION - Montant: " + simulationDTO.getMontantDemande() + 
                   ", Durée: " + simulationDTO.getDureeMois() + " mois");

        // Validation des paramètres
        if (simulationDTO.getMontantDemande() == null || simulationDTO.getDureeMois() == null || 
            simulationDTO.getIdTypePret() == null) {
            throw new IllegalArgumentException("Paramètres de simulation incomplets (montant, durée, type de prêt)");
        }

        // Récupération du type de prêt pour les plafonds
        Optional<TypePret> typePretOpt = typePretRepository.findById(simulationDTO.getIdTypePret());
        if (typePretOpt.isEmpty()) {
            throw new IllegalArgumentException("Type de prêt non trouvé");
        }
        TypePret typePret = typePretOpt.get();

        // RÈGLE : Vérification des plafonds du type de prêt
        if (!CalculPretUtil.estMontantValide(simulationDTO.getMontantDemande(), 
                                             typePret.getMontantMin(), 
                                             typePret.getMontantMax())) {
            throw new IllegalArgumentException(
                String.format("Le montant doit être entre %s et %s pour ce type de prêt", 
                             typePret.getMontantMin(), typePret.getMontantMax())
            );
        }

        if (!CalculPretUtil.estDureeValide(simulationDTO.getDureeMois(), 
                                          typePret.getDureeMin(), 
                                          typePret.getDureeMax())) {
            throw new IllegalArgumentException(
                String.format("La durée doit être entre %d et %d mois pour ce type de prêt", 
                             typePret.getDureeMin(), typePret.getDureeMax())
            );
        }

        // RÈGLE : Calcul de la mensualité selon la formule M = [C × i] / [1 - (1 + i)^-n]
        BigDecimal mensualite = CalculPretUtil.calculerMensualite(
            simulationDTO.getMontantDemande(),
            typePret.getTauxInteretAnnuel(),
            simulationDTO.getDureeMois()
        );

        // RÈGLE : Vérification 33% du revenu (si revenu fourni)
        if (simulationDTO.getRevenuMensuel() != null && simulationDTO.getRevenuMensuel().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal seuilEndettement = simulationDTO.getRevenuMensuel()
                .multiply(BigDecimal.valueOf(0.33))
                .setScale(2, RoundingMode.HALF_UP);
            
            if (mensualite.compareTo(seuilEndettement) > 0) {
                throw new IllegalArgumentException(
                    String.format(" La mensualité (%s) dépasse 33%% de votre revenu mensuel (%s). Seuil maximum : %s", 
                                 mensualite, simulationDTO.getRevenuMensuel(), seuilEndettement)
                );
            }
        }

        // Calcul du montant total dû
        BigDecimal montantTotalDu = CalculPretUtil.calculerMontantTotalDu(
            mensualite, 
            simulationDTO.getDureeMois()
        );

        // RÈGLE : Affichage du tableau d'amortissement prévisionnel
        LocalDate datePremiereEcheance = LocalDate.now().plusMonths(1);
        List<EcheanceDTO> tableauAmortissement = CalculPretUtil.genererTableauAmortissement(
            simulationDTO.getMontantDemande(),
            typePret.getTauxInteretAnnuel(),
            simulationDTO.getDureeMois(),
            datePremiereEcheance
        );

        // Calcul du total des intérêts
        BigDecimal totalInterets = CalculPretUtil.calculerTotalInterets(tableauAmortissement);

        // Calcul du coût total du crédit
        BigDecimal frais = typePret.getFraisDossier() != null ? 
                          typePret.getFraisDossier() : BigDecimal.ZERO;
        BigDecimal coutTotalCredit = CalculPretUtil.calculerCoutTotalCredit(
            montantTotalDu,
            simulationDTO.getMontantDemande(),
            frais
        );

        // Construction de la réponse
        SimulationPretDTO resultat = SimulationPretDTO.builder()
                .idTypePret(typePret.getIdTypePret())
                .montantDemande(simulationDTO.getMontantDemande())
                .dureeMois(simulationDTO.getDureeMois())
                .tauxInteretAnnuel(typePret.getTauxInteretAnnuel())
                .fraisDossier(frais)
                .mensualite(mensualite)
                .montantTotalDu(montantTotalDu)
                .coutTotalCredit(coutTotalCredit)
                .totalInterets(totalInterets)
                .totalFrais(frais)
                .revenuMensuel(simulationDTO.getRevenuMensuel())
                .tableauAmortissement(tableauAmortissement)
                .build();

        LOGGER.info("✅ PHASE 1 terminée - Mensualité: " + mensualite + 
                   ", Coût total: " + coutTotalCredit + ", Tableau: " + tableauAmortissement.size() + " échéances");

        return resultat;
    }

    @Override
    public PretDTO creerDemandePret(PretDTO pretDTO) {
        LOGGER.info(" PHASE 2 : DEMANDE DE PRÊT pour le client: " + pretDTO.getIdClient());

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

        // RÈGLE : Vérifier que le client est actif
        if (client.getStatut() != com.banque.pret.entity.enums.StatutClient.ACTIF) {
            throw new IllegalStateException("Le client doit être actif pour demander un prêt");
        }

        // Récupération du type de prêt
        Optional<TypePret> typePretOpt = typePretRepository.findById(pretDTO.getIdTypePret());
        if (typePretOpt.isEmpty()) {
            throw new IllegalArgumentException("Type de prêt non trouvé");
        }
        TypePret typePret = typePretOpt.get();

        // RÈGLE : Vérification de l'éligibilité (montant, durée, 33% revenu)
        verifierEligibilite(pretDTO, typePret, client);

        // RÈGLE : Calcul de la mensualité et du montant total
        BigDecimal mensualite = CalculPretUtil.calculerMensualite(
            pretDTO.getMontantDemande(),
            typePret.getTauxInteretAnnuel(),
            pretDTO.getDureeMois()
        );

        BigDecimal montantTotalDu = CalculPretUtil.calculerMontantTotalDu(
            mensualite,
            pretDTO.getDureeMois()
        );

        // RÈGLE : Calcul des dates des échéances théoriques
        LocalDate datePremiereEcheance = LocalDate.now().plusMonths(1);
        LocalDate dateDerniereEcheance = datePremiereEcheance.plusMonths(pretDTO.getDureeMois() - 1);

        // RÈGLE : Création de l'entité Prêt avec statut EN_ATTENTE et génération du numéro unique
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

        LOGGER.info("✅ PHASE 2 terminée - Demande créée: " + pretCree.getNumeroPret() + 
                   " - Statut: EN_ATTENTE - Notification à l'agent de crédit");

        return PretMapper.toDTO(pretCree);
    }

    @Override
    public PretDTO approuverPret(Long idPret) {
        LOGGER.info(" PHASE 3 : INSTRUCTION & VALIDATION du prêt: " + idPret);

        Optional<Pret> pretOpt = pretRepository.findById(idPret);
        if (pretOpt.isEmpty()) {
            throw new IllegalArgumentException("Prêt non trouvé");
        }

        Pret pret = pretOpt.get();

        if (pret.getStatut() != StatutPret.EN_ATTENTE) {
            throw new IllegalStateException("Seuls les prêts en attente peuvent être approuvés");
        }

        Client client = pret.getClient();

        // RÈGLE : Vérification de l'éligibilité complète
        // 1. Client actif et en règle
        if (client.getStatut() != com.banque.pret.entity.enums.StatutClient.ACTIF) {
            throw new IllegalStateException(" Le client doit être actif");
        }

        // 2. Revenus stables et suffisants
        if (client.getRevenuMensuel() == null || client.getRevenuMensuel().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(" Le client doit avoir des revenus déclarés");
        }

        // 3. Taux d'endettement < 33% après le nouveau prêt
        BigDecimal chargesMensuelles = client.getChargesMensuelles() != null ? 
            client.getChargesMensuelles() : BigDecimal.ZERO;
        BigDecimal nouvellesCharges = chargesMensuelles.add(pret.getMensualite());
        BigDecimal tauxEndettement = nouvellesCharges
            .divide(client.getRevenuMensuel(), 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100));

        if (tauxEndettement.compareTo(BigDecimal.valueOf(33)) > 0) {
            throw new IllegalStateException(
                String.format("Taux d'endettement trop élevé: %.2f%% (max 33%%)", tauxEndettement)
            );
        }

        // 4. Calcul de capacité de remboursement: (Revenus - Charges existantes) ≥ Mensualité × 1.3
        BigDecimal capaciteRemboursement = client.getRevenuMensuel().subtract(chargesMensuelles);
        BigDecimal seuilCapacite = pret.getMensualite().multiply(BigDecimal.valueOf(1.3));

        if (capaciteRemboursement.compareTo(seuilCapacite) < 0) {
            throw new IllegalStateException(
                String.format(" Capacité de remboursement insuffisante. Requis: %s, Disponible: %s", 
                             seuilCapacite, capaciteRemboursement)
            );
        }

        // RÈGLE : Si tous les critères sont remplis => APPROUVE
        pret.setStatut(StatutPret.APPROUVE);
        pret.setDateApprobation(LocalDate.now());
        
        // Note: Le montant accordé peut être ajusté si nécessaire
        // Pour l'instant, on garde le montant demandé
        
        LOGGER.info("✅ PHASE 3 terminée - Prêt approuvé: " + pret.getNumeroPret());

        // PHASE 4 : Génération automatique du tableau d'amortissement définitif
        genererTableauAmortissementDefinitif(pret);

        // Mise à jour du statut à EN_COURS après génération des échéances
        pret.setStatut(StatutPret.EN_COURS);

        Pret pretApprouve = pretRepository.update(pret);

        LOGGER.info("📊 PHASE 4 terminée - Tableau d'amortissement généré: " + pretApprouve.getNumeroPret());

        return PretMapper.toDTO(pretApprouve);
    }

    @Override
    public PretDTO refuserPret(Long idPret, String motifRefus) {
        LOGGER.info("🔍 PHASE 3 : INSTRUCTION & VALIDATION - Refus du prêt: " + idPret);

        Optional<Pret> pretOpt = pretRepository.findById(idPret);
        if (pretOpt.isEmpty()) {
            throw new IllegalArgumentException("Prêt non trouvé");
        }

        Pret pret = pretOpt.get();

        if (pret.getStatut() != StatutPret.EN_ATTENTE) {
            throw new IllegalStateException("Seuls les prêts en attente peuvent être refusés");
        }

        // RÈGLE : Si au moins un critère non respecté => REFUSE + motif de refus
        pret.setStatut(StatutPret.REFUSE);
        pret.setMotifRefus(motifRefus != null && !motifRefus.isEmpty() ? 
                          motifRefus : "Critères d'éligibilité non respectés");

        Pret pretRefuse = pretRepository.update(pret);

        LOGGER.info(" PHASE 3 terminée - Prêt refusé: " + pretRefuse.getNumeroPret() + 
                   " - Motif: " + pretRefuse.getMotifRefus());

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
     * PHASE 2 : Vérifie l'éligibilité d'un client pour un prêt
     */
    private void verifierEligibilite(PretDTO pretDTO, TypePret typePret, Client client) {
        // RÈGLE : Vérification du montant dans les plafonds
        if (!CalculPretUtil.estMontantValide(pretDTO.getMontantDemande(), 
                                             typePret.getMontantMin(), 
                                             typePret.getMontantMax())) {
            throw new IllegalArgumentException(
                String.format(" Le montant doit être entre %s et %s", 
                             typePret.getMontantMin(), typePret.getMontantMax())
            );
        }

        // RÈGLE : Vérification de la durée dans les plafonds
        if (!CalculPretUtil.estDureeValide(pretDTO.getDureeMois(), 
                                          typePret.getDureeMin(), 
                                          typePret.getDureeMax())) {
            throw new IllegalArgumentException(
                String.format(" La durée doit être entre %d et %d mois", 
                             typePret.getDureeMin(), typePret.getDureeMax())
            );
        }

        // RÈGLE : Mensualité ≤ 33% du revenu mensuel du client
        if (client.getRevenuMensuel() != null && client.getRevenuMensuel().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal mensualite = CalculPretUtil.calculerMensualite(
                pretDTO.getMontantDemande(),
                typePret.getTauxInteretAnnuel(),
                pretDTO.getDureeMois()
            );

            BigDecimal seuilEndettement = client.getRevenuMensuel()
                .multiply(BigDecimal.valueOf(0.33))
                .setScale(2, RoundingMode.HALF_UP);
            
            if (mensualite.compareTo(seuilEndettement) > 0) {
                throw new IllegalArgumentException(
                    String.format(" La mensualité (%s) dépasse 33%% du revenu mensuel (%s). Seuil maximum : %s", 
                                 mensualite, client.getRevenuMensuel(), seuilEndettement)
                );
            }
        }
    }

    /**
     * PHASE 4 : Génère le tableau d'amortissement définitif pour un prêt approuvé
     * RÈGLE : Création automatique de N échéances (N = durée en mois) après approbation
     */
    private void genererTableauAmortissementDefinitif(Pret pret) {
        LOGGER.info("📊 PHASE 4 : GÉNÉRATION DES ÉCHÉANCES pour le prêt: " + pret.getNumeroPret());

        // RÈGLE : Date de première échéance = date d'approbation + 1 mois
        LocalDate datePremiereEcheance = pret.getDateApprobation().plusMonths(1);
        
        // Mise à jour de la date dans le prêt
        pret.setDatePremiereEcheance(datePremiereEcheance);
        pret.setDateDerniereEcheance(datePremiereEcheance.plusMonths(pret.getDureeMois() - 1));

        // RÈGLE : Calcul selon méthode d'amortissement constant
        List<EcheanceDTO> tableauDTO = CalculPretUtil.genererTableauAmortissement(
            pret.getMontantAccorde(),
            pret.getTauxInteretAnnuel(),
            pret.getDureeMois(),
            datePremiereEcheance
        );

        // RÈGLE : Conversion et sauvegarde des échéances avec statut initial A_VENIR
        for (EcheanceDTO echeanceDTO : tableauDTO) {
            Echeance echeance = EcheanceMapper.toEntity(echeanceDTO);
            echeance.setPret(pret);
            echeance.setStatut(StatutEcheance.A_VENIR); // Statut initial = "A_VENIR"
            echeanceRepository.save(echeance);
        }

        LOGGER.info("PHASE 4 terminée - " + tableauDTO.size() + " échéances générées automatiquement");
    }
}
