package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.service.ITourLogService;
import at.technikum.javafx.service.ITourService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class TourViewModel {

    private static final Logger log = LoggerFactory.getLogger(TourViewModel.class);

    private final ITourService tourService;
    private final ITourLogService tourLogService;
    private final EventManager eventManager;

    private final StringProperty popularity = new SimpleStringProperty("0");
    private final StringProperty childFriendliness = new SimpleStringProperty("0.00");

    private final ObservableList<Tour> tours = FXCollections.observableArrayList();
    private final FilteredList<Tour> filteredTour = new FilteredList<>(tours, t -> true);
    private final ObjectProperty<Tour> selectedTour = new SimpleObjectProperty<>();
    private final ListProperty<Tour> selectedTours = new SimpleListProperty<>(FXCollections.observableArrayList());

    public TourViewModel(ITourService tourService, ITourLogService tourLogService, EventManager eventManager) {
        this.tourService = tourService;
        this.tourLogService = tourLogService;
        this.eventManager = eventManager;

        loadTours();
        log.info("TourViewModel initialized with {} tours", tours.size());

        eventManager.subscribe(Events.SEARCH_TERM_SELECTED, payload -> applyFilter((String) payload));
        eventManager.subscribe(Events.TOUR_LOGS_CHANGED, payload -> {
            if (payload instanceof Tour t && t.equals(selectedTour.get())) {
                updateMetricsFor(t);
            }
        });
        eventManager.subscribe(Events.TOURS_CHANGED, payload -> loadTours());

        selectedTourProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) updateMetricsFor(newT);
            else {
                popularity.set("0");
                childFriendliness.set("0.00");
            }
        });
    }

    public ObservableList<Tour> getTours() {
        return filteredTour;
    }

    public ObservableList<Tour> getSelectedTours() {
        return selectedTours.get();
    }

    public ObjectProperty<Tour> selectedTourProperty() {
        return selectedTour;
    }

    public StringProperty popularityProperty() {
        return popularity;
    }

    public StringProperty childFriendlinessProperty() {
        return childFriendliness;
    }

    public void createTour(Tour tour) {
        log.info("Creating new tour: {}", tour.getName());
        try {
            tourService.createTour(tour);
            log.info("Successfully created tour: {}", tour.getName());
            eventManager.publish(Events.TOURS_CHANGED, null);
        } catch (Exception e) {
            log.error("Failed to create tour: {}", tour.getName(), e);
        }
    }

    public void updateTour(Tour tour) {
        log.info("Updating tour (id={}): {}", tour.getId(), tour.getName());
        try {
            tourService.updateTour(tour);
            loadTours();
            log.info("Successfully updated tour: {}", tour.getName());
            eventManager.publish(Events.TOURS_CHANGED, null);
        } catch (Exception e) {
            log.error("Failed to update tour: {}", tour.getName(), e);
        }
    }

    public void deleteTour(Tour tour) {
        log.info("Deleting tour (id={}): {}", tour.getId(), tour.getName());
        try {
            tourService.deleteTour(tour);
            tours.remove(tour);
            log.info("Successfully deleted tour: {}", tour.getName());
            eventManager.publish(Events.TOURS_CHANGED, null);
        } catch (Exception e) {
            log.error("Failed to delete tour: {}", tour.getName(), e);
        }
    }

    private void loadTours() {
        tours.setAll(tourService.getAllTours());
    }

    private void updateMetricsFor(Tour t) {
        List<TourLog> logs = tourLogService.getLogsForTour(t);
        popularity.set(String.valueOf(logs.size()));
        childFriendliness.set(String.format("%.2f", computeChildFriendliness(logs)));
    }

    private void applyFilter(String term) {
        String lower = term == null ? "" : term.toLowerCase();
        filteredTour.setPredicate(tour -> {
            if (lower.isBlank()) return true;
            return containsIgnoreCase(tour.getName(), lower) ||
                    containsIgnoreCase(tour.getDescription(), lower) ||
                    containsIgnoreCase(tour.getFromLocation(), lower) ||
                    containsIgnoreCase(tour.getToLocation(), lower) ||
                    containsIgnoreCase(tour.getTransportType(), lower);
        });
    }

    private boolean containsIgnoreCase(String field, String term) {
        return field != null && field.toLowerCase().contains(term);
    }

    private double computeChildFriendliness(List<TourLog> logs) {
        if (logs.isEmpty()) return 0.0;

        double[] diffs = logs.stream()
                .mapToDouble(log -> switch (log.getDifficulty().toLowerCase()) {
                    case "easy" -> 1.0;
                    case "medium" -> 3.0;
                    case "hard" -> 5.0;
                    default -> Double.NaN;
                })
                .filter(d -> !Double.isNaN(d))
                .toArray();

        double diffScore = 0.5;
        if (diffs.length > 0) {
            double avg = Arrays.stream(diffs).average().orElse(3.0);
            diffScore = (5.0 - avg) / 4.0;
        }

        long[] times = logs.stream()
                .mapToLong(log -> safeParseSeconds(log.getTotalTime()))
                .filter(sec -> sec >= 0)
                .toArray();

        double timeScore = 0.5;
        if (times.length > 0) {
            double avgSeconds = Arrays.stream(times).average().orElse(3600.0);
            double hours = avgSeconds / 3600.0;
            timeScore = 1.0 / (hours + 1.0);
        }

        double[] kms = logs.stream()
                .mapToDouble(TourLog::getTotalDistance)
                .filter(d -> d >= 0)
                .toArray();

        double distScore = 0.5;
        if (kms.length > 0) {
            double avgKm = Arrays.stream(kms).average().orElse(1.0);
            distScore = 1.0 / (avgKm + 1.0);
        }

        double cf = (diffScore + timeScore + distScore) / 3.0;
        return Math.max(0.0, Math.min(cf, 1.0));
    }

    private long safeParseSeconds(String hms) {
        if (hms == null) return -1;
        String[] parts = hms.split(":");
        if (parts.length != 3) return -1;
        try {
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int s = Integer.parseInt(parts[2]);
            return h * 3600L + m * 60 + s;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}