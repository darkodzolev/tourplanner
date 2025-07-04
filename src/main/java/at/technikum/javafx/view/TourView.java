package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.viewmodel.TourViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import at.technikum.javafx.service.GeocodeResult;
import at.technikum.javafx.service.RouteResult;

import java.util.function.Consumer;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import at.technikum.javafx.service.MapService;
import at.technikum.javafx.service.OrsService;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.web.WebEngine;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.collections.ListChangeListener;
import javafx.scene.control.SelectionMode;

public class TourView implements Initializable {
    private final TourViewModel viewModel;

    @FXML private ListView<Tour> tourList;
    @FXML private Button newButton, editButton, deleteButton;

    public TourView(TourViewModel viewModel) {
        this.viewModel = viewModel;
    }

    // for talking to ORS
    private final OrsService        orsService  = new OrsService();
    // for dumping the .js file
    private final MapService        mapService  = new MapService();
    // where we write our leaflet artifacts
    private final Path              leafletDir  = Paths.get(System.getProperty("user.home"), ".tourplanner", "leaflet");
    // this will get initialized in initialize(...)
    private WebEngine               mapEngine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tourList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Tour tour, boolean empty) {
                super.updateItem(tour, empty);
                setText(empty || tour == null ? "" : tour.getName());
            }
        });

        // bind the list and selection
        tourList.setItems(viewModel.getTours());
        viewModel.selectedTourProperty().bind(tourList.getSelectionModel().selectedItemProperty());

        tourList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        tourList.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Tour>) c -> {
            viewModel.getSelectedTours().setAll(tourList.getSelectionModel().getSelectedItems());
        });

        // handlers for the CRUD buttons
        newButton.setOnAction(e -> onNewTour());
        editButton.setOnAction(e -> onEditTour());
        deleteButton.setOnAction(e -> onDeleteTour());
    }

    @FXML
    private void onNewTour() {
        try {
            ResourceBundle i18n = ResourceBundle.getBundle(
                    "at.technikum.javafx.i18n_en", Locale.ENGLISH
            );
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/at/technikum/javafx/tour-dialog.fxml"),
                    i18n
            );
            DialogPane pane = loader.load();
            TourDialogView controller = loader.getController();

            Dialog<Tour> dialog = new Dialog<>();
            dialog.setTitle(i18n.getString("dialog.new.title"));
            dialog.setDialogPane(pane);
            dialog.setResultConverter(btn -> {
                if (btn != null && btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    return controller.getTourFromFields();
                }
                return null;
            });

            Optional<Tour> maybe = dialog.showAndWait();

            maybe.ifPresent(t -> computeRouteAndSave(t, viewModel::createTour));

        } catch (IOException ex) {
            showAlert("Error", "Unable to open New Tour dialog: " + ex.getMessage());
        }
    }

    @FXML
    private void onEditTour() {
        Tour selected = viewModel.selectedTourProperty().get();
        if (selected == null) {
            showAlert("Selection Error", "No tour selected to edit.");
            return;
        }

        try {
            ResourceBundle i18n = ResourceBundle.getBundle(
                    "at.technikum.javafx.i18n_en", Locale.ENGLISH
            );
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/at/technikum/javafx/tour-dialog.fxml"),
                    i18n
            );
            DialogPane pane = loader.load();
            TourDialogView controller = loader.getController();

            // prefill fields
            controller.setTour(selected);

            Dialog<Tour> dialog = new Dialog<>();
            dialog.setTitle(i18n.getString("dialog.edit.title"));  // add this key
            dialog.setDialogPane(pane);

            dialog.setResultConverter(btn -> {
                // only fire on your “Save” button (OK_DONE)
                if (btn != null && btn.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    // apply the changes onto the existing Tour
                    return controller.getUpdatedTour(selected);
                }
                return null;
            });

            Optional<Tour> maybe = dialog.showAndWait();
            maybe.ifPresent(updatedTour -> {
                // 1) persist the changes
                viewModel.updateTour(updatedTour);
                // 2) force a selection-change so TourRouteView.drawRoute(...) runs again
                tourList.getSelectionModel().clearSelection();
                tourList.getSelectionModel().select(updatedTour);
            });

        } catch (IOException ex) {
            showAlert("Error", "Unable to open Edit Tour dialog: " + ex.getMessage());
        }
    }

    @FXML
    private void onDeleteTour() {
        Tour sel = viewModel.selectedTourProperty().get();
        if (sel == null) {
            showAlert("Delete Error", "No tour selected to delete.");
            return;
        }
        Alert confirm = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Are you sure you want to delete this tour?",
                ButtonType.YES, ButtonType.NO
        );
        confirm.setHeaderText(null);
        confirm.setTitle("Confirm Delete");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                viewModel.deleteTour(sel);
            }
        });
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }

    private void computeRouteAndSave(Tour t, Consumer<Tour> saveAction) {
        try {
            GeocodeResult fromGeo = orsService.geocode(t.getFromLocation())
                    .orElseThrow(() -> new IllegalArgumentException("Could not geocode origin"));
            GeocodeResult toGeo = orsService.geocode(t.getToLocation())
                    .orElseThrow(() -> new IllegalArgumentException("Could not geocode destination"));

            RouteResult route = orsService.directions(
                    t.getTransportType().trim().toLowerCase(),
                    fromGeo.getLongitude(), fromGeo.getLatitude(),
                    toGeo.getLongitude(),   toGeo.getLatitude()
            ).orElseThrow(() -> new IllegalArgumentException("No route found"));

            t.setDistance(route.getDistance());
            t.setEstimatedTime(formatDuration(route.getDuration()));
            t.setRouteImagePath(leafletDir.resolve("directions.js").toString());

            saveAction.accept(t);
        } catch (Exception ex) {
            showAlert("Error", ex.getMessage());
        }
    }

    private String formatDuration(double seconds) {
        long secs = Math.round(seconds);
        long hrs  = secs / 3600;
        long min  = (secs % 3600) / 60;
        long sec  = secs % 60;
        return String.format("%02d:%02d:%02d", hrs, min, sec);
    }
}