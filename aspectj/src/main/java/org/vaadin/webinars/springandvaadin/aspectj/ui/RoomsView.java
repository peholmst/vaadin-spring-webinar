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

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.vaadin.webinars.springandvaadin.aspectj.backend.ChatService;
import org.vaadin.webinars.springandvaadin.aspectj.backend.RoomCreatedEvent;

import javax.annotation.PostConstruct;

/**
 * @author petter@vaadin.com
 */
@Configurable
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
