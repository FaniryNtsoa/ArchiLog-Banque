package com.banque.pret.repository;

import com.banque.pret.entity.Remboursement;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Repository pour la gestion des remboursements
 */
@Stateless
public class RemboursementRepository {

    private static final Logger LOGGER = Logger.getLogger(RemboursementRepository.class.getName());

    @PersistenceContext(unitName = "PretPU")
    private EntityManager entityManager;

    /**
     * Sauvegarde un remboursement
     */
    public Remboursement save(Remboursement remboursement) {
        LOGGER.info("Sauvegarde du remboursement");
        entityManager.persist(remboursement);
        entityManager.flush();
        return remboursement;
    }

    /**
     * Met à jour un remboursement
     */
    public Remboursement update(Remboursement remboursement) {
        LOGGER.info("Mise à jour du remboursement : " + remboursement.getIdRemboursement());
        return entityManager.merge(remboursement);
    }

    /**
     * Recherche un remboursement par son ID
     */
    public Optional<Remboursement> findById(Long id) {
        LOGGER.info("Recherche du remboursement par ID : " + id);
        Remboursement remboursement = entityManager.find(Remboursement.class, id);
        return Optional.ofNullable(remboursement);
    }

    /**
     * Récupère tous les remboursements d'une échéance
     */
    public List<Remboursement> findByEcheanceId(Long echeanceId) {
        LOGGER.info("Récupération des remboursements de l'échéance : " + echeanceId);
        TypedQuery<Remboursement> query = entityManager.createQuery(
            "SELECT r FROM Remboursement r WHERE r.echeance.idEcheance = :echeanceId ORDER BY r.datePaiement DESC", Remboursement.class);
        query.setParameter("echeanceId", echeanceId);
        return query.getResultList();
    }

    /**
     * Récupère tous les remboursements d'un prêt
     */
    public List<Remboursement> findByPretId(Long pretId) {
        LOGGER.info("Récupération des remboursements du prêt : " + pretId);
        TypedQuery<Remboursement> query = entityManager.createQuery(
            "SELECT r FROM Remboursement r WHERE r.echeance.pret.idPret = :pretId ORDER BY r.datePaiement DESC", Remboursement.class);
        query.setParameter("pretId", pretId);
        return query.getResultList();
    }

    /**
     * Supprime un remboursement
     */
    public void deleteById(Long id) {
        LOGGER.info("Suppression du remboursement : " + id);
        Remboursement remboursement = entityManager.find(Remboursement.class, id);
        if (remboursement != null) {
            entityManager.remove(remboursement);
        }
    }
}
