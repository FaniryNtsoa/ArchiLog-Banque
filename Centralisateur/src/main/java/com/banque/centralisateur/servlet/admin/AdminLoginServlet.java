package com.banque.centralisateur.servlet.admin;

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
import java.io.IOException;

/**
 * Servlet de connexion admin
 */
@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/templates/admin/login.html").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String login = request.getParameter("login");
        String motDePasse = request.getParameter("motDePasse");

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
            LoginResponseDTO loginResponse = authService.authenticate(loginRequest, (com.banque.situationbancaire.session.UserSessionBean) userSessionBean);

            if (loginResponse.isSuccess()) {
                // Stocker le UserSessionBeanRemote dans la session HTTP
                HttpSession httpSession = request.getSession(true);
                httpSession.setAttribute("userSessionBean", userSessionBean);
                httpSession.setAttribute("sessionId", loginResponse.getSessionId());
                httpSession.setAttribute("utilisateur", loginResponse.getUtilisateur());

                // Rediriger vers le dashboard admin
                response.sendRedirect(request.getContextPath() + "/admin/dashboard");
            } else {
                // Erreur de connexion
                request.setAttribute("error", loginResponse.getMessage());
                request.getRequestDispatcher("/templates/admin/login.html").forward(request, response);
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur lors de la connexion : " + e.getMessage());
            request.getRequestDispatcher("/templates/admin/login.html").forward(request, response);
        }
    }
}
