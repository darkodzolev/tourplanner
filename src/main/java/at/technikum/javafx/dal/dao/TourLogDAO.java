package at.technikum.javafx.dal.dao;

import at.technikum.javafx.entity.TourLog;
import java.util.List;

public interface TourLogDAO {

    TourLog findById(Long id);

    List<TourLog> findAll();

    List<TourLog> findByTourId(Long tourId);

    void save(TourLog tourLog);

    void update(TourLog tourLog);

    void delete(TourLog tourLog);
}