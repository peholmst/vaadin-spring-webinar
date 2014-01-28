package org.vaadin.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Josh Long (josh@joshlong.com)
 * @author petter@vaadin.com
 * @see org.vaadin.spring.EnableVaadin
 */
@Configuration
public class VaadinConfiguration {

    @Bean
    static VaadinUiScope uiScope() {
        return new VaadinUiScope();
    }
}
