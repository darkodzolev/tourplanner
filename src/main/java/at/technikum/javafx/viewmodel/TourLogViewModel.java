package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.service.ITourLogService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TourLogViewModel {

    private static final Logger log = LoggerFactory.getLogger(TourLogViewModel.class);

    private final ITourLogService tourLogService;
    private final EventManager eventManager;

    private final ObservableList<TourLog> logs = FXCollections.observableArrayList();
    private final ObjectProperty<TourLog> selectedLog = new SimpleObjectProperty<>();
    private final ObjectProperty<Tour> selectedTour = new SimpleObjectProperty<>();

    public TourLogViewModel(ITourLogService tourLogService, EventManager eventManager) {
        this.tourLogService = tourLogService;
        this.eventManager = eventManager;
        log.info("TourLogViewModel initialized");
    }

    public ObservableList<TourLog> getLogs() {
        return logs;
    }

    public ObjectProperty<TourLog> selectedLogProperty() {
        return selectedLog;
    }

    public ObjectProperty<Tour> selectedTourProperty() {
        return selectedTour;
    }

    public Tour getSelectedTour() {
        return selectedTour.get();
    }

    public void loadLogsForTour(Tour tour) {
        selectedTour.set(tour);
        logs.setAll(tourLogService.getLogsForTour(tour));
        log.info("Loaded {} logs for tour (id={})", logs.size(), tour.getId());
    }

    public void clearLogs() {
        selectedTour.set(null);
        logs.clear();
        log.info("Cleared logs list");
    }

    public void createLog(TourLog entry) {
        log.info("Creating log for tour (id={}): {}", entry.getTour().getId(), entry);
        try {
            TourLog created = tourLogService.createLog(entry);
            logs.add(created);
            log.info("Successfully created log (id={}) for tour (id={})",
                    created.getId(), created.getTour().getId());
            eventManager.publish(Events.TOUR_LOGS_CHANGED, created.getTour());
        } catch (Exception e) {
            log.error("Failed to create log for tour (id={}): {}", entry.getTour().getId(), entry, e);
        }
    }

    public void updateLog(TourLog entry) {
        log.info("Updating log (id={}) for tour (id={}): {}",
                entry.getId(), entry.getTour().getId(), entry);
        try {
            tourLogService.updateLog(entry);
            if (getSelectedTour() != null) {
                loadLogsForTour(getSelectedTour());
            }
            log.info("Successfully updated log (id={})", entry.getId());
            eventManager.publish(Events.TOUR_LOGS_CHANGED, entry.getTour());
        } catch (Exception e) {
            log.error("Failed to update log (id={}): {}", entry.getId(), entry, e);
        }
    }

    public void deleteLog(TourLog entry) {
        log.info("Deleting log (id={}) for tour (id={})",
                entry.getId(), entry.getTour().getId());
        try {
            tourLogService.deleteLog(entry);
            logs.remove(entry);
            log.info("Successfully deleted log (id={})", entry.getId());
            eventManager.publish(Events.TOUR_LOGS_CHANGED, entry.getTour());
        } catch (Exception e) {
            log.error("Failed to delete log (id={}): {}", entry.getId(), entry, e);
        }
    }
}