package org.vaadin.webinars.springandvaadin.multiple.ui;

import com.vaadin.event.ShortcutAction;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.Reindeer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.vaadin.webinars.springandvaadin.multiple.backend.ChatMessage;
import org.vaadin.webinars.springandvaadin.multiple.backend.ChatService;
import org.vaadin.webinars.springandvaadin.multiple.backend.MessagePostedEvent;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

/**
 * @author petter@vaadin.com
 */
@ManagedComponent("chat")
@Scope("prototype")
public class ChatView extends VerticalLayout implements View, ApplicationListener<MessagePostedEvent> {

    @Autowired
    SpringManagedUI ui;
    @Autowired
    ChatService chatService;
    @Autowired
    EventBroker eventBroker;
    private Label roomLabel;
    private Panel messagesPanel;
    private VerticalLayout messagesLayout;
    private TextField message;
    private Button post;
    private String room;

    @PostConstruct
    void init() {
        setSizeFull();
        setMargin(true);
        setSpacing(true);

        roomLabel = new Label();
        roomLabel.addStyleName(Reindeer.LABEL_H1);
        addComponent(roomLabel);

        messagesPanel = new Panel();
        messagesPanel.setSizeFull();
        addComponent(messagesPanel);
        setExpandRatio(messagesPanel, 1f);

        messagesLayout = new VerticalLayout();
        messagesLayout.setMargin(true);
        messagesLayout.setSpacing(true);
        messagesPanel.setContent(messagesLayout);

        HorizontalLayout bar = new HorizontalLayout();
        bar.setSpacing(true);
        addComponent(bar);

        message = new TextField();
        bar.addComponent(message);

        post = new Button("Post", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent event) {
                chatService.post(new ChatMessage(ui.getAuthor(), room, message.getValue()));
                message.setValue("");
                message.focus();
            }
        });
        post.setClickShortcut(ShortcutAction.KeyCode.ENTER);
        bar.addComponent(post);

        eventBroker.addApplicationListener(this);
    }

    @PreDestroy
    void destroy() {
        eventBroker.removeApplicationListener(this);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent viewChangeEvent) {
        room = viewChangeEvent.getParameters();
        for (ChatMessage message : chatService.getMessagesInRoom(room)) {
            addMessage(message);
        }
    }

    private void addMessage(ChatMessage message) {
        messagesLayout.addComponent(new Label(String.format("%s %s: %s", message.getTimestamp(),
                message.getSender(), message.getMessage())));
    }

    @Override
    public void onApplicationEvent(MessagePostedEvent event) {
        if (event.getMessage().getRoom().equals(room)) {
            addMessage(event.getMessage());
        }
    }
}
