package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.viewmodel.TourDialogViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
        transportCombo.setItems(vm.getTransportOptions());
        nameField.textProperty().bindBidirectional(vm.nameProperty());
        descriptionField.textProperty().bindBidirectional(vm.descriptionProperty());
        fromField.textProperty().bindBidirectional(vm.fromLocationProperty());
        toField.textProperty().bindBidirectional(vm.toLocationProperty());
        transportCombo.valueProperty().bindBidirectional(vm.transportTypeProperty());
    }

    /** Pre-fill fields when editing */
    public void setTour(Tour tour) {
        vm.setTour(tour);
    }

    /** Build a new Tour from the dialog */
    public Tour getTourFromFields() {
        return vm.createTour();
    }

    /** Update an existing Tour from the dialog */
    public Tour getUpdatedTour(Tour existing) {
        return vm.updateTour(existing);
    }
}