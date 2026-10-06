package com.utexo.rgblibjvm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class RgbLibNativeLoader {
    private static final String RESOURCE_PATH = "/native/linux-x86_64/librgblibcffi.so";
    private static volatile boolean loaded;

    private RgbLibNativeLoader() {}

    public static synchronized void load() {
        if (loaded) {
            return;
        }

        String osName = System.getProperty("os.name", "").toLowerCase();
        if (!osName.contains("linux")) {
            throw new IllegalStateException("rgb-lib-jvm currently supports Linux runtime only");
        }

        try (InputStream in = RgbLibNativeLoader.class.getResourceAsStream(RESOURCE_PATH)) {
            if (in == null) {
                throw new IllegalStateException("Native library resource not found: " + RESOURCE_PATH);
            }

            Path tempFile = Files.createTempFile("librgblibcffi-", ".so");
            tempFile.toFile().deleteOnExit();
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            System.load(tempFile.toAbsolutePath().toString());
            loaded = true;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load native rgb-lib library", e);
        }
    }

    public static boolean isLoaded() {
        return loaded;
    }
}
