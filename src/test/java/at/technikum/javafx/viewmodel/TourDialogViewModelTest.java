package at.technikum.javafx.viewmodel;

import at.technikum.javafx.entity.Tour;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TourDialogViewModelTest {

    private TourDialogViewModel vm;

    @BeforeEach
    void setUp() {
        vm = new TourDialogViewModel();
    }

    @Test
    void transportOptions_areInitialized() {
        ObservableList<String> opts = vm.getTransportOptions();
        assertNotNull(opts);
        assertEquals(3, opts.size());
        assertTrue(opts.contains("driving-car"));
        assertTrue(opts.contains("foot-walking"));
        assertTrue(opts.contains("cycling-regular"));
    }

    @Test
    void setTour_populatesFields() {
        Tour t = new Tour();
        t.setId(5L);
        t.setName("My Tour");
        t.setDescription("Desc");
        t.setFromLocation("A");
        t.setToLocation("B");
        t.setTransportType("foot-walking");

        vm.setTour(t);

        assertEquals("My Tour", vm.nameProperty().get());
        assertEquals("Desc", vm.descriptionProperty().get());
        assertEquals("A", vm.fromLocationProperty().get());
        assertEquals("B", vm.toLocationProperty().get());
        assertEquals("foot-walking", vm.transportTypeProperty().get());
    }

    @Test
    void createTour_returnsNewTourWithFields() {
        vm.nameProperty().set("X");
        vm.descriptionProperty().set("Y");
        vm.fromLocationProperty().set("From");
        vm.toLocationProperty().set("To");
        vm.transportTypeProperty().set("driving-car");

        Tour created = vm.createTour();
        assertNull(created.getId());
        assertEquals("X", created.getName());
        assertEquals("Y", created.getDescription());
        assertEquals("From", created.getFromLocation());
        assertEquals("To", created.getToLocation());
        assertEquals("driving-car", created.getTransportType());
    }

    @Test
    void updateTour_modifiesExisting() {
        Tour existing = new Tour();
        existing.setId(99L);
        existing.setName("old");
        existing.setDescription("oldDesc");
        existing.setFromLocation("oldFrom");
        existing.setToLocation("oldTo");
        existing.setTransportType("cycling-regular");

        vm.nameProperty().set("newName");
        vm.descriptionProperty().set("newDesc");
        vm.fromLocationProperty().set("newFrom");
        vm.toLocationProperty().set("newTo");
        vm.transportTypeProperty().set("foot-walking");

        Tour updated = vm.updateTour(existing);
        assertSame(existing, updated);
        assertEquals(99L, existing.getId());
        assertEquals("newName", existing.getName());
        assertEquals("newDesc", existing.getDescription());
        assertEquals("newFrom", existing.getFromLocation());
        assertEquals("newTo", existing.getToLocation());
        assertEquals("foot-walking", existing.getTransportType());
    }
}