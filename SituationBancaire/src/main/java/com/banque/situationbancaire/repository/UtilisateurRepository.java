package com.banque.situationbancaire.repository;

import com.banque.situationbancaire.entity.Utilisateur;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.NoResultException;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des utilisateurs
 */
@Stateless
public class UtilisateurRepository {

    @PersistenceContext(unitName = "SituationBancairePU")
    private EntityManager em;

    public Utilisateur save(Utilisateur utilisateur) {
        if (utilisateur.getIdUtilisateur() == null) {
            em.persist(utilisateur);
            return utilisateur;
        } else {
            return em.merge(utilisateur);
        }
    }

    public Optional<Utilisateur> findById(Integer id) {
        Utilisateur utilisateur = em.find(Utilisateur.class, id);
        return Optional.ofNullable(utilisateur);
    }

    public Optional<Utilisateur> findByLogin(String login) {
        try {
            Utilisateur utilisateur = em.createQuery(
                    "SELECT u FROM Utilisateur u WHERE u.loginUtilisateur = :login", 
                    Utilisateur.class)
                    .setParameter("login", login)
                    .getSingleResult();
            return Optional.of(utilisateur);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    public List<Utilisateur> findAll() {
        return em.createQuery("SELECT u FROM Utilisateur u", Utilisateur.class)
                .getResultList();
    }

    public List<Utilisateur> findByRole(Integer role) {
        return em.createQuery(
                "SELECT u FROM Utilisateur u WHERE u.roleUtilisateur = :role", 
                Utilisateur.class)
                .setParameter("role", role)
                .getResultList();
    }

    public List<Utilisateur> findByDirection(Integer idDirection) {
        return em.createQuery(
                "SELECT u FROM Utilisateur u WHERE u.direction.idDirection = :idDirection", 
                Utilisateur.class)
                .setParameter("idDirection", idDirection)
                .getResultList();
    }

    public void delete(Utilisateur utilisateur) {
        if (!em.contains(utilisateur)) {
            utilisateur = em.merge(utilisateur);
        }
        em.remove(utilisateur);
    }

    public void deleteById(Integer id) {
        findById(id).ifPresent(this::delete);
    }
}
