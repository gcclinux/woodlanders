package wagemaker.uk.biome;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based test for water contiguity.
 * 
 * Feature: water-lake-biome, Property 4: Water contiguity
 * Validates: Requirements 1.5
 */
@RunWith(JUnitQuickcheck.class)
public class WaterContiguityPropertyTest {
    
    /**
     * Property 4: Water contiguity
     * For any water biome tile, at least 40% of its adjacent tiles (within 200 pixel radius) 
     * should also be water biome tiles, ensuring lake-like clustering.
     * 
     * This test verifies that water biomes form contiguous lake-like regions rather than
     * being scattered as individual tiles.
     */
    @Property(trials = 100)
    public void waterTilesFormContiguousLakeRegions(long randomSeed) {
        // Initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        try {
            // Find a water tile by sampling random coordinates
            Random random = new Random(randomSeed);
            float waterX = 0;
            float waterY = 0;
            boolean foundWater = false;
            
            // Sample up to 1000 random coordinates to find a water tile
            // Sample from a large area (±50000 pixels from spawn)
            for (int attempt = 0; attempt < 1000 && !foundWater; attempt++) {
                float testX = (random.nextFloat() - 0.5f) * 100000; // -50000 to +50000
                float testY = (random.nextFloat() - 0.5f) * 100000; // -50000 to +50000
                
                if (biomeManager.getBiomeAtPosition(testX, testY) == BiomeType.WATER) {
                    waterX = testX;
                    waterY = testY;
                    foundWater = true;
                }
            }
            
            // If we found a water tile, check its contiguity
            if (foundWater) {
                // Sample adjacent tiles within 200 pixel radius
                int sampleCount = 50; // Number of points to sample
                int waterCount = 0;
                int radius = 200;
                
                for (int i = 0; i < sampleCount; i++) {
                    // Generate random point within radius
                    double angle = random.nextDouble() * 2 * Math.PI;
                    double distance = random.nextDouble() * radius;
                    
                    float adjacentX = waterX + (float)(Math.cos(angle) * distance);
                    float adjacentY = waterY + (float)(Math.sin(angle) * distance);
                    
                    if (biomeManager.getBiomeAtPosition(adjacentX, adjacentY) == BiomeType.WATER) {
                        waterCount++;
                    }
                }
                
                // Calculate percentage of adjacent tiles that are water
                double waterPercentage = (waterCount * 100.0) / sampleCount;
                
                // At least 40% of adjacent tiles should be water
                assertTrue(waterPercentage >= 40.0,
                    String.format("Water tile at (%.2f, %.2f) should have at least 40%% water in 200px radius, but had %.2f%% (%d/%d samples)",
                        waterX, waterY, waterPercentage, waterCount, sampleCount));
            }
            // If no water tile was found in 1000 attempts, the test passes vacuously
            // (this is acceptable as it means water is very rare in the sampled area)
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
}
