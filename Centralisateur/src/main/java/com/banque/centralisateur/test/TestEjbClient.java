package com.banque.centralisateur.test;

import com.banque.centralisateur.client.SituationBancaireClient;
import javax.naming.NamingException;

/**
 * Client de test standalone pour tester la connexion EJB
 * sans déploiement complet
 */
public class TestEjbClient {
    
    public static void main(String[] args) {
        System.out.println("=== Test de connexion EJB avec SituationBancaire ===");
        
        try {
            SituationBancaireClient client = new SituationBancaireClient();
            
            // Test de connexion
            System.out.println("1. Test de connexion...");
            boolean connected = client.testConnection();
            System.out.println("Connexion: " + (connected ? "SUCCESS" : "FAILED"));
            
            if (connected) {
                System.out.println("2. Test de lookup EJB...");
                
                // Simulation de lookup - en production, remplacer par les vraies interfaces
                // ClientServiceRemote clientService = client.lookupRemoteBean("ClientServiceImpl", ClientServiceRemote.class);
                // System.out.println("Lookup réussi: " + clientService);
                
                System.out.println("Lookup simulation: SUCCESS");
            }
            
        } catch (Exception e) {
            System.err.println("Erreur lors du test: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== Fin du test ===");
    }
}