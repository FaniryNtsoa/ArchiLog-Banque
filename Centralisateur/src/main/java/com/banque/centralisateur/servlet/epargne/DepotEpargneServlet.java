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
 * Servlet admin pour gÃ©rer les dÃ©pÃ´ts sur les comptes Ã©pargne
 */
@WebServlet(name = "AdminDepotEpargneServlet", urlPatterns = {"/admin/epargne/depot"})
public class DepotEpargneServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(DepotEpargneServlet.class.getName());
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
        if (session == null || session.getAttribute("userSessionBean") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        LOGGER.info("Affichage du formulaire de dÃ©pÃ´t Ã©pargne - Interface Admin");
        
        // CrÃ©er le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // RÃ©cupÃ©rer tous les comptes Ã©pargne pour l'admin
            List<JsonObject> comptes = epargneClient.getAllComptes();
            
            // Filtrer uniquement les comptes actifs
            List<CompteSimpleView> comptesActifs = new ArrayList<>();
            for (JsonObject compte : comptes) {
                String statut = JsonHelper.getSafeString(compte, "statut", "");
                if ("ACTIF".equals(statut)) {
                    CompteSimpleView view = new CompteSimpleView();
                    view.setIdCompte(JsonHelper.getSafeLong(compte, "idCompte"));
                    view.setNumeroCompte(JsonHelper.getSafeString(compte, "numeroCompte", ""));
                    view.setSolde(JsonHelper.getSafeBigDecimal(compte, "solde", BigDecimal.ZERO));
                    
                    // Ajouter info client pour l'admin
                    if (compte.containsKey("client") || compte.containsKey("Client")) {
                        JsonObject client = compte.containsKey("client") ? 
                            compte.getJsonObject("client") : 
                            compte.getJsonObject("Client");
                        view.setClientNom(JsonHelper.getSafeString(client, "nom", ""));
                        view.setClientPrenom(JsonHelper.getSafeString(client, "prenom", ""));
                    }
                    
                    if (compte.containsKey("typeCompte") || compte.containsKey("TypeCompte")) {
                        JsonObject typeCompte = compte.containsKey("typeCompte") ? 
                            compte.getJsonObject("typeCompte") : 
                            compte.getJsonObject("TypeCompte");
                        view.setTypeLibelle(JsonHelper.getSafeString(typeCompte, "libelle", ""));
                    }
                    
                    comptesActifs.add(view);
                }
            }
            
            // Ajouter les variables au contexte pour l'admin
            context.setVariable("pageTitle", "DÃ©pÃ´t Ã‰pargne - Administration");
            context.setVariable("currentPage", "admin-depot-epargne");
            context.setVariable("moduleName", "Ã‰pargne");
            context.setVariable("operationType", "DÃ©pÃ´t");
            context.setVariable("comptes", comptesActifs);
            context.setVariable("hasComptes", !comptesActifs.isEmpty());
            
            // Messages de session
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                context.setVariable("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }
            
            // Rendu du template admin
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("epargne/depot", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du formulaire de dÃ©pÃ´t Ã©pargne", e);
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
        
        LOGGER.info("Traitement admin d'un dÃ©pÃ´t sur compte Ã©pargne");
        
        // RÃ©cupÃ©rer les paramÃ¨tres
        String compteIdStr = request.getParameter("compteId");
        String montantStr = request.getParameter("montant");
        String description = request.getParameter("description");
        
        try {
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
            
            // Effectuer le dÃ©pÃ´t via l'API (avec ID administrateur par dÃ©faut)
            JsonObject responseJson = epargneClient.effectuerDepot(compteId, montant, description, 1L);
            
            if (responseJson != null && JsonHelper.getSafeBoolean(responseJson, "success", false)) {
                JsonObject operation = responseJson.getJsonObject("data");
                BigDecimal nouveauSolde = JsonHelper.getSafeBigDecimal(operation, "soldeApres", BigDecimal.ZERO);
                
                session.setAttribute("successMessage", 
                    String.format("DÃ©pÃ´t de %s effectuÃ© avec succÃ¨s ! Nouveau solde : %s", 
                        montant, nouveauSolde));
                response.sendRedirect(request.getContextPath() + "/admin/epargne/comptes");
                
            } else {
                String errorMsg = responseJson != null ? 
                    JsonHelper.getSafeString(responseJson, "message", "Erreur lors du dÃ©pÃ´t") : 
                    "Erreur lors du depot";
                    
                session.setAttribute("errorMessage", errorMsg);
                response.sendRedirect(request.getContextPath() + "/admin/epargne/depot");
            }
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format de donnÃ©es invalide", e);
            session.setAttribute("errorMessage", "Format de donnÃ©es invalide. Veuillez vÃ©rifier vos saisies.");
            response.sendRedirect(request.getContextPath() + "/admin/epargne/depot");
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Validation Ã©chouÃ©e: " + e.getMessage(), e);
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/epargne/depot");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du dÃ©pÃ´t Ã©pargne", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors du dÃ©pÃ´t. Veuillez rÃ©essayer.");
            response.sendRedirect(request.getContextPath() + "/admin/epargne/depot");
        }
    }
    
    /**
     * Classe interne pour afficher un compte de maniÃ¨re simplifiÃ©e
     */
    public static class CompteSimpleView {
        private Long idCompte;
        private String numeroCompte;
        private BigDecimal solde;
        private String typeLibelle;
        private String clientNom;
        private String clientPrenom;
        
        public Long getIdCompte() { return idCompte; }
        public void setIdCompte(Long idCompte) { this.idCompte = idCompte; }
        
        public String getNumeroCompte() { return numeroCompte; }
        public void setNumeroCompte(String numeroCompte) { this.numeroCompte = numeroCompte; }
        
        public BigDecimal getSolde() { return solde; }
        public void setSolde(BigDecimal solde) { this.solde = solde; }
        
        public String getTypeLibelle() { return typeLibelle; }
        public void setTypeLibelle(String typeLibelle) { this.typeLibelle = typeLibelle; }
        
        public String getClientNom() { return clientNom; }
        public void setClientNom(String clientNom) { this.clientNom = clientNom; }
        
        public String getClientPrenom() { return clientPrenom; }
        public void setClientPrenom(String clientPrenom) { this.clientPrenom = clientPrenom; }
    }
}

