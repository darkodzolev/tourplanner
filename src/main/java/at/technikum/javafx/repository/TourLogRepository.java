package at.technikum.javafx.repository;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;

import java.util.List;
import java.util.Optional;

public interface TourLogRepository {

    Optional<TourLog> find(Long id);

    List<TourLog> findAll();

    TourLog save(TourLog entity);

    TourLog delete(TourLog entity);

    List<TourLog> deleteAll();

    List<TourLog> findByTour(Tour tour);
}