/*
 * Copyright 2014 The original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.vaadin.webinars.springandvaadin.aspectj.backend;

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
