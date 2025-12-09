package wagemaker.uk.biome;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Backward compatibility tests for biome system.
 * Verifies that grass and sand biomes still function correctly after adding water biome.
 * 
 * These tests ensure that:
 * - Grass biome behavior remains unchanged
 * - Sand biome behavior remains unchanged
 * - Existing biome zones are preserved
 * - Biome distribution patterns are maintained
 * 
 * Requirements: 5.4 (maintain compatibility with existing BiomeConfig parameters)
 *               5.5 (not break existing grass and sand biome functionality)
 */
public class BiomeBackwardCompatibilityTest {
    
    private BiomeManager biomeManager;
    
    @BeforeEach
    public void setUp() {
        biomeManager = new BiomeManager();
        biomeManager.initialize();
    }
    
    @AfterEach
    public void tearDown() {
        if (biomeManager != null) {
            biomeManager.dispose();
        }
    }
    
    // ===== Grass Biome Backward Compatibility Tests =====
    
    @Test
    public void testGrassAtSpawnPointUnchanged() {
        // Spawn point should always be grass (unchanged behavior)
        BiomeType biome = biomeManager.getBiomeAtPosition(0.0f, 0.0f);
        assertEquals(BiomeType.GRASS, biome, 
            "Spawn point (0,0) should remain GRASS after water biome addition");
    }
    
    @Test
    public void testGrassInInnerZoneUnchanged() {
        // Test multiple positions in inner grass zone (< 1500px from spawn)
        // These should never be water due to water exclusion zone
        float[][] innerGrassPositions = {
            {100.0f, 100.0f},
            {500.0f, 0.0f},
            {0.0f, 1000.0f},
            {1000.0f, 1000.0f},
            {-500.0f, -500.0f}
        };
        
        for (float[] pos : innerGrassPositions) {
            BiomeType biome = biomeManager.getBiomeAtPosition(pos[0], pos[1]);
            // Should be grass (water doesn't spawn within 1500px)
            // Sand also doesn't spawn within 1000px
            assertEquals(BiomeType.GRASS, biome,
                String.format("Position (%.1f, %.1f) in inner zone should remain GRASS", 
                    pos[0], pos[1]));
        }
    }
    
    @Test
    public void testGrassInOuterZoneStillExists() {
        // Test that grass still exists in outer zones (beyond sand zone)
        // At least some positions far from spawn should be grass
        float[][] outerPositions = {
            {20000.0f, 0.0f},
            {0.0f, 25000.0f},
            {-30000.0f, 0.0f},
            {0.0f, -35000.0f}
        };
        
        int grassCount = 0;
        for (float[] pos : outerPositions) {
            BiomeType biome = biomeManager.getBiomeAtPosition(pos[0], pos[1]);
            if (biome == BiomeType.GRASS) {
                grassCount++;
            }
        }
        
        assertTrue(grassCount > 0, 
            "Grass biome should still exist in outer zones after water addition");
    }
    
    @Test
    public void testGrassBiomeDeterminismUnchanged() {
        // Grass biome determination should still be deterministic
        float testX = 5000.0f;
        float testY = 5000.0f;
        
        BiomeType biome1 = biomeManager.getBiomeAtPosition(testX, testY);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(testX, testY);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(testX, testY);
        
        assertEquals(biome1, biome2, 
            "Grass biome determination should remain deterministic");
        assertEquals(biome2, biome3, 
            "Grass biome determination should remain deterministic");
    }
    
    @Test
    public void testGrassDisplayNameUnchanged() {
        // Grass display name should remain "grass"
        assertEquals("grass", BiomeType.GRASS.getDisplayName(),
            "GRASS display name should remain unchanged");
    }
    
    // ===== Sand Biome Backward Compatibility Tests =====
    
