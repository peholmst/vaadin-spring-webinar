package org.vaadin.webinars.springandvaadin.multiple.ui;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.vaadin.webinars.springandvaadin.multiple.backend.ChatService;
import org.vaadin.webinars.springandvaadin.multiple.backend.RoomCreatedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author petter@vaadin.com
 */
@ManagedComponent("rooms")
public class RoomsView extends VerticalLayout implements View, ApplicationListener<RoomCreatedEvent> {

    @Autowired
    ChatService chatService;

    @Autowired
    EventBroker eventBroker;

    private TextField newRoomField;
    private ListSelect rooms;
    private Button addRoom;
    private Button goToRoom;

    @PostConstruct
    void init() {
        setSizeFull();
        setMargin(true);
        setSpacing(true);

        HorizontalLayout bar = new HorizontalLayout();
        bar.setSpacing(true);
        addComponent(bar);

        newRoomField = new TextField();
        bar.addComponent(newRoomField);
        newRoomField.setInputPrompt("Enter name of new room");
        newRoomField.setWidth("15em");

        addRoom = new Button("Add room", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                chatService.createRoom(newRoomField.getValue());
                newRoomField.setValue("");
            }
        });
        bar.addComponent(addRoom);

        rooms = new ListSelect("Rooms");
        rooms.setWidth("200px");
        rooms.setHeight("100%");
        rooms.setNullSelectionAllowed(false);
        addComponent(rooms);
        setExpandRatio(rooms, 1);
        for (String room : chatService.getRooms()) {
            rooms.addItem(room);
        }

        goToRoom = new Button("Go to selected room", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                getUI().getNavigator().navigateTo("chat/" + rooms.getValue());
            }
        });
        addComponent(goToRoom);

        eventBroker.addApplicationListener(this);
    }

    @PreDestroy
    void destroy() {
        eventBroker.removeApplicationListener(this);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
    }

    @Override
    public void onApplicationEvent(RoomCreatedEvent event) {
        rooms.addItem(event.getRoom());
    }
}
