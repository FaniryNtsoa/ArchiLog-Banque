package com.banque.centralisateur.ejb;

import com.banque.situationbancaire.ejb.remote.ClientServiceRemote;
import com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote;
import com.banque.situationbancaire.ejb.remote.OperationServiceRemote;
import com.banque.situationbancaire.ejb.remote.TypeCompteServiceRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Factory pour créer des clients EJB distants
 * Gère les lookups JNDI vers le module SituationBancaire
 */
public class EJBClientFactory {
    
    private static final Logger LOGGER = Logger.getLogger(EJBClientFactory.class.getName());
    
    // Nom JNDI de l'application SituationBancaire déployée sur WildFly
    // Pour un WAR avec EJB: le app-name est vide, module-name est le nom du WAR sans extension
    private static final String APP_NAME = ""; // Vide pour un WAR simple
    private static final String MODULE_NAME = "situation-bancaire"; // Nom du WAR sans .war
    private static final String DISTINCT_NAME = "";
    
    private static Context context;
    
    /**
     * Initialise le contexte JNDI
     */
    private static synchronized Context getContext() throws NamingException {
        if (context == null) {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
            props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
            props.put("jboss.naming.client.ejb.context", true);
            
            context = new InitialContext(props);
            LOGGER.info("Contexte JNDI initialisé avec succès");
        }
        return context;
    }
    
    /**
     * Construit le nom JNDI pour un EJB distant
     */
    private static String buildJNDIName(String beanName, Class<?> interfaceClass) {
        // Format pour WAR: ejb:/module-name/bean-name!interface-name
        // Pour un WAR, APP_NAME doit être vide
        String jndiName;
        if (APP_NAME.isEmpty()) {
            jndiName = String.format("ejb:/%s/%s!%s",
                MODULE_NAME,
                beanName,
                interfaceClass.getName()
            );
        } else {
            jndiName = String.format("ejb:%s/%s/%s/%s!%s",
                APP_NAME,
                MODULE_NAME,
                DISTINCT_NAME,
                beanName,
                interfaceClass.getName()
            );
        }
        LOGGER.info("JNDI Name: " + jndiName);
        return jndiName;
    }
    
    /**
     * Récupère le service Client distant
     */
    public static ClientServiceRemote getClientService() {
        try {
            String jndiName = buildJNDIName("ClientServiceImpl", ClientServiceRemote.class);
            ClientServiceRemote service = (ClientServiceRemote) getContext().lookup(jndiName);
            LOGGER.info("ClientService EJB récupéré avec succès");
            return service;
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de ClientService", e);
            throw new RuntimeException("Impossible de se connecter au service Client distant", e);
        }
    }
    
    /**
     * Récupère le service CompteCourant distant
     */
    public static CompteCourantServiceRemote getCompteCourantService() {
        try {
            String jndiName = buildJNDIName("CompteCourantServiceImpl", CompteCourantServiceRemote.class);
            CompteCourantServiceRemote service = (CompteCourantServiceRemote) getContext().lookup(jndiName);
            LOGGER.info("CompteCourantService EJB récupéré avec succès");
            return service;
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de CompteCourantService", e);
            throw new RuntimeException("Impossible de se connecter au service CompteCourant distant", e);
        }
    }
    
    /**
     * Récupère le service Operation distant
     */
    public static OperationServiceRemote getOperationService() {
        try {
            String jndiName = buildJNDIName("OperationServiceImpl", OperationServiceRemote.class);
            OperationServiceRemote service = (OperationServiceRemote) getContext().lookup(jndiName);
            LOGGER.info("OperationService EJB récupéré avec succès");
            return service;
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de OperationService", e);
            throw new RuntimeException("Impossible de se connecter au service Operation distant", e);
        }
    }
    
    /**
     * Récupère le service TypeCompte distant
     */
    public static TypeCompteServiceRemote getTypeCompteService() {
        try {
            String jndiName = buildJNDIName("TypeCompteServiceImpl", TypeCompteServiceRemote.class);
            TypeCompteServiceRemote service = (TypeCompteServiceRemote) getContext().lookup(jndiName);
            LOGGER.info("TypeCompteService EJB récupéré avec succès");
            return service;
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de TypeCompteService", e);
            throw new RuntimeException("Impossible de se connecter au service TypeCompte distant", e);
        }
    }
    
    /**
     * Ferme le contexte JNDI
     */
    public static void closeContext() {
        if (context != null) {
            try {
                context.close();
                context = null;
                LOGGER.info("Contexte JNDI fermé");
            } catch (NamingException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture du contexte", e);
            }
        }
    }
}
