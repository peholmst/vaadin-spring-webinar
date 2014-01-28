package org.vaadin.spring.boot;

import com.vaadin.ui.UI;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.vaadin.spring.EnableVaadin;
import org.vaadin.spring.SpringAwareVaadinServlet;
import org.vaadin.spring.VaadinConfigurer;
import org.vaadin.spring.VaadinUiScope;

import java.util.List;

/**
 * TODO support the *Configurer pattern to permit parameterization of things \
 * TODO like the {@link org.vaadin.spring.SpringAwareVaadinServlet registration}.
 *
 * @author petter@vaadin.com
 * @author Josh Long (josh@joshlong.com)
 * @see org.vaadin.spring.EnableVaadin
 */
@Configuration
@ConditionalOnClass(VaadinUiScope.class)
public class VaadinAutoConfiguration {

    private static Logger logger = Logger.getLogger(VaadinAutoConfiguration.class);

    /*
     * If the outer {@code \@Configuration} class is enabled (e.g., the
     * {@link VaadinUiScope UI scope} implementation is on the CLASSPATH),
     * _then_ we let Spring import the configuration class.
     */
    @Configuration
    @EnableVaadin
    static class EnableVaadinConfiguration implements InitializingBean {

        @Autowired(required = false)
        private List<VaadinConfigurer> vaadinConfigurers;

        @Override
        public void afterPropertiesSet() throws Exception {
            logger.debug(getClass().getName() + " has finished running");
        }

        @Bean
        ServletRegistrationBean vaadinServlet() {
            logger.debug("registering vaadinServlet()");

            Assert.notNull(this.vaadinConfigurers);
            Assert.isTrue(this.vaadinConfigurers.size() == 1, "there must be one and only one VaadinConfigurer provided");
            Class<? extends UI> uiClass =
                    this.vaadinConfigurers.iterator().next().uiClass() ;
            ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                    new SpringAwareVaadinServlet(uiClass), "/*", "/VAADIN/*");
            registrationBean.addInitParameter("heartbeatInterval", "10"); // In order to test that orphaned UIs are detached properly
            return registrationBean;
        }
    }
}
