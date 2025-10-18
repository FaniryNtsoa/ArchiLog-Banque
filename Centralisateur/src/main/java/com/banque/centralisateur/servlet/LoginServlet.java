package com.banque.centralisateur.servlet;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.ClientDTO;
import com.banque.situationbancaire.ejb.remote.ClientServiceRemote;
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
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet pour gérer la connexion des clients
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(LoginServlet.class.getName());
    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialiser Thymeleaf
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("Affichage de la page de connexion");
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        // Ajouter les variables au contexte
        context.setVariable("pageTitle", "Connexion - Banque Premium");
        
        // Vérifier s'il y a un message de succès
        String success = request.getParameter("success");
        if ("inscription".equals(success)) {
            context.setVariable("successMessage", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
        }
        
        // Rendre le template
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("login", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("Traitement de la connexion");
        
        // Récupérer les données du formulaire
        String email = request.getParameter("email");
        String motDePasse = request.getParameter("motDePasse");
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Connexion - Banque Premium");
        
        try {
            // Validation des champs
            if (email == null || email.trim().isEmpty() ||
                motDePasse == null || motDePasse.trim().isEmpty()) {
                
                context.setVariable("errorMessage", "Veuillez remplir tous les champs");
                context.setVariable("email", email);
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("login", context, response.getWriter());
                return;
            }
            
            // Appeler le service distant pour authentifier le client
            ClientServiceRemote clientService = EJBClientFactory.getClientService();
            ClientDTO client = clientService.authentifierClient(email, motDePasse);
            
            if (client == null) {
                LOGGER.warning("Échec de l'authentification pour: " + email);
                context.setVariable("errorMessage", "Email ou mot de passe incorrect");
                context.setVariable("email", email);
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("login", context, response.getWriter());
                return;
            }
            
            // Authentification réussie - créer une session
            HttpSession session = request.getSession(true);
            session.setAttribute("client", client);
            session.setAttribute("clientId", client.getIdClient());
            session.setAttribute("clientEmail", client.getEmail());
            session.setAttribute("clientNom", client.getNom());
            session.setAttribute("clientPrenom", client.getPrenom());
            
            LOGGER.info("Client connecté avec succès: " + client.getEmail());
            
            // Rediriger vers le dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la connexion", e);
            context.setVariable("errorMessage", "Une erreur est survenue. Veuillez réessayer.");
            context.setVariable("email", email);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("login", context, response.getWriter());
        }
    }
}
