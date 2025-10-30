package com.project.skin_me.util;

import java.nio.file.Files;
import java.nio.file.Paths;

public final class MarkdownCatalogLoader {

    private static final String FILE_PATH = "exported-catalog/product-catalog.md";

    private MarkdownCatalogLoader() {}

    public static String load() throws Exception {
        var path = Paths.get(FILE_PATH);
        if (!Files.exists(path)) {
            throw new IllegalStateException("Product catalog not found. Ask admin to export it first.");
        }
        return Files.readString(path);
    }
}