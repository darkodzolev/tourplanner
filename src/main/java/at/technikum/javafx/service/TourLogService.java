package at.technikum.javafx.service;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.repository.TourLogRepository;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TourLogService implements ITourLogService {

    private final TourLogRepository tourLogRepository;
    private final EventManager eventManager;

    public TourLogService(TourLogRepository tourLogRepository,
                          EventManager eventManager) {
        this.tourLogRepository = tourLogRepository;
        this.eventManager      = eventManager;
    }

    public List<TourLog> getAllLogs() {
        return tourLogRepository.findAll();
    }

    public List<TourLog> getLogsForTour(Tour tour) {
        if (tour == null || tour.getId() == null) {
            throw new IllegalArgumentException("Tour must be specified to retrieve logs");
        }
        return tourLogRepository.findByTour(tour);
    }

    public TourLog createLog(TourLog log) {
        validateLog(log, false);
        TourLog created = tourLogRepository.save(log);
        eventManager.publish(Events.TOUR_LOGS_CHANGED, created.getTour());
        return created;
    }

    public TourLog updateLog(TourLog log) {
        if (log.getId() == null) {
            throw new IllegalArgumentException("Cannot update a log without an ID");
        }
        validateLog(log, true);
        TourLog updated = tourLogRepository.save(log);
        eventManager.publish(Events.TOUR_LOGS_CHANGED, updated.getTour());
        return updated;
    }

    public void deleteLog(TourLog log) {
        if (log.getId() == null) {
            throw new IllegalArgumentException("Cannot delete a log without an ID");
        }
        tourLogRepository.delete(log);
        eventManager.publish(Events.TOUR_LOGS_CHANGED, log.getTour());
    }

    public Optional<TourLog> findById(Long id) {
        return tourLogRepository.findById(id);
    }

    private void validateLog(TourLog log, boolean isUpdate) {
        if (log == null) {
            throw new IllegalArgumentException("TourLog cannot be null");
        }
        if (log.getTour() == null || log.getTour().getId() == null) {
            throw new IllegalArgumentException("Associated tour must be specified");
        }
        if (log.getDateTime() == null) {
            throw new IllegalArgumentException("Date and time are required");
        }
        if (log.getComment() == null || log.getComment().isBlank()) {
            throw new IllegalArgumentException("Comment is required");
        }
        if (log.getDifficulty() == null || log.getDifficulty().isBlank()) {
            throw new IllegalArgumentException("Difficulty is required");
        }
        if (log.getTotalDistance() < 0) {
            throw new IllegalArgumentException("Total distance must be zero or positive");
        }
        if (log.getTotalTime() == null || log.getTotalTime().isBlank()) {
            throw new IllegalArgumentException("Total time is required");
        }
        int rating = log.getRating();
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}