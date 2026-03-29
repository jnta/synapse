package org.synapse.infrastructure.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AtomicFileStore {

    public void writeAtomic(Path targetPath, String content) throws IOException {
        Path parent = targetPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        // Use the same directory for the temp file to ensure it's on the same partition (required for ATOMIC_MOVE)
        Path tempFile = Files.createTempFile(parent != null ? parent : Path.of("."), "synapse-", ".tmp");
        try {
            byte[] bytes = content.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            Files.write(tempFile, bytes, java.nio.file.StandardOpenOption.WRITE, 
                        java.nio.file.StandardOpenOption.CREATE, 
                        java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
            
            Files.move(tempFile, targetPath, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
