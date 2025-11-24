package wagemaker.uk.launcher.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import wagemaker.uk.launcher.model.ReleaseInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Iterator;
import java.util.Optional;

/**
 * Talks to GitHub to figure out what the latest Woodlanders build is.
 */
public class GithubReleaseService {
    private static final Logger LOG = LoggerFactory.getLogger(GithubReleaseService.class);
    private static final URI LATEST_RELEASE_URI = URI.create("https://api.github.com/repos/gcclinux/woodlanders/releases/latest");
    private static final String TARGET_ASSET_NAME = "woodlanders-client.jar";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(20);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String userAgent;

    public GithubReleaseService(HttpClient httpClient, ObjectMapper objectMapper, String userAgent) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.userAgent = userAgent;
    }

    public Optional<ReleaseInfo> fetchLatestRelease() {
        HttpRequest request = HttpRequest.newBuilder(LATEST_RELEASE_URI)
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/vnd.github+json")
                .header("User-Agent", userAgent)
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return parseRelease(response.body());
            }
            LOG.warn("GitHub latest release check failed with status {}", response.statusCode());
        } catch (IOException e) {
            LOG.warn("I/O error while talking to GitHub", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("GitHub request interrupted", e);
        }
        return Optional.empty();
    }

    private Optional<ReleaseInfo> parseRelease(String body) throws IOException {
        JsonNode root = objectMapper.readTree(body);
        String tagName = textValue(root, "tag_name");
        if (tagName == null || tagName.isBlank()) {
            LOG.warn("Latest release response did not contain a tag_name");
            return Optional.empty();
        }
        JsonNode assetsNode = root.path("assets");
        if (!assetsNode.isArray()) {
            LOG.warn("Latest release response did not contain any assets");
            return Optional.empty();
        }
        return selectAsset(assetsNode)
                .map(asset -> buildReleaseInfo(tagName, asset));
    }

    private Optional<JsonNode> selectAsset(JsonNode assetsNode) {
        JsonNode preferred = null;
        Iterator<JsonNode> iterator = assetsNode.elements();
        while (iterator.hasNext()) {
            JsonNode asset = iterator.next();
            String name = textValue(asset, "name");
            if (name == null) {
                continue;
            }
            if (TARGET_ASSET_NAME.equals(name)) {
                return Optional.of(asset);
            }
            if (name.endsWith(".jar")) {
                preferred = asset;
            }
        }
        return Optional.ofNullable(preferred);
    }

    private ReleaseInfo buildReleaseInfo(String tagName, JsonNode asset) {
        String downloadUrl = textValue(asset, "browser_download_url");
        long size = asset.path("size").asLong();
        if (downloadUrl == null || downloadUrl.isBlank()) {
            throw new IllegalStateException("Asset missing browser_download_url");
        }
        return new ReleaseInfo(tagName, URI.create(downloadUrl), size);
    }

    private String textValue(JsonNode node, String fieldName) {
        JsonNode field = node.get(fieldName);
        return field != null && !field.isNull() ? field.asText() : null;
    }
}
