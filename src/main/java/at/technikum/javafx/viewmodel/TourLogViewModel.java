package at.technikum.javafx.viewmodel;

import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.service.TourLogService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TourLogViewModel {

    private final TourLogService tourLogService;
    private final EventManager   eventManager;

    // Logs for the currently selected tour
    private final ObservableList<TourLog> logs = FXCollections.observableArrayList();

    // Currently selected log entry
    private final ObjectProperty<TourLog> selectedLog = new SimpleObjectProperty<>();

    // Currently selected tour (set by the view when tour changes)
    private final ObjectProperty<Tour> selectedTour = new SimpleObjectProperty<>();

    public TourLogViewModel(TourLogService tourLogService, EventManager eventManager) {
        this.tourLogService = tourLogService;
        this.eventManager   = eventManager;
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

    public void loadLogsForTour(Tour tour) {
        selectedTour.set(tour);
        logs.setAll(tourLogService.getLogsForTour(tour));
    }

    public void clearLogs() {
        selectedTour.set(null);
        logs.clear();
    }

    public Tour getSelectedTour() {
        return selectedTour.get();
    }

    public void createLog(TourLog log) {
        TourLog created = tourLogService.createLog(log);
        logs.add(created);
        // Notify that this Tour’s logs changed
        eventManager.publish(Events.TOUR_LOGS_CHANGED, created.getTour());
    }

    public void updateLog(TourLog log) {
        tourLogService.updateLog(log);
        if (getSelectedTour() != null) {
            loadLogsForTour(getSelectedTour());
        }
        eventManager.publish(Events.TOUR_LOGS_CHANGED, log.getTour());
    }

    public void deleteLog(TourLog log) {
        tourLogService.deleteLog(log);
        logs.remove(log);
        eventManager.publish(Events.TOUR_LOGS_CHANGED, log.getTour());
    }
}