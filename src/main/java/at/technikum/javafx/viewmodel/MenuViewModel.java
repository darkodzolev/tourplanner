package at.technikum.javafx.viewmodel;

import at.technikum.javafx.service.SearchTermService;

public class MenuViewModel {

    private final SearchTermService searchTermService;

    public MenuViewModel(SearchTermService searchTermService) {
        this.searchTermService = searchTermService;
    }

    public void clearHistory() {
        searchTermService.clearSearchTerms();
    }
}
