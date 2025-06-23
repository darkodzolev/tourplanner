package at.technikum.javafx.repository;

import at.technikum.javafx.entity.Tour;

import java.util.List;
import java.util.Optional;

public interface TourRepository {

    Optional<Tour> find(Long id);

    List<Tour> findAll();

    Tour save(Tour entity);

    Tour delete(Tour entity);

    List<Tour> deleteAll();

    Optional<Tour> findByName(String name);
}