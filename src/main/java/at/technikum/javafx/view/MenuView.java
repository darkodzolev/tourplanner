package at.technikum.javafx.view;

import at.technikum.javafx.viewmodel.MenuViewModel;
import at.technikum.javafx.viewmodel.TourViewModel;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MenuView implements Initializable {
    private final MenuViewModel menuViewModel;
    private final TourViewModel tourViewModel;

    @FXML private MenuItem importMenuItem;
    @FXML private MenuItem exportMenuItem;

    public MenuView(MenuViewModel menuViewModel, TourViewModel tourViewModel) {
        this.menuViewModel = menuViewModel;
        this.tourViewModel = tourViewModel;
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
                    showAlert("Import error", ex.getMessage());
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
            if (f == null) return;

            try {
                var sel = tourViewModel.getSelectedTours();
                if (sel.isEmpty()) {
                    menuViewModel.exportAllTours(f);
                } else {
                    menuViewModel.exportTours(sel, f);
                }
            } catch (Exception ex) {
                showAlert("Export error", ex.getMessage());
            }
        });
    }

    private void showAlert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.ERROR, m, ButtonType.OK);
        a.setTitle(t); a.setHeaderText(null);
        a.showAndWait();
    }
}