package wagemaker.uk.biome;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property-based test for biome calculation determinism in beach-style water biome system.
 * 
 * Feature: beach-style-water-biome, Property 11: Biome calculation determinism
 * Validates: Requirements 4.4
 * 
 * This test verifies that biome calculation is deterministic and consistent in the
 * beach-style water biome system, which is essential for multiplayer synchronization.
 * The same coordinates should always return the same biome type across multiple calls
 * and across different BiomeManager instances.
 */
@RunWith(JUnitQuickcheck.class)
public class BeachStyleBiomeCalculationDeterminismPropertyTest {
    
    /**
     * Property 11: Biome calculation determinism
     * For any world coordinate, calling getBiomeAtPosition multiple times with the same 
     * coordinates should always return the identical BiomeType value.
     * 
     * This property ensures that the beach-style biome calculation system maintains
     * deterministic behavior, which is critical for multiplayer synchronization where
     * all clients must see identical biome layouts.
     * 
     * Validates: Requirements 4.4
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
                    String.format("Beach-style getBiomeAtPosition(%.2f, %.2f) should return consistent results. " +
                        "First call returned %s, but call #%d returned %s. " +
                        "Deterministic behavior is essential for multiplayer synchronization.",
                        worldX, worldY, firstResult, i + 2, result));
            }
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
    
    /**
     * Additional test: Verify determinism across multiple BiomeManager instances.
     * This ensures that different clients in multiplayer will see the same beach-style biomes.
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
                String.format("Different BiomeManager instances should return the same beach-style biome type at (%.2f, %.2f). " +
                    "Instance 1 returned %s, but instance 2 returned %s. " +
                    "This consistency is critical for multiplayer synchronization in the beach-style system.",
                    worldX, worldY, result1, result2));
            
        } finally {
            // Clean up resources
            biomeManager1.dispose();
            biomeManager2.dispose();
        }
    }
    
    /**
     * Additional test: Verify base biome calculation determinism.
     * This ensures that the two-phase biome calculation (base biome + water overlay)
     * produces consistent results for the base biome layer.
     */
    @Property(trials = 100)
    public void getBaseBiomeAtPositionIsDeterministic(float worldX, float worldY) {
        // Initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        try {
            // Call getBaseBiomeAtPosition multiple times with the same coordinates
            BiomeType firstResult = biomeManager.getBaseBiomeAtPosition(worldX, worldY);
            
            // Verify that subsequent calls return the same result
            for (int i = 0; i < 10; i++) {
                BiomeType result = biomeManager.getBaseBiomeAtPosition(worldX, worldY);
                
                assertEquals(firstResult, result,
                    String.format("Beach-style getBaseBiomeAtPosition(%.2f, %.2f) should return consistent results. " +
                        "First call returned %s, but call #%d returned %s. " +
                        "Base biome determinism is essential for consistent water placement.",
                        worldX, worldY, firstResult, i + 2, result));
            }
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
    
    /**
     * Additional test: Verify buffer zone validation determinism.
     * This ensures that water placement validation produces consistent results.
     */
    @Property(trials = 100)
    public void bufferZoneValidationIsDeterministic(float worldX, float worldY) {
        // Initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        try {
            // Call isValidBeachBuffer multiple times with the same coordinates
            boolean firstResult = biomeManager.isValidBeachBuffer(worldX, worldY);
            
            // Verify that subsequent calls return the same result
            for (int i = 0; i < 5; i++) {
                boolean result = biomeManager.isValidBeachBuffer(worldX, worldY);
                
                assertEquals(firstResult, result,
                    String.format("Beach buffer validation at (%.2f, %.2f) should return consistent results. " +
                        "First call returned %s, but call #%d returned %s. " +
                        "Buffer validation determinism is essential for consistent water placement.",
                        worldX, worldY, firstResult, i + 2, result));
            }
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
}