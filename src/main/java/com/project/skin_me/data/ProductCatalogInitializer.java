package com.project.skin_me.data;

import com.project.skin_me.service.product.ProductChangeListener;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductCatalogInitializer {

    private final ProductChangeListener productChangeListener;

    @EventListener(ApplicationReadyEvent.class)
    public void exportOnStartup() {
        productChangeListener.onProductChange();
    }
}