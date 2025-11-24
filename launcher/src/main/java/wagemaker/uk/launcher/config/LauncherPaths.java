package wagemaker.uk.launcher.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Centralizes all filesystem locations used by the launcher.
 */
public final class LauncherPaths {
    private static final String CONFIG_ROOT = ".config";
    private static final String APP_DIR = "woodlanders";
    private static final String CLIENT_JAR = "woodlanders-client.jar";
    private static final String VERSION_FILE = "version.json";

    private LauncherPaths() {
    }

    public static Path configDirectory() {
        String home = System.getProperty("user.home");
        Objects.requireNonNull(home, "user.home system property is missing");
        return Path.of(home, CONFIG_ROOT, APP_DIR);
    }

    public static Path clientJarPath() {
        return configDirectory().resolve(CLIENT_JAR);
    }

    public static Path versionMetadataPath() {
        return configDirectory().resolve(VERSION_FILE);
    }

    public static Path tempDownloadPath() throws IOException {
        return Files.createTempFile(configDirectory(), "woodlanders-client", ".tmp");
    }

    public static void ensureConfigDirectory() throws IOException {
        Files.createDirectories(configDirectory());
    }
}
