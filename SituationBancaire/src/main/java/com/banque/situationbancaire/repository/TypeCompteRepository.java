package com.banque.situationbancaire.repository;

import com.banque.situationbancaire.entity.TypeCompte;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Optional;

/**
 * Repository pour la gestion des types de comptes
 */
@Stateless
public class TypeCompteRepository {

    @PersistenceContext(unitName = "SituationBancairePU")
    private EntityManager em;

    public Optional<TypeCompte> findByCode(String code) {
        TypedQuery<TypeCompte> query = em.createQuery(
            "SELECT t FROM TypeCompte t WHERE t.codeType = :code", TypeCompte.class);
        query.setParameter("code", code);
        return query.getResultList().stream().findFirst();
    }

    public Optional<TypeCompte> findById(Long id) {
        return Optional.ofNullable(em.find(TypeCompte.class, id));
    }

    public Optional<TypeCompte> findByDefaut() {
        TypedQuery<TypeCompte> query = em.createQuery(
            "SELECT t FROM TypeCompte t WHERE t.parDefaut = true", TypeCompte.class);
        return query.getResultList().stream().findFirst();
    }
}
