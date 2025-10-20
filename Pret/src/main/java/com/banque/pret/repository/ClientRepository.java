package com.banque.pret.repository;

import com.banque.pret.entity.Client;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Repository pour la gestion des clients
 */
@Stateless
public class ClientRepository {

    private static final Logger LOGGER = Logger.getLogger(ClientRepository.class.getName());

    @PersistenceContext(unitName = "PretPU")
    private EntityManager entityManager;

    /**
     * Sauvegarde un client
     */
    public Client save(Client client) {
        LOGGER.info("Sauvegarde du client : " + client.getEmail());
        entityManager.persist(client);
        entityManager.flush();
        return client;
    }

    /**
     * Met à jour un client
     */
    public Client update(Client client) {
        LOGGER.info("Mise à jour du client : " + client.getIdClient());
        return entityManager.merge(client);
    }

    /**
     * Recherche un client par son ID
     */
    public Optional<Client> findById(Long id) {
        LOGGER.info("Recherche du client par ID : " + id);
        Client client = entityManager.find(Client.class, id);
        return Optional.ofNullable(client);
    }

    /**
     * Recherche un client par son numéro
     */
    public Optional<Client> findByNumeroClient(String numeroClient) {
        LOGGER.info("Recherche du client par numéro : " + numeroClient);
        TypedQuery<Client> query = entityManager.createQuery(
            "SELECT c FROM Client c WHERE c.numeroClient = :numeroClient", Client.class);
        query.setParameter("numeroClient", numeroClient);
        
        List<Client> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Recherche un client par son email
     */
    public Optional<Client> findByEmail(String email) {
        LOGGER.info("Recherche du client par email : " + email);
        TypedQuery<Client> query = entityManager.createQuery(
            "SELECT c FROM Client c WHERE c.email = :email", Client.class);
        query.setParameter("email", email);
        
        List<Client> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Recherche un client par son numéro CIN
     */
    public Optional<Client> findByNumCin(String numCin) {
        LOGGER.info("Recherche du client par CIN : " + numCin);
        TypedQuery<Client> query = entityManager.createQuery(
            "SELECT c FROM Client c WHERE c.numCin = :numCin", Client.class);
        query.setParameter("numCin", numCin);
        
        List<Client> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Récupère tous les clients
     */
    public List<Client> findAll() {
        LOGGER.info("Récupération de tous les clients");
        TypedQuery<Client> query = entityManager.createQuery(
            "SELECT c FROM Client c ORDER BY c.dateCreation DESC", Client.class);
        return query.getResultList();
    }

    /**
     * Supprime un client
     */
    public void deleteById(Long id) {
        LOGGER.info("Suppression du client : " + id);
        Client client = entityManager.find(Client.class, id);
        if (client != null) {
            entityManager.remove(client);
        }
    }
}
