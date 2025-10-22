package com.banque.centralisateur.servlet.admin;

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
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Servlet CRUD pour les mouvements (avec vérification des permissions)
 */
@WebServlet("/admin/mouvements")
public class AdminMouvementServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userSessionBean") == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");

        try {
            // Obtenir le service distant
            MouvementAdminServiceRemote mouvementService = EJBClientFactory.getMouvementAdminService();
            
            // Récupérer tous les mouvements (vérification permission SELECT)
            List<MouvementDTO> mouvements = mouvementService.findAll((com.banque.situationbancaire.session.UserSessionBean) userSessionBean);
            request.setAttribute("mouvements", mouvements);
            request.setAttribute("canInsert", userSessionBean.hasPermission("mouvement", "INSERT"));
            request.setAttribute("canUpdate", userSessionBean.hasPermission("mouvement", "UPDATE"));
            request.setAttribute("canDelete", userSessionBean.hasPermission("mouvement", "DELETE"));
            
            request.getRequestDispatcher("/templates/admin/mouvements.html").forward(request, response);
        } catch (SecurityException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/templates/admin/dashboard.html").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Erreur : " + e.getMessage());
            request.getRequestDispatcher("/templates/admin/dashboard.html").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userSessionBean") == null) {
            response.sendRedirect(request.getContextPath() + "/admin/login");
            return;
        }

        UserSessionBeanRemote userSessionBean = (UserSessionBeanRemote) session.getAttribute("userSessionBean");
        String action = request.getParameter("action");

        try {
            MouvementAdminServiceRemote mouvementService = EJBClientFactory.getMouvementAdminService();
            
            if ("create".equals(action)) {
                createMouvement(request, userSessionBean, mouvementService);
                response.sendRedirect(request.getContextPath() + "/admin/mouvements?success=Mouvement créé");
            } else if ("update".equals(action)) {
                updateMouvement(request, userSessionBean, mouvementService);
                response.sendRedirect(request.getContextPath() + "/admin/mouvements?success=Mouvement modifié");
            } else if ("delete".equals(action)) {
                deleteMouvement(request, userSessionBean, mouvementService);
                response.sendRedirect(request.getContextPath() + "/admin/mouvements?success=Mouvement supprimé");
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

        mouvementService.create(mouvementDTO, (com.banque.situationbancaire.session.UserSessionBean) userSessionBean);
    }

    private void updateMouvement(HttpServletRequest request, UserSessionBeanRemote userSessionBean,
                                 MouvementAdminServiceRemote mouvementService) {
        MouvementDTO mouvementDTO = MouvementDTO.builder()
                .idMouvement(Long.parseLong(request.getParameter("idMouvement")))
                .montant(new BigDecimal(request.getParameter("montant")))
                .libelleOperation(request.getParameter("libelle"))
                .build();

        mouvementService.update(mouvementDTO, (com.banque.situationbancaire.session.UserSessionBean) userSessionBean);
    }

    private void deleteMouvement(HttpServletRequest request, UserSessionBeanRemote userSessionBean,
                                 MouvementAdminServiceRemote mouvementService) {
        Long idMouvement = Long.parseLong(request.getParameter("idMouvement"));
        mouvementService.delete(idMouvement, (com.banque.situationbancaire.session.UserSessionBean) userSessionBean);
    }
}
