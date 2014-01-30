/*
 * Copyright 2014 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.webinars.springandvaadin.i18n.ui;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * @author petter@vaadin.com
 */
@WebServlet(urlPatterns = "/*")
@VaadinServletConfiguration(ui = I18nUI.class, productionMode = false)
public class Servlet extends VaadinServlet {

    private LocaleResolver localeResolver;

    @Override
    protected void servletInitialized() throws ServletException {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        try {
            localeResolver = context.getBean(DispatcherServlet.LOCALE_RESOLVER_BEAN_NAME, LocaleResolver.class);
        } catch (NoSuchBeanDefinitionException e) {
            localeResolver = new SessionLocaleResolver();
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        final Locale locale = localeResolver.resolveLocale(request);
        LocaleContextHolder.setLocale(locale);
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);
        try {
            super.service(new HttpServletRequestWrapper(request) {
                @Override
                public Locale getLocale() {
                    return locale;
                }
            }, response);
        } finally {
            if (!locale.equals(LocaleContextHolder.getLocale())) {
                localeResolver.setLocale(request, response, LocaleContextHolder.getLocale());
            }
            LocaleContextHolder.resetLocaleContext();
            RequestContextHolder.resetRequestAttributes();
        }
    }
}
