package com.banque.situationbancaire.service;

import com.banque.situationbancaire.dto.LoginRequestDTO;
import com.banque.situationbancaire.dto.LoginResponseDTO;
import com.banque.situationbancaire.dto.UtilisateurDTO;
import com.banque.situationbancaire.dto.DirectionDTO;
import com.banque.situationbancaire.dto.ActionRoleDTO;
import com.banque.situationbancaire.entity.ActionRole;
import com.banque.situationbancaire.entity.Direction;
import com.banque.situationbancaire.entity.Utilisateur;
import com.banque.situationbancaire.repository.ActionRoleRepository;
import com.banque.situationbancaire.repository.DirectionRepository;
import com.banque.situationbancaire.repository.UtilisateurRepository;
import com.banque.situationbancaire.ejb.remote.UserSessionBeanRemote;
import com.banque.situationbancaire.ejb.remote.AuthenticationServiceRemote;

import java.util.stream.Collectors;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import java.util.List;
import java.util.Optional;

/**
 * Service d'authentification des utilisateurs
 */
@Stateless
public class AuthenticationService implements AuthenticationServiceRemote {

    @EJB
    private UtilisateurRepository utilisateurRepository;

    @EJB
    private ActionRoleRepository actionRoleRepository;

    @EJB
    private DirectionRepository directionRepository;

    /**
     * Authentifie un utilisateur et crée une session
     * @param loginRequest Requête de connexion
     * @param userSession Session utilisateur (Stateful bean)
     * @return Réponse de connexion
     */
    public LoginResponseDTO authenticate(LoginRequestDTO loginRequest, UserSessionBeanRemote userSession) {
        try {
            // Vérifier les identifiants
            Optional<Utilisateur> utilisateurOpt = utilisateurRepository.findByLogin(loginRequest.getLogin());
            
            if (utilisateurOpt.isEmpty()) {
                return LoginResponseDTO.builder()
                        .success(false)
                        .message("Login ou mot de passe incorrect")
                        .build();
            }

            Utilisateur utilisateur = utilisateurOpt.get();

            // Vérifier le mot de passe (en production, utiliser BCrypt ou autre)
            if (!utilisateur.getMotDePasse().equals(loginRequest.getMotDePasse())) {
                return LoginResponseDTO.builder()
                        .success(false)
                        .message("Login ou mot de passe incorrect")
                        .build();
            }

            // Récupérer toutes les directions
            List<Direction> directions = directionRepository.findAll();

            // Récupérer les actions autorisées pour le rôle de l'utilisateur
            List<ActionRole> actionsAutorisees = actionRoleRepository.findByRole(utilisateur.getRoleUtilisateur());

            // Convertir les entités en DTOs pour l'interface Remote
            UtilisateurDTO utilisateurDTO = UtilisateurDTO.builder()
                    .idUtilisateur(utilisateur.getIdUtilisateur())
                    .loginUtilisateur(utilisateur.getLoginUtilisateur())
                    .roleUtilisateur(utilisateur.getRoleUtilisateur())
                    .idDirection(utilisateur.getDirection() != null ? utilisateur.getDirection().getIdDirection() : null)
                    .niveauDirection(utilisateur.getDirection() != null ? utilisateur.getDirection().getNiveau() : null)
                    .build();

            List<DirectionDTO> directionsDTO = directions.stream()
                    .map(dir -> DirectionDTO.builder()
                            .idDirection(dir.getIdDirection())
                            .niveau(dir.getNiveau())
                            .build())
                    .collect(Collectors.toList());

            List<ActionRoleDTO> actionsDTO = actionsAutorisees.stream()
                    .map(action -> ActionRoleDTO.builder()
                            .idActionRole(action.getIdActionRole())
                            .nomTable(action.getNomTable())
                            .actionAutorisee(action.getActionAutorisee())
                            .roleRequis(action.getRoleRequis())
                            .build())
                    .collect(Collectors.toList());

            // Initialiser la session utilisateur (Stateful bean) avec les DTOs
            userSession.initSession(utilisateurDTO, directionsDTO, actionsDTO);

            return LoginResponseDTO.builder()
                    .success(true)
                    .message("Connexion réussie")
                    .utilisateur(utilisateurDTO)
                    .sessionId(userSession.getSessionId())
                    .build();

        } catch (Exception e) {
            return LoginResponseDTO.builder()
                    .success(false)
                    .message("Erreur lors de la connexion : " + e.getMessage())
                    .build();
        }
    }

    /**
     * Déconnecte un utilisateur et détruit sa session
     */
    public void logout(UserSessionBeanRemote userSession) {
        if (userSession != null) {
            userSession.destroy();
        }
    }
}
