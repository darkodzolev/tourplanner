package at.technikum.javafx.repository;

import at.technikum.javafx.dal.JPAUtil;
import at.technikum.javafx.entity.Tour;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import java.util.List;
import java.util.Optional;

public class TourRepositoryOrm implements TourRepository {

    public TourRepositoryOrm() {

    }

    private EntityManager em() {
        return JPAUtil.getEntityManager();
    }

    @Override
    public Optional<Tour> find(Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(Tour.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Tour> findAll() {
        EntityManager em = em();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Tour> cq = cb.createQuery(Tour.class);
            Root<Tour> root = cq.from(Tour.class);
            cq.select(root);
            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public Tour save(Tour entity) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (entity.getId() == null) {
                em.persist(entity);
            } else {
                entity = em.merge(entity);
            }
            tx.commit();
            return entity;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Tour delete(Tour entity) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Tour managed = em.merge(entity);
            em.remove(managed);
            tx.commit();
            return entity;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Tour> deleteAll() {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaDelete<Tour> cd = cb.createCriteriaDelete(Tour.class);
            cd.from(Tour.class);
            em.createQuery(cd).executeUpdate();
            tx.commit();
            return List.of();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Tour> findByName(String name) {
        EntityManager em = em();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Tour> cq = cb.createQuery(Tour.class);
            Root<Tour> root = cq.from(Tour.class);
            cq.select(root)
                    .where(cb.equal(root.get("name"), name));
            Tour result = em.createQuery(cq)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            return Optional.ofNullable(result);
        } finally {
            em.close();
        }
    }
}