package at.technikum.javafx;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class SearchApplication extends Application {

    private static final Logger log = LoggerFactory.getLogger(SearchApplication.class);
    private static HostServices hostServices;

    private static final Path leafletDir = Paths.get(
            System.getProperty("user.home"), ".tourplanner", "leaflet"
    );

    @Override
    public void start(Stage stage) {
        Path logsDir = Paths.get(System.getProperty("user.dir"), "logs");
        try {
            Files.createDirectories(logsDir);
        } catch (IOException e) {
            log.warn("Could not create logs directory", e);
        }

        log.info("JavaFX start() called");
        hostServices = getHostServices();

        try {
            Files.createDirectories(leafletDir);
            log.debug("Ensured leaflet directory exists at {}", leafletDir);

            try (InputStream in = getClass().getResourceAsStream("/leaflet/leaflet.html")) {
                if (in == null) {
                    throw new IOException("Could not find /leaflet/leaflet.html on classpath");
                }
                Files.copy(in, leafletDir.resolve("leaflet.html"), StandardCopyOption.REPLACE_EXISTING);
                log.info("Copied leaflet.html to {}", leafletDir);
            }
        } catch (IOException e) {
            log.error("Failed to prepare leaflet files", e);
        }

        try {
            Parent mainView = FXMLDependencyInjector.load("main-view.fxml", Locale.ENGLISH);
            Scene scene = new Scene(mainView);
            stage.setTitle("Tour Planner");
            stage.setScene(scene);
            stage.show();
            log.info("Main window displayed");
        } catch (Exception e) {
            log.error("Failed to load or display main view", e);
            throw new RuntimeException(e);
        }
    }

    public static void showMap() {
        URL htmlUrl = SearchApplication.class.getResource("/leaflet/leaflet.html");
        hostServices.showDocument(htmlUrl.toExternalForm());
    }

    public static void main(String[] args) {
        TourPlannerApplication.main(args);
    }
}