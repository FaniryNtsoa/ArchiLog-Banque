package com.banque.centralisateur.servlet.situation;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.dto.VirementDTO;

import com.banque.situationbancaire.dto.UtilisateurDTO;
import com.banque.situationbancaire.ejb.remote.UserSessionBeanRemote;
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
 * Servlet pour les virements entre comptes - Administration
 * GÃ¨re les virements avec vÃ©rification des autorisations ActionRole
 */
@WebServlet(name = "AdminVirementServlet", urlPatterns = {"/admin/situation/virement"})
public class AdminVirementServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(AdminVirementServlet.class.getName());
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
        UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");
        if (!userSessionBean.hasPermission("virement", "CREATE")) {
            session.setAttribute("errorMessage", "Vous n'avez pas l'autorisation d'effectuer des virements");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        LOGGER.info("Affichage du formulaire de virement - Interface Admin");
        
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // RÃ©cupÃ©rer la liste des comptes courants
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            List<CompteCourantDTO> comptes = compteService.findAll();
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Effectuer un Virement - Administration");
            context.setVariable("currentPage", "admin-virement");
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
            templateEngine.process("situation/admin-virement", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du formulaire de virement", e);
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
        UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");
        if (!userSessionBean.hasPermission("operation", "CREATE")) {
            session.setAttribute("errorMessage", "Vous n'avez pas l'autorisation d'effectuer des virements");
            response.sendRedirect(request.getContextPath() + "/admin/situation/virement");
            return;
        }
        
        LOGGER.info("Traitement de virement par l'admin");
        
        try {
            // RÃ©cupÃ©rer les paramÃ¨tres du formulaire
            String compteDebiteurIdStr = request.getParameter("compteDebiteurId");
            String compteCrediteurIdStr = request.getParameter("compteCrediteurId");
            String montantStr = request.getParameter("montant");
            String motif = request.getParameter("motif");
            
            // Validation des paramÃ¨tres obligatoires
            if (compteDebiteurIdStr == null || compteDebiteurIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez sÃ©lectionner le compte dÃ©biteur");
            }
            
            if (compteCrediteurIdStr == null || compteCrediteurIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez sÃ©lectionner le compte crÃ©diteur");
            }
            
            if (montantStr == null || montantStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez saisir un montant");
            }
            
            Long compteDebiteurId = Long.parseLong(compteDebiteurIdStr);
            Long compteCrediteurId = Long.parseLong(compteCrediteurIdStr);
            BigDecimal montant = new BigDecimal(montantStr);
            
            // Validation mÃ©tier
            if (compteDebiteurId.equals(compteCrediteurId)) {
                throw new IllegalArgumentException("Le compte dÃ©biteur et crÃ©diteur doivent Ãªtre diffÃ©rents");
            }
            
            if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant doit Ãªtre positif");
            }
            
            if (montant.compareTo(new BigDecimal("10000000")) > 0) {
                throw new IllegalArgumentException("Le montant ne peut pas dÃ©passer 10,000,000 XOF par virement");
            }
            
            // RÃ©cupÃ©rer les comptes
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            CompteCourantDTO compteDebiteur = compteService.rechercherCompteParId(compteDebiteurId);
            CompteCourantDTO compteCrediteur = compteService.rechercherCompteParId(compteCrediteurId);
            
            if (compteDebiteur == null) {
                throw new IllegalArgumentException("Compte dÃ©biteur introuvable");
            }
            
            if (compteCrediteur == null) {
                throw new IllegalArgumentException("Compte crÃ©diteur introuvable");
            }
            
            // RÃ©cupÃ©rer l'ID de l'utilisateur admin
            UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");
            UtilisateurDTO utilisateur = userSessionBean.getUtilisateur();
            Long idAdministrateur = utilisateur != null ? utilisateur.getIdUtilisateur() : 1L;
            
            // Effectuer le virement via EJB avec traÃ§abilitÃ© admin
            OperationServiceRemote operationService = EJBClientFactory.getOperationService();
            VirementDTO operationEffectuee = operationService.effectuerVirementAdmin(
                compteDebiteur.getNumeroCompte(), 
                compteCrediteur.getNumeroCompte(),
                montant, 
                motif != null && !motif.trim().isEmpty() ? motif : "Virement effectuÃ© par l'administration",
                idAdministrateur
            );
            
            session.setAttribute("successMessage", 
                String.format("Virement de %s XOF effectuÃ© avec succÃ¨s du compte %s vers %s", 
                    operationEffectuee.getMontant(), 
                    operationEffectuee.getNumeroCompteDebit(),
                    operationEffectuee.getNumeroCompteCredit()));
            
            // Rediriger vers le formulaire avec confirmation
            response.sendRedirect(request.getContextPath() + "/admin/situation/virement");
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format de donnÃ©es invalide", e);
            session.setAttribute("errorMessage", "Format de donnÃ©es invalide. Utilisez des nombres valides.");
            response.sendRedirect(request.getContextPath() + "/admin/situation/virement");
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Validation Ã©chouÃ©e: " + e.getMessage(), e);
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/situation/virement");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du virement", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors de l'opÃ©ration. Veuillez rÃ©essayer.");
            response.sendRedirect(request.getContextPath() + "/admin/situation/virement");
        }
    }
    
    /**
     * VÃ©rifie si l'utilisateur a l'autorisation pour effectuer une action sur une table
     */
    @SuppressWarnings("unchecked")
    private boolean verifierAutorisation(HttpSession session, String nomTable, String action) {
        try {
            UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");
            UtilisateurDTO utilisateur = userSessionBean.getUtilisateur();
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
