package at.technikum.javafx;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class TourPlannerApplication {

    private static ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        springContext = SpringApplication.run(TourPlannerApplication.class, args);
        Application.launch(SearchApplication.class, args);
    }

    public static <T> T getBean(Class<T> beanType) {
        return springContext.getBean(beanType);
    }
}