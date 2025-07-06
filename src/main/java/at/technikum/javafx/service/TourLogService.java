package at.technikum.javafx.service;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.repository.TourLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TourLogService implements ITourLogService {

    private static final Logger log = LoggerFactory.getLogger(TourLogService.class);

    private final TourLogRepository tourLogRepository;
    private final EventManager      eventManager;

    public TourLogService(TourLogRepository tourLogRepository,
                          EventManager eventManager) {
        this.tourLogRepository = tourLogRepository;
        this.eventManager      = eventManager;
        log.info("TourLogService initialized");
    }

    @Override
    public List<TourLog> getAllLogs() {
        log.debug("Fetching all tour logs");
        try {
            List<TourLog> logs = tourLogRepository.findAll();
            log.debug("Fetched {} tour logs", logs.size());
            return logs;
        } catch (Exception e) {
            log.error("Error fetching all tour logs", e);
            throw e;
        }
    }

    @Override
    public List<TourLog> getLogsForTour(Tour tour) {
        log.debug("Fetching logs for tour (id={})",
                tour != null ? tour.getId() : null);
        if (tour == null || tour.getId() == null) {
            String msg = "Tour must be specified to retrieve logs";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        try {
            List<TourLog> logs = tourLogRepository.findByTour(tour);
            log.debug("Fetched {} logs for tour (id={})", logs.size(), tour.getId());
            return logs;
        } catch (Exception e) {
            log.error("Error fetching logs for tour (id={})", tour.getId(), e);
            throw e;
        }
    }

    @Override
    public TourLog createLog(TourLog logEntry) {
        log.info("Creating tour log for tour (id={})",
                logEntry != null && logEntry.getTour() != null ? logEntry.getTour().getId() : null);
        validateLog(logEntry, false);
        try {
            TourLog created = tourLogRepository.save(logEntry);
            log.info("Successfully created tour log (id={})", created.getId());
            eventManager.publish(Events.TOUR_LOGS_CHANGED, created.getTour());
            return created;
        } catch (Exception e) {
            log.error("Failed to create tour log for tour (id={})",
                    logEntry.getTour().getId(), e);
            throw e;
        }
    }

    @Override
    public TourLog updateLog(TourLog logEntry) {
        log.info("Updating tour log (id={}) for tour (id={})",
                logEntry != null ? logEntry.getId() : null,
                logEntry != null && logEntry.getTour() != null ? logEntry.getTour().getId() : null);
        if (logEntry == null || logEntry.getId() == null) {
            String msg = "Cannot update a log without an ID";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        validateLog(logEntry, true);
        try {
            TourLog updated = tourLogRepository.save(logEntry);
            log.info("Successfully updated tour log (id={})", updated.getId());
            eventManager.publish(Events.TOUR_LOGS_CHANGED, updated.getTour());
            return updated;
        } catch (Exception e) {
            log.error("Failed to update tour log (id={})", logEntry.getId(), e);
            throw e;
        }
    }

    @Override
    public void deleteLog(TourLog logEntry) {
        log.info("Deleting tour log (id={}) for tour (id={})",
                logEntry != null ? logEntry.getId() : null,
                logEntry != null && logEntry.getTour() != null ? logEntry.getTour().getId() : null);
        if (logEntry == null || logEntry.getId() == null) {
            String msg = "Cannot delete a log without an ID";
            log.error(msg);
            throw new IllegalArgumentException(msg);
        }
        try {
            tourLogRepository.delete(logEntry);
            log.info("Successfully deleted tour log (id={})", logEntry.getId());
            eventManager.publish(Events.TOUR_LOGS_CHANGED, logEntry.getTour());
        } catch (Exception e) {
            log.error("Failed to delete tour log (id={})", logEntry.getId(), e);
            throw e;
        }
    }

    @Override
    public Optional<TourLog> findById(Long id) {
        log.debug("Looking up tour log by id={}", id);
        try {
            Optional<TourLog> result = tourLogRepository.findById(id);
            log.debug("Lookup by id={} returned {}", id, result.isPresent());
            return result;
        } catch (Exception e) {
            log.error("Error looking up tour log by id={}", id, e);
            throw e;
        }
    }

    private void validateLog(TourLog logEntry, boolean isUpdate) {
        log.trace("Validating TourLog (id={})", logEntry != null ? logEntry.getId() : null);
        if (logEntry == null) {
            throw new IllegalArgumentException("TourLog cannot be null");
        }
        if (logEntry.getTour() == null || logEntry.getTour().getId() == null) {
            throw new IllegalArgumentException("Associated tour must be specified");
        }
        if (logEntry.getDateTime() == null) {
            throw new IllegalArgumentException("Date and time are required");
        }
        if (logEntry.getComment() == null || logEntry.getComment().isBlank()) {
            throw new IllegalArgumentException("Comment is required");
        }
        if (logEntry.getDifficulty() == null || logEntry.getDifficulty().isBlank()) {
            throw new IllegalArgumentException("Difficulty is required");
        }
        if (logEntry.getTotalDistance() < 0) {
            throw new IllegalArgumentException("Total distance must be zero or positive");
        }
        if (logEntry.getTotalTime() == null || logEntry.getTotalTime().isBlank()) {
            throw new IllegalArgumentException("Total time is required");
        }
        int rating = logEntry.getRating();
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}