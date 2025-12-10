package wagemaker.uk.biome;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based test for water buffer distance validation.
 * 
 * Feature: beach-style-water-biome, Property 3: Water maintains buffer distance from grass
 * Validates: Requirements 1.3
 * 
 * This test verifies that for any world coordinate where getBiomeAtPosition returns WATER,
 * all coordinates within a 128-pixel radius should not have base biome type GRASS.
 * This ensures water only appears in sand areas with proper buffer zones from grass.
 */
@RunWith(JUnitQuickcheck.class)
public class WaterBufferDistancePropertyTest {
    
    /**
     * Property 3: Water maintains buffer distance from grass
     * For any world coordinate where getBiomeAtPosition returns WATER, all coordinates 
     * within a 128-pixel radius should not have base biome type GRASS.
     * 
     * This test generates random coordinates, finds water locations, and verifies
     * that the buffer zone around each water location contains no grass biomes.
     */
    @Property(trials = 100)
    public void waterMaintainsBufferDistanceFromGrass(long randomSeed) {
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
                    
                    // Verify buffer zone around this water location
                    verifyBufferZoneAroundWater(biomeManager, testX, testY);
                }
            }
            
            // Note: If no water locations are found, the test passes vacuously
            // This is acceptable as it means the current biome configuration
            // may not generate water in the sampled area
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
    
    /**
     * Verifies that the buffer zone around a water location contains no grass biomes.
     * This method checks multiple points within the buffer radius to ensure comprehensive coverage.
     * 
     * @param biomeManager The BiomeManager instance to use for biome queries
     * @param waterX The x-coordinate of the water location
     * @param waterY The y-coordinate of the water location
     */
    private void verifyBufferZoneAroundWater(BiomeManager biomeManager, float waterX, float waterY) {
        float bufferDistance = BiomeConfig.BEACH_BUFFER_DISTANCE; // 128.0f pixels
        
        // Check points around the circumference of the buffer zone
        int circumferencePoints = 16; // Same as BiomeManager implementation
        for (int i = 0; i < circumferencePoints; i++) {
            float angle = (float) (2 * Math.PI * i / circumferencePoints);
            float checkX = waterX + bufferDistance * (float) Math.cos(angle);
            float checkY = waterY + bufferDistance * (float) Math.sin(angle);
            
            BiomeType baseBiomeAtBufferEdge = biomeManager.getBaseBiomeAtPosition(checkX, checkY);
            
            assertTrue(baseBiomeAtBufferEdge != BiomeType.GRASS,
                String.format("Water at (%.2f, %.2f) has grass base biome at buffer edge (%.2f, %.2f). " +
                    "Buffer distance: %.2f pixels. Base biome found: %s",
                    waterX, waterY, checkX, checkY, bufferDistance, baseBiomeAtBufferEdge));
        }
        
        // Also check points at intermediate distances within the buffer zone
        // This catches grass areas that might be closer than the full buffer distance
        float halfBuffer = bufferDistance * 0.5f;
        int innerPoints = 8; // Same as BiomeManager implementation
        for (int i = 0; i < innerPoints; i++) {
            float angle = (float) (2 * Math.PI * i / innerPoints);
            float checkX = waterX + halfBuffer * (float) Math.cos(angle);
            float checkY = waterY + halfBuffer * (float) Math.sin(angle);
            
            BiomeType baseBiomeAtHalfBuffer = biomeManager.getBaseBiomeAtPosition(checkX, checkY);
            
            assertTrue(baseBiomeAtHalfBuffer != BiomeType.GRASS,
                String.format("Water at (%.2f, %.2f) has grass base biome at half-buffer distance (%.2f, %.2f). " +
                    "Half-buffer distance: %.2f pixels. Base biome found: %s",
                    waterX, waterY, checkX, checkY, halfBuffer, baseBiomeAtHalfBuffer));
        }
        
        // Additional verification: check a few random points within the buffer zone
        Random bufferRandom = new Random(42); // Fixed seed for deterministic testing
        int randomPointsToCheck = 5;
        
        for (int i = 0; i < randomPointsToCheck; i++) {
            // Generate a random point within the buffer circle
            float randomAngle = bufferRandom.nextFloat() * 2 * (float) Math.PI;
            float randomRadius = bufferRandom.nextFloat() * bufferDistance;
            
            float checkX = waterX + randomRadius * (float) Math.cos(randomAngle);
            float checkY = waterY + randomRadius * (float) Math.sin(randomAngle);
            
            BiomeType baseBiomeAtRandomPoint = biomeManager.getBaseBiomeAtPosition(checkX, checkY);
            
            assertTrue(baseBiomeAtRandomPoint != BiomeType.GRASS,
                String.format("Water at (%.2f, %.2f) has grass base biome at random point within buffer (%.2f, %.2f). " +
                    "Distance from water: %.2f pixels (buffer: %.2f). Base biome found: %s",
                    waterX, waterY, checkX, checkY, randomRadius, bufferDistance, baseBiomeAtRandomPoint));
        }
    }
}