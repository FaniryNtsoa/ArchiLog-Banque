package com.banque.situationbancaire.repository;

import com.banque.situationbancaire.entity.Client;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des clients
 */
@Stateless
public class ClientRepository {

    @PersistenceContext(unitName = "SituationBancairePU")
    private EntityManager em;

    public Client save(Client client) {
        if (client.getIdClient() == null) {
            em.persist(client);
            return client;
        } else {
            return em.merge(client);
        }
    }

    public Optional<Client> findById(Long id) {
        return Optional.ofNullable(em.find(Client.class, id));
    }

    public Optional<Client> findByNumeroClient(String numeroClient) {
        TypedQuery<Client> query = em.createQuery(
            "SELECT c FROM Client c WHERE c.numeroClient = :numeroClient", Client.class);
        query.setParameter("numeroClient", numeroClient);
        return query.getResultList().stream().findFirst();
    }

    public Optional<Client> findByEmail(String email) {
        TypedQuery<Client> query = em.createQuery(
            "SELECT c FROM Client c WHERE c.email = :email", Client.class);
        query.setParameter("email", email);
        return query.getResultList().stream().findFirst();
    }

    public List<Client> findAll() {
        TypedQuery<Client> query = em.createQuery("SELECT c FROM Client c", Client.class);
        return query.getResultList();
    }

    public void delete(Client client) {
        if (em.contains(client)) {
            em.remove(client);
        } else {
            em.remove(em.merge(client));
        }
    }

    public boolean existsByNumeroClient(String numeroClient) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(c) FROM Client c WHERE c.numeroClient = :numeroClient", Long.class);
        query.setParameter("numeroClient", numeroClient);
        return query.getSingleResult() > 0;
    }
}
