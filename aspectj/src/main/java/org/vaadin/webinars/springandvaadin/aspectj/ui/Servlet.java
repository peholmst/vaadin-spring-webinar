package org.vaadin.webinars.springandvaadin.aspectj.ui;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.*;
import com.vaadin.ui.UI;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

/**
 * @author petter@vaadin.com
 */
@WebServlet(urlPatterns = "/*")
@VaadinServletConfiguration(ui = AspectJManagedUI.class, productionMode = false)
public class Servlet extends VaadinServlet {

}
