package com.banque.centralisateur.rest;

import com.banque.centralisateur.service.CentralisateurClientService;
import com.banque.centralisateur.service.CentralisateurCompteService;
import com.banque.centralisateur.service.CentralisateurOperationService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * REST Controller complet pour toutes les fonctionnalités bancaires
 * via communication EJB avec SituationBancaire
 */
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestCommunicationResource {
    
    private static final Logger logger = Logger.getLogger(TestCommunicationResource.class.getName());
    
    @Inject
    private CentralisateurClientService clientService;
    
    @Inject
    private CentralisateurCompteService compteService;
    
    @Inject
    private CentralisateurOperationService operationService;
    

    
    // ========== GESTION DES CLIENTS ==========
    
    /**
     * Crée un nouveau client
     */
    @POST
    @Path("/clients")
    public Response creerClient(Map<String, Object> clientData) {
        try {
            logger.info("Création d'un nouveau client via REST");
            Object client = clientService.creerClient(clientData);
            return Response.status(Response.Status.CREATED).entity(client).build();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur REST lors de la création du client", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la création du client: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Récupère un client par son ID
     */
    @GET
    @Path("/clients/{id}")
    public Response getClient(@PathParam("id") Long clientId) {
        try {
            Object client = clientService.rechercherClientParId(clientId);
            
            if (client != null) {
                return Response.ok(client).build();
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Client non trouvé");
                return Response.status(Response.Status.NOT_FOUND).entity(error).build();
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération du client: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Récupère tous les clients
     */
    @GET
    @Path("/clients")
    public Response getAllClients() {
        try {
            List<Object> clients = clientService.listerTousLesClients();
            
            Map<String, Object> response = new HashMap<>();
            response.put("clients", clients);
            response.put("count", clients.size());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération des clients: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Récupère un client par son numéro
     */
    @GET
    @Path("/clients/numero/{numero}")
    public Response getClientByNumero(@PathParam("numero") String numeroClient) {
        try {
            Object client = clientService.rechercherClientParNumero(numeroClient);
            
            if (client != null) {
                return Response.ok(client).build();
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Client non trouvé avec le numéro: " + numeroClient);
                return Response.status(Response.Status.NOT_FOUND).entity(error).build();
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération du client: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Récupère un client par son email
     */
    @GET
    @Path("/clients/email/{email}")
    public Response getClientByEmail(@PathParam("email") String email) {
        try {
            Object client = clientService.rechercherClientParEmail(email);
            
            if (client != null) {
                return Response.ok(client).build();
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Client non trouvé avec l'email: " + email);
                return Response.status(Response.Status.NOT_FOUND).entity(error).build();
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération du client: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Modifie un client existant
     */
    @PUT
    @Path("/clients/{id}")
    public Response modifierClient(@PathParam("id") Long clientId, Object clientData) {
        try {
            Object client = clientService.modifierClient(clientData);
            return Response.ok(client).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la modification du client: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Supprime un client
     */
    @DELETE
    @Path("/clients/{id}")
    public Response supprimerClient(@PathParam("id") Long clientId) {
        try {
            clientService.supprimerClient(clientId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Client supprimé avec succès");
            return Response.ok(response).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la suppression du client: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    // ========== GESTION DES COMPTES ==========
    
    /**
     * Crée un nouveau compte pour un client
     */
    @POST
    @Path("/clients/{clientId}/comptes")
    public Response creerCompte(@PathParam("clientId") Long clientId, Map<String, Object> compteData) {
        try {
            Object compte = compteService.creerCompte(compteData, clientId);
            return Response.status(Response.Status.CREATED).entity(compte).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la création du compte: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Récupère les comptes d'un client
     */
    @GET
    @Path("/clients/{clientId}/comptes")
    public Response getComptesClient(@PathParam("clientId") Long clientId) {
        try {
            List<Object> comptes = compteService.listerComptesParClient(clientId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("comptes", comptes);
            response.put("count", comptes.size());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération des comptes: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Récupère un compte par son numéro
     */
    @GET
    @Path("/comptes/{numeroCompte}")
    public Response getCompte(@PathParam("numeroCompte") String numeroCompte) {
        try {
            Object compte = compteService.rechercherCompteParNumero(numeroCompte);
            
            if (compte != null) {
                return Response.ok(compte).build();
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Compte non trouvé: " + numeroCompte);
                return Response.status(Response.Status.NOT_FOUND).entity(error).build();
            }
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération du compte: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Consulte le solde d'un compte
     */
    @GET
    @Path("/comptes/{numeroCompte}/solde")
    public Response getSoldeCompte(@PathParam("numeroCompte") String numeroCompte) {
        try {
            BigDecimal solde = compteService.calculerSoldeActuel(numeroCompte);
            
            Map<String, Object> response = new HashMap<>();
            response.put("numeroCompte", numeroCompte);
            response.put("soldeActuel", solde);
            response.put("timestamp", System.currentTimeMillis());
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors du calcul du solde: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Bloque un compte
     */
    @PUT
    @Path("/comptes/{numeroCompte}/bloquer")
    public Response bloquerCompte(@PathParam("numeroCompte") String numeroCompte) {
        try {
            compteService.bloquerCompte(numeroCompte);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Compte bloqué avec succès");
            response.put("numeroCompte", numeroCompte);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors du blocage du compte: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Débloque un compte
     */
    @PUT
    @Path("/comptes/{numeroCompte}/debloquer")
    public Response debloquerCompte(@PathParam("numeroCompte") String numeroCompte) {
        try {
            compteService.debloquerCompte(numeroCompte);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Compte débloqué avec succès");
            response.put("numeroCompte", numeroCompte);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors du déblocage du compte: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    // ========== OPÉRATIONS BANCAIRES ==========
    
    /**
     * Effectue un dépôt
     */
    @POST
    @Path("/comptes/{numeroCompte}/depot")
    public Response effectuerDepot(@PathParam("numeroCompte") String numeroCompte, 
                                  Map<String, Object> operationData) {
        try {
            BigDecimal montant = new BigDecimal(operationData.get("montant").toString());
            String libelle = (String) operationData.get("libelle");
            
            Object mouvement = operationService.effectuerDepot(numeroCompte, montant, libelle);
            
            return Response.status(Response.Status.CREATED).entity(mouvement).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors du dépôt: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Effectue un retrait
     */
    @POST
    @Path("/comptes/{numeroCompte}/retrait")
    public Response effectuerRetrait(@PathParam("numeroCompte") String numeroCompte, 
                                   Map<String, Object> operationData) {
        try {
            BigDecimal montant = new BigDecimal(operationData.get("montant").toString());
            String libelle = (String) operationData.get("libelle");
            
            Object mouvement = operationService.effectuerRetrait(numeroCompte, montant, libelle);
            
            return Response.status(Response.Status.CREATED).entity(mouvement).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors du retrait: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Effectue un virement
     */
    @POST
    @Path("/virements")
    public Response effectuerVirement(Map<String, Object> virementData) {
        try {
            String compteDebiteur = (String) virementData.get("compteDebiteur");
            String compteCrediteur = (String) virementData.get("compteCrediteur");
            BigDecimal montant = new BigDecimal(virementData.get("montant").toString());
            String libelle = (String) virementData.get("libelle");
            
            Object virement = operationService.effectuerVirement(compteDebiteur, compteCrediteur, montant, libelle);
            
            return Response.status(Response.Status.CREATED).entity(virement).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors du virement: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    /**
     * Récupère l'historique des mouvements d'un compte
     */
    @GET
    @Path("/comptes/{numeroCompte}/historique")
    public Response getHistoriqueCompte(@PathParam("numeroCompte") String numeroCompte,
                                       @QueryParam("dateDebut") String dateDebutStr,
                                       @QueryParam("dateFin") String dateFinStr) {
        try {
            LocalDate dateDebut = null;
            LocalDate dateFin = null;
            
            if (dateDebutStr != null && !dateDebutStr.isEmpty()) {
                dateDebut = LocalDate.parse(dateDebutStr, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            if (dateFinStr != null && !dateFinStr.isEmpty()) {
                dateFin = LocalDate.parse(dateFinStr, DateTimeFormatter.ISO_LOCAL_DATE);
            }
            
            List<Object> mouvements = operationService.obtenirHistoriqueMouvements(numeroCompte, dateDebut, dateFin);
            
            Map<String, Object> response = new HashMap<>();
            response.put("numeroCompte", numeroCompte);
            response.put("mouvements", mouvements);
            response.put("count", mouvements.size());
            response.put("periode", dateDebutStr + " - " + dateFinStr);
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Erreur lors de la récupération de l'historique: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }
    
    // ========== ENDPOINTS DE TEST ==========
    
    /**
     * Test simple de connectivité
     */
    @GET
    @Path("/test/ping")
    public Response ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Centralisateur is running");
        response.put("timestamp", System.currentTimeMillis());
        response.put("version", "2.0.0");
        
        return Response.ok(response).build();
    }
    
    /**
     * Test de connexion EJB avec SituationBancaire
     */
    @GET
    @Path("/test/ejb-connection")
    public Response testEjbConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean connected = clientService.testerConnexion();
            response.put("ejbConnectionStatus", connected ? "SUCCESS" : "FAILED");
            response.put("message", connected ? 
                "Connexion EJB avec SituationBancaire établie" : 
                "Impossible de se connecter au module SituationBancaire");
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            response.put("ejbConnectionStatus", "ERROR");
            response.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                          .entity(response).build();
        }
    }
    
    /**
     * Test complet avec création d'un client et d'un compte
     */
    @POST
    @Path("/test/scenario-complet")
    public Response testScenarioComplet() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. Créer un client de test
            Map<String, Object> clientData = new HashMap<>();
            clientData.put("nom", "Test");
            clientData.put("prenom", "Utilisateur");
            clientData.put("email", "test@example.com");
            clientData.put("telephone", "0123456789");
            clientData.put("numCin", "TEST123456");
            clientData.put("dateNaissance", LocalDate.of(1990, 1, 1));
            
            Object client = clientService.creerClient(clientData);
            response.put("clientCree", client);
            
            // 2. Créer un compte pour ce client
            Map<String, Object> compteData = new HashMap<>();
            compteData.put("soldeActuel", new BigDecimal("1000.00"));
            compteData.put("decouvertAutorise", new BigDecimal("-500.00"));
            
            // Obtenir l'ID du client créé via reflection
            Long clientId = (Long) client.getClass().getMethod("getIdClient").invoke(client);
            
            Object compte = compteService.creerCompte(compteData, clientId);
            response.put("compteCree", compte);
            
            // 3. Effectuer un dépôt
            String numeroCompte = (String) compte.getClass().getMethod("getNumeroCompte").invoke(compte);
            Object depot = operationService.effectuerDepot(numeroCompte, new BigDecimal("250.00"), "Dépôt de test");
            response.put("depotEffectue", depot);
            
            // 4. Consulter le solde
            BigDecimal solde = compteService.calculerSoldeActuel(numeroCompte);
            response.put("soldeApresDepot", solde);
            
            response.put("status", "SUCCESS");
            response.put("message", "Scénario complet exécuté avec succès");
            
            return Response.ok(response).build();
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors du test du scénario complet", e);
            response.put("status", "ERROR");
            response.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(response).build();
        }
    }
}