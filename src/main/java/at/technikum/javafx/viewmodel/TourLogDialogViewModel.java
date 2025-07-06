package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.TourLog;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TourLogDialogViewModel {
    private static final Logger log = LoggerFactory.getLogger(TourLogDialogViewModel.class);

    private final StringProperty comment = new SimpleStringProperty();
    private final StringProperty difficulty = new SimpleStringProperty();
    private final StringProperty distance = new SimpleStringProperty();
    private final StringProperty time = new SimpleStringProperty();
    private final IntegerProperty rating = new SimpleIntegerProperty(1);

    private final ObservableList<String> difficultyOptions =
            FXCollections.observableArrayList("Easy", "Medium", "Hard");

    public TourLogDialogViewModel() {
        log.info("TourLogDialogViewModel initialized");
    }

    public ObservableList<String> getDifficultyOptions() {
        return difficultyOptions;
    }

    public StringProperty commentProperty() {
        return comment;
    }

    public StringProperty difficultyProperty() {
        return difficulty;
    }

    public StringProperty distanceProperty() {
        return distance;
    }

    public StringProperty timeProperty() {
        return time;
    }

    public IntegerProperty ratingProperty() {
        return rating;
    }

    public void setLog(TourLog logEntry) {
        comment.set(logEntry.getComment());
        difficulty.set(logEntry.getDifficulty());
        distance.set(Double.toString(logEntry.getTotalDistance()));
        time.set(logEntry.getTotalTime());
        rating.set(logEntry.getRating());
        log.debug("TourLogDialogViewModel setLog: id={}, comment='{}'",
                logEntry.getId(), logEntry.getComment());
    }

    public TourLog createLog() {
        TourLog entry = new TourLog();
        entry.setComment(comment.get());
        entry.setDifficulty(difficulty.get());
        entry.setTotalDistance(Double.parseDouble(distance.get()));
        entry.setTotalTime(time.get());
        entry.setRating(rating.get());
        log.info("Created TourLog from dialog: tourId=?, difficulty='{}', rating={}",
                entry.getDifficulty(), entry.getRating());
        return entry;
    }

    public TourLog updateLog(TourLog existing) {
        existing.setComment(comment.get());
        existing.setDifficulty(difficulty.get());
        existing.setTotalDistance(Double.parseDouble(distance.get()));
        existing.setTotalTime(time.get());
        existing.setRating(rating.get());
        log.info("Updated TourLog from dialog: id={}, newComment='{}', newRating={}",
                existing.getId(), existing.getComment(), existing.getRating());
        return existing;
    }
}