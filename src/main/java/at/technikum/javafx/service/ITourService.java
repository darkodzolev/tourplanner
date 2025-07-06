package at.technikum.javafx.service;

import at.technikum.javafx.entity.Tour;
import java.util.List;
import java.util.Optional;

public interface ITourService {
    List<Tour> getAllTours();
    Tour createTour(Tour tour);
    Tour updateTour(Tour tour);
    void deleteTour(Tour tour);
    Optional<Tour> findById(Long id);
    Optional<Tour> findByName(String name);
}