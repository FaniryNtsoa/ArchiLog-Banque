package com.banque.centralisateur.client;

import jakarta.json.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client REST pour communiquer avec l'API Épargne (.NET)
 * Gère les requêtes HTTP vers http://localhost:5000/api
 */
public class EpargneRestClient {
    
    private static final Logger LOGGER = Logger.getLogger(EpargneRestClient.class.getName());
    private static final String BASE_URL = "http://localhost:5000/api";
    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;
    
    /**
     * Inscrit un nouveau client dans le module Épargne
     */
    public JsonObject inscrireClient(String nom, String prenom, String email, String telephone,
                                     String dateNaissance, String numCin, String adresse,
                                     String codePostal, String ville, String profession,
                                     java.math.BigDecimal revenuMensuel, java.math.BigDecimal soldeInitial,
                                     String situationFamiliale, String motDePasse) {
        try {
            LOGGER.info("Inscription d'un client dans le module Épargne: " + email);
            
            // Construire le JSON de la requête
            JsonObject requestBody = Json.createObjectBuilder()
                .add("nom", nom)
                .add("prenom", prenom)
                .add("email", email)
                .add("telephone", telephone != null ? telephone : "")
                .add("dateNaissance", dateNaissance)
                .add("numCin", numCin)
                .add("adresse", adresse != null ? adresse : "")
                .add("codePostal", codePostal)
                .add("ville", ville)
                .add("profession", profession != null ? profession : "")
                .add("revenuMensuel", revenuMensuel)
                .add("soldeInitial", soldeInitial != null ? soldeInitial : java.math.BigDecimal.ZERO)
                .add("situationFamiliale", situationFamiliale != null ? situationFamiliale : "")
                .add("motDePasse", motDePasse)
                .build();
            
            // L'endpoint est /api/clients (pas /api/clients/register)
            return sendPostRequest("/clients", requestBody);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'inscription du client dans Épargne", e);
            throw new RuntimeException("Erreur lors de l'inscription dans le module Épargne: " + e.getMessage(), e);
        }
    }
    
    /**
     * Authentifie un client dans le module Épargne
     */
    public JsonObject authentifierClient(String email, String motDePasse) {
        try {
            LOGGER.info("Authentification du client dans le module Épargne: " + email);
            
            JsonObject requestBody = Json.createObjectBuilder()
                .add("email", email)
                .add("motDePasse", motDePasse)
                .build();
            
            return sendPostRequest("/clients/login", requestBody);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors de l'authentification dans Épargne", e);
            return null;
        }
    }
    
