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
        // Bind the toggle to the VM property
        themeToggle.selectedProperty().bindBidirectional(viewModel.darkModeProperty());

        // Whenever darkMode changes, reload stylesheets in every window
        viewModel.darkModeProperty().addListener((obs, wasDark, isNowDark) -> {
            String sheet = isNowDark
                    ? "/at/technikum/javafx/css/dark-theme.css"
                    : "/at/technikum/javafx/css/light-theme.css";
            String urlForm = getClass().getResource(sheet).toExternalForm();

            // apply to all open windows
            for (Window w : Window.getWindows()) {
                Scene s = w.getScene();
                if (s != null) {
                    s.getStylesheets().setAll(urlForm);
                }
            }
        });

        // Also catch any dialogs/new windows that open later
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

        // On startup (after the scene is ready), apply the current theme to all windows
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