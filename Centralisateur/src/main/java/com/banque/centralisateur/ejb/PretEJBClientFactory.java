package com.banque.centralisateur.ejb;

import com.banque.pret.ejb.remote.ClientServiceRemote;
import com.banque.pret.ejb.remote.PretServiceRemote;
import com.banque.pret.ejb.remote.EcheanceServiceRemote;
import com.banque.pret.ejb.remote.TypePretServiceRemote;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Factory pour créer des clients EJB distants vers le module Prêt
 * Gère les lookups JNDI vers le module Prêt sur le port 8180
 */
public class PretEJBClientFactory {
    
    private static final Logger LOGGER = Logger.getLogger(PretEJBClientFactory.class.getName());
    
    // Nom JNDI de l'application Prêt déployée sur WildFly (port 8180)
    private static final String APP_NAME = ""; // Vide pour un WAR simple
    private static final String MODULE_NAME = "pret"; // Nom du WAR sans .war
    private static final String DISTINCT_NAME = "";
    
    private static Context context;
    
    /**
     * Initialise le contexte JNDI pour le module Prêt (port 8180)
     */
    private static synchronized Context getContext() throws NamingException {
        if (context == null) {
            Properties props = new Properties();
            props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
            // Port 8180 pour le module Prêt (offset +100)
            props.put(Context.PROVIDER_URL, "http-remoting://localhost:8180");
            props.put("jboss.naming.client.ejb.context", true);
            
            context = new InitialContext(props);
            LOGGER.info("Contexte JNDI Prêt initialisé avec succès (port 8180)");
        }
        return context;
    }
    
    /**
     * Construit le nom JNDI pour un EJB distant
     */
    private static String buildJNDIName(String beanName, Class<?> interfaceClass) {
        // Format pour WAR: ejb:/module-name/bean-name!interface-name
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
        LOGGER.info("JNDI Name (Prêt): " + jndiName);
        return jndiName;
    }
    
    /**
     * Récupère le service Client distant du module Prêt
     */
    public static ClientServiceRemote getClientService() {
        try {
            String jndiName = buildJNDIName("ClientServiceImpl", ClientServiceRemote.class);
            ClientServiceRemote service = (ClientServiceRemote) getContext().lookup(jndiName);
            LOGGER.info("ClientService Prêt EJB récupéré avec succès");
            return service;
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de ClientService Prêt", e);
            throw new RuntimeException("Impossible de se connecter au service Client Prêt distant", e);
        }
    }
    
    /**
     * Récupère le service Prêt distant
     */
    public static PretServiceRemote getPretService() {
        try {
            String jndiName = buildJNDIName("PretServiceImpl", PretServiceRemote.class);
            PretServiceRemote service = (PretServiceRemote) getContext().lookup(jndiName);
            LOGGER.info("PretService EJB récupéré avec succès");
            return service;
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de PretService", e);
            throw new RuntimeException("Impossible de se connecter au service Prêt distant", e);
        }
    }
    
    /**
     * Récupère le service Echeance distant
     */
    public static EcheanceServiceRemote getEcheanceService() {
        try {
            String jndiName = buildJNDIName("EcheanceServiceImpl", EcheanceServiceRemote.class);
            EcheanceServiceRemote service = (EcheanceServiceRemote) getContext().lookup(jndiName);
            LOGGER.info("EcheanceService EJB récupéré avec succès");
            return service;
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de EcheanceService", e);
            throw new RuntimeException("Impossible de se connecter au service Echeance distant", e);
        }
    }
    
    /**
     * Récupère le service TypePret distant
     */
    public static TypePretServiceRemote getTypePretService() {
        try {
            String jndiName = buildJNDIName("TypePretServiceImpl", TypePretServiceRemote.class);
            TypePretServiceRemote service = (TypePretServiceRemote) getContext().lookup(jndiName);
            LOGGER.info("TypePretService EJB récupéré avec succès");
            return service;
        } catch (NamingException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de TypePretService", e);
            throw new RuntimeException("Impossible de se connecter au service TypePret distant", e);
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
                LOGGER.info("Contexte JNDI Prêt fermé");
            } catch (NamingException e) {
                LOGGER.log(Level.WARNING, "Erreur lors de la fermeture du contexte Prêt", e);
            }
        }
    }
}
