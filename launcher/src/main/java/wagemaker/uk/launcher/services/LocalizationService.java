package wagemaker.uk.launcher.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

/**
 * Service for loading and managing launcher localization.
 */
public class LocalizationService {
    private static final Logger LOG = LoggerFactory.getLogger(LocalizationService.class);
    private static final String DEFAULT_LANGUAGE = "en";
    
    private final ObjectMapper objectMapper;
    private JsonNode translations;
    private String currentLanguage;

    public LocalizationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.currentLanguage = detectSystemLanguage();
        loadTranslations(currentLanguage);
    }

    private String detectSystemLanguage() {
        String systemLang = Locale.getDefault().getLanguage().toLowerCase();
        // Map system locale to supported languages
        return switch (systemLang) {
            case "pl" -> "pl";
            case "pt" -> "pt";
            case "nl" -> "nl";
            case "de" -> "de";
            default -> DEFAULT_LANGUAGE;
        };
    }

    private void loadTranslations(String language) {
        String resourcePath = "/localization/" + language + ".json";
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) {
                LOG.warn("Translation file not found: {}, falling back to English", resourcePath);
                loadFallback();
                return;
            }
            translations = objectMapper.readTree(is);
            LOG.info("Loaded translations for language: {}", language);
        } catch (IOException e) {
            LOG.error("Failed to load translations for {}", language, e);
            loadFallback();
        }
    }

    private void loadFallback() {
        try (InputStream is = getClass().getResourceAsStream("/localization/" + DEFAULT_LANGUAGE + ".json")) {
            if (is != null) {
                translations = objectMapper.readTree(is);
                currentLanguage = DEFAULT_LANGUAGE;
            }
        } catch (IOException e) {
            LOG.error("Failed to load fallback translations", e);
        }
    }

    public String get(String key) {
        if (translations == null) {
            return key;
        }
        
        String[] parts = key.split("\\.");
        JsonNode node = translations;
        for (String part : parts) {
            node = node.get(part);
            if (node == null) {
                LOG.debug("Translation key not found: {}", key);
                return key;
            }
        }
        return node.asText(key);
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void setLanguage(String language) {
        this.currentLanguage = language;
        loadTranslations(language);
    }
}
