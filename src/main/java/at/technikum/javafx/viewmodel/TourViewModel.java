package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.service.TourService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class TourViewModel {

    private final TourService tourService;

    // all tours, backing the ListView
    private final ObservableList<Tour> tours = FXCollections.observableArrayList();

    // currently selected Tour
    private final ObjectProperty<Tour> selectedTour = new SimpleObjectProperty<>();

    private final FilteredList<Tour> filteredTour = new FilteredList<>(tours, t -> true);

    public TourViewModel(TourService tourService, EventManager eventManager) {
        this.tourService = tourService;
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

            // TODO: include computed attributes here too once you have them
            return false;
        });
    }
}