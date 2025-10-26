package com.banque.centralisateur.servlet.situation;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.dto.MouvementDTO;
import com.banque.situationbancaire.dto.ActionRoleDTO;
import com.banque.situationbancaire.dto.UtilisateurDTO;
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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet pour les retraits sur comptes courants - Administration
 * GÃ¨re les retraits avec vÃ©rification des autorisations ActionRole
 */
@WebServlet(name = "AdminRetraitServlet", urlPatterns = {"/admin/situation/retrait"})
public class AdminRetraitServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(AdminRetraitServlet.class.getName());
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
        
        // VÃ©rifier les autorisations ActionRole
        if (!verifierAutorisation(session, "mouvement", "CREATE")) {
            session.setAttribute("errorMessage", "Vous n'avez pas l'autorisation d'effectuer des retraits");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        LOGGER.info("Affichage du formulaire de retrait - Interface Admin");
        
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // RÃ©cupÃ©rer la liste des comptes courants
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            List<CompteCourantDTO> comptes = compteService.findAll();
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Effectuer un Retrait - Administration");
            context.setVariable("currentPage", "admin-retrait");
            context.setVariable("moduleName", "Situation Bancaire");
            context.setVariable("comptes", comptes);
            context.setVariable("hasComptes", !comptes.isEmpty());
            
            // Messages de session
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                context.setVariable("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }
            
            String successMessage = (String) session.getAttribute("successMessage");
            if (successMessage != null) {
                context.setVariable("successMessage", successMessage);
                session.removeAttribute("successMessage");
            }
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("situation/admin-retrait", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du formulaire de retrait", e);
            session.setAttribute("errorMessage", "Impossible de charger le formulaire. Veuillez rÃ©essayer.");
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
        
        // VÃ©rifier les autorisations ActionRole
        if (!verifierAutorisation(session, "operation", "CREATE")) {
            session.setAttribute("errorMessage", "Vous n'avez pas l'autorisation d'effectuer des retraits");
            response.sendRedirect(request.getContextPath() + "/admin/situation/retrait");
            return;
        }
        
        LOGGER.info("Traitement de retrait par l'admin");
        
        try {
            // RÃ©cupÃ©rer les paramÃ¨tres du formulaire
            String compteIdStr = request.getParameter("compteId");
            String montantStr = request.getParameter("montant");
            String motif = request.getParameter("motif");
            
            // Validation
            if (compteIdStr == null || compteIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez sÃ©lectionner un compte");
            }
            
            if (montantStr == null || montantStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez saisir un montant");
            }
            
            Long compteId = Long.parseLong(compteIdStr);
            BigDecimal montant = new BigDecimal(montantStr);
            
            if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant doit Ãªtre positif");
            }
            
            if (montant.compareTo(new BigDecimal("5000000")) > 0) {
                throw new IllegalArgumentException("Le montant ne peut pas dÃ©passer 5,000,000 XOF par transaction");
            }
            
            // RÃ©cupÃ©rer le compte et vÃ©rifier le solde disponible
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            CompteCourantDTO compte = compteService.rechercherCompteParId(compteId);
            if (compte == null) {
                throw new IllegalArgumentException("Compte introuvable");
            }
            
            // RÃ©cupÃ©rer l'ID de l'utilisateur admin
            UtilisateurDTO utilisateur = (UtilisateurDTO) session.getAttribute("utilisateur");
            Long idAdministrateur = utilisateur != null ? utilisateur.getIdUtilisateur() : 1L;
            
            // Effectuer le retrait via EJB avec traÃ§abilitÃ© admin
            OperationServiceRemote operationService = EJBClientFactory.getOperationService();
            MouvementDTO operationEffectuee = operationService.effectuerRetraitAdmin(
                compte.getNumeroCompte(), 
                montant, 
                motif != null && !motif.trim().isEmpty() ? motif : "Retrait effectuÃ© par l'administration",
                idAdministrateur
            );
            
            session.setAttribute("successMessage", 
                String.format("Retrait de %s XOF effectuÃ© avec succÃ¨s. Nouveau solde: %s XOF", 
                    operationEffectuee.getMontant(), operationEffectuee.getSoldeApresOperation()));
            
            // Rediriger vers le formulaire avec confirmation
            response.sendRedirect(request.getContextPath() + "/admin/situation/retrait");
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format de donnÃ©es invalide", e);
            session.setAttribute("errorMessage", "Format de montant invalide. Utilisez des nombres valides.");
            response.sendRedirect(request.getContextPath() + "/admin/situation/retrait");
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Validation Ã©chouÃ©e: " + e.getMessage(), e);
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/situation/retrait");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du retrait", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors de l'opÃ©ration. Veuillez rÃ©essayer.");
            response.sendRedirect(request.getContextPath() + "/admin/situation/retrait");
        }
    }
    
    /**
     * VÃ©rifie si l'utilisateur a l'autorisation pour effectuer une action sur une table
     */
    @SuppressWarnings("unchecked")
    private boolean verifierAutorisation(HttpSession session, String nomTable, String action) {
        try {
            UtilisateurDTO utilisateur = (UtilisateurDTO) session.getAttribute("utilisateur");
            List<ActionRoleDTO> autorisations = (List<ActionRoleDTO>) session.getAttribute("autorisations");
            
            if (utilisateur == null || autorisations == null) {
                LOGGER.warning("Utilisateur ou autorisations manquants en session");
                return false;
            }
            
            Integer roleUtilisateur = utilisateur.getRoleUtilisateur();
            
            // VÃ©rifier si l'utilisateur a l'autorisation pour cette action sur cette table
            return autorisations.stream()
                .anyMatch(auth -> nomTable.equals(auth.getNomTable()) && 
                                action.equals(auth.getActionAutorisee()) && 
                                roleUtilisateur.equals(auth.getRoleRequis()));
                                
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vÃ©rification des autorisations", e);
            return false;
        }
    }
}
