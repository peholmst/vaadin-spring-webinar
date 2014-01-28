package org.vaadin.webinars.springandvaadin.bootexample.backend;

import org.springframework.context.ApplicationEvent;

/**
 * @author petter@vaadin.com
 */
public class MessagePostedEvent extends ApplicationEvent {

    private final ChatMessage message;

    public MessagePostedEvent(Object source, ChatMessage message) {
        super(source);
        this.message = message;
    }

    public ChatMessage getMessage() {
        return message;
    }
}
