package at.technikum.javafx.viewmodel;

import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.service.SearchTermService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class SearchViewModel {

    private final EventManager eventManager;

    private final SearchTermService searchTermService;

    private final StringProperty searchText
            = new SimpleStringProperty("");

    private final BooleanProperty searchDisabled
            = new SimpleBooleanProperty(true);

    public SearchViewModel(
            EventManager eventManager,
            SearchTermService searchTermService
    ) {
        this.eventManager = eventManager;
        this.searchTermService = searchTermService;

        this.searchDisabled.bind(searchText.isEmpty());

        this.eventManager.subscribe(
                Events.SEARCH_TERM_SELECTED, this::onSearchTermSelected
        );
    }

    public void search() {
        this.searchTermService.add(searchText.get());

        // notice: no view.clearSearch()
        searchText.set("");
    }

    public void onSearchTermSelected(String message) {
        if (null == message) {
            this.searchText.set("");
            return;
        }

        this.searchText.set(message);
    }

    public String getSearchText() {
        return searchText.get();
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public boolean isSearchDisabled() {
        return searchDisabled.get();
    }

    public BooleanProperty searchDisabledProperty() {
        return searchDisabled;
    }
}
