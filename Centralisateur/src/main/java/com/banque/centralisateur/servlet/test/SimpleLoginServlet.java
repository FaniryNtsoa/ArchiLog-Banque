package com.banque.centralisateur.servlet.test;

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
 * Servlet de login simplifié pour test sans EJB
 */
@WebServlet(name = "SimpleLoginServlet", urlPatterns = {"/simple-login"})
public class SimpleLoginServlet extends HttpServlet {

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
        
        try {
            IWebExchange webExchange = this.application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange);
            
            context.setVariable("pageTitle", "Connexion Simple - Administration Bancaire");
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("utils/login", context, response.getWriter());
            
        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println("Erreur: " + e.getMessage());
            e.printStackTrace(response.getWriter());
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        
        try {
            // Test simple de connexion
            if ("admin".equals(username) && "admin".equals(password)) {
                // Succès - créer une session simple
                HttpSession session = request.getSession(true);
                session.setAttribute("userSessionBean", "simple-test-session");
                session.setAttribute("utilisateur", username);
                
                // Rediriger vers dashboard ou une page de test
                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else {
                // Échec - afficher erreur
                IWebExchange webExchange = this.application.buildExchange(request, response);
                WebContext context = new WebContext(webExchange);
                
                context.setVariable("pageTitle", "Connexion Simple - Administration Bancaire");
                context.setVariable("errorMessage", "Nom d'utilisateur ou mot de passe incorrect (test: admin/admin)");
                
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("utils/login", context, response.getWriter());
            }
            
        } catch (Exception e) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().println("Erreur: " + e.getMessage());
            e.printStackTrace(response.getWriter());
        }
    }
}