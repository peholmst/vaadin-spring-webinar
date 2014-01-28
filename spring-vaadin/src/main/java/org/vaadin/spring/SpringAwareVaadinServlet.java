package org.vaadin.spring;

import com.vaadin.server.*;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;

/**
 * Subclass of the standard Vaadin {@link com.vaadin.server.VaadinServlet vaadinServlet} that registers information
 * about the current Vaadin {@link com.vaadin.ui.UI} in a thread-local
 * for the custom {@link org.vaadin.spring.VaadinUiScope scope}.
 *
 * @author petter@vaadin.com
 * @author Josh Long (josh@joshlong.com)
 */
public class SpringAwareVaadinServlet extends VaadinServlet {

    private Class<? extends UI> uiClass;

    public SpringAwareVaadinServlet(Class<? extends UI> uiClass) {
        this.uiClass = uiClass;
    }

    @Override
    protected void servletInitialized() throws ServletException {
        getService().addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent sessionInitEvent) throws ServiceException {
                WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
                UIScopedAwareUiProvider uiProvider = new UIScopedAwareUiProvider(webApplicationContext, uiClass);
                sessionInitEvent.getSession().addUIProvider(uiProvider);
            }
        });
    }

    /**
     *
     */
    public static class UIScopedAwareUiProvider extends UIProvider {

        private Logger logger = Logger.getLogger(getClass());
        private WebApplicationContext webApplicationContext;
        private Class<? extends UI> uiClass;

        public UIScopedAwareUiProvider(WebApplicationContext webApplicationContext, Class<? extends UI> uiClass) {
            this.webApplicationContext = webApplicationContext;
            this.uiClass = uiClass;
        }

        @Override
        public Class<? extends UI> getUIClass(UIClassSelectionEvent uiClassSelectionEvent) {
            return this.uiClass;
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
