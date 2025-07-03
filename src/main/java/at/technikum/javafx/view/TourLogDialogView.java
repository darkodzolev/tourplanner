package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.viewmodel.TourLogDialogViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class TourLogDialogView implements Initializable {
    @FXML private TextArea commentField;
    @FXML private ComboBox<String> difficultyCombo;
    @FXML private TextField distanceField;
    @FXML private TextField timeField;
    @FXML private Spinner<Integer> ratingSpinner;

    private final TourLogDialogViewModel vm = new TourLogDialogViewModel();
    private Tour parentTour;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // populate options
        difficultyCombo.setItems(vm.getDifficultyOptions());
        ratingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 5, 1));
        ratingSpinner.setEditable(true);

        // bind UI ↔ VM
        commentField.textProperty().bindBidirectional(vm.commentProperty());
        difficultyCombo.valueProperty().bindBidirectional(vm.difficultyProperty());
        distanceField.textProperty().bindBidirectional(vm.distanceProperty());
        timeField.textProperty().bindBidirectional(vm.timeProperty());
        ratingSpinner.getValueFactory().valueProperty().bindBidirectional(vm.ratingProperty().asObject());
    }

    public void setParentTour(Tour tour) {
        this.parentTour = tour;
    }

    public void setLog(TourLog log) {
        vm.setLog(log);
    }

    public TourLog getLogFromFields() {
        TourLog log = vm.createLog();
        // attach tour
        if (parentTour != null) {
            log.setTour(parentTour);
        }
        // stamp the current date/time so validation passes
        log.setDateTime(LocalDateTime.now());
        return log;
    }

    public TourLog getUpdatedLog(TourLog existing) {
        return vm.updateLog(existing);
    }
}