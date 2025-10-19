package com.banque.centralisateur.servlet.pret;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.PretEJBClientFactory;
import com.banque.pret.dto.PretDTO;
import com.banque.pret.dto.TypePretDTO;
import com.banque.pret.ejb.remote.PretServiceRemote;
import com.banque.pret.ejb.remote.TypePretServiceRemote;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet pour créer une demande de prêt
 */
@WebServlet(name = "DemandePretServlet", urlPatterns = {"/pret/demande"})
public class DemandePretServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(DemandePretServlet.class.getName());
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
        
        LOGGER.info("Affichage de la page de demande de prêt");
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        // Charger les types de prêts disponibles
        try {
            TypePretServiceRemote typePretService = PretEJBClientFactory.getTypePretService();
            List<TypePretDTO> typesPrets = typePretService.listerTypesPretsActifs();
            context.setVariable("typesPrets", typesPrets);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des types de prêts", e);
            context.setVariable("errorMessage", "Impossible de charger les types de prêts");
        }
        
        // Ajouter les variables au contexte
        context.setVariable("pageTitle", "Demande de Prêt - Banque Premium");
        context.setVariable("currentPage", "demande-pret");
        context.setVariable("clientNom", session.getAttribute("clientNom"));
        context.setVariable("clientPrenom", session.getAttribute("clientPrenom"));
        
        // Rendre le template
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("pret/demande-pret", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("clientNom") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        Long idClient = (Long) session.getAttribute("clientId");
        
        LOGGER.info("Traitement d'une demande de prêt pour le client ID: " + idClient);
        
        // Récupérer les paramètres du formulaire
        String idTypePretStr = request.getParameter("idTypePret");
        String montantStr = request.getParameter("montant");
        String dureeMoisStr = request.getParameter("dureeMois");
        String dateDebutRemboursementStr = request.getParameter("dateDebutRemboursement");
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Demande de Prêt - Banque Premium");
        context.setVariable("currentPage", "demande-pret");
        context.setVariable("clientNom", session.getAttribute("clientNom"));
        context.setVariable("clientPrenom", session.getAttribute("clientPrenom"));
        
        // Charger les types de prêts disponibles
        try {
            TypePretServiceRemote typePretService = PretEJBClientFactory.getTypePretService();
            List<TypePretDTO> typesPrets = typePretService.listerTypesPretsActifs();
            context.setVariable("typesPrets", typesPrets);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des types de prêts", e);
        }
        
        try {
            // Validation des champs
            if (idTypePretStr == null || idTypePretStr.trim().isEmpty() ||
                montantStr == null || montantStr.trim().isEmpty() ||
                dureeMoisStr == null || dureeMoisStr.trim().isEmpty() ||
                dateDebutRemboursementStr == null || dateDebutRemboursementStr.trim().isEmpty()) {
                
                context.setVariable("errorMessage", "Veuillez remplir tous les champs obligatoires");
                context.setVariable("idTypePret", idTypePretStr);
                context.setVariable("montant", montantStr);
                context.setVariable("dureeMois", dureeMoisStr);
                context.setVariable("dateDebutRemboursement", dateDebutRemboursementStr);
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("pret/demande-pret", context, response.getWriter());
                return;
            }
            
            // Créer le DTO de prêt
            PretDTO pretDTO = new PretDTO();
            pretDTO.setIdClient(idClient);
            pretDTO.setIdTypePret(Long.parseLong(idTypePretStr));
            pretDTO.setMontantDemande(new BigDecimal(montantStr));
            pretDTO.setDureeMois(Integer.parseInt(dureeMoisStr));
            
            // Parser la date de début
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate dateDebutRemboursement = LocalDate.parse(dateDebutRemboursementStr, formatter);
            pretDTO.setDatePremiereEcheance(dateDebutRemboursement);
            
            // Appeler le service pour créer la demande
            PretServiceRemote pretService = PretEJBClientFactory.getPretService();
            PretDTO pretCree = pretService.creerDemandePret(pretDTO);
            
            LOGGER.info("Demande de prêt créée avec succès: " + pretCree.getNumeroPret());
            
            // Rediriger vers la liste des prêts avec un message de succès
            session.setAttribute("successMessage", "Votre demande de prêt a été créée avec succès. Numéro: " + pretCree.getNumeroPret());
            response.sendRedirect(request.getContextPath() + "/pret/mes-prets");
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Erreur de format de nombre", e);
            context.setVariable("errorMessage", "Format de nombre invalide");
            context.setVariable("idTypePret", idTypePretStr);
            context.setVariable("montant", montantStr);
            context.setVariable("dureeMois", dureeMoisStr);
            context.setVariable("dateDebutRemboursement", dateDebutRemboursementStr);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/demande-pret", context, response.getWriter());
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Erreur de validation: " + e.getMessage(), e);
            context.setVariable("errorMessage", e.getMessage());
            context.setVariable("idTypePret", idTypePretStr);
            context.setVariable("montant", montantStr);
            context.setVariable("dureeMois", dureeMoisStr);
            context.setVariable("dateDebutRemboursement", dateDebutRemboursementStr);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/demande-pret", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création de la demande de prêt", e);
            context.setVariable("errorMessage", "Une erreur est survenue lors de la création de votre demande. Veuillez réessayer.");
            context.setVariable("idTypePret", idTypePretStr);
            context.setVariable("montant", montantStr);
            context.setVariable("dureeMois", dureeMoisStr);
            context.setVariable("dateDebutRemboursement", dateDebutRemboursementStr);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/demande-pret", context, response.getWriter());
        }
    }
}
