package org.vaadin.webinars.springandvaadin.bootexample.ui;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.vaadin.spring.UiScope;

/**
 * @author petter@vaadin.com
 */
@VaadinComponent
@UiScope
public class SpringViewProvider implements ViewProvider {

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public String getViewName(String s) {
        int separator = s.indexOf('/');
        String viewName;
        if (separator == -1) {
            viewName = s;
        } else {
            viewName = s.substring(0, separator);
        }
        if (applicationContext.containsBean(viewName)) {
            return viewName;
        }
        return null;
    }

    @Override
    public View getView(String s) {
        return applicationContext.getBean(s, View.class);
    }
}
