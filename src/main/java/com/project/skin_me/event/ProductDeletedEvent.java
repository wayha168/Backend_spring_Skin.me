// com.project.skin_me.event.ProductDeletedEvent
package com.project.skin_me.event;

import org.springframework.context.ApplicationEvent;

public class ProductDeletedEvent extends ApplicationEvent {
    public ProductDeletedEvent(Object source) {
        super(source);
    }
}