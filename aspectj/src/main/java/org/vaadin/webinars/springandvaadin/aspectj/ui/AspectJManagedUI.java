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
package org.vaadin.webinars.springandvaadin.aspectj.ui;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Configurable;

/**
 * @author petter@vaadin.com
 */
@Configurable
public class AspectJManagedUI extends UI {

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
        navigator.addView("rooms", new RoomsView());
        navigator.addView("chat", ChatView.class);
        if (navigator.getState().isEmpty()) {
            navigator.navigateTo("rooms");
        }
        setPollInterval(800);
    }

    public String getAuthor() {
        return author.getValue();
    }
}
