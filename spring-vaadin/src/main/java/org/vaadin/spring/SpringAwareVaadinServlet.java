package org.vaadin.spring;

import com.vaadin.server.*;
import com.vaadin.ui.UI;
import com.vaadin.util.CurrentInstance;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;

/**
 * Subclass of the standard Vaadin {@link com.vaadin.server.VaadinServlet vaadinServlet} that registers information
 * about the current Vaadin {@link com.vaadin.ui.UI} in a thread-local
 * for the custom {@link VaadinUIScope scope}.
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

        private Log logger = LogFactory.getLog(getClass());
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
            final String sessionId = event.getRequest().getWrappedSession().getId();
            final Class<VaadinUIIdentifier> key = VaadinUIIdentifier.class;
            CurrentInstance.set(key, new VaadinUIIdentifier(uiId, sessionId));
            try {
                logger.debug(String.format("Creating new UI instance with ID [%d] in session [%s]", uiId, sessionId));
                return webApplicationContext.getBean(event.getUIClass());
            } finally {
                CurrentInstance.set(key, null);
            }
        }
    }
}
