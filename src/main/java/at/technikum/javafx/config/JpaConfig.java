package at.technikum.javafx.config;

import jakarta.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@Configuration
public class JpaConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            EntityManagerFactoryBuilder builder,
            DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean emf = builder
                .dataSource(dataSource)
                .packages("at.technikum.javafx.entity")  // your entity package
                .persistenceUnit("default")
                .build();

        // Force it to use the JPA EntityManagerFactory interface
        emf.setEntityManagerFactoryInterface(EntityManagerFactory.class);
        return emf;
    }
}