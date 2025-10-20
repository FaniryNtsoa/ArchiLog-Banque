package com.banque.pret.repository;

import com.banque.pret.entity.Echeance;
import com.banque.pret.entity.enums.StatutEcheance;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Repository pour la gestion des échéances
 */
@Stateless
public class EcheanceRepository {

    private static final Logger LOGGER = Logger.getLogger(EcheanceRepository.class.getName());

    @PersistenceContext(unitName = "PretPU")
    private EntityManager entityManager;

    /**
     * Sauvegarde une échéance
     */
    public Echeance save(Echeance echeance) {
        LOGGER.info("Sauvegarde de l'échéance n°" + echeance.getNumeroEcheance());
        entityManager.persist(echeance);
        entityManager.flush();
        return echeance;
    }

    /**
     * Met à jour une échéance
     */
    public Echeance update(Echeance echeance) {
        LOGGER.info("Mise à jour de l'échéance : " + echeance.getIdEcheance());
        return entityManager.merge(echeance);
    }

    /**
     * Recherche une échéance par son ID
     */
    public Optional<Echeance> findById(Long id) {
        LOGGER.info("Recherche de l'échéance par ID : " + id);
        Echeance echeance = entityManager.find(Echeance.class, id);
        return Optional.ofNullable(echeance);
    }

    /**
     * Récupère toutes les échéances d'un prêt
     */
    public List<Echeance> findByPretId(Long pretId) {
        LOGGER.info("Récupération des échéances du prêt : " + pretId);
        TypedQuery<Echeance> query = entityManager.createQuery(
            "SELECT e FROM Echeance e WHERE e.pret.idPret = :pretId ORDER BY e.numeroEcheance ASC", Echeance.class);
        query.setParameter("pretId", pretId);
        return query.getResultList();
    }

    /**
     * Récupère toutes les échéances d'un prêt par statut
     */
    public List<Echeance> findByPretIdAndStatut(Long pretId, StatutEcheance statut) {
        LOGGER.info("Récupération des échéances du prêt " + pretId + " avec statut : " + statut);
        TypedQuery<Echeance> query = entityManager.createQuery(
            "SELECT e FROM Echeance e WHERE e.pret.idPret = :pretId AND e.statut = :statut ORDER BY e.numeroEcheance ASC", Echeance.class);
        query.setParameter("pretId", pretId);
        query.setParameter("statut", statut);
        return query.getResultList();
    }

    /**
     * Récupère les échéances à échoir avant une date donnée
     */
    public List<Echeance> findEcheancesAvantDate(LocalDate date) {
        LOGGER.info("Récupération des échéances avant : " + date);
        TypedQuery<Echeance> query = entityManager.createQuery(
            "SELECT e FROM Echeance e WHERE e.dateEcheance <= :date AND e.statut != :statut ORDER BY e.dateEcheance ASC", Echeance.class);
        query.setParameter("date", date);
        query.setParameter("statut", StatutEcheance.PAYE);
        return query.getResultList();
    }

    /**
     * Récupère les échéances en retard
     */
    public List<Echeance> findEcheancesEnRetard() {
        LOGGER.info("Récupération des échéances en retard");
        TypedQuery<Echeance> query = entityManager.createQuery(
            "SELECT e FROM Echeance e WHERE e.statut = :statut ORDER BY e.dateEcheance ASC", Echeance.class);
        query.setParameter("statut", StatutEcheance.EN_RETARD);
        return query.getResultList();
    }

    /**
     * Supprime une échéance
     */
    public void deleteById(Long id) {
        LOGGER.info("Suppression de l'échéance : " + id);
        Echeance echeance = entityManager.find(Echeance.class, id);
        if (echeance != null) {
            entityManager.remove(echeance);
        }
    }

    /**
     * Récupère toutes les échéances
     */
    public List<Echeance> findAll() {
        LOGGER.info("Récupération de toutes les échéances");
        TypedQuery<Echeance> query = entityManager.createQuery(
            "SELECT e FROM Echeance e ORDER BY e.pret.idPret, e.numeroEcheance ASC", Echeance.class);
        return query.getResultList();
    }
}
