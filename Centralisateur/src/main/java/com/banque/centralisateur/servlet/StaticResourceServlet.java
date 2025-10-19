package com.banque.centralisateur.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Servlet pour servir les fichiers statiques (CSS, JS, images)
 * Nécessaire sur WildFly car l'accès direct aux ressources peut être bloqué
 */
@WebServlet(name = "StaticResourceServlet", urlPatterns = {"/css/*", "/js/*", "/images/*"})
public class StaticResourceServlet extends HttpServlet {

    private static final int BUFFER_SIZE = 8192;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Récupérer le chemin de la ressource demandée
        String path = request.getPathInfo();
        if (path == null || path.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Construire le chemin complet de la ressource
        String resourcePath = request.getServletPath() + path;
        
        // Supprimer le slash initial pour webapp resources
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }

        // Essayer de charger la ressource depuis le WAR
        InputStream resourceStream = getServletContext().getResourceAsStream("/" + resourcePath);
        
        if (resourceStream == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, 
                "Resource not found: " + resourcePath);
            return;
        }

        // Déterminer le type MIME
        String mimeType = getServletContext().getMimeType(resourcePath);
        if (mimeType == null) {
            mimeType = determineMimeType(resourcePath);
        }
        response.setContentType(mimeType);

        // Définir les en-têtes de cache
        response.setHeader("Cache-Control", "public, max-age=31536000"); // 1 an
        response.setDateHeader("Expires", System.currentTimeMillis() + 31536000000L);

        // Copier le contenu de la ressource vers la réponse
        try (InputStream input = resourceStream;
             OutputStream output = response.getOutputStream()) {
            
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            
            output.flush();
        }
    }

    /**
     * Détermine le type MIME en fonction de l'extension du fichier
     */
    private String determineMimeType(String resourcePath) {
        if (resourcePath.endsWith(".css")) {
            return "text/css";
        } else if (resourcePath.endsWith(".js")) {
            return "application/javascript";
        } else if (resourcePath.endsWith(".png")) {
            return "image/png";
        } else if (resourcePath.endsWith(".jpg") || resourcePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (resourcePath.endsWith(".gif")) {
            return "image/gif";
        } else if (resourcePath.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (resourcePath.endsWith(".ico")) {
            return "image/x-icon";
        } else if (resourcePath.endsWith(".woff")) {
            return "font/woff";
        } else if (resourcePath.endsWith(".woff2")) {
            return "font/woff2";
        } else if (resourcePath.endsWith(".ttf")) {
            return "font/ttf";
        }
        return "application/octet-stream";
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}
