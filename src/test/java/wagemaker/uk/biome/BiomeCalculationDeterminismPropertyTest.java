package wagemaker.uk.biome;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property-based test for biome calculation determinism.
 * 
 * Feature: water-lake-biome, Property 10: Biome calculation determinism
 * Validates: Requirements 4.1, 4.2, 4.4
 */
@RunWith(JUnitQuickcheck.class)
public class BiomeCalculationDeterminismPropertyTest {
    
    /**
     * Property 10: Biome calculation determinism
     * For any world coordinate, calling getBiomeAtPosition multiple times with the same 
     * coordinates should always return the identical BiomeType value.
     * 
     * This test verifies that biome calculation is deterministic and consistent,
     * which is essential for multiplayer synchronization.
     */
    @Property(trials = 100)
    public void getBiomeAtPositionIsDeterministic(float worldX, float worldY) {
        // Initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        try {
            // Call getBiomeAtPosition multiple times with the same coordinates
            BiomeType firstResult = biomeManager.getBiomeAtPosition(worldX, worldY);
            
            // Verify that subsequent calls return the same result
            for (int i = 0; i < 10; i++) {
                BiomeType result = biomeManager.getBiomeAtPosition(worldX, worldY);
                
                assertEquals(firstResult, result,
                    String.format("getBiomeAtPosition(%.2f, %.2f) should return consistent results. " +
                        "First call returned %s, but call #%d returned %s",
                        worldX, worldY, firstResult, i + 2, result));
            }
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
    
    /**
     * Additional test: Verify determinism across multiple BiomeManager instances.
     * This ensures that different clients in multiplayer will see the same biomes.
     */
    @Property(trials = 100)
    public void getBiomeAtPositionIsConsistentAcrossInstances(float worldX, float worldY) {
        // Create first BiomeManager instance
        BiomeManager biomeManager1 = new BiomeManager();
        biomeManager1.initialize();
        
        // Create second BiomeManager instance
        BiomeManager biomeManager2 = new BiomeManager();
        biomeManager2.initialize();
        
        try {
            // Get biome type from both instances
            BiomeType result1 = biomeManager1.getBiomeAtPosition(worldX, worldY);
            BiomeType result2 = biomeManager2.getBiomeAtPosition(worldX, worldY);
            
            // Verify both instances return the same result
            assertEquals(result1, result2,
                String.format("Different BiomeManager instances should return the same biome type at (%.2f, %.2f). " +
                    "Instance 1 returned %s, but instance 2 returned %s",
                    worldX, worldY, result1, result2));
            
        } finally {
            // Clean up resources
            biomeManager1.dispose();
            biomeManager2.dispose();
        }
    }
}
