package com.banque.centralisateur.client;

import com.banque.centralisateur.interfaces.ClientServiceRemote;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Client EJB pour communiquer avec le module SituationBancaire
 * Ce client sera injecté dans les services du centralisateur
 */
@Singleton
@Startup
public class SituationBancaireClient {
    
    private static final Logger logger = Logger.getLogger(SituationBancaireClient.class.getName());
    
    // Configuration pour WildFly remote EJB
    private static final String JNDI_PREFIX = "ejb:";
    private static final String APP_NAME = "situation-bancaire";
    private static final String MODULE_NAME = "situation-bancaire";
    private static final String DISTINCT_NAME = "";
    
    // Services EJB Remote
    private ClientServiceRemote clientService;
    
    private Context initialContext;
    
    public void init() {
        try {
            this.initialContext = getInitialContext();
            this.clientService = lookupClientService();
            logger.info("Client EJB SituationBancaire initialisé avec succès");
        } catch (NamingException e) {
            logger.log(Level.SEVERE, "Erreur lors de l'initialisation du client EJB", e);
            throw new RuntimeException("Impossible d'initialiser le client EJB", e);
        }
    }
    
    /**
     * Obtient le service Client distant
     */
    public ClientServiceRemote getClientService() throws NamingException {
        if (clientService == null) {
            clientService = lookupClientService();
        }
        return clientService;
    }
    
    /**
     * Lookup pour le service Client
     */
    private ClientServiceRemote lookupClientService() throws NamingException {
        return lookupRemoteBean("ClientServiceImpl", ClientServiceRemote.class);
    }
    
    /**
     * Obtient le contexte JNDI pour les lookups EJB
     */
    private Context getInitialContext() throws NamingException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
        
        // Configuration pour l'authentification si nécessaire
        // props.put(Context.SECURITY_PRINCIPAL, "username");
        // props.put(Context.SECURITY_CREDENTIALS, "password");
        
        return new InitialContext(props);
    }
    
    /**
     * Construit le nom JNDI pour un EJB distant
     */
    private String buildJndiName(String beanName, String interfaceName) {
        // Format: ejb:<app-name>/<module-name>/<distinct-name>/<bean-name>!<interface-name>
        return String.format("%s%s/%s/%s/%s!%s",
                JNDI_PREFIX,
                APP_NAME,
                MODULE_NAME,
                DISTINCT_NAME,
                beanName,
                interfaceName);
    }
    
    /**
     * Effectue un lookup pour obtenir un bean distant
     */
    public <T> T lookupRemoteBean(String beanName, Class<T> interfaceClass) throws NamingException {
        String jndiName = buildJndiName(beanName, interfaceClass.getName());
        logger.info("Tentative de lookup EJB distant: " + jndiName);
        
        try {
            @SuppressWarnings("unchecked")
            T bean = (T) initialContext.lookup(jndiName);
            logger.info("Bean distant obtenu avec succès: " + beanName + " - " + interfaceClass.getSimpleName());
            return bean;
        } catch (NamingException e) {
            logger.log(Level.SEVERE, "Erreur lors du lookup du bean " + beanName + " : " + e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Test de connexion au module distant
     */
    public boolean testConnection() {
        try {
            // Test simple pour vérifier la connectivité
            initialContext.list("");
            return true;
        } catch (Exception e) {
            logger.warning("Test de connexion échoué: " + e.getMessage());
            return false;
        }
    }
}
