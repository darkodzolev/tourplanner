package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.service.ITourLogService;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TourLogViewModelTest {

    @Mock ITourLogService logService;
    @Mock EventManager eventManager;
    @InjectMocks TourLogViewModel vm;

    private Tour sampleTour;
    private TourLog log1, log2;

    @BeforeEach
    void setUp() {
        sampleTour = new Tour();
        sampleTour.setId(10L);

        log1 = new TourLog(); log1.setId(1L); log1.setTour(sampleTour);
        log2 = new TourLog(); log2.setId(2L); log2.setTour(sampleTour);
    }

    @Test
    void loadLogsForTour_populatesListAndSelectedTour() {
        when(logService.getLogsForTour(sampleTour)).thenReturn(java.util.List.of(log1, log2));

        vm.loadLogsForTour(sampleTour);

        ObservableList<TourLog> list = vm.getLogs();
        assertEquals(2, list.size());
        assertTrue(list.containsAll(java.util.List.of(log1, log2)));
        assertSame(sampleTour, vm.selectedTourProperty().get());
        verify(logService).getLogsForTour(sampleTour);
    }

    @Test
    void clearLogs_clearsListAndSelectedTour() {
        vm.loadLogsForTour(sampleTour);
        vm.clearLogs();

        assertTrue(vm.getLogs().isEmpty());
        assertNull(vm.selectedTourProperty().get());
    }

    @Test
    void createLog_addsToListAndFiresEvent() {
        TourLog newLog = new TourLog(); newLog.setTour(sampleTour);
        TourLog created = new TourLog(); created.setId(99L); created.setTour(sampleTour);

        when(logService.createLog(newLog)).thenReturn(created);

        vm.loadLogsForTour(sampleTour);
        vm.createLog(newLog);

        assertTrue(vm.getLogs().contains(created));
        verify(eventManager).publish(Events.TOUR_LOGS_CHANGED, sampleTour);
    }

    @Test
    void updateLog_reloadsAndFiresEvent() {
        when(logService.getLogsForTour(sampleTour)).thenReturn(java.util.List.of(log1));
        vm.loadLogsForTour(sampleTour);

        log1.setComment("updated");
        vm.updateLog(log1);

        verify(logService, atLeastOnce()).getLogsForTour(sampleTour);
        verify(eventManager).publish(Events.TOUR_LOGS_CHANGED, sampleTour);
    }

    @Test
    void deleteLog_removesAndFiresEvent() {
        vm.loadLogsForTour(sampleTour);
        vm.getLogs().add(log1);

        doNothing().when(logService).deleteLog(log1);
        vm.deleteLog(log1);

        assertFalse(vm.getLogs().contains(log1));
        verify(eventManager).publish(Events.TOUR_LOGS_CHANGED, sampleTour);
    }
}