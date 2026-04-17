package be.gilmotech.brain.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class BashTool {

    @Value("${brain.sandbox.path}")
    private String sandboxPath;

    private static final List<String> BLACKLIST = List.of(
            "rm -rf /", "sudo", "chmod 777", "shutdown", "reboot", "kill",
            "wget http://10.", "wget http://192.", "wget http://172.",
            "curl http://10.", "curl http://192.", "curl http://172.",
            "wget https://10.", "wget https://192.", "wget https://172.",
            "curl https://10.", "curl https://192.", "curl https://172.",
            "> /dev/", "dd if=", "mkfs", "fdisk", "parted"
    );

    public record BashResult(String stdout, String stderr, int exitCode, boolean timedOut) {}

    public BashResult execute(String command) {
        long start = System.currentTimeMillis();

        String lc = command.toLowerCase();
        for (String blocked : BLACKLIST) {
            if (lc.contains(blocked.toLowerCase())) {
                log.warn("BashTool: blocked command containing '{}'", blocked);
                return new BashResult("", "Command blocked by security policy: contains '" + blocked + "'", 1, false);
            }
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
            pb.directory(new File(sandboxPath));
            pb.redirectErrorStream(false);
            Process process = pb.start();

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                log.warn("BashTool: command timed out: {}", command);
                return new BashResult("", "Command timed out after 30 seconds", 1, true);
            }

            String stdout = new String(process.getInputStream().readAllBytes());
            String stderr = new String(process.getErrorStream().readAllBytes());
            int exitCode = process.exitValue();

            log.info("BashTool: cmd='{}' exit={} duration={}ms stdout={}",
                    command.length() > 80 ? command.substring(0, 80) + "..." : command,
                    exitCode, System.currentTimeMillis() - start,
                    stdout.length() > 100 ? stdout.substring(0, 100) + "..." : stdout.trim());

            return new BashResult(stdout, stderr, exitCode, false);

        } catch (IOException | InterruptedException e) {
            log.error("BashTool error: {}", e.getMessage());
            Thread.currentThread().interrupt();
            return new BashResult("", "Execution error: " + e.getMessage(), 1, false);
        }
    }
}
