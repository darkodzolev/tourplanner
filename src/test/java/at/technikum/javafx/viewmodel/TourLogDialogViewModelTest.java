package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.TourLog;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TourLogDialogViewModelTest {

    private TourLogDialogViewModel vm;

    @BeforeEach
    void setUp() {
        vm = new TourLogDialogViewModel();
    }

    @Test
    void difficultyOptions_areInitialized() {
        ObservableList<String> opts = vm.getDifficultyOptions();
        assertNotNull(opts);
        assertEquals(3, opts.size());
        assertTrue(opts.contains("Easy"));
        assertTrue(opts.contains("Medium"));
        assertTrue(opts.contains("Hard"));
    }

    @Test
    void setLog_populatesFields() {
        TourLog log = new TourLog();
        log.setId(42L);
        log.setComment("c");
        log.setDifficulty("Medium");
        log.setTotalDistance(12.34);
        log.setTotalTime("01:23");
        log.setRating(5);

        vm.setLog(log);

        assertEquals("c", vm.commentProperty().get());
        assertEquals("Medium", vm.difficultyProperty().get());
        assertEquals("12.34", vm.distanceProperty().get());
        assertEquals("01:23", vm.timeProperty().get());
        assertEquals(5, vm.ratingProperty().get());
    }

    @Test
    void createLog_parsesAndReturnsNewTourLog() {
        vm.commentProperty().set("abc");
        vm.difficultyProperty().set("Hard");
        vm.distanceProperty().set("7.5");
        vm.timeProperty().set("00:45");
        vm.ratingProperty().set(3);

        TourLog entry = vm.createLog();
        assertNull(entry.getId());
        assertEquals("abc", entry.getComment());
        assertEquals("Hard", entry.getDifficulty());
        assertEquals(7.5, entry.getTotalDistance(), 1e-6);
        assertEquals("00:45", entry.getTotalTime());
        assertEquals(3, entry.getRating());
    }

    @Test
    void updateLog_modifiesExistingTourLog() {
        TourLog existing = new TourLog();
        existing.setId(99L);
        existing.setComment("old");
        existing.setDifficulty("Easy");
        existing.setTotalDistance(1.0);
        existing.setTotalTime("00:10");
        existing.setRating(1);

        vm.commentProperty().set("newC");
        vm.difficultyProperty().set("Medium");
        vm.distanceProperty().set("2.5");
        vm.timeProperty().set("02:00");
        vm.ratingProperty().set(4);

        TourLog updated = vm.updateLog(existing);
        assertSame(existing, updated);
        assertEquals(99L, existing.getId());
        assertEquals("newC", existing.getComment());
        assertEquals("Medium", existing.getDifficulty());
        assertEquals(2.5, existing.getTotalDistance(), 1e-6);
        assertEquals("02:00", existing.getTotalTime());
        assertEquals(4, existing.getRating());
    }
}