package com.banque.centralisateur.servlet.utils;

import com.banque.centralisateur.config.ThymeleafConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;

/**
 * Servlet centralisé pour le tableau de bord administrateur
 * URL de base : /dashboard
 */
@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        super.init();
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Vérifier si l'utilisateur est connecté
        if (session == null || session.getAttribute("userSessionBean") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        // Utiliser Thymeleaf pour rendre le dashboard
        try {
            IWebExchange webExchange = this.application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange);
            
            // Récupérer les informations utilisateur depuis la session
            String utilisateurLogin = (String) session.getAttribute("utilisateurLogin");
            context.setVariable("pageTitle", "Dashboard - Administration Bancaire");
            context.setVariable("currentUser", utilisateurLogin != null ? utilisateurLogin : "Admin");
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("utils/dashboard", context, response.getWriter());
        } catch (Exception e) {
            throw new ServletException("Erreur lors du rendu du dashboard", e);
        }
    }
}