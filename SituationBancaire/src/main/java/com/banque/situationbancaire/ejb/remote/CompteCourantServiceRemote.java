package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.entity.CompteCourant;
import jakarta.ejb.Remote;
import java.math.BigDecimal;
import java.util.List;

/**
 * Interface Remote pour la gestion des comptes courants
 */
@Remote
public interface CompteCourantServiceRemote {
    
    /**
     * Crée un nouveau compte courant
     * @param compte Informations du compte
     * @param idClient ID du client propriétaire
     * @return Le compte créé
     */
    CompteCourant creerCompte(CompteCourant compte, Long idClient);
    
    /**
     * Récupère un compte par son ID
     * @param idCompte ID du compte
     * @return Le compte trouvé ou null
     */
    CompteCourant rechercherCompteParId(Long idCompte);
    
    /**
     * Récupère un compte par son numéro
     * @param numeroCompte Numéro du compte
     * @return Le compte trouvé ou null
     */
    CompteCourant rechercherCompteParNumero(String numeroCompte);
    
    /**
     * Récupère tous les comptes d'un client
     * @param idClient ID du client
     * @return Liste des comptes du client
     */
    List<CompteCourant> listerComptesParClient(Long idClient);
    
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
    CompteCourant obtenirInfosCompte(String numeroCompte);
    
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
}
