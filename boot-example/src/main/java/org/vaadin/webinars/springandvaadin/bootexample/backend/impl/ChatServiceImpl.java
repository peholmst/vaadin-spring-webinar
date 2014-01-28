package org.vaadin.webinars.springandvaadin.bootexample.backend.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.vaadin.webinars.springandvaadin.bootexample.backend.ChatMessage;
import org.vaadin.webinars.springandvaadin.bootexample.backend.ChatService;
import org.vaadin.webinars.springandvaadin.bootexample.backend.MessagePostedEvent;
import org.vaadin.webinars.springandvaadin.bootexample.backend.RoomCreatedEvent;

import java.util.*;

/**
 * @author petter@vaadin.com
 */
@Service
public class ChatServiceImpl implements ChatService {

    private final Map<String, List<ChatMessage>> messages = new HashMap<>();
    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Override
    public List<String> getRooms() {
        synchronized (messages) {
            return new ArrayList<>(messages.keySet());
        }
    }

    @Override
    public void createRoom(String room) {
        if (room == null || room.isEmpty()) {
            return;
        }
        synchronized (messages) {
            if (messages.containsKey(room)) {
                return;
            }
            messages.put(room, new LinkedList<ChatMessage>());
        }
        eventPublisher.publishEvent(new RoomCreatedEvent(this, room));
    }

    @Override
    public List<ChatMessage> getMessagesInRoom(String room) {
        List<ChatMessage> messageList;
        synchronized (messages) {
            messageList = messages.get(room);
        }
        if (messageList != null) {
            synchronized (messageList) {
                return new ArrayList<>(messageList);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void post(ChatMessage message) {
        List<ChatMessage> messagesList;
        synchronized (messages) {
            messagesList = messages.get(message.getRoom());
        }
        if (messagesList != null) {
            synchronized (messagesList) {
                messagesList.add(message);
            }
            eventPublisher.publishEvent(new MessagePostedEvent(this, message));
        } else {
            throw new IllegalArgumentException("Unknown room: " + message.getRoom());
        }
    }
}
