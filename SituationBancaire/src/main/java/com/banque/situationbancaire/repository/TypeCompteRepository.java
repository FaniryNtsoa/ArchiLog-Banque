package com.banque.situationbancaire.repository;

import com.banque.situationbancaire.entity.TypeCompte;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Repository pour la gestion des types de compte
 */
@ApplicationScoped
public class TypeCompteRepository {

    private static final Logger LOGGER = Logger.getLogger(TypeCompteRepository.class.getName());

    @PersistenceContext(unitName = "SituationBancairePU")
    private EntityManager entityManager;

    /**
     * Trouve tous les types de compte avec leurs paramètres
     * 
     * @return Liste de tous les types de compte
     */
    public List<TypeCompte> findAll() {
        try {
            TypedQuery<TypeCompte> query = entityManager.createQuery(
                    "SELECT t FROM TypeCompte t " +
                            "LEFT JOIN FETCH t.parametreActuel " + // Supprimer l'alias 'p'
                            "LEFT JOIN FETCH t.parametreActuel.tauxDecouvert " + // Accéder directement à la relation
                            "ORDER BY t.libelle",
                    TypeCompte.class);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la récupération des types de compte: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des types de compte", e);
        }
    }

    /**
     * Trouve un type de compte par son ID
     * 
     * @param id L'ID du type de compte
     * @return Le type de compte ou empty
     */
    public Optional<TypeCompte> findById(Long id) {
        try {
            TypedQuery<TypeCompte> query = entityManager.createQuery(
                    "SELECT t FROM TypeCompte t " +
                            "LEFT JOIN FETCH t.parametreActuel " +
                            "LEFT JOIN FETCH t.parametreActuel.tauxDecouvert " + // Correction ici
                            "WHERE t.idTypeCompte = :id",
                    TypeCompte.class);
            query.setParameter("id", id);
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            LOGGER.warning("Type de compte non trouvé avec l'ID: " + id);
            return Optional.empty();
        }
    }

    /**
     * Trouve un type de compte par son code
     * 
     * @param codeType Le code du type de compte
     * @return Le type de compte ou empty
     */
    public Optional<TypeCompte> findByCode(String codeType) {
        try {
            TypedQuery<TypeCompte> query = entityManager.createQuery(
                    "SELECT t FROM TypeCompte t " +
                            "LEFT JOIN FETCH t.parametreActuel " +
                            "LEFT JOIN FETCH t.parametreActuel.tauxDecouvert " + // Correction ici
                            "WHERE t.codeType = :codeType",
                    TypeCompte.class);
            query.setParameter("codeType", codeType);
            return Optional.of(query.getSingleResult());
        } catch (Exception e) {
            LOGGER.warning("Type de compte non trouvé avec le code: " + codeType);
            return Optional.empty();
        }
    }

    /**
     * Trouve le type de compte par défaut (STANDARD)
     * 
     * @return Le type de compte par défaut ou empty
     */
    public Optional<TypeCompte> findByDefaut() {
        return findByCode("STANDARD");
    }

    /**
     * Vérifie si un type de compte existe par son code
     * 
     * @param codeType Le code du type de compte
     * @return true si existe, false sinon
     */
    public boolean existsByCode(String codeType) {
        try {
            TypedQuery<Long> query = entityManager.createQuery(
                    "SELECT COUNT(t) FROM TypeCompte t WHERE t.codeType = :codeType",
                    Long.class);
            query.setParameter("codeType", codeType);
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de la vérification de l'existence du type de compte: " + e.getMessage());
            return false;
        }
    }
}
