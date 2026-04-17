package be.gilmotech.brain.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class BashToolTest {

    private BashTool bashTool;

    @TempDir
    Path sandbox;

    @BeforeEach
    void setUp() {
        bashTool = new BashTool();
        ReflectionTestUtils.setField(bashTool, "sandboxPath", sandbox.toString());
    }

    @Test
    void blocksRmRfSlash() {
        BashTool.BashResult result = bashTool.execute("rm -rf /");
        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.stderr()).contains("blocked");
    }

    @Test
    void blocksSudo() {
        BashTool.BashResult result = bashTool.execute("sudo apt install something");
        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.stderr()).contains("blocked");
    }

    @Test
    void blocksShutdown() {
        BashTool.BashResult result = bashTool.execute("shutdown now");
        assertThat(result.exitCode()).isEqualTo(1);
    }

    @Test
    void blocksPrivateIpWget() {
        BashTool.BashResult result = bashTool.execute("wget http://192.168.1.1/file");
        assertThat(result.exitCode()).isEqualTo(1);
        assertThat(result.stderr()).contains("blocked");
    }

    @Test
    void runsSimpleCommand() {
        BashTool.BashResult result = bashTool.execute("echo hello");
        assertThat(result.exitCode()).isEqualTo(0);
        assertThat(result.stdout()).contains("hello");
        assertThat(result.timedOut()).isFalse();
    }

    @Test
    void runningDirectoryIsSandbox() {
        BashTool.BashResult result = bashTool.execute("pwd");
        assertThat(result.stdout().trim()).isEqualTo(sandbox.toString());
    }
}
