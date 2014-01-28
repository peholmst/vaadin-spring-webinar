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
import javax.annotation.PreDestroy;

/**
 * @author petter@vaadin.com
 */
@ManagedComponent("rooms")
@Scope("ui")
public class RoomsView extends VerticalLayout implements View {

    private final ApplicationListener<RoomCreatedEvent> roomCreatedEventListener = new ApplicationListener<RoomCreatedEvent>() {
        @Override
        public void onApplicationEvent(RoomCreatedEvent event) {
            rooms.addItem(event.getRoom());
        }
    };
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
        eventMulticaster.addApplicationListener(roomCreatedEventListener);  // TODO Would it be possible to let RoomsView implement ApplicationListener directly and let Spring handle the registration and deregistration?
    }

    @PreDestroy
    void destroy() {
        System.out.println(this + " is cleaning up after itself");
        eventMulticaster.removeApplicationListener(roomCreatedEventListener);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
    }
}
