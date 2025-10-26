package com.banque.centralisateur.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Servlet pour la page d'accueil
 * Redirige vers le dashboard si connecté, sinon vers le login
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"", "/"})
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Si l'utilisateur est déjà connecté, rediriger vers le dashboard
        if (session != null && session.getAttribute("userSessionBean") != null) {
            response.sendRedirect(request.getContextPath() + "/dashboard");
        } else {
            // Sinon, rediriger vers la page de login
            response.sendRedirect(request.getContextPath() + "/login");
        }
    }
}