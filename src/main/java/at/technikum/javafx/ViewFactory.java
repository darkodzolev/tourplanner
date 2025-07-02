package at.technikum.javafx;

import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.repository.SearchTermRepository;
import at.technikum.javafx.repository.SearchTermRepositoryOrm;
import at.technikum.javafx.repository.TourLogRepository;
import at.technikum.javafx.repository.TourLogRepositoryOrm;
import at.technikum.javafx.repository.TourRepository;
import at.technikum.javafx.repository.TourRepositoryOrm;
import at.technikum.javafx.service.SearchTermService;
import at.technikum.javafx.service.TourLogService;
import at.technikum.javafx.service.TourService;
import at.technikum.javafx.view.MainView;
import at.technikum.javafx.view.MenuView;
import at.technikum.javafx.view.SearchView;
import at.technikum.javafx.view.TourLogView;
import at.technikum.javafx.view.TourView;
import at.technikum.javafx.viewmodel.MainViewModel;
import at.technikum.javafx.viewmodel.MenuViewModel;
import at.technikum.javafx.viewmodel.SearchViewModel;
import at.technikum.javafx.viewmodel.TourLogViewModel;
import at.technikum.javafx.viewmodel.TourViewModel;

public class ViewFactory {

    private static ViewFactory instance;

    private final EventManager eventManager;

    private final SearchTermRepository searchTermRepository;
    private final SearchTermService searchTermService;

    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;

    private final TourService tourService;
    private final TourLogService tourLogService;

    private final TourViewModel tourViewModel;
    private final TourLogViewModel tourLogViewModel;

    private ViewFactory() {
        // Core frameworks
        this.eventManager          = new EventManager();

        // Search term setup
        this.searchTermRepository  = new SearchTermRepositoryOrm();
        this.searchTermService     = new SearchTermService(eventManager, searchTermRepository);

        // Tour and log repositories
        this.tourRepository        = new TourRepositoryOrm();
        this.tourLogRepository     = new TourLogRepositoryOrm();

        // Services, injecting log repo into TourService too
        this.tourService           = new TourService(tourRepository, tourLogRepository);
        this.tourLogService        = new TourLogService(tourLogRepository);

        // ViewModels
        this.tourViewModel         = new TourViewModel(tourService, eventManager);
        this.tourLogViewModel      = new TourLogViewModel(tourLogService);

        // Wire tour selection → log loading
        tourViewModel.selectedTourProperty().addListener((obs, oldTour, newTour) -> {
            if (newTour != null) {
                tourLogViewModel.loadLogsForTour(newTour);
            } else {
                tourLogViewModel.clearLogs();
            }
        });
    }

    public static ViewFactory getInstance() {
        if (null == instance) {
            instance = new ViewFactory();
        }

        return instance;
    }

    public Object create(Class<?> viewClass) {
        if (MainView.class == viewClass) {
            return new MainView(new MainViewModel(tourService, searchTermService));
        }

        if (TourView.class == viewClass) {
            return new TourView(new TourViewModel(tourService, eventManager));
        }

        if (TourLogView.class == viewClass) {
            return new TourLogView(tourLogViewModel);
        }

        if (MenuView.class == viewClass) {
            return new MenuView(new MenuViewModel(searchTermService));
        }

        if (SearchView.class == viewClass) {
            return new SearchView(new SearchViewModel(eventManager, searchTermService));
        }

        throw new IllegalArgumentException(
                "Unknown view class: " + viewClass
        );
    }
}