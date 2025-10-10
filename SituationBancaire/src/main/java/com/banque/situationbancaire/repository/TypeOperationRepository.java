package com.banque.situationbancaire.repository;

import com.banque.situationbancaire.entity.TypeOperation;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.Optional;

/**
 * Repository pour la gestion des types d'opérations
 */
@Stateless
public class TypeOperationRepository {

    @PersistenceContext(unitName = "SituationBancairePU")
    private EntityManager em;

    public Optional<TypeOperation> findByCode(String code) {
        TypedQuery<TypeOperation> query = em.createQuery(
            "SELECT t FROM TypeOperation t WHERE t.codeOperation = :code", TypeOperation.class);
        query.setParameter("code", code);
        return query.getResultList().stream().findFirst();
    }

    public Optional<TypeOperation> findById(Long id) {
        return Optional.ofNullable(em.find(TypeOperation.class, id));
    }
}
