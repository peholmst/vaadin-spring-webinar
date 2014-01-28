package org.vaadin.spring;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Brings in the  machinery to setup Spring + Vaadin applications.
 *
 * @author Josh Long (josh@joshlong.com)
 * @author petter@vaadin.com
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import( VaadinConfiguration.class)
public @interface EnableVaadin {
}
