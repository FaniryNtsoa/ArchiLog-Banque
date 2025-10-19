package com.banque.situationbancaire.ejb.impl;

import com.banque.situationbancaire.dto.MouvementDTO;
import com.banque.situationbancaire.dto.VirementDTO;
import com.banque.situationbancaire.ejb.remote.OperationServiceRemote;
import com.banque.situationbancaire.entity.*;
import com.banque.situationbancaire.entity.enums.StatutCompte;
import com.banque.situationbancaire.mapper.MouvementMapper;
import com.banque.situationbancaire.mapper.VirementMapper;
import com.banque.situationbancaire.repository.*;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * Implémentation complète du service des opérations bancaires avec règles métier
 */
@Stateless
public class OperationServiceImpl implements OperationServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(OperationServiceImpl.class.getName());

    @Inject
    private CompteCourantRepository compteCourantRepository;
    
    @Inject
    private MouvementRepository mouvementRepository;
    
    @Inject
    private TypeOperationRepository typeOperationRepository;
    
    @Inject
    private VirementRepository virementRepository;

    @Override
    public MouvementDTO effectuerDepot(String numeroCompte, BigDecimal montant, String libelle) {
        LOGGER.info("Dépôt de " + montant + " XOF sur le compte " + numeroCompte);
        
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        
        // Vérifier l'existence et le statut du compte
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé : " + numeroCompte);
        }
        
        CompteCourant compte = compteOpt.get();
        if (compte.getStatut() != StatutCompte.OUVERT) {
            throw new IllegalStateException("Le compte n'est pas actif");
        }
        
        // Récupérer le type d'opération DEPOT
        Optional<TypeOperation> typeDepotOpt = typeOperationRepository.findByCode("DEPOT");
        if (typeDepotOpt.isEmpty()) {
            throw new IllegalStateException("Type d'opération DEPOT non configuré");
        }
        
        // Calculer le solde actuel avant opération
        BigDecimal soldeAvant = calculerSoldeCompte(compte);
        
        // Créer le mouvement
        Mouvement mouvement = Mouvement.builder()
            .compte(compte)
            .typeOperation(typeDepotOpt.get())
            .montant(montant) // Positif pour un dépôt
            .soldeAvantOperation(soldeAvant)
            .soldeApresOperation(soldeAvant.add(montant))
            .dateOperation(LocalDateTime.now())
            .libelleOperation(libelle != null ? libelle : "Dépôt espèces")
            .reference(genererReference("DEP"))
            .build();
        
        Mouvement mouvementCree = mouvementRepository.save(mouvement);
        
        // Appliquer les intérêts de découvert si nécessaire après le dépôt
        gererInteretsDecouvertAutomatique(compte);
        
        return MouvementMapper.toDTO(mouvementCree);
    }

    @Override
    public MouvementDTO effectuerRetrait(String numeroCompte, BigDecimal montant, String libelle) {
        LOGGER.info("Retrait de " + montant + " XOF sur le compte " + numeroCompte);
        
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        
        // Vérifier l'existence et le statut du compte
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé : " + numeroCompte);
        }
        
        CompteCourant compte = compteOpt.get();
        if (compte.getStatut() != StatutCompte.OUVERT) {
            throw new IllegalStateException("Le compte n'est pas actif");
        }
        
        // Vérifier les plafonds de retrait journalier
        if (!verifierPlafonds(numeroCompte, montant, "RETRAIT")) {
            throw new IllegalStateException("Plafond de retrait journalier dépassé");
        }
        
        // Calculer le solde actuel
        BigDecimal soldeAvant = calculerSoldeCompte(compte);
        BigDecimal nouveauSolde = soldeAvant.subtract(montant);
        
        // Vérifier le découvert autorisé
        BigDecimal decouvertAutorise = BigDecimal.ZERO;
        if (compte.getTypeCompte() != null && compte.getTypeCompte().getParametreActuel() != null) {
            decouvertAutorise = compte.getTypeCompte().getParametreActuel().getMontantDecouvertAutorise();
        }
        
        if (nouveauSolde.compareTo(decouvertAutorise.negate()) < 0) {
            throw new IllegalStateException("Solde insuffisant. Découvert autorisé : " + decouvertAutorise + " XOF");
        }
        
        // Récupérer le type d'opération RETRAIT
        Optional<TypeOperation> typeRetraitOpt = typeOperationRepository.findByCode("RETRAIT");
        if (typeRetraitOpt.isEmpty()) {
            throw new IllegalStateException("Type d'opération RETRAIT non configuré");
        }
        
        // Créer le mouvement (montant négatif)
        Mouvement mouvement = Mouvement.builder()
            .compte(compte)
            .typeOperation(typeRetraitOpt.get())
            .montant(montant.negate()) // Négatif pour un retrait
            .soldeAvantOperation(soldeAvant)
            .soldeApresOperation(nouveauSolde)
            .dateOperation(LocalDateTime.now())
            .libelleOperation(libelle != null ? libelle : "Retrait espèces")
            .reference(genererReference("RET"))
            .build();
        
        Mouvement mouvementCree = mouvementRepository.save(mouvement);
        
        // Appliquer les intérêts de découvert si nécessaire après le retrait
        gererInteretsDecouvertAutomatique(compte);
        
        return MouvementMapper.toDTO(mouvementCree);
    }

    @Override
    public VirementDTO effectuerVirement(String numeroCompteDebiteur, String numeroCompteCrediteur, BigDecimal montant, String libelle) {
        LOGGER.info("Virement de " + montant + " de " + numeroCompteDebiteur + " vers " + numeroCompteCrediteur);
        
        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }
        
        if (numeroCompteDebiteur.equals(numeroCompteCrediteur)) {
            throw new IllegalArgumentException("Les comptes débiteur et créditeur doivent être différents");
        }
        
        // Vérifier l'existence des deux comptes
        Optional<CompteCourant> compteDebiteurOpt = compteCourantRepository.findByNumeroCompte(numeroCompteDebiteur);
        if (compteDebiteurOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte débiteur non trouvé : " + numeroCompteDebiteur);
        }
        
        Optional<CompteCourant> compteCrediteurOpt = compteCourantRepository.findByNumeroCompte(numeroCompteCrediteur);
        if (compteCrediteurOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte créditeur non trouvé : " + numeroCompteCrediteur);
        }
        
        CompteCourant compteDebiteur = compteDebiteurOpt.get();
        CompteCourant compteCrediteur = compteCrediteurOpt.get();
        
        // Vérifier le statut des comptes
        if (compteDebiteur.getStatut() != StatutCompte.OUVERT) {
            throw new IllegalStateException("Le compte débiteur n'est pas actif");
        }
        
        if (compteCrediteur.getStatut() != StatutCompte.OUVERT) {
            throw new IllegalStateException("Le compte créditeur n'est pas actif");
        }
        
        // Vérifier les plafonds de virement journalier
        if (!verifierPlafonds(numeroCompteDebiteur, montant, "VIREMENT")) {
            throw new IllegalStateException("Plafond de virement journalier dépassé");
        }
        
        // Calculer le solde du compte débiteur
        BigDecimal soldeAvantDebiteur = calculerSoldeCompte(compteDebiteur);
        BigDecimal nouveauSoldeDebiteur = soldeAvantDebiteur.subtract(montant);
        
        // Vérifier le découvert autorisé du compte débiteur
        BigDecimal decouvertAutorise = BigDecimal.ZERO;
        if (compteDebiteur.getTypeCompte() != null && compteDebiteur.getTypeCompte().getParametreActuel() != null) {
            decouvertAutorise = compteDebiteur.getTypeCompte().getParametreActuel().getMontantDecouvertAutorise();
        }
        
        if (nouveauSoldeDebiteur.compareTo(decouvertAutorise.negate()) < 0) {
            throw new IllegalStateException("Solde insuffisant. Découvert autorisé : " + decouvertAutorise + " XOF");
        }
        
        // Récupérer le type d'opération VIREMENT
        Optional<TypeOperation> typeVirementOpt = typeOperationRepository.findByCode("VIREMENT");
        if (typeVirementOpt.isEmpty()) {
            throw new IllegalStateException("Type d'opération VIREMENT non configuré");
        }
        
        // Calculer le solde du compte créditeur
        BigDecimal soldeAvantCrediteur = calculerSoldeCompte(compteCrediteur);
        
        // Créer le mouvement de débit
        Mouvement mouvementDebit = Mouvement.builder()
            .compte(compteDebiteur)
            .typeOperation(typeVirementOpt.get())
            .montant(montant.negate()) // Négatif pour le débit
            .soldeAvantOperation(soldeAvantDebiteur)
            .soldeApresOperation(nouveauSoldeDebiteur)
            .dateOperation(LocalDateTime.now())
            .libelleOperation(libelle != null ? libelle : "Virement émis vers " + numeroCompteCrediteur)
            .reference(genererReference("VIR"))
            .build();
        
        Mouvement mouvementDebitCree = mouvementRepository.save(mouvementDebit);
        
        // Créer le mouvement de crédit
        Mouvement mouvementCredit = Mouvement.builder()
            .compte(compteCrediteur)
            .typeOperation(typeVirementOpt.get())
            .montant(montant) // Positif pour le crédit
            .soldeAvantOperation(soldeAvantCrediteur)
            .soldeApresOperation(soldeAvantCrediteur.add(montant))
            .dateOperation(LocalDateTime.now())
            .libelleOperation(libelle != null ? libelle : "Virement reçu de " + numeroCompteDebiteur)
            .reference(genererReference("VIR"))
            .build();
        
        Mouvement mouvementCreditCree = mouvementRepository.save(mouvementCredit);
        
        // Créer l'entité Virement qui lie les deux mouvements
        Virement virement = Virement.builder()
            .montant(montant)
            .mouvementDebit(mouvementDebitCree)
            .mouvementCredit(mouvementCreditCree)
            .dateVirement(LocalDateTime.now())
            .build();
        
        Virement virementCree = virementRepository.save(virement);
        
        // Appliquer les intérêts de découvert si nécessaire après le virement
        gererInteretsDecouvertAutomatique(compteDebiteur);
        gererInteretsDecouvertAutomatique(compteCrediteur);
        
        // Créer le DTO de retour
        VirementDTO virementDTO = VirementMapper.toDTO(virementCree);
        virementDTO.setNumeroCompteDebiteur(numeroCompteDebiteur);
        virementDTO.setNumeroCompteCrediteur(numeroCompteCrediteur);
        virementDTO.setLibelle(libelle != null ? libelle : "Virement");
        virementDTO.setStatut("EXECUTE");
        
        return virementDTO;
    }

    @Override
    public List<MouvementDTO> obtenirHistoriqueMouvements(String numeroCompte, LocalDate dateDebut, LocalDate dateFin) {
        LOGGER.info("Récupération de l'historique pour le compte : " + numeroCompte);

        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé : " + numeroCompte);
        }

        CompteCourant compte = compteOpt.get();
        List<Mouvement> mouvements;

        if (dateDebut != null && dateFin != null) {
            LocalDateTime dateTimeDebut = dateDebut.atStartOfDay();
            LocalDateTime dateTimeFin = dateFin.atTime(23, 59, 59);
            mouvements = mouvementRepository.findByCompteIdBetweenDates(compte.getIdCompte(), dateTimeDebut, dateTimeFin);
        } else {
            mouvements = mouvementRepository.findByCompteId(compte.getIdCompte());
        }

        return mouvements.stream()
                .map(MouvementMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MouvementDTO appliquerFraisTenueCompte(String numeroCompte) {
        LOGGER.info("Application des frais de tenue de compte pour : " + numeroCompte);

        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé : " + numeroCompte);
        }

        CompteCourant compte = compteOpt.get();
        if (compte.getStatut() != StatutCompte.OUVERT) {
            return null; // Pas de frais sur les comptes fermés ou bloqués
        }

        // Récupérer les paramètres du compte
        if (compte.getTypeCompte() == null || compte.getTypeCompte().getParametreActuel() == null) {
            throw new IllegalStateException("Aucun paramètre configuré pour le compte");
        }

        ParametresCompte parametres = compte.getTypeCompte().getParametreActuel();
        BigDecimal fraisTenue = parametres.getFraisTenueCompte();

        // Récupérer le type d'opération "FRAIS"
        Optional<TypeOperation> typeFraisOpt = typeOperationRepository.findByCode("FRAIS");
        if (typeFraisOpt.isEmpty()) {
            throw new IllegalStateException("Type d'opération FRAIS non configuré");
        }

        BigDecimal soldeAvant = calculerSoldeCompte(compte);

        Mouvement mouvement = Mouvement.builder()
            .compte(compte)
            .typeOperation(typeFraisOpt.get())
            .montant(fraisTenue.negate()) // Négatif car c'est un débit
            .soldeAvantOperation(soldeAvant)
            .soldeApresOperation(soldeAvant.subtract(fraisTenue))
            .dateOperation(LocalDateTime.now())
            .libelleOperation("Frais de tenue de compte - " + LocalDate.now().getMonth())
            .reference(genererReference("FRA"))
            .build();

        Mouvement mouvementCree = mouvementRepository.save(mouvement);
        return MouvementMapper.toDTO(mouvementCree);
    }

    @Override
    public MouvementDTO appliquerInteretsDecouvert(String numeroCompte) {
        LOGGER.info("Application manuelle des intérêts de découvert pour : " + numeroCompte);

        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé : " + numeroCompte);
        }

        CompteCourant compte = compteOpt.get();
        BigDecimal solde = calculerSoldeCompte(compte);

        // Intérêts seulement si le compte est à découvert
        if (solde.compareTo(BigDecimal.ZERO) >= 0) {
            return null; // Pas d'intérêts si le solde est positif ou nul
        }

        calculerEtAppliquerInteretsDecouvert(compte, solde);

        // Retourner le dernier mouvement d'intérêts créé
        List<Mouvement> derniersMouvements = mouvementRepository.findByCompteId(compte.getIdCompte());
        if (!derniersMouvements.isEmpty()) {
            return MouvementMapper.toDTO(derniersMouvements.get(0)); // Le plus récent
        }

        return null;
    }

    @Override
    public boolean verifierPlafonds(String numeroCompte, BigDecimal montant, String typeOperation) {
        LOGGER.info("Vérification des plafonds pour le compte " + numeroCompte + ", opération : " + typeOperation);
        
        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            return false;
        }
        
        CompteCourant compte = compteOpt.get();
        
        // Récupérer les paramètres du compte
        if (compte.getTypeCompte() == null || compte.getTypeCompte().getParametreActuel() == null) {
            LOGGER.warning("Aucun paramètre configuré pour le compte : " + numeroCompte);
            return false;
        }
        
        ParametresCompte parametres = compte.getTypeCompte().getParametreActuel();
        
        // Calculer les totaux du jour
        LocalDateTime debutJour = LocalDate.now().atStartOfDay();
        LocalDateTime finJour = LocalDate.now().atTime(23, 59, 59);
        
        List<Mouvement> mouvementsJour = mouvementRepository.findByCompteIdBetweenDates(
            compte.getIdCompte(), debutJour, finJour);
        
        BigDecimal totalRetraitsJour = BigDecimal.ZERO;
        BigDecimal totalVirementsJour = BigDecimal.ZERO;
        
        for (Mouvement mvt : mouvementsJour) {
            String codeOperation = mvt.getTypeOperation().getCodeOperation();
            BigDecimal montantAbs = mvt.getMontant().abs();
            
            if ("RETRAIT".equals(codeOperation) && mvt.getMontant().compareTo(BigDecimal.ZERO) < 0) {
                totalRetraitsJour = totalRetraitsJour.add(montantAbs);
            } else if ("VIREMENT".equals(codeOperation) && mvt.getMontant().compareTo(BigDecimal.ZERO) < 0) {
                totalVirementsJour = totalVirementsJour.add(montantAbs);
            }
        }
        
        // Vérifier les plafonds selon le type d'opération
        switch (typeOperation.toUpperCase()) {
            case "RETRAIT":
                BigDecimal nouveauTotalRetraits = totalRetraitsJour.add(montant);
                return nouveauTotalRetraits.compareTo(parametres.getPlafondRetraitJournalier()) <= 0;
                
            case "VIREMENT":
                BigDecimal nouveauTotalVirements = totalVirementsJour.add(montant);
                return nouveauTotalVirements.compareTo(parametres.getPlafondVirementJournalier()) <= 0;
                
            default:
                return true; // Pas de plafond pour les autres opérations
        }
    }

    @Override
    public MouvementDTO rechercherMouvementParReference(String reference) {
        LOGGER.info("Recherche du mouvement par référence " + reference);
        
        Optional<Mouvement> mouvementOpt = mouvementRepository.findByReference(reference);
        return mouvementOpt.map(MouvementMapper::toDTO).orElse(null);
    }

    /**
     * Calcule le solde actuel d'un compte
     */
    private BigDecimal calculerSoldeCompte(CompteCourant compte) {
        BigDecimal soldeInitial = compte.getSoldeInitial();
        BigDecimal totalMouvements = mouvementRepository.calculerSoldeMouvements(compte.getIdCompte());
        return soldeInitial.add(totalMouvements != null ? totalMouvements : BigDecimal.ZERO);
    }

    /**
     * Génère une référence unique pour un mouvement
     */
    private String genererReference(String prefixe) {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 9999);
        return String.format("%s%d%04d", prefixe, timestamp, random);
    }

    /**
     * Gère automatiquement les intérêts de découvert après chaque opération
     */
    private void gererInteretsDecouvertAutomatique(CompteCourant compte) {
        try {
            BigDecimal soldeActuel = calculerSoldeCompte(compte);
            
            // Appliquer les intérêts seulement si le compte est en découvert
            if (soldeActuel.compareTo(BigDecimal.ZERO) < 0) {
                calculerEtAppliquerInteretsDecouvert(compte, soldeActuel);
            }
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de l'application automatique des intérêts : " + e.getMessage());
        }
    }

    /**
     * Calcule et applique les intérêts de découvert
     */
    private void calculerEtAppliquerInteretsDecouvert(CompteCourant compte, BigDecimal montantDecouvert) {
        if (compte.getTypeCompte() == null || compte.getTypeCompte().getParametreActuel() == null) {
            return;
        }

        ParametresCompte parametres = compte.getTypeCompte().getParametreActuel();
        BigDecimal tauxAnnuel = parametres.getTauxDecouvert().getTauxAnnuel();
        
        // Convertir le taux annuel en taux journalier (divisé par 365)
        BigDecimal tauxJournalier = tauxAnnuel.divide(new BigDecimal("365"), 6, RoundingMode.HALF_UP);
        BigDecimal interets = montantDecouvert.abs().multiply(tauxJournalier).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        // Seuil minimum pour appliquer les intérêts (1 XOF)
        if (interets.compareTo(BigDecimal.ONE) >= 0) {
            // Récupérer le type d'opération INTERETS
            Optional<TypeOperation> typeInteretsOpt = typeOperationRepository.findByCode("INTERETS");
            if (typeInteretsOpt.isPresent()) {
                BigDecimal soldeAvant = calculerSoldeCompte(compte);
                
                Mouvement mouvementInterets = Mouvement.builder()
                    .compte(compte)
                    .typeOperation(typeInteretsOpt.get())
                    .montant(interets.negate()) // Négatif car c'est un débit
                    .soldeAvantOperation(soldeAvant)
                    .soldeApresOperation(soldeAvant.subtract(interets))
                    .dateOperation(LocalDateTime.now())
                    .libelleOperation("Intérêts découvert journalier")
                    .reference(genererReference("INT"))
                    .build();
                
                mouvementRepository.save(mouvementInterets);
                LOGGER.info("Intérêts de découvert appliqués : " + interets + " XOF sur le compte " + compte.getNumeroCompte());
            }
        }
    }
}