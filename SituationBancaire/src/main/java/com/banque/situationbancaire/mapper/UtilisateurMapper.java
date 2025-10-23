package com.banque.situationbancaire.mapper;

import com.banque.situationbancaire.dto.UtilisateurDTO;
import com.banque.situationbancaire.entity.Utilisateur;

public class UtilisateurMapper {

    public static UtilisateurDTO toDTO(Utilisateur utilisateur) {
        if (utilisateur == null) {
            return null;
        }

        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setIdUtilisateur(utilisateur.getIdUtilisateur());
        dto.setLoginUtilisateur(utilisateur.getLoginUtilisateur());
        dto.setRoleUtilisateur(utilisateur.getRoleUtilisateur());
        dto.setIdDirection(utilisateur.getDirection().getIdDirection());
        dto.setMotDePasse(utilisateur.getMotDePasse());
        // Mappez d'autres champs si nécessaire

        return dto;
    }

    public static Utilisateur toEntity(UtilisateurDTO dto) {
        if (dto == null) {
            return null;
        }

        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setIdUtilisateur(dto.getIdUtilisateur());
        utilisateur.setLoginUtilisateur(dto.getLoginUtilisateur());
        utilisateur.setRoleUtilisateur(dto.getRoleUtilisateur());
        // La gestion de la Direction doit être faite séparément
        utilisateur.setMotDePasse(dto.getMotDePasse());
        // Mappez d'autres champs si nécessaire

        return utilisateur;
    }
}
