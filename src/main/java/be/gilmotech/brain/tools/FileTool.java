package be.gilmotech.brain.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class FileTool {

    @Value("${brain.sandbox.path}")
    private String sandboxPath;

    private Path resolve(String relativePath) {
        Path sandbox = Paths.get(sandboxPath).toAbsolutePath().normalize();
        Path resolved = sandbox.resolve(relativePath).normalize();
        if (!resolved.startsWith(sandbox)) {
            throw new SecurityException("Path traversal attempt blocked: " + relativePath);
        }
        return resolved;
    }

    public String readFile(String path) {
        long start = System.currentTimeMillis();
        try {
            Path target = resolve(path);
            String content = Files.readString(target);
            log.info("FileTool read: path='{}' bytes={} duration={}ms",
                    path, content.length(), System.currentTimeMillis() - start);
            return content;
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Cannot read file: " + e.getMessage(), e);
        }
    }

    public void writeFile(String path, String content) {
        long start = System.currentTimeMillis();
        try {
            Path target = resolve(path);
            Files.createDirectories(target.getParent());
            Files.writeString(target, content);
            log.info("FileTool write: path='{}' bytes={} duration={}ms",
                    path, content.length(), System.currentTimeMillis() - start);
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Cannot write file: " + e.getMessage(), e);
        }
    }

    public List<String> listFiles(String path) {
        long start = System.currentTimeMillis();
        try {
            Path target = resolve(path);
            List<String> files;
            try (Stream<Path> stream = Files.list(target)) {
                files = stream
                        .map(p -> Paths.get(sandboxPath).toAbsolutePath().normalize().relativize(p).toString())
                        .collect(Collectors.toList());
            }
            log.info("FileTool list: path='{}' count={} duration={}ms",
                    path, files.size(), System.currentTimeMillis() - start);
            return files;
        } catch (SecurityException e) {
            throw e;
        } catch (IOException e) {
            throw new RuntimeException("Cannot list directory: " + e.getMessage(), e);
        }
    }
}
