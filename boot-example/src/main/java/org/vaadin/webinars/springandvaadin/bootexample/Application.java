package org.vaadin.webinars.springandvaadin.bootexample;


import com.vaadin.server.ClientConnector.DetachEvent;
import com.vaadin.server.ClientConnector.DetachListener;
import com.vaadin.server.*;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.vaadin.webinars.springandvaadin.bootexample.ui.SpringManagedUI;

import javax.servlet.ServletException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author petter@vaadin.com
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    static VaadinUiScope uiScope() {
        return new VaadinUiScope();
    }

    @Bean
    ServletRegistrationBean vaadinServlet() {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(
                new Servlet(), "/*", "/VAADIN/*");
        registrationBean.addInitParameter("heartbeatInterval", "10"); // In order to test that orphaned UIs are detached properly
        return registrationBean;
    }
}

class Servlet extends VaadinServlet {

    @Override
    protected void servletInitialized() throws ServletException {
        final WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        getService().addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent sessionInitEvent) throws ServiceException {
                // TODO Move anonymous UIProvider implementation to its own class
                sessionInitEvent.getSession().addUIProvider(new UIProvider() {
                    @Override
                    public Class<? extends UI> getUIClass(UIClassSelectionEvent uiClassSelectionEvent) {
                        return SpringManagedUI.class;
                    }

                    @Override
                    public UI createInstance(UICreateEvent event) {
                        final int uiId = event.getUiId();
                        final Class<VaadinUiIdentifier> key = VaadinUiIdentifier.class;
                        CurrentInstance.set(key, new VaadinUiIdentifier(uiId));
                        try {
                            // TODO Replace with log entry
                            System.out.println("Creating new UI instance with id " + uiId);
                            return webApplicationContext.getBean(event.getUIClass());
                        } finally {
                            CurrentInstance.set(key, null);
                        }
                    }
                });
            }
        });
    }
}

class VaadinUiIdentifier {
    private final int uiId;

    public VaadinUiIdentifier(int uiId) {
        this.uiId = uiId;
    }

    public VaadinUiIdentifier(UI ui) {
        this.uiId = ui.getUIId();
    }

    public int getUiId() {
        return uiId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        VaadinUiIdentifier that = (VaadinUiIdentifier) o;

        if (uiId != that.uiId) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uiId;
    }

    @Override
    public String toString() {
        return String.format("%s(%d)", VaadinUiIdentifier.class.getSimpleName(), uiId);
    }
}

/**
 * this implementation is based on https://raw.github.com/michaldo/spring-ui-scope/master/src/main/java/com/cybercom/vaadin/spring/UIScope.java
 *
 * @author michaldo
 * @author joshlong
 * @author peholmst
 */
class VaadinUiScope implements Scope, DetachListener, BeanFactoryPostProcessor {

    private final Map<VaadinUiIdentifier, Map<String, Object>> objectMap = Collections.synchronizedMap(new HashMap<VaadinUiIdentifier, Map<String, Object>>());
    private final Map<VaadinUiIdentifier, Map<String, Runnable>> destructionCallbackMap = Collections.synchronizedMap(new HashMap<VaadinUiIdentifier, Map<String, Runnable>>());

    private VaadinUiIdentifier currentUiId() {
        final UI currentUI = UI.getCurrent();
        if (currentUI != null) {
            return new VaadinUiIdentifier(currentUI);
        } else {
            VaadinUiIdentifier currentIdentifier = CurrentInstance.get(VaadinUiIdentifier.class);
            Assert.notNull(currentIdentifier, "found no valid " + VaadinUiIdentifier.class.getName() + " instance!");
            return currentIdentifier;
        }
    }

    @Override
    public Object get(String name, ObjectFactory<?> objectFactory) {
        final VaadinUiIdentifier uiIdentifier = currentUiId();
        System.out.println("Getting bean with name " + name + " [UI-ID: " + uiIdentifier + "]");
        Map<String, Object> uiSpace = objectMap.get(uiIdentifier);
        if (uiSpace == null) {
            System.out.println("Creating new uiSpace [UI-ID: " + uiIdentifier + "]");
            uiSpace = Collections.synchronizedMap(new LinkedHashMap<String, Object>());
            objectMap.put(uiIdentifier, uiSpace);
        }

        Object bean = uiSpace.get(name);
        if (bean == null) {
            System.out.println("Bean " + name + " not found in uiSpace, invoking object factory [UI-ID: " + uiIdentifier + "]");
            bean = objectFactory.getObject();
            if (bean instanceof UI) {
                System.out.println("Registering DetachListener with " + bean + "[UI-ID: " + uiIdentifier + "]");
                ((UI) bean).addDetachListener(this);
            }
            uiSpace.put(name, bean);
        }

        System.out.println("Returning bean " + bean + " with name " + name + " [UI-ID: " + uiIdentifier + "]");
        return bean;
    }

    @Override
    public Object remove(String name) {
        final VaadinUiIdentifier uiIdentifier = currentUiId();
        System.out.println("Removing bean with name " + name + " [UI-ID: " + uiIdentifier + "]");

        final Map<String, Runnable> destructionSpace = destructionCallbackMap.get(uiIdentifier);
        if (destructionSpace != null) {
            destructionSpace.remove(name);
        }

        final Map<String, Object> uiSpace = objectMap.get(uiIdentifier);
        if (uiSpace != null) {
            try {
                return uiSpace.remove(name);
            } finally {
                if (uiSpace.isEmpty()) {
                    System.out.println("UiSpace empty, removing [UI-ID: " + uiIdentifier + "]");
                    objectMap.remove(uiIdentifier);
                }
            }
        }
        return null;
    }

    @Override
    public void registerDestructionCallback(String name, Runnable callback) {
        final VaadinUiIdentifier uiIdentifier = currentUiId();
        System.out.println("Registering destruction callback " + callback + " for bean with name " + name + " [UI-ID: " + uiIdentifier + "]");
        Map<String, Runnable> destructionSpace = destructionCallbackMap.get(uiIdentifier);
        if (destructionSpace == null) {
            destructionSpace = Collections.synchronizedMap(new LinkedHashMap<String, Runnable>());
            destructionCallbackMap.put(uiIdentifier, destructionSpace);
        }
        destructionSpace.put(name, callback);
    }

    @Override
    public Object resolveContextualObject(String key) {
        return null;
    }

    @Override
    public String getConversationId() {
        return currentUiId().toString();
    }

    @Override
    public void detach(DetachEvent event) {
        System.out.println("Received DetachEvent from " + event.getSource());
        final VaadinUiIdentifier uiIdentifier = new VaadinUiIdentifier((UI) event.getSource());
        final Map<String, Runnable> destructionSpace = destructionCallbackMap.remove(uiIdentifier);
        if (destructionSpace != null) {
            for (Runnable runnable : destructionSpace.values()) {
                runnable.run();
            }
        }
        objectMap.remove(uiIdentifier);
    }

    @Override
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("Registering UI scope with beanFactory " + beanFactory);
        beanFactory.registerScope("ui", this);
    }

}
