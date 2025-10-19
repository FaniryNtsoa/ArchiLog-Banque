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
import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet pour effectuer un retrait
 */
@WebServlet(name = "RetraitServlet", urlPatterns = {"/retrait"})
public class RetraitServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(RetraitServlet.class.getName());
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
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Retrait - Banque Premium");
            context.setVariable("currentPage", "retrait");
        context.setVariable("client", client);
        
        try {
            // Récupérer les comptes du client
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            List<CompteCourantDTO> comptes = compteService.listerComptesParClient(client.getIdClient());
            context.setVariable("comptes", comptes);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des comptes", e);
            context.setVariable("errorMessage", "Erreur lors du chargement des comptes");
        }
        
        // Rendre le template
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("retrait", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Vérifier si l'utilisateur est connecté
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("client") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        ClientDTO client = (ClientDTO) session.getAttribute("client");
        LOGGER.info("Retrait pour: " + client.getEmail());
        
        // Récupérer les données du formulaire
        String numeroCompte = request.getParameter("numeroCompte");
        String montantStr = request.getParameter("montant");
        String libelle = request.getParameter("libelle");
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Retrait - Banque Premium");
            context.setVariable("currentPage", "retrait");
        context.setVariable("client", client);
        
        try {
            // Récupérer les comptes pour l'affichage
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            List<CompteCourantDTO> comptes = compteService.listerComptesParClient(client.getIdClient());
            context.setVariable("comptes", comptes);
            
            // Validation
            if (numeroCompte == null || numeroCompte.trim().isEmpty()) {
                context.setVariable("errorMessage", "Veuillez sélectionner un compte");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("retrait", context, response.getWriter());
                return;
            }
            
            // Vérifier que le compte appartient bien au client connecté
            if (!compteService.verifierProprietaireCompte(numeroCompte, client.getIdClient())) {
                context.setVariable("errorMessage", "Ce compte ne vous appartient pas");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("retrait", context, response.getWriter());
                return;
            }
            
            if (montantStr == null || montantStr.trim().isEmpty()) {
                context.setVariable("errorMessage", "Le montant est obligatoire");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("retrait", context, response.getWriter());
                return;
            }
            
            BigDecimal montant;
            try {
                montant = new BigDecimal(montantStr);
                if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                    context.setVariable("errorMessage", "Le montant doit être positif");
                    response.setContentType("text/html;charset=UTF-8");
                    templateEngine.process("retrait", context, response.getWriter());
                    return;
                }
            } catch (NumberFormatException e) {
                context.setVariable("errorMessage", "Montant invalide");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("retrait", context, response.getWriter());
                return;
            }
            
            // Appeler le service distant pour effectuer le retrait
            OperationServiceRemote operationService = EJBClientFactory.getOperationService();
            MouvementDTO mouvement = operationService.effectuerRetrait(
                numeroCompte, 
                montant, 
                libelle != null && !libelle.trim().isEmpty() ? libelle : "Retrait"
            );
            
            LOGGER.info("Retrait effectué avec succès: " + mouvement.getReference());
            
            // Rediriger vers le dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard?success=retrait");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du retrait", e);
            context.setVariable("errorMessage", "Erreur lors du retrait: " + e.getMessage());
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("retrait", context, response.getWriter());
        }
    }
}
