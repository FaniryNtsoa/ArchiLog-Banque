package com.banque.centralisateur.servlet.pret;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.PretEJBClientFactory;
import com.banque.pret.dto.EcheanceDTO;
import com.banque.pret.dto.PretDTO;
import com.banque.pret.dto.RemboursementDTO;
import com.banque.pret.ejb.remote.EcheanceServiceRemote;
import com.banque.pret.ejb.remote.PretServiceRemote;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet pour la gestion des remboursements de prÃªts - Interface Admin
 * Permet d'enregistrer les paiements et de suivre les Ã©chÃ©ances
 */
@WebServlet(name = "AdminRemboursementServlet", urlPatterns = {"/admin/pret/remboursement"})
public class AdminRemboursementServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(AdminRemboursementServlet.class.getName());
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
        if (session == null || session.getAttribute("userSessionBean") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String action = request.getParameter("action");
        String pretIdStr = request.getParameter("pretId");
        
        try {
            if ("echeances".equals(action) && pretIdStr != null) {
                afficherEcheancesPret(request, response, Long.parseLong(pretIdStr));
            } else {
                afficherEcheancesEnRetard(request, response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur dans AdminRemboursementServlet.doGet", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors du chargement");
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userSessionBean") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        String action = request.getParameter("action");
        
        try {
            if ("enregistrer-paiement".equals(action)) {
                enregistrerPaiement(request, response);
            } else {
                throw new IllegalArgumentException("Action non reconnue : " + action);
            }
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'action : " + action, e);
            session.setAttribute("errorMessage", "Erreur : " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/pret/remboursement");
        }
    }
    
    /**
     * Affiche toutes les Ã©chÃ©ances en retard pour surveillance admin
     */
    private void afficherEcheancesEnRetard(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            EcheanceServiceRemote echeanceService = PretEJBClientFactory.getEcheanceService();
            
            // RÃ©cupÃ©rer toutes les Ã©chÃ©ances en retard
            List<EcheanceDTO> echeancesEnRetard = echeanceService.listerEcheancesEnRetard();
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Suivi des Remboursements - Administration");
            context.setVariable("currentPage", "admin-remboursement");
            context.setVariable("moduleName", "PrÃªts");
            context.setVariable("echeancesEnRetard", echeancesEnRetard);
            context.setVariable("hasEcheancesEnRetard", !echeancesEnRetard.isEmpty());
            context.setVariable("totalEcheancesEnRetard", echeancesEnRetard.size());
            
            // Messages de session
            String successMessage = (String) session.getAttribute("successMessage");
            if (successMessage != null) {
                context.setVariable("successMessage", successMessage);
                session.removeAttribute("successMessage");
            }
            
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                context.setVariable("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/admin-remboursement", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des Ã©chÃ©ances en retard", e);
            throw new ServletException("Impossible de charger les donnÃ©es de remboursement", e);
        }
    }
    
    /**
     * Affiche les Ã©chÃ©ances d'un prÃªt spÃ©cifique
     */
    private void afficherEcheancesPret(HttpServletRequest request, HttpServletResponse response, Long pretId) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            PretServiceRemote pretService = PretEJBClientFactory.getPretService();
            EcheanceServiceRemote echeanceService = PretEJBClientFactory.getEcheanceService();
            
            // RÃ©cupÃ©rer le prÃªt
            PretDTO pret = pretService.rechercherPretParId(pretId);
            if (pret == null) {
                throw new IllegalArgumentException("PrÃªt non trouvÃ© : " + pretId);
            }
            
            // RÃ©cupÃ©rer toutes les Ã©chÃ©ances du prÃªt
            List<EcheanceDTO> echeances = echeanceService.obtenirTableauAmortissement(pretId);
            
            // RÃ©cupÃ©rer les Ã©chÃ©ances impayÃ©es
            List<EcheanceDTO> echeancesImpayees = echeanceService.listerEcheancesImpayees(pretId);
            
            // RÃ©cupÃ©rer l'historique des remboursements
            List<RemboursementDTO> remboursements = echeanceService.listerRemboursementsParPret(pretId);
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Ã‰chÃ©ances - PrÃªt nÂ°" + pret.getNumeroPret());
            context.setVariable("currentPage", "admin-echeances-detail");
            context.setVariable("pret", pret);
            context.setVariable("echeances", echeances);
            context.setVariable("echeancesImpayees", echeancesImpayees);
            context.setVariable("remboursements", remboursements);
            context.setVariable("hasEcheances", !echeances.isEmpty());
            context.setVariable("hasEcheancesImpayees", !echeancesImpayees.isEmpty());
            context.setVariable("hasRemboursements", !remboursements.isEmpty());
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/admin-echeances-detail", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des Ã©chÃ©ances du prÃªt " + pretId, e);
            session.setAttribute("errorMessage", "Impossible de charger les Ã©chÃ©ances du prÃªt");
            response.sendRedirect(request.getContextPath() + "/admin/pret/remboursement");
        }
    }
    
    /**
     * Enregistre un paiement d'Ã©chÃ©ance
     */
    private void enregistrerPaiement(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        try {
            // RÃ©cupÃ©rer les paramÃ¨tres
            String echeanceIdStr = request.getParameter("echeanceId");
            String montantPayeStr = request.getParameter("montantPaye");
            String dateRemboursementStr = request.getParameter("dateRemboursement");
            String modeRemboursement = request.getParameter("modeRemboursement");
            
            // Validation
            if (echeanceIdStr == null || echeanceIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("ID d'Ã©chÃ©ance manquant");
            }
            
            if (montantPayeStr == null || montantPayeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez saisir le montant payÃ©");
            }
            
            Long echeanceId = Long.parseLong(echeanceIdStr);
            BigDecimal montantPaye = new BigDecimal(montantPayeStr);
            
            if (montantPaye.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant payÃ© doit Ãªtre positif");
            }
            
            // CrÃ©er le DTO de remboursement
            RemboursementDTO remboursementDTO = RemboursementDTO.builder()
                .idEcheance(echeanceId)
                .montantPaye(montantPaye)
                .datePaiement(dateRemboursementStr != null && !dateRemboursementStr.trim().isEmpty() ? 
                    LocalDate.parse(dateRemboursementStr).atStartOfDay() : LocalDateTime.now())
                .build();
            
            // Enregistrer le remboursement via EJB
            EcheanceServiceRemote echeanceService = PretEJBClientFactory.getEcheanceService();
            RemboursementDTO remboursementCree = echeanceService.enregistrerRemboursement(remboursementDTO);
            
            session.setAttribute("successMessage", 
                String.format("Remboursement de %s XOF enregistrÃ© avec succÃ¨s", montantPaye));
            
            // Rediriger vers la page de remboursement
            response.sendRedirect(request.getContextPath() + "/admin/pret/remboursement");
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format de donnÃ©es invalide", e);
            session.setAttribute("errorMessage", "Format de donnÃ©es invalide. Veuillez vÃ©rifier vos saisies.");
            response.sendRedirect(request.getContextPath() + "/admin/pret/remboursement");
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Validation Ã©chouÃ©e: " + e.getMessage(), e);
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/pret/remboursement");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'enregistrement du remboursement", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors de l'enregistrement");
            response.sendRedirect(request.getContextPath() + "/admin/pret/remboursement");
        }
    }
}
