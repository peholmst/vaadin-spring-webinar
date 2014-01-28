package org.vaadin.spring;

import com.vaadin.server.*;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.vaadin.webinars.springandvaadin.bootexample.ui.SpringManagedUI;

import javax.servlet.ServletException;

/**
 * Subclass of the standard Vaadin {@link com.vaadin.server.VaadinServlet vaadinServlet} that registers information
 * about the current Vaadin {@link com.vaadin.ui.UI} in a thread-local
 * for the custom {@link org.vaadin.spring.VaadinUiScope scope}.
 *
 * @author Petter Holmström (petter@vaadin.com)
 * @author Josh Long (josh@joshlong.com)
 */
public class SpringAwareVaadinServlet extends VaadinServlet {

    @Override
    protected void servletInitialized() throws ServletException {
        getService().addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent sessionInitEvent) throws ServiceException {
                UIScopedAwareUiProvider UIScopedAwareUiProvider =
                        new UIScopedAwareUiProvider( WebApplicationContextUtils.getWebApplicationContext(getServletContext()));
                sessionInitEvent.getSession().addUIProvider(UIScopedAwareUiProvider);
            }
        });
    }

    public static class UIScopedAwareUiProvider extends UIProvider {

        private Logger logger = Logger.getLogger(getClass());

        private WebApplicationContext webApplicationContext;

        public UIScopedAwareUiProvider(WebApplicationContext webApplicationContext) {
            this.webApplicationContext = webApplicationContext;
        }

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
                logger.debug("Creating new UI instance with id " + uiId);
                return webApplicationContext.getBean(event.getUIClass());
            } finally {
                CurrentInstance.set(key, null);
            }
        }
    }
}
