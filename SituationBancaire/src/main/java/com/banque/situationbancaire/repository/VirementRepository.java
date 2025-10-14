package com.banque.situationbancaire.repository;

import com.banque.situationbancaire.entity.Virement;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des virements
 */
@Stateless
public class VirementRepository {

    @PersistenceContext(unitName = "SituationBancairePU")
    private EntityManager em;

    public Virement save(Virement virement) {
        if (virement.getIdVirement() == null) {
            em.persist(virement);
            return virement;
        } else {
            return em.merge(virement);
        }
    }

    public Optional<Virement> findById(Long id) {
        return Optional.ofNullable(em.find(Virement.class, id));
    }

    public Optional<Virement> findByReference(String reference) {
        TypedQuery<Virement> query = em.createQuery(
            "SELECT v FROM Virement v WHERE v.reference = :reference", Virement.class);
        query.setParameter("reference", reference);
        return query.getResultList().stream().findFirst();
    }

    public List<Virement> findByCompteDebiteurId(Long compteId) {
        TypedQuery<Virement> query = em.createQuery(
            "SELECT v FROM Virement v WHERE v.compteDebiteur.idCompte = :compteId ORDER BY v.dateVirement DESC", 
            Virement.class);
        query.setParameter("compteId", compteId);
        return query.getResultList();
    }

    public List<Virement> findByCompteCrediteurId(Long compteId) {
        TypedQuery<Virement> query = em.createQuery(
            "SELECT v FROM Virement v WHERE v.compteCrediteur.idCompte = :compteId ORDER BY v.dateVirement DESC", 
            Virement.class);
        query.setParameter("compteId", compteId);
        return query.getResultList();
    }

    public List<Virement> findByCompteId(Long compteId) {
        TypedQuery<Virement> query = em.createQuery(
            "SELECT v FROM Virement v WHERE v.compteDebiteur.idCompte = :compteId OR v.compteCrediteur.idCompte = :compteId ORDER BY v.dateVirement DESC", 
            Virement.class);
        query.setParameter("compteId", compteId);
        return query.getResultList();
    }

    public List<Virement> findBetweenDates(LocalDateTime dateDebut, LocalDateTime dateFin) {
        TypedQuery<Virement> query = em.createQuery(
            "SELECT v FROM Virement v WHERE v.dateVirement BETWEEN :dateDebut AND :dateFin ORDER BY v.dateVirement DESC", 
            Virement.class);
        query.setParameter("dateDebut", dateDebut);
        query.setParameter("dateFin", dateFin);
        return query.getResultList();
    }
}