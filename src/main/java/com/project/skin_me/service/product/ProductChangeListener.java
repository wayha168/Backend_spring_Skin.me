package com.project.skin_me.service.product;

import com.project.skin_me.event.ProductAddedEvent;
import com.project.skin_me.event.ProductDeletedEvent;
import com.project.skin_me.event.ProductUpdatedEvent;
import com.project.skin_me.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductChangeListener {

    private final IProductService productService;

    @Value("${catalog.export.dir}")
    private String exportDir;

    private Path getExportDir() {
        return Paths.get(exportDir);
    }

    private Path getCatalogFile() {
        return getExportDir().resolve("product-catalog.md");
    }

    @Async
    @EventListener({ProductAddedEvent.class, ProductUpdatedEvent.class, ProductDeletedEvent.class})
    public void onProductChange() {
        try {
            Path exportPath = getExportDir();
            Path catalogFile = getCatalogFile();

            Files.createDirectories(exportPath);

            var products = productService.getAllProducts();
            String md = productService.toMarkdownTable(products);
            if (md == null || md.isBlank()) md = "_No products available._\n";

            Files.writeString(catalogFile, md);

            System.out.println("[CATALOG] Exported to " + catalogFile.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("[CATALOG] EXPORT FAILED!");
            e.printStackTrace();
        }
    }
}