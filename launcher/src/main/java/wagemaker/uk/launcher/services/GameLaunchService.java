package wagemaker.uk.launcher.services;

import wagemaker.uk.launcher.config.LauncherPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Spawns the actual Woodlanders Java client.
 */
public class GameLaunchService {
    private static final Logger LOG = LoggerFactory.getLogger(GameLaunchService.class);

    public Process launchClient() throws IOException {
        Path jarPath = LauncherPaths.clientJarPath();
        if (!Files.exists(jarPath)) {
            throw new IOException("Woodlanders client jar not found at " + jarPath);
        }
        ProcessBuilder builder = new ProcessBuilder("java", "-jar", jarPath.toString());
        builder.directory(LauncherPaths.configDirectory().toFile());
        builder.inheritIO();
        LOG.info("Launching Woodlanders from {}", jarPath);
        return builder.start();
    }
}
