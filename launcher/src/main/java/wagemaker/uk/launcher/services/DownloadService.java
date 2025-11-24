package wagemaker.uk.launcher.services;

import wagemaker.uk.launcher.config.LauncherPaths;
import wagemaker.uk.launcher.model.DownloadResult;
import wagemaker.uk.launcher.model.ReleaseInfo;
import wagemaker.uk.launcher.util.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Duration;

/**
 * Handles streaming downloads with basic integrity checks.
 */
public class DownloadService {
    private static final Logger LOG = LoggerFactory.getLogger(DownloadService.class);
    private static final Duration DOWNLOAD_TIMEOUT = Duration.ofMinutes(5);

    private final HttpClient httpClient;
    private final String userAgent;

    public DownloadService(HttpClient httpClient, String userAgent) {
        this.httpClient = httpClient;
        this.userAgent = userAgent;
    }

    public DownloadResult downloadRelease(ReleaseInfo releaseInfo) throws IOException, InterruptedException {
        LauncherPaths.ensureConfigDirectory();
        Path tempFile = LauncherPaths.tempDownloadPath();
        HttpRequest request = HttpRequest.newBuilder(releaseInfo.downloadUrl())
                .timeout(DOWNLOAD_TIMEOUT)
                .header("User-Agent", userAgent)
                .build();
        try {
            HttpResponse<Path> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofFile(tempFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE));
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IOException("Download failed with status " + response.statusCode());
            }
            Path finalJar = moveIntoPlace(tempFile);
            long size = Files.size(finalJar);
            String sha256 = Hashing.sha256(finalJar);
            return new DownloadResult(finalJar, sha256, size);
        } catch (IOException | InterruptedException e) {
            tryDelete(tempFile);
            throw e;
        }
    }

    private Path moveIntoPlace(Path tempFile) throws IOException {
        Path jarPath = LauncherPaths.clientJarPath();
        try {
            Files.move(tempFile, jarPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            LOG.debug("Atomic move not supported, falling back to regular replace", e);
            Files.move(tempFile, jarPath, StandardCopyOption.REPLACE_EXISTING);
        }
        return jarPath;
    }

    private void tryDelete(Path file) {
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            LOG.warn("Failed to delete temporary file {}", file, e);
        }
    }
}
