package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.dto.OperationDTO;

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
    OperationDTO deposer(String numeroCompte, BigDecimal montant, String libelle);
    
    /**
     * Effectue un retrait sur un compte
     * @param numeroCompte Numéro du compte
     * @param montant Montant à retirer
     * @param libelle Libellé de l'opération
     * @return L'opération créée
     */
    OperationDTO retirer(String numeroCompte, BigDecimal montant, String libelle);
    
    /**
     * Effectue un virement entre deux comptes
     * @param numeroCompteDebiteur Compte à débiter
     * @param numeroCompteCrediteur Compte à créditer
     * @param montant Montant du virement
     * @param libelle Libellé du virement
     * @return L'ID du virement créé
     */
    Long virer(String numeroCompteDebiteur, String numeroCompteCrediteur, BigDecimal montant, String libelle);
    
    /**
     * Récupère l'historique des opérations d'un compte
     * @param numeroCompte Numéro du compte
     * @param dateDebut Date de début (optionnel)
     * @param dateFin Date de fin (optionnel)
     * @return Liste des opérations
     */
    List<OperationDTO> getHistoriqueOperations(String numeroCompte, LocalDate dateDebut, LocalDate dateFin);
    
    /**
     * Récupère une opération par sa référence
     * @param reference Référence de l'opération
     * @return L'opération trouvée ou null
     */
    OperationDTO getOperationParReference(String reference);
}
