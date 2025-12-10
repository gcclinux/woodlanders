package wagemaker.uk.biome;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based test for base biome distribution.
 * 
 * Feature: beach-style-water-biome, Property 6: Base biome distribution
 * Validates: Requirements 2.1
 */
@RunWith(JUnitQuickcheck.class)
public class BaseBiomeDistributionPropertyTest {
    
    /**
     * Property 6: Base biome distribution
     * For any large sample of world coordinates outside spawn zones (n > 10000), 
     * approximately 50% should be grass base biome and 50% should be sand base biome 
     * (within ±5% tolerance).
     * 
     * This test verifies that the base biome calculation (before water overlay) 
     * produces the expected 50/50 distribution between grass and sand areas.
     * The base biome distribution is the foundation for beach-style water placement.
     */
    @Property(trials = 10)
    public void baseBiomeDistributionIs50Percent(long randomSeed) {
        // Initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        try {
            // Sample a large number of random coordinates outside spawn zones
            int sampleSize = 10000;
            Random random = new Random(randomSeed);
            
            int grassBaseCount = 0;
            int sandBaseCount = 0;
            
            // Sample coordinates from areas outside the fixed spawn zones
            // Skip the inner grass zone (0-10000px) and sand zone (10000-13000px)
            // to measure the true noise-based base biome distribution
            for (int i = 0; i < sampleSize; i++) {
                // Generate random coordinates beyond the fixed zones
                // Sample from a large area where noise-based distribution applies
                float worldX, worldY;
                
                // Generate coordinates that are definitely outside spawn zones
                // Use a large rectangular area from -30000 to +30000 in both axes
                worldX = -30000 + random.nextFloat() * 60000;
                worldY = -30000 + random.nextFloat() * 60000;
                
                // Ensure we're sampling from areas beyond the fixed zones
                float distance = (float) Math.sqrt(worldX * worldX + worldY * worldY);
                if (distance <= BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH) {
                    // Skip coordinates in fixed zones, generate new ones
                    i--;
                    continue;
                }
                
                BiomeType baseBiome = biomeManager.getBaseBiomeAtPosition(worldX, worldY);
                
                switch (baseBiome) {
                    case GRASS:
                        grassBaseCount++;
                        break;
                    case SAND:
                        sandBaseCount++;
                        break;
                    case WATER:
                        // Base biome should never be WATER - this would be a bug
                        throw new AssertionError(String.format(
                            "Base biome returned WATER at (%.1f, %.1f), but base biome should only be GRASS or SAND",
                            worldX, worldY));
                }
            }
            
            // Calculate percentages
            double grassBasePercent = (grassBaseCount * 100.0) / sampleSize;
            double sandBasePercent = (sandBaseCount * 100.0) / sampleSize;
            
            // Expected distribution: 50% grass base, 50% sand base (±5% tolerance)
            double tolerance = 5.0;
            
            assertTrue(grassBasePercent >= 45.0 && grassBasePercent <= 55.0,
                String.format("Grass base biome distribution should be 50%% ±5%%, but was %.2f%% (%d/%d samples)", 
                    grassBasePercent, grassBaseCount, sampleSize));
            
            assertTrue(sandBasePercent >= 45.0 && sandBasePercent <= 55.0,
                String.format("Sand base biome distribution should be 50%% ±5%%, but was %.2f%% (%d/%d samples)", 
                    sandBasePercent, sandBaseCount, sampleSize));
            
            // Verify that grass + sand = 100% (no other base biome types)
            assertTrue(grassBaseCount + sandBaseCount == sampleSize,
                String.format("Base biome counts should sum to sample size: %d + %d = %d (expected %d)",
                    grassBaseCount, sandBaseCount, grassBaseCount + sandBaseCount, sampleSize));
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
}