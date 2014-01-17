package org.vaadin.webinars.springandvaadin.multiple.ui;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author petter@vaadin.com
 */
@ManagedComponent
public class EventBroker {

    @Autowired
    ApplicationContext applicationContext;
    private ApplicationEventMulticaster localEventMulticaster = new SimpleApplicationEventMulticaster();
    private ApplicationListener<ApplicationEvent> listener = new ApplicationListener<ApplicationEvent>() {

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            localEventMulticaster.multicastEvent(event);
        }
    };

    public void addApplicationListener(ApplicationListener<?> listener) {
        localEventMulticaster.addApplicationListener(listener);
    }

    public void removeApplicationListener(ApplicationListener<?> listener) {
        localEventMulticaster.removeApplicationListener(listener);
    }

    @PostConstruct
    void init() {
        applicationContext.getParent().getBean(ApplicationEventMulticaster.class).addApplicationListener(listener);
    }

    @PreDestroy
    void destroy() {
        applicationContext.getParent().getBean(ApplicationEventMulticaster.class).removeApplicationListener(listener);
    }

}
