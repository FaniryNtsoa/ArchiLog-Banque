package com.banque.pret.repository;

import com.banque.pret.entity.Pret;
import com.banque.pret.entity.enums.StatutPret;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Repository pour la gestion des prêts
 */
@Stateless
public class PretRepository {

    private static final Logger LOGGER = Logger.getLogger(PretRepository.class.getName());

    @PersistenceContext(unitName = "PretPU")
    private EntityManager entityManager;

    /**
     * Sauvegarde un prêt
     */
    public Pret save(Pret pret) {
        LOGGER.info("Sauvegarde du prêt : " + pret.getNumeroPret());
        entityManager.persist(pret);
        entityManager.flush();
        return pret;
    }

    /**
     * Met à jour un prêt
     */
    public Pret update(Pret pret) {
        LOGGER.info("Mise à jour du prêt : " + pret.getIdPret());
        return entityManager.merge(pret);
    }

    /**
     * Recherche un prêt par son ID
     */
    public Optional<Pret> findById(Long id) {
        LOGGER.info("Recherche du prêt par ID : " + id);
        Pret pret = entityManager.find(Pret.class, id);
        return Optional.ofNullable(pret);
    }

    /**
     * Recherche un prêt par son numéro
     */
    public Optional<Pret> findByNumeroPret(String numeroPret) {
        LOGGER.info("Recherche du prêt par numéro : " + numeroPret);
        TypedQuery<Pret> query = entityManager.createQuery(
            "SELECT p FROM Pret p WHERE p.numeroPret = :numeroPret", Pret.class);
        query.setParameter("numeroPret", numeroPret);
        
        List<Pret> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Récupère tous les prêts
     */
    public List<Pret> findAll() {
        LOGGER.info("Récupération de tous les prêts");
        TypedQuery<Pret> query = entityManager.createQuery(
            "SELECT p FROM Pret p ORDER BY p.dateDemande DESC", Pret.class);
        return query.getResultList();
    }

    /**
     * Récupère tous les prêts d'un client
     */
    public List<Pret> findByClientId(Long clientId) {
        LOGGER.info("Récupération des prêts du client : " + clientId);
        TypedQuery<Pret> query = entityManager.createQuery(
            "SELECT p FROM Pret p WHERE p.client.idClient = :clientId ORDER BY p.dateDemande DESC", Pret.class);
        query.setParameter("clientId", clientId);
        return query.getResultList();
    }

    /**
     * Récupère tous les prêts par statut
     */
    public List<Pret> findByStatut(StatutPret statut) {
        LOGGER.info("Récupération des prêts avec statut : " + statut);
        TypedQuery<Pret> query = entityManager.createQuery(
            "SELECT p FROM Pret p WHERE p.statut = :statut ORDER BY p.dateDemande DESC", Pret.class);
        query.setParameter("statut", statut);
        return query.getResultList();
    }

    /**
     * Récupère tous les prêts d'un client par statut
     */
    public List<Pret> findByClientIdAndStatut(Long clientId, StatutPret statut) {
        LOGGER.info("Récupération des prêts du client " + clientId + " avec statut : " + statut);
        TypedQuery<Pret> query = entityManager.createQuery(
            "SELECT p FROM Pret p WHERE p.client.idClient = :clientId AND p.statut = :statut ORDER BY p.dateDemande DESC", Pret.class);
        query.setParameter("clientId", clientId);
        query.setParameter("statut", statut);
        return query.getResultList();
    }

    /**
     * Supprime un prêt
     */
    public void deleteById(Long id) {
        LOGGER.info("Suppression du prêt : " + id);
        Pret pret = entityManager.find(Pret.class, id);
        if (pret != null) {
            entityManager.remove(pret);
        }
    }
}
