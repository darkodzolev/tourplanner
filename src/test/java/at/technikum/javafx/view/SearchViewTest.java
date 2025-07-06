package at.technikum.javafx.view;

import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.viewmodel.SearchViewModel;
import javafx.embed.swing.JFXPanel;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchViewTest {

    private SearchViewModel viewModel;
    private SearchView controller;
    private TextField searchInput;

    @BeforeEach
    void setUp() throws Exception {
        // Force JavaFX toolkit initialization:
        new JFXPanel();

        viewModel = spy(new SearchViewModel(mock(EventManager.class)));
        controller = new SearchView(viewModel);

        // Create & inject the TextField via reflection
        TextField tf = new TextField();
        Field fld = SearchView.class.getDeclaredField("searchInput");
        fld.setAccessible(true);
        fld.set(controller, tf);
        searchInput = tf;

        // Now bind
        controller.initialize(null, null);
    }

    @Test
    void typingInTextFieldUpdatesViewModel() {
        searchInput.setText("foo");
        StringProperty prop = viewModel.searchTextProperty();
        assertEquals("foo", prop.get());
    }

    @Test
    void updatingViewModelUpdatesTextField() {
        viewModel.searchTextProperty().set("bar");
        assertEquals("bar", searchInput.getText());
    }
}