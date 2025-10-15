package com.banque.centralisateur.rest;

import com.banque.centralisateur.dto.ClientInfoDTO;
import com.banque.centralisateur.service.CentralisateurClientService;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller pour tester la communication EJB avec SituationBancaire
 */
@Path("/api/test")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TestCommunicationResource {
    
    @Inject
    private CentralisateurClientService clientService;
    
    /**
     * Test simple de connectivité
     */
    @GET
    @Path("/ping")
    public Response ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Centralisateur is running");
        response.put("timestamp", System.currentTimeMillis());
        
        return Response.ok(response).build();
    }
    
    /**
     * Test de connexion EJB avec SituationBancaire
     */
    @GET
    @Path("/ejb-connection")
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
     * Test de récupération d'un client par ID
     */
    @GET
    @Path("/clients/{id}")
    public Response getClient(@PathParam("id") Long clientId) {
        try {
            ClientInfoDTO client = clientService.rechercherClientParId(clientId);
            
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
     * Test de récupération de tous les clients
     */
    @GET
    @Path("/clients")
    public Response getAllClients() {
        try {
            List<ClientInfoDTO> clients = clientService.listerTousLesClients();
            
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
     * Test de récupération d'un client par numéro
     */
    @GET
    @Path("/clients/numero/{numero}")
    public Response getClientByNumero(@PathParam("numero") String numeroClient) {
        try {
            ClientInfoDTO client = clientService.rechercherClientParNumero(numeroClient);
            
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
}