package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.service.TourService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MainViewModel {

    private final TourService tourService;

    // list of all tours for the ListView
    private final ObservableList<Tour> tours = FXCollections.observableArrayList();

    // the currently-selected tour in the ListView
    private final ObjectProperty<Tour> selectedTour = new SimpleObjectProperty<>();

    public MainViewModel(
            TourService tourService
    ) {
        this.tourService        = tourService;

        // load existing tours on startup
        tours.setAll(tourService.getAllTours());
    }

    public ObservableList<Tour> getTours() {
        return tours;
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
        // refresh list
        tours.setAll(tourService.getAllTours());
    }

    public void deleteTour(Tour tour) {
        tourService.deleteTour(tour);
        tours.remove(tour);
    }
}