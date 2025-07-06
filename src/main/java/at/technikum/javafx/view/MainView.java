package at.technikum.javafx.view;

import at.technikum.javafx.viewmodel.MainViewModel;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.stage.Window;

import java.net.URL;
import java.util.ResourceBundle;

public class MainView implements Initializable {

    @FXML
    private ToggleButton themeToggle;

    private final MainViewModel viewModel;

    public MainView(MainViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        themeToggle.selectedProperty().bindBidirectional(viewModel.darkModeProperty());

        // Apply theme change to all open windows when toggled
        viewModel.darkModeProperty().addListener((obs, wasDark, isNowDark) -> {
            String sheet = isNowDark
                    ? "/at/technikum/javafx/css/dark-theme.css"
                    : "/at/technikum/javafx/css/light-theme.css";
            String urlForm = getClass().getResource(sheet).toExternalForm();

            for (Window w : Window.getWindows()) {
                Scene s = w.getScene();
                if (s != null) {
                    s.getStylesheets().setAll(urlForm);
                }
            }
        });

        // Apply theme to any newly opened windows (e.g. dialogs)
        Window.getWindows().addListener((ListChangeListener<Window>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    boolean dark = viewModel.isDarkMode();
                    String sheet = dark
                            ? "/at/technikum/javafx/css/dark-theme.css"
                            : "/at/technikum/javafx/css/light-theme.css";
                    String urlForm = getClass().getResource(sheet).toExternalForm();

                    for (Window w : change.getAddedSubList()) {
                        Scene s = w.getScene();
                        if (s != null) {
                            s.getStylesheets().setAll(urlForm);
                        }
                    }
                }
            }
        });

        // Initial theme setup after scene is shown
        Platform.runLater(() -> {
            boolean dark = viewModel.isDarkMode();
            String sheet = dark
                    ? "/at/technikum/javafx/css/dark-theme.css"
                    : "/at/technikum/javafx/css/light-theme.css";
            String urlForm = getClass().getResource(sheet).toExternalForm();

            for (Window w : Window.getWindows()) {
                Scene s = w.getScene();
                if (s != null) {
                    s.getStylesheets().setAll(urlForm);
                }
            }
        });
    }
}