package at.technikum.javafx.repository;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourLogRepository extends JpaRepository<TourLog, Long> {
    List<TourLog> findByTour(Tour tour);
}