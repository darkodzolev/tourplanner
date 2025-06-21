package at.technikum.javafx.service;

import at.technikum.javafx.entity.SearchTerm;
import at.technikum.javafx.event.EventListener;
import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.event.Events;
import at.technikum.javafx.repository.SearchTermRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SearchTermService {

    private final EventManager eventManager;

    private final SearchTermRepository searchTermRepository;

    public SearchTermService(
            EventManager eventManager,
            SearchTermRepository searchTermRepository
    ) {
        this.eventManager = eventManager;
        this.searchTermRepository = searchTermRepository;
    }

    public void add(String term) {
        SearchTerm searchTerm = new SearchTerm();
        searchTerm.setId(UUID.randomUUID().toString());
        searchTerm.setTerm(term);
        searchTerm.setFirstSearched(LocalDateTime.now());

        this.searchTermRepository.save(searchTerm);

        eventManager.publish(Events.SEARCH_TERMS_CHANGED, "NEW");
    }

    public void clearSearchTerms() {
        this.searchTermRepository.deleteAll();

        eventManager.publish(Events.SEARCH_TERMS_CHANGED, "CLEAR");
    }

    public List<String> getSearchTerms() {
        return this.searchTermRepository
                .findAll().stream()
                .map(SearchTerm::getTerm)
                .toList();
    }
}
