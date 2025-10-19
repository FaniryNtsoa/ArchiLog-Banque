package com.banque.situationbancaire.mapper;

import com.banque.situationbancaire.dto.ClientDTO;
import com.banque.situationbancaire.entity.Client;
import com.banque.situationbancaire.entity.enums.SituationFamiliale;
import com.banque.situationbancaire.entity.enums.StatutClient;

/**
 * Mapper pour convertir entre Client et ClientDTO
 */
public class ClientMapper {
    
    /**
     * Convertit une entité Client vers un ClientDTO
     */
    public static ClientDTO toDTO(Client client) {
        if (client == null) {
            return null;
        }
        
        ClientDTO dto = new ClientDTO();
        dto.setIdClient(client.getIdClient());
        dto.setNumeroClient(client.getNumeroClient());
        dto.setNom(client.getNom());
        dto.setPrenom(client.getPrenom());
        dto.setEmail(client.getEmail());
        dto.setTelephone(client.getTelephone());
        dto.setAdresse(client.getAdresse());
        dto.setCodePostal(client.getCodePostal());
        dto.setVille(client.getVille());
        dto.setDateNaissance(client.getDateNaissance());
        dto.setNumCin(client.getNumCin());
        dto.setSituationFamiliale(client.getSituationFamiliale() != null ? 
            client.getSituationFamiliale().name() : null);
        dto.setStatut(client.getStatut() != null ? 
            client.getStatut().name() : null);
        dto.setRevenuMensuel(client.getRevenuMensuel());
        dto.setProfession(client.getProfession());
        dto.setEntreprise(null); // Champ non présent dans l'entité
        dto.setDateCreation(client.getDateCreation());
        dto.setDateModification(client.getDateModification());
        
        return dto;
    }
    
    /**
     * Convertit un ClientDTO vers une entité Client
     */
    public static Client toEntity(ClientDTO dto) {
        if (dto == null) {
            return null;
        }
        
        Client client = new Client();
        client.setIdClient(dto.getIdClient());
        client.setNumeroClient(dto.getNumeroClient());
        client.setNom(dto.getNom());
        client.setPrenom(dto.getPrenom());
        client.setEmail(dto.getEmail());
        client.setTelephone(dto.getTelephone());
        client.setAdresse(dto.getAdresse());
        client.setCodePostal(dto.getCodePostal());
        client.setVille(dto.getVille());
        client.setDateNaissance(dto.getDateNaissance());
        client.setNumCin(dto.getNumCin());
        
        if (dto.getSituationFamiliale() != null) {
            try {
                client.setSituationFamiliale(SituationFamiliale.valueOf(dto.getSituationFamiliale()));
            } catch (IllegalArgumentException e) {
                // Log l'erreur et utiliser une valeur par défaut
                client.setSituationFamiliale(SituationFamiliale.CELIBATAIRE);
            }
        }
        
        if (dto.getStatut() != null) {
            try {
                client.setStatut(StatutClient.valueOf(dto.getStatut()));
            } catch (IllegalArgumentException e) {
                // Log l'erreur et utiliser une valeur par défaut
                client.setStatut(StatutClient.ACTIF);
            }
        }
        
        client.setRevenuMensuel(dto.getRevenuMensuel());
        client.setProfession(dto.getProfession());
        // Pas de champ entreprise dans l'entité
        client.setDateCreation(dto.getDateCreation());
        client.setDateModification(dto.getDateModification());
        
        return client;
    }
    
    /**
     * Met à jour une entité Client existante avec les données d'un ClientDTO
     */
    public static void updateEntity(Client client, ClientDTO dto) {
        if (client == null || dto == null) {
            return;
        }
        
        client.setNom(dto.getNom());
        client.setPrenom(dto.getPrenom());
        client.setEmail(dto.getEmail());
        client.setTelephone(dto.getTelephone());
        client.setAdresse(dto.getAdresse());
        client.setCodePostal(dto.getCodePostal());
        client.setVille(dto.getVille());
        client.setDateNaissance(dto.getDateNaissance());
        client.setNumCin(dto.getNumCin());
        
        if (dto.getSituationFamiliale() != null) {
            try {
                client.setSituationFamiliale(SituationFamiliale.valueOf(dto.getSituationFamiliale()));
            } catch (IllegalArgumentException e) {
                // Conserver la valeur existante si la nouvelle valeur est invalide
            }
        }
        
        if (dto.getStatut() != null) {
            try {
                client.setStatut(StatutClient.valueOf(dto.getStatut()));
            } catch (IllegalArgumentException e) {
                // Conserver la valeur existante si la nouvelle valeur est invalide
            }
        }
        
        client.setRevenuMensuel(dto.getRevenuMensuel());
        client.setProfession(dto.getProfession());
        // Pas de champ entreprise dans l'entité
    }
}