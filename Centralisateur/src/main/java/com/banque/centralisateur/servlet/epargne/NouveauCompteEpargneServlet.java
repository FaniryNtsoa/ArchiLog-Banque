package com.banque.centralisateur.servlet.epargne;

import com.banque.centralisateur.client.EpargneRestClient;
import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.util.JsonHelper;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet pour gérer la création d'un nouveau compte épargne
 */
@WebServlet(name = "NouveauCompteEpargneServlet", urlPatterns = {"/epargne/nouveau-compte"})
public class NouveauCompteEpargneServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(NouveauCompteEpargneServlet.class.getName());
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
        
        LOGGER.info("Affichage du formulaire de création de compte épargne");
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // Récupérer les types de comptes épargne disponibles
            List<JsonObject> typesComptes = epargneClient.getTypesComptesActifs();
            
            // Préparer les données pour l'affichage
            List<TypeCompteView> typesView = new ArrayList<>();
            for (JsonObject type : typesComptes) {
                TypeCompteView view = new TypeCompteView();
                view.setIdTypeCompte(JsonHelper.getSafeLong(type, "idTypeCompte"));
                view.setLibelle(JsonHelper.getSafeString(type, "libelle", ""));
                view.setCodeType(JsonHelper.getSafeString(type, "codeType", ""));
                view.setDescription(JsonHelper.getSafeString(type, "description", ""));
                view.setTauxInteretAnnuel(JsonHelper.getSafeBigDecimal(type, "tauxInteretAnnuel", BigDecimal.ZERO));
                view.setDepotInitialMin(JsonHelper.getSafeBigDecimal(type, "depotInitialMin", BigDecimal.ZERO));
                view.setSoldeMinObligatoire(JsonHelper.getSafeBigDecimal(type, "soldeMinObligatoire", BigDecimal.ZERO));
                view.setPlafondDepot(JsonHelper.getSafeBigDecimal(type, "plafondDepot", BigDecimal.ZERO));
                view.setPeriodiciteCalculInteret(JsonHelper.getSafeString(type, "periodiciteCalculInteret", ""));
                typesView.add(view);
            }
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Ouvrir un Compte Épargne - Banque Premium");
            context.setVariable("currentPage", "nouveau-compte-epargne");
            context.setVariable("clientPrenom", session.getAttribute("clientPrenom"));
            context.setVariable("clientNom", session.getAttribute("clientNom"));
            context.setVariable("typesComptes", typesView);
            
            // Messages de session
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                context.setVariable("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("epargne/nouveau-compte-epargne", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du formulaire", e);
            session.setAttribute("errorMessage", "Impossible de charger le formulaire. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("clientId") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        Long clientId = (Long) session.getAttribute("clientId");
        LOGGER.info("Création d'un compte épargne pour le client: " + clientId);
        
        // Récupérer les paramètres du formulaire
        String typeCompteIdStr = request.getParameter("typeCompte");
        String depotInitialStr = request.getParameter("depotInitial");
        
        try {
            // Validation
            if (typeCompteIdStr == null || typeCompteIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez sélectionner un type de compte");
            }
            
            if (depotInitialStr == null || depotInitialStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez saisir le montant du dépôt initial");
            }
            
            Long typeCompteId = Long.parseLong(typeCompteIdStr);
            BigDecimal depotInitial = new BigDecimal(depotInitialStr);
            
            if (depotInitial.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le dépôt initial doit être positif");
            }
            
            // Appeler l'API Épargne (avec ID administrateur par défaut)
            JsonObject responseJson = epargneClient.creerCompteEpargne(clientId, typeCompteId, depotInitial, 1L);
            
            if (responseJson != null && responseJson.getBoolean("success", false)) {
                JsonObject compte = responseJson.getJsonObject("data");
                String numeroCompte = JsonHelper.getSafeString(compte, "numeroCompte", "");
                
                session.setAttribute("successMessage", 
                    "Compte épargne créé avec succès ! Numéro de compte : " + numeroCompte);
                response.sendRedirect(request.getContextPath() + "/epargne/comptes");
                
            } else {
                String errorMsg = responseJson != null ? 
                    JsonHelper.getSafeString(responseJson, "message", "Erreur lors de la création du compte") : 
                    "Erreur lors de la création du compte";
                    
                session.setAttribute("errorMessage", errorMsg);
                response.sendRedirect(request.getContextPath() + "/epargne/nouveau-compte");
            }
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format de données invalide", e);
            session.setAttribute("errorMessage", "Format de données invalide. Veuillez vérifier vos saisies.");
            response.sendRedirect(request.getContextPath() + "/epargne/nouveau-compte");
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Validation échouée: " + e.getMessage(), e);
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/epargne/nouveau-compte");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création du compte épargne", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors de la création du compte. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/epargne/nouveau-compte");
        }
    }
    
    /**
     * Classe interne pour encapsuler les données d'un type de compte pour l'affichage
     */
    public static class TypeCompteView {
        private Long idTypeCompte;
        private String libelle;
        private String codeType;
        private String description;
        private BigDecimal tauxInteretAnnuel;
        private BigDecimal depotInitialMin;
        private BigDecimal soldeMinObligatoire;
        private BigDecimal plafondDepot;
        private String periodiciteCalculInteret;
        
        // Getters et setters
        public Long getIdTypeCompte() { return idTypeCompte; }
        public void setIdTypeCompte(Long idTypeCompte) { this.idTypeCompte = idTypeCompte; }
        
        public String getLibelle() { return libelle; }
        public void setLibelle(String libelle) { this.libelle = libelle; }
        
        public String getCodeType() { return codeType; }
        public void setCodeType(String codeType) { this.codeType = codeType; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public BigDecimal getTauxInteretAnnuel() { return tauxInteretAnnuel; }
        public void setTauxInteretAnnuel(BigDecimal tauxInteretAnnuel) { this.tauxInteretAnnuel = tauxInteretAnnuel; }
        
        public BigDecimal getDepotInitialMin() { return depotInitialMin; }
        public void setDepotInitialMin(BigDecimal depotInitialMin) { this.depotInitialMin = depotInitialMin; }
        
        public BigDecimal getSoldeMinObligatoire() { return soldeMinObligatoire; }
        public void setSoldeMinObligatoire(BigDecimal soldeMinObligatoire) { this.soldeMinObligatoire = soldeMinObligatoire; }
        
        public BigDecimal getPlafondDepot() { return plafondDepot; }
        public void setPlafondDepot(BigDecimal plafondDepot) { this.plafondDepot = plafondDepot; }
        
        public String getPeriodiciteCalculInteret() { return periodiciteCalculInteret; }
        public void setPeriodiciteCalculInteret(String periodiciteCalculInteret) { this.periodiciteCalculInteret = periodiciteCalculInteret; }
    }
}
