package org.vaadin.webinars.springandvaadin.bootexample;


import com.vaadin.ui.UI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.vaadin.spring.VaadinConfigurer;
import org.vaadin.webinars.springandvaadin.bootexample.ui.SpringManagedUI;

/**
 * Entry point into the Vaadin web application. You may run this from
 * {@code public static void main} or change the Maven {@code packaging} to {@code war}
 * and disable the {@code }
 * and deploy to any Servlet 3 container, Java code unchanged.
 *
 * @author Petter Holmström (petter@vaadin.com)
 * @author Josh Long (josh@joshlong.com)
 */
@EnableAutoConfiguration
@ComponentScan
public class Application extends SpringBootServletInitializer implements VaadinConfigurer {

    private static Class<Application> applicationClass = Application.class;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(applicationClass);
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(applicationClass, args);
    }

    @Override
    public Class<? extends UI> uiClass() {
        return SpringManagedUI.class;
    }
}

