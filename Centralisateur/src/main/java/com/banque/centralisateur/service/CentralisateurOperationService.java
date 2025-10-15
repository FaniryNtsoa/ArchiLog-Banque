package com.banque.centralisateur.service;

import com.banque.centralisateur.client.SituationBancaireClient;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service centralisateur pour la gestion des opérations bancaires
 * Utilise les EJB distants du module SituationBancaire
 */
@Stateless
public class CentralisateurOperationService {
    
    private static final Logger logger = Logger.getLogger(CentralisateurOperationService.class.getName());
    
    @Inject
    private SituationBancaireClient ejbClient;
    
    /**
     * Effectue un dépôt sur un compte
     */
    public Object effectuerDepot(String numeroCompte, BigDecimal montant, String libelle) {
        try {
            logger.info("Dépôt de " + montant + " sur le compte " + numeroCompte);
            
            Object operationService = ejbClient.lookupRemoteBean(
                "OperationServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.OperationServiceRemote")
            );
            
            Object mouvement = operationService.getClass()
                .getMethod("effectuerDepot", String.class, BigDecimal.class, String.class)
                .invoke(operationService, numeroCompte, montant, libelle);
                
            logger.info("Dépôt effectué avec succès");
            return mouvement;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors du dépôt", e);
            throw new RuntimeException("Erreur lors du dépôt: " + e.getMessage(), e);
        }
    }
    
    /**
     * Effectue un retrait sur un compte
     */
    public Object effectuerRetrait(String numeroCompte, BigDecimal montant, String libelle) {
        try {
            logger.info("Retrait de " + montant + " du compte " + numeroCompte);
            
            Object operationService = ejbClient.lookupRemoteBean(
                "OperationServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.OperationServiceRemote")
            );
            
            Object mouvement = operationService.getClass()
                .getMethod("effectuerRetrait", String.class, BigDecimal.class, String.class)
                .invoke(operationService, numeroCompte, montant, libelle);
                
            logger.info("Retrait effectué avec succès");
            return mouvement;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors du retrait", e);
            throw new RuntimeException("Erreur lors du retrait: " + e.getMessage(), e);
        }
    }
    
    /**
     * Effectue un virement entre deux comptes
     */
    public Object effectuerVirement(String numeroCompteDebiteur, String numeroCompteCrediteur, 
                                   BigDecimal montant, String libelle) {
        try {
            logger.info("Virement de " + montant + " du compte " + numeroCompteDebiteur + 
                       " vers le compte " + numeroCompteCrediteur);
            
            Object operationService = ejbClient.lookupRemoteBean(
                "OperationServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.OperationServiceRemote")
            );
            
            Object virement = operationService.getClass()
                .getMethod("effectuerVirement", String.class, String.class, BigDecimal.class, String.class)
                .invoke(operationService, numeroCompteDebiteur, numeroCompteCrediteur, montant, libelle);
                
            logger.info("Virement effectué avec succès");
            return virement;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors du virement", e);
            throw new RuntimeException("Erreur lors du virement: " + e.getMessage(), e);
        }
    }
    
    /**
     * Récupère l'historique des mouvements d'un compte
     */
    @SuppressWarnings("unchecked")
    public List<Object> obtenirHistoriqueMouvements(String numeroCompte, LocalDate dateDebut, LocalDate dateFin) {
        try {
            logger.info("Récupération de l'historique du compte: " + numeroCompte + 
                       " du " + dateDebut + " au " + dateFin);
            
            Object operationService = ejbClient.lookupRemoteBean(
                "OperationServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.OperationServiceRemote")
            );
            
            List<Object> mouvements = (List<Object>) operationService.getClass()
                .getMethod("obtenirHistoriqueMouvements", String.class, LocalDate.class, LocalDate.class)
                .invoke(operationService, numeroCompte, dateDebut, dateFin);
                
            logger.info("Nombre de mouvements trouvés: " + mouvements.size());
            return mouvements;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la récupération de l'historique", e);
            throw new RuntimeException("Erreur lors de la récupération de l'historique: " + e.getMessage(), e);
        }
    }
    
    /**
     * Récupère l'historique complet d'un compte (sans filtres de dates)
     */
    public List<Object> obtenirHistoriqueComplet(String numeroCompte) {
        return obtenirHistoriqueMouvements(numeroCompte, null, null);
    }
    
    /**
     * Applique les frais de tenue de compte
     */
    public Object appliquerFraisTenueCompte(String numeroCompte) {
        try {
            logger.info("Application des frais de tenue de compte pour: " + numeroCompte);
            
            Object operationService = ejbClient.lookupRemoteBean(
                "OperationServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.OperationServiceRemote")
            );
            
            Object mouvement = operationService.getClass()
                .getMethod("appliquerFraisTenueCompte", String.class)
                .invoke(operationService, numeroCompte);
                
            logger.info("Frais de tenue appliqués avec succès");
            return mouvement;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de l'application des frais", e);
            throw new RuntimeException("Erreur lors de l'application des frais: " + e.getMessage(), e);
        }
    }
    
    /**
     * Applique les intérêts de découvert
     */
    public Object appliquerInteretsDecouvert(String numeroCompte) {
        try {
            logger.info("Application des intérêts de découvert pour: " + numeroCompte);
            
            Object operationService = ejbClient.lookupRemoteBean(
                "OperationServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.OperationServiceRemote")
            );
            
            Object mouvement = operationService.getClass()
                .getMethod("appliquerInteretsDecouvert", String.class)
                .invoke(operationService, numeroCompte);
                
            if (mouvement != null) {
                logger.info("Intérêts de découvert appliqués avec succès");
            } else {
                logger.info("Aucun intérêt de découvert à appliquer");
            }
            return mouvement;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de l'application des intérêts", e);
            throw new RuntimeException("Erreur lors de l'application des intérêts: " + e.getMessage(), e);
        }
    }
    
    /**
     * Vérifie les plafonds avant une opération
     */
    public boolean verifierPlafonds(String numeroCompte, BigDecimal montant, String typeOperation) {
        try {
            logger.info("Vérification des plafonds pour le compte: " + numeroCompte + 
                       " - Montant: " + montant + " - Type: " + typeOperation);
            
            Object operationService = ejbClient.lookupRemoteBean(
                "OperationServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.OperationServiceRemote")
            );
            
            Boolean autorise = (Boolean) operationService.getClass()
                .getMethod("verifierPlafonds", String.class, BigDecimal.class, String.class)
                .invoke(operationService, numeroCompte, montant, typeOperation);
                
            logger.info("Vérification des plafonds: " + (autorise ? "AUTORISÉ" : "REFUSÉ"));
            return autorise;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la vérification des plafonds", e);
            throw new RuntimeException("Erreur lors de la vérification des plafonds: " + e.getMessage(), e);
        }
    }
    
    /**
     * Recherche un mouvement par sa référence
     */
    public Object rechercherMouvementParReference(String reference) {
        try {
            logger.info("Recherche du mouvement avec référence: " + reference);
            
            Object operationService = ejbClient.lookupRemoteBean(
                "OperationServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.OperationServiceRemote")
            );
            
            Object mouvement = operationService.getClass()
                .getMethod("rechercherMouvementParReference", String.class)
                .invoke(operationService, reference);
                
            return mouvement;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche du mouvement", e);
            throw new RuntimeException("Erreur lors de la recherche du mouvement: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtient le résumé des opérations d'un compte sur une période
     */
    public Object obtenirResumeOperations(String numeroCompte, LocalDate dateDebut, LocalDate dateFin) {
        try {
            List<Object> mouvements = obtenirHistoriqueMouvements(numeroCompte, dateDebut, dateFin);
            
            // Création d'un résumé simple
            java.util.Map<String, Object> resume = new java.util.HashMap<>();
            resume.put("numeroCompte", numeroCompte);
            resume.put("periode", (dateDebut != null ? dateDebut : "Début") + " - " + (dateFin != null ? dateFin : "Fin"));
            resume.put("nombreOperations", mouvements.size());
            
            // Vous pouvez ajouter plus de calculs ici (totaux, moyennes, etc.)
            
            return resume;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la génération du résumé", e);
            throw new RuntimeException("Erreur lors de la génération du résumé: " + e.getMessage(), e);
        }
    }
}