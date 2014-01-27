package org.vaadin.webinars.springandvaadin.serialization;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TextField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;

import javax.annotation.PostConstruct;

/**
 * @author petter@vaadin.com
 */
@org.springframework.stereotype.Component
@Scope("prototype")
public class SerializableComponent extends CustomComponent {

    TextField textField;

    @Autowired
    transient BackendInterface backend;

    @PostConstruct
    void init() {
        setCompositionRoot(textField = new TextField());
    }

}
