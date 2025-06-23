package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.service.TourLogService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TourLogViewModel {

    private final TourLogService tourLogService;

    // Logs for the currently selected tour
    private final ObservableList<TourLog> logs = FXCollections.observableArrayList();

    // Currently selected log entry
    private final ObjectProperty<TourLog> selectedLog = new SimpleObjectProperty<>();

    // Currently selected tour (set by the view when tour changes)
    private final ObjectProperty<Tour> selectedTour = new SimpleObjectProperty<>();

    public TourLogViewModel(TourLogService tourLogService) {
        this.tourLogService = tourLogService;
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
    }

    public void updateLog(TourLog log) {
        tourLogService.updateLog(log);
        if (getSelectedTour() != null) {
            loadLogsForTour(getSelectedTour());
        }
    }

    public void deleteLog(TourLog log) {
        tourLogService.deleteLog(log);
        logs.remove(log);
    }
}