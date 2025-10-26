package com.banque.centralisateur.servlet.situation;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.MouvementDTO;
import com.banque.situationbancaire.ejb.remote.MouvementAdminServiceRemote;
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
import java.math.BigDecimal;
import java.util.List;

/**
 * Servlet CRUD pour les mouvements (avec vÃ©rification des permissions)
 */
@WebServlet("/admin/mouvements")
public class AdminMouvementServlet extends HttpServlet {

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

        UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");

        try {
            // Obtenir le service distant
            MouvementAdminServiceRemote mouvementService = EJBClientFactory.getMouvementAdminService();
            
            // Récupérer tous les mouvements (sans cast d'entité)
            List<MouvementDTO> mouvements = mouvementService.findAll(userSessionBean);
            
            // Utiliser Thymeleaf pour rendre la page
            IWebExchange webExchange = this.application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange);
            
            context.setVariable("pageTitle", "Gestion des Mouvements");
            context.setVariable("mouvements", mouvements);
            context.setVariable("canInsert", userSessionBean.hasPermission("mouvement", "INSERT"));
            context.setVariable("canUpdate", userSessionBean.hasPermission("mouvement", "UPDATE"));
            context.setVariable("canDelete", userSessionBean.hasPermission("mouvement", "DELETE"));
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("situation/admin-mouvements", context, response.getWriter());
            
        } catch (SecurityException e) {
            // Erreur de sécurité - rediriger vers dashboard avec message
            IWebExchange webExchange = this.application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange);
            context.setVariable("pageTitle", "Dashboard - Administration Bancaire");
            context.setVariable("errorMessage", e.getMessage());
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("utils/dashboard", context, response.getWriter());
            
        } catch (Exception e) {
            // Erreur générale
            IWebExchange webExchange = this.application.buildExchange(request, response);
            WebContext context = new WebContext(webExchange);
            context.setVariable("pageTitle", "Dashboard - Administration Bancaire");
            context.setVariable("errorMessage", "Erreur : " + e.getMessage());
            
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("utils/dashboard", context, response.getWriter());
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

        UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");
        String action = request.getParameter("action");

        try {
            MouvementAdminServiceRemote mouvementService = EJBClientFactory.getMouvementAdminService();
            
            if ("create".equals(action)) {
                createMouvement(request, userSessionBean, mouvementService);
                response.sendRedirect(request.getContextPath() + "/admin/mouvements?success=Mouvement crÃ©Ã©");
            } else if ("update".equals(action)) {
                updateMouvement(request, userSessionBean, mouvementService);
                response.sendRedirect(request.getContextPath() + "/admin/mouvements?success=Mouvement modifiÃ©");
            } else if ("delete".equals(action)) {
                deleteMouvement(request, userSessionBean, mouvementService);
                response.sendRedirect(request.getContextPath() + "/admin/mouvements?success=Mouvement supprimÃ©");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/mouvements");
            }
        } catch (SecurityException e) {
            response.sendRedirect(request.getContextPath() + "/admin/mouvements?error=" + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/admin/mouvements?error=" + e.getMessage());
        }
    }

    private void createMouvement(HttpServletRequest request, UserSessionBeanRemote userSessionBean, 
                                 MouvementAdminServiceRemote mouvementService) {
        MouvementDTO mouvementDTO = MouvementDTO.builder()
                .idCompte(Long.parseLong(request.getParameter("idCompte")))
                .idTypeOperation(Long.parseLong(request.getParameter("idTypeOperation")))
                .montant(new BigDecimal(request.getParameter("montant")))
                .soldeAvantOperation(new BigDecimal(request.getParameter("soldeAvant")))
                .soldeApresOperation(new BigDecimal(request.getParameter("soldeApres")))
                .reference(request.getParameter("reference"))
                .libelleOperation(request.getParameter("libelle"))
                .build();

        mouvementService.create(mouvementDTO, userSessionBean);
    }

    private void updateMouvement(HttpServletRequest request, UserSessionBeanRemote userSessionBean,
                                 MouvementAdminServiceRemote mouvementService) {
        MouvementDTO mouvementDTO = MouvementDTO.builder()
                .idMouvement(Long.parseLong(request.getParameter("idMouvement")))
                .montant(new BigDecimal(request.getParameter("montant")))
                .libelleOperation(request.getParameter("libelle"))
                .build();

        mouvementService.update(mouvementDTO, userSessionBean);
    }

    private void deleteMouvement(HttpServletRequest request, UserSessionBeanRemote userSessionBean,
                                 MouvementAdminServiceRemote mouvementService) {
        Long idMouvement = Long.parseLong(request.getParameter("idMouvement"));
        mouvementService.delete(idMouvement, userSessionBean);
    }
}

