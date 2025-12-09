package wagemaker.uk.biome;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based test for biome type exhaustiveness.
 * 
 * Feature: water-lake-biome, Property 1: Biome type exhaustiveness
 * Validates: Requirements 1.2
 */
@RunWith(JUnitQuickcheck.class)
public class BiomeTypeExhaustivenessPropertyTest {
    
    /**
     * Property 1: Biome type exhaustiveness
     * For any world coordinate, getBiomeAtPosition should return exactly one of 
     * the three valid BiomeType enum values (GRASS, SAND, or WATER).
     * 
     * This test verifies that the biome system always returns a valid biome type
     * and never returns null or an unexpected value.
     */
    @Property(trials = 100)
    public void getBiomeAtPositionReturnsValidBiomeType(float worldX, float worldY) {
        // Initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        try {
            // Get biome type at random coordinates
            BiomeType biomeType = biomeManager.getBiomeAtPosition(worldX, worldY);
            
            // Verify result is not null
            assertNotNull(biomeType, 
                String.format("getBiomeAtPosition(%f, %f) should not return null", worldX, worldY));
            
            // Verify result is one of the three valid biome types
            boolean isValidBiomeType = biomeType == BiomeType.GRASS 
                                    || biomeType == BiomeType.SAND 
                                    || biomeType == BiomeType.WATER;
            
            assertTrue(isValidBiomeType, 
                String.format("getBiomeAtPosition(%f, %f) returned %s, which is not a valid biome type (GRASS, SAND, or WATER)", 
                    worldX, worldY, biomeType));
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
}
