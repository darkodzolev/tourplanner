package at.technikum.javafx.viewmodel;

import at.technikum.javafx.service.ITourService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class MainViewModel {

    private final ITourService tourService;
    private final BooleanProperty darkMode = new SimpleBooleanProperty(false);

    public MainViewModel(ITourService tourService) {
        this.tourService = tourService;
    }

    public BooleanProperty darkModeProperty() {
        return darkMode;
    }

    public boolean isDarkMode() {
        return darkMode.get();
    }

    public void setDarkMode(boolean dark) {
        darkMode.set(dark);
    }
}