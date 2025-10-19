package com.banque.pret.mapper;

import com.banque.pret.dto.ClientDTO;
import com.banque.pret.entity.Client;
import com.banque.pret.entity.enums.SituationFamiliale;
import com.banque.pret.entity.enums.StatutClient;

/**
 * Mapper pour convertir entre Client et ClientDTO
 */
public class ClientMapper {

    /**
     * Convertit une entité Client en ClientDTO
     */
    public static ClientDTO toDTO(Client client) {
        if (client == null) {
            return null;
        }

        return ClientDTO.builder()
                .idClient(client.getIdClient())
                .numeroClient(client.getNumeroClient())
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .dateNaissance(client.getDateNaissance())
                .numCin(client.getNumCin())
                .email(client.getEmail())
                .telephone(client.getTelephone())
                .adresse(client.getAdresse())
                .codePostal(client.getCodePostal())
                .ville(client.getVille())
                .profession(client.getProfession())
                .revenuMensuel(client.getRevenuMensuel())
                .soldeInitial(client.getSoldeInitial())
                .situationFamiliale(client.getSituationFamiliale() != null ? client.getSituationFamiliale().name() : null)
                .statut(client.getStatut() != null ? client.getStatut().name() : null)
                .dateCreation(client.getDateCreation())
                .dateModification(client.getDateModification())
                .build();
    }

    /**
     * Convertit un ClientDTO en entité Client
     */
    public static Client toEntity(ClientDTO dto) {
        if (dto == null) {
            return null;
        }

        return Client.builder()
                .idClient(dto.getIdClient())
                .numeroClient(dto.getNumeroClient())
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .dateNaissance(dto.getDateNaissance())
                .numCin(dto.getNumCin())
                .email(dto.getEmail())
                .telephone(dto.getTelephone())
                .adresse(dto.getAdresse())
                .codePostal(dto.getCodePostal())
                .ville(dto.getVille())
                .profession(dto.getProfession())
                .revenuMensuel(dto.getRevenuMensuel())
                .soldeInitial(dto.getSoldeInitial())
                .situationFamiliale(dto.getSituationFamiliale() != null ? SituationFamiliale.valueOf(dto.getSituationFamiliale()) : null)
                .motDePasse(dto.getMotDePasse())
                .statut(dto.getStatut() != null ? StatutClient.valueOf(dto.getStatut()) : null)
                .dateCreation(dto.getDateCreation())
                .dateModification(dto.getDateModification())
                .build();
    }

    /**
     * Met à jour une entité Client existante avec les données d'un DTO
     */
    public static void updateEntity(Client client, ClientDTO dto) {
        if (client == null || dto == null) {
            return;
        }

        client.setNom(dto.getNom());
        client.setPrenom(dto.getPrenom());
        client.setDateNaissance(dto.getDateNaissance());
        client.setNumCin(dto.getNumCin());
        client.setEmail(dto.getEmail());
        client.setTelephone(dto.getTelephone());
        client.setAdresse(dto.getAdresse());
        client.setCodePostal(dto.getCodePostal());
        client.setVille(dto.getVille());
        client.setProfession(dto.getProfession());
        client.setRevenuMensuel(dto.getRevenuMensuel());
        client.setSituationFamiliale(dto.getSituationFamiliale() != null ? SituationFamiliale.valueOf(dto.getSituationFamiliale()) : null);
        client.setStatut(dto.getStatut() != null ? StatutClient.valueOf(dto.getStatut()) : null);
    }
}
