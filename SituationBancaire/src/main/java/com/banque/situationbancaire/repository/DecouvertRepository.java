package com.banque.situationbancaire.repository;

import com.banque.situationbancaire.entity.Decouvert;
import com.banque.situationbancaire.entity.enums.StatutDecouvert;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Repository pour la gestion des découverts
 */
@ApplicationScoped
public class DecouvertRepository {
    
    private static final Logger LOGGER = Logger.getLogger(DecouvertRepository.class.getName());
    
    @PersistenceContext(unitName = "SituationBancairePU")
    private EntityManager entityManager;
    
    /**
     * Sauvegarde un découvert
     * @param decouvert Le découvert à sauvegarder
     * @return Le découvert sauvegardé
     */
    public Decouvert save(Decouvert decouvert) {
        try {
            if (decouvert.getIdDecouvert() == null) {
                entityManager.persist(decouvert);
                return decouvert;
            } else {
                return entityManager.merge(decouvert);
            }
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la sauvegarde du découvert: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la sauvegarde du découvert", e);
        }
    }
    
    /**
     * Trouve un découvert par son ID
     * @param id L'ID du découvert
     * @return Le découvert ou empty
     */
    public Optional<Decouvert> findById(Long id) {
        try {
            Decouvert decouvert = entityManager.find(Decouvert.class, id);
            return Optional.ofNullable(decouvert);
        } catch (Exception e) {
            LOGGER.warning("Découvert non trouvé avec l'ID: " + id);
            return Optional.empty();
        }
    }
    
    /**
     * Trouve le découvert actif d'un compte
     * @param idCompte L'ID du compte
     * @return Le découvert actif ou empty
     */
    public Optional<Decouvert> findActiveByCompte(Long idCompte) {
        try {
            TypedQuery<Decouvert> query = entityManager.createQuery(
                "SELECT d FROM Decouvert d " +
                "WHERE d.compte.idCompte = :idCompte " +
                "AND d.statut = :statut " +
                "AND (d.dateFin IS NULL OR d.dateFin >= :today) " +
                "ORDER BY d.dateDebut DESC", 
                Decouvert.class);
            query.setParameter("idCompte", idCompte);
            query.setParameter("statut", StatutDecouvert.ACTIF);
            query.setParameter("today", LocalDate.now());
            query.setMaxResults(1);
            
            List<Decouvert> results = query.getResultList();
            return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
        } catch (Exception e) {
            LOGGER.warning("Erreur lors de la recherche du découvert actif pour le compte: " + idCompte);
            return Optional.empty();
        }
    }
    
    /**
     * Trouve tous les découverts d'un compte
     * @param idCompte L'ID du compte
     * @return La liste des découverts
     */
    public List<Decouvert> findByCompte(Long idCompte) {
        try {
            TypedQuery<Decouvert> query = entityManager.createQuery(
                "SELECT d FROM Decouvert d " +
                "WHERE d.compte.idCompte = :idCompte " +
                "ORDER BY d.dateDebut DESC", 
                Decouvert.class);
            query.setParameter("idCompte", idCompte);
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la récupération des découverts du compte: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des découverts", e);
        }
    }
    
    /**
     * Trouve tous les découverts actifs nécessitant un calcul d'intérêts
     * @return La liste des découverts actifs
     */
    public List<Decouvert> findActiveDecouverts() {
        try {
            TypedQuery<Decouvert> query = entityManager.createQuery(
                "SELECT d FROM Decouvert d " +
                "WHERE d.statut = :statut " +
                "AND (d.dateFin IS NULL OR d.dateFin >= :today) " +
                "ORDER BY d.dateDebut ASC", 
                Decouvert.class);
            query.setParameter("statut", StatutDecouvert.ACTIF);
            query.setParameter("today", LocalDate.now());
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la récupération des découverts actifs: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la récupération des découverts actifs", e);
        }
    }
    
    /**
     * Ferme un découvert
     * @param decouvert Le découvert à fermer
     * @param motif Le motif de fermeture
     */
    public void fermerDecouvert(Decouvert decouvert, String motif) {
        try {
            decouvert.setStatut(StatutDecouvert.FERME);
            decouvert.setDateFin(LocalDate.now());
            decouvert.setMotifFermeture(motif);
            entityManager.merge(decouvert);
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la fermeture du découvert: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la fermeture du découvert", e);
        }
    }
    
    /**
     * Met à jour le montant du découvert
     * @param decouvert Le découvert à mettre à jour
     */
    public void updateMontant(Decouvert decouvert) {
        try {
            entityManager.merge(decouvert);
        } catch (Exception e) {
            LOGGER.severe("Erreur lors de la mise à jour du montant du découvert: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour du découvert", e);
        }
    }
}