package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.service.ITourService;
import at.technikum.javafx.service.ITourLogService;
import javafx.collections.transformation.FilteredList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TourViewModelTest {

    @Mock ITourService tourService;
    @Mock ITourLogService logService;
    @Mock EventManager eventManager;

    private TourViewModel vm;
    private Tour t1, t2;
    private TourLog l1, l2;

    @BeforeEach
    void setUp() {
        t1 = new Tour(); t1.setId(1L); t1.setName("Alpha"); t1.setDescription("desc");
        t2 = new Tour(); t2.setId(2L); t2.setName("Beta");  t2.setDescription("other");
        when(tourService.getAllTours()).thenReturn(List.of(t1, t2));

        vm = new TourViewModel(tourService, logService, eventManager);

        l1 = new TourLog(); l1.setDifficulty("Easy"); l1.setTotalDistance(1.0); l1.setTotalTime("00:30");
        l2 = new TourLog(); l2.setDifficulty("Hard"); l2.setTotalDistance(3.0); l2.setTotalTime("01:00");
    }

    @Test
    void initialLoad_populatesTours() {
        var list = vm.getTours();
        assertEquals(2, list.size(), "Should load both tours from service");
        assertTrue(list.containsAll(List.of(t1, t2)));
    }

    @Test
    void selectingTour_updatesPopularityAndChildFriendliness() {
        when(logService.getLogsForTour(t1)).thenReturn(List.of(l1, l2));

        vm.selectedTourProperty().set(t1);

        assertEquals("2", vm.popularityProperty().get());

        String cfString = vm.childFriendlinessProperty().get().replace(',', '.');
        double cf = Double.parseDouble(cfString);
        assertFalse(Double.isNaN(cf), "CF should be a number");
        assertFalse(Double.isInfinite(cf), "CF should be finite");
    }

    @Test
    void createUpdateDelete_publishEvents() {
        Tour newTour = new Tour();
        newTour.setName("New");

        vm.createTour(newTour);
        verify(eventManager).publish(Events.TOURS_CHANGED, null);

        newTour.setId(99L);
        vm.updateTour(newTour);
        verify(eventManager, times(2)).publish(eq(Events.TOURS_CHANGED), isNull());

        vm.deleteTour(newTour);
        verify(eventManager, times(3)).publish(eq(Events.TOURS_CHANGED), isNull());
    }

    @Test
    void searchFilter_appliesNameFilter() {
        var realMgr = new EventManager();
        vm = new TourViewModel(tourService, logService, realMgr);

        realMgr.publish(Events.SEARCH_TERM_SELECTED, "alp");
        @SuppressWarnings("unchecked")
        var filtered = (FilteredList<Tour>) vm.getTours();

        assertEquals(1, filtered.size());
        assertEquals(t1, filtered.get(0));
    }
}