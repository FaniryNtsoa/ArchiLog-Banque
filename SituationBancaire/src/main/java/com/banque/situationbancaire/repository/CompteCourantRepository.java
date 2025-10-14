package com.banque.situationbancaire.repository;

import com.banque.situationbancaire.entity.CompteCourant;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des comptes courants
 */
@Stateless
public class CompteCourantRepository {

    @PersistenceContext(unitName = "SituationBancairePU")
    private EntityManager em;

    public CompteCourant save(CompteCourant compte) {
        if (compte.getIdCompte() == null) {
            em.persist(compte);
            return compte;
        } else {
            return em.merge(compte);
        }
    }

    public Optional<CompteCourant> findById(Long id) {
        return Optional.ofNullable(em.find(CompteCourant.class, id));
    }

    public Optional<CompteCourant> findByNumeroCompte(String numeroCompte) {
        TypedQuery<CompteCourant> query = em.createQuery(
            "SELECT c FROM CompteCourant c WHERE c.numeroCompte = :numeroCompte", CompteCourant.class);
        query.setParameter("numeroCompte", numeroCompte);
        return query.getResultList().stream().findFirst();
    }

    public List<CompteCourant> findByClientId(Long clientId) {
        TypedQuery<CompteCourant> query = em.createQuery(
            "SELECT c FROM CompteCourant c WHERE c.client.idClient = :clientId", CompteCourant.class);
        query.setParameter("clientId", clientId);
        return query.getResultList();
    }

    public List<CompteCourant> findAll() {
        TypedQuery<CompteCourant> query = em.createQuery("SELECT c FROM CompteCourant c", CompteCourant.class);
        return query.getResultList();
    }

    public void delete(CompteCourant compte) {
        if (em.contains(compte)) {
            em.remove(compte);
        } else {
            em.remove(em.merge(compte));
        }
    }

    public CompteCourant update(CompteCourant compte) {
        return em.merge(compte);
    }

    public void deleteById(Long id) {
        CompteCourant compte = em.find(CompteCourant.class, id);
        if (compte != null) {
            em.remove(compte);
        }
    }
}
