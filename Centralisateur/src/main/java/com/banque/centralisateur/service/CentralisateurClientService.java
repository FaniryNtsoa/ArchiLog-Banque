package com.banque.centralisateur.service;

import com.banque.centralisateur.client.SituationBancaireClient;
import com.banque.centralisateur.dto.ClientInfoDTO;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;
import java.util.ArrayList;
import javax.naming.NamingException;

/**
 * Service centralisateur pour la gestion des clients
 * Utilise les EJB distants du module SituationBancaire
 */
@Stateless
public class CentralisateurClientService {
    
    @Inject
    private SituationBancaireClient ejbClient;
    
    /**
     * Recherche un client par son ID via EJB distant
     */
    public ClientInfoDTO rechercherClientParId(Long idClient) {
        try {
            // Utilisation d'un exemple simple car nous n'avons pas les interfaces
            // En production, nous ferions le lookup et l'appel EJB
            
            // ClientServiceRemote clientService = ejbClient.lookupRemoteBean("ClientServiceImpl", ClientServiceRemote.class);
            // Client client = clientService.rechercherClientParId(idClient);
            // return convertToDTO(client);
            
            // Simulation pour le test
            return new ClientInfoDTO(
                idClient,
                "CLI" + idClient,
                "Dupont",
                "Jean",
                "jean.dupont@email.com",
                "0123456789",
                "123 Rue de la Paix",
                null,
                "123456789",
                "CELIBATAIRE",
                "ACTIF"
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche du client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Recherche un client par son numéro
     */
    public ClientInfoDTO rechercherClientParNumero(String numeroClient) {
        try {
            // En production: lookup EJB et appel de méthode
            // Simulation pour le test
            return new ClientInfoDTO(
                1L,
                numeroClient,
                "Martin",
                "Marie",
                "marie.martin@email.com",
                "0987654321",
                "456 Avenue des Champs",
                null,
                "987654321",
                "MARIE",
                "ACTIF"
            );
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la recherche du client par numéro: " + e.getMessage(), e);
        }
    }
    
    /**
     * Liste tous les clients
     */
    public List<ClientInfoDTO> listerTousLesClients() {
        try {
            // En production: lookup EJB et appel de méthode
            List<ClientInfoDTO> clients = new ArrayList<>();
            
            // Simulation pour le test
            clients.add(new ClientInfoDTO(1L, "CLI001", "Dupont", "Jean", 
                    "jean.dupont@email.com", "0123456789", "123 Rue de la Paix", 
                    null, "123456789", "CELIBATAIRE", "ACTIF"));
            
            clients.add(new ClientInfoDTO(2L, "CLI002", "Martin", "Marie", 
                    "marie.martin@email.com", "0987654321", "456 Avenue des Champs", 
                    null, "987654321", "MARIE", "ACTIF"));
            
            return clients;
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la récupération de la liste des clients: " + e.getMessage(), e);
        }
    }
    
    /**
     * Test de connectivité avec le module SituationBancaire
     */
    public boolean testerConnexion() {
        try {
            return ejbClient.testConnection();
        } catch (Exception e) {
            return false;
        }
    }
}