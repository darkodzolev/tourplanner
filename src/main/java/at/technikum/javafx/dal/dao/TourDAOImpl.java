package at.technikum.javafx.dal.dao;

import at.technikum.javafx.dal.JPAUtil;
import at.technikum.javafx.entity.Tour;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class TourDAOImpl implements TourDAO {

    @Override
    public Tour findById(Long id) {
        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            return entityManager.find(Tour.class, id);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public List<Tour> findAll() {
        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            TypedQuery<Tour> query = entityManager.createQuery(
                    "SELECT tour FROM Tour tour", Tour.class);
            return query.getResultList();
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void save(Tour tour) {
        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.persist(tour);
            entityManager.getTransaction().commit();
        } catch (RuntimeException e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void update(Tour tour) {
        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            entityManager.merge(tour);
            entityManager.getTransaction().commit();
        } catch (RuntimeException e) {
            entityManager.getTransaction().rollback();
            throw e;
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void delete(Tour tour) {
        EntityManager entityManager = JPAUtil.getEntityManager();
        try {
            entityManager.getTransaction().begin();
            // Ensure the entity is managed before removal
            Tour managed = entityManager.contains(tour) ? tour : entityManager.merge(tour);
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