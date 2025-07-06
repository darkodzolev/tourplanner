package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.service.ITourLogService;
import at.technikum.javafx.service.ITourService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class MenuViewModel {
    private static final Logger log = LoggerFactory.getLogger(MenuViewModel.class);

    private final ITourService tourService;
    private final ITourLogService tourLogService;
    private final EventManager eventManager;
    private final ObjectMapper mapper = new ObjectMapper();

    public MenuViewModel(ITourService tourService,
                         ITourLogService tourLogService,
                         EventManager eventManager) {
        this.tourService    = tourService;
        this.tourLogService = tourLogService;
        this.eventManager   = eventManager;
        log.info("MenuViewModel initialized");
    }

    public void importAllTours(File fromFile) throws Exception {
        log.info("Importing all tours from '{}'", fromFile.getAbsolutePath());
        try {
            Tour[] importedTours = mapper.readValue(fromFile, Tour[].class);
            int count = importedTours.length;
            for (Tour t : importedTours) {
                t.setId(null);
                Tour created = tourService.createTour(t);
                if (t.getLogs() != null) {
                    for (TourLog logEntry : t.getLogs()) {
                        logEntry.setId(null);
                        logEntry.setTour(created);
                        tourLogService.createLog(logEntry);
                    }
                }
            }
            eventManager.publish(Events.TOURS_CHANGED, null);
            log.info("Successfully imported {} tours from '{}'", count, fromFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to import tours from '{}'", fromFile.getAbsolutePath(), e);
            throw e;
        }
    }

    public void exportTours(List<Tour> tours, File toFile) throws Exception {
        log.info("Exporting {} tours to '{}'", tours.size(), toFile.getAbsolutePath());
        try {
            tours.forEach(t -> t.setLogs(Collections.emptyList()));
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(toFile, tours);
            log.info("Successfully exported {} tours to '{}'", tours.size(), toFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to export tours to '{}'", toFile.getAbsolutePath(), e);
            throw e;
        }
    }

    public void exportAllTours(File toFile) throws Exception {
        log.info("Exporting all tours to '{}'", toFile.getAbsolutePath());
        try {
            List<Tour> all = tourService.getAllTours();
            all.forEach(t -> t.setLogs(Collections.emptyList()));
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(toFile, all);
            log.info("Successfully exported all tours (count={}) to '{}'", all.size(), toFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to export all tours to '{}'", toFile.getAbsolutePath(), e);
            throw e;
        }
    }
}