package wagemaker.uk.launcher.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import wagemaker.uk.launcher.config.LauncherPaths;
import wagemaker.uk.launcher.model.VersionMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Persists the metadata that describes the cached client JAR.
 */
public class VersionService {
    private static final Logger LOG = LoggerFactory.getLogger(VersionService.class);

    private final ObjectMapper objectMapper;
    private final Path metadataPath;

    public VersionService(ObjectMapper objectMapper) {
        this(objectMapper, LauncherPaths.versionMetadataPath());
    }

    public VersionService(ObjectMapper objectMapper, Path metadataPath) {
        this.objectMapper = objectMapper;
        this.metadataPath = metadataPath;
    }

    public Optional<VersionMetadata> readMetadata() {
        if (!Files.exists(metadataPath)) {
            return Optional.empty();
        }
        try (Reader reader = Files.newBufferedReader(metadataPath, StandardCharsets.UTF_8)) {
            return Optional.ofNullable(objectMapper.readValue(reader, VersionMetadata.class));
        } catch (IOException e) {
            LOG.warn("Failed to parse version metadata at {}", metadataPath, e);
            return Optional.empty();
        }
    }

    public void writeMetadata(VersionMetadata metadata) throws IOException {
        Path parent = metadataPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        try (Writer writer = Files.newBufferedWriter(metadataPath, StandardCharsets.UTF_8)) {
            objectMapper.writeValue(writer, metadata);
        }
    }
}
