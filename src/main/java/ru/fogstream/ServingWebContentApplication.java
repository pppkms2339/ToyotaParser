package ru.fogstream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import ru.fogstream.config.DataSourceConfig;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableJpaRepositories("ru.fogstream.repository")
@Import(DataSourceConfig.class)
//@EnableScheduling
public class ServingWebContentApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ServingWebContentApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ServingWebContentApplication.class);
    }

}
