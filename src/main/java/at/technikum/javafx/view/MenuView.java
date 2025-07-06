package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.service.IReportService;
import at.technikum.javafx.viewmodel.MenuViewModel;
import at.technikum.javafx.viewmodel.TourViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;

import java.io.File;

public class MenuView implements Initializable {
    private final MenuViewModel  menuViewModel;
    private final TourViewModel  tourViewModel;
    private final IReportService reportService;
    private final TourRouteView  tourRouteView;

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
                    showException("Import error", ex);
                }
            }
        });

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
                    showException("Export error", ex);
                }
            }
        });

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
                reportService.generateTourReport(
                        selected,
                        tourRouteView.getMapView(),
                        f
                );
            } catch (Exception ex) {
                showException("Report error", ex);
            }
        });

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
                    showException("Report error", ex);
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

    private void showException(String title, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(ex.getMessage() != null ? ex.getMessage() : title);

        // Capture stack trace
        StringWriter sw = new StringWriter();
        ex.printStackTrace(new PrintWriter(sw));
        TextArea textArea = new TextArea(sw.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);

        // Expandable Exception area
        alert.getDialogPane().setExpandableContent(new TitledPane("Details", textArea));

        alert.showAndWait();
    }
}