package org.vaadin.webinars.springandvaadin.serialization;

import org.springframework.stereotype.Service;

/**
 * @author petter@vaadin.com
 */
@Service
public class BackendClass implements BackendInterface {
    @Override
    public void aMethod() {
        System.out.println("aMethod");
    }
}
