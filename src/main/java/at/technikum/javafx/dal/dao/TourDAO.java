package at.technikum.javafx.dal.dao;

import at.technikum.javafx.entity.Tour;
import java.util.List;

public interface TourDAO {

    Tour findById(Long id);

    List<Tour> findAll();

    void save(Tour tour);

    void update(Tour tour);

    void delete(Tour tour);
}