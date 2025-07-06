package at.technikum.javafx.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.repository.TourRepository;

@ExtendWith(MockitoExtension.class)
class TourServiceTest {

    @Mock
    private TourRepository tourRepo;
    @Mock
    private EventManager eventManager;

    @InjectMocks
    private TourService service;

    private Tour t;

    @BeforeEach
    void setUp() {
        t = new Tour();
        t.setName("Test Tour");
        t.setFromLocation("A");
        t.setToLocation("B");
        t.setTransportType("driving-car");
        t.setDistance(10.0);
        t.setEstimatedTime("01:00:00");
    }

    @Test
    void getAllTours_delegatesToRepo() {
        when(tourRepo.findAll()).thenReturn(List.of(t));
        List<Tour> result = service.getAllTours();
        assertEquals(1, result.size());
        verify(tourRepo).findAll();
    }

    @Test
    void createTour_successPublishesEvent() {
        // no existing tour
        when(tourRepo.findByName(t.getName())).thenReturn(Optional.empty());
        // repo assigns ID
        Tour saved = new Tour();
        saved.setId(5L);
        when(tourRepo.save(t)).thenReturn(saved);

        Tour out = service.createTour(t);

        assertEquals(5L, out.getId());
        verify(tourRepo).findByName(t.getName());
        verify(tourRepo).save(t);
        verify(eventManager).publish(Events.TOURS_CHANGED, saved);
    }

    @Test
    void createTour_duplicateNameThrows() {
        when(tourRepo.findByName(t.getName())).thenReturn(Optional.of(new Tour()));
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.createTour(t)
        );
        assertTrue(ex.getMessage().contains("already exists"));
        verify(tourRepo).findByName(t.getName());
        verify(tourRepo, never()).save(any());
        verify(eventManager, never()).publish(any(), any());
    }

    @Test
    void updateTour_nullIdThrows() {
        t.setId(null);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.updateTour(t)
        );
        assertTrue(ex.getMessage().contains("without an ID"));
    }

    @Test
    void updateTour_successPublishesEvent() {
        t.setId(7L);
        when(tourRepo.save(t)).thenReturn(t);

        Tour out = service.updateTour(t);

        assertEquals(7L, out.getId());
        verify(tourRepo).save(t);
        verify(eventManager).publish(Events.TOURS_CHANGED, t);
    }

    @Test
    void deleteTour_callsRepoAndPublishes() {
        t.setId(8L);
        doNothing().when(tourRepo).delete(t);

        service.deleteTour(t);

        verify(tourRepo).delete(t);
        verify(eventManager).publish(Events.TOURS_CHANGED, t);
    }

    @Test
    void findById_and_findByName_delegate() {
        when(tourRepo.findById(9L)).thenReturn(Optional.of(t));
        when(tourRepo.findByName("Test")).thenReturn(Optional.of(t));

        assertTrue(service.findById(9L).isPresent());
        assertTrue(service.findByName("Test").isPresent());

        verify(tourRepo).findById(9L);
        verify(tourRepo).findByName("Test");
    }
}