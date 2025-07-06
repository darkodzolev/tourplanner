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
import java.time.format.DateTimeFormatter;
import java.io.PrintWriter;
import java.io.StringWriter;

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
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            logList.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(TourLog log, boolean empty) {
                    super.updateItem(log, empty);
                    if (empty || log == null) {
                        setText("");
                    } else {
                        setText(String.format(
                                "%s — %s (%.1f km, %s)",
                                log.getDateTime().format(dtf),
                                log.getComment(),
                                log.getTotalDistance(),
                                log.getTotalTime()
                        ));
                    }
                }
            });

            logList.setItems(viewModel.getLogs());
            viewModel.selectedLogProperty().bind(logList.getSelectionModel().selectedItemProperty());
            viewModel.selectedTourProperty().addListener((obs, oldT, newT) -> {
                if (newT != null) viewModel.loadLogsForTour(newT);
                else viewModel.clearLogs();
            });

            newLogButton.setOnAction(e -> openNewLogDialog());
            editLogButton.setOnAction(e -> openEditLogDialog());
            deleteLogButton.setOnAction(e -> handleDeleteLog());
        } catch (Exception ex) {
            showException("Error initializing Tour Log view", ex);
        }
    }

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
            Tour parent = viewModel.getSelectedTour();
            ctrl.setParentTour(parent);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(i18n.getString("log.dialog.new.title"));
            dialog.setDialogPane(pane);

            Optional<ButtonType> res = dialog.showAndWait();
            if (res.isPresent() && res.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                TourLog log = ctrl.getLogFromFields();
                if (log != null) viewModel.createLog(log);
            }
        } catch (IOException ex) {
            showException("Cannot open New Log dialog", ex);
        }
    }

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
            ctrl.setLog(selected);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle(i18n.getString("log.dialog.edit.title"));
            dialog.setDialogPane(pane);

            Optional<ButtonType> res = dialog.showAndWait();
            if (res.isPresent() && res.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                TourLog updated = ctrl.getUpdatedLog(selected);
                viewModel.updateLog(updated);
            }
        } catch (IOException ex) {
            showException("Cannot open Edit Log dialog", ex);
        }
    }

    private void handleDeleteLog() {
        TourLog selected = viewModel.selectedLogProperty().get();
        if (selected == null) {
            showAlert("Selection Error", "No log selected to delete.");
            return;
        }
        try {
            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete this log?",
                    ButtonType.YES, ButtonType.NO
            );
            confirm.setHeaderText(null);
            confirm.setTitle("Confirm Delete");
            Optional<ButtonType> resp = confirm.showAndWait();
            if (resp.isPresent() && resp.get() == ButtonType.YES) {
                viewModel.deleteLog(selected);
            }
        } catch (Exception ex) {
            showException("Error deleting log", ex);
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
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