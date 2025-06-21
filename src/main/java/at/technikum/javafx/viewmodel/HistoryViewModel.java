package at.technikum.javafx.viewmodel;

import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.service.SearchTermService;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;

public class HistoryViewModel {

    private final EventManager eventManager;

    private final SearchTermService searchTermService;

    private final ObservableList<String> searchHistory
            = FXCollections.observableArrayList();

    private final ObjectProperty<String> selectedItem = new SimpleObjectProperty<>();


    public HistoryViewModel(
            EventManager eventManager,
            SearchTermService searchTermService
    ) {
        this.eventManager = eventManager;
        this.searchTermService = searchTermService;

        this.selectedItem.addListener(this::onSelectedItemChanged);

        this.eventManager.subscribe(
                Events.SEARCH_TERMS_CHANGED, this::onSearchTermChanged
        );

        this.searchHistory.addAll(searchTermService.getSearchTerms());
    }

    private void onSelectedItemChanged(Observable observable) {
        this.eventManager.publish(
                Events.SEARCH_TERM_SELECTED, getSelectedItem()
        );
    }

    private void onSearchTermChanged(String message) {
        this.searchHistory.clear();
        this.searchHistory.addAll(searchTermService.getSearchTerms());
    }

    public ObservableList<String> getSearchHistory() {
        return searchHistory;
    }

    public String getSelectedItem() {
        return selectedItem.get();
    }

    public ObjectProperty<String> selectedItemProperty() {
        return selectedItem;
    }
}
