package wagemaker.uk.launcher.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

/**
 * Represents the locally cached metadata that tracks the downloaded client.
 */
public record VersionMetadata(
        @JsonProperty("version") String version,
        @JsonProperty("sha256") String sha256,
        @JsonProperty("downloaded_at") Instant downloadedAt,
        @JsonProperty("asset_size") long assetSize
) {
}
