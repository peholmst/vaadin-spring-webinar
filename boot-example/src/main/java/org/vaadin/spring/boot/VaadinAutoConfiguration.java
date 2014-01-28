package org.vaadin.spring.boot;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vaadin.spring.EnableVaadin;
import org.vaadin.spring.SpringAwareVaadinServlet;
import org.vaadin.spring.VaadinUiScope;

/**
 * Provides auto configuration for the Vaadin-Spring integration.
 *
 * @author Josh Long (josh@joshlong.com)
 * @author petter@vaadin.com
 */
@Configuration
@ConditionalOnClass(VaadinUiScope.class)
public class VaadinAutoConfiguration {

    @Configuration
    @EnableVaadin
    class EnableVaadinConfiguration {}
}