    /**
     * Récupère la liste des types de comptes épargne disponibles
     */
    public List<JsonObject> getTypesComptesActifs() {
        try {
            LOGGER.info("Récupération des types de comptes épargne actifs");
            
            JsonObject response = sendGetRequest("/typescomptes/actifs");
            
            if (response != null && response.getBoolean("success", false)) {
                JsonArray data = response.getJsonArray("data");
                List<JsonObject> types = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    types.add(data.getJsonObject(i));
                }
                return types;
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des types de comptes épargne", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Récupère les comptes épargne d'un client
     */
    public List<JsonObject> getComptesClient(Long clientId) {
        try {
            LOGGER.info("Récupération des comptes épargne du client: " + clientId);
            
            JsonObject response = sendGetRequest("/comptesepargne/client/" + clientId);
            
            if (response != null && response.getBoolean("success", false)) {
                JsonArray data = response.getJsonArray("data");
                List<JsonObject> comptes = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    comptes.add(data.getJsonObject(i));
                }
                return comptes;
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des comptes épargne", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Crée un nouveau compte épargne
     */
    public JsonObject creerCompteEpargne(Long clientId, Long typeCompteId, java.math.BigDecimal depotInitial) {
        try {
            LOGGER.info("Création d'un compte épargne pour le client: " + clientId);
            
            JsonObject requestBody = Json.createObjectBuilder()
                .add("idClient", clientId)
                .add("idTypeCompte", typeCompteId)
                .add("depotInitial", depotInitial)
                .build();
            
            return sendPostRequest("/comptesepargne", requestBody);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création du compte épargne", e);
            throw new RuntimeException("Erreur lors de la création du compte épargne: " + e.getMessage(), e);
        }
    }
    
    /**
     * Effectue un dépôt sur un compte épargne
     */
    public JsonObject effectuerDepot(Long compteId, java.math.BigDecimal montant, String description) {
        try {
            LOGGER.info("Dépôt de " + montant + " sur le compte épargne: " + compteId);
            
            JsonObject requestBody = Json.createObjectBuilder()
                .add("montant", montant)
                .add("description", description != null ? description : "Dépôt")
                .build();
            
            return sendPostRequest("/comptesepargne/" + compteId + "/depot", requestBody);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du dépôt sur compte épargne", e);
            throw new RuntimeException("Erreur lors du dépôt: " + e.getMessage(), e);
        }
    }
    
    /**
     * Effectue un retrait sur un compte épargne
     */
    public JsonObject effectuerRetrait(Long compteId, java.math.BigDecimal montant, String description) {
        try {
            LOGGER.info("Retrait de " + montant + " sur le compte épargne: " + compteId);
            
            JsonObject requestBody = Json.createObjectBuilder()
                .add("montant", montant)
                .add("description", description != null ? description : "Retrait")
                .build();
            
            return sendPostRequest("/comptesepargne/" + compteId + "/retrait", requestBody);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du retrait sur compte épargne", e);
            throw new RuntimeException("Erreur lors du retrait: " + e.getMessage(), e);
        }
    }
    
    /**
     * Récupère l'historique des opérations d'un compte épargne
     */
    public List<JsonObject> getOperationsCompte(Long compteId, int page, int pageSize) {
        try {
            LOGGER.info("Récupération des opérations du compte épargne: " + compteId);
            
            String url = "/comptesepargne/" + compteId + "/operations?page=" + page + "&pageSize=" + pageSize;
            JsonObject response = sendGetRequest(url);
            
            if (response != null && response.getBoolean("success", false)) {
                JsonArray data = response.getJsonArray("data");
                List<JsonObject> operations = new ArrayList<>();
                for (int i = 0; i < data.size(); i++) {
                    operations.add(data.getJsonObject(i));
                }
                return operations;
            }
            
            return new ArrayList<>();
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des opérations épargne", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Récupère les détails d'un compte épargne
     */
    public JsonObject getCompteDetails(Long compteId) {
        try {
            LOGGER.info("Récupération des détails du compte épargne: " + compteId);
            
            return sendGetRequest("/comptesepargne/" + compteId);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des détails du compte épargne", e);
            return null;
        }
    }
    
    // ========== Méthodes privées pour les requêtes HTTP ==========
    
    /**
     * Envoie une requête GET
     */
    private JsonObject sendGetRequest(String endpoint) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            
            int responseCode = conn.getResponseCode();
            LOGGER.info("GET " + endpoint + " - Response Code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readJsonResponse(conn.getInputStream());
            } else {
                LOGGER.warning("Erreur HTTP " + responseCode + " lors de GET " + endpoint);
                return readJsonResponse(conn.getErrorStream());
            }
            
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * Envoie une requête POST
     */
    private JsonObject sendPostRequest(String endpoint, JsonObject requestBody) throws IOException {
        URL url = new URL(BASE_URL + endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            
            // Écrire le corps de la requête
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = requestBody.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            
            int responseCode = conn.getResponseCode();
            LOGGER.info("POST " + endpoint + " - Response Code: " + responseCode);
            
            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                return readJsonResponse(conn.getInputStream());
            } else {
                LOGGER.warning("Erreur HTTP " + responseCode + " lors de POST " + endpoint);
                return readJsonResponse(conn.getErrorStream());
            }
            
        } finally {
            conn.disconnect();
        }
    }
    
    /**
     * Lit la réponse JSON depuis un flux
     * Gère à la fois les objets JSON et les tableaux JSON
     */
    private JsonObject readJsonResponse(InputStream inputStream) {
        if (inputStream == null) {
            return Json.createObjectBuilder()
                .add("success", false)
                .add("message", "Aucune réponse du serveur")
                .build();
        }
        
        try (JsonReader jsonReader = Json.createReader(inputStream)) {
            // Détecter si la réponse est un objet ou un tableau
            JsonStructure structure = jsonReader.read();
            
            if (structure instanceof JsonObject) {
                // Réponse est un objet JSON
                return (JsonObject) structure;
            } else if (structure instanceof JsonArray) {
                // Réponse est un tableau JSON - l'envelopper dans un objet standard
                return Json.createObjectBuilder()
                    .add("success", true)
                    .add("data", (JsonArray) structure)
                    .build();
            } else {
                return Json.createObjectBuilder()
                    .add("success", false)
                    .add("message", "Format de réponse inattendu")
                    .build();
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Erreur lors de la lecture de la réponse JSON", e);
            return Json.createObjectBuilder()
                .add("success", false)
                .add("message", "Erreur lors de la lecture de la réponse")
                .build();
        }
    }
}
