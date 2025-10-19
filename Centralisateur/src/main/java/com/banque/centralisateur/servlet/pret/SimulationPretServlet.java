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
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet pour la simulation de prêt
 */
@WebServlet(name = "SimulationPretServlet", urlPatterns = {"/pret/simulation"})
public class SimulationPretServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(SimulationPretServlet.class.getName());
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
        
        LOGGER.info("Affichage de la page de simulation de prêt");
        
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
        context.setVariable("pageTitle", "Simulation de Prêt - Banque Premium");
        context.setVariable("currentPage", "simulation-pret");
        context.setVariable("clientNom", session.getAttribute("clientNom"));
        context.setVariable("clientPrenom", session.getAttribute("clientPrenom"));
        
        // Rendre le template
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("pret/simulation-pret", context, response.getWriter());
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
        
        LOGGER.info("Traitement d'une simulation de prêt pour le client ID: " + idClient);
        
        // Récupérer les paramètres du formulaire
        String idTypePretStr = request.getParameter("idTypePret");
        String montantStr = request.getParameter("montant");
        String dureeEnMoisStr = request.getParameter("dureeEnMois");
        String tauxInteretAnnuelStr = request.getParameter("tauxInteretAnnuel");
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Simulation de Prêt - Banque Premium");
        context.setVariable("currentPage", "simulation-pret");
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
                dureeEnMoisStr == null || dureeEnMoisStr.trim().isEmpty()) {
                
                context.setVariable("errorMessage", "Veuillez remplir tous les champs obligatoires");
                context.setVariable("idTypePret", idTypePretStr);
                context.setVariable("montant", montantStr);
                context.setVariable("dureeEnMois", dureeEnMoisStr);
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("pret/simulation-pret", context, response.getWriter());
                return;
            }
            
            // Récupérer le revenu mensuel du client pour la vérification des 33%
            BigDecimal revenuMensuel = null;
            try {
                com.banque.pret.ejb.remote.ClientServiceRemote clientService = PretEJBClientFactory.getClientService();
                com.banque.pret.dto.ClientDTO clientDTO = clientService.rechercherClientParId(idClient);
                if (clientDTO != null) {
                    revenuMensuel = clientDTO.getRevenuMensuel();
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Impossible de récupérer le revenu mensuel du client", e);
            }
            
            // Créer le DTO de simulation
            SimulationPretDTO simulationDTO = new SimulationPretDTO();
            
            Long idTypePret = Long.parseLong(idTypePretStr);
            BigDecimal montant = new BigDecimal(montantStr);
            Integer dureeEnMois = Integer.parseInt(dureeEnMoisStr);
            
            simulationDTO.setIdTypePret(idTypePret);
            simulationDTO.setMontantDemande(montant);
            simulationDTO.setDureeMois(dureeEnMois);
            simulationDTO.setRevenuMensuel(revenuMensuel);
            
            // Si le taux est fourni, l'utiliser, sinon le service utilisera celui du type
            if (tauxInteretAnnuelStr != null && !tauxInteretAnnuelStr.trim().isEmpty()) {
                BigDecimal tauxInteretAnnuel = new BigDecimal(tauxInteretAnnuelStr);
                simulationDTO.setTauxInteretAnnuel(tauxInteretAnnuel);
            }
            
            // Appeler le service de simulation
            PretServiceRemote pretService = PretEJBClientFactory.getPretService();
            SimulationPretDTO resultatSimulation = pretService.simulerPret(simulationDTO);
            
            LOGGER.info("Simulation de prêt effectuée avec succès");
            
            // Ajouter le résultat au contexte
            context.setVariable("simulation", resultatSimulation);
            context.setVariable("successMessage", "Simulation effectuée avec succès");
            context.setVariable("idTypePret", idTypePretStr);
            context.setVariable("montant", montantStr);
            context.setVariable("dureeEnMois", dureeEnMoisStr);
            
            // Rendre le template avec les résultats
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/simulation-pret", context, response.getWriter());
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Erreur de format de nombre", e);
            context.setVariable("errorMessage", "Format de nombre invalide");
            context.setVariable("idTypePret", idTypePretStr);
            context.setVariable("montant", montantStr);
            context.setVariable("dureeEnMois", dureeEnMoisStr);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/simulation-pret", context, response.getWriter());
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Erreur de validation: " + e.getMessage(), e);
            context.setVariable("errorMessage", e.getMessage());
            context.setVariable("idTypePret", idTypePretStr);
            context.setVariable("montant", montantStr);
            context.setVariable("dureeEnMois", dureeEnMoisStr);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/simulation-pret", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la simulation de prêt", e);
            context.setVariable("errorMessage", "Une erreur est survenue lors de la simulation. Veuillez réessayer.");
            context.setVariable("idTypePret", idTypePretStr);
            context.setVariable("montant", montantStr);
            context.setVariable("dureeEnMois", dureeEnMoisStr);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/simulation-pret", context, response.getWriter());
        }
    }
}
