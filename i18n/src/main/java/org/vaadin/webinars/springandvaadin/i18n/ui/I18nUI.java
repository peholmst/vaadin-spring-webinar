package org.vaadin.webinars.springandvaadin.i18n.ui;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

/**
 * @author petter@vaadin.com
 */
@Configurable
public class I18nUI extends UI {

    @Autowired
    MessageSource messageSource;

    @Override
    protected void init(VaadinRequest request) {
        setLocale(request.getLocale());

        getPage().setTitle(messageSource.getMessage("page.title", null, getLocale()));

        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        setContent(layout);

        final TextField textField = new TextField(messageSource.getMessage("textField.caption", null, getLocale()));
        layout.addComponent(textField);

        final Button button = new Button(messageSource.getMessage("button.caption", null, getLocale()));
        button.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                Notification.show(messageSource.getMessage("greeting.caption", new Object[]{textField.getValue()}, getLocale()));
            }
        });
        layout.addComponent(button);

        final Button swe = new Button("På svenska", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                LocaleContextHolder.setLocale(new Locale("sv"));
                getPage().reload();
            }
        });
        layout.addComponent(swe);

        final Button eng = new Button("In English", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                LocaleContextHolder.setLocale(new Locale("en"));
                getPage().reload();
            }
        });
        layout.addComponent(eng);

    }
}
