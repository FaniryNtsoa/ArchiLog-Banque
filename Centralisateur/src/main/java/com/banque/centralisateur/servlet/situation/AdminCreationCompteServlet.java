package com.banque.centralisateur.servlet.situation;

import com.banque.centralisateur.config.ThymeleafConfig;
import com.banque.centralisateur.ejb.EJBClientFactory;
import com.banque.situationbancaire.dto.ClientDTO;
import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.dto.ActionRoleDTO;
import com.banque.situationbancaire.dto.UtilisateurDTO;
import com.banque.situationbancaire.ejb.remote.ClientServiceRemote;
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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet pour la crÃ©ation de comptes courants - Administration
 * GÃ¨re la crÃ©ation de comptes avec vÃ©rification des autorisations ActionRole
 */
@WebServlet(name = "AdminCreationCompteServlet", urlPatterns = {"/admin/situation/nouveau-compte"})
public class AdminCreationCompteServlet extends HttpServlet {
    
    private static final Logger LOGGER = Logger.getLogger(AdminCreationCompteServlet.class.getName());
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
        
        // VÃ©rifier les autorisations ActionRole
        if (!verifierAutorisation(session, "compte_courant", "CREATE")) {
            session.setAttribute("errorMessage", "Vous n'avez pas l'autorisation de crÃ©er des comptes");
            response.sendRedirect(request.getContextPath() + "/dashboard");
            return;
        }
        
        LOGGER.info("Affichage du formulaire de crÃ©ation de compte - Interface Admin");
        
        IWebExchange webExchange = this.application.buildExchange(request, response);
        WebContext context = new WebContext(webExchange);
        
