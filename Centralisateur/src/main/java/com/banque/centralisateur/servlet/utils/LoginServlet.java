package com.banque.centralisateur.servlet.utils;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.LoginRequestDTO;
import com.banque.situationbancaire.dto.LoginResponseDTO;
import com.banque.situationbancaire.ejb.remote.AuthenticationServiceRemote;
import com.banque.situationbancaire.ejb.remote.UserSessionBeanRemote;

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

/**
 * Servlet centralisé pour la gestion de l'authentification administrateur
 * URL de base : /login
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

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
        
        // Utiliser Thymeleaf pour rendre le template de login
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        context.setVariable("pageTitle", "Connexion - Administration Bancaire");
        
        response.setContentType("text/html;charset=UTF-8");
        templateEngine.process("utils/login", context, response.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String login = request.getParameter("username");
        String motDePasse = request.getParameter("password");

        LoginRequestDTO loginRequest = LoginRequestDTO.builder()
                .login(login)
                .motDePasse(motDePasse)
                .build();

        try {
            // Créer une nouvelle session EJB Stateful distante
            UserSessionBeanRemote userSessionBean = EJBClientFactory.createUserSession();
            
            // Obtenir le service d'authentification distant
            AuthenticationServiceRemote authService = EJBClientFactory.getAuthenticationService();
            
            // Authentification et initialisation de la session EJB Stateful
            LoginResponseDTO loginResponse = authService.authenticate(loginRequest, userSessionBean);

            if (loginResponse.isSuccess()) {
                // Stocker le UserSessionBeanRemote dans la session HTTP
                HttpSession httpSession = request.getSession(true);
                httpSession.setAttribute("userSessionBean", userSessionBean);
                httpSession.setAttribute("sessionId", loginResponse.getSessionId());
                
                // Récupérer et stocker l'utilisateur et les autorisations
                httpSession.setAttribute("utilisateur", loginResponse.getUtilisateur());
                httpSession.setAttribute("utilisateurLogin", loginResponse.getUtilisateur().getLoginUtilisateur());
                httpSession.setAttribute("autorisations", userSessionBean.getActionsAutorisees());

                // Rediriger vers le dashboard admin
                response.sendRedirect(request.getContextPath() + "/dashboard");
            } else {
                // Erreur de connexion - utiliser Thymeleaf
                IWebExchange webExchange = this.application.buildExchange(request, response);
                WebContext context = new WebContext(webExchange);
                context.setVariable("pageTitle", "Connexion - Administration Bancaire");
                context.setVariable("errorMessage", loginResponse.getMessage());
                
                response.setContentType("text/html;charset=UTF-8");
                templateEngine.process("utils/login", context, response.getWriter());
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Erreur technique - utiliser Thymeleaf
            IWebExchange webExchange = this.application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange);
            context.setVariable("pageTitle", "Connexion - Administration Bancaire");
            context.setVariable("errorMessage", "Erreur lors de la connexion : " + e.getMessage());
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("utils/login", context, response.getWriter());
        }
    }
}
