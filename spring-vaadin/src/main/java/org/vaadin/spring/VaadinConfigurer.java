package org.vaadin.spring;

import com.vaadin.ui.UI;

/**
 * Callback interface that must be implemented by clases that use
 * {@link org.vaadin.spring.EnableVaadin enableVaadin}.
 *
 * @author Josh Long (josh@joshlong.com)
 * @author petter@vaadin.com
 */
public interface VaadinConfigurer {
    Class<? extends UI> uiClass() ;

}
