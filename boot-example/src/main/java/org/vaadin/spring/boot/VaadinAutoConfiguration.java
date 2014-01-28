package org.vaadin.spring.boot;

import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vaadin.spring.SpringAwareVaadinServlet;
import org.vaadin.spring.VaadinUiScope;

/**
 * Provides auto configuration for the Vaadin-Spring integration.
 *
 * @author Josh Long (josh@joshlong.com)
 * @author Petter Holmström (petter@vaadin.com)
 */
@Configuration
@ConditionalOnClass(VaadinUiScope.class)
public class VaadinAutoConfiguration {

    private Logger logger = Logger.getLogger(getClass());


    @Configuration
    static class VaadinUiScopeConfiguration {
        @Bean
        static VaadinUiScope uiScope() {
            return new VaadinUiScope();
        }
    }

    @Bean
    ServletRegistrationBean vaadinServlet() {
        logger.debug("registering vaadinServlet()");
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                new SpringAwareVaadinServlet(), "/*", "/VAADIN/*");
        registrationBean.addInitParameter("heartbeatInterval", "10"); // In order to test that orphaned UIs are detached properly
        return registrationBean;
    }
}
