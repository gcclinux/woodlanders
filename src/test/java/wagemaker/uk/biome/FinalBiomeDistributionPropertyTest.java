package wagemaker.uk.biome;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based test for final biome distribution in the beach-style water biome system.
 * 
 * Feature: beach-style-water-biome, Property 7: Final biome distribution
 * Validates: Requirements 2.3
 */
@RunWith(JUnitQuickcheck.class)
public class FinalBiomeDistributionPropertyTest {
    
    /**
     * Property 7: Final biome distribution
     * For any large sample of world coordinates (n > 10000), the final distribution 
     * should be approximately 50% grass, 30% sand, and 20% water (within ±5% tolerance).
     * 
     * This test verifies that the beach-style biome generation system produces the expected
     * final distribution after applying the two-phase calculation (base biome + water overlay).
     * The distribution reflects the beach-style approach where water only appears within
     * sand base areas, creating realistic coastal environments.
     */
    @Property(trials = 10)
    public void finalBiomeDistributionMatchesBeachStyleTargets(long randomSeed) {
        // Initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        try {
            // Sample a large number of random coordinates
            int sampleSize = 10000;
            Random random = new Random(randomSeed);
            
            int grassCount = 0;
            int sandCount = 0;
            int waterCount = 0;
            
            // Sample coordinates from a representative area of the world
            // Use a large rectangular area that includes both fixed zones and noise-based areas
            // Sample from -30000 to +30000 in both X and Y (60000x60000 area)
            // This ensures we get a representative sample of the entire biome system
            for (int i = 0; i < sampleSize; i++) {
                // Generate random coordinates in a square area
                float worldX = -30000 + random.nextFloat() * 60000;
                float worldY = -30000 + random.nextFloat() * 60000;
                
                BiomeType biomeType = biomeManager.getBiomeAtPosition(worldX, worldY);
                
                switch (biomeType) {
                    case GRASS:
                        grassCount++;
                        break;
                    case SAND:
                        sandCount++;
                        break;
                    case WATER:
                        waterCount++;
                        break;
                }
            }
            
            // Calculate percentages
            double grassPercent = (grassCount * 100.0) / sampleSize;
            double sandPercent = (sandCount * 100.0) / sampleSize;
            double waterPercent = (waterCount * 100.0) / sampleSize;
            
            // Expected distribution for beach-style biome system:
            // - Grass: 50% (unchanged from base biome)
            // - Sand: 30% (60% of original sand areas remain sand)
            // - Water: 20% (40% of original sand areas become water)
            // Tolerance: ±5% for each biome type
            double tolerance = 5.0;
            
            assertTrue(grassPercent >= 45.0 && grassPercent <= 55.0,
                String.format("Final grass distribution should be 50%% ±5%%, but was %.2f%% (%d/%d samples)", 
                    grassPercent, grassCount, sampleSize));
            
            assertTrue(sandPercent >= 25.0 && sandPercent <= 35.0,
                String.format("Final sand distribution should be 30%% ±5%%, but was %.2f%% (%d/%d samples)", 
                    sandPercent, sandCount, sampleSize));
            
            assertTrue(waterPercent >= 15.0 && waterPercent <= 25.0,
                String.format("Final water distribution should be 20%% ±5%%, but was %.2f%% (%d/%d samples)", 
                    waterPercent, waterCount, sampleSize));
            
            // Verify that all biome types sum to 100% (no missing or extra biome types)
            assertTrue(grassCount + sandCount + waterCount == sampleSize,
                String.format("Biome counts should sum to sample size: %d + %d + %d = %d (expected %d)",
                    grassCount, sandCount, waterCount, grassCount + sandCount + waterCount, sampleSize));
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
}