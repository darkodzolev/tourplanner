package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.entity.TourLog;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.service.ITourLogService;
import at.technikum.javafx.service.ITourService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MenuViewModelTest {

    @Mock ITourService tourService;
    @Mock ITourLogService tourLogService;
    @Mock EventManager eventManager;

    @InjectMocks MenuViewModel vm;

    private final ObjectMapper mapper = new ObjectMapper();

    @TempDir File tempDir;

    @Test
    void importAllTours_success() throws Exception {
        Tour t1 = new Tour(); t1.setName("A");
        TourLog log = new TourLog(); log.setComment("ok");
        t1.setLogs(Collections.singletonList(log));
        Tour t2 = new Tour(); t2.setName("B");

        File input = new File(tempDir, "in.json");
        mapper.writeValue(input, new Tour[]{t1, t2});

        when(tourService.createTour(any())).thenAnswer(inv -> {
            Tour arg = inv.getArgument(0);
            arg.setId(42L);
            return arg;
        });
        when(tourLogService.createLog(any())).thenReturn(new TourLog());

        vm.importAllTours(input);

        verify(tourService, times(2)).createTour(any());
        verify(tourLogService, times(1)).createLog(any());
        verify(eventManager).publish(eq(Events.TOURS_CHANGED), isNull());
    }

    @Test
    void importAllTours_badJson_throws() {
        File bad = new File(tempDir, "bad.json");
        assertDoesNotThrow(() -> Files.writeString(bad.toPath(), "{ not json }"));

        assertThrows(Exception.class, () -> vm.importAllTours(bad));
        verifyNoInteractions(tourService, tourLogService, eventManager);
    }

    @Test
    void exportTours_writesFile() throws Exception {
        Tour t1 = new Tour(); t1.setName("X"); t1.setLogs(Arrays.asList(new TourLog()));
        List<Tour> list = Collections.singletonList(t1);
        File out = new File(tempDir, "out.json");

        vm.exportTours(list, out);

        assertTrue(out.exists());
        Tour[] read = mapper.readValue(out, Tour[].class);
        assertEquals(1, read.length);
        assertEquals("X", read[0].getName());
        assertTrue(read[0].getLogs().isEmpty());
    }

    @Test
    void exportAllTours_writesAll() throws Exception {
        Tour t1 = new Tour(); t1.setName("1"); t1.setLogs(Arrays.asList(new TourLog()));
        Tour t2 = new Tour(); t2.setName("2"); t2.setLogs(Collections.emptyList());
        when(tourService.getAllTours()).thenReturn(Arrays.asList(t1, t2));

        File out = new File(tempDir, "all.json");
        vm.exportAllTours(out);

        assertTrue(out.exists());
        Tour[] read = mapper.readValue(out, Tour[].class);
        assertEquals(2, read.length);
        assertEquals("1", read[0].getName());
        assertEquals("2", read[1].getName());
        assertTrue(read[0].getLogs().isEmpty());
        assertTrue(read[1].getLogs().isEmpty());
    }
}