package com.banque.centralisateur.servlet.pret;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.PretEJBClientFactory;
import com.banque.pret.dto.PretDTO;
import com.banque.pret.ejb.remote.PretServiceRemote;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet pour afficher la liste des prêts - MODE ADMIN
 */
@WebServlet(name = "MesPretsServlet", urlPatterns = {"/pret/mes-prets"})
public class MesPretsServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(MesPretsServlet.class.getName());
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
        
        // MODE ADMIN : Plus d'authentification client requise
        // HttpSession session = request.getSession(false);
        // if (session == null || session.getAttribute("clientNom") == null) {
        //     response.sendRedirect(request.getContextPath() + "/login");
        //     return;
        // }
        
        // Long idClient = (Long) session.getAttribute("clientId");
        Long idAdministrateur = 1L; // ID admin par défaut
        
        LOGGER.info("Affichage de tous les prêts - MODE ADMIN par administrateur ID: " + idAdministrateur);
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // Récupérer TOUS les prêts (mode admin)
            PretServiceRemote pretService = PretEJBClientFactory.getPretService();
            List<PretDTO> prets = pretService.listerTousLesPrets(); // Méthode admin
            
            context.setVariable("prets", prets);
            context.setVariable("hasPrets", prets != null && !prets.isEmpty());
            
            LOGGER.info("Nombre total de prêts trouvés: " + (prets != null ? prets.size() : 0));
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des prêts", e);
            context.setVariable("errorMessage", "Impossible de charger les prêts");
            context.setVariable("hasPrets", false);
        }
        
        // Ajouter les variables au contexte
        context.setVariable("pageTitle", "Gestion des Prêts - Banque Premium");
        context.setVariable("currentPage", "mes-prets");
        context.setVariable("clientNom", "Admin"); // MODE ADMIN
        context.setVariable("clientPrenom", "Système"); // MODE ADMIN
        
        // Rendre le template
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("pret/mes-prets", context, response.getWriter());
    }
}
