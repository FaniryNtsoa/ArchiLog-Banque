package com.banque.situationbancaire.ejb.remote;

import com.banque.situationbancaire.dto.ClientDTO;

import jakarta.ejb.Remote;
import java.util.List;

/**
 * Interface Remote pour la gestion des clients
 * Cette interface sera utilisée par le module Centralisateur pour accéder aux services
 */
@Remote
public interface ClientServiceRemote {
    
    /**
     * Crée un nouveau client
     * @param clientDTO Informations du client
     * @return Le client créé
     */
    ClientDTO creerClient(ClientDTO clientDTO);
    
    /**
     * Récupère un client par son ID
     * @param idClient ID du client
     * @return Le client trouvé ou null
     */
    ClientDTO getClientParId(Long idClient);
    
    /**
     * Récupère un client par son numéro
     * @param numeroClient Numéro du client
     * @return Le client trouvé ou null
     */
    ClientDTO getClientParNumero(String numeroClient);
    
    /**
     * Récupère tous les clients
     * @return Liste de tous les clients
     */
    List<ClientDTO> getAllClients();
    
    /**
     * Met à jour un client
     * @param clientDTO Informations du client à mettre à jour
     * @return Le client mis à jour
     */
    ClientDTO mettreAJourClient(ClientDTO clientDTO);
    
    /**
     * Supprime un client (désactive)
     * @param idClient ID du client à supprimer
     */
    void supprimerClient(Long idClient);
    
    /**
     * Vérifie si un client existe
     * @param numeroClient Numéro du client
     * @return true si le client existe
     */
    boolean clientExiste(String numeroClient);
}
