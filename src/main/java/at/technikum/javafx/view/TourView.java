package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.service.GeocodeResult;
import at.technikum.javafx.service.MapService;
import at.technikum.javafx.service.OrsService;
import at.technikum.javafx.service.RouteResult;
import at.technikum.javafx.viewmodel.TourViewModel;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class TourView implements Initializable {

    private final TourViewModel viewModel;

    @FXML private ListView<Tour> tourList;
    @FXML private Button newButton, editButton, deleteButton;

    private final OrsService orsService = new OrsService();
    private final MapService mapService = new MapService();
    private final Path leafletDir = Paths.get(System.getProperty("user.home"), ".tourplanner", "leaflet");

    public TourView(TourViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            tourList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Tour tour, boolean empty) {
                    super.updateItem(tour, empty);
                    setText(empty || tour == null ? "" : tour.getName());
                }
            });

            tourList.setItems(viewModel.getTours());
            viewModel.selectedTourProperty().bind(tourList.getSelectionModel().selectedItemProperty());

            tourList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            tourList.getSelectionModel().getSelectedItems().addListener(
                    (ListChangeListener<Tour>) c ->
                            viewModel.getSelectedTours().setAll(tourList.getSelectionModel().getSelectedItems())
            );

            newButton.setOnAction(e -> onNewTour());
            editButton.setOnAction(e -> onEditTour());
            deleteButton.setOnAction(e -> onDeleteTour());

        } catch (Exception ex) {
            showException("Tour view initialization error", ex);
        }
    }

    @FXML
    private void onNewTour() {
        try {
            ResourceBundle i18n = ResourceBundle.getBundle("at.technikum.javafx.i18n_en", Locale.ENGLISH);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/at/technikum/javafx/tour-dialog.fxml"), i18n);
            DialogPane pane = loader.load();
            TourDialogView controller = loader.getController();

            Dialog<Tour> dialog = new Dialog<>();
            dialog.setTitle(i18n.getString("dialog.new.title"));
            dialog.setDialogPane(pane);
            dialog.setResultConverter(btn ->
                    (btn != null && btn.getButtonData() == ButtonData.OK_DONE)
                            ? controller.getTourFromFields()
                            : null
            );

            Optional<Tour> maybe = dialog.showAndWait();
            maybe.ifPresent(t -> computeRouteAndSave(t, viewModel::createTour));

        } catch (IOException ex) {
            showException("Cannot open New Tour dialog", ex);
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
            ResourceBundle i18n = ResourceBundle.getBundle("at.technikum.javafx.i18n_en", Locale.ENGLISH);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/at/technikum/javafx/tour-dialog.fxml"), i18n);
            DialogPane pane = loader.load();
            TourDialogView controller = loader.getController();
            controller.setTour(selected);

            Dialog<Tour> dialog = new Dialog<>();
            dialog.setTitle(i18n.getString("dialog.edit.title"));
            dialog.setDialogPane(pane);
            dialog.setResultConverter(btn ->
                    (btn != null && btn.getButtonData() == ButtonData.OK_DONE)
                            ? controller.getUpdatedTour(selected)
                            : null
            );

            Optional<Tour> maybe = dialog.showAndWait();
            maybe.ifPresent(updated -> {
                viewModel.updateTour(updated);
                tourList.getSelectionModel().clearSelection();
                tourList.getSelectionModel().select(updated);
            });

        } catch (IOException ex) {
            showException("Cannot open Edit Tour dialog", ex);
        }
    }

    @FXML
    private void onDeleteTour() {
        Tour sel = viewModel.selectedTourProperty().get();
        if (sel == null) {
            showAlert("Delete Error", "No tour selected to delete.");
            return;
        }

        try {
            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete this tour?",
                    ButtonType.YES, ButtonType.NO
            );
            confirm.setHeaderText(null);
            confirm.setTitle("Confirm Delete");

            Optional<ButtonType> resp = confirm.showAndWait();
            if (resp.isPresent() && resp.get() == ButtonType.YES) {
                viewModel.deleteTour(sel);
            }
        } catch (Exception ex) {
            showException("Error deleting tour", ex);
        }
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
                    toGeo.getLongitude(), toGeo.getLatitude()
            ).orElseThrow(() -> new IllegalArgumentException("No route found"));

            t.setDistance(route.getDistance());
            t.setEstimatedTime(formatDuration(route.getDuration()));
            saveAction.accept(t);
        } catch (Exception ex) {
            showException("Error computing route", ex);
        }
    }

    private String formatDuration(double seconds) {
        long secs = Math.round(seconds);
        long hrs = secs / 3600;
        long min = (secs % 3600) / 60;
        long sec = secs % 60;
        return String.format("%02d:%02d:%02d", hrs, min, sec);
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        a.setHeaderText(null);
        a.setTitle(title);
        a.showAndWait();
    }

    private void showException(String title, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(ex.getMessage() != null ? ex.getMessage() : title);

        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        alert.getDialogPane().setExpandableContent(new TitledPane("Details", textArea));
        alert.showAndWait();
    }
}