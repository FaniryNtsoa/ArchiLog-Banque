package com.banque.centralisateur.interfaces;

import com.banque.centralisateur.dto.ClientDTO;
import java.util.List;

/**
 * Interface pour communiquer avec le service Client distant
 * Cette interface définit les méthodes disponibles via EJB Remote
 */
public interface ClientServiceRemote {
    
    /**
     * Crée un nouveau client
     * @param clientDTO Le client à créer
     * @return Le client créé avec son ID
     */
    ClientDTO creerClient(ClientDTO clientDTO);
    
    /**
     * Recherche un client par son ID
     * @param idClient L'ID du client
     * @return Le client trouvé ou null
     */
    ClientDTO rechercherClientParId(Long idClient);
    
    /**
     * Recherche un client par son numéro
     * @param numeroClient Le numéro du client
     * @return Le client trouvé ou null
     */
    ClientDTO rechercherClientParNumero(String numeroClient);
    
    /**
     * Recherche un client par son email
     * @param email L'email du client
     * @return Le client trouvé ou null
     */
    ClientDTO rechercherClientParEmail(String email);
    
    /**
     * Met à jour un client
     * @param clientDTO Le client à mettre à jour
     * @return Le client mis à jour
     */
    ClientDTO modifierClient(ClientDTO clientDTO);
    
    /**
     * Liste tous les clients
     * @return La liste de tous les clients
     */
    List<ClientDTO> listerTousLesClients();
    
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