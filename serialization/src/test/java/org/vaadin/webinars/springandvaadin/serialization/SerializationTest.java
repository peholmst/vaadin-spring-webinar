package org.vaadin.webinars.springandvaadin.serialization;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.*;

import static org.junit.Assert.*;

/**
 * @author petter@vaadin.com
 */
public class SerializationTest {

    ClassPathXmlApplicationContext applicationContext;

    static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(byteArrayOutputStream)) {
            oos.writeObject(obj);
        }
        return byteArrayOutputStream.toByteArray();
    }

    static Object deserialize(byte[] buffer) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        try (ObjectInputStream ois = new ObjectInputStream(byteArrayInputStream)) {
            return ois.readObject();
        }
    }


    @Before
    public void setUp() {
        applicationContext = new ClassPathXmlApplicationContext("org/vaadin/webinars/springandvaadin/serialization/uiContext.xml");
        SerializableUI.applicationContextHolder.set(applicationContext);
    }

    @After
    public void tearDown() {
        SerializableUI.applicationContextHolder.remove();
        applicationContext.close();
    }

    @Test
    public void serializeAndDeserialize() throws IOException, ClassNotFoundException {
        SerializableUI ui = applicationContext.getBean(SerializableUI.class);
        ui.component.textField.setValue("A state");

        assertSame(applicationContext, ui.applicationContext);
        assertNotNull(ui.backend);
        assertSame(ui.backend, ui.component.backend);
        assertEquals("A state", ui.component.textField.getValue());

        SerializableUI otherUi = (SerializableUI) deserialize(serialize(ui));

        assertSame(applicationContext, otherUi.applicationContext);
        assertNotNull(otherUi.backend);
        assertSame(ui.backend, ui.component.backend);
        // Component has been recreated and has lost all of its state
        assertEquals("", otherUi.component.textField.getValue());
    }
}
