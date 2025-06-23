package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.service.TourService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TourViewModel {

    private final TourService tourService;

    // all tours, backing the ListView
    private final ObservableList<Tour> tours = FXCollections.observableArrayList();

    // currently selected Tour
    private final ObjectProperty<Tour> selectedTour = new SimpleObjectProperty<>();

    public TourViewModel(TourService tourService) {
        this.tourService = tourService;
        loadTours();
    }

    private void loadTours() {
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
        loadTours();
    }

    public void deleteTour(Tour tour) {
        tourService.deleteTour(tour);
        tours.remove(tour);
    }
}