package org.vaadin.webinars.springandvaadin.single.ui;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.vaadin.webinars.springandvaadin.single.backend.ChatService;
import org.vaadin.webinars.springandvaadin.single.backend.RoomCreatedEvent;

import javax.annotation.PostConstruct;

/**
 * @author petter@vaadin.com
 */
@ManagedComponent("rooms")
@Scope("prototype")
public class RoomsView extends VerticalLayout implements View, ApplicationListener<RoomCreatedEvent> {

    @Autowired
    ChatService chatService;
    @Autowired
    ApplicationEventMulticaster eventMulticaster;

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
    }

    @Override
    public void attach() {
        super.attach();
        eventMulticaster.addApplicationListener(this);
    }

    @Override
    public void detach() {
        eventMulticaster.removeApplicationListener(this);
        super.detach();
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
    }

    @Override
    public void onApplicationEvent(RoomCreatedEvent event) {
        rooms.addItem(event.getRoom());
    }
}
