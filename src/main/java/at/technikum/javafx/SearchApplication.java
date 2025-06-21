package at.technikum.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class SearchApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Parent mainView = FXMLDependencyInjector.load(
                "main-view.fxml",
                Locale.ENGLISH
        );
        Scene scene = new Scene(mainView);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
