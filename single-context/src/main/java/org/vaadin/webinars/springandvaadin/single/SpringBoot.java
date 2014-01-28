package org.vaadin.webinars.springandvaadin.single;

import com.cybercom.vaadin.spring.UIScope;
import com.vaadin.ui.UI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.vaadin.webinars.springandvaadin.single.ui.Servlet;
import org.vaadin.webinars.springandvaadin.single.ui.SpringManagedUI;

/**
 * @author petter@vaadin.com
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class SpringBoot extends SpringBootServletInitializer {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(SpringBoot.class, args);
    }

    @Bean
    static UIScope uiScope() {
        return new UIScope();
    }

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        final ServletRegistrationBean servletRegistrationBean
                = new ServletRegistrationBean(
                new Servlet(), "/*", "/VAADIN/*");
        return servletRegistrationBean;
    }
}
