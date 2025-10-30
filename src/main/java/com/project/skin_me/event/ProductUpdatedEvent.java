
package com.project.skin_me.event;

import org.springframework.context.ApplicationEvent;

public class ProductUpdatedEvent extends ApplicationEvent {
    public ProductUpdatedEvent(Object source) {
        super(source);
    }
}