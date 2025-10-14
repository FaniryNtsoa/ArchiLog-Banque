package com.banque.situationbancaire.ejb.impl;

import com.banque.situationbancaire.ejb.remote.OperationServiceRemote;
import com.banque.situationbancaire.entity.*;
import com.banque.situationbancaire.entity.enums.StatutCompte;
import com.banque.situationbancaire.repository.*;
import java.math.RoundingMode;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implémentation du service de gestion des opérations bancaires
 */
@Stateless
@Transactional
public class OperationServiceImpl implements OperationServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(OperationServiceImpl.class.getName());

    @EJB
    private CompteCourantRepository compteCourantRepository;

    @EJB
    private MouvementRepository mouvementRepository;

    @EJB
    private TypeOperationRepository typeOperationRepository;

    @Override
    public Mouvement effectuerDepot(String numeroCompte, BigDecimal montant, String libelle) {
        LOGGER.info("Dépôt de " + montant + " sur le compte : " + numeroCompte);

        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }

        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé");
        }

        CompteCourant compte = compteOpt.get();
        if (compte.getStatut() != StatutCompte.OUVERT) {
            throw new IllegalStateException("Le compte n'est pas actif");
        }

        // Récupérer le type d'opération "DEPOT"
        Optional<TypeOperation> typeDepotOpt = typeOperationRepository.findByCode("DEPOT");
        if (typeDepotOpt.isEmpty()) {
            throw new IllegalStateException("Type d'opération DEPOT non configuré");
        }

        // Créer le mouvement de crédit
        Mouvement mouvement = creerMouvement(compte, montant, libelle, typeDepotOpt.get());

        return mouvementRepository.save(mouvement);
    }

    @Override
    public Mouvement effectuerRetrait(String numeroCompte, BigDecimal montant, String libelle) {
        LOGGER.info("Retrait de " + montant + " sur le compte : " + numeroCompte);

        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }

        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé");
        }

        CompteCourant compte = compteOpt.get();
        if (compte.getStatut() != StatutCompte.OUVERT) {
            throw new IllegalStateException("Le compte n'est pas actif");
        }

        // Vérifier les plafonds
        if (!verifierPlafonds(numeroCompte, montant, "DEBIT")) {
            throw new IllegalStateException("Plafond de retrait dépassé");
        }

        // Vérifier que le compte a suffisamment de fonds (avec découvert autorisé)
        BigDecimal soldeActuel = calculerSolde(compte);
        BigDecimal nouveauSolde = soldeActuel.subtract(montant);
        
        // Récupérer le découvert autorisé depuis les paramètres du compte
        BigDecimal decouvertAutorise = compte.getTypeCompte().getParametreActuel().getMontantDecouvertAutorise();
        
        if (nouveauSolde.compareTo(decouvertAutorise.negate()) < 0) {
            throw new IllegalStateException("Solde insuffisant (découvert autorisé : " + decouvertAutorise + " XOF)");
        }

        // Récupérer le type d'opération "RETRAIT"
        Optional<TypeOperation> typeRetraitOpt = typeOperationRepository.findByCode("RETRAIT");
        if (typeRetraitOpt.isEmpty()) {
            throw new IllegalStateException("Type d'opération RETRAIT non configuré");
        }

        // Créer le mouvement de débit (montant négatif)
        Mouvement mouvement = creerMouvement(compte, montant.negate(), libelle, typeRetraitOpt.get());

        return mouvementRepository.save(mouvement);
    }

    @Override
    public Virement effectuerVirement(String numeroCompteDebiteur, String numeroCompteCrediteur, 
                                     BigDecimal montant, String libelle) {
        LOGGER.info("Virement de " + montant + " de " + numeroCompteDebiteur + " vers " + numeroCompteCrediteur);

        if (montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif");
        }

        if (numeroCompteDebiteur.equals(numeroCompteCrediteur)) {
            throw new IllegalArgumentException("Les comptes source et destination doivent être différents");
        }

        // Vérifier les deux comptes
        Optional<CompteCourant> compteDebiteurOpt = compteCourantRepository.findByNumeroCompte(numeroCompteDebiteur);
        Optional<CompteCourant> compteCrediteurOpt = compteCourantRepository.findByNumeroCompte(numeroCompteCrediteur);

        if (compteDebiteurOpt.isEmpty() || compteCrediteurOpt.isEmpty()) {
            throw new IllegalArgumentException("Un ou plusieurs comptes non trouvés");
        }

        CompteCourant compteDebiteur = compteDebiteurOpt.get();
        CompteCourant compteCrediteur = compteCrediteurOpt.get();

        if (compteDebiteur.getStatut() != StatutCompte.OUVERT || compteCrediteur.getStatut() != StatutCompte.OUVERT) {
            throw new IllegalStateException("Un ou plusieurs comptes ne sont pas actifs");
        }

        // Vérifier les plafonds pour le compte débiteur
        if (!verifierPlafonds(numeroCompteDebiteur, montant, "VIREMENT")) {
            throw new IllegalStateException("Plafond de virement dépassé");
        }

        // Vérifier les fonds du compte débiteur
        BigDecimal soldeDebiteur = calculerSolde(compteDebiteur);
        BigDecimal nouveauSoldeDebiteur = soldeDebiteur.subtract(montant);
        
        // Récupérer le découvert autorisé depuis les paramètres du compte
        BigDecimal decouvertAutorise = compteDebiteur.getTypeCompte().getParametreActuel().getMontantDecouvertAutorise();
        if (nouveauSoldeDebiteur.compareTo(decouvertAutorise.negate()) < 0) {
            throw new IllegalStateException("Solde insuffisant sur le compte débiteur");
        }

        // Récupérer le type d'opération "VIREMENT"
        Optional<TypeOperation> typeVirementOpt = typeOperationRepository.findByCode("VIREMENT");
        if (typeVirementOpt.isEmpty()) {
            throw new IllegalStateException("Type d'opération VIREMENT non configuré");
        }

        TypeOperation typeVirement = typeVirementOpt.get();

        // Créer les mouvements correspondants
        Mouvement mouvementDebit = creerMouvement(compteDebiteur, montant.negate(), 
            "Virement vers " + numeroCompteCrediteur + " - " + libelle, typeVirement);

        Mouvement mouvementCredit = creerMouvement(compteCrediteur, montant, 
            "Virement de " + numeroCompteDebiteur + " - " + libelle, typeVirement);

        mouvementDebit = mouvementRepository.save(mouvementDebit);
        mouvementCredit = mouvementRepository.save(mouvementCredit);

        // Créer le virement avec les mouvements
        Virement virement = new Virement();
        virement.setMouvementDebit(mouvementDebit);
        virement.setMouvementCredit(mouvementCredit);
        virement.setMontant(montant);
        virement.setDateVirement(LocalDateTime.now());

        return virement; // Note: pas de repository pour virement pour l'instant
    }

    @Override
    public List<Mouvement> obtenirHistoriqueMouvements(String numeroCompte, LocalDate dateDebut, LocalDate dateFin) {
        LOGGER.info("Récupération de l'historique pour le compte : " + numeroCompte);

        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé");
        }

        CompteCourant compte = compteOpt.get();

        if (dateDebut != null && dateFin != null) {
            LocalDateTime dateTimeDebut = dateDebut.atStartOfDay();
            LocalDateTime dateTimeFin = dateFin.atTime(23, 59, 59);
            return mouvementRepository.findByCompteIdBetweenDates(compte.getIdCompte(), dateTimeDebut, dateTimeFin);
        } else {
            return mouvementRepository.findByCompteId(compte.getIdCompte());
        }
    }

    @Override
    public Mouvement appliquerFraisTenueCompte(String numeroCompte) {
        LOGGER.info("Application des frais de tenue de compte pour : " + numeroCompte);

        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé");
        }

        CompteCourant compte = compteOpt.get();
        if (compte.getStatut() != StatutCompte.OUVERT) {
            return null; // Pas de frais sur les comptes fermés ou bloqués
        }

        // Récupérer le type d'opération "FRAIS"
        Optional<TypeOperation> typeFraisOpt = typeOperationRepository.findByCode("FRAIS");
        if (typeFraisOpt.isEmpty()) {
            throw new IllegalStateException("Type d'opération FRAIS non configuré");
        }

        // Récupérer les frais de tenue depuis les paramètres du compte
        BigDecimal fraisTenue = compte.getTypeCompte().getParametreActuel().getFraisTenueCompte();
        Mouvement mouvement = creerMouvement(compte, fraisTenue.negate(), 
            "Frais de tenue de compte - " + LocalDate.now().getMonth(), typeFraisOpt.get());

        return mouvementRepository.save(mouvement);
    }

    @Override
    public Mouvement appliquerInteretsDecouvert(String numeroCompte) {
        LOGGER.info("Application des intérêts de découvert pour : " + numeroCompte);

        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            throw new IllegalArgumentException("Compte non trouvé");
        }

        CompteCourant compte = compteOpt.get();
        BigDecimal solde = calculerSolde(compte);

        // Intérêts seulement si le compte est à découvert
        if (solde.compareTo(BigDecimal.ZERO) >= 0) {
            return null; // Pas d'intérêts si le solde est positif ou nul
        }

        BigDecimal montantDecouvert = solde.abs();
        
        // Récupérer le taux d'intérêt depuis les paramètres du compte
        ParametresCompte parametres = compte.getTypeCompte().getParametreActuel();
        BigDecimal tauxAnnuel = parametres.getTauxDecouvert().getTauxAnnuel();
        
        // Convertir le taux annuel en taux journalier (divisé par 365)
        BigDecimal tauxQuotidien = tauxAnnuel.divide(new BigDecimal("365"), 6, java.math.RoundingMode.HALF_UP);
        BigDecimal interets = montantDecouvert.multiply(tauxQuotidien);

        // Récupérer le type d'opération "INTERETS"
        Optional<TypeOperation> typeInteretsOpt = typeOperationRepository.findByCode("INTERETS");
        if (typeInteretsOpt.isEmpty()) {
            throw new IllegalStateException("Type d'opération INTERETS non configuré");
        }

        Mouvement mouvement = creerMouvement(compte, interets.negate(), 
            "Intérêts de découvert - " + LocalDate.now(), typeInteretsOpt.get());

        return mouvementRepository.save(mouvement);
    }

    @Override
    public boolean verifierPlafonds(String numeroCompte, BigDecimal montant, String typeOperation) {
        // Récupérer les opérations du jour
        LocalDateTime debutJour = LocalDate.now().atStartOfDay();
        LocalDateTime finJour = LocalDate.now().atTime(23, 59, 59);

        Optional<CompteCourant> compteOpt = compteCourantRepository.findByNumeroCompte(numeroCompte);
        if (compteOpt.isEmpty()) {
            return false;
        }

        CompteCourant compte = compteOpt.get();
        
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

        // Récupérer les plafonds depuis les paramètres du compte
        ParametresCompte parametres = compte.getTypeCompte().getParametreActuel();
        
        switch (typeOperation) {
            case "DEBIT":
                return totalRetraitsJour.add(montant).compareTo(parametres.getPlafondRetraitJournalier()) <= 0;
            case "VIREMENT":
                return totalVirementsJour.add(montant).compareTo(parametres.getPlafondVirementJournalier()) <= 0;
            default:
                return true;
        }
    }

    @Override
    public Mouvement rechercherMouvementParReference(String reference) {
        Optional<Mouvement> mouvement = mouvementRepository.findByReference(reference);
        return mouvement.orElse(null);
    }

    /**
     * Méthodes utilitaires privées
     */
    private Mouvement creerMouvement(CompteCourant compte, BigDecimal montant, String libelle, TypeOperation typeOperation) {
        Mouvement mouvement = new Mouvement();
        mouvement.setCompte(compte);
        mouvement.setTypeOperation(typeOperation);
        mouvement.setMontant(montant);
        mouvement.setLibelleOperation(libelle);
        mouvement.setDateOperation(LocalDateTime.now());
        mouvement.setReference(genererReference(typeOperation.getCodeOperation()));
        
        // Calculer le nouveau solde après cette opération
        BigDecimal soldeAvant = calculerSolde(compte);
        mouvement.setSoldeAvantOperation(soldeAvant);
        mouvement.setSoldeApresOperation(soldeAvant.add(montant));

        return mouvement;
    }

    private BigDecimal calculerSolde(CompteCourant compte) {
        BigDecimal soldeInitial = compte.getClient().getSoldeInitial();
        BigDecimal totalMouvements = mouvementRepository.calculerSoldeMouvements(compte.getIdCompte());
        return soldeInitial.add(totalMouvements != null ? totalMouvements : BigDecimal.ZERO);
    }

    private String genererReference(String prefixe) {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 9999);
        return String.format("%s%d%04d", prefixe, timestamp, random);
    }
}