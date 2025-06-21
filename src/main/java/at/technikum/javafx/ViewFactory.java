package at.technikum.javafx;

import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.repository.SearchTermRepository;
import at.technikum.javafx.repository.SearchTermRepositoryOrm;
import at.technikum.javafx.service.SearchTermService;
import at.technikum.javafx.view.HistoryView;
import at.technikum.javafx.view.MainView;
import at.technikum.javafx.view.MenuView;
import at.technikum.javafx.view.SearchView;
import at.technikum.javafx.viewmodel.HistoryViewModel;
import at.technikum.javafx.viewmodel.MainViewModel;
import at.technikum.javafx.viewmodel.MenuViewModel;
import at.technikum.javafx.viewmodel.SearchViewModel;

public class ViewFactory {

    private static ViewFactory instance;

    private final EventManager eventManager;

    private final SearchTermRepository searchTermRepository;

    private final SearchTermService searchTermService;

    private ViewFactory() {
        this.eventManager = new EventManager();
        this.searchTermRepository = new SearchTermRepositoryOrm();
        this.searchTermService = new SearchTermService(eventManager, searchTermRepository);
    }

    public static ViewFactory getInstance() {
        if (null == instance) {
            instance = new ViewFactory();
        }

        return instance;
    }

    public Object create(Class<?> viewClass) {
        if (MainView.class == viewClass) {
            return new MainView(new MainViewModel());
        }

        if (MenuView.class == viewClass) {
            return new MenuView(new MenuViewModel(searchTermService));
        }

        if (SearchView.class == viewClass) {
            return new SearchView(
                    new SearchViewModel(
                            eventManager,
                            searchTermService
                    )
            );
        }

        if (HistoryView.class == viewClass) {
            return new HistoryView(
                    new HistoryViewModel(
                            eventManager,
                            searchTermService
                    )
            );
        }

        throw new IllegalArgumentException(
                "Unknown view class: " + viewClass
        );
    }
}
