package com.hotel.repository;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File I/O Engine responsible for reading from and writing to disk.
 */
public class FilePersistenceEngine {
    private final String dataDir;

    public FilePersistenceEngine(String dataDir) {
        this.dataDir = dataDir;
        ensureDataDirectoryExists();
    }

    private void ensureDataDirectoryExists() {
        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public synchronized String readFile(String filename) {
        Path path = Paths.get(dataDir, filename);
        if (!Files.exists(path)) {
            return null;
        }
        try {
            return Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("[FilePersistenceEngine] Error reading file " + filename + ": " + e.getMessage());
            return null;
        }
    }

    public synchronized boolean writeFile(String filename, String content) {
        Path path = Paths.get(dataDir, filename);
        try {
            Files.writeString(path, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            System.err.println("[FilePersistenceEngine] Error writing file " + filename + ": " + e.getMessage());
            return false;
        }
    }
}
