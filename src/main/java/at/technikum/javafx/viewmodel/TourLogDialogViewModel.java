package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.TourLog;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class TourLogDialogViewModel {
    private final StringProperty comment = new SimpleStringProperty();
    private final StringProperty difficulty = new SimpleStringProperty();
    private final StringProperty distance = new SimpleStringProperty();
    private final StringProperty time = new SimpleStringProperty();
    private final IntegerProperty rating = new SimpleIntegerProperty(1);

    private final ObservableList<String> difficultyOptions =
            FXCollections.observableArrayList("Easy", "Medium", "Hard");

    public ObservableList<String> getDifficultyOptions() {
        return difficultyOptions;
    }
    public StringProperty commentProperty() { return comment; }
    public StringProperty difficultyProperty() { return difficulty; }
    public StringProperty distanceProperty() { return distance; }
    public StringProperty timeProperty() { return time; }
    public IntegerProperty ratingProperty() { return rating; }

    /** Prefill fields when editing */
    public void setLog(TourLog log) {
        comment.set(log.getComment());
        difficulty.set(log.getDifficulty());
        distance.set(Double.toString(log.getTotalDistance()));
        time.set(log.getTotalTime());
        rating.set(log.getRating());
    }
    /** Build a new TourLog from user input */
    public TourLog createLog() {
        TourLog log = new TourLog();
        log.setComment(comment.get());
        log.setDifficulty(difficulty.get());
        log.setTotalDistance(Double.parseDouble(distance.get()));
        log.setTotalTime(time.get());
        log.setRating(rating.get());
        return log;
    }
    /** Update an existing TourLog in place */
    public TourLog updateLog(TourLog existing) {
        existing.setComment(comment.get());
        existing.setDifficulty(difficulty.get());
        existing.setTotalDistance(Double.parseDouble(distance.get()));
        existing.setTotalTime(time.get());
        existing.setRating(rating.get());
        return existing;
    }
}