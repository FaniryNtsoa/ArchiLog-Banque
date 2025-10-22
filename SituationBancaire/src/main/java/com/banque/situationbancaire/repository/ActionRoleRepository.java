package com.banque.situationbancaire.repository;

import com.banque.situationbancaire.entity.ActionRole;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des permissions d'action par rôle
 */
@Stateless
public class ActionRoleRepository {

    @PersistenceContext(unitName = "SituationBancairePU")
    private EntityManager em;

    public ActionRole save(ActionRole actionRole) {
        if (actionRole.getIdActionRole() == null) {
            em.persist(actionRole);
            return actionRole;
        } else {
            return em.merge(actionRole);
        }
    }

    public Optional<ActionRole> findById(Integer id) {
        ActionRole actionRole = em.find(ActionRole.class, id);
        return Optional.ofNullable(actionRole);
    }

    public List<ActionRole> findAll() {
        return em.createQuery("SELECT ar FROM ActionRole ar", ActionRole.class)
                .getResultList();
    }

    /**
     * Récupère toutes les permissions pour un rôle donné
     */
    public List<ActionRole> findByRole(Integer role) {
        return em.createQuery(
                "SELECT ar FROM ActionRole ar WHERE ar.roleRequis = :role", 
                ActionRole.class)
                .setParameter("role", role)
                .getResultList();
    }

    /**
     * Vérifie si une action est autorisée pour un rôle sur une table donnée
     */
    public boolean isActionAuthorized(String nomTable, String action, Integer role) {
        Long count = em.createQuery(
                "SELECT COUNT(ar) FROM ActionRole ar " +
                "WHERE ar.nomTable = :nomTable " +
                "AND ar.actionAutorisee = :action " +
                "AND ar.roleRequis = :role", 
                Long.class)
                .setParameter("nomTable", nomTable)
                .setParameter("action", action)
                .setParameter("role", role)
                .getSingleResult();
        return count > 0;
    }

    /**
     * Récupère toutes les actions autorisées pour une table et un rôle
     */
    public List<ActionRole> findByTableAndRole(String nomTable, Integer role) {
        return em.createQuery(
                "SELECT ar FROM ActionRole ar " +
                "WHERE ar.nomTable = :nomTable " +
                "AND ar.roleRequis = :role", 
                ActionRole.class)
                .setParameter("nomTable", nomTable)
                .setParameter("role", role)
                .getResultList();
    }

    public void delete(ActionRole actionRole) {
        if (!em.contains(actionRole)) {
            actionRole = em.merge(actionRole);
        }
        em.remove(actionRole);
    }

    public void deleteById(Integer id) {
        findById(id).ifPresent(this::delete);
    }
}
