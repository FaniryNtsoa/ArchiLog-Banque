package com.banque.centralisateur.service;

import com.banque.centralisateur.client.SituationBancaireClient;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Service centralisateur pour la gestion des comptes courants
 * Utilise les EJB distants du module SituationBancaire
 */
@Stateless
public class CentralisateurCompteService {
    
    private static final Logger logger = Logger.getLogger(CentralisateurCompteService.class.getName());
    
    @Inject
    private SituationBancaireClient ejbClient;
    
    /**
     * Crée un nouveau compte courant
     */
    public Object creerCompte(Map<String, Object> compteData, Long idClient) {
        try {
            logger.info("Création d'un nouveau compte pour le client ID: " + idClient);
            
            Object compteService = ejbClient.lookupRemoteBean(
                "CompteCourantServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote")
            );
            
            // Création d'un objet CompteCourant via reflection
            Class<?> compteClass = Class.forName("com.banque.situationbancaire.entity.CompteCourant");
            Object compte = compteClass.getDeclaredConstructor().newInstance();
            
            // Remplissage des données compte via reflection
            remplirDonneesCompte(compte, compteData);
            
            // Appel de la méthode creerCompte via reflection
            Object compteCree = compteService.getClass()
                .getMethod("creerCompte", compteClass, Long.class)
                .invoke(compteService, compte, idClient);
                
            logger.info("Compte créé avec succès");
            return compteCree;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la création du compte", e);
            throw new RuntimeException("Erreur lors de la création du compte: " + e.getMessage(), e);
        }
    }
    
    /**
     * Recherche un compte par son ID
     */
    public Object rechercherCompteParId(Long idCompte) {
        try {
            logger.info("Recherche du compte avec ID: " + idCompte);
            
            Object compteService = ejbClient.lookupRemoteBean(
                "CompteCourantServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote")
            );
            
            Object compte = compteService.getClass()
                .getMethod("rechercherCompteParId", Long.class)
                .invoke(compteService, idCompte);
                
            return compte;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche du compte par ID", e);
            throw new RuntimeException("Erreur lors de la recherche du compte: " + e.getMessage(), e);
        }
    }
    
    /**
     * Recherche un compte par son numéro
     */
    public Object rechercherCompteParNumero(String numeroCompte) {
        try {
            logger.info("Recherche du compte avec numéro: " + numeroCompte);
            
            Object compteService = ejbClient.lookupRemoteBean(
                "CompteCourantServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote")
            );
            
            Object compte = compteService.getClass()
                .getMethod("rechercherCompteParNumero", String.class)
                .invoke(compteService, numeroCompte);
                
            return compte;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la recherche du compte par numéro", e);
            throw new RuntimeException("Erreur lors de la recherche du compte: " + e.getMessage(), e);
        }
    }
    
    /**
     * Liste tous les comptes d'un client
     */
    @SuppressWarnings("unchecked")
    public List<Object> listerComptesParClient(Long idClient) {
        try {
            logger.info("Récupération des comptes du client ID: " + idClient);
            
            Object compteService = ejbClient.lookupRemoteBean(
                "CompteCourantServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote")
            );
            
            List<Object> comptes = (List<Object>) compteService.getClass()
                .getMethod("listerComptesParClient", Long.class)
                .invoke(compteService, idClient);
                
            logger.info("Nombre de comptes trouvés: " + comptes.size());
            return comptes;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la récupération des comptes du client", e);
            throw new RuntimeException("Erreur lors de la récupération des comptes: " + e.getMessage(), e);
        }
    }
    
    /**
     * Calcule le solde actuel d'un compte
     */
    public BigDecimal calculerSoldeActuel(String numeroCompte) {
        try {
            logger.info("Calcul du solde pour le compte: " + numeroCompte);
            
            Object compteService = ejbClient.lookupRemoteBean(
                "CompteCourantServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote")
            );
            
            BigDecimal solde = (BigDecimal) compteService.getClass()
                .getMethod("calculerSoldeActuel", String.class)
                .invoke(compteService, numeroCompte);
                
            logger.info("Solde calculé: " + solde);
            return solde;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors du calcul du solde", e);
            throw new RuntimeException("Erreur lors du calcul du solde: " + e.getMessage(), e);
        }
    }
    
    /**
     * Obtient les informations complètes d'un compte
     */
    public Object obtenirInfosCompte(String numeroCompte) {
        try {
            logger.info("Récupération des informations du compte: " + numeroCompte);
            
            Object compteService = ejbClient.lookupRemoteBean(
                "CompteCourantServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote")
            );
            
            Object compte = compteService.getClass()
                .getMethod("obtenirInfosCompte", String.class)
                .invoke(compteService, numeroCompte);
                
            return compte;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la récupération des informations du compte", e);
            throw new RuntimeException("Erreur lors de la récupération des informations: " + e.getMessage(), e);
        }
    }
    
    /**
     * Ferme un compte
     */
    public void fermerCompte(String numeroCompte, String motif) {
        try {
            logger.info("Fermeture du compte: " + numeroCompte + " - Motif: " + motif);
            
            Object compteService = ejbClient.lookupRemoteBean(
                "CompteCourantServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote")
            );
            
            compteService.getClass()
                .getMethod("fermerCompte", String.class, String.class)
                .invoke(compteService, numeroCompte, motif);
                
            logger.info("Compte fermé avec succès");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la fermeture du compte", e);
            throw new RuntimeException("Erreur lors de la fermeture du compte: " + e.getMessage(), e);
        }
    }
    
    /**
     * Bloque un compte
     */
    public void bloquerCompte(String numeroCompte) {
        try {
            logger.info("Blocage du compte: " + numeroCompte);
            
            Object compteService = ejbClient.lookupRemoteBean(
                "CompteCourantServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote")
            );
            
            compteService.getClass()
                .getMethod("bloquerCompte", String.class)
                .invoke(compteService, numeroCompte);
                
            logger.info("Compte bloqué avec succès");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors du blocage du compte", e);
            throw new RuntimeException("Erreur lors du blocage du compte: " + e.getMessage(), e);
        }
    }
    
    /**
     * Débloque un compte
     */
    public void debloquerCompte(String numeroCompte) {
        try {
            logger.info("Déblocage du compte: " + numeroCompte);
            
            Object compteService = ejbClient.lookupRemoteBean(
                "CompteCourantServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote")
            );
            
            compteService.getClass()
                .getMethod("debloquerCompte", String.class)
                .invoke(compteService, numeroCompte);
                
            logger.info("Compte débloqué avec succès");
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors du déblocage du compte", e);
            throw new RuntimeException("Erreur lors du déblocage du compte: " + e.getMessage(), e);
        }
    }
    
    /**
     * Vérifie si un compte existe et est actif
     */
    public boolean compteExisteEtActif(String numeroCompte) {
        try {
            Object compteService = ejbClient.lookupRemoteBean(
                "CompteCourantServiceImpl", 
                Class.forName("com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote")
            );
            
            Boolean existe = (Boolean) compteService.getClass()
                .getMethod("compteExisteEtActif", String.class)
                .invoke(compteService, numeroCompte);
                
            return existe;
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Erreur lors de la vérification du compte", e);
            throw new RuntimeException("Erreur lors de la vérification du compte: " + e.getMessage(), e);
        }
    }
    
    /**
     * Remplit les données d'un compte via reflection
     */
    private void remplirDonneesCompte(Object compte, Map<String, Object> data) throws Exception {
        Class<?> compteClass = compte.getClass();
        
        if (data.get("numeroCompte") != null) {
            compteClass.getMethod("setNumeroCompte", String.class).invoke(compte, data.get("numeroCompte"));
        }
        if (data.get("soldeActuel") != null) {
            compteClass.getMethod("setSoldeActuel", BigDecimal.class).invoke(compte, data.get("soldeActuel"));
        }
        if (data.get("decouvertAutorise") != null) {
            compteClass.getMethod("setDecouvertAutorise", BigDecimal.class).invoke(compte, data.get("decouvertAutorise"));
        }
    }
}