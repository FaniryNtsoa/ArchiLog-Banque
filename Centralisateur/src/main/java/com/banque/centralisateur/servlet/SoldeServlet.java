package com.banque.centralisateur.servlet;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.ClientDTO;
import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote;
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
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet pour afficher les soldes des comptes
 */
@WebServlet(name = "SoldeServlet", urlPatterns = {"/solde"})
public class SoldeServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(SoldeServlet.class.getName());
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
        
        // Vérifier si l'utilisateur est connecté
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("client") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        ClientDTO client = (ClientDTO) session.getAttribute("client");
        LOGGER.info("Consultation des soldes pour: " + client.getEmail());
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Consultation des soldes - Banque Premium");
        context.setVariable("currentPage", "solde");
        context.setVariable("client", client);
        
        try {
            // Récupérer les comptes du client
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            List<CompteCourantDTO> comptes = compteService.listerComptesParClient(client.getIdClient());
            
            context.setVariable("comptes", comptes);
            context.setVariable("hasComptes", comptes != null && !comptes.isEmpty());
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("solde", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des soldes", e);
            context.setVariable("errorMessage", "Erreur lors de la récupération des soldes: " + e.getMessage());
            context.setVariable("comptes", new java.util.ArrayList<>());
            context.setVariable("hasComptes", false);
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("solde", context, response.getWriter());
        }
    }
}
