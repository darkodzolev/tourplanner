package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.service.IReportService;
import at.technikum.javafx.viewmodel.MenuViewModel;
import at.technikum.javafx.viewmodel.TourViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import javafx.scene.control.SingleSelectionModel;

public class MenuView implements Initializable {
    private final MenuViewModel menuViewModel;
    private final TourViewModel tourViewModel;
    private final IReportService reportService;
    private final TourRouteView tourRouteView;

    @FXML private MenuItem importMenuItem;
    @FXML private MenuItem exportMenuItem;
    @FXML private MenuItem singleReportItem;
    @FXML private MenuItem summaryReportItem;

    public MenuView(MenuViewModel menuViewModel,
                    TourViewModel tourViewModel,
                    IReportService reportService,
                    TourRouteView tourRouteView) {
        this.menuViewModel   = menuViewModel;
        this.tourViewModel   = tourViewModel;
        this.reportService   = reportService;
        this.tourRouteView   = tourRouteView;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Import tours
        importMenuItem.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Import Tours JSON");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );
            File f = chooser.showOpenDialog(null);
            if (f != null) {
                try {
                    menuViewModel.importAllTours(f);
                } catch (Exception ex) {
                    showAlert("Import error", ex.getMessage());
                }
            }
        });

        // Export tours
        exportMenuItem.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Export Tours JSON");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );
            File f = chooser.showSaveDialog(null);
            if (f != null) {
                try {
                    if (tourViewModel.getSelectedTours().isEmpty()) {
                        menuViewModel.exportAllTours(f);
                    } else {
                        menuViewModel.exportTours(tourViewModel.getSelectedTours(), f);
                    }
                } catch (Exception ex) {
                    showAlert("Export error", ex.getMessage());
                }
            }
        });

        // Generate single tour report
        singleReportItem.setOnAction(e -> {
            Tour selected = tourViewModel.selectedTourProperty().get();
            if (selected == null) {
                showAlert("No tour selected", "Please select a tour first.");
                return;
            }

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Tour Report");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            File f = chooser.showSaveDialog(null);
            if (f == null) return;

            try {
                // Directly snapshot the already-loaded mapView internally
                reportService.generateTourReport(
                        selected,
                        tourRouteView.getMapView(),
                        f
                );
            } catch (Exception ex) {
                showAlert("Report error", ex.getMessage());
            }
        });

        // Generate summary report
        summaryReportItem.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save Summary Report");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            File f = chooser.showSaveDialog(null);
            if (f != null) {
                try {
                    reportService.generateSummaryReport(
                            tourViewModel.getTours(),
                            f
                    );
                } catch (Exception ex) {
                    showAlert("Report error", ex.getMessage());
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}