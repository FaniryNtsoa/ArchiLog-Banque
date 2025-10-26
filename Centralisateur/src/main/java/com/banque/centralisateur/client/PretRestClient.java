package com.banque.centralisateur.client;

import jakarta.json.JsonObject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client REST pour l'interaction avec le module Prêt
 * TODO: Implémenter les méthodes pour communiquer avec le module Prêt
 */
public class PretRestClient {
    
    private static final Logger logger = Logger.getLogger(PretRestClient.class.getName());
    private static final String PRET_BASE_URL = "http://localhost:8080/pret"; // À adapter selon la configuration
    
    private final Client client;
    
    public PretRestClient() {
        this.client = ClientBuilder.newClient();
    }
    
    /**
     * Récupère toutes les demandes de prêt
     * @return Liste des demandes de prêt
     */
    public List<JsonObject> getAllDemandes() {
        try {
            WebTarget target = client.target(PRET_BASE_URL).path("/api/prets/demandes");
            
            Response response = target.request(MediaType.APPLICATION_JSON).get();
            
            if (response.getStatus() == 200) {
                // TODO: Parser la réponse JSON
                logger.info("Demandes de prêt récupérées avec succès");
                return new ArrayList<>(); // Temporaire
            } else {
                logger.warning("Erreur lors de la récupération des demandes: " + response.getStatus());
                return new ArrayList<>();
            }
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la communication avec le module Prêt", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Récupère tous les prêts approuvés
     * @return Liste des prêts approuvés
     */
    public List<JsonObject> getAllPrets() {
        // TODO: Implémenter la récupération des prêts
        return new ArrayList<>();
    }
    
    /**
     * Récupère les types de prêt disponibles
     * @return Liste des types de prêt
     */
    public List<JsonObject> getTypesPret() {
        // TODO: Implémenter la récupération des types de prêt
        return new ArrayList<>();
    }
    
    /**
     * Simule un prêt
     * @param montant Montant du prêt
     * @param duree Durée en mois
     * @param typePret Type de prêt
     * @return Résultat de la simulation
     */
    public JsonObject simulerPret(Double montant, Integer duree, String typePret) {
        // TODO: Implémenter la simulation de prêt
        logger.info("Simulation de prêt demandée: " + montant + "€ sur " + duree + " mois");
        return null;
    }
    
    /**
     * Ferme le client REST
     */
    public void close() {
        if (client != null) {
            client.close();
        }
    }
}