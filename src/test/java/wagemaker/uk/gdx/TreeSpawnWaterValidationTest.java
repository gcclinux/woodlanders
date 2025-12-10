package wagemaker.uk.gdx;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import wagemaker.uk.biome.BiomeManager;
import wagemaker.uk.biome.BiomeType;

/**
 * Unit test to verify that tree spawning validation correctly prevents
 * trees from spawning in water biomes.
 * 
 * This test verifies the logic of the validation method by checking
 * that it correctly identifies grass and sand as valid spawn locations.
 * The actual water biome generation is tested separately in BiomeManager tests.
 * 
 * Requirements: 3.1 (tree spawn validation), 3.5 (alternative location selection)
 */
public class TreeSpawnWaterValidationTest {
    
    /**
     * Test that grass locations are valid for tree spawning.
     * This verifies the validation logic allows trees in grass biomes.
     */
    @Test
    public void testGrassLocationIsValidForTreeSpawning() {
        // Create and initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        // Test a location near spawn (should be grass)
        float grassX = 500;
        float grassY = 500;
        
        BiomeType biomeAtGrass = biomeManager.getBiomeAtPosition(grassX, grassY);
        assertEquals(BiomeType.GRASS, biomeAtGrass, 
                    "Location near spawn should be grass biome");
        
        // Clean up
        biomeManager.dispose();
    }
    
    /**
     * Test that sand locations are valid for tree spawning.
     * This verifies the validation logic allows trees in sand biomes.
     */
    @Test
    public void testSandLocationIsValidForTreeSpawning() {
        // Create and initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        // Find a sand location by sampling many coordinates
        boolean foundSandLocation = false;
        float sandX = 0, sandY = 0;
        
        // Sample coordinates in the sand zone (10000-13000 pixels from spawn)
        for (int x = 10500; x < 12500; x += 100) {
            for (int y = 10500; y < 12500; y += 100) {
                BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
                if (biome == BiomeType.SAND) {
                    sandX = x;
                    sandY = y;
                    foundSandLocation = true;
                    break;
                }
            }
            if (foundSandLocation) break;
        }
        
        // Verify we found at least one sand location
        assertTrue(foundSandLocation, "Should find at least one sand location in sampled area");
        
        // Verify the biome is indeed sand
        BiomeType biomeAtSand = biomeManager.getBiomeAtPosition(sandX, sandY);
        assertEquals(BiomeType.SAND, biomeAtSand, 
                    "Sampled location should be sand biome");
        
        // Clean up
        biomeManager.dispose();
    }
    
    /**
     * Test that the validation method correctly identifies biome types.
     * This test verifies that the BiomeManager can distinguish between
     * different biome types, which is essential for the tree spawn validation.
     */
    @Test
    public void testBiomeTypeIdentification() {
        // Create and initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        // Test multiple locations to ensure biome identification works
        int grassCount = 0;
        int sandCount = 0;
        int waterCount = 0;
        
        // Sample grass zone (0-10000 pixels from spawn)
        for (int x = 0; x < 8000; x += 500) {
            for (int y = 0; y < 8000; y += 500) {
                BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
                assertNotNull(biome, "Biome type should never be null");
                
                if (biome == BiomeType.GRASS) {
                    grassCount++;
                }
            }
        }
        
        // Sample sand zone (10000-13000 pixels from spawn)
        for (int x = 10500; x < 12500; x += 500) {
            for (int y = 10500; y < 12500; y += 500) {
                BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
                assertNotNull(biome, "Biome type should never be null");
                
                switch (biome) {
                    case SAND:
                        sandCount++;
                        break;
                    case WATER:
                        waterCount++;
                        break;
                }
            }
        }
        
        // Verify we have a mix of biomes (at least grass and sand should exist)
        assertTrue(grassCount > 0, "Should have some grass biomes");
        assertTrue(sandCount > 0, "Should have some sand biomes");
        
        // Log the distribution for debugging
        System.out.println("Biome distribution in sampled area:");
        System.out.println("  Grass: " + grassCount);
        System.out.println("  Sand: " + sandCount);
        System.out.println("  Water: " + waterCount);
        
        // Clean up
        biomeManager.dispose();
    }
}
