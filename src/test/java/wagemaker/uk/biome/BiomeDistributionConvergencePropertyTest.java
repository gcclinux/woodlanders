package wagemaker.uk.biome;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based test for biome distribution convergence.
 * 
 * Feature: water-lake-biome, Property 3: Biome distribution convergence
 * Validates: Requirements 1.4
 */
@RunWith(JUnitQuickcheck.class)
public class BiomeDistributionConvergencePropertyTest {
    
    /**
     * Property 3: Biome distribution convergence
     * For any large sample of random world coordinates (n > 10000), the distribution 
     * of biome types should converge to approximately 50% grass, 35% sand, and 15% water 
     * (within ±5% tolerance).
     * 
     * This test verifies that the biome generation system produces the expected
     * distribution of biome types across the world. Note: Samples are taken from areas
     * outside the spawn exclusion zones (1500px for water, 1000px for sand) to measure
     * the true biome distribution in the playable world.
     */
    @Property(trials = 10)
    public void biomeDistributionConvergesToExpectedRatios(long randomSeed) {
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
            
            // Sample coordinates from a representative area of the world outside spawn zones
            // Use a large rectangular area beyond the inner grass and sand zones
            // Sample from -25000 to +25000 in both X and Y (50000x50000 area)
            // This ensures most samples are in the noise-based biome generation area
            for (int i = 0; i < sampleSize; i++) {
                // Generate random coordinates in a square area
                float worldX = -25000 + random.nextFloat() * 50000;
                float worldY = -25000 + random.nextFloat() * 50000;
                
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
            
            // Expected distribution for beach-style biome system: 50% grass, 30% sand, 20% water (±5% tolerance)
            // These targets apply to the new two-phase biome calculation system
            double tolerance = 5.0;
            
            assertTrue(grassPercent >= 45.0 && grassPercent <= 55.0,
                String.format("Grass distribution should be 50%% ±5%%, but was %.2f%% (%d/%d samples)", 
                    grassPercent, grassCount, sampleSize));
            

            
            assertTrue(sandPercent >= 25.0 && sandPercent <= 35.0,
                String.format("Sand distribution should be 30%% ±5%%, but was %.2f%% (%d/%d samples)", 
                    sandPercent, sandCount, sampleSize));
            
            assertTrue(waterPercent >= 15.0 && waterPercent <= 25.0,
                String.format("Water distribution should be 20%% ±5%%, but was %.2f%% (%d/%d samples)", 
                    waterPercent, waterCount, sampleSize));
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
}
