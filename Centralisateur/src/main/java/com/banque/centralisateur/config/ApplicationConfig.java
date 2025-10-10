package com.banque.centralisateur.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Configuration JAX-RS pour les services REST du centralisateur
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    // La configuration par défaut suffit pour le moment
}
