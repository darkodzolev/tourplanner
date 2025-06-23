package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.viewmodel.TourViewModel;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.net.URL;
import java.util.ResourceBundle;

public class TourView implements Initializable {

    private final TourViewModel viewModel;

    public TourView(TourViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML private ListView<Tour> tourList;
    @FXML private Button newButton;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    @FXML private TextField nameField;
    @FXML private TextArea descField;
    @FXML private TextField fromField;
    @FXML private TextField toField;
    @FXML private TextField transportField;
    @FXML private TextField distanceField;
    @FXML private TextField timeField;

    @FXML private ImageView mapView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
                        transportField.setText(newTour.getTransportType());
                        distanceField.setText(String.valueOf(newTour.getDistance()));
                        timeField.setText(newTour.getEstimatedTime());
                        mapView.setImage(
                                new Image("file:" + newTour.getRouteImagePath())
                        );
                    } else {
                        clearFields();
                    }
                }
        );

        // New Tour handler with validation
        newButton.setOnAction(e -> {
            try {
                double dist = Double.parseDouble(distanceField.getText());
                if (nameField.getText().isBlank()) {
                    showAlert("Validation error", "Tour name is required");
                    return;
                }
                Tour t = new Tour();
                t.setName(nameField.getText());
                t.setDescription(descField.getText());
                t.setFromLocation(fromField.getText());
                t.setToLocation(toField.getText());
                t.setTransportType(transportField.getText());
                t.setDistance(dist);
                t.setEstimatedTime(timeField.getText());
                // TODO: set routeImagePath
                try {
                    viewModel.createTour(t);
                } catch (IllegalArgumentException ex) {
                    showAlert("Validation error", ex.getMessage());
                }
            } catch (NumberFormatException ex) {
                showAlert("Invalid input", "Distance must be a valid number.");
            }
        });

        // Edit Tour handler with validation
        editButton.setOnAction(e -> {
            Tour t = viewModel.selectedTourProperty().get();
            if (t == null) {
                showAlert("Selection error", "No tour selected to edit.");
                return;
            }
            try {
                double dist = Double.parseDouble(distanceField.getText());
                if (nameField.getText().isBlank()) {
                    showAlert("Validation error", "Tour name is required");
                    return;
                }
                t.setName(nameField.getText());
                t.setDescription(descField.getText());
                t.setFromLocation(fromField.getText());
                t.setToLocation(toField.getText());
                t.setTransportType(transportField.getText());
                t.setDistance(dist);
                t.setEstimatedTime(timeField.getText());
                try {
                    viewModel.updateTour(t);
                } catch (IllegalArgumentException ex) {
                    showAlert("Validation error", ex.getMessage());
                }
            } catch (NumberFormatException ex) {
                showAlert("Invalid input", "Distance must be a valid number.");
            }
        });

        // Delete Tour handler
        deleteButton.setOnAction(e -> {
            Tour t = viewModel.selectedTourProperty().get();
            if (t != null) {
                viewModel.deleteTour(t);
                clearFields();
            } else {
                showAlert("Selection error", "No tour selected to delete.");
            }
        });
    }

    private void clearFields() {
        nameField.clear();
        descField.clear();
        fromField.clear();
        toField.clear();
        transportField.clear();
        distanceField.clear();
        timeField.clear();
        mapView.setImage(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.showAndWait();
    }
}