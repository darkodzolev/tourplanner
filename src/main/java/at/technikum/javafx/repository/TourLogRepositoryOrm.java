package at.technikum.javafx.repository;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

public class TourLogRepositoryOrm implements TourLogRepository {

    private final EntityManagerFactory emf;

    public TourLogRepositoryOrm() {
        this.emf = Persistence.createEntityManagerFactory("hibernate");
    }

    private EntityManager em() {
        return emf.createEntityManager();
    }

    @Override
    public Optional<TourLog> find(Long id) {
        EntityManager em = em();
        try {
            return Optional.ofNullable(em.find(TourLog.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<TourLog> findAll() {
        EntityManager em = em();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<TourLog> cq = cb.createQuery(TourLog.class);
            Root<TourLog> root = cq.from(TourLog.class);
            cq.select(root);
            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public TourLog save(TourLog entity) {
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
    public TourLog delete(TourLog entity) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            TourLog managed = em.find(TourLog.class, entity.getId());
            if (managed != null) {
                em.remove(managed);
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
    public List<TourLog> deleteAll() {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaDelete<TourLog> cd = cb.createCriteriaDelete(TourLog.class);
            cd.from(TourLog.class);
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
    public List<TourLog> findByTour(Tour tour) {
        EntityManager em = em();
        try {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<TourLog> cq = cb.createQuery(TourLog.class);
            Root<TourLog> root = cq.from(TourLog.class);
            cq.select(root).where(cb.equal(root.get("tour"), tour));
            return em.createQuery(cq).getResultList();
        } finally {
            em.close();
        }
    }
}