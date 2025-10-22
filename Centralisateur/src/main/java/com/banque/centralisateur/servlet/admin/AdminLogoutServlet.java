package com.banque.centralisateur.servlet.admin;

import com.banque.centralisateur.ejb.EJBClientFactory;
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
 * Servlet de déconnexion admin
 */
@WebServlet("/admin/logout")
public class AdminLogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Récupérer le UserSessionBeanRemote et le détruire
            UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");
            if (userSessionBean != null) {
                try {
                    AuthenticationServiceRemote authService = EJBClientFactory.getAuthenticationService();
                    authService.logout((com.banque.situationbancaire.session.UserSessionBean) userSessionBean);
                } catch (Exception e) {
                    // Log mais continue la déconnexion
                    e.printStackTrace();
                }
            }
            
            // Invalider la session HTTP
            session.invalidate();
        }

        // Rediriger vers la page de connexion
        response.sendRedirect(request.getContextPath() + "/admin/login");
    }
}
