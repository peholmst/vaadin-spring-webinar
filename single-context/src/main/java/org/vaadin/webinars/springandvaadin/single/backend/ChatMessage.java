package org.vaadin.webinars.springandvaadin.single.backend;

import java.io.Serializable;
import java.util.Date;

/**
 * @author petter@vaadin.com
 */
public class ChatMessage implements Serializable {

    private final String sender;
    private final Date timestamp;
    private final String room;
    private final String message;

    public ChatMessage(String sender, String room, String message) {
        this.sender = sender;
        this.room = room;
        this.message = message;
        timestamp = new Date();
    }

    public String getSender() {
        return sender;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getRoom() {
        return room;
    }

    public String getMessage() {
        return message;
    }
}
