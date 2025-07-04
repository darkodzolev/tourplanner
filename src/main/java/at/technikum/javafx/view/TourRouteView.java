package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.service.GeocodeResult;
import at.technikum.javafx.service.MapService;
import at.technikum.javafx.service.OrsService;
import at.technikum.javafx.service.RouteResult;
import at.technikum.javafx.viewmodel.TourViewModel;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ResourceBundle;

public class TourRouteView implements Initializable {
    @FXML private WebView mapView;
    @FXML private Label placeholderLabel;

    private WebEngine     mapEngine;
    private final OrsService  orsService = new OrsService();
    private final MapService  mapService = new MapService();
    private final Path        leafletDir = Paths.get(
            System.getProperty("user.home"), ".tourplanner", "leaflet"
    );

    public TourRouteView(TourViewModel tourVm) {
        // redraw or clear whenever selection changes
        tourVm.selectedTourProperty().addListener((obs, oldT, newT) -> {
            Platform.runLater(() -> {
                if (newT != null) {
                    placeholderLabel.setVisible(false);
                    drawRoute(newT);
                } else {
                    placeholderLabel.setVisible(true);
                    if (mapEngine != null) {
                        mapEngine.loadContent("");
                    }
                }
            });
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mapEngine = mapView.getEngine();
        // start with placeholder visible
        placeholderLabel.setVisible(true);

        try {
            Files.createDirectories(leafletDir);
        } catch (IOException ex) {
            throw new RuntimeException("Unable to create map cache dir", ex);
        }
    }

    private void drawRoute(Tour tour) {
        try {
            // 1) Geocode + routing
            GeocodeResult fromGeo = orsService.geocode(tour.getFromLocation())
                    .orElseThrow(() -> new IllegalArgumentException("Could not geocode origin"));
            GeocodeResult toGeo = orsService.geocode(tour.getToLocation())
                    .orElseThrow(() -> new IllegalArgumentException("Could not geocode destination"));

            RouteResult route = orsService.directions(
                    tour.getTransportType().trim().toLowerCase(),
                    fromGeo.getLongitude(), fromGeo.getLatitude(),
                    toGeo.getLongitude(),   toGeo.getLatitude()
            ).orElseThrow(() -> new IllegalArgumentException("No route found"));

            // 2) Write JS
            mapService.writeDirectionsJs(route, leafletDir);

            // 3) Cache-busted URL
            Path htmlPath = leafletDir.resolve("leaflet.html");
            String baseUrl = htmlPath.toUri().toString();
            String url = baseUrl + "?t=" + System.nanoTime();

            // 4) Listen for load-complete and invalidate twice
            ChangeListener<Worker.State> listener = new ChangeListener<>() {
                @Override
                public void changed(ObservableValue<? extends Worker.State> obs,
                                    Worker.State oldState, Worker.State newState) {
                    if (newState == Worker.State.SUCCEEDED) {
                        // immediate invalidate
                        mapEngine.executeScript("if(window.map) map.invalidateSize();");
                        // delayed invalidate (200ms)
                        mapEngine.executeScript(
                                "setTimeout(()=>{ if(window.map) map.invalidateSize(); },200);"
                        );
                        mapEngine.getLoadWorker()
                                .stateProperty()
                                .removeListener(this);
                    }
                }
            };
            mapEngine.getLoadWorker().stateProperty().addListener(listener);

            // 5) Load the map
            mapEngine.load(url);
            mapEngine.reload();

        } catch (Exception ex) {
            mapEngine.loadContent("");
            System.err.println("drawRoute error: " + ex.getMessage());
        }
    }
}