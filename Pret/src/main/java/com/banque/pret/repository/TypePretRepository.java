package com.banque.pret.repository;

import com.banque.pret.entity.TypePret;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Repository pour la gestion des types de prêts
 */
@Stateless
public class TypePretRepository {

    private static final Logger LOGGER = Logger.getLogger(TypePretRepository.class.getName());

    @PersistenceContext(unitName = "PretPU")
    private EntityManager entityManager;

    /**
     * Sauvegarde un type de prêt
     */
    public TypePret save(TypePret typePret) {
        LOGGER.info("Sauvegarde du type de prêt : " + typePret.getLibelle());
        entityManager.persist(typePret);
        entityManager.flush();
        return typePret;
    }

    /**
     * Met à jour un type de prêt
     */
    public TypePret update(TypePret typePret) {
        LOGGER.info("Mise à jour du type de prêt : " + typePret.getIdTypePret());
        return entityManager.merge(typePret);
    }

    /**
     * Recherche un type de prêt par son ID
     */
    public Optional<TypePret> findById(Long id) {
        LOGGER.info("Recherche du type de prêt par ID : " + id);
        TypePret typePret = entityManager.find(TypePret.class, id);
        return Optional.ofNullable(typePret);
    }

    /**
     * Recherche un type de prêt par son code
     */
    public Optional<TypePret> findByCodeType(String codeType) {
        LOGGER.info("Recherche du type de prêt par code : " + codeType);
        TypedQuery<TypePret> query = entityManager.createQuery(
            "SELECT t FROM TypePret t WHERE t.codeType = :codeType", TypePret.class);
        query.setParameter("codeType", codeType);
        
        List<TypePret> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Récupère tous les types de prêts
     */
    public List<TypePret> findAll() {
        LOGGER.info("Récupération de tous les types de prêts");
        TypedQuery<TypePret> query = entityManager.createQuery(
            "SELECT t FROM TypePret t ORDER BY t.libelle ASC", TypePret.class);
        return query.getResultList();
    }

    /**
     * Récupère tous les types de prêts actifs
     */
    public List<TypePret> findAllActifs() {
        LOGGER.info("Récupération de tous les types de prêts actifs");
        TypedQuery<TypePret> query = entityManager.createQuery(
            "SELECT t FROM TypePret t WHERE t.actif = true ORDER BY t.libelle ASC", TypePret.class);
        return query.getResultList();
    }

    /**
     * Supprime un type de prêt
     */
    public void deleteById(Long id) {
        LOGGER.info("Suppression du type de prêt : " + id);
        TypePret typePret = entityManager.find(TypePret.class, id);
        if (typePret != null) {
            entityManager.remove(typePret);
        }
    }
}
