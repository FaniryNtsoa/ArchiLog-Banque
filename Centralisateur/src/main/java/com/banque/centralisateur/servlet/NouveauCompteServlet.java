package com.banque.centralisateur.servlet;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.ClientDTO;
import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.dto.TypeCompteDTO;
import com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote;
import com.banque.situationbancaire.ejb.remote.TypeCompteServiceRemote;
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
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet pour créer un nouveau compte
 */
@WebServlet(name = "NouveauCompteServlet", urlPatterns = {"/nouveau-compte"})
public class NouveauCompteServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(NouveauCompteServlet.class.getName());
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
        
        try {
            // Récupérer la liste des types de compte disponibles
            TypeCompteServiceRemote typeCompteService = EJBClientFactory.getTypeCompteService();
            java.util.List<TypeCompteDTO> typesCompte = typeCompteService.listerTousLesTypesCompte();
            
            // Créer le contexte Thymeleaf
            IWebExchange webExchange = this.application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange);
            context.setVariable("pageTitle", "Nouveau Compte - Banque Premium");
            context.setVariable("currentPage", "nouveau-compte");
            context.setVariable("client", client);
            context.setVariable("typesCompte", typesCompte);
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("nouveau-compte", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des types de compte", e);
            
            // Créer le contexte avec un message d'erreur
            IWebExchange webExchange = this.application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange);
            context.setVariable("pageTitle", "Nouveau Compte - Banque Premium");
            context.setVariable("currentPage", "nouveau-compte");
            context.setVariable("client", client);
            context.setVariable("errorMessage", "Erreur lors du chargement des types de compte");
            context.setVariable("typesCompte", new java.util.ArrayList<>());
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("nouveau-compte", context, response.getWriter());
        }
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
        LOGGER.info("Création d'un nouveau compte pour: " + client.getEmail());
        
        // Récupérer les données du formulaire
        String libelle = request.getParameter("libelle");
        String idTypeCompteStr = request.getParameter("idTypeCompte");
        
        try {
            // Récupérer la liste des types de compte pour l'affichage en cas d'erreur
            TypeCompteServiceRemote typeCompteService = EJBClientFactory.getTypeCompteService();
            java.util.List<TypeCompteDTO> typesCompte = typeCompteService.listerTousLesTypesCompte();
            
            // Créer le contexte Thymeleaf
            IWebExchange webExchange = this.application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange);
            context.setVariable("pageTitle", "Nouveau Compte - Banque Premium");
            context.setVariable("currentPage", "nouveau-compte");
            context.setVariable("client", client);
            context.setVariable("typesCompte", typesCompte);
            context.setVariable("libelle", libelle);
            context.setVariable("selectedTypeCompte", idTypeCompteStr);
            
            // Validation
            if (libelle == null || libelle.trim().isEmpty()) {
                context.setVariable("errorMessage", "Le libellé du compte est obligatoire");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("nouveau-compte", context, response.getWriter());
                return;
            }
            
            if (idTypeCompteStr == null || idTypeCompteStr.trim().isEmpty()) {
                context.setVariable("errorMessage", "Veuillez sélectionner un type de compte");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("nouveau-compte", context, response.getWriter());
                return;
            }
            
            // Récupérer le type de compte sélectionné
            Long idTypeCompte;
            try {
                idTypeCompte = Long.parseLong(idTypeCompteStr);
            } catch (NumberFormatException e) {
                context.setVariable("errorMessage", "Type de compte invalide");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("nouveau-compte", context, response.getWriter());
                return;
            }
            
            TypeCompteDTO typeCompte = typeCompteService.rechercherTypeCompteParId(idTypeCompte);
            if (typeCompte == null) {
                context.setVariable("errorMessage", "Type de compte non trouvé");
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("nouveau-compte", context, response.getWriter());
                return;
            }
            
            // Créer le DTO Compte avec les paramètres du type sélectionné
            CompteCourantDTO compteDTO = new CompteCourantDTO();
            compteDTO.setLibelleCompte(libelle);
            compteDTO.setSoldeInitial(BigDecimal.ZERO); // Solde initial à 0
            compteDTO.setDecouvertAutorise(typeCompte.getMontantDecouvertAutorise());
            
            // Appeler le service distant pour créer le compte avec l'ID du type de compte
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            CompteCourantDTO compteCree = compteService.creerCompte(compteDTO, client.getIdClient(), idTypeCompte);
            
            LOGGER.info("Compte créé avec succès: " + compteCree.getNumeroCompte());
            
            // Rediriger vers le dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard?success=compte_cree");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création du compte", e);
            
            try {
                // Récupérer à nouveau les types de compte pour l'affichage
                TypeCompteServiceRemote typeCompteService = EJBClientFactory.getTypeCompteService();
                java.util.List<TypeCompteDTO> typesCompte = typeCompteService.listerTousLesTypesCompte();
                
                IWebExchange webExchange = this.application.buildExchange(request, response);
                WebContext context = new WebContext(webExchange);
                context.setVariable("pageTitle", "Nouveau Compte - Banque Premium");
                context.setVariable("currentPage", "nouveau-compte");
                context.setVariable("client", client);
                context.setVariable("typesCompte", typesCompte);
                context.setVariable("libelle", libelle);
                context.setVariable("selectedTypeCompte", idTypeCompteStr);
                context.setVariable("errorMessage", "Erreur lors de la création du compte: " + e.getMessage());
                
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("nouveau-compte", context, response.getWriter());
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Erreur fatale", ex);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Une erreur inattendue s'est produite");
            }
        }
    }
}