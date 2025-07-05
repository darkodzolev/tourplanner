package at.technikum.javafx.dal.dao;

import at.technikum.javafx.dal.JPAUtil;
import at.technikum.javafx.entity.TourLog;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class TourLogDAOImpl implements TourLogDAO {

    @Override
    public TourLog findById(Long id) {
        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            return entityManager.find(TourLog.class, id);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<TourLog> findAll() {
        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            TypedQuery<TourLog> query = entityManager.createQuery(
                    "SELECT logEntry FROM TourLog logEntry", TourLog.class);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<TourLog> findByTourId(Long tourId) {
        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            TypedQuery<TourLog> query = entityManager.createQuery(
                    "SELECT logEntry FROM TourLog logEntry WHERE logEntry.tour.id = :tid", TourLog.class);
            query.setParameter("tid", tourId);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void save(TourLog tourLog) {
        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(tourLog);
            entityManager.getTransaction().commit();
        } catch (RuntimeException e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void update(TourLog tourLog) {
        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.merge(tourLog);
            entityManager.getTransaction().commit();
        } catch (RuntimeException e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void delete(TourLog tourLog) {
        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            TourLog managed = entityManager.contains(tourLog)
                    ? tourLog
                    : entityManager.merge(tourLog);
            entityManager.remove(managed);
            entityManager.getTransaction().commit();
        } catch (RuntimeException e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }
}