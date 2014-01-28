package org.vaadin.spring;


import com.vaadin.server.ClientConnector;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.Scope;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * Implementation of Spring's {@link org.springframework.beans.factory.config.Scope} contract.
 * Registered by default as the scope {@code ui}.
 *
 * @author Josh Long (josh@joshlong.com)
 * @author Petter Holmström (petter@vaadin.com)
 * @see org.vaadin.spring.UiScoped
 */
public class VaadinUiScope implements Scope, ClientConnector.DetachListener, BeanFactoryPostProcessor {

    private Logger logger = Logger.getLogger(getClass()) ;

    private final Map<VaadinUiIdentifier, Map<String, Object>> objectMap =
            new ConcurrentHashMap<VaadinUiIdentifier, Map<String, Object>>();

    private final Map<VaadinUiIdentifier, Map<String, Runnable>> destructionCallbackMap =
            new ConcurrentHashMap<VaadinUiIdentifier, Map<String, Runnable>>();

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
            logger.debug("Creating new uiSpace [UI-ID: " + uiIdentifier + "]");
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
    public void detach(ClientConnector.DetachEvent event) {
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
