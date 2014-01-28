package org.vaadin.spring.boot;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.vaadin.spring.EnableVaadin;
import org.vaadin.spring.VaadinUiScope;

import javax.annotation.PostConstruct;

/**
 * Provides auto configuration for the Vaadin-Spring integration.
 *
 * @author Josh Long (josh@joshlong.com)
 * @author petter@vaadin.com
 */
@Configuration
@ConditionalOnClass(VaadinUiScope.class)
public class VaadinAutoConfiguration implements InitializingBean {

    private static Logger logger = Logger.getLogger(VaadinAutoConfiguration.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        logger.debug(getClass().getName() + " has finished running");
    }

    /*
     * If the outer {@code \@Configuration} class is enabled (e.g., the
     * {@link VaadinUiScope UI scope} implementation is on the CLASSPATH),
     * _then_ we let Spring import the configuration class.
     */
    @Configuration
    @EnableVaadin
    static class EnableVaadinConfiguration
            implements InitializingBean {
        @Override
        public void afterPropertiesSet() throws Exception {
            logger.debug(getClass().getName() + " has finished running");
        }
    }
}
