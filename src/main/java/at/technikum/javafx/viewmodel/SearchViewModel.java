package at.technikum.javafx.viewmodel;

import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SearchViewModel {
    private final EventManager eventManager;
    private final StringProperty searchText = new SimpleStringProperty("");

    public SearchViewModel(EventManager eventManager) {
        this.eventManager = eventManager;
        // automatically publish on every keystroke
        this.searchText.addListener((obs, oldVal, newVal) ->
                eventManager.publish(Events.SEARCH_TERM_SELECTED, newVal)
        );
    }

    public void search() {
        // nothing here
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public String getSearchText() {
        return searchText.get();
    }
}