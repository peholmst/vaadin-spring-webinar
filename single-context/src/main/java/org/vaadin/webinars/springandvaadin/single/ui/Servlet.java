package org.vaadin.webinars.springandvaadin.single.ui;

import com.vaadin.server.*;
import com.vaadin.ui.UI;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

/**
 * @author petter@vaadin.com
 */
@WebServlet(urlPatterns = "/*")
public class Servlet extends VaadinServlet {

    @Override
    protected void servletInitialized() throws ServletException {
        final WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        getService().addSessionInitListener(new SessionInitListener() {
            @Override
            public void sessionInit(SessionInitEvent sessionInitEvent) throws ServiceException {
                sessionInitEvent.getSession().addUIProvider(new UIProvider() {
                    @Override
                    public Class<? extends UI> getUIClass(UIClassSelectionEvent uiClassSelectionEvent) {
                        return SpringManagedUI.class;
                    }

                    @Override
                    public UI createInstance(UICreateEvent event) {
                        return webApplicationContext.getBean(event.getUIClass());
                    }
                });
            }
        });
    }
}
