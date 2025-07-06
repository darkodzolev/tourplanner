package at.technikum.javafx.service;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;

import java.util.List;
import java.util.Optional;

public interface ITourLogService {
    List<TourLog> getAllLogs();
    List<TourLog> getLogsForTour(Tour tour);
    TourLog createLog(TourLog log);
    TourLog updateLog(TourLog log);
    void deleteLog(TourLog log);
    Optional<TourLog> findById(Long id);
}