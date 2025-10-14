package com.banque.situationbancaire.ejb.impl;

import com.banque.situationbancaire.ejb.remote.ClientServiceRemote;
import com.banque.situationbancaire.entity.Client;
import com.banque.situationbancaire.repository.ClientRepository;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Implémentation du service de gestion des clients
 */
@Stateless
@Transactional
public class ClientServiceImpl implements ClientServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(ClientServiceImpl.class.getName());

    @EJB
    private ClientRepository clientRepository;

    @Override
    public Client creerClient(Client client) {
        LOGGER.info("Création d'un nouveau client : " + client.getEmail());
        
        // Vérifications métier
        if (existeParEmail(client.getEmail())) {
            throw new IllegalArgumentException("Un client avec cet email existe déjà");
        }
        
        if (existeParNumCin(client.getNumCin())) {
            throw new IllegalArgumentException("Un client avec ce numéro CIN existe déjà");
        }
        
        return clientRepository.save(client);
    }

    @Override
    public Client rechercherClientParId(Long idClient) {
        LOGGER.info("Recherche du client par ID : " + idClient);
        Optional<Client> client = clientRepository.findById(idClient);
        return client.orElse(null);
    }

    @Override
    public Client rechercherClientParNumero(String numeroClient) {
        LOGGER.info("Recherche du client par numéro : " + numeroClient);
        Optional<Client> client = clientRepository.findByNumeroClient(numeroClient);
        return client.orElse(null);
    }

    @Override
    public Client rechercherClientParEmail(String email) {
        LOGGER.info("Recherche du client par email : " + email);
        Optional<Client> client = clientRepository.findByEmail(email);
        return client.orElse(null);
    }

    @Override
    public Client modifierClient(Client client) {
        LOGGER.info("Modification du client : " + client.getIdClient());
        
        Optional<Client> existantOpt = clientRepository.findById(client.getIdClient());
        if (existantOpt.isEmpty()) {
            throw new IllegalArgumentException("Client non trouvé");
        }
        
        Client existant = existantOpt.get();
        
        // Vérifier que l'email n'est pas utilisé par un autre client
        if (!existant.getEmail().equals(client.getEmail()) && existeParEmail(client.getEmail())) {
            throw new IllegalArgumentException("Un autre client utilise déjà cet email");
        }
        
        // Vérifier que le CIN n'est pas utilisé par un autre client
        if (!existant.getNumCin().equals(client.getNumCin()) && existeParNumCin(client.getNumCin())) {
            throw new IllegalArgumentException("Un autre client utilise déjà ce numéro CIN");
        }
        
        return clientRepository.update(client);
    }

    @Override
    public List<Client> listerTousLesClients() {
        LOGGER.info("Récupération de tous les clients");
        return clientRepository.findAll();
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
}