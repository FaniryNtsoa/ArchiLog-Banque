package com.banque.centralisateur.servlet;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.ClientDTO;
import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.dto.VirementDTO;
import com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote;
import com.banque.situationbancaire.ejb.remote.OperationServiceRemote;
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
 * Servlet pour effectuer un virement
 */
@WebServlet(name = "VirementServlet", urlPatterns = {"/virement"})
public class VirementServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(VirementServlet.class.getName());
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
        
        // Vérifier si l'utilisateur est connecté
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("client") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        ClientDTO client = (ClientDTO) session.getAttribute("client");
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Virement - Banque Premium");
        context.setVariable("currentPage", "virement");
        context.setVariable("client", client);
        
        try {
            // Récupérer les comptes du client
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            List<CompteCourantDTO> comptes = compteService.listerComptesParClient(client.getIdClient());
            context.setVariable("comptes", comptes);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement des comptes", e);
            context.setVariable("errorMessage", "Erreur lors du chargement des comptes");
        }
        
        // Rendre le template
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("virement", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Vérifier si l'utilisateur est connecté
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("client") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        
        ClientDTO client = (ClientDTO) session.getAttribute("client");
        LOGGER.info("Virement pour: " + client.getEmail());
        
        // Récupérer les données du formulaire
        String numeroCompteDebiteur = request.getParameter("numeroCompteDebiteur");
        String numeroCompteCrediteur = request.getParameter("numeroCompteCrediteur");
        String montantStr = request.getParameter("montant");
        String libelle = request.getParameter("libelle");
        
        try {
            // Récupérer les comptes pour l'affichage
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            List<CompteCourantDTO> comptes = compteService.listerComptesParClient(client.getIdClient());
            
            // Créer le contexte Thymeleaf
            IWebExchange webExchange = this.application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange);
            context.setVariable("pageTitle", "Virement - Banque Premium");
            context.setVariable("currentPage", "virement");
            context.setVariable("client", client);
            context.setVariable("comptes", comptes);
            context.setVariable("numeroCompteDebiteur", numeroCompteDebiteur);
            context.setVariable("numeroCompteCrediteur", numeroCompteCrediteur);
            context.setVariable("montant", montantStr);
            context.setVariable("libelle", libelle);
            
            // Validation
            if (numeroCompteDebiteur == null || numeroCompteDebiteur.trim().isEmpty()) {
                context.setVariable("errorMessage", "Veuillez sélectionner le compte à débiter");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("virement", context, response.getWriter());
                return;
            }
            
            if (numeroCompteCrediteur == null || numeroCompteCrediteur.trim().isEmpty()) {
                context.setVariable("errorMessage", "Veuillez saisir le compte à créditer");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("virement", context, response.getWriter());
                return;
            }
            
            if (numeroCompteDebiteur.equals(numeroCompteCrediteur)) {
                context.setVariable("errorMessage", "Les comptes débiteur et créditeur doivent être différents");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("virement", context, response.getWriter());
                return;
            }
            
            // Vérifier que le compte débiteur appartient bien au client connecté
            if (!compteService.verifierProprietaireCompte(numeroCompteDebiteur, client.getIdClient())) {
                context.setVariable("errorMessage", "Le compte débiteur ne vous appartient pas");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("virement", context, response.getWriter());
                return;
            }
            
            if (montantStr == null || montantStr.trim().isEmpty()) {
                context.setVariable("errorMessage", "Le montant est obligatoire");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("virement", context, response.getWriter());
                return;
            }
            
            BigDecimal montant;
            try {
                montant = new BigDecimal(montantStr);
                if (montant.compareTo(BigDecimal.ZERO) <= 0) {
                    context.setVariable("errorMessage", "Le montant doit être positif");
                    response.setContentType("text/html;charset=UTF-8");
                    templateEngine.process("virement", context, response.getWriter());
                    return;
                }
            } catch (NumberFormatException e) {
                context.setVariable("errorMessage", "Montant invalide");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("virement", context, response.getWriter());
                return;
            }
            
            // Vérifier que le compte créditeur existe
            CompteCourantDTO compteCrediteur = compteService.rechercherCompteParNumero(numeroCompteCrediteur);
            if (compteCrediteur == null) {
                context.setVariable("errorMessage", "Le compte créditeur n'existe pas");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("virement", context, response.getWriter());
                return;
            }
            
            // Appeler le service distant pour effectuer le virement
            OperationServiceRemote operationService = EJBClientFactory.getOperationService();
            String libelleVirement = libelle != null && !libelle.trim().isEmpty() ? libelle : "Virement";
            
            VirementDTO virementEffectue = operationService.effectuerVirement(
                numeroCompteDebiteur, 
                numeroCompteCrediteur, 
                montant, 
                libelleVirement
            );
            
            LOGGER.info("Virement effectué avec succès: " + virementEffectue.getIdVirement());
            
            // Rediriger vers le dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard?success=virement");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du virement", e);
            
            try {
                // Récupérer à nouveau les comptes pour l'affichage
                CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
                List<CompteCourantDTO> comptes = compteService.listerComptesParClient(client.getIdClient());
                
                IWebExchange webExchange = this.application.buildExchange(request, response);
                WebContext context = new WebContext(webExchange);
                context.setVariable("pageTitle", "Virement - Banque Premium");
            context.setVariable("currentPage", "virement");
                context.setVariable("client", client);
                context.setVariable("comptes", comptes);
                context.setVariable("numeroCompteDebiteur", numeroCompteDebiteur);
                context.setVariable("numeroCompteCrediteur", numeroCompteCrediteur);
                context.setVariable("montant", montantStr);
                context.setVariable("libelle", libelle);
                context.setVariable("errorMessage", "Erreur lors du virement: " + e.getMessage());
                
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("virement", context, response.getWriter());
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Erreur fatale", ex);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Une erreur inattendue s'est produite");
            }
        }
    }
}