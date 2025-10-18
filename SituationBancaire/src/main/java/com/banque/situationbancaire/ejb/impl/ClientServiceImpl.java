package com.banque.situationbancaire.ejb.impl;

import com.banque.situationbancaire.dto.ClientDTO;
import com.banque.situationbancaire.dto.CompteCourantDTO;
import com.banque.situationbancaire.ejb.remote.ClientServiceRemote;
import com.banque.situationbancaire.entity.*;
import com.banque.situationbancaire.entity.enums.StatutClient;
import com.banque.situationbancaire.mapper.ClientMapper;
import com.banque.situationbancaire.mapper.CompteCourantMapper;
import com.banque.situationbancaire.repository.*;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Logger;

/**
 * Implémentation complète du service client avec création automatique de compte
 */
@Stateless
public class ClientServiceImpl implements ClientServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(ClientServiceImpl.class.getName());

    @Inject
    private ClientRepository clientRepository;

    @Override
    public ClientDTO creerClient(ClientDTO clientDTO) {
        LOGGER.info("Création d'un nouveau client : " + clientDTO.getEmail());
        
        // Vérifications métier
        if (existeParEmail(clientDTO.getEmail())) {
            throw new IllegalArgumentException("Un client avec cet email existe déjà");
        }
        
        if (existeParNumCin(clientDTO.getNumCin())) {
            throw new IllegalArgumentException("Un client avec ce numéro CIN existe déjà");
        }
        
        // Conversion DTO vers entité avec chiffrement du mot de passe
        Client client = ClientMapper.toEntity(clientDTO);
        client.setMotDePasse(hashPassword(clientDTO.getMotDePasse()));
        client.setStatut(StatutClient.ACTIF);
        client.setDateCreation(LocalDateTime.now());
        
        // Sauvegarder le client
        Client clientCree = clientRepository.save(client);
        
        // Note: Le compte courant sera créé manuellement via le dashboard
        LOGGER.info("Client créé avec succès. Compte courant à créer via le dashboard.");
        
        // Conversion entité vers DTO
        return ClientMapper.toDTO(clientCree);
    }

    @Override
    public ClientDTO rechercherClientParId(Long idClient) {
        LOGGER.info("Recherche du client par ID : " + idClient);
        Optional<Client> client = clientRepository.findById(idClient);
        return client.map(ClientMapper::toDTO).orElse(null);
    }

    @Override
    public ClientDTO rechercherClientParNumero(String numeroClient) {
        LOGGER.info("Recherche du client par numéro : " + numeroClient);
        Optional<Client> clientOptional = clientRepository.findByNumeroClient(numeroClient);
        return clientOptional.map(ClientMapper::toDTO).orElse(null);
    }

    @Override
    public ClientDTO rechercherClientParEmail(String email) {
        LOGGER.info("Recherche du client par email : " + email);
        Optional<Client> client = clientRepository.findByEmail(email);
        return client.map(ClientMapper::toDTO).orElse(null);
    }

    @Override
    public ClientDTO modifierClient(ClientDTO clientDTO) {
        LOGGER.info("Modification du client : " + clientDTO.getIdClient());
        
        Optional<Client> existantOpt = clientRepository.findById(clientDTO.getIdClient());
        if (existantOpt.isEmpty()) {
            throw new IllegalArgumentException("Client non trouvé");
        }
        
        Client existant = existantOpt.get();
        
        // Vérifier que l'email n'est pas utilisé par un autre client
        if (!existant.getEmail().equals(clientDTO.getEmail()) && existeParEmail(clientDTO.getEmail())) {
            throw new IllegalArgumentException("Un autre client utilise déjà cet email");
        }
        
        // Vérifier que le CIN n'est pas utilisé par un autre client
        if (!existant.getNumCin().equals(clientDTO.getNumCin()) && existeParNumCin(clientDTO.getNumCin())) {
            throw new IllegalArgumentException("Un autre client utilise déjà ce numéro CIN");
        }
        
        // Mettre à jour les propriétés de l'entité existante
        ClientMapper.updateEntity(existant, clientDTO);
        Client clientModifie = clientRepository.update(existant);
        
        return ClientMapper.toDTO(clientModifie);
    }

    @Override
    public List<ClientDTO> listerTousLesClients() {
        LOGGER.info("Récupération de tous les clients");
        List<Client> clients = clientRepository.findAll();
        return clients.stream()
                .map(ClientMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void supprimerClient(Long idClient) {
        LOGGER.info("Suppression du client : " + idClient);
        
        Optional<Client> clientOpt = clientRepository.findById(idClient);
        if (clientOpt.isEmpty()) {
            throw new IllegalArgumentException("Client non trouvé");
        }
        
        Client client = clientOpt.get();
        
        // Vérifier qu'il n'y a pas de comptes actifs
        if (!client.getComptes().isEmpty()) {
            boolean hasActiveAccount = client.getComptes().stream()
                .anyMatch(compte -> compte.getStatut().name().equals("OUVERT"));
            
            if (hasActiveAccount) {
                throw new IllegalStateException("Impossible de supprimer un client avec des comptes actifs");
            }
        }
        
        clientRepository.deleteById(idClient);
    }

    @Override
    public boolean existeParEmail(String email) {
        Optional<Client> client = clientRepository.findByEmail(email);
        return client.isPresent();
    }

    @Override
    public boolean existeParNumCin(String numCin) {
        Optional<Client> client = clientRepository.findByNumCin(numCin);
        return client.isPresent();
    }

    @Override 
    public ClientDTO authentifierClient(String email, String motDePasse) {
        LOGGER.info("Tentative d'authentification pour : " + email);
        
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        if (clientOpt.isEmpty()) {
            return null; // Client non trouvé
        }
        
        Client client = clientOpt.get();
        
        // Vérifier le mot de passe
        if (!verifyPassword(motDePasse, client.getMotDePasse())) {
            return null; // Mot de passe incorrect
        }
        
        // Vérifier que le client est actif
        if (client.getStatut() != StatutClient.ACTIF) {
            throw new IllegalStateException("Compte client non actif");
        }
        
        return ClientMapper.toDTO(client);
    }



    /**
     * Hash le mot de passe (version simplifiée pour test)
     */
    private String hashPassword(String motDePasse) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(motDePasse.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }

    /**
     * Vérifie le mot de passe
     */
    private boolean verifyPassword(String motDePasseClair, String motDePasseHash) {
        return hashPassword(motDePasseClair).equals(motDePasseHash);
    }
}