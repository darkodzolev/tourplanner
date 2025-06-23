package at.technikum.javafx.service;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.repository.TourLogRepository;
import at.technikum.javafx.repository.TourRepository;

import java.util.List;
import java.util.Optional;

public class TourService {

    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;

    public TourService(TourRepository tourRepository, TourLogRepository tourLogRepository) {
        this.tourRepository = tourRepository;
        this.tourLogRepository = tourLogRepository;
    }

    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    public Tour createTour(Tour tour) {
        validateTour(tour);
        Optional<Tour> existing = tourRepository.findByName(tour.getName());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("A tour with this name already exists: " + tour.getName());
        }
        return tourRepository.save(tour);
    }

    public Tour updateTour(Tour tour) {
        if (tour.getId() == null) {
            throw new IllegalArgumentException("Cannot update tour without an ID");
        }
        validateTour(tour);
        return tourRepository.save(tour);
    }

    public void deleteTour(Tour tour) {
        tourRepository.delete(tour);
    }

    public Optional<Tour> findById(Long id) {
        return tourRepository.find(id);
    }

    public Optional<Tour> findByName(String name) {
        return tourRepository.findByName(name);
    }

    private void validateTour(Tour tour) {
        if (tour.getName() == null || tour.getName().isBlank()) {
            throw new IllegalArgumentException("Tour name is required");
        }
        if (tour.getFromLocation() == null || tour.getFromLocation().isBlank()) {
            throw new IllegalArgumentException("Origin location is required");
        }
        if (tour.getToLocation() == null || tour.getToLocation().isBlank()) {
            throw new IllegalArgumentException("Destination location is required");
        }
        if (tour.getTransportType() == null || tour.getTransportType().isBlank()) {
            throw new IllegalArgumentException("Transport type is required");
        }
        if (tour.getDistance() < 0) {
            throw new IllegalArgumentException("Distance must be zero or positive");
        }
        if (tour.getEstimatedTime() == null || tour.getEstimatedTime().isBlank()) {
            throw new IllegalArgumentException("Estimated time is required");
        }
    }
}