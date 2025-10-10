package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.dto.CompteCourantDTO;

import jakarta.ejb.Remote;
import java.util.List;

/**
 * Interface Remote pour la gestion des comptes courants
 */
@Remote
public interface CompteCourantServiceRemote {
    
    /**
     * Crée un nouveau compte courant
     * @param compteDTO Informations du compte
     * @return Le compte créé
     */
    CompteCourantDTO creerCompte(CompteCourantDTO compteDTO);
    
    /**
     * Récupère un compte par son ID
     * @param idCompte ID du compte
     * @return Le compte trouvé ou null
     */
    CompteCourantDTO getCompteParId(Long idCompte);
    
    /**
     * Récupère un compte par son numéro
     * @param numeroCompte Numéro du compte
     * @return Le compte trouvé ou null
     */
    CompteCourantDTO getCompteParNumero(String numeroCompte);
    
    /**
     * Récupère tous les comptes d'un client
     * @param idClient ID du client
     * @return Liste des comptes du client
     */
    List<CompteCourantDTO> getComptesParClient(Long idClient);
    
    /**
     * Récupère le solde actuel d'un compte
     * @param numeroCompte Numéro du compte
     * @return Le solde actuel ou null si compte inexistant
     */
    java.math.BigDecimal getSoldeActuel(String numeroCompte);
    
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
}
