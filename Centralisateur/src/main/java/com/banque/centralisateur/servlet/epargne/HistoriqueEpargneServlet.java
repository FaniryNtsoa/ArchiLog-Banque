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
 * Servlet pour gérer l'historique des opérations sur les comptes épargne
 */
@WebServlet(name = "HistoriqueEpargneServlet", urlPatterns = {"/epargne/historique"})
public class HistoriqueEpargneServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(HistoriqueEpargneServlet.class.getName());
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
        String compteIdStr = request.getParameter("compteId");
        
        LOGGER.info("Affichage de l'historique épargne pour le client: " + clientId);
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // Récupérer tous les comptes épargne du client
            List<JsonObject> comptesJson = epargneClient.getComptesClient(clientId);
            
            List<CompteSimpleView> comptes = new ArrayList<>();
            Long selectedCompteId = null;
            
            // Convertir en vue simple
            for (JsonObject compte : comptesJson) {
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
                
                comptes.add(view);
                
                // Si c'est le compte sélectionné
                if (compteIdStr != null && !compteIdStr.isEmpty()) {
                    Long paramCompteId = Long.parseLong(compteIdStr);
                    if (view.getIdCompte().equals(paramCompteId)) {
                        selectedCompteId = paramCompteId;
                    }
                }
            }
            
            // Si aucun compte sélectionné mais il y a des comptes, sélectionner le premier
            if (selectedCompteId == null && !comptes.isEmpty()) {
                selectedCompteId = comptes.get(0).getIdCompte();
            }
            
            // Récupérer les opérations du compte sélectionné
            List<OperationView> operations = new ArrayList<>();
            if (selectedCompteId != null) {
                List<JsonObject> operationsJson = epargneClient.getOperationsCompte(selectedCompteId, 1, 50);
                
                for (JsonObject op : operationsJson) {
                    OperationView opView = new OperationView();
                    opView.setIdOperation(JsonHelper.getSafeLong(op, "idOperation"));
                    opView.setTypeOperation(JsonHelper.getSafeString(op, "typeOperation", ""));
                    opView.setMontant(JsonHelper.getSafeBigDecimal(op, "montant", BigDecimal.ZERO));
                    opView.setSoldeAvant(JsonHelper.getSafeBigDecimal(op, "soldeAvant", BigDecimal.ZERO));
                    opView.setSoldeApres(JsonHelper.getSafeBigDecimal(op, "soldeApres", BigDecimal.ZERO));
                    opView.setDescription(JsonHelper.getSafeString(op, "description", ""));
                    opView.setDateOperation(JsonHelper.getSafeString(op, "dateOperation", ""));
                    operations.add(opView);
                }
            }
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Historique Épargne - Banque Premium");
            context.setVariable("currentPage", "historique-epargne");
            context.setVariable("clientPrenom", session.getAttribute("clientPrenom"));
            context.setVariable("clientNom", session.getAttribute("clientNom"));
            context.setVariable("comptes", comptes);
            context.setVariable("selectedCompteId", selectedCompteId);
            context.setVariable("operations", operations);
            context.setVariable("hasComptes", !comptes.isEmpty());
            context.setVariable("hasOperations", !operations.isEmpty());
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("epargne/historique-epargne", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de l'historique épargne", e);
            context.setVariable("pageTitle", "Historique Épargne - Banque Premium");
            context.setVariable("currentPage", "historique-epargne");
            context.setVariable("clientPrenom", session.getAttribute("clientPrenom"));
            context.setVariable("clientNom", session.getAttribute("clientNom"));
            context.setVariable("errorMessage", "Impossible de récupérer l'historique. Veuillez réessayer.");
            context.setVariable("comptes", new ArrayList<>());
            context.setVariable("operations", new ArrayList<>());
            context.setVariable("hasComptes", false);
            context.setVariable("hasOperations", false);
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("epargne/historique-epargne", context, response.getWriter());
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
    
    /**
     * Classe interne pour afficher une opération
     */
    public static class OperationView {
        private Long idOperation;
        private String typeOperation;
        private BigDecimal montant;
        private BigDecimal soldeAvant;
        private BigDecimal soldeApres;
        private String description;
        private String dateOperation;
        
        public Long getIdOperation() { return idOperation; }
        public void setIdOperation(Long idOperation) { this.idOperation = idOperation; }
        
        public String getTypeOperation() { return typeOperation; }
        public void setTypeOperation(String typeOperation) { this.typeOperation = typeOperation; }
        
        public BigDecimal getMontant() { return montant; }
        public void setMontant(BigDecimal montant) { this.montant = montant; }
        
        public BigDecimal getSoldeAvant() { return soldeAvant; }
        public void setSoldeAvant(BigDecimal soldeAvant) { this.soldeAvant = soldeAvant; }
        
        public BigDecimal getSoldeApres() { return soldeApres; }
        public void setSoldeApres(BigDecimal soldeApres) { this.soldeApres = soldeApres; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getDateOperation() { return dateOperation; }
        public void setDateOperation(String dateOperation) { this.dateOperation = dateOperation; }
    }
}
