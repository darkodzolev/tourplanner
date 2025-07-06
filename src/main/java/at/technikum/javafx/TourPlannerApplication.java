package at.technikum.javafx;

import javafx.application.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TourPlannerApplication {

    private static final Logger log = LoggerFactory.getLogger(TourPlannerApplication.class);
    private static ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        log.info("Starting Spring context for TourPlanner");
        springContext = SpringApplication.run(TourPlannerApplication.class, args);
        log.info("Spring context initialized, launching JavaFX");
        Application.launch(SearchApplication.class, args);
    }

    public static <T> T getBean(Class<T> beanType) {
        return springContext.getBean(beanType);
    }
}