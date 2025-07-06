package at.technikum.javafx.viewmodel;

import at.technikum.javafx.service.ITourService;

public class MainViewModel {

    private final ITourService tourService;

    public MainViewModel(ITourService tourService) {
        this.tourService = tourService;
    }
}