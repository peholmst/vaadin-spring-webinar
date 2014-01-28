package org.vaadin.webinars.springandvaadin.single.ui;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

/**
 * @author petter@vaadin.com
 */
@ManagedComponent
@Scope("ui")
public class SpringManagedUI extends UI {

    @Autowired
    SpringViewProvider viewProvider;
    private TextField author;
    private Panel viewContainer;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setSpacing(true);
        layout.setMargin(true);

        author = new TextField("Author");
        author.setImmediate(true);
        layout.addComponent(author);

        viewContainer = new Panel();
        viewContainer.setSizeFull();
        layout.addComponent(viewContainer);
        layout.setExpandRatio(viewContainer, 1f);

        setContent(layout);

        Navigator navigator = new Navigator(this, viewContainer);
        navigator.addProvider(viewProvider);
        if (navigator.getState().isEmpty()) {
            navigator.navigateTo("rooms");
        }
        setPollInterval(800);
    }

    public String getAuthor() {
        return author.getValue();
    }
}
