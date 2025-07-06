package at.technikum.javafx;

import at.technikum.javafx.event.EventManager;
import at.technikum.javafx.service.IReportService;
import at.technikum.javafx.service.ITourLogService;
import at.technikum.javafx.service.ITourService;
import at.technikum.javafx.view.MainView;
import at.technikum.javafx.view.MenuView;
import at.technikum.javafx.view.SearchView;
import at.technikum.javafx.view.TourGeneralView;
import at.technikum.javafx.view.TourLogView;
import at.technikum.javafx.view.TourRouteView;
import at.technikum.javafx.view.TourView;
import at.technikum.javafx.viewmodel.MainViewModel;
import at.technikum.javafx.viewmodel.MenuViewModel;
import at.technikum.javafx.viewmodel.SearchViewModel;
import at.technikum.javafx.viewmodel.TourLogViewModel;
import at.technikum.javafx.viewmodel.TourViewModel;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;

public class ViewFactory {

    private static ViewFactory instance;

    private final EventManager    eventManager;
    private final ITourService     tourService;
    private final ITourLogService  tourLogService;
    private final IReportService reportService;

    private final SearchViewModel     searchViewModel;
    private final MenuViewModel       menuViewModel;
    private final TourViewModel       tourViewModel;
    private final TourLogViewModel    tourLogViewModel;
    private final TourRouteView       tourRouteView;

    private ViewFactory() {
        this.eventManager    = TourPlannerApplication.getBean(EventManager.class);
        this.tourService     = TourPlannerApplication.getBean(ITourService.class);
        this.tourLogService  = TourPlannerApplication.getBean(ITourLogService.class);
        this.reportService = TourPlannerApplication.getBean(IReportService.class);

        this.searchViewModel  = new SearchViewModel(eventManager);
        this.menuViewModel    = new MenuViewModel(tourService, tourLogService, eventManager);
        this.tourViewModel    = new TourViewModel(tourService, tourLogService, eventManager);
        this.tourLogViewModel = new TourLogViewModel(tourLogService, eventManager);

        this.tourRouteView    = new TourRouteView(tourViewModel);

        tourViewModel.selectedTourProperty().addListener((obs, oldT, newT) -> {
            if (newT != null) tourLogViewModel.loadLogsForTour(newT);
            else               tourLogViewModel.clearLogs();
        });
    }

    public static ViewFactory getInstance() {
        if (instance == null) instance = new ViewFactory();
        return instance;
    }

    public Object create(Class<?> viewClass) {
        try {
            if (MainView.class == viewClass) {
                // MainView is defined entirely in code
                return new MainView(new MainViewModel(tourService));
            }
            if (TourView.class == viewClass) {
                return new TourView(tourViewModel);
            }
            if (TourGeneralView.class == viewClass) {
                return new TourGeneralView(tourViewModel);
            }
            if (TourRouteView.class == viewClass) {
                // Load FXML and inject our TourRouteView controller
                FXMLLoader loader = new FXMLLoader(
                        Objects.requireNonNull(
                                getClass().getResource("/at/technikum/javafx/tour-route-view.fxml")
                        )
                );
                loader.setControllerFactory(type -> {
                    if (type == TourRouteView.class) return tourRouteView;
                    try { return type.getDeclaredConstructor().newInstance(); }
                    catch (Exception e) { throw new RuntimeException(e); }
                });
                Parent root = loader.load();
                return loader.getController();
            }
            if (TourLogView.class == viewClass) {
                return new TourLogView(tourLogViewModel);
            }
            if (MenuView.class == viewClass) {
                // 1) Load the FXML without a ResourceBundle
                FXMLLoader loader = new FXMLLoader(
                        Objects.requireNonNull(
                                getClass().getResource("/at/technikum/javafx/menu-view.fxml")
                        )
                );
                // 2) Inject our custom constructor
                loader.setControllerFactory(type -> {
                    if (type == MenuView.class) {
                        return new MenuView(menuViewModel, tourViewModel, reportService, tourRouteView);
                    }
                    try {
                        return type.getDeclaredConstructor().newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
                // 3) Actually load & return the controller
                Parent root = loader.load();
                return loader.getController();
            }
            if (SearchView.class == viewClass) {
                return new SearchView(searchViewModel);
            }
            throw new IllegalArgumentException("Unknown view class: " + viewClass);
        } catch (IOException ioEx) {
            throw new RuntimeException("Failed to load FXML for " + viewClass.getSimpleName(), ioEx);
        }
    }
}