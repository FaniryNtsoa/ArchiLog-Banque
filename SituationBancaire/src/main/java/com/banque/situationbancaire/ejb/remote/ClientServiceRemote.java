package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.entity.Client;
import jakarta.ejb.Remote;
import java.util.List;

/**
 * Interface remote pour le service de gestion des clients
 */
@Remote
public interface ClientServiceRemote {
    
    /**
     * Crée un nouveau client
     * @param client Le client à créer
     * @return Le client créé avec son ID
     */
    Client creerClient(Client client);
    
    /**
     * Recherche un client par son ID
     * @param idClient L'ID du client
     * @return Le client trouvé ou null
     */
    Client rechercherClientParId(Long idClient);
    
    /**
     * Recherche un client par son numéro
     * @param numeroClient Le numéro du client
     * @return Le client trouvé ou null
     */
    Client rechercherClientParNumero(String numeroClient);
    
    /**
     * Recherche un client par son email
     * @param email L'email du client
     * @return Le client trouvé ou null
     */
    Client rechercherClientParEmail(String email);
    
    /**
     * Met à jour un client
     * @param client Le client à mettre à jour
     * @return Le client mis à jour
     */
    Client modifierClient(Client client);
    
    /**
     * Liste tous les clients
     * @return La liste de tous les clients
     */
    List<Client> listerTousLesClients();
    
    /**
     * Supprime un client
     * @param idClient L'ID du client à supprimer
     */
    void supprimerClient(Long idClient);
    
    /**
     * Vérifie si un client existe par son email
     * @param email L'email à vérifier
     * @return true si l'email existe, false sinon
     */
    boolean existeParEmail(String email);
    
    /**
     * Vérifie si un client existe par son numéro CIN
     * @param numCin Le numéro CIN à vérifier
     * @return true si le CIN existe, false sinon
     */
    boolean existeParNumCin(String numCin);
}