        try {
            // RÃ©cupÃ©rer la liste des clients
            ClientServiceRemote clientService = EJBClientFactory.getClientService();
            List<ClientDTO> clients = clientService.listerTousLesClients();
            
            // RÃ©cupÃ©rer les types de comptes disponibles
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            List<String> typesComptes = compteService.listerTypesComptesDisponibles();
            
            // Ajouter les variables au contexte
            context.setVariable("pageTitle", "Nouveau Compte Courant - Administration");
            context.setVariable("currentPage", "admin-nouveau-compte");
            context.setVariable("moduleName", "Situation Bancaire");
            context.setVariable("clients", clients);
            context.setVariable("typesComptes", typesComptes);
            context.setVariable("hasClients", !clients.isEmpty());
            context.setVariable("hasTypesComptes", !typesComptes.isEmpty());
            
            // Messages de session
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                context.setVariable("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
            }
            
            String successMessage = (String) session.getAttribute("successMessage");
            if (successMessage != null) {
                context.setVariable("successMessage", successMessage);
                session.removeAttribute("successMessage");
            }
            
            // Rendre le template
            response.setContentType("text/html;charset=UTF-8");
            templateEngine.process("situation/admin-nouveau-compte", context, response.getWriter());
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du chargement du formulaire de crÃ©ation de compte", e);
            session.setAttribute("errorMessage", "Impossible de charger le formulaire. Veuillez rÃ©essayer.");
            response.sendRedirect(request.getContextPath() + "/dashboard");
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
        
        // VÃ©rifier les autorisations ActionRole
        if (!verifierAutorisation(session, "compte_courant", "CREATE")) {
            session.setAttribute("errorMessage", "Vous n'avez pas l'autorisation de crÃ©er des comptes");
            response.sendRedirect(request.getContextPath() + "/admin/situation/nouveau-compte");
            return;
        }
        
        LOGGER.info("Traitement de crÃ©ation de compte courant par l'admin");
        
        try {
            // RÃ©cupÃ©rer les paramÃ¨tres du formulaire
            String clientIdStr = request.getParameter("clientId");
            String typeCompteIdStr = request.getParameter("typeCompteId");
            String libelleCompte = request.getParameter("libelleCompte");
            String soldeInitialStr = request.getParameter("soldeInitial");
            
            // Validation
            if (clientIdStr == null || clientIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez sÃ©lectionner un client");
            }
            
            if (typeCompteIdStr == null || typeCompteIdStr.trim().isEmpty()) {
                throw new IllegalArgumentException("Veuillez sÃ©lectionner un type de compte");
            }
            
            Long clientId = Long.parseLong(clientIdStr);
            Long typeCompteId = Long.parseLong(typeCompteIdStr);
            BigDecimal soldeInitial = BigDecimal.ZERO;
            
            if (soldeInitialStr != null && !soldeInitialStr.trim().isEmpty()) {
                soldeInitial = new BigDecimal(soldeInitialStr);
                if (soldeInitial.compareTo(BigDecimal.ZERO) < 0) {
                    throw new IllegalArgumentException("Le solde initial ne peut pas Ãªtre nÃ©gatif");
                }
            }
            
            // RÃ©cupÃ©rer l'ID de l'utilisateur admin
            UtilisateurDTO utilisateur = (UtilisateurDTO) session.getAttribute("utilisateur");
            Long idAdministrateur = utilisateur != null ? utilisateur.getIdUtilisateur() : 1L;
            
            // CrÃ©er le DTO du compte
            CompteCourantDTO compteDTO = new CompteCourantDTO();
            compteDTO.setLibelleCompte(libelleCompte != null && !libelleCompte.trim().isEmpty() ? libelleCompte : "Compte courant");
            compteDTO.setSoldeInitial(soldeInitial);
            
            // CrÃ©er le compte via EJB avec traÃ§abilitÃ© admin
            CompteCourantServiceRemote compteService = EJBClientFactory.getCompteCourantService();
            CompteCourantDTO compteCree = compteService.creerCompteAdmin(compteDTO, clientId, typeCompteId, idAdministrateur);
            
            session.setAttribute("successMessage", 
                String.format("Compte nÂ°%s crÃ©Ã© avec succÃ¨s pour le solde initial de %s XOF", 
                    compteCree.getNumeroCompte(), compteCree.getSoldeInitial()));
            
            // Rediriger vers la liste des comptes
            response.sendRedirect(request.getContextPath() + "/admin/situation/comptes");
            
        } catch (NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Format de donnÃ©es invalide", e);
            session.setAttribute("errorMessage", "Format de donnÃ©es invalide. Veuillez vÃ©rifier vos saisies.");
            response.sendRedirect(request.getContextPath() + "/admin/situation/nouveau-compte");
            
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Validation Ã©chouÃ©e: " + e.getMessage(), e);
            session.setAttribute("errorMessage", e.getMessage());
            response.sendRedirect(request.getContextPath() + "/admin/situation/nouveau-compte");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la crÃ©ation du compte", e);
            session.setAttribute("errorMessage", "Une erreur est survenue lors de la crÃ©ation. Veuillez rÃ©essayer.");
            response.sendRedirect(request.getContextPath() + "/admin/situation/nouveau-compte");
        }
    }
    
    /**
     * VÃ©rifie si l'utilisateur a l'autorisation pour effectuer une action sur une table
     */
    @SuppressWarnings("unchecked")
    private boolean verifierAutorisation(HttpSession session, String nomTable, String action) {
        try {
            UtilisateurDTO utilisateur = (UtilisateurDTO) session.getAttribute("utilisateur");
            List<ActionRoleDTO> autorisations = (List<ActionRoleDTO>) session.getAttribute("autorisations");
            
            if (utilisateur == null || autorisations == null) {
                LOGGER.warning("Utilisateur ou autorisations manquants en session");
                return false;
            }
            
            Integer roleUtilisateur = utilisateur.getRoleUtilisateur();
            
            // VÃ©rifier si l'utilisateur a l'autorisation pour cette action sur cette table
            return autorisations.stream()
                .anyMatch(auth -> nomTable.equals(auth.getNomTable()) && 
                                action.equals(auth.getActionAutorisee()) && 
                                roleUtilisateur.equals(auth.getRoleRequis()));
                                
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la vÃ©rification des autorisations", e);
            return false;
        }
    }
}
