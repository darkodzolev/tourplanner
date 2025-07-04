package at.technikum.javafx.viewmodel;

import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.service.TourLogService;
import at.technikum.javafx.service.TourService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.List;
import java.util.Collections;

public class MenuViewModel {
    private final TourService tourService;
    private final TourLogService tourLogService;
    private final EventManager eventManager;
    private final ObjectMapper mapper = new ObjectMapper();

    public MenuViewModel(TourService tourService, TourLogService tourLogService, EventManager eventManager) {
        this.tourService    = tourService;
        this.tourLogService = tourLogService;
        this.eventManager = eventManager;
    }

    public void importAllTours(File fromFile) throws Exception {
        Tour[] importedTours = mapper.readValue(fromFile, Tour[].class);
        for (Tour t : importedTours) {
            t.setId(null);
            Tour created = tourService.createTour(t);
            if (t.getLogs() != null) {
                for (TourLog log : t.getLogs()) {
                    log.setId(null);
                    log.setTour(created);
                    tourLogService.createLog(log);
                }
            }
        }
        eventManager.publish(Events.TOURS_CHANGED, null);
    }

    public void exportTours(List<Tour> tours, File toFile) throws Exception {
        tours.forEach(t -> t.setLogs(Collections.emptyList()));
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(toFile, tours);
    }

    public void exportAllTours(File toFile) throws Exception {
        List<Tour> tours = tourService.getAllTours();
        // eagerly fetch logs so they get serialized too
        tours.forEach(t -> t.setLogs(Collections.emptyList()));
        mapper.writerWithDefaultPrettyPrinter().writeValue(toFile, tours);
    }
}