package com.banque.centralisateur.servlet.pret;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.PretEJBClientFactory;
import com.banque.pret.dto.PretDTO;
import com.banque.pret.dto.EcheanceDTO;
import com.banque.pret.ejb.remote.PretServiceRemote;
import com.banque.pret.ejb.remote.EcheanceServiceRemote;
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
 * Servlet pour l'administration des prêts - Interface Admin
 * Gestion complète des demandes, approbations, refus via EJB
 */
@WebServlet(name = "AdminPretServlet", urlPatterns = {"/admin/pret/gestion"})
public class AdminPretServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(AdminPretServlet.class.getName());
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
        String idPretStr = request.getParameter("idPret");
        
        try {
            if ("detail".equals(action) && idPretStr != null) {
                afficherDetailPret(request, response, Long.parseLong(idPretStr));
            } else {
                afficherListePrets(request, response);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur dans AdminPretServlet.doGet", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors du chargement des données");
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
        String idPretStr = request.getParameter("idPret");
        
        try {
            Long idPret = Long.parseLong(idPretStr);
            PretServiceRemote pretService = PretEJBClientFactory.getPretService();
            
            switch (action) {
                case "approuver":
                    PretDTO pretApprouve = pretService.approuverPretAdmin(idPret, 1L); // ID admin par défaut
                    session.setAttribute("successMessage", 
                        "Prêt n°" + pretApprouve.getNumeroPret() + " approuvé avec succès");
                    break;
                    
                case "refuser":
                    String motifRefus = request.getParameter("motifRefus");
                    if (motifRefus == null || motifRefus.trim().isEmpty()) {
                        throw new IllegalArgumentException("Le motif de refus est obligatoire");
                    }
                    PretDTO pretRefuse = pretService.refuserPretAdmin(idPret, motifRefus, 1L);
                    session.setAttribute("successMessage", 
                        "Prêt n°" + pretRefuse.getNumeroPret() + " refusé");
                    break;
                    
                default:
                    throw new IllegalArgumentException("Action non reconnue : " + action);
            }
            
            response.sendRedirect(request.getContextPath() + "/admin/pret/gestion");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'action : " + action, e);
            session.setAttribute("errorMessage", "Erreur : " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/pret/gestion");
        }
    }
    
    /**
     * Affiche la liste de tous les prêts pour l'administration
     */
    private void afficherListePrets(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            PretServiceRemote pretService = PretEJBClientFactory.getPretService();
            
            // Récupérer tous les prêts
            List<PretDTO> tousLesPrets = pretService.listerTousLesPrets();
            
            // Séparer par statut pour faciliter l'affichage
            List<PretDTO> demandesEnAttente = tousLesPrets.stream()
                .filter(p -> "EN_ATTENTE".equals(p.getStatut()))
                .toList();
                
            List<PretDTO> pretsApprouves = tousLesPrets.stream()
                .filter(p -> "APPROUVE".equals(p.getStatut()))
                .toList();
                
            List<PretDTO> pretsRefuses = tousLesPrets.stream()
                .filter(p -> "REFUSE".equals(p.getStatut()))
                .toList();
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Administration des Prêts");
            context.setVariable("currentPage", "admin-pret");
            context.setVariable("moduleName", "Prêts");
            
            context.setVariable("demandesEnAttente", demandesEnAttente);
            context.setVariable("pretsApprouves", pretsApprouves);
            context.setVariable("pretsRefuses", pretsRefuses);
            context.setVariable("totalPrets", tousLesPrets.size());
            
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
            templateEngine.process("pret/admin-gestion", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des prêts", e);
            throw new ServletException("Impossible de charger les données des prêts", e);
        }
    }
    
    /**
     * Affiche le détail d'un prêt avec son tableau d'amortissement
     */
    private void afficherDetailPret(HttpServletRequest request, HttpServletResponse response, Long idPret) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            PretServiceRemote pretService = PretEJBClientFactory.getPretService();
            EcheanceServiceRemote echeanceService = PretEJBClientFactory.getEcheanceService();
            
            // Récupérer le prêt
            PretDTO pret = pretService.rechercherPretParId(idPret);
            if (pret == null) {
                throw new IllegalArgumentException("Prêt non trouvé : " + idPret);
            }
            
            // Récupérer le tableau d'amortissement si le prêt est approuvé
            List<EcheanceDTO> echeances = null;
            if ("APPROUVE".equals(pret.getStatut())) {
                echeances = echeanceService.obtenirTableauAmortissement(idPret);
            }
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Détail Prêt n°" + pret.getNumeroPret());
            context.setVariable("currentPage", "admin-pret-detail");
            context.setVariable("pret", pret);
            context.setVariable("echeances", echeances);
            context.setVariable("hasEcheances", echeances != null && !echeances.isEmpty());
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/admin-detail", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du détail du prêt " + idPret, e);
            session.setAttribute("errorMessage", "Impossible de charger les détails du prêt");
            response.sendRedirect(request.getContextPath() + "/admin/pret/gestion");
        }
    }
}