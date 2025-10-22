package com.banque.situationbancaire.repository;

import com.banque.situationbancaire.entity.Direction;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

/**
 * Repository pour la gestion des directions
 */
@Stateless
public class DirectionRepository {

    @PersistenceContext(unitName = "SituationBancairePU")
    private EntityManager em;

    public Direction save(Direction direction) {
        if (direction.getIdDirection() == null) {
            em.persist(direction);
            return direction;
        } else {
            return em.merge(direction);
        }
    }

    public Optional<Direction> findById(Integer id) {
        Direction direction = em.find(Direction.class, id);
        return Optional.ofNullable(direction);
    }

    public List<Direction> findAll() {
        return em.createQuery("SELECT d FROM Direction d", Direction.class)
                .getResultList();
    }

    public void delete(Direction direction) {
        if (!em.contains(direction)) {
            direction = em.merge(direction);
        }
        em.remove(direction);
    }

    public void deleteById(Integer id) {
        findById(id).ifPresent(this::delete);
    }
}
