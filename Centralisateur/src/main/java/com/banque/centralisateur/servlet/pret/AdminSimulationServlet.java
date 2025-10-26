package com.banque.centralisateur.servlet.pret;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.PretEJBClientFactory;
import com.banque.pret.dto.SimulationPretDTO;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet pour la simulation de prÃªt via EJB - Module PrÃªt
 * CORRIGÃ‰ pour utiliser les services EJB au lieu de REST
 */
@WebServlet(name = "PretSimulationServlet", urlPatterns = {"/admin/pret/simulation"})
public class AdminSimulationServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(AdminSimulationServlet.class.getName());
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
        
        LOGGER.info("Affichage de la simulation de prÃªt via EJB - Interface Admin");
        
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // RÃ©cupÃ©rer les types de prÃªts via EJB
            TypePretServiceRemote typePretService = PretEJBClientFactory.getTypePretService();
            List<TypePretDTO> typesPrets = typePretService.listerTousLesTypesPrets();
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Simulation de PrÃªt - Administration");
            context.setVariable("currentPage", "admin-simulation-pret");
            context.setVariable("moduleName", "PrÃªts");
            context.setVariable("typesPrets", typesPrets);
            context.setVariable("hasTypesPrets", !typesPrets.isEmpty());
            
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
            
            // RÃ©sultats de simulation prÃ©cÃ©dente
            SimulationPretDTO simulationResultat = (SimulationPretDTO) session.getAttribute("simulationResultat");
            if (simulationResultat != null) {
                context.setVariable("simulationResultat", simulationResultat);
                context.setVariable("hasSimulation", true);
                session.removeAttribute("simulationResultat");
            }
            
            // Rendu du template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/admin-simulation", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement de la simulation de prÃªt via EJB", e);
            session.setAttribute("errorMessage", "Impossible de charger la page de simulation");
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
        
        LOGGER.info("Traitement d'une simulation de prÃªt via EJB - Interface Admin");
        
        try {
            // RÃ©cupÃ©rer les paramÃ¨tres du formulaire
            String montantStr = request.getParameter("montant");
            String dureeStr = request.getParameter("duree");
            String typePretIdStr = request.getParameter("typePretId");
            String tauxPersonnaliseStr = request.getParameter("tauxPersonnalise");
            
            // Validation
            if (montantStr == null || montantStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez saisir un montant");
            }
            
            if (dureeStr == null || dureeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez saisir une durÃ©e");
            }
            
            if (typePretIdStr == null || typePretIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez sÃ©lectionner un type de prÃªt");
            }
            
            BigDecimal montant = new BigDecimal(montantStr);
            Integer duree = Integer.parseInt(dureeStr);
            Long typePretId = Long.parseLong(typePretIdStr);
            
            if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant doit Ãªtre positif");
            }
            
            if (duree <= 0 || duree > 360) {
                throw new IllegalArgumentException("La durÃ©e doit Ãªtre entre 1 et 360 mois");
            }
            
            // CrÃ©er le DTO de simulation
            SimulationPretDTO simulationDTO = SimulationPretDTO.builder()
                .montantDemande(montant)
                .dureeMois(duree)
                .idTypePret(typePretId)
                .build();
            
            // Ajouter un taux personnalisÃ© si fourni
            if (tauxPersonnaliseStr != null && !tauxPersonnaliseStr.trim().isEmpty()) {
                BigDecimal tauxPersonnalise = new BigDecimal(tauxPersonnaliseStr);
                if (tauxPersonnalise.compareTo(BigDecimal.ZERO) > 0 && tauxPersonnalise.compareTo(new BigDecimal("50")) <= 0) {
                    simulationDTO.setTauxInteretAnnuel(tauxPersonnalise);
                }
            }
            
            // Effectuer la simulation via EJB
            PretServiceRemote pretService = PretEJBClientFactory.getPretService();
            SimulationPretDTO resultatSimulation = pretService.simulerPret(simulationDTO);
            
            // Stocker le rÃ©sultat pour affichage
            session.setAttribute("simulationResultat", resultatSimulation);
            session.setAttribute("successMessage", "Simulation calculÃ©e avec succÃ¨s !");
            
            response.sendRedirect(request.getContextPath() + "/admin/pret/simulation");
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format de donnÃ©es invalide pour la simulation", e);
            session.setAttribute("errorMessage", "Format de donnÃ©es invalide. VÃ©rifiez vos saisies numÃ©riques.");
            response.sendRedirect(request.getContextPath() + "/admin/pret/simulation");
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Validation Ã©chouÃ©e: " + e.getMessage(), e);
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/pret/simulation");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la simulation via EJB", e);
            session.setAttribute("errorMessage", "Erreur systÃ¨me lors de la simulation. Veuillez rÃ©essayer.");
            response.sendRedirect(request.getContextPath() + "/admin/pret/simulation");
        }
    }
}
