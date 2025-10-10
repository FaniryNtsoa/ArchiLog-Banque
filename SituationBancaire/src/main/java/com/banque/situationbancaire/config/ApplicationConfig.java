package com.banque.situationbancaire.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Configuration JAX-RS pour les services REST (si nécessaire)
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    // Configuration par défaut
}
