package org.vaadin.webinars.springandvaadin.bootexample;


import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup;
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

        contactsContainer = new BeanItemContainer<Contact>(Contact.class);

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
                add();
            }
        });
        toolbar.addComponent(add);

        refresh();
    }

    private void refresh() {
        contactsContainer.removeAllItems();
        contactsContainer.addAll(repository.findAll());
    }

    private void add() {
        final VerticalLayout windowLayout = new VerticalLayout();
        windowLayout.setSpacing(true);
        windowLayout.setMargin(true);

        final Window window = new Window("Add Contact", windowLayout);
        window.setModal(true);
        window.center();
        window.setResizable(false);

        final FormLayout form = new FormLayout();
        final BeanFieldGroup<Contact> binder = new BeanFieldGroup<Contact>(Contact.class);
        binder.setBuffered(false);
        form.addComponent(binder.buildAndBind("First name", "firstName"));
        form.addComponent(binder.buildAndBind("Last name", "lastName"));
        form.addComponent(binder.buildAndBind("E-mail", "email"));

        final Contact newContact = new Contact();
        binder.setItemDataSource(newContact);

        Button commit = new Button("Save & Close", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                repository.saveAndFlush(newContact);
                window.close();
                refresh();
            }
        });

        windowLayout.addComponent(form);
        windowLayout.addComponent(commit);

        getUI().addWindow(window);
    }
}