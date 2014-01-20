package org.vaadin.webinars.springandvaadin.aspectj.backend;

import java.util.List;

/**
 * @author petter@vaadin.com
 */
public interface ChatService {

    List<String> getRooms();

    void createRoom(String room);

    List<ChatMessage> getMessagesInRoom(String room);

    void post(ChatMessage message);

}
