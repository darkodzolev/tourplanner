package at.technikum.javafx.viewmodel;

import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchViewModel {
    private static final Logger log = LoggerFactory.getLogger(SearchViewModel.class);

    private final EventManager eventManager;
    private final StringProperty searchText = new SimpleStringProperty("");

    public SearchViewModel(EventManager eventManager) {
        this.eventManager = eventManager;

        // automatically publish on every keystroke
        this.searchText.addListener((obs, oldVal, newVal) -> {
            log.debug("Search text changed from '{}' to '{}'", oldVal, newVal);
            eventManager.publish(Events.SEARCH_TERM_SELECTED, newVal);
        });
    }

    public void search() {
        String term = getSearchText();
        log.info("User initiated search for term '{}'", term);
        try {
            eventManager.publish(Events.SEARCH_PERFORMED, term);
        } catch (Exception e) {
            log.error("Error while publishing SEARCH_PERFORMED for '{}'", term, e);
        }
    }

    public StringProperty searchTextProperty() {
        return searchText;
    }

    public String getSearchText() {
        return searchText.get();
    }
}