package wagemaker.uk.biome;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for biome configuration validation.
 * 
 * Feature: beach-style-water-biome, Property: Configuration constants validation
 * Validates: Requirements 5.4
 */
@RunWith(JUnitQuickcheck.class)
public class BiomeConfigValidationPropertyTest {
    
    /**
     * Property: Configuration constants validation
     * For any BiomeConfig constant, the value should be within valid ranges and 
     * maintain compatibility with existing BiomeConfig parameters while adding 
     * beach-specific settings.
     * 
     * This test verifies that all configuration constants are properly defined
     * and within reasonable ranges for game functionality.
     */
    @Property(trials = 100)
    public void biomeConfigConstantsAreValid() {
        // Test basic biome zone definitions
        assertTrue(BiomeConfig.INNER_GRASS_RADIUS > 0, 
            "INNER_GRASS_RADIUS must be positive");
        assertTrue(BiomeConfig.INNER_GRASS_RADIUS >= 5000 && BiomeConfig.INNER_GRASS_RADIUS <= 15000,
            "INNER_GRASS_RADIUS should be in recommended range 5000-15000");
        
        assertTrue(BiomeConfig.SAND_ZONE_WIDTH > 0, 
            "SAND_ZONE_WIDTH must be positive");
        assertTrue(BiomeConfig.SAND_ZONE_WIDTH >= 2000 && BiomeConfig.SAND_ZONE_WIDTH <= 5000,
            "SAND_ZONE_WIDTH should be in recommended range 2000-5000");
        
        // Test texture generation parameters
        assertTrue(BiomeConfig.TEXTURE_SIZE > 0, 
            "TEXTURE_SIZE must be positive");
        assertTrue(BiomeConfig.TEXTURE_SIZE >= 32 && BiomeConfig.TEXTURE_SIZE <= 128,
            "TEXTURE_SIZE should be in recommended range 32-128");
        assertTrue((BiomeConfig.TEXTURE_SIZE & (BiomeConfig.TEXTURE_SIZE - 1)) == 0,
            "TEXTURE_SIZE should be power of 2 for optimal performance");
        
        // Test color arrays are properly defined (RGBA format)
        validateColorArray(BiomeConfig.GRASS_BASE_COLOR, "GRASS_BASE_COLOR");
        validateColorArray(BiomeConfig.GRASS_LIGHT_COLOR, "GRASS_LIGHT_COLOR");
        validateColorArray(BiomeConfig.GRASS_MEDIUM_COLOR, "GRASS_MEDIUM_COLOR");
        validateColorArray(BiomeConfig.GRASS_BROWNISH_COLOR, "GRASS_BROWNISH_COLOR");
        
        validateColorArray(BiomeConfig.SAND_BASE_COLOR, "SAND_BASE_COLOR");
        validateColorArray(BiomeConfig.SAND_LIGHT_COLOR, "SAND_LIGHT_COLOR");
        validateColorArray(BiomeConfig.SAND_DARK_COLOR, "SAND_DARK_COLOR");
        
        validateColorArray(BiomeConfig.WATER_BASE_COLOR, "WATER_BASE_COLOR");
        validateColorArray(BiomeConfig.WATER_LIGHT_COLOR, "WATER_LIGHT_COLOR");
        validateColorArray(BiomeConfig.WATER_DARK_COLOR, "WATER_DARK_COLOR");
        
        // Test water biome thresholds
        assertTrue(BiomeConfig.WATER_NOISE_THRESHOLD >= 0.0f && BiomeConfig.WATER_NOISE_THRESHOLD <= 1.0f,
            "WATER_NOISE_THRESHOLD must be between 0.0 and 1.0");
        assertTrue(BiomeConfig.WATER_NOISE_THRESHOLD >= 0.50f && BiomeConfig.WATER_NOISE_THRESHOLD <= 0.60f,
            "WATER_NOISE_THRESHOLD should be in recommended range 0.50-0.60");
        
        // Test beach-style biome configuration constants
        assertTrue(BiomeConfig.SAND_BASE_THRESHOLD >= 0.0f && BiomeConfig.SAND_BASE_THRESHOLD <= 1.0f,
            "SAND_BASE_THRESHOLD must be between 0.0 and 1.0");
        
        assertTrue(BiomeConfig.WATER_IN_SAND_THRESHOLD >= 0.0f && BiomeConfig.WATER_IN_SAND_THRESHOLD <= 1.0f,
            "WATER_IN_SAND_THRESHOLD must be between 0.0 and 1.0");
        
        assertTrue(BiomeConfig.BEACH_BUFFER_DISTANCE > 0,
            "BEACH_BUFFER_DISTANCE must be positive");
        assertTrue(BiomeConfig.BEACH_BUFFER_DISTANCE >= 32.0f && BiomeConfig.BEACH_BUFFER_DISTANCE <= 256.0f,
            "BEACH_BUFFER_DISTANCE should be reasonable for gameplay (32-256 pixels)");
        
        assertTrue(BiomeConfig.BASE_BIOME_NOISE_SCALE > 0,
            "BASE_BIOME_NOISE_SCALE must be positive");
        assertTrue(BiomeConfig.BASE_BIOME_NOISE_SCALE >= 0.0001f && BiomeConfig.BASE_BIOME_NOISE_SCALE <= 0.001f,
            "BASE_BIOME_NOISE_SCALE should create appropriately sized regions");
        
        assertTrue(BiomeConfig.WATER_NOISE_SCALE > 0,
            "WATER_NOISE_SCALE must be positive");
        assertTrue(BiomeConfig.WATER_NOISE_SCALE >= 0.0001f && BiomeConfig.WATER_NOISE_SCALE <= 0.01f,
            "WATER_NOISE_SCALE should create appropriately sized water areas");
        
        // Test seeds are different to avoid conflicts
        assertNotEquals(BiomeConfig.BASE_BIOME_SEED, BiomeConfig.WATER_IN_SAND_SEED,
            "BASE_BIOME_SEED and WATER_IN_SAND_SEED should be different");
        assertNotEquals(BiomeConfig.BASE_BIOME_SEED, BiomeConfig.TEXTURE_SEED_GRASS,
            "BASE_BIOME_SEED should be different from existing texture seeds");
        assertNotEquals(BiomeConfig.BASE_BIOME_SEED, BiomeConfig.TEXTURE_SEED_SAND,
            "BASE_BIOME_SEED should be different from existing texture seeds");
        assertNotEquals(BiomeConfig.BASE_BIOME_SEED, BiomeConfig.TEXTURE_SEED_WATER,
            "BASE_BIOME_SEED should be different from existing texture seeds");
        
        assertNotEquals(BiomeConfig.WATER_IN_SAND_SEED, BiomeConfig.TEXTURE_SEED_GRASS,
            "WATER_IN_SAND_SEED should be different from existing texture seeds");
        assertNotEquals(BiomeConfig.WATER_IN_SAND_SEED, BiomeConfig.TEXTURE_SEED_SAND,
            "WATER_IN_SAND_SEED should be different from existing texture seeds");
        assertNotEquals(BiomeConfig.WATER_IN_SAND_SEED, BiomeConfig.TEXTURE_SEED_WATER,
            "WATER_IN_SAND_SEED should be different from existing texture seeds");
        
        // Test logical relationships between thresholds
        // For 40% water coverage in sand with buffer validation, threshold should be around 0.52
        assertTrue(BiomeConfig.WATER_IN_SAND_THRESHOLD >= 0.45f && BiomeConfig.WATER_IN_SAND_THRESHOLD <= 0.65f,
            "WATER_IN_SAND_THRESHOLD should be tuned for approximately 40% coverage");
        
        // For 50/50 grass/sand split, threshold should be around 0.5
        assertTrue(Math.abs(BiomeConfig.SAND_BASE_THRESHOLD - 0.5f) <= 0.1f,
            "SAND_BASE_THRESHOLD should be close to 0.5 for 50/50 distribution");
        
        // Test that beach buffer is reasonable relative to texture size
        assertTrue(BiomeConfig.BEACH_BUFFER_DISTANCE >= BiomeConfig.TEXTURE_SIZE,
            "BEACH_BUFFER_DISTANCE should be at least one texture size");
        
        // Test noise scales create different sized features
        assertTrue(BiomeConfig.BASE_BIOME_NOISE_SCALE < BiomeConfig.WATER_NOISE_SCALE,
            "BASE_BIOME_NOISE_SCALE should be smaller than WATER_NOISE_SCALE to create larger base regions");
    }
    
    /**
     * Helper method to validate color arrays are properly formatted RGBA values.
     */
    private void validateColorArray(float[] color, String colorName) {
        assertNotNull(color, colorName + " should not be null");
        assertEquals(4, color.length, colorName + " should have 4 components (RGBA)");
        
        for (int i = 0; i < 4; i++) {
            assertTrue(color[i] >= 0.0f && color[i] <= 1.0f,
                colorName + " component " + i + " should be between 0.0 and 1.0");
        }
        
        // Alpha component should typically be 1.0 for opaque colors
        assertEquals(1.0f, color[3], 0.001f, 
            colorName + " alpha component should be 1.0 for opaque rendering");
    }
}