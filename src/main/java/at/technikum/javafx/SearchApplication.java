package at.technikum.javafx;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.ResourceBundle;

public class SearchApplication extends Application {

    private static HostServices hostServices;
    private static final Path leafletDir = Paths.get(
            System.getProperty("user.home"), ".tourplanner", "leaflet"
    );

    @Override
    public void start(Stage stage) throws IOException {
        hostServices = getHostServices();

        Files.createDirectories(leafletDir);
        try (InputStream in = getClass().getResourceAsStream("/leaflet/leaflet.html")) {
            if (in == null) {
                throw new IOException("Could not find /leaflet/leaflet.html on classpath");
            }
            Files.copy(in, leafletDir.resolve("leaflet.html"), StandardCopyOption.REPLACE_EXISTING);
        }

        // Add this in its place:
        Parent mainView = FXMLDependencyInjector.load("main-view.fxml", Locale.ENGLISH);

        // Show the scene
        Scene scene = new Scene(mainView);
        stage.setTitle("Tour Planner");
        stage.setScene(scene);
        stage.show();
    }

    public static void showMap() {
        URL htmlUrl = SearchApplication.class.getResource("/leaflet/leaflet.html");
        hostServices.showDocument(htmlUrl.toExternalForm());
    }

    public static void main(String[] args) {
        TourPlannerApplication.main(args);
    }
}