package com.banque.centralisateur.servlet.epargne;

import com.banque.centralisateur.client.EpargneRestClient;
import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.util.JsonHelper;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet pour gérer les comptes épargne
 * Affiche la liste des comptes épargne d'un client
 */
@WebServlet(name = "ComptesEpargneServlet", urlPatterns = {"/epargne/comptes"})
public class ComptesEpargneServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(ComptesEpargneServlet.class.getName());
    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;
    private EpargneRestClient epargneClient;

    @Override
    public void init() throws ServletException {
        super.init();
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
        this.epargneClient = new EpargneRestClient();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("clientId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        Long clientId = (Long) session.getAttribute("clientId");
        LOGGER.info("Affichage des comptes épargne pour le client: " + clientId);
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // Récupérer les comptes épargne du client
            List<JsonObject> comptes = epargneClient.getComptesClient(clientId);
            
            // Préparer les données pour l'affichage
            List<CompteEpargneView> comptesView = new ArrayList<>();
            for (JsonObject compte : comptes) {
                CompteEpargneView view = new CompteEpargneView();
                view.setIdCompte(JsonHelper.getSafeLong(compte, "idCompte"));
                view.setNumeroCompte(JsonHelper.getSafeString(compte, "numeroCompte", ""));
                view.setSolde(JsonHelper.getSafeBigDecimal(compte, "solde", java.math.BigDecimal.ZERO));
                view.setDateOuverture(JsonHelper.getSafeString(compte, "dateOuverture", ""));
                view.setStatut(JsonHelper.getSafeString(compte, "statut", ""));
                
                // Type de compte
                if (compte.containsKey("typeCompte") || compte.containsKey("TypeCompte")) {
                    JsonObject typeCompte = compte.containsKey("typeCompte") ? 
                        compte.getJsonObject("typeCompte") : 
                        compte.getJsonObject("TypeCompte");
                    view.setTypeLibelle(JsonHelper.getSafeString(typeCompte, "libelle", ""));
                    view.setTauxInteret(JsonHelper.getSafeBigDecimal(typeCompte, "tauxInteretAnnuel", java.math.BigDecimal.ZERO));
                }
                
                comptesView.add(view);
            }
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Mes Comptes Épargne - Banque Premium");
            context.setVariable("currentPage", "comptes-epargne");
            context.setVariable("clientPrenom", session.getAttribute("clientPrenom"));
            context.setVariable("clientNom", session.getAttribute("clientNom"));
            context.setVariable("comptes", comptesView);
            context.setVariable("hasComptes", !comptesView.isEmpty());
            
            // Messages de session
            String successMessage = (String) session.getAttribute("successMessage");
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (successMessage != null) {
                context.setVariable("successMessage", successMessage);
                session.removeAttribute("successMessage");
            }
            if (errorMessage != null) {
                context.setVariable("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("epargne/comptes-epargne", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des comptes épargne", e);
            context.setVariable("pageTitle", "Erreur - Banque Premium");
            context.setVariable("currentPage", "comptes-epargne");
            context.setVariable("clientPrenom", session.getAttribute("clientPrenom"));
            context.setVariable("clientNom", session.getAttribute("clientNom"));
            context.setVariable("errorMessage", "Impossible de récupérer vos comptes épargne. Veuillez réessayer.");
            context.setVariable("comptes", new ArrayList<>());
            context.setVariable("hasComptes", false);
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("epargne/comptes-epargne", context, response.getWriter());
        }
    }
    
    /**
     * Classe interne pour encapsuler les données d'un compte pour l'affichage
     */
    public static class CompteEpargneView {
        private Long idCompte;
        private String numeroCompte;
        private java.math.BigDecimal solde;
        private String dateOuverture;
        private String statut;
        private String typeLibelle;
        private java.math.BigDecimal tauxInteret;
        
        // Getters et setters
        public Long getIdCompte() { return idCompte; }
        public void setIdCompte(Long idCompte) { this.idCompte = idCompte; }
        
        public String getNumeroCompte() { return numeroCompte; }
        public void setNumeroCompte(String numeroCompte) { this.numeroCompte = numeroCompte; }
        
        public java.math.BigDecimal getSolde() { return solde; }
        public void setSolde(java.math.BigDecimal solde) { this.solde = solde; }
        
        public String getDateOuverture() { return dateOuverture; }
        public void setDateOuverture(String dateOuverture) { this.dateOuverture = dateOuverture; }
        
        public String getStatut() { return statut; }
        public void setStatut(String statut) { this.statut = statut; }
        
        public String getTypeLibelle() { return typeLibelle; }
        public void setTypeLibelle(String typeLibelle) { this.typeLibelle = typeLibelle; }
        
        public java.math.BigDecimal getTauxInteret() { return tauxInteret; }
        public void setTauxInteret(java.math.BigDecimal tauxInteret) { this.tauxInteret = tauxInteret; }
    }
}
