package at.technikum.javafx.service;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.repository.TourLogRepository;
import at.technikum.javafx.repository.TourRepository;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TourService implements ITourService {

    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;
    private final EventManager eventManager;

    public TourService(TourRepository tourRepository,
                       TourLogRepository tourLogRepository,
                       EventManager eventManager) {
        this.tourRepository    = tourRepository;
        this.tourLogRepository = tourLogRepository;
        this.eventManager      = eventManager;
    }

    public List<Tour> getAllTours() {
        return tourRepository.findAll();
    }

    public Tour createTour(Tour tour) {
        validateTour(tour);
        tourRepository.findByName(tour.getName())
                .ifPresent(t -> { throw new IllegalArgumentException(
                        "A tour with this name already exists: " + tour.getName()
                ); });
        Tour created = tourRepository.save(tour);
        eventManager.publish(Events.TOURS_CHANGED, created);
        return created;
    }

    public Tour updateTour(Tour tour) {
        if (tour.getId() == null) {
            throw new IllegalArgumentException("Cannot update tour without an ID");
        }
        validateTour(tour);
        Tour updated = tourRepository.save(tour);
        eventManager.publish(Events.TOURS_CHANGED, updated);
        return updated;
    }

    public void deleteTour(Tour tour) {
        tourRepository.delete(tour);
        eventManager.publish(Events.TOURS_CHANGED, tour);
    }

    public Optional<Tour> findById(Long id) {
        return tourRepository.findById(id);
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