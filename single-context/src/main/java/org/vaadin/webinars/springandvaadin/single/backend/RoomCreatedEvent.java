package org.vaadin.webinars.springandvaadin.single.backend;

import org.springframework.context.ApplicationEvent;

/**
 * @author petter@vaadin.com
 */
public class RoomCreatedEvent extends ApplicationEvent {

    private final String room;

    public RoomCreatedEvent(Object source, String room) {
        super(source);
        this.room = room;
    }

    public String getRoom() {
        return room;
    }
}
