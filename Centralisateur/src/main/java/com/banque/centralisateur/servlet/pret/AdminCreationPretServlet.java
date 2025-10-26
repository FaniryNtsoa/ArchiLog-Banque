package com.banque.centralisateur.servlet.pret;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.PretEJBClientFactory;
import com.banque.pret.dto.PretDTO;
import com.banque.pret.dto.TypePretDTO;
import com.banque.pret.dto.ClientDTO;
import com.banque.pret.ejb.remote.PretServiceRemote;
import com.banque.pret.ejb.remote.TypePretServiceRemote;
import com.banque.pret.ejb.remote.ClientServiceRemote;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet pour la création de demandes de prÃªt en mode administrateur
 * Interface admin pour crÃ©er des prÃªts au nom des clients
 */
@WebServlet(name = "AdminCreationPretServlet", urlPatterns = {"/admin/pret/nouveau"})
public class AdminCreationPretServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(AdminCreationPretServlet.class.getName());
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
        
        LOGGER.info("Affichage du formulaire de création de prÃªt - Interface Admin");
        
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // RÃ©cupÃ©rer la liste des types de prÃªts
            TypePretServiceRemote typePretService = PretEJBClientFactory.getTypePretService();
            List<TypePretDTO> typesPrets = typePretService.listerTousLesTypesPrets();
            
            // RÃ©cupÃ©rer la liste des clients
            ClientServiceRemote clientService = PretEJBClientFactory.getClientService();
            List<ClientDTO> clients = clientService.listerTousLesClients();
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Nouveau PrÃªt - Administration");
            context.setVariable("currentPage", "admin-nouveau-pret");
            context.setVariable("moduleName", "PrÃªts");
            context.setVariable("typesPrets", typesPrets);
            context.setVariable("clients", clients);
            context.setVariable("hasTypesPrets", !typesPrets.isEmpty());
            context.setVariable("hasClients", !clients.isEmpty());
            
            // Messages de session
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                context.setVariable("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }
            
            String successMessage = (String) session.getAttribute("successMessage");
            if (successMessage != null) {
                context.setVariable("successMessage", successMessage);
                session.removeAttribute("successMessage");
            }
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("pret/admin-nouveau", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du formulaire de création de prÃªt", e);
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
        
        LOGGER.info("Traitement de création de demande de prÃªt par l'admin");
        
        try {
            // RÃ©cupÃ©rer les paramÃ¨tres du formulaire
            String clientIdStr = request.getParameter("clientId");
            String typePretIdStr = request.getParameter("typePretId");
            String montantDemandeStr = request.getParameter("montantDemande");
            String dureeMoisStr = request.getParameter("dureeMois");
            String tauxInteretStr = request.getParameter("tauxInteret");
            
            // Validation
            if (clientIdStr == null || clientIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez sÃ©lectionner un client");
            }
            
            if (typePretIdStr == null || typePretIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez sÃ©lectionner un type de prÃªt");
            }
            
            if (montantDemandeStr == null || montantDemandeStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez saisir le montant demandÃ©");
            }
            
            if (dureeMoisStr == null || dureeMoisStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez saisir la durÃ©e en mois");
            }
            
            Long clientId = Long.parseLong(clientIdStr);
            Long typePretId = Long.parseLong(typePretIdStr);
            BigDecimal montantDemande = new BigDecimal(montantDemandeStr);
            Integer dureeMois = Integer.parseInt(dureeMoisStr);
            
            if (montantDemande.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Le montant doit Ãªtre positif");
            }
            
            if (dureeMois <= 0) {
                throw new IllegalArgumentException("La durÃ©e doit Ãªtre positive");
            }
            
            // CrÃ©er le DTO du prÃªt
            PretDTO pretDTO = PretDTO.builder()
                .idClient(clientId)
                .idTypePret(typePretId)
                .montantDemande(montantDemande)
                .dureeMois(dureeMois)
                .dateDemande(LocalDate.now())
                .idAdministrateur(1L) // ID administrateur par dÃ©faut
                .build();
            
            // Si un taux personnalisÃ© est fourni
            if (tauxInteretStr != null && !tauxInteretStr.trim().isEmpty()) {
                BigDecimal tauxInteret = new BigDecimal(tauxInteretStr);
                if (tauxInteret.compareTo(BigDecimal.ZERO) > 0) {
                    pretDTO.setTauxInteretAnnuel(tauxInteret);
                }
            }
            
            // CrÃ©er la demande de prÃªt via EJB
            PretServiceRemote pretService = PretEJBClientFactory.getPretService();
            PretDTO pretCree = pretService.creerDemandePretAdmin(pretDTO);
            
            session.setAttribute("successMessage", 
                String.format("Demande de prÃªt nÂ°%s crÃ©Ã©e avec succÃ¨s pour le montant de %s XOF", 
                    pretCree.getNumeroPret(), pretCree.getMontantDemande()));
            
            // Rediriger vers la gestion des prÃªts
            response.sendRedirect(request.getContextPath() + "/admin/pret/gestion");
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format de donnÃ©es invalide", e);
            session.setAttribute("errorMessage", "Format de donnÃ©es invalide. Veuillez vÃ©rifier vos saisies.");
            response.sendRedirect(request.getContextPath() + "/admin/pret/nouveau");
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Validation Ã©chouÃ©e: " + e.getMessage(), e);
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/pret/nouveau");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création du prÃªt", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors de la création. Veuillez rÃ©essayer.");
            response.sendRedirect(request.getContextPath() + "/admin/pret/nouveau");
        }
    }
}
