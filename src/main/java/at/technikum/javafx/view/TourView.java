package at.technikum.javafx.view;

import at.technikum.javafx.SearchApplication;
import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.service.GeocodeResult;
import at.technikum.javafx.service.MapService;
import at.technikum.javafx.service.OrsService;
import at.technikum.javafx.service.RouteResult;
import at.technikum.javafx.viewmodel.TourViewModel;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class TourView implements Initializable {
    private final TourViewModel viewModel;

    private final OrsService orsService = new OrsService();
    private final MapService mapService = new MapService();
    private final Path leafletDir = Paths.get(System.getProperty("user.home"), ".tourplanner", "leaflet");

    public TourView(TourViewModel viewModel) {
        this.viewModel = viewModel;

        try {
            Files.createDirectories(leafletDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not create map folder: " + leafletDir, e);
        }
    }

    @FXML private ListView<Tour> tourList;
    @FXML private Button newButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private TextField fromField;
    @FXML private TextField toField;
    @FXML private ComboBox<String> transportField;
    @FXML private TextField distanceField;
    @FXML private TextField timeField;

    @FXML private WebView mapView;
    private WebEngine mapEngine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mapEngine = mapView.getEngine();

        // Bind the list of tours
        tourList.setItems(viewModel.getTours());

        // Sync selection
        viewModel.selectedTourProperty().bind(
                tourList.getSelectionModel().selectedItemProperty()
        );

        // When selection changes, populate the detail fields
        viewModel.selectedTourProperty().addListener(
                (ObservableValue<? extends Tour> obs, Tour oldTour, Tour newTour) -> {
                    if (newTour != null) {
                        nameField.setText(newTour.getName());
                        descField.setText(newTour.getDescription());
                        fromField.setText(newTour.getFromLocation());
                        toField.setText(newTour.getToLocation());
                        transportField.setValue(newTour.getTransportType());
                        distanceField.setText(String.valueOf(newTour.getDistance()));
                        timeField.setText(newTour.getEstimatedTime());
                    } else {
                        clearFields();
                    }
                }
        );

        // New Tour handler with validation
        newButton.setOnAction(e -> {
            try {
                if (nameField.getText().isBlank()) {
                    showAlert("Validation error", "Tour name is required");
                    return;
                }
                Tour t = new Tour();
                t.setName(nameField.getText());
                t.setDescription(descField.getText());
                t.setFromLocation(fromField.getText());
                t.setToLocation(toField.getText());
                t.setTransportType(transportField.getValue());
                t.setEstimatedTime(timeField.getText());

                GeocodeResult fromGeo = orsService.geocode(t.getFromLocation())
                        .orElseThrow(() -> new IllegalArgumentException("Could not geocode origin"));
                GeocodeResult toGeo   = orsService.geocode(t.getToLocation())
                        .orElseThrow(() -> new IllegalArgumentException("Could not geocode destination"));

                String rawProfile = transportField.getValue().trim().toLowerCase();
                String profile;
                switch (rawProfile) {
                    case "driving-car", "car", "auto" ->
                            profile = "driving-car";
                    case "foot-walking", "walking", "walk" ->
                            profile = "foot-walking";
                    case "cycling-regular", "bike", "bicycle" ->
                            profile = "cycling-regular";
                    default -> {
                        showAlert("Validation error",
                                "Transport must be one of: driving-car, foot-walking, cycling-regular");
                        return;  // abort the handler
                    }
                }

                RouteResult route = orsService.directions(
                        profile,
                        fromGeo.getLongitude(), fromGeo.getLatitude(),
                        toGeo.getLongitude(),   toGeo.getLatitude()
                ).orElseThrow(() -> new IllegalArgumentException("No route found"));

                try {
                    mapService.writeDirectionsJs(route, leafletDir);
                    String htmlUrl = leafletDir.resolve("leaflet.html").toUri().toString();
                    mapEngine.load(htmlUrl);
                } catch (IOException ioEx) {
                    showAlert("Map Error", "Could not generate or open the map: " + ioEx.getMessage());
                    return;  // bail out, don’t try to save the Tour
                }

                // Stamp your Tour and persist
                t.setDistance(route.getDistance());
                t.setEstimatedTime(formatDuration(route.getDuration()));
                t.setRouteImagePath(leafletDir.resolve("directions.js").toString());

                viewModel.createTour(t);

            } catch (IllegalArgumentException ex) {
                showAlert("Validation error", ex.getMessage());
            }
        });

        editButton.setOnAction(e -> {
            Tour t = viewModel.selectedTourProperty().get();
            if (t == null) {
                showAlert("Selection error", "No tour selected to edit.");
                return;
            }
            try {
                // 1) Basic validation
                if (nameField.getText().isBlank()) {
                    showAlert("Validation error", "Tour name is required");
                    return;
                }

                // 2) Geocode both ends
                GeocodeResult fromGeo = orsService.geocode(fromField.getText())
                        .orElseThrow(() -> new IllegalArgumentException("Could not geocode origin"));
                GeocodeResult toGeo   = orsService.geocode(toField.getText())
                        .orElseThrow(() -> new IllegalArgumentException("Could not geocode destination"));

                // 3) Get routing info
                RouteResult route = orsService.directions(
                        transportField.getValue(),
                        fromGeo.getLongitude(), fromGeo.getLatitude(),
                        toGeo.getLongitude(),   toGeo.getLatitude()
                ).orElseThrow(() -> new IllegalArgumentException("No route found"));

                // 4) Generate directions.js and open map
                try {
                    mapService.writeDirectionsJs(route, leafletDir);
                    String htmlUrl = leafletDir.resolve("leaflet.html").toUri().toString();
                    mapEngine.load(htmlUrl);
                } catch (IOException ioEx) {
                    showAlert("Map Error", "Could not generate or open the map: " + ioEx.getMessage());
                    return;
                }

                // 5) Update the Tour object
                t.setName(nameField.getText());
                t.setDescription(descField.getText());
                t.setFromLocation(fromField.getText());
                t.setToLocation(toField.getText());
                t.setTransportType(transportField.getValue());
                t.setDistance(route.getDistance());
                t.setEstimatedTime(formatDuration(route.getDuration()));
                t.setRouteImagePath(leafletDir.resolve("directions.js").toString());

                // 6) Persist changes
                viewModel.updateTour(t);

            } catch (IllegalArgumentException ex) {
                showAlert("Validation error", ex.getMessage());
            }
        });
    }

    private void clearFields() {
        nameField.clear();
        descField.clear();
        fromField.clear();
        toField.clear();
        transportField.setValue(null);
        distanceField.clear();
        timeField.clear();
        mapEngine.loadContent("");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }

    private String formatDuration(double seconds) {
        long secs = (long) Math.round(seconds);
        long hrs  = secs / 3600;
        long min  = (secs % 3600) / 60;
        long sec  = secs % 60;
        return String.format("%02d:%02d:%02d", hrs, min, sec);
    }
}