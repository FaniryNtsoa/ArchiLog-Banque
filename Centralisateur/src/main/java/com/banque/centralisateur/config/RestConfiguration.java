package com.banque.centralisateur.config;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Configuration JAX-RS pour l'application Centralisateur
 */
@ApplicationPath("/")
public class RestConfiguration extends Application {
    // Cette classe active JAX-RS pour l'application
    // Toutes les ressources REST seront disponibles sous le path de base "/"
}