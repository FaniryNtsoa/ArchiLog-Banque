package com.banque.centralisateur.servlet.test;

import com.banque.centralisateur.config.ThymeleafConfig;
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

/**
 * Servlet de test pour vérifier Thymeleaf
 */
@WebServlet(name = "TestThymeleafServlet", urlPatterns = {"/test-login"})
public class TestThymeleafServlet extends HttpServlet {

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
        
        try {
            IWebExchange webExchange = this.application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange);
            
            context.setVariable("pageTitle", "Test - Connexion Administration Bancaire");
            context.setVariable("errorMessage", "Test: Si vous voyez ce message, Thymeleaf fonctionne !");
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("utils/login", context, response.getWriter());
            
        } catch (Exception e) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println("<!DOCTYPE html>");
            response.getWriter().println("<html><head><title>Erreur Thymeleaf</title></head><body>");
            response.getWriter().println("<h1>Erreur lors du rendu Thymeleaf:</h1>");
            response.getWriter().println("<pre>" + e.getMessage() + "</pre>");
            response.getWriter().println("<h2>Stack trace:</h2>");
            response.getWriter().println("<pre>");
            e.printStackTrace(response.getWriter());
            response.getWriter().println("</pre>");
            response.getWriter().println("</body></html>");
        }
    }
}