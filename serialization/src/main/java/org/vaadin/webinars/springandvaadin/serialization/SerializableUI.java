package org.vaadin.webinars.springandvaadin.serialization;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.wiring.BeanConfigurerSupport;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author petter@vaadin.com
 */
@org.springframework.stereotype.Component
@Scope("prototype")
public class SerializableUI extends UI {

    @Autowired
    transient ApplicationContext applicationContext;
    @Autowired
    transient BackendInterface backend;
    @Autowired
    SerializableComponent component;

    @Override
    protected void init(VaadinRequest request) {
    }

    @PostConstruct
    void initComponent() {
        setContent(component);
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        ConfigurableApplicationContext appContext = (ConfigurableApplicationContext) applicationContextHolder.get();
        BeanConfigurerSupport configurerSupport = new BeanConfigurerSupport();
        configurerSupport.setBeanFactory(appContext.getBeanFactory());
        configurerSupport.afterPropertiesSet();
        configurerSupport.configureBean(this);
        configurerSupport.destroy();
    }

    // In a real Spring web app, you would use some other mechanism for getting hold of the app context
    static final ThreadLocal<ApplicationContext> applicationContextHolder = new ThreadLocal<>();
}
