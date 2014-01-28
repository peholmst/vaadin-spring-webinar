package org.vaadin.webinars.springandvaadin.single;


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
import org.vaadin.webinars.springandvaadin.single.ui.SpringManagedUI;

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
        return new ServletRegistrationBean(
                new Servlet(), "/*", "/VAADIN/*");
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

    public int getUiId() {
        return uiId;
    }

    public VaadinUiIdentifier(int uiId) {
        this.uiId = uiId;
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
 * this implementation is from https://raw.github.com/michaldo/spring-ui-scope/master/src/main/java/com/cybercom/vaadin/spring/UIScope.java
 *
 * @author michaldo
 */
class VaadinUiScope implements Scope, DetachListener, BeanFactoryPostProcessor {

    private final Map<VaadinUiIdentifier, Map<String, Object>> objectMap = Collections.synchronizedMap(new HashMap<VaadinUiIdentifier, Map<String, Object>>());
//    private final Map<VaadinUiIdentifier, Map<String, Runnable>> destructionCallbackMap = Collections.synchronizedMap(new HashMap<Integer, Map<String, Runnable>>());


    private VaadinUiIdentifier currentUiId() {
        final UI currentUI = UI.getCurrent();
        if (currentUI != null && currentUI.getUIId() != -1) {
            return new VaadinUiIdentifier(currentUI.getUIId());
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
        // TODO Remove registered destructionCallbacks
        Map<String, Object> uiSpace = objectMap.get(uiIdentifier);
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
   /*     UI ui = UI.getCurrent();
        Map<String, Runnable> destructionSpace = destructionCallbackMap.get(ui);
        if (destructionSpace == null) {
            destructionSpace = Collections.synchronizedMap(new HashMap<String, Runnable>());
            destructionCallbackMap.put(ui, destructionSpace);
        }
        destructionSpace.put(name, callback);*/
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
/*        UI ui = (UI) event.getSource();

        Map<String, Runnable> destructionSpace = destructionCallbackMap.remove(ui);
        if (destructionSpace != null) {
            for (Runnable runnable : destructionSpace.values()) {
                runnable.run();
            }
        }

        objectMap.remove(ui);*/
    }

    @Override
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {
        System.out.println("Registering UI scope with beanFactory " + beanFactory);
        beanFactory.registerScope("ui", this);
    }

}
 /*
class CDIUIProvider extends DefaultUIProvider implements Serializable {

    @Override
    public UI createInstance(UICreateEvent uiCreateEvent) {
        Class<? extends UI> type = uiCreateEvent.getUIClass();
        int uiId = uiCreateEvent.getUiId();
        VaadinRequest request = uiCreateEvent.getRequest();
        Bean<?> bean = scanForBeans(type);
        String uiMapping = "";
        if (bean == null) {
            if (type.isAnnotationPresent(CDIUI.class)) {
                uiMapping = parseUIMapping(request);
                bean = getUIBeanWithMapping(uiMapping);
            } else {
                throw new IllegalStateException("UI class: " + type.getName()
                        + " with mapping: " + uiMapping
                        + " is not annotated with CDIUI!");
            }
        }
        UIBean uiBean = new UIBean(bean, uiId);
        try {
            // Make the UIBean available to UIScopedContext when creating nested
            // injected objects
            CurrentInstance.set(UIBean.class, uiBean);
            return (UI) getBeanManager().getReference(uiBean, type,
                    getBeanManager().createCreationalContext(bean));
        } finally {
            CurrentInstance.set(UIBean.class, null);
        }
    }

    @Override
    public Class<? extends UI> getUIClass(UIClassSelectionEvent selectionEvent) {
        VaadinRequest request = selectionEvent.getRequest();
        String uiMapping = parseUIMapping(request);
        if (isRoot(request)) {
            return rootUI();
        }
        Bean<?> uiBean = getUIBeanWithMapping(uiMapping);

        if (uiBean != null) {
            return uiBean.getBeanClass().asSubclass(UI.class);
        }

        if (uiMapping.isEmpty()) {
            // See if UI is configured to web.xml with VaadinCDIServlet. This is
            // done only if no specific UI name is given.
            return super.getUIClass(selectionEvent);
        }

        return null;
    }

    boolean isRoot(VaadinRequest request) {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            return false;
        }

        return pathInfo.equals("/");
    }

    Class<? extends UI> rootUI() {
        Set<Bean<?>> rootBeans = AnnotationUtil
                .getRootUiBeans(getBeanManager());
        if (rootBeans.isEmpty()) {
            return null;
        }
        if (rootBeans.size() > 1) {
            StringBuilder errorMessage = new StringBuilder();
            for (Bean<?> bean : rootBeans) {
                errorMessage.append(bean.getBeanClass().getName());
                errorMessage.append("\n");
            }
            throw new IllegalStateException(
                    "Multiple beans are annotated with @CDIUI without context path: "
                            + errorMessage.toString());
        }
        Bean<?> uiBean = rootBeans.iterator().next();
        Class<?> rootUI = uiBean.getBeanClass();
        return rootUI.asSubclass(UI.class);
    }

    private Bean<?> getUIBeanWithMapping(String mapping) {
        Set<Bean<?>> beans = AnnotationUtil.getUiBeans(getBeanManager());

        for (Bean<?> bean : beans) {
            // We need this check since the returned beans can also be producers
            if (UI.class.isAssignableFrom(bean.getBeanClass())) {
                Class<? extends UI> beanClass = bean.getBeanClass().asSubclass(
                        UI.class);

                if (beanClass.isAnnotationPresent(CDIUI.class)) {
                    String computedMapping = Conventions
                            .deriveMappingForUI(beanClass);
                    if (mapping.equals(computedMapping)) {
                        return bean;
                    }
                }
            }
        }

        return null;
    }

    private Bean<?> scanForBeans(Class<? extends UI> type) {

        Set<Bean<?>> beans = getBeanManager().getBeans(type,
                new AnnotationLiteral<Any>() {
                });

        if (beans.isEmpty()) {
            getLogger().warning(
                    "Could not find UI bean for " + type.getCanonicalName());
            return null;
        }

        if (beans.size() > 1) {
            getLogger().warning(
                    "Found multiple UI beans for " + type.getCanonicalName());
            return null;
        }

        return beans.iterator().next();
    }

    String parseUIMapping(VaadinRequest request) {
        return parseUIMapping(request.getPathInfo());
    }

    String parseUIMapping(String requestPath) {
        if (requestPath != null && requestPath.length() > 1) {
            String path = requestPath;
            if (requestPath.endsWith("/")) {
                path = requestPath.substring(0, requestPath.length() - 1);
            }
            if (!path.contains("!")) {
                int lastIndex = path.lastIndexOf('/');
                return path.substring(lastIndex + 1);
            } else {
                int lastIndexOfBang = path.lastIndexOf('!');
                // strip slash with bank => /!
                String pathWithoutView = path.substring(0, lastIndexOfBang - 1);
                int lastSlashIndex = pathWithoutView.lastIndexOf('/');
                return pathWithoutView.substring(lastSlashIndex + 1);
            }
        }
        return "";
    }

    // TODO a better way to do this could be custom injection management in the
    // Extension if feasible
    private BeanManager beanManager;

    private BeanManager getBeanManager() {
        if (beanManager == null) {
            // as the CDIUIProvider is not injected, need to use JNDI lookup
            try {
                InitialContext initialContext = new InitialContext();
                beanManager = (BeanManager) initialContext
                        .lookup("java:comp/BeanManager");
            } catch (NamingException e) {
                getLogger().severe("Could not get BeanManager through JNDI");
                beanManager = null;
            }
        }
        return beanManager;
    }

    private static Logger getLogger() {
        return Logger.getLogger(CDIUIProvider.class.getCanonicalName());
    }
} */