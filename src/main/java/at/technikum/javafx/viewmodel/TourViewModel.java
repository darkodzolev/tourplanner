package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.service.TourLogService;
import at.technikum.javafx.service.TourService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class TourViewModel {

    private final TourService tourService;
    private final TourLogService tourLogService;
    private final EventManager eventManager;

    private final StringProperty popularity = new SimpleStringProperty("0");
    private final StringProperty childFriendliness = new SimpleStringProperty("0.00");

    private final ObservableList<Tour> tours = FXCollections.observableArrayList();
    private final ObjectProperty<Tour> selectedTour = new SimpleObjectProperty<>();
    private final FilteredList<Tour> filteredTour = new FilteredList<>(tours, t -> true);
    private final ListProperty<Tour> selectedTours = new SimpleListProperty<>(FXCollections.observableArrayList());

    public TourViewModel(TourService tourService, TourLogService tourLogService, EventManager eventManager) {
        this.tourService = tourService;
        this.tourLogService = tourLogService;
        this.eventManager = eventManager;
        loadTours();
        eventManager.subscribe(Events.SEARCH_TERM_SELECTED, payload -> {
            applyFilter((String) payload);
        });

        selectedTourProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) {
                var logs = tourLogService.getLogsForTour(newT);
                popularity.set(String.valueOf(logs.size()));
                childFriendliness.set(
                        String.format("%.2f", computeChildFriendliness(logs))
                );
            } else {
                popularity.set("0");
                childFriendliness.set("0.00");
            }
        });

        eventManager.subscribe(Events.TOUR_LOGS_CHANGED, payload -> {
            if (!(payload instanceof Tour)) return;
            Tour t = (Tour) payload;
            // only if it’s the current tour
            if (t.equals(selectedTour.get())) {
                List<TourLog> logs = tourLogService.getLogsForTour(t);
                popularity.set(String.valueOf(logs.size()));
                childFriendliness.set(
                        String.format("%.2f", computeChildFriendliness(logs))
                );
            }
        });

        eventManager.subscribe(Events.TOURS_CHANGED, payload -> {
            loadTours();
        });
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

    public StringProperty popularityProperty() {
        return popularity;
    }

    public StringProperty childFriendlinessProperty() {
        return childFriendliness;
    }

    private int computePopularity(Tour tour) {
        return tour.getLogs().size();
    }

    private double computeChildFriendliness(List<TourLog> logs) {
        if (logs.isEmpty()) return 0.0;

        // 1) Difficulty → numeric
        double[] diffs = logs.stream()
                .mapToDouble(log -> {
                    return switch (log.getDifficulty().toLowerCase()) {
                        case "easy"   -> 1.0;
                        case "medium" -> 3.0;
                        case "hard"   -> 5.0;
                        default       -> Double.NaN;
                    };
                })
                .filter(d -> !Double.isNaN(d))
                .toArray();

        double diffScore = 0.5;
        if (diffs.length > 0) {
            double avg = Arrays.stream(diffs).average().orElse(3.0);
            diffScore = (5.0 - avg) / 4.0;
        }

        // 2) Time → score = 1/(hours+1)
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

        // 3) Distance in *km* → score = 1/(km+1)
        double[] kms = logs.stream()
                .mapToDouble(TourLog::getTotalDistance)
                .filter(d -> d >= 0)
                .toArray();

        double distScore = 0.5;
        if (kms.length > 0) {
            double avgKm = Arrays.stream(kms).average().orElse(1.0);
            distScore = 1.0 / (avgKm + 1.0);
        }

        // 4) Final child-friendliness: average of the three
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
            return (long)h * 3600 + m * 60 + s;
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private void applyFilter(String term) {
        String lower = term == null ? "" : term.toLowerCase();
        filteredTour.setPredicate(tour -> {
            if (lower.isBlank()) return true;

            // null-safe, case-insensitive contains
            if (containsIgnoreCase(tour.getName(),          lower)) return true;
            if (containsIgnoreCase(tour.getDescription(),   lower)) return true;
            if (containsIgnoreCase(tour.getFromLocation(),  lower)) return true;
            if (containsIgnoreCase(tour.getToLocation(),    lower)) return true;
            if (containsIgnoreCase(tour.getTransportType(), lower)) return true;

            return false;
        });
    }

    private boolean containsIgnoreCase(String field, String term) {
        return field != null && field.toLowerCase().contains(term);
    }

    public ObservableList<Tour> getSelectedTours() {
        return selectedTours.get();
    }
}