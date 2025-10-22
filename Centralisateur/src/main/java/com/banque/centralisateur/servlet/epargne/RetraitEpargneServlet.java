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
 * Servlet pour gérer les retraits sur les comptes épargne
 */
@WebServlet(name = "RetraitEpargneServlet", urlPatterns = {"/epargne/retrait"})
public class RetraitEpargneServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(RetraitEpargneServlet.class.getName());
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
        LOGGER.info("Affichage du formulaire de retrait épargne pour le client: " + clientId);
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // Récupérer les comptes épargne du client
            List<JsonObject> comptes = epargneClient.getComptesClient(clientId);
            
            // Filtrer uniquement les comptes actifs
            List<CompteSimpleView> comptesActifs = new ArrayList<>();
            for (JsonObject compte : comptes) {
                String statut = JsonHelper.getSafeString(compte, "statut", "");
                if ("ACTIF".equals(statut)) {
                    CompteSimpleView view = new CompteSimpleView();
                    view.setIdCompte(JsonHelper.getSafeLong(compte, "idCompte"));
                    view.setNumeroCompte(JsonHelper.getSafeString(compte, "numeroCompte", ""));
                    view.setSolde(JsonHelper.getSafeBigDecimal(compte, "solde", BigDecimal.ZERO));
                    
                    if (compte.containsKey("typeCompte") || compte.containsKey("TypeCompte")) {
                        JsonObject typeCompte = compte.containsKey("typeCompte") ? 
                            compte.getJsonObject("typeCompte") : 
                            compte.getJsonObject("TypeCompte");
                        view.setTypeLibelle(JsonHelper.getSafeString(typeCompte, "libelle", ""));
                    }
                    
                    comptesActifs.add(view);
                }
            }
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Retrait Épargne - Banque Premium");
            context.setVariable("currentPage", "retrait-epargne");
            context.setVariable("clientPrenom", session.getAttribute("clientPrenom"));
            context.setVariable("clientNom", session.getAttribute("clientNom"));
            context.setVariable("comptes", comptesActifs);
            context.setVariable("hasComptes", !comptesActifs.isEmpty());
            
            // Messages de session
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                context.setVariable("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("epargne/retrait-epargne", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du formulaire de retrait épargne", e);
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
        
        LOGGER.info("Traitement d'un retrait sur compte épargne");
        
        // Récupérer les paramètres
        String compteIdStr = request.getParameter("compteId");
        String montantStr = request.getParameter("montant");
        String description = request.getParameter("description");
        
        try {
            // Validation
            if (compteIdStr == null || compteIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez sélectionner un compte");
            }
            
            if (montantStr == null || montantStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez saisir un montant");
            }
            
            Long compteId = Long.parseLong(compteIdStr);
            BigDecimal montant = new BigDecimal(montantStr);
            
            if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant doit être positif");
            }
            
            // Effectuer le retrait via l'API (avec ID administrateur par défaut)
            JsonObject responseJson = epargneClient.effectuerRetrait(compteId, montant, description, 1L);
            
            if (responseJson != null && JsonHelper.getSafeBoolean(responseJson, "success", false)) {
                JsonObject operation = responseJson.getJsonObject("data");
                BigDecimal nouveauSolde = JsonHelper.getSafeBigDecimal(operation, "soldeApres", BigDecimal.ZERO);
                
                session.setAttribute("successMessage", 
                    String.format("Retrait de %s effectué avec succès ! Nouveau solde : %s", 
                        montant, nouveauSolde));
                response.sendRedirect(request.getContextPath() + "/epargne/comptes");
                
            } else {
                String errorMsg = responseJson != null ? 
                    JsonHelper.getSafeString(responseJson, "message", "Erreur lors du retrait") : 
                    "Erreur lors du retrait";
                    
                session.setAttribute("errorMessage", errorMsg);
                response.sendRedirect(request.getContextPath() + "/epargne/retrait");
            }
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format de données invalide", e);
            session.setAttribute("errorMessage", "Format de données invalide. Veuillez vérifier vos saisies.");
            response.sendRedirect(request.getContextPath() + "/epargne/retrait");
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Validation échouée: " + e.getMessage(), e);
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/epargne/retrait");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du retrait épargne", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors du retrait. Veuillez réessayer.");
            response.sendRedirect(request.getContextPath() + "/epargne/retrait");
        }
    }
    
    /**
     * Classe interne pour afficher un compte de manière simplifiée
     */
    public static class CompteSimpleView {
        private Long idCompte;
        private String numeroCompte;
        private BigDecimal solde;
        private String typeLibelle;
        
        public Long getIdCompte() { return idCompte; }
        public void setIdCompte(Long idCompte) { this.idCompte = idCompte; }
        
        public String getNumeroCompte() { return numeroCompte; }
        public void setNumeroCompte(String numeroCompte) { this.numeroCompte = numeroCompte; }
        
        public BigDecimal getSolde() { return solde; }
        public void setSolde(BigDecimal solde) { this.solde = solde; }
        
        public String getTypeLibelle() { return typeLibelle; }
        public void setTypeLibelle(String typeLibelle) { this.typeLibelle = typeLibelle; }
    }
}
