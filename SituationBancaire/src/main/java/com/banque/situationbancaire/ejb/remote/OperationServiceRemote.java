package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.dto.MouvementDTO;
import com.banque.situationbancaire.dto.VirementDTO;
import jakarta.ejb.Remote;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Interface Remote pour la gestion des opérations bancaires
 */
@Remote
public interface OperationServiceRemote {
    
    /**
     * Effectue un dépôt sur un compte
     * @param numeroCompte Numéro du compte
     * @param montant Montant à déposer
     * @param libelle Libellé de l'opération
     * @return L'opération créée
     */
    MouvementDTO effectuerDepot(String numeroCompte, BigDecimal montant, String libelle);
    
    /**
     * Effectue un retrait sur un compte
     * @param numeroCompte Numéro du compte
     * @param montant Montant à retirer
     * @param libelle Libellé de l'opération
     * @return L'opération créée
     */
    MouvementDTO effectuerRetrait(String numeroCompte, BigDecimal montant, String libelle);
    
    /**
     * Effectue un virement entre deux comptes
     * @param numeroCompteDebiteur Compte à débiter
     * @param numeroCompteCrediteur Compte à créditer
     * @param montant Montant du virement
     * @param libelle Libellé du virement
     * @return Le virement créé
     */
    VirementDTO effectuerVirement(String numeroCompteDebiteur, String numeroCompteCrediteur, BigDecimal montant, String libelle);
    
    /**
     * Récupère l'historique des mouvements d'un compte
     * @param numeroCompte Numéro du compte
     * @param dateDebut Date de début (optionnel)
     * @param dateFin Date de fin (optionnel)
     * @return Liste des mouvements
     */
    List<MouvementDTO> obtenirHistoriqueMouvements(String numeroCompte, LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Applique les frais de tenue de compte périodiques
     * @param numeroCompte Numéro du compte
     * @return Le mouvement de frais créé
     */
    MouvementDTO appliquerFraisTenueCompte(String numeroCompte);
    
    /**
     * Calcule et applique les intérêts de découvert
     * @param numeroCompte Numéro du compte
     * @return Le mouvement d'intérêts créé (si applicable)
     */
    MouvementDTO appliquerInteretsDecouvert(String numeroCompte);
    
    /**
     * Vérifie les plafonds et limites avant une opération
     * @param numeroCompte Numéro du compte
     * @param montant Montant de l'opération
     * @param typeOperation Type d'opération (DEBIT/CREDIT)
     * @return true si l'opération est autorisée
     */
    boolean verifierPlafonds(String numeroCompte, BigDecimal montant, String typeOperation);
    
    /**
     * Récupère un mouvement par sa référence
     * @param reference Référence du mouvement
     * @return Le mouvement trouvé ou null
     */
    MouvementDTO rechercherMouvementParReference(String reference);
}
