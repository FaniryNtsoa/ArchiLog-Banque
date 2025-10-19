package com.banque.centralisateur.servlet.pret;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.PretEJBClientFactory;
import com.banque.pret.dto.EcheanceDTO;
import com.banque.pret.dto.PretDTO;
import com.banque.pret.dto.RemboursementDTO;
import com.banque.pret.ejb.remote.EcheanceServiceRemote;
import com.banque.pret.ejb.remote.PretServiceRemote;
import com.banque.pret.entity.enums.StatutPret;
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
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet pour gérer les remboursements d'échéances
 */
@WebServlet(name = "RemboursementServlet", urlPatterns = {"/pret/remboursement"})
public class RemboursementServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(RemboursementServlet.class.getName());
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
        if (session == null || session.getAttribute("clientNom") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        Long idClient = (Long) session.getAttribute("clientId");
        
        LOGGER.info("Affichage de la page de remboursement pour le client ID: " + idClient);
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // Récupérer les prêts actifs du client
            PretServiceRemote pretService = PretEJBClientFactory.getPretService();
            List<PretDTO> pretsActifs = pretService.listerPretsParClientEtStatut(idClient, StatutPret.APPROUVE);
            
            context.setVariable("pretsActifs", pretsActifs);
            context.setVariable("hasPretsActifs", pretsActifs != null && !pretsActifs.isEmpty());
            
            // Si un prêt est sélectionné, charger ses échéances impayées
            String idPretStr = request.getParameter("idPret");
            if (idPretStr != null && !idPretStr.trim().isEmpty()) {
                Long idPret = Long.parseLong(idPretStr);
                EcheanceServiceRemote echeanceService = PretEJBClientFactory.getEcheanceService();
                List<EcheanceDTO> echeancesImpayees = echeanceService.listerEcheancesImpayees(idPret);
                
                context.setVariable("echeancesImpayees", echeancesImpayees);
                context.setVariable("idPretSelectionne", idPret);
                context.setVariable("hasEcheances", echeancesImpayees != null && !echeancesImpayees.isEmpty());
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des prêts actifs", e);
            context.setVariable("errorMessage", "Impossible de charger vos prêts actifs");
            context.setVariable("hasPretsActifs", false);
        }
        
        // Ajouter les variables au contexte
        context.setVariable("pageTitle", "Remboursement - Banque Premium");
        context.setVariable("currentPage", "remboursement");
        context.setVariable("clientNom", session.getAttribute("clientNom"));
        context.setVariable("clientPrenom", session.getAttribute("clientPrenom"));
        
        // Rendre le template
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("pret/remboursement", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("clientNom") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        Long idClient = (Long) session.getAttribute("clientId");
        
        LOGGER.info("Traitement d'un remboursement pour le client ID: " + idClient);
        
        // Récupérer les paramètres du formulaire
        String idEcheanceStr = request.getParameter("idEcheance");
        String montantStr = request.getParameter("montant");
        
        try {
            // Validation
            if (idEcheanceStr == null || idEcheanceStr.trim().isEmpty() ||
                montantStr == null || montantStr.trim().isEmpty()) {
                
                session.setAttribute("errorMessage", "Veuillez remplir tous les champs obligatoires");
                response.sendRedirect(request.getContextPath() + "/pret/remboursement");
                return;
            }
            
            Long idEcheance = Long.parseLong(idEcheanceStr);
            BigDecimal montant = new BigDecimal(montantStr);
            
            // Créer le DTO de remboursement
            RemboursementDTO remboursementDTO = new RemboursementDTO();
            remboursementDTO.setIdEcheance(idEcheance);
            remboursementDTO.setMontant(montant);
            remboursementDTO.setDatePaiement(java.time.LocalDateTime.now());
            
            // Appeler le service pour enregistrer le remboursement
            EcheanceServiceRemote echeanceService = PretEJBClientFactory.getEcheanceService();
            RemboursementDTO remboursementCree = echeanceService.enregistrerRemboursement(remboursementDTO);
            
            LOGGER.info("Remboursement enregistré avec succès. Montant: " + remboursementCree.getMontant());
            
            // Rediriger avec un message de succès
            session.setAttribute("successMessage", "Remboursement enregistré avec succès. Montant: " + 
                montant + " €");
            response.sendRedirect(request.getContextPath() + "/pret/remboursement");
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Erreur de format de nombre", e);
            session.setAttribute("errorMessage", "Format de nombre invalide");
            response.sendRedirect(request.getContextPath() + "/pret/remboursement");
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Erreur de validation: " + e.getMessage(), e);
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/pret/remboursement");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'enregistrement du remboursement", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors du remboursement. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/pret/remboursement");
        }
    }
}