    @Test
    public void testSandBiomeStillExists() {
        // Sand biome should still exist in the world
        // Test multiple positions to find at least some sand
        boolean foundSand = false;
        
        // Test positions in typical sand zone range (10000-13000px from spawn)
        float[][] sandZonePositions = {
            {11000.0f, 0.0f},
            {0.0f, 11500.0f},
            {-12000.0f, 0.0f},
            {0.0f, -12500.0f},
            {8000.0f, 8000.0f},  // ~11314px diagonal
            {-9000.0f, -9000.0f} // ~12728px diagonal
        };
        
        for (float[] pos : sandZonePositions) {
            BiomeType biome = biomeManager.getBiomeAtPosition(pos[0], pos[1]);
            if (biome == BiomeType.SAND) {
                foundSand = true;
                break;
            }
        }
        
        assertTrue(foundSand, 
            "Sand biome should still exist after water biome addition");
    }
    
    @Test
    public void testSandExclusionZoneUnchanged() {
        // Sand should not spawn within 1000px of spawn (unchanged behavior)
        float[][] innerPositions = {
            {500.0f, 0.0f},
            {0.0f, 800.0f},
            {-600.0f, -600.0f},
            {700.0f, 700.0f} // ~990px diagonal
        };
        
        for (float[] pos : innerPositions) {
            BiomeType biome = biomeManager.getBiomeAtPosition(pos[0], pos[1]);
            assertNotEquals(BiomeType.SAND, biome,
                String.format("Position (%.1f, %.1f) within 1000px should not be SAND", 
                    pos[0], pos[1]));
        }
    }
    
    @Test
    public void testSandBiomeDeterminismUnchanged() {
        // Sand biome determination should still be deterministic
        // Find a sand position first
        float testX = 11000.0f;
        float testY = 0.0f;
        
        BiomeType biome1 = biomeManager.getBiomeAtPosition(testX, testY);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(testX, testY);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(testX, testY);
        
        assertEquals(biome1, biome2, 
            "Sand biome determination should remain deterministic");
        assertEquals(biome2, biome3, 
            "Sand biome determination should remain deterministic");
    }
    
    @Test
    public void testSandDisplayNameUnchanged() {
        // Sand display name should remain "sand"
        assertEquals("sand", BiomeType.SAND.getDisplayName(),
            "SAND display name should remain unchanged");
    }
    
    // ===== Biome Zone Configuration Backward Compatibility Tests =====
    
    @Test
    public void testBiomeZoneCountUnchanged() {
        // Should still have 3 biome zones (inner grass, sand, outer grass)
        var zones = biomeManager.getBiomeZones();
        assertEquals(3, zones.size(), 
            "Should maintain 3 biome zones after water addition");
    }
    
