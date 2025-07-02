package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.service.TourLogService;
import at.technikum.javafx.service.TourService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class TourViewModel {

    private final TourService tourService;
    private final TourLogService tourLogService;
    private final EventManager eventManager;

    // all tours, backing the ListView
    private final ObservableList<Tour> tours = FXCollections.observableArrayList();

    // currently selected Tour
    private final ObjectProperty<Tour> selectedTour = new SimpleObjectProperty<>();

    private final FilteredList<Tour> filteredTour = new FilteredList<>(tours, t -> true);

    public TourViewModel(TourService tourService, TourLogService tourLogService, EventManager eventManager) {
        this.tourService = tourService;
        this.tourLogService = tourLogService;
        this.eventManager = eventManager;
        loadTours();
        eventManager.subscribe(Events.SEARCH_TERM_SELECTED, this::applyFilter);
    }

    private void loadTours() {
        tours.setAll(tourService.getAllTours());
    }

    public ObservableList<Tour> getTours() {
        return filteredTour;
    }

    public ObjectProperty<Tour> selectedTourProperty() {
        return selectedTour;
    }

    public void createTour(Tour tour) {
        Tour created = tourService.createTour(tour);
        tours.add(created);
    }

    public void updateTour(Tour tour) {
        tourService.updateTour(tour);
        loadTours();
    }

    public void deleteTour(Tour tour) {
        tourService.deleteTour(tour);
        tours.remove(tour);
    }

    private int computePopularity(Tour tour) {
        return tour.getLogs().size();
    }

    private double computeChildFriendliness(Tour tour) {
        var logs = tour.getLogs();
        if (logs.isEmpty()) return 0.0;

        // parse difficulty (string) into a double
        double avgDiff = logs.stream()
                .mapToDouble(l -> {
                    try {
                        return Double.parseDouble(l.getDifficulty());
                    } catch (NumberFormatException ex) {
                        return 0.0; // or some default
                    }
                })
                .average().orElse(0.0);

        // avg total time in seconds
        double avgTime = logs.stream()
                .mapToDouble(l -> parseSeconds(l.getTotalTime()))
                .average().orElse(0.0);

        // avg total distance
        double avgDist = logs.stream()
                .mapToDouble(TourLog::getTotalDistance)
                .average().orElse(0.0);

        // normalize each metric to 0–1
        double diffScore = (5.0 - avgDiff) / 4.0;               // 1.0 if avgDiff=1, 0.0 if avgDiff=5
        double timeScore = 1.0 / (avgTime / 3600.0 + 1.0);      // 1.0 if instant, ↓ as time grows
        double distScore = 1.0 / (avgDist / 1000.0 + 1.0);      // 1.0 if zero km, ↓ as distance grows

        // final score is the average
        return (diffScore + timeScore + distScore) / 3.0;
    }

    private long parseSeconds(String hms) {
        String[] p = hms.split(":");
        return Integer.parseInt(p[0]) * 3600
                + Integer.parseInt(p[1]) * 60
                + Integer.parseInt(p[2]);
    }

    private void applyFilter(String term) {
        String lower = term == null ? "" : term.toLowerCase();
        filteredTour.setPredicate(tour -> {
            if (lower.isBlank()) return true;

            // full-text check across all fields
            if (tour.getName().toLowerCase().contains(lower)) return true;
            if (tour.getDescription().toLowerCase().contains(lower)) return true;
            if (tour.getFromLocation().toLowerCase().contains(lower)) return true;
            if (tour.getToLocation().toLowerCase().contains(lower)) return true;
            if (tour.getTransportType().toLowerCase().contains(lower)) return true;

            if (String.valueOf(computePopularity(tour)).contains(lower)) {
                return true;
            }

            String cf = String.format("%.2f", computeChildFriendliness(tour));
            if (cf.contains(lower)) {
                return true;
            }

            return false;
        });
    }
}