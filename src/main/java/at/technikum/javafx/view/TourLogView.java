package at.technikum.javafx.view;

import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.viewmodel.TourLogViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Region;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class TourLogView implements Initializable {
    private final TourLogViewModel viewModel;

    @FXML private ListView<TourLog> logList;
    @FXML private Button newLogButton;
    @FXML private Button editLogButton;
    @FXML private Button deleteLogButton;

    public TourLogView(TourLogViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        viewModel.selectedTourProperty().addListener((obs, oldTour, newTour) -> {
            if (newTour != null) {
                viewModel.loadLogsForTour(newTour);
            } else {
                viewModel.clearLogs();
            }
        });

        // bind the list of logs
        logList.setItems(viewModel.getLogs());

        // wire up the CRUD buttons…
        newLogButton.setOnAction(e -> openNewLogDialog());
        editLogButton.setOnAction(e -> openEditLogDialog());
        deleteLogButton.setOnAction(e -> handleDeleteLog());
    }

    @FXML
    private void openNewLogDialog() {
        try {
            ResourceBundle i18n = ResourceBundle.getBundle(
                    "at.technikum.javafx.i18n_en", Locale.ENGLISH
            );
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/at/technikum/javafx/tourlog-dialog.fxml"),
                    i18n
            );
            DialogPane pane = loader.load();
            TourLogDialogView dialogCtrl = loader.getController();

            // attach current Tour to the new log
            Tour parentTour = viewModel.getSelectedTour();
            dialogCtrl.setParentTour(parentTour);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(i18n.getString("log.dialog.new.title"));  // you can add this key or use a literal
            dialog.setDialogPane(pane);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.orElse(ButtonType.CANCEL) == ButtonType.OK) {
                TourLog newLog = dialogCtrl.getLogFromFields();
                viewModel.createLog(newLog);
            }
        } catch (IOException ex) {
            showAlert("Error", "Cannot open New Log dialog: " + ex.getMessage());
        }
    }

    @FXML
    private void openEditLogDialog() {
        TourLog selected = viewModel.selectedLogProperty().get();
        if (selected == null) {
            showAlert("Selection Error", "No log selected to edit.");
            return;
        }

        try {
            ResourceBundle i18n = ResourceBundle.getBundle(
                    "at.technikum.javafx.i18n_en", Locale.ENGLISH
            );
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/at/technikum/javafx/tourlog-dialog.fxml"),
                    i18n
            );
            DialogPane pane = loader.load();
            TourLogDialogView dialogCtrl = loader.getController();

            // prefill fields for editing
            dialogCtrl.setLog(selected);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(i18n.getString("log.dialog.edit.title"));  // add this key or use literal
            dialog.setDialogPane(pane);

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.orElse(ButtonType.CANCEL) == ButtonType.OK) {
                TourLog updated = dialogCtrl.getUpdatedLog(selected);
                viewModel.updateLog(updated);
            }
        } catch (IOException ex) {
            showAlert("Error", "Cannot open Edit Log dialog: " + ex.getMessage());
        }
    }

    @FXML
    private void handleDeleteLog() {
        TourLog selected = viewModel.selectedLogProperty().get();
        if (selected == null) {
            showAlert("Selection Error", "No log selected to delete.");
        } else {
            viewModel.deleteLog(selected);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        // Ensure the dialog resizes to fit content
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }
}