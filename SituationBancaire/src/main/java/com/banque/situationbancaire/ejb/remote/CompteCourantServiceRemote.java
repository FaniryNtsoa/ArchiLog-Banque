package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.dto.CompteCourantDTO;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interface Remote pour la gestion des comptes courants
 */
public interface CompteCourantServiceRemote {
    
    /**
     * Crée un nouveau compte courant
     * @param compteDTO Informations du compte
     * @param idClient ID du client propriétaire
     * @return Le compte créé
     */
    CompteCourantDTO creerCompte(CompteCourantDTO compteDTO, Long idClient);
    
    /**
     * Récupère un compte par son ID
     * @param idCompte ID du compte
     * @return Le compte trouvé ou null
     */
    CompteCourantDTO rechercherCompteParId(Long idCompte);
    
    /**
     * Récupère un compte par son numéro
     * @param numeroCompte Numéro du compte
     * @return Le compte trouvé ou null
     */
    CompteCourantDTO rechercherCompteParNumero(String numeroCompte);
    
    /**
     * Récupère tous les comptes d'un client
     * @param idClient ID du client
     * @return Liste des comptes du client
     */
    List<CompteCourantDTO> listerComptesParClient(Long idClient);
    
    /**
     * Calcule le solde actuel d'un compte (solde + tous les mouvements)
     * @param numeroCompte Numéro du compte
     * @return Le solde actuel calculé
     */
    BigDecimal calculerSoldeActuel(String numeroCompte);
    
    /**
     * Obtient les informations complètes d'un compte avec son solde
     * @param numeroCompte Numéro du compte
     * @return Les informations du compte
     */
    CompteCourantDTO obtenirInfosCompte(String numeroCompte);
    
    /**
     * Ferme un compte
     * @param numeroCompte Numéro du compte à fermer
     * @param motif Motif de fermeture
     */
    void fermerCompte(String numeroCompte, String motif);
    
    /**
     * Bloque un compte
     * @param numeroCompte Numéro du compte à bloquer
     */
    void bloquerCompte(String numeroCompte);
    
    /**
     * Débloque un compte
     * @param numeroCompte Numéro du compte à débloquer
     */
    void debloquerCompte(String numeroCompte);
    
    /**
     * Vérifie si un compte existe et est actif
     * @param numeroCompte Numéro du compte
     * @return true si le compte existe et est actif
     */
    boolean compteExisteEtActif(String numeroCompte);
    
    /**
     * Liste tous les types de comptes disponibles
     * @return Liste des types de comptes avec leurs paramètres
     */
    List<String> listerTypesComptesDisponibles();
}
