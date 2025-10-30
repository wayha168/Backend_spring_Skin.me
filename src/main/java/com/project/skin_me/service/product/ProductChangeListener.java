package com.project.skin_me.service.product;

import com.project.skin_me.event.ProductAddedEvent;
import com.project.skin_me.event.ProductDeletedEvent;
import com.project.skin_me.event.ProductUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class ProductChangeListener {

    private final IProductService productService;

    // ---- ALWAYS use an absolute path (project root) ----
    private static final Path EXPORT_DIR = Paths.get(System.getProperty("user.dir"), "exported-catalog");
    private static final Path CATALOG_FILE = EXPORT_DIR.resolve("product-catalog.md");

    @Async
    @EventListener({
            ProductAddedEvent.class,
            ProductUpdatedEvent.class,
            ProductDeletedEvent.class
    })
    public void onProductChange() {
        CompletableFuture.runAsync(() -> {
            try {
                // 1. Make sure the folder exists
                Files.createDirectories(EXPORT_DIR);
                System.out.println("[CATALOG] Export folder ready: " + EXPORT_DIR.toAbsolutePath());

                // 2. Build markdown
                var products = productService.getAllProducts();
                String md = productService.toMarkdownTable(products);
                if (md == null || md.isBlank()) {
                    md = "_No products available._\n";
                }

                // 3. Write file
                Files.writeString(CATALOG_FILE, md);
                System.out.println("[CATALOG] EXPORTED to " + CATALOG_FILE.toAbsolutePath());

            } catch (Exception e) {
                System.err.println("[CATALOG] EXPORT FAILED!");
                e.printStackTrace();                 // <-- THIS SHOWS THE REAL ERROR
            }
        });
    }
}