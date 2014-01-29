package org.vaadin.webinars.springandvaadin.bootexample;

import javax.persistence.*;

/**
 * This class must be public, otherwise the BeanItemContainer won't work properly (it uses introspection)
 *
 * @author petter@vaadin.com
 */
@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue
    @Column(name = "contact_id")
    private long id;
    @Column(name = "first_name")
    private String firstName = "";
    @Column(name = "last_name")
    private String lastName = "";
    @Column(name = "email")
    private String email = "";

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
