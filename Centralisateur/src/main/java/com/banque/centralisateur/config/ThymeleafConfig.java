package com.banque.centralisateur.config;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import jakarta.servlet.ServletContext;

/**
 * Configuration Thymeleaf pour Jakarta EE
 * Classe utilitaire simple sans CDI pour éviter les problèmes de proxy
 */
public class ThymeleafConfig {

    private static TemplateEngine templateEngine;

    /**
     * Initialise le TemplateEngine (appelé une seule fois)
     */
    public static synchronized TemplateEngine getTemplateEngine(ServletContext servletContext) {
        if (templateEngine == null) {
            templateEngine = createTemplateEngine();
        }
        return templateEngine;
    }
    
    /**
     * Crée et configure le moteur de template
     */
    private static TemplateEngine createTemplateEngine() {
        // Configurer le résolveur de templates
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setTemplateMode("HTML");
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L); // Cache 1 heure
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false); // Désactiver le cache en développement
        
        // Créer le moteur de template
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(templateResolver);
        
        return engine;
    }
}