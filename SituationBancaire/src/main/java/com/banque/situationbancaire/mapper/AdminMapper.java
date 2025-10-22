package com.banque.situationbancaire.mapper;

import com.banque.situationbancaire.dto.DirectionDTO;
import com.banque.situationbancaire.dto.UtilisateurDTO;
import com.banque.situationbancaire.dto.ActionRoleDTO;
import com.banque.situationbancaire.entity.Direction;
import com.banque.situationbancaire.entity.Utilisateur;
import com.banque.situationbancaire.entity.ActionRole;

import jakarta.ejb.Stateless;

/**
 * Mapper pour les entités admin
 */
@Stateless
public class AdminMapper {

    public DirectionDTO toDTO(Direction direction) {
        if (direction == null) return null;
        
        return DirectionDTO.builder()
                .idDirection(direction.getIdDirection())
                .niveau(direction.getNiveau())
                .build();
    }

    public Direction toEntity(DirectionDTO dto) {
        if (dto == null) return null;
        
        return Direction.builder()
                .idDirection(dto.getIdDirection())
                .niveau(dto.getNiveau())
                .build();
    }

    public UtilisateurDTO toDTO(Utilisateur utilisateur) {
        if (utilisateur == null) return null;
        
        return UtilisateurDTO.builder()
                .idUtilisateur(utilisateur.getIdUtilisateur())
                .loginUtilisateur(utilisateur.getLoginUtilisateur())
                .roleUtilisateur(utilisateur.getRoleUtilisateur())
                .idDirection(utilisateur.getDirection() != null ? 
                    utilisateur.getDirection().getIdDirection() : null)
                .niveauDirection(utilisateur.getDirection() != null ? 
                    utilisateur.getDirection().getNiveau() : null)
                .build();
    }

    public Utilisateur toEntity(UtilisateurDTO dto) {
        if (dto == null) return null;
        
        Utilisateur utilisateur = Utilisateur.builder()
                .idUtilisateur(dto.getIdUtilisateur())
                .loginUtilisateur(dto.getLoginUtilisateur())
                .motDePasse(dto.getMotDePasse())
                .roleUtilisateur(dto.getRoleUtilisateur())
                .build();
        
        if (dto.getIdDirection() != null) {
            Direction direction = Direction.builder()
                    .idDirection(dto.getIdDirection())
                    .niveau(dto.getNiveauDirection())
                    .build();
            utilisateur.setDirection(direction);
        }
        
        return utilisateur;
    }

    public ActionRoleDTO toDTO(ActionRole actionRole) {
        if (actionRole == null) return null;
        
        return ActionRoleDTO.builder()
                .idActionRole(actionRole.getIdActionRole())
                .nomTable(actionRole.getNomTable())
                .actionAutorisee(actionRole.getActionAutorisee())
                .roleRequis(actionRole.getRoleRequis())
                .build();
    }

    public ActionRole toEntity(ActionRoleDTO dto) {
        if (dto == null) return null;
        
        return ActionRole.builder()
                .idActionRole(dto.getIdActionRole())
                .nomTable(dto.getNomTable())
                .actionAutorisee(dto.getActionAutorisee())
                .roleRequis(dto.getRoleRequis())
                .build();
    }
}