    @Test
    public void testInnerGrassZoneUnchanged() {
        // Zone 1: Inner grass (0 to 10000) should be unchanged
        var zones = biomeManager.getBiomeZones();
        BiomeZone innerGrassZone = zones.get(0);
        
        assertEquals(0.0f, innerGrassZone.getMinDistance(), 0.01f,
            "Inner grass zone min distance should remain 0");
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS, innerGrassZone.getMaxDistance(), 0.01f,
            "Inner grass zone max distance should remain at INNER_GRASS_RADIUS");
        assertEquals(BiomeType.GRASS, innerGrassZone.getBiomeType(),
            "Inner zone should remain GRASS type");
    }
    
    @Test
    public void testSandZoneUnchanged() {
        // Zone 2: Sand (10000 to 13000) should be unchanged
        var zones = biomeManager.getBiomeZones();
        BiomeZone sandZone = zones.get(1);
        
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS, sandZone.getMinDistance(), 0.01f,
            "Sand zone min distance should remain at INNER_GRASS_RADIUS");
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH, 
            sandZone.getMaxDistance(), 0.01f,
            "Sand zone max distance should remain unchanged");
        assertEquals(BiomeType.SAND, sandZone.getBiomeType(),
            "Sand zone should remain SAND type");
    }
    
    @Test
    public void testOuterGrassZoneUnchanged() {
        // Zone 3: Outer grass (13000 to infinity) should be unchanged
        var zones = biomeManager.getBiomeZones();
        BiomeZone outerGrassZone = zones.get(2);
        
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH, 
            outerGrassZone.getMinDistance(), 0.01f,
            "Outer grass zone min distance should remain unchanged");
        assertEquals(Float.MAX_VALUE, outerGrassZone.getMaxDistance(), 0.01f,
            "Outer grass zone max distance should remain at MAX_VALUE");
        assertEquals(BiomeType.GRASS, outerGrassZone.getBiomeType(),
            "Outer zone should remain GRASS type");
    }
    
    @Test
    public void testBiomeZoneOrderUnchanged() {
        // Zone order should remain: inner grass, sand, outer grass
        var zones = biomeManager.getBiomeZones();
        
        assertEquals(BiomeType.GRASS, zones.get(0).getBiomeType(),
            "First zone should remain GRASS");
        assertEquals(BiomeType.SAND, zones.get(1).getBiomeType(),
            "Second zone should remain SAND");
        assertEquals(BiomeType.GRASS, zones.get(2).getBiomeType(),
            "Third zone should remain GRASS");
    }
    
    // ===== BiomeConfig Constants Backward Compatibility Tests =====
    
    @Test
    public void testInnerGrassRadiusUnchanged() {
        // INNER_GRASS_RADIUS should remain 10000
        assertEquals(10000.0f, BiomeConfig.INNER_GRASS_RADIUS, 0.01f,
            "INNER_GRASS_RADIUS should remain unchanged at 10000");
    }
    
    @Test
    public void testSandZoneWidthUnchanged() {
        // SAND_ZONE_WIDTH should remain 3000
        assertEquals(3000.0f, BiomeConfig.SAND_ZONE_WIDTH, 0.01f,
            "SAND_ZONE_WIDTH should remain unchanged at 3000");
    }
    
    @Test
    public void testTextureSizeUnchanged() {
        // TEXTURE_SIZE should remain 64
        assertEquals(64, BiomeConfig.TEXTURE_SIZE,
            "TEXTURE_SIZE should remain unchanged at 64");
    }
    
    @Test
    public void testGrassTextureSeedUnchanged() {
        // TEXTURE_SEED_GRASS should remain 12345
        assertEquals(12345, BiomeConfig.TEXTURE_SEED_GRASS,
            "TEXTURE_SEED_GRASS should remain unchanged");
    }
    
    @Test
    public void testSandTextureSeedUnchanged() {
        // TEXTURE_SEED_SAND should remain 54321
        assertEquals(54321, BiomeConfig.TEXTURE_SEED_SAND,
            "TEXTURE_SEED_SAND should remain unchanged");
    }
    
    // ===== Biome Distribution Backward Compatibility Tests =====
    
    @Test
    public void testGrassStillMostCommonBiome() {
        // Sample many positions and verify grass is still the most common biome
        int grassCount = 0;
        int sandCount = 0;
        int waterCount = 0;
        int sampleSize = 1000;
        
        // Sample positions across a large area
        for (int i = 0; i < sampleSize; i++) {
            float x = (float) (Math.random() * 40000 - 20000); // -20000 to 20000
            float y = (float) (Math.random() * 40000 - 20000);
            
            BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
            switch (biome) {
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
        
        assertTrue(grassCount > sandCount,
            "Grass should still be more common than sand");
        assertTrue(grassCount > waterCount,
            "Grass should still be more common than water");
    }
    
    @Test
    public void testSandStillExistsInSignificantAmounts() {
        // Sample many positions and verify sand still exists in reasonable amounts
        int sandCount = 0;
        int sampleSize = 1000;
        
        // Sample positions in typical sand zone range
        for (int i = 0; i < sampleSize; i++) {
            double angle = Math.random() * 2 * Math.PI;
            float distance = 11000.0f + (float) (Math.random() * 2000); // 11000-13000
            float x = (float) (Math.cos(angle) * distance);
            float y = (float) (Math.sin(angle) * distance);
            
            BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
            if (biome == BiomeType.SAND) {
                sandCount++;
            }
        }
        
        // Sand should appear in at least 10% of samples in its typical zone
        assertTrue(sandCount > sampleSize * 0.1,
            String.format("Sand should still exist in significant amounts (found %d/%d = %.1f%%)",
                sandCount, sampleSize, (sandCount * 100.0 / sampleSize)));
    }
    
    // ===== Biome Enum Backward Compatibility Tests =====
    
    @Test
    public void testBiomeTypeEnumHasThreeValues() {
        // BiomeType enum should now have exactly 3 values
        BiomeType[] types = BiomeType.values();
        assertEquals(3, types.length,
            "BiomeType enum should have exactly 3 values after water addition");
    }
    
    @Test
    public void testGrassEnumValueUnchanged() {
        // GRASS should still be the first enum value
        BiomeType[] types = BiomeType.values();
        assertEquals(BiomeType.GRASS, types[0],
            "GRASS should remain the first BiomeType enum value");
    }
    
    @Test
    public void testSandEnumValueUnchanged() {
        // SAND should still be the second enum value
        BiomeType[] types = BiomeType.values();
        assertEquals(BiomeType.SAND, types[1],
            "SAND should remain the second BiomeType enum value");
    }
    
    // ===== BiomeManager API Backward Compatibility Tests =====
    
    @Test
    public void testGetBiomeAtPositionStillWorks() {
        // getBiomeAtPosition should still work for all positions
        assertDoesNotThrow(() -> {
            biomeManager.getBiomeAtPosition(0.0f, 0.0f);
            biomeManager.getBiomeAtPosition(5000.0f, 5000.0f);
            biomeManager.getBiomeAtPosition(-10000.0f, 10000.0f);
        }, "getBiomeAtPosition should still work after water addition");
    }
    
    @Test
    public void testGetBiomeZonesStillWorks() {
        // getBiomeZones should still return valid zones
        var zones = biomeManager.getBiomeZones();
        assertNotNull(zones, "getBiomeZones should still return zones");
        assertFalse(zones.isEmpty(), "getBiomeZones should return non-empty list");
    }
    
    @Test
    public void testIsInitializedStillWorks() {
        // isInitialized should still work
        assertTrue(biomeManager.isInitialized(),
            "isInitialized should still work after water addition");
    }
    
    @Test
    public void testDisposeStillWorks() {
        // dispose should still work without errors
        assertDoesNotThrow(() -> biomeManager.dispose(),
            "dispose should still work after water addition");
    }
    
    // ===== Negative Coordinate Backward Compatibility Tests =====
    
    @Test
    public void testNegativeCoordinatesStillWork() {
        // Negative coordinates should still work correctly
        assertDoesNotThrow(() -> {
            BiomeType biome1 = biomeManager.getBiomeAtPosition(-5000.0f, 0.0f);
            BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, -5000.0f);
            BiomeType biome3 = biomeManager.getBiomeAtPosition(-5000.0f, -5000.0f);
            
            assertNotNull(biome1, "Should handle negative X");
            assertNotNull(biome2, "Should handle negative Y");
            assertNotNull(biome3, "Should handle both negative");
        }, "Negative coordinates should still work after water addition");
    }
    
    // ===== Extreme Distance Backward Compatibility Tests =====
    
    @Test
    public void testExtremeDistancesStillWork() {
        // Extreme distances should still work correctly
        assertDoesNotThrow(() -> {
            BiomeType biome1 = biomeManager.getBiomeAtPosition(50000.0f, 0.0f);
            BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, 100000.0f);
            BiomeType biome3 = biomeManager.getBiomeAtPosition(-75000.0f, -75000.0f);
            
            assertNotNull(biome1, "Should handle extreme positive X");
            assertNotNull(biome2, "Should handle extreme positive Y");
            assertNotNull(biome3, "Should handle extreme negative coordinates");
        }, "Extreme distances should still work after water addition");
    }
}
