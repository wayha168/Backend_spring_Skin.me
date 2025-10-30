package com.project.skin_me.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public final class MarkdownCatalogLoader {

    private static final Path FILE_PATH = Paths.get("/home/skinme/exported-catalog/product-catalog.md");

    private MarkdownCatalogLoader() {}

    public static String load() throws Exception {
        if (!Files.exists(FILE_PATH)) {
            throw new IllegalStateException(
                    "Product catalog not found. Ask admin to export it first."
            );
        }
        return Files.readString(FILE_PATH);
    }
}
