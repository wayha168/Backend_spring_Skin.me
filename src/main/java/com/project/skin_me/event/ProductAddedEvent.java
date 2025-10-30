
package com.project.skin_me.event;

import org.springframework.context.ApplicationEvent;

public class ProductAddedEvent extends ApplicationEvent {
    public ProductAddedEvent(Object source) {
        super(source);
    }
}