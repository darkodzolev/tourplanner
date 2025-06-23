package at.technikum.javafx;

import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class SearchApplication extends Application {
    private static HostServices hostServices;

    @Override
    public void start(Stage stage) throws IOException {
        hostServices = getHostServices();
        Parent mainView = FXMLDependencyInjector.load(
                "main-view.fxml",
                Locale.ENGLISH
        );
        Scene scene = new Scene(mainView);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void showMap(Path leafletDir) {
        String url = leafletDir
            .resolve("leaflet.html")
            .toUri()
            .toString();
        hostServices.showDocument(url);
    }

    public static void main(String[] args) {
        launch();
    }
}