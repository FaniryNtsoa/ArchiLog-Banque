package com.banque.centralisateur.servlet;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.ClientDTO;
import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.dto.MouvementDTO;
import com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote;
import com.banque.situationbancaire.ejb.remote.OperationServiceRemote;
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
 * Servlet pour afficher l'historique des opérations
 */
@WebServlet(name = "HistoriqueServlet", urlPatterns = {"/historique"})
public class HistoriqueServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(HistoriqueServlet.class.getName());
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
        String numeroCompte = request.getParameter("numeroCompte");
        
        LOGGER.info("Consultation de l'historique pour: " + client.getEmail() + 
                    (numeroCompte != null ? " - Compte: " + numeroCompte : ""));
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Historique des opérations - Banque Premium");
        context.setVariable("currentPage", "historique");
        context.setVariable("client", client);
        
        try {
            // Récupérer les comptes du client
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            List<CompteCourantDTO> comptes = compteService.listerComptesParClient(client.getIdClient());
            context.setVariable("comptes", comptes);
            
            // Si un compte est sélectionné, récupérer son historique
            if (numeroCompte != null && !numeroCompte.trim().isEmpty()) {
                // Vérifier que le compte appartient bien au client
                if (!compteService.verifierProprietaireCompte(numeroCompte, client.getIdClient())) {
                    context.setVariable("errorMessage", "Ce compte ne vous appartient pas");
                } else {
                    OperationServiceRemote operationService = EJBClientFactory.getOperationService();
                    List<MouvementDTO> mouvements = operationService.obtenirHistoriqueMouvements(numeroCompte, null, null);
                    
                    context.setVariable("mouvements", mouvements);
                    context.setVariable("numeroCompteSelectionne", numeroCompte);
                }
            }
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("historique", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de l'historique", e);
            context.setVariable("errorMessage", "Erreur lors de la récupération de l'historique: " + e.getMessage());
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("historique", context, response.getWriter());
        }
    }
}
