package at.technikum.javafx.viewmodel;

import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import javafx.beans.property.StringProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchViewModelTest {

    @Mock EventManager eventManager;
    @InjectMocks SearchViewModel vm;

    @BeforeEach
    void setup() {
    }

    @Test
    void changingSearchTextPublishesSearchTermSelected() {
        StringProperty textProp = vm.searchTextProperty();

        textProp.set("hello");
        verify(eventManager).publish(Events.SEARCH_TERM_SELECTED, "hello");

        textProp.set("world");
        verify(eventManager).publish(Events.SEARCH_TERM_SELECTED, "world");

        verifyNoMoreInteractions(eventManager);
    }

    @Test
    void searchMethodPublishesSearchPerformed_onlyOnce() {
        reset(eventManager);

        vm.searchTextProperty().set("find me");

        reset(eventManager);

        vm.search();

        verify(eventManager).publish(Events.SEARCH_PERFORMED, "find me");
        verifyNoMoreInteractions(eventManager);
    }
}