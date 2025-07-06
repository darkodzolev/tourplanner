package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.service.ITourService;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainViewModel {

    private static final Logger log = LoggerFactory.getLogger(MainViewModel.class);

    private final ITourService tourService;

    // list of all tours for the ListView
    private final ObservableList<Tour> tours = FXCollections.observableArrayList();

    // the currently-selected tour in the ListView
    private final ObjectProperty<Tour> selectedTour = new SimpleObjectProperty<>();

    public MainViewModel(ITourService tourService) {
        this.tourService = tourService;
        tours.setAll(tourService.getAllTours());
        log.info("MainViewModel initialized with {} tours", tours.size());
    }

    public ObservableList<Tour> getTours() {
        return tours;
    }

    public ObjectProperty<Tour> selectedTourProperty() {
        return selectedTour;
    }

    public void createTour(Tour tour) {
        log.info("MainViewModel: creating tour '{}'", tour.getName());
        try {
            Tour created = tourService.createTour(tour);
            tours.add(created);
            log.info("MainViewModel: tour created (id={}, name='{}')", created.getId(), created.getName());
        } catch (Exception e) {
            log.error("MainViewModel: failed to create tour '{}'", tour.getName(), e);
        }
    }

    public void updateTour(Tour tour) {
        log.info("MainViewModel: updating tour (id={}, name='{}')", tour.getId(), tour.getName());
        try {
            tourService.updateTour(tour);
            tours.setAll(tourService.getAllTours());
            log.info("MainViewModel: tour updated (id={}, name='{}')", tour.getId(), tour.getName());
        } catch (Exception e) {
            log.error("MainViewModel: failed to update tour (id={}, name='{}')", tour.getId(), tour.getName(), e);
        }
    }

    public void deleteTour(Tour tour) {
        log.info("MainViewModel: deleting tour (id={}, name='{}')", tour.getId(), tour.getName());
        try {
            tourService.deleteTour(tour);
            tours.remove(tour);
            log.info("MainViewModel: tour deleted (id={}, name='{}')", tour.getId(), tour.getName());
        } catch (Exception e) {
            log.error("MainViewModel: failed to delete tour (id={}, name='{}')", tour.getId(), tour.getName(), e);
        }
    }
}