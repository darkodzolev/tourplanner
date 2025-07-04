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
        // 1) show meaningful text for each TourLog
        logList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(TourLog log, boolean empty) {
                super.updateItem(log, empty);
                if (empty || log == null) {
                    setText("");
                } else {
                    setText(String.format(
                            "%s — %s (%.1f km, %s)",
                            log.getDateTime().toLocalDate(),
                            log.getComment(),
                            log.getTotalDistance(),
                            log.getTotalTime()
                    ));
                }
            }
        });

        // 2) bind list items & selection
        logList.setItems(viewModel.getLogs());
        viewModel.selectedLogProperty()
                .bind(logList.getSelectionModel().selectedItemProperty());

        // 3) reload logs when tour changes
        viewModel.selectedTourProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) {
                viewModel.loadLogsForTour(newT);
            } else {
                viewModel.clearLogs();
            }
        });

        // 4) wire up buttons
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
            TourLogDialogView ctrl = loader.getController();

            // attach the currently selected Tour
            Tour parent = viewModel.getSelectedTour();
            ctrl.setParentTour(parent);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(i18n.getString("log.dialog.new.title"));
            dialog.setDialogPane(pane);

            // show and wait for button press
            Optional<ButtonType> res = dialog.showAndWait();
            if (res.isPresent() &&
                    res.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                TourLog log = ctrl.getLogFromFields();
                viewModel.createLog(log);
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
            TourLogDialogView ctrl = loader.getController();

            // prefill with the existing log
            ctrl.setLog(selected);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(i18n.getString("log.dialog.edit.title"));
            dialog.setDialogPane(pane);

            Optional<ButtonType> res = dialog.showAndWait();
            if (res.isPresent() &&
                    res.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                TourLog updated = ctrl.getUpdatedLog(selected);
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