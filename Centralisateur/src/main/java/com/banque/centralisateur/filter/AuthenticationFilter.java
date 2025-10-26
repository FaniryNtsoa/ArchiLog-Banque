package com.banque.centralisateur.filter;

import com.banque.situationbancaire.ejb.remote.UserSessionBeanRemote;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Filtre d'authentification pour protéger les pages d'administration
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/admin/*"})
public class AuthenticationFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        HttpSession session = httpRequest.getSession(false);
        
        // Vérifier si l'utilisateur est connecté
        if (session == null || session.getAttribute("userSessionBean") == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }
        
        // Vérifier si la session EJB est valide
        UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");
        try {
            if (!userSessionBean.isValid()) {
                session.invalidate();
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
                return;
            }
        } catch (Exception e) {
            // La session EJB a expiré ou n'est plus valide
            session.invalidate();
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            return;
        }
        
        // L'utilisateur est authentifié, continuer
        chain.doFilter(request, response);
    }
}