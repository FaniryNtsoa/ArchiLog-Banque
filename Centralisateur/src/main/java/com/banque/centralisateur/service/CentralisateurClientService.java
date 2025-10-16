package com.banque.centralisateur.service;

import com.banque.centralisateur.client.SituationBancaireClient;
import com.banque.centralisateur.dto.ClientDTO;
import com.banque.centralisateur.interfaces.ClientServiceRemote;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.naming.NamingException;

/**
 * Service centralisateur pour la gestion complète des clients
 * Utilise les EJB distants du module SituationBancaire avec des DTOs
 */
@Stateless
public class CentralisateurClientService {
    
    private static final Logger logger = Logger.getLogger(CentralisateurClientService.class.getName());
    
    @Inject
    private SituationBancaireClient ejbClient;
    
    /**
     * Crée un nouveau client via EJB distant
     */
    public Object creerClient(Map<String, Object> clientData) {
        try {
            logger.info("Création d'un nouveau client...");
            
            // Le lookup se fait dynamiquement pour éviter les problèmes de dépendances
            Object clientService = ejbClient.lookupRemoteBean(
                "ClientServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.ClientServiceRemote")
            );
            
            // Création d'un objet Client via reflection
            Class<?> clientClass = Class.forName("com.banque.situationbancaire.entity.Client");
            Object client = clientClass.getDeclaredConstructor().newInstance();
            
            // Remplissage des données client via reflection
            remplirDonneesClient(client, clientData);
            
            // Appel de la méthode creerClient via reflection
            Object clientCree = clientService.getClass()
                .getMethod("creerClient", clientClass)
                .invoke(clientService, client);
                
            logger.info("Client créé avec succès");
            return clientCree;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la création du client", e);
            throw new RuntimeException("Erreur lors de la création du client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Recherche un client par son ID via EJB distant
     */
    public Object rechercherClientParId(Long idClient) {
        try {
            logger.info("Recherche du client avec ID: " + idClient);
            
            Object clientService = ejbClient.lookupRemoteBean(
                "ClientServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.ClientServiceRemote")
            );
            
            Object client = clientService.getClass()
                .getMethod("rechercherClientParId", Long.class)
                .invoke(clientService, idClient);
                
            return client;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche du client par ID", e);
            throw new RuntimeException("Erreur lors de la recherche du client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Recherche un client par son numéro
     */
    public Object rechercherClientParNumero(String numeroClient) {
        try {
            logger.info("Recherche du client avec numéro: " + numeroClient);
            
            Object clientService = ejbClient.lookupRemoteBean(
                "ClientServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.ClientServiceRemote")
            );
            
            Object client = clientService.getClass()
                .getMethod("rechercherClientParNumero", String.class)
                .invoke(clientService, numeroClient);
                
            return client;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche du client par numéro", e);
            throw new RuntimeException("Erreur lors de la recherche du client par numéro: " + e.getMessage(), e);
        }
    }
    
    /**
     * Recherche un client par son email
     */
    public Object rechercherClientParEmail(String email) {
        try {
            logger.info("Recherche du client avec email: " + email);
            
            Object clientService = ejbClient.lookupRemoteBean(
                "ClientServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.ClientServiceRemote")
            );
            
            Object client = clientService.getClass()
                .getMethod("rechercherClientParEmail", String.class)
                .invoke(clientService, email);
                
            return client;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche du client par email", e);
            throw new RuntimeException("Erreur lors de la recherche du client par email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Liste tous les clients
     */
    @SuppressWarnings("unchecked")
    public List<Object> listerTousLesClients() {
        try {
            logger.info("Récupération de la liste de tous les clients");
            
            Object clientService = ejbClient.lookupRemoteBean(
                "ClientServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.ClientServiceRemote")
            );
            
            List<Object> clients = (List<Object>) clientService.getClass()
                .getMethod("listerTousLesClients")
                .invoke(clientService);
                
            logger.info("Nombre de clients trouvés: " + clients.size());
            return clients;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la récupération de la liste des clients", e);
            throw new RuntimeException("Erreur lors de la récupération de la liste des clients: " + e.getMessage(), e);
        }
    }
    
    /**
     * Modifie un client existant
     */
    public Object modifierClient(Object client) {
        try {
            logger.info("Modification d'un client");
            
            Object clientService = ejbClient.lookupRemoteBean(
                "ClientServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.ClientServiceRemote")
            );
            
            Class<?> clientClass = Class.forName("com.banque.situationbancaire.entity.Client");
            Object clientModifie = clientService.getClass()
                .getMethod("modifierClient", clientClass)
                .invoke(clientService, client);
                
            logger.info("Client modifié avec succès");
            return clientModifie;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la modification du client", e);
            throw new RuntimeException("Erreur lors de la modification du client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Supprime un client
     */
    public void supprimerClient(Long idClient) {
        try {
            logger.info("Suppression du client avec ID: " + idClient);
            
            Object clientService = ejbClient.lookupRemoteBean(
                "ClientServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.ClientServiceRemote")
            );
            
            clientService.getClass()
                .getMethod("supprimerClient", Long.class)
                .invoke(clientService, idClient);
                
            logger.info("Client supprimé avec succès");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la suppression du client", e);
            throw new RuntimeException("Erreur lors de la suppression du client: " + e.getMessage(), e);
        }
    }
    
    /**
     * Vérifie si un email existe
     */
    public boolean existeParEmail(String email) {
        try {
            Object clientService = ejbClient.lookupRemoteBean(
                "ClientServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.ClientServiceRemote")
            );
            
            Boolean existe = (Boolean) clientService.getClass()
                .getMethod("existeParEmail", String.class)
                .invoke(clientService, email);
                
            return existe;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la vérification de l'email", e);
            throw new RuntimeException("Erreur lors de la vérification de l'email: " + e.getMessage(), e);
        }
    }
    
    /**
     * Vérifie si un numéro CIN existe
     */
    public boolean existeParNumCin(String numCin) {
        try {
            Object clientService = ejbClient.lookupRemoteBean(
                "ClientServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.ClientServiceRemote")
            );
            
            Boolean existe = (Boolean) clientService.getClass()
                .getMethod("existeParNumCin", String.class)
                .invoke(clientService, numCin);
                
            return existe;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la vérification du CIN", e);
            throw new RuntimeException("Erreur lors de la vérification du CIN: " + e.getMessage(), e);
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
    
    /**
     * Remplit les données d'un client via reflection
     */
    private void remplirDonneesClient(Object client, Map<String, Object> data) throws Exception {
        Class<?> clientClass = client.getClass();
        
        if (data.get("nom") != null) {
            clientClass.getMethod("setNom", String.class).invoke(client, data.get("nom"));
        }
        if (data.get("prenom") != null) {
            clientClass.getMethod("setPrenom", String.class).invoke(client, data.get("prenom"));
        }
        if (data.get("email") != null) {
            clientClass.getMethod("setEmail", String.class).invoke(client, data.get("email"));
        }
        if (data.get("telephone") != null) {
            clientClass.getMethod("setTelephone", String.class).invoke(client, data.get("telephone"));
        }
        if (data.get("adresse") != null) {
            clientClass.getMethod("setAdresse", String.class).invoke(client, data.get("adresse"));
        }
        if (data.get("numCin") != null) {
            clientClass.getMethod("setNumCin", String.class).invoke(client, data.get("numCin"));
        }
        if (data.get("dateNaissance") != null) {
            clientClass.getMethod("setDateNaissance", java.time.LocalDate.class)
                .invoke(client, data.get("dateNaissance"));
        }
    }
}