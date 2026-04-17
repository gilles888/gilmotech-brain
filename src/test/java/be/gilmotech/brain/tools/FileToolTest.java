package be.gilmotech.brain.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileToolTest {

    private FileTool fileTool;

    @TempDir
    Path sandbox;

    @BeforeEach
    void setUp() {
        fileTool = new FileTool();
        ReflectionTestUtils.setField(fileTool, "sandboxPath", sandbox.toString());
    }

    @Test
    void writeAndReadFile() {
        fileTool.writeFile("test.txt", "hello world");
        String content = fileTool.readFile("test.txt");
        assertThat(content).isEqualTo("hello world");
    }

    @Test
    void listFilesInSandbox() {
        fileTool.writeFile("a.txt", "A");
        fileTool.writeFile("b.txt", "B");
        List<String> files = fileTool.listFiles(".");
        assertThat(files).hasSize(2);
    }

    @Test
    void pathTraversalIsBlocked() {
        assertThatThrownBy(() -> fileTool.readFile("../../etc/passwd"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal");
    }

    @Test
    void pathTraversalWithAbsolutePathIsBlocked() {
        assertThatThrownBy(() -> fileTool.readFile("/etc/passwd"))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("Path traversal");
    }

    @Test
    void nestedDirectoriesAreCreated() {
        fileTool.writeFile("sub/dir/file.txt", "nested");
        assertThat(fileTool.readFile("sub/dir/file.txt")).isEqualTo("nested");
    }
}
