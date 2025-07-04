package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.viewmodel.TourViewModel;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.util.ResourceBundle;

public class TourGeneralView implements Initializable {
    @FXML private StackPane container;
    @FXML private Label placeholder;

    @FXML private GridPane detailsGrid;
    @FXML private Label nameValue, descValue, fromValue, toValue;
    @FXML private Label transValue, distValue, timeValue;
    @FXML private Label popValue, cfValue;

    private final TourViewModel tourVm;

    public TourGeneralView(TourViewModel tourVm) {
        this.tourVm = tourVm;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Listen for tour selection changes:
        ChangeListener<Tour> listener = (obs, oldT, newT) -> {
            if (newT == null) {
                placeholder.setVisible(true);
                detailsGrid.setVisible(false);
            } else {
                placeholder.setVisible(false);
                detailsGrid.setVisible(true);

                nameValue.setText(newT.getName());
                descValue.setText(newT.getDescription());
                fromValue.setText(newT.getFromLocation());
                toValue.setText(newT.getToLocation());
                transValue.setText(friendlyTransport(newT.getTransportType()));
                distValue.setText(String.format("%.1f km", newT.getDistance()/1000.0));
                timeValue.setText(formatFriendlyTime(newT.getEstimatedTime()));

                // popularity and child-friendly come from the VM
                popValue.textProperty().bind(tourVm.popularityProperty());
                cfValue.textProperty().bind(tourVm.childFriendlinessProperty());
            }
        };

        // attach it
        tourVm.selectedTourProperty().addListener(listener);
        // also run once for initial state
        listener.changed(null, null, tourVm.selectedTourProperty().get());
    }

    private String formatFriendlyTime(String hhmmss) {
        // hh:mm:ss → “X hours Y minutes” or “Y minutes”
        String[] p = hhmmss.split(":");
        int h = Integer.parseInt(p[0]);
        int m = Integer.parseInt(p[1]);
        if (h > 0) {
            return String.format("%d hour%s %d minute%s",
                    h, (h==1?"":"s"),
                    m, (m==1?"":"s"));
        } else {
            return String.format("%d minute%s", m, (m==1?"":"s"));
        }
    }

    private String friendlyTransport(String code) {
        return switch (code) {
            case "driving-car"    -> "Car";
            case "foot-walking"   -> "Walking";
            case "cycling-regular"-> "Bicycle";
            default                -> code;  // fallback
        };
    }
}