package com.banque.pret.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Configuration de l'application JAX-RS
 */
@ApplicationPath("/api")
public class ApplicationConfig extends Application {
    // La configuration par défaut suffit pour l'instant
}
