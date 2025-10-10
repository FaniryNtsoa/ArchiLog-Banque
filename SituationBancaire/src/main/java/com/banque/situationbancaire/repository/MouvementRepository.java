package com.banque.situationbancaire.repository;

import com.banque.situationbancaire.entity.Mouvement;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des mouvements bancaires
 */
@Stateless
public class MouvementRepository {

    @PersistenceContext(unitName = "SituationBancairePU")
    private EntityManager em;

    public Mouvement save(Mouvement mouvement) {
        if (mouvement.getIdMouvement() == null) {
            em.persist(mouvement);
            return mouvement;
        } else {
            return em.merge(mouvement);
        }
    }

    public Optional<Mouvement> findById(Long id) {
        return Optional.ofNullable(em.find(Mouvement.class, id));
    }

    public Optional<Mouvement> findByReference(String reference) {
        TypedQuery<Mouvement> query = em.createQuery(
            "SELECT m FROM Mouvement m WHERE m.reference = :reference", Mouvement.class);
        query.setParameter("reference", reference);
        return query.getResultList().stream().findFirst();
    }

    public List<Mouvement> findByCompteId(Long compteId) {
        TypedQuery<Mouvement> query = em.createQuery(
            "SELECT m FROM Mouvement m WHERE m.compte.idCompte = :compteId ORDER BY m.dateOperation DESC", 
            Mouvement.class);
        query.setParameter("compteId", compteId);
        return query.getResultList();
    }

    public List<Mouvement> findByCompteIdBetweenDates(Long compteId, LocalDateTime dateDebut, LocalDateTime dateFin) {
        TypedQuery<Mouvement> query = em.createQuery(
            "SELECT m FROM Mouvement m WHERE m.compte.idCompte = :compteId " +
            "AND m.dateOperation BETWEEN :dateDebut AND :dateFin ORDER BY m.dateOperation DESC", 
            Mouvement.class);
        query.setParameter("compteId", compteId);
        query.setParameter("dateDebut", dateDebut);
        query.setParameter("dateFin", dateFin);
        return query.getResultList();
    }

    public Optional<Mouvement> findDernierMouvement(Long compteId) {
        TypedQuery<Mouvement> query = em.createQuery(
            "SELECT m FROM Mouvement m WHERE m.compte.idCompte = :compteId " +
            "ORDER BY m.dateOperation DESC, m.idMouvement DESC", 
            Mouvement.class);
        query.setParameter("compteId", compteId);
        query.setMaxResults(1);
        return query.getResultList().stream().findFirst();
    }
}
