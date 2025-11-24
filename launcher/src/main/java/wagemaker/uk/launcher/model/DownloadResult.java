package wagemaker.uk.launcher.model;

import java.nio.file.Path;

/**
 * Outcome of a download along with integrity metadata.
 */
public record DownloadResult(Path file, String sha256, long size) {
}
