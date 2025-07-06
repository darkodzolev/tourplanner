package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.viewmodel.TourDialogViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

public class TourDialogView implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextArea descriptionField;
    @FXML private TextField fromField;
    @FXML private TextField toField;
    @FXML private ComboBox<String> transportCombo;

    private final TourDialogViewModel vm = new TourDialogViewModel();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            transportCombo.setItems(vm.getTransportOptions());
            nameField.textProperty().bindBidirectional(vm.nameProperty());
            descriptionField.textProperty().bindBidirectional(vm.descriptionProperty());
            fromField.textProperty().bindBidirectional(vm.fromLocationProperty());
            toField.textProperty().bindBidirectional(vm.toLocationProperty());
            transportCombo.valueProperty().bindBidirectional(vm.transportTypeProperty());
        } catch (Exception ex) {
            showException("Tour dialog error", ex);
        }
    }

    public void setTour(Tour tour) {
        try {
            vm.setTour(tour);
        } catch (Exception ex) {
            showException("Error setting tour in dialog", ex);
        }
    }

    public Tour getTourFromFields() {
        try {
            return vm.createTour();
        } catch (Exception ex) {
            showException("Invalid tour data", ex);
            return null;
        }
    }

    public Tour getUpdatedTour(Tour existing) {
        try {
            return vm.updateTour(existing);
        } catch (Exception ex) {
            showException("Error updating tour", ex);
            return existing;
        }
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