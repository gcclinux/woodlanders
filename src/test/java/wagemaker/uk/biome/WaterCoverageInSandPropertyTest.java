package wagemaker.uk.biome;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based test for water coverage in sand areas.
 * 
 * Feature: beach-style-water-biome, Property 4: Water coverage in sand areas
 * Validates: Requirements 1.4
 * 
 * This test verifies that for any large sample of sand base biome coordinates (n > 1000),
 * approximately 40% should contain water biomes (within ±5% tolerance).
 * This ensures the beach-style water system achieves the target water coverage within sand areas.
 */
@RunWith(JUnitQuickcheck.class)
public class WaterCoverageInSandPropertyTest {
    
    /**
     * Property 4: Water coverage in sand areas
     * For any large sample of sand base biome coordinates (n > 1000), approximately 40% 
     * should contain water biomes (within ±5% tolerance).
     * 
     * This test generates random coordinates, filters for sand base biome areas,
     * and verifies that the percentage of those areas containing water falls
     * within the expected range of 35-45% (40% ±5%).
     */
    @Property(trials = 10)
    public void waterCoverageInSandAreasIsApproximately40Percent(long randomSeed) {
        // Initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        try {
            Random random = new Random(randomSeed);
            
            // Collect sand base biome coordinates
            int targetSandSamples = 1000; // Minimum sample size for statistical validity
            int maxSearchAttempts = 10000; // Limit search attempts to avoid infinite loops
            
            int sandLocationsFound = 0;
            int waterInSandCount = 0;
            
            for (int attempt = 0; attempt < maxSearchAttempts && sandLocationsFound < targetSandSamples; attempt++) {
                // Generate a random coordinate
                // Sample from a large area (±50000 pixels from spawn) to find diverse sand areas
                float testX = (random.nextFloat() - 0.5f) * 100000; // -50000 to +50000
                float testY = (random.nextFloat() - 0.5f) * 100000; // -50000 to +50000
                
                // Check if this coordinate has sand base biome
                BiomeType baseBiome = biomeManager.getBaseBiomeAtPosition(testX, testY);
                
                if (baseBiome == BiomeType.SAND) {
                    sandLocationsFound++;
                    
                    // Check if this sand location contains water
                    BiomeType finalBiome = biomeManager.getBiomeAtPosition(testX, testY);
                    if (finalBiome == BiomeType.WATER) {
                        waterInSandCount++;
                    }
                }
            }
            
            // Verify we found enough sand samples for statistical validity
            assertTrue(sandLocationsFound >= targetSandSamples,
                String.format("Insufficient sand samples found: %d (required: %d). " +
                    "This may indicate biome configuration issues or insufficient search area.",
                    sandLocationsFound, targetSandSamples));
            
            // Calculate water coverage percentage in sand areas
            double waterCoveragePercentage = (double) waterInSandCount / sandLocationsFound * 100.0;
            
            // Verify water coverage is within expected range (40% ±5%)
            double expectedPercentage = 40.0;
            double tolerance = 5.0;
            double minExpected = expectedPercentage - tolerance; // 35%
            double maxExpected = expectedPercentage + tolerance; // 45%
            
            assertTrue(waterCoveragePercentage >= minExpected && waterCoveragePercentage <= maxExpected,
                String.format("Water coverage in sand areas is %.2f%%, expected %.2f%% ±%.2f%% (range: %.2f%%-%.2f%%). " +
                    "Sand samples: %d, Water in sand: %d. " +
                    "This indicates the water-in-sand threshold may need adjustment.",
                    waterCoveragePercentage, expectedPercentage, tolerance, minExpected, maxExpected,
                    sandLocationsFound, waterInSandCount));
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
}