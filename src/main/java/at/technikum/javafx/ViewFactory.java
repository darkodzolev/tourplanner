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
import at.technikum.javafx.view.TourGeneralView;
import at.technikum.javafx.view.TourLogView;
import at.technikum.javafx.view.TourView;
import at.technikum.javafx.viewmodel.MainViewModel;
import at.technikum.javafx.viewmodel.MenuViewModel;
import at.technikum.javafx.viewmodel.SearchViewModel;
import at.technikum.javafx.viewmodel.TourLogViewModel;
import at.technikum.javafx.viewmodel.TourViewModel;
import at.technikum.javafx.view.TourRouteView;

public class ViewFactory {

    private static ViewFactory instance;

    private final EventManager eventManager;

    private final SearchTermRepository searchTermRepository;
    private final SearchTermService searchTermService;
    private final SearchViewModel searchViewModel;

    private final TourRepository tourRepository;
    private final TourLogRepository tourLogRepository;

    private final TourService tourService;
    private final TourLogService tourLogService;

    private final TourViewModel tourViewModel;
    private final TourLogViewModel tourLogViewModel;

    private ViewFactory() {
        // Core frameworks
        this.eventManager = new EventManager();

        // search history (still used by Main/Menu VMs)
        this.searchTermRepository = new SearchTermRepositoryOrm();
        this.searchTermService = new SearchTermService(eventManager, searchTermRepository);

        // **single** SearchViewModel, auto-publishing on typing
        this.searchViewModel = new SearchViewModel(eventManager);

        // tours + logs
        this.tourRepository = new TourRepositoryOrm();
        this.tourLogRepository = new TourLogRepositoryOrm();
        this.tourService = new TourService(tourRepository, tourLogRepository);
        this.tourLogService = new TourLogService(tourLogRepository);

        // Tour screen
        this.tourViewModel = new TourViewModel(tourService, tourLogService, eventManager);
        this.tourLogViewModel = new TourLogViewModel(tourLogService, eventManager);

        // Wire tour selection → log loading
        tourViewModel.selectedTourProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) {
                tourLogViewModel.loadLogsForTour(newT);
            } else {
                tourLogViewModel.clearLogs();
            }
        });
    }

    public static ViewFactory getInstance() {
        if (instance == null) {
            instance = new ViewFactory();
        }
        return instance;
    }

    public Object create(Class<?> viewClass) {
        if (MainView.class == viewClass) {
            return new MainView(new MainViewModel(tourService, searchTermService));
        }
        if (TourView.class == viewClass) {
            return new TourView(tourViewModel);
        }
        if (TourGeneralView.class == viewClass) {
            return new TourGeneralView(tourViewModel);
        }
        if (TourRouteView.class == viewClass) {
            return new TourRouteView(tourViewModel);
        }
        if (TourLogView.class == viewClass) {
            return new TourLogView(tourLogViewModel);
        }
        if (MenuView.class == viewClass) {
            return new MenuView(new MenuViewModel(searchTermService));
        }
        if (SearchView.class == viewClass) {
            // reuse the one VM that auto-fires on typing
            return new SearchView(searchViewModel);
        }
        throw new IllegalArgumentException("Unknown view class: " + viewClass);
    }
}