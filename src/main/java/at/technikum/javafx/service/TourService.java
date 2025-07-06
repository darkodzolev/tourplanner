package at.technikum.javafx.service;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.repository.TourLogRepository;
import at.technikum.javafx.repository.TourRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TourService implements ITourService {

    private static final Logger log = LoggerFactory.getLogger(TourService.class);

    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;
    private final EventManager eventManager;

    public TourService(TourRepository tourRepository,
                       TourLogRepository tourLogRepository,
                       EventManager eventManager) {
        this.tourRepository = tourRepository;
        this.tourLogRepository = tourLogRepository;
        this.eventManager = eventManager;
        log.info("TourService initialized");
    }

    @Override
    public List<Tour> getAllTours() {
        log.debug("Fetching all tours");
        try {
            List<Tour> tours = tourRepository.findAll();
            log.debug("Fetched {} tours", tours.size());
            return tours;
        } catch (Exception e) {
            log.error("Error fetching tours", e);
            throw e;
        }
    }

    @Override
    public Tour createTour(Tour tour) {
        log.info("Creating new tour: {}", tour.getName());

        // Ensure required fields and uniqueness
        validateTour(tour);
        tourRepository.findByName(tour.getName()).ifPresent(t -> {
            String msg = "A tour with this name already exists: " + tour.getName();
            log.warn(msg);
            throw new IllegalArgumentException(msg);
        });

        try {
            Tour created = tourRepository.save(tour);
            log.info("Successfully created tour (id={}): {}", created.getId(), created.getName());

            // Notify system that tours changed
            eventManager.publish(Events.TOURS_CHANGED, created);

            return created;
        } catch (Exception e) {
            log.error("Failed to create tour: {}", tour.getName(), e);
            throw e;
        }
    }

    @Override
    public Tour updateTour(Tour tour) {
        log.info("Updating tour (id={}): {}", tour.getId(), tour.getName());

        if (tour.getId() == null) {
            String msg = "Cannot update tour without an ID";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }

        validateTour(tour);

        try {
            Tour updated = tourRepository.save(tour);
            log.info("Successfully updated tour (id={}): {}", updated.getId(), updated.getName());

            // Notify system that tours changed
            eventManager.publish(Events.TOURS_CHANGED, updated);

            return updated;
        } catch (Exception e) {
            log.error("Failed to update tour (id={}): {}", tour.getId(), tour.getName(), e);
            throw e;
        }
    }

    @Override
    public void deleteTour(Tour tour) {
        log.info("Deleting tour (id={}): {}", tour.getId(), tour.getName());

        try {
            tourRepository.delete(tour);
            log.info("Successfully deleted tour (id={}): {}", tour.getId(), tour.getName());

            // Notify system that tours changed
            eventManager.publish(Events.TOURS_CHANGED, tour);

        } catch (Exception e) {
            log.error("Failed to delete tour (id={}): {}", tour.getId(), tour.getName(), e);
            throw e;
        }
    }

    @Override
    public Optional<Tour> findById(Long id) {
        log.debug("Looking up tour by id={}", id);
        return tourRepository.findById(id);
    }

    @Override
    public Optional<Tour> findByName(String name) {
        log.debug("Looking up tour by name='{}'", name);
        return tourRepository.findByName(name);
    }

    // Ensures required fields are valid
    private void validateTour(Tour tour) {
        log.trace("Validating tour: {}", tour);
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