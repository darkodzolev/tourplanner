package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TourDialogViewModel {
    private static final Logger log = LoggerFactory.getLogger(TourDialogViewModel.class);

    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty fromLocation = new SimpleStringProperty();
    private final StringProperty toLocation = new SimpleStringProperty();
    private final ObjectProperty<String> transportType = new SimpleObjectProperty<>();
    private final ObservableList<String> transportOptions = FXCollections.observableArrayList(
            "driving-car", "foot-walking", "cycling-regular"
    );

    public TourDialogViewModel() {
        log.info("TourDialogViewModel initialized");
    }

    public ObservableList<String> getTransportOptions() {
        return transportOptions;
    }

    public StringProperty nameProperty() {
        return name;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty fromLocationProperty() {
        return fromLocation;
    }

    public StringProperty toLocationProperty() {
        return toLocation;
    }

    public ObjectProperty<String> transportTypeProperty() {
        return transportType;
    }

    public void setTour(Tour t) {
        name.set(t.getName());
        description.set(t.getDescription());
        fromLocation.set(t.getFromLocation());
        toLocation.set(t.getToLocation());
        transportType.set(t.getTransportType());
        log.debug("TourDialogViewModel setTour: id={} name={}", t.getId(), t.getName());
    }

    public Tour createTour() {
        Tour t = new Tour();
        t.setName(name.get());
        t.setDescription(description.get());
        t.setFromLocation(fromLocation.get());
        t.setToLocation(toLocation.get());
        t.setTransportType(transportType.get());
        log.info("Created Tour from dialog: name='{}', from='{}', to='{}', transport='{}'",
                t.getName(), t.getFromLocation(), t.getToLocation(), t.getTransportType());
        return t;
    }

    public Tour updateTour(Tour existing) {
        existing.setName(name.get());
        existing.setDescription(description.get());
        existing.setFromLocation(fromLocation.get());
        existing.setToLocation(toLocation.get());
        existing.setTransportType(transportType.get());
        log.info("Updated Tour from dialog: id={}, name='{}'", existing.getId(), existing.getName());
        return existing;
    }
}