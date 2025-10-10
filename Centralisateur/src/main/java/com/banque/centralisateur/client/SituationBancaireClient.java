package com.banque.centralisateur.client;

import jakarta.ejb.Stateless;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Client EJB pour communiquer avec le module SituationBancaire
 * Ce client sera injecté dans les services du centralisateur
 */
@Stateless
public class SituationBancaireClient {
    
    private static final Logger logger = LoggerFactory.getLogger(SituationBancaireClient.class);
    
    // Ces valeurs seront configurées selon le serveur d'applications
    private static final String JNDI_PREFIX = "ejb:";
    private static final String APP_NAME = "situation-bancaire";
    private static final String MODULE_NAME = "situation-bancaire";
    
    /**
     * Obtient le contexte JNDI pour les lookups EJB
     */
    private Context getInitialContext() throws NamingException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
        // Ajouter les credentials si nécessaire
        // props.put(Context.SECURITY_PRINCIPAL, "username");
        // props.put(Context.SECURITY_CREDENTIALS, "password");
        
        return new InitialContext(props);
    }
    
    /**
     * Construit le nom JNDI pour un EJB distant
     */
    private String buildJndiName(String beanName, String interfaceName) {
        return String.format("%s%s/%s/%s!%s",
                JNDI_PREFIX,
                APP_NAME,
                MODULE_NAME,
                beanName,
                interfaceName);
    }
    
    /**
     * Effectue un lookup pour obtenir un bean distant
     */
    protected <T> T lookupRemoteBean(String beanName, Class<T> interfaceClass) throws NamingException {
        String jndiName = buildJndiName(beanName, interfaceClass.getName());
        logger.info("Lookup EJB distant: {}", jndiName);
        
        Context ctx = getInitialContext();
        try {
            @SuppressWarnings("unchecked")
            T bean = (T) ctx.lookup(jndiName);
            logger.info("Bean distant obtenu avec succès: {}", beanName);
            return bean;
        } finally {
            ctx.close();
        }
    }
}
