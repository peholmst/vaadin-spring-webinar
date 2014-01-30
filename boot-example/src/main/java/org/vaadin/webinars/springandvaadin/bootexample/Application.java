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
package org.vaadin.webinars.springandvaadin.bootexample;

import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.vaadin.spring.VaadinUI;

interface ContactRepository extends JpaRepository<Contact, Long> {
}

@EnableAutoConfiguration
@EnableTransactionManagement
@ComponentScan
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}

@VaadinUI
class ContactUI extends UI {

    @Autowired
    ContactRepository repository;
    private BeanItemContainer<Contact> contactsContainer;
    private Table contactsTable;
    private Button refresh;
    private Button add;

    @Override
    protected void init(VaadinRequest request) {
        VerticalLayout layout = new VerticalLayout();
        layout.setSizeFull();
        layout.setMargin(true);
        layout.setSpacing(true);
        setContent(layout);

        Label title = new Label("Contacts");
        title.addStyleName(Reindeer.LABEL_H1);
        layout.addComponent(title);

        contactsContainer = new BeanItemContainer<>(Contact.class);

        contactsTable = new Table();
        contactsTable.setSizeFull();
        contactsTable.setContainerDataSource(contactsContainer);
        contactsTable.setVisibleColumns("firstName", "lastName", "email");

        layout.addComponent(contactsTable);
        layout.setExpandRatio(contactsTable, 1f);

        HorizontalLayout toolbar = new HorizontalLayout();
        toolbar.setSpacing(true);
        layout.addComponent(toolbar);

        refresh = new Button("Refresh", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                refresh();
            }
        });
        toolbar.addComponent(refresh);

        add = new Button("Add...", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                addWindow(new NewContactWindow());
            }
        });
        toolbar.addComponent(add);

        refresh();
    }

    private void refresh() {
        contactsContainer.removeAllItems();
        contactsContainer.addAll(repository.findAll());
    }

    class NewContactWindow extends Window {

        private TextField firstName = new TextField("First name");
        private TextField lastName = new TextField("Last name");
        private TextField email = new TextField("E-mail");

        private Button commit = new Button("Save & Close", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                repository.saveAndFlush(newContact);
                close();
                refresh();
            }
        });

        private Contact newContact = new Contact();

        public NewContactWindow() {
            setCaption("Add new contact");
            setModal(true);
            setResizable(false);

            // Build layout containing fields and button
            FormLayout form = new FormLayout(firstName, lastName, email);
            VerticalLayout verticalLayout = new VerticalLayout(form, commit);
            verticalLayout.setMargin(true);
            verticalLayout.setSpacing(true);
            setContent(verticalLayout);

            // Bind fields to entity properties by naming convention
            BeanFieldGroup<Contact> binder = new BeanFieldGroup<>(Contact.class);
            binder.setBuffered(false);
            binder.bindMemberFields(NewContactWindow.this);
            binder.setItemDataSource(newContact);

            // Automatically focus the firsName field
            firstName.focus();

        }

    }

}
