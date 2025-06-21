package at.technikum.javafx.repository;

import at.technikum.javafx.entity.SearchTerm;

import java.util.List;
import java.util.Optional;

public interface SearchTermRepository {

    Optional<SearchTerm> find(String id);

    List<SearchTerm> findAll();

    SearchTerm save(SearchTerm entity);

    SearchTerm delete(SearchTerm entity);

    List<SearchTerm> deleteAll();

    Optional<SearchTerm> findByTerm(String term);
}
