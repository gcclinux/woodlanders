package wagemaker.uk.biome;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property-based test for water-only-in-sand constraint.
 * 
 * Feature: beach-style-water-biome, Property 1: Water only spawns in sand areas
 * Validates: Requirements 1.1
 * 
 * This test verifies that for any world coordinate where getBiomeAtPosition returns WATER,
 * the base biome at that coordinate should be SAND. This ensures water only appears
 * within sand base areas, creating realistic beach-style coastal environments.
 */
@RunWith(JUnitQuickcheck.class)
public class WaterOnlyInSandPropertyTest {
    
    /**
     * Property 1: Water only spawns in sand areas
     * For any world coordinate where getBiomeAtPosition returns WATER, the base biome 
     * at that coordinate should be SAND.
     * 
     * This test generates random coordinates, finds water locations, and verifies
     * that each water location has a sand base biome. This is fundamental to the
     * beach-style biome system where water can only exist within sand areas.
     */
    @Property(trials = 100)
    public void waterOnlySpawnsInSandAreas(long randomSeed) {
        // Initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        try {
            Random random = new Random(randomSeed);
            
            // Find water locations by sampling random coordinates
            // We'll test up to 10 water locations per trial
            int waterLocationsFound = 0;
            int maxWaterLocationsToTest = 10;
            int maxSearchAttempts = 200; // Limit search attempts to avoid infinite loops
            
            for (int attempt = 0; attempt < maxSearchAttempts && waterLocationsFound < maxWaterLocationsToTest; attempt++) {
                // Generate a random coordinate
                // Sample from a large area (±50000 pixels from spawn) to find water
                float testX = (random.nextFloat() - 0.5f) * 100000; // -50000 to +50000
                float testY = (random.nextFloat() - 0.5f) * 100000; // -50000 to +50000
                
                // Check if this coordinate has water
                BiomeType biomeType = biomeManager.getBiomeAtPosition(testX, testY);
                
                if (biomeType == BiomeType.WATER) {
                    waterLocationsFound++;
                    
                    // Verify that the base biome at this water location is SAND
                    BiomeType baseBiome = biomeManager.getBaseBiomeAtPosition(testX, testY);
                    
                    assertEquals(BiomeType.SAND, baseBiome,
                        String.format("Water found at (%.2f, %.2f) but base biome is %s, expected SAND. " +
                            "Water can only spawn in sand base areas according to beach-style biome requirements.",
                            testX, testY, baseBiome));
                }
            }
            
            // Note: If no water locations are found, the test passes vacuously
            // This is acceptable as it means the current biome configuration
            // may not generate water in the sampled area, which doesn't violate
            // the constraint that water can only appear in sand areas
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
}