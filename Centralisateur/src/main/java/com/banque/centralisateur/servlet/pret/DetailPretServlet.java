package com.banque.centralisateur.servlet.pret;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.PretEJBClientFactory;
import com.banque.pret.dto.EcheanceDTO;
import com.banque.pret.dto.PretDTO;
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
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet pour afficher le détail d'un prêt avec son tableau d'amortissement final
 */
@WebServlet(name = "DetailPretServlet", urlPatterns = {"/pret/detail"})
public class DetailPretServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(DetailPretServlet.class.getName());
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
        if (session == null || session.getAttribute("clientNom") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        Long idClient = (Long) session.getAttribute("clientId");
        String idPretStr = request.getParameter("idPret");
        
        LOGGER.info("Affichage du détail du prêt ID: " + idPretStr + " pour le client ID: " + idClient);
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Détail du Prêt - Banque Premium");
        context.setVariable("currentPage", "mes-prets");
        context.setVariable("clientNom", session.getAttribute("clientNom"));
        context.setVariable("clientPrenom", session.getAttribute("clientPrenom"));
        
        try {
            // Validation
            if (idPretStr == null || idPretStr.trim().isEmpty()) {
                context.setVariable("errorMessage", "ID du prêt manquant");
                response.sendRedirect(request.getContextPath() + "/pret/mes-prets");
                return;
            }
            
            Long idPret = Long.parseLong(idPretStr);
            
            // Récupération du prêt
            PretServiceRemote pretService = PretEJBClientFactory.getPretService();
            PretDTO pret = pretService.rechercherPretParId(idPret);
            
            if (pret == null) {
                context.setVariable("errorMessage", "Prêt non trouvé");
                response.sendRedirect(request.getContextPath() + "/pret/mes-prets");
                return;
            }
            
            // Vérifier que le prêt appartient bien au client connecté
            if (!pret.getIdClient().equals(idClient)) {
                context.setVariable("errorMessage", "Accès non autorisé");
                response.sendRedirect(request.getContextPath() + "/pret/mes-prets");
                return;
            }
            
            // Récupération du tableau d'amortissement
            EcheanceServiceRemote echeanceService = PretEJBClientFactory.getEcheanceService();
            List<EcheanceDTO> tableauAmortissement = echeanceService.obtenirTableauAmortissement(idPret);
            
            // Calculs statistiques
            long nombreEcheancesTotales = tableauAmortissement.size();
            long nombreEcheancesPayees = tableauAmortissement.stream()
                .filter(e -> "PAYE".equals(e.getStatut()))
                .count();
            long nombreEcheancesEnRetard = tableauAmortissement.stream()
                .filter(e -> "EN_RETARD".equals(e.getStatut()))
                .count();
            
            BigDecimal totalPenalites = tableauAmortissement.stream()
                .filter(e -> e.getPenaliteAppliquee() != null)
                .map(EcheanceDTO::getPenaliteAppliquee)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Progression en pourcentage
            double progressionPaiement = nombreEcheancesTotales > 0 ? 
                (nombreEcheancesPayees * 100.0 / nombreEcheancesTotales) : 0;
            
            // Ajouter les variables au contexte
            context.setVariable("pret", pret);
            context.setVariable("tableauAmortissement", tableauAmortissement);
            context.setVariable("nombreEcheancesTotales", nombreEcheancesTotales);
            context.setVariable("nombreEcheancesPayees", nombreEcheancesPayees);
            context.setVariable("nombreEcheancesEnRetard", nombreEcheancesEnRetard);
            context.setVariable("totalPenalites", totalPenalites);
            context.setVariable("progressionPaiement", Math.round(progressionPaiement));
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/detail-pret", context, response.getWriter());
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format d'ID invalide", e);
            response.sendRedirect(request.getContextPath() + "/pret/mes-prets");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'affichage du détail du prêt", e);
            context.setVariable("errorMessage", "Une erreur est survenue lors de l'affichage du prêt");
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/detail-pret", context, response.getWriter());
        }
    }
}
