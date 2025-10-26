package com.banque.centralisateur.servlet.situation;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote;
import com.banque.situationbancaire.ejb.remote.UserSessionBeanRemote;

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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet pour la liste des comptes courants - Administration
 * Affiche la liste de tous les comptes avec possibilitÃ© de gestion
 */
@WebServlet(name = "AdminComptesServlet", urlPatterns = {"/admin/situation/comptes"})
public class AdminComptesServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(AdminComptesServlet.class.getName());
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
        
        // Vérifier les autorisations ActionRole
        UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");
        if (!userSessionBean.hasPermission("compte_courant", "READ")) {
            session.setAttribute("errorMessage", "Vous n'avez pas l'autorisation de consulter les comptes");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        LOGGER.info("Affichage de la liste des comptes - Interface Admin");
        
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // RÃ©cupÃ©rer tous les comptes
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            List<CompteCourantDTO> comptes = compteService.findAll();
            
            // Statistiques simples
            long comptesActifs = comptes.stream()
                .filter(c -> c.getStatut() == null || !"FERME".equals(c.getStatut()))
                .count();
            
            long comptesFermes = comptes.size() - comptesActifs;
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Gestion des Comptes - Administration");
            context.setVariable("currentPage", "admin-comptes");
            context.setVariable("moduleName", "Situation Bancaire");
            context.setVariable("comptes", comptes);
            context.setVariable("hasComptes", !comptes.isEmpty());
            context.setVariable("nombreTotalComptes", comptes.size());
            context.setVariable("nombreComptesActifs", comptesActifs);
            context.setVariable("nombreComptesFermes", comptesFermes);
            
            // Vérifier les autorisations pour les actions
            context.setVariable("peutCreer", userSessionBean.hasPermission("compte_courant", "CREATE"));
            context.setVariable("peutModifier", userSessionBean.hasPermission("compte_courant", "UPDATE"));
            context.setVariable("peutSupprimer", userSessionBean.hasPermission("compte_courant", "DELETE"));
            
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
            templateEngine.process("situation/admin-comptes", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la liste des comptes", e);
            session.setAttribute("errorMessage", "Impossible de charger la liste des comptes. Veuillez rÃ©essayer.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");
        
        if (userSessionBean == null || !userSessionBean.isValid()) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        LOGGER.info("Traitement d'action sur compte par l'admin");
        
        try {
            String action = request.getParameter("action");
            String compteIdStr = request.getParameter("compteId");
            
            if (action == null || compteIdStr == null) {
                throw new IllegalArgumentException("ParamÃ¨tres manquants");
            }
            
            Long compteId = Long.parseLong(compteIdStr);
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            CompteCourantDTO compte = compteService.rechercherCompteParId(compteId);
            
            if (compte == null) {
                throw new IllegalArgumentException("Compte introuvable");
            }
            
            switch (action) {
                case "bloquer":
                    if (!userSessionBean.hasPermission("compte_courant", "UPDATE")) {
                        throw new SecurityException("Autorisation insuffisante pour bloquer un compte");
                    }
                    compteService.bloquerCompte(compte.getNumeroCompte());
                    session.setAttribute("successMessage", 
                        "Compte " + compte.getNumeroCompte() + " bloquÃ© avec succÃ¨s");
                    break;
                    
                case "debloquer":
                    if (!userSessionBean.hasPermission("compte_courant", "UPDATE")) {
                        throw new SecurityException("Autorisation insuffisante pour débloquer un compte");
                    }
                    compteService.debloquerCompte(compte.getNumeroCompte());
                    session.setAttribute("successMessage", 
                        "Compte " + compte.getNumeroCompte() + " dÃ©bloquÃ© avec succÃ¨s");
                    break;
                    
                case "fermer":
                    if (!userSessionBean.hasPermission("compte_courant", "DELETE")) {
                        throw new SecurityException("Autorisation insuffisante pour fermer un compte");
                    }
                    compteService.fermerCompte(compte.getNumeroCompte(), "Compte fermÃ© par l'administration");
                    session.setAttribute("successMessage", 
                        "Compte " + compte.getNumeroCompte() + " fermÃ© avec succÃ¨s");
                    break;
                    
                default:
                    throw new IllegalArgumentException("Action non supportÃ©e: " + action);
            }
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format de donnÃ©es invalide", e);
            session.setAttribute("errorMessage", "Format de donnÃ©es invalide");
            
        } catch (IllegalArgumentException | SecurityException e) {
            LOGGER.log(Level.WARNING, "Erreur de validation: " + e.getMessage(), e);
            session.setAttribute("errorMessage", e.getMessage());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'action sur le compte", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors de l'opÃ©ration. Veuillez rÃ©essayer.");
        }
        
        response.sendRedirect(request.getContextPath() + "/admin/situation/comptes");
    }
}
