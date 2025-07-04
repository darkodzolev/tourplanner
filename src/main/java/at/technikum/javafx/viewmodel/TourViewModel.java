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

    public TourViewModel(TourService tourService, TourLogService tourLogService, EventManager eventManager) {
        this.tourService = tourService;
        this.tourLogService = tourLogService;
        this.eventManager = eventManager;
        loadTours();
        eventManager.subscribe(Events.SEARCH_TERM_SELECTED, this::applyFilter);

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
        if (logs.isEmpty()) {
            return 0.0;
        }

        var validDiffs = logs.stream()
                .map(TourLog::getDifficulty)
                .flatMap(s -> {
                    try {
                        double d = Double.parseDouble(s);
                        return (d >= 1 && d <= 5) ? Stream.of(d) : Stream.empty();
                    } catch (NumberFormatException ex) {
                        return Stream.empty();
                    }
                })
                .mapToDouble(Double::doubleValue)
                .toArray();

        double diffScore;
        if (validDiffs.length == 0) {
            diffScore = 0.5;     // assume “medium” if no valid difficulties
        } else {
            double avgDiff = Arrays.stream(validDiffs).average().getAsDouble();
            diffScore = (5.0 - avgDiff) / 4.0;
        }

        var validTimes = logs.stream()
                .map(TourLog::getTotalTime)
                .flatMapToLong(s -> {
                    long secs = safeParseSeconds(s);    // returns -1 if invalid
                    return secs >= 0 ? LongStream.of(secs) : LongStream.empty();
                })
                .toArray();

        double timeScore;
        if (validTimes.length == 0) {
            timeScore = 0.5;     // medium default
        } else {
            double avgTime = Arrays.stream(validTimes).average().getAsDouble();
            timeScore = 1.0 / (avgTime / 3600.0 + 1.0);
        }

        var validDists = logs.stream()
                .mapToDouble(TourLog::getTotalDistance)
                .filter(d -> d >= 0)
                .toArray();

        double distScore;
        if (validDists.length == 0) {
            distScore = 0.5;    // medium default
        } else {
            double avgDist = Arrays.stream(validDists).average().getAsDouble();
            distScore = 1.0 / (avgDist / 1000.0 + 1.0);
        }

        // Average the three sub-scores
        double cf = (diffScore + timeScore + distScore) / 3.0;
        // clamp just in case
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
}