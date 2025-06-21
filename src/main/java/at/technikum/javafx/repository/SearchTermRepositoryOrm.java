package at.technikum.javafx.repository;

import at.technikum.javafx.entity.SearchTerm;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import java.util.List;
import java.util.Optional;

public class SearchTermRepositoryOrm implements SearchTermRepository {

    private final EntityManagerFactory entityManagerFactory;

    public SearchTermRepositoryOrm() {
        this.entityManagerFactory =
                Persistence.createEntityManagerFactory("hibernate");
    }

    @Override
    public Optional<SearchTerm> find(String id) {
        CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        CriteriaQuery<SearchTerm> query = cb.createQuery(SearchTerm.class);
        Root<SearchTerm> root = query.from(SearchTerm.class);

        // SELECT * FROM SearchTerm WHERE id == ?
        query.select(root).where(cb.equal(root.get("id"), id));

        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return Optional.ofNullable(
                    entityManager.createQuery(query).getSingleResultOrNull()
            );
        }

    }

    @Override
    public List<SearchTerm> findAll() {
        CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        CriteriaQuery<SearchTerm> query = cb.createQuery(SearchTerm.class);
        Root<SearchTerm> root = query.from(SearchTerm.class);

        query.select(root);

        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return entityManager.createQuery(query).getResultList();
        }

    }

    @Override
    public SearchTerm save(SearchTerm entity) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();

            transaction.begin();
            if (find(entity.getId()).isEmpty()) {
                entityManager.persist(entity);
            } else {
                entity = entityManager.merge(entity);
            }
            transaction.commit();

            return entity;
        }
    }

    @Override
    public SearchTerm delete(SearchTerm entity) {
        return null;
    }

    @Override
    public List<SearchTerm> deleteAll() {
        List<SearchTerm> searchTerms = findAll();

        CriteriaBuilder cb = entityManagerFactory.getCriteriaBuilder();
        CriteriaDelete<SearchTerm> query = cb.createCriteriaDelete(SearchTerm.class);
        Root<SearchTerm> root = query.from(SearchTerm.class);

        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();

            transaction.begin();
            entityManager.createQuery(query).executeUpdate();
            transaction.commit();
        }

        return searchTerms;
    }

    @Override
    public Optional<SearchTerm> findByTerm(String term) {
        return Optional.empty();
    }
}
