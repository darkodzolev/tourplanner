package at.technikum.javafx.view;

import at.technikum.javafx.entity.Tour;
import at.technikum.javafx.viewmodel.TourDialogViewModel;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.*;

class TourDialogViewTest {

    private TourDialogView controller;
    private TourDialogViewModel dialogVm;

    private TextField   nameField;
    private TextArea    descriptionField;
    private TextField   fromField;
    private TextField   toField;
    private ComboBox<String> transportCombo;

    @BeforeEach
    void setUp() throws Exception {
        new JFXPanel();

        controller = new TourDialogView();

        Field vmField = TourDialogView.class.getDeclaredField("vm");
        vmField.setAccessible(true);
        dialogVm = (TourDialogViewModel) vmField.get(controller);

        nameField        = new TextField();
        descriptionField = new TextArea();
        fromField        = new TextField();
        toField          = new TextField();
        transportCombo   = new ComboBox<>();

        for (Field f : TourDialogView.class.getDeclaredFields()) {
            f.setAccessible(true);
            switch (f.getName()) {
                case "nameField"        -> f.set(controller, nameField);
                case "descriptionField" -> f.set(controller, descriptionField);
                case "fromField"        -> f.set(controller, fromField);
                case "toField"          -> f.set(controller, toField);
                case "transportCombo"   -> f.set(controller, transportCombo);
            }
        }

        controller.initialize(
                null,
                ResourceBundle.getBundle("at.technikum.javafx.i18n_en", Locale.ENGLISH)
        );
    }

    @Test
    void initialize_setsTransportOptionsAndBindings() {
        List<String> opts = transportCombo.getItems();
        assertEquals(3, opts.size());
        assertTrue(opts.containsAll(
                List.of("driving-car","foot-walking","cycling-regular")
        ));

        nameField.setText("X");
        assertEquals("X", dialogVm.nameProperty().get());

        dialogVm.descriptionProperty().set("Desc");
        assertEquals("Desc", descriptionField.getText());
    }

    @Test
    void setTour_populatesAllFields() {
        Tour t = new Tour();
        t.setName("My Tour");
        t.setDescription("D");
        t.setFromLocation("A");
        t.setToLocation("B");
        t.setTransportType("foot-walking");

        controller.setTour(t);

        assertEquals("My Tour", nameField.getText());
        assertEquals("D",    descriptionField.getText());
        assertEquals("A",    fromField.getText());
        assertEquals("B",    toField.getText());
        assertEquals("foot-walking", transportCombo.getValue());
    }

    @Test
    void getTourFromFields_and_updateTour() {
        nameField.setText("N");
        descriptionField.setText("Desc");
        fromField.setText("X");
        toField.setText("Y");
        transportCombo.setValue("cycling-regular");

        Tour created = controller.getTourFromFields();
        assertNull(created.getId());
        assertEquals("N", created.getName());
        assertEquals("X", created.getFromLocation());

        Tour existing = new Tour();
        existing.setId(9L);
        Tour updated = controller.getUpdatedTour(existing);
        assertSame(existing, updated);
        assertEquals(9L, existing.getId());
        assertEquals("N", existing.getName());
    }
}