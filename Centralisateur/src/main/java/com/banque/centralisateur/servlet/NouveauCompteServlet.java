package com.banque.centralisateur.servlet;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.ClientDTO;
import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.ejb.remote.CompteCourantServiceRemote;
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
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Nouveau Compte - Banque Premium");
        context.setVariable("client", client);
        
        // Rendre le template
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("nouveau-compte", context, response.getWriter());
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
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Nouveau Compte - Banque Premium");
        context.setVariable("client", client);
        
        try {
            // Validation
            if (libelle == null || libelle.trim().isEmpty()) {
                context.setVariable("errorMessage", "Le libellé du compte est obligatoire");
                context.setVariable("libelle", libelle);
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("nouveau-compte", context, response.getWriter());
                return;
            }
            
            // Créer le DTO Compte avec valeurs par défaut
            CompteCourantDTO compteDTO = new CompteCourantDTO();
            compteDTO.setLibelleCompte(libelle);
            compteDTO.setSoldeInitial(BigDecimal.ZERO); // Solde initial à 0
            compteDTO.setDecouvertAutorise(new BigDecimal("300.00")); // 300€ de découvert par défaut
            
            // Appeler le service distant pour créer le compte
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            CompteCourantDTO compteCree = compteService.creerCompte(compteDTO, client.getIdClient());
            
            LOGGER.info("Compte créé avec succès: " + compteCree.getNumeroCompte());
            
            // Rediriger vers le dashboard
            response.sendRedirect(request.getContextPath() + "/dashboard?success=compte_cree");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création du compte", e);
            context.setVariable("errorMessage", "Erreur lors de la création du compte: " + e.getMessage());
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("nouveau-compte", context, response.getWriter());
        }
    }
}
