package com.banque.centralisateur.servlet;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.ClientDTO;
import com.banque.situationbancaire.ejb.remote.ClientServiceRemote;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Servlet pour gérer l'inscription des nouveaux clients
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(RegisterServlet.class.getName());
    private TemplateEngine templateEngine;
    private JakartaServletWebApplication application;

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialiser Thymeleaf
        this.application = JakartaServletWebApplication.buildApplication(getServletContext());
        this.templateEngine = ThymeleafConfig.getTemplateEngine(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("Affichage de la page d'inscription");
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        // Ajouter les variables au contexte
        context.setVariable("pageTitle", "Inscription - Banque Premium");
            context.setVariable("currentPage", "register");
        
        // Rendre le template
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("register", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        LOGGER.info("Traitement de l'inscription d'un nouveau client");
        
        // Récupérer les données du formulaire
        String prenom = request.getParameter("prenom");
        String nom = request.getParameter("nom");
        String email = request.getParameter("email");
        String telephone = request.getParameter("telephone");
        String dateNaissanceStr = request.getParameter("dateNaissance");
        String numCin = request.getParameter("numCin");
        String adresse = request.getParameter("adresse");
        String codePostal = request.getParameter("codePostal");
        String ville = request.getParameter("ville");
        String profession = request.getParameter("profession");
        String revenuMensuelStr = request.getParameter("revenuMensuel");
        String situationFamiliale = request.getParameter("situationFamiliale");
        String motDePasse = request.getParameter("motDePasse");
        String confirmerMotDePasse = request.getParameter("confirmerMotDePasse");
        
        // Créer le contexte Thymeleaf
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        context.setVariable("pageTitle", "Inscription - Banque Premium");
            context.setVariable("currentPage", "register");
        
        try {
            // Validation des champs obligatoires
            if (prenom == null || prenom.trim().isEmpty() ||
                nom == null || nom.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                dateNaissanceStr == null || dateNaissanceStr.trim().isEmpty() ||
                numCin == null || numCin.trim().isEmpty() ||
                codePostal == null || codePostal.trim().isEmpty() ||
                ville == null || ville.trim().isEmpty() ||
                revenuMensuelStr == null || revenuMensuelStr.trim().isEmpty() ||
                motDePasse == null || motDePasse.trim().isEmpty() ||
                confirmerMotDePasse == null || confirmerMotDePasse.trim().isEmpty()) {
                
                context.setVariable("errorMessage", "Veuillez remplir tous les champs obligatoires (*)");
                context.setVariable("prenom", prenom);
                context.setVariable("nom", nom);
                context.setVariable("email", email);
                context.setVariable("telephone", telephone);
                context.setVariable("dateNaissance", dateNaissanceStr);
                context.setVariable("numCin", numCin);
                context.setVariable("adresse", adresse);
                context.setVariable("codePostal", codePostal);
                context.setVariable("ville", ville);
                context.setVariable("profession", profession);
                context.setVariable("revenuMensuel", revenuMensuelStr);
                context.setVariable("situationFamiliale", situationFamiliale);
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("register", context, response.getWriter());
                return;
            }
            
            // Vérifier que les mots de passe correspondent
            if (!motDePasse.equals(confirmerMotDePasse)) {
                context.setVariable("errorMessage", "Les mots de passe ne correspondent pas");
                context.setVariable("prenom", prenom);
                context.setVariable("nom", nom);
                context.setVariable("email", email);
                context.setVariable("telephone", telephone);
                context.setVariable("dateNaissance", dateNaissanceStr);
                context.setVariable("numCin", numCin);
                context.setVariable("adresse", adresse);
                context.setVariable("codePostal", codePostal);
                context.setVariable("ville", ville);
                context.setVariable("profession", profession);
                context.setVariable("revenuMensuel", revenuMensuelStr);
                context.setVariable("situationFamiliale", situationFamiliale);
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("register", context, response.getWriter());
                return;
            }
            
            // Créer le DTO Client
            ClientDTO clientDTO = new ClientDTO();
            clientDTO.setPrenom(prenom);
            clientDTO.setNom(nom);
            clientDTO.setEmail(email);
            clientDTO.setTelephone(telephone);
            clientDTO.setNumCin(numCin);
            clientDTO.setAdresse(adresse);
            clientDTO.setCodePostal(codePostal);
            clientDTO.setVille(ville);
            clientDTO.setMotDePasse(motDePasse);
            clientDTO.setProfession(profession);
            clientDTO.setSituationFamiliale(situationFamiliale);
            
            // Parser le revenu mensuel
            try {
                java.math.BigDecimal revenuMensuel = new java.math.BigDecimal(revenuMensuelStr);
                clientDTO.setRevenuMensuel(revenuMensuel);
            } catch (Exception e) {
                context.setVariable("errorMessage", "Format de revenu mensuel invalide");
                context.setVariable("prenom", prenom);
                context.setVariable("nom", nom);
                context.setVariable("email", email);
                context.setVariable("telephone", telephone);
                context.setVariable("dateNaissance", dateNaissanceStr);
                context.setVariable("numCin", numCin);
                context.setVariable("adresse", adresse);
                context.setVariable("codePostal", codePostal);
                context.setVariable("ville", ville);
                context.setVariable("profession", profession);
                context.setVariable("revenuMensuel", revenuMensuelStr);
                context.setVariable("situationFamiliale", situationFamiliale);
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("register", context, response.getWriter());
                return;
            }
            
            // Parser la date de naissance
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate dateNaissance = LocalDate.parse(dateNaissanceStr, formatter);
                clientDTO.setDateNaissance(dateNaissance);
            } catch (Exception e) {
                context.setVariable("errorMessage", "Format de date invalide");
                context.setVariable("prenom", prenom);
                context.setVariable("nom", nom);
                context.setVariable("email", email);
                context.setVariable("telephone", telephone);
                context.setVariable("dateNaissance", dateNaissanceStr);
                context.setVariable("numCin", numCin);
                context.setVariable("adresse", adresse);
                context.setVariable("codePostal", codePostal);
                context.setVariable("ville", ville);
                context.setVariable("profession", profession);
                context.setVariable("revenuMensuel", revenuMensuelStr);
                context.setVariable("situationFamiliale", situationFamiliale);
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("register", context, response.getWriter());
                return;
            }
            
            // Appeler le service distant pour créer le client
            ClientServiceRemote clientService = EJBClientFactory.getClientService();
            ClientDTO clientCree = clientService.creerClient(clientDTO);
            
            LOGGER.info("Client créé avec succès: " + clientCree.getNumeroClient());
            
            // Rediriger vers la page de connexion avec un message de succès
            response.sendRedirect(request.getContextPath() + "/login?success=inscription");
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Erreur de validation: " + e.getMessage(), e);
            context.setVariable("errorMessage", e.getMessage());
            context.setVariable("prenom", prenom);
            context.setVariable("nom", nom);
            context.setVariable("email", email);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("register", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création du client", e);
            context.setVariable("errorMessage", "Une erreur est survenue lors de l'inscription. Veuillez réessayer.");
            context.setVariable("prenom", prenom);
            context.setVariable("nom", nom);
            context.setVariable("email", email);
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("register", context, response.getWriter());
        }
    }
}
