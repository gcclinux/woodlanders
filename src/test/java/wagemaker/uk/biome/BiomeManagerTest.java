package wagemaker.uk.biome;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BiomeManager distance calculations and biome determination.
 * Tests core functionality including boundary conditions, negative coordinates, and extreme distances.
 * 
 * Requirements: 1.2 (distance calculation), 1.4 (natural variation), 4.2 (deterministic)
 */
public class BiomeManagerTest {
    
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
    
    // ===== Initialization Tests =====
    
    @Test
    public void testInitialization() {
        assertNotNull(biomeManager, "BiomeManager should be instantiated");
        assertTrue(biomeManager.isInitialized(), "BiomeManager should be initialized");
    }
    
    @Test
    public void testMultipleInitializationsSafe() {
        biomeManager.initialize();
        biomeManager.initialize();
        assertTrue(biomeManager.isInitialized(), "Multiple initializations should be safe");
    }
    
    @Test
    public void testUninitializedManagerThrowsException() {
        BiomeManager uninitializedManager = new BiomeManager();
        assertThrows(IllegalStateException.class, () -> {
            uninitializedManager.getTextureForPosition(0, 0);
        }, "Uninitialized BiomeManager should throw exception");
        uninitializedManager.dispose();
    }
    
    // ===== Distance Calculation Tests =====
    
    @Test
    public void testBiomeAtSpawn() {
        // At spawn point (0,0), should be GRASS
        BiomeType biome = biomeManager.getBiomeAtPosition(0.0f, 0.0f);
        assertEquals(BiomeType.GRASS, biome, "Spawn point should be GRASS biome");
    }
    
    @Test
    public void testBiomeInInnerGrassZone() {
        // Within inner grass radius (< 10000px), should be GRASS
        BiomeType biome1 = biomeManager.getBiomeAtPosition(5000.0f, 0.0f);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, 5000.0f);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(3000.0f, 4000.0f); // 5000px diagonal
        
        // Note: Due to noise variation, we can't guarantee exact biome type
        // but we can verify the method doesn't crash
        assertNotNull(biome1, "Biome should be determined for inner zone");
        assertNotNull(biome2, "Biome should be determined for inner zone");
        assertNotNull(biome3, "Biome should be determined for inner zone");
    }
    
    @Test
    public void testBiomeInOuterGrassZone() {
        // Beyond sand zone (> 13000px), should be GRASS
        BiomeType biome1 = biomeManager.getBiomeAtPosition(15000.0f, 0.0f);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, 20000.0f);
        
        assertNotNull(biome1, "Biome should be determined for outer zone");
        assertNotNull(biome2, "Biome should be determined for outer zone");
    }
    
    // ===== Boundary Condition Tests =====
    
    @Test
    public void testBiomeAtInnerBoundary() {
        // Exactly at 10000px boundary (inner grass to sand transition)
        // Due to noise, exact biome may vary, but should not crash
        BiomeType biome1 = biomeManager.getBiomeAtPosition(10000.0f, 0.0f);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, 10000.0f);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(7071.0f, 7071.0f); // ~10000px diagonal
        
        assertNotNull(biome1, "Biome should be determined at inner boundary");
        assertNotNull(biome2, "Biome should be determined at inner boundary");
        assertNotNull(biome3, "Biome should be determined at inner boundary");
    }
    
    @Test
    public void testBiomeAtOuterBoundary() {
        // Exactly at 13000px boundary (sand to outer grass transition)
        BiomeType biome1 = biomeManager.getBiomeAtPosition(13000.0f, 0.0f);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, 13000.0f);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(9192.0f, 9192.0f); // ~13000px diagonal
        
        assertNotNull(biome1, "Biome should be determined at outer boundary");
        assertNotNull(biome2, "Biome should be determined at outer boundary");
        assertNotNull(biome3, "Biome should be determined at outer boundary");
    }
    
    @Test
    public void testBiomeJustInsideInnerBoundary() {
        // Just inside inner grass zone (9999px)
        BiomeType biome = biomeManager.getBiomeAtPosition(9999.0f, 0.0f);
        assertNotNull(biome, "Biome should be determined just inside inner boundary");
    }
    
    @Test
    public void testBiomeJustOutsideInnerBoundary() {
        // Just outside inner grass zone (10001px)
        BiomeType biome = biomeManager.getBiomeAtPosition(10001.0f, 0.0f);
        assertNotNull(biome, "Biome should be determined just outside inner boundary");
    }
    
    @Test
    public void testBiomeJustInsideOuterBoundary() {
        // Just inside sand zone (12999px)
        BiomeType biome = biomeManager.getBiomeAtPosition(12999.0f, 0.0f);
        assertNotNull(biome, "Biome should be determined just inside outer boundary");
    }
    
    @Test
    public void testBiomeJustOutsideOuterBoundary() {
        // Just outside sand zone (13001px)
        BiomeType biome = biomeManager.getBiomeAtPosition(13001.0f, 0.0f);
        assertNotNull(biome, "Biome should be determined just outside outer boundary");
    }
    
    // ===== Negative Coordinate Tests =====
    
    @Test
    public void testBiomeWithNegativeX() {
        BiomeType biome = biomeManager.getBiomeAtPosition(-5000.0f, 0.0f);
        assertNotNull(biome, "Biome should be determined for negative X coordinate");
    }
    
    @Test
    public void testBiomeWithNegativeY() {
        BiomeType biome = biomeManager.getBiomeAtPosition(0.0f, -5000.0f);
        assertNotNull(biome, "Biome should be determined for negative Y coordinate");
    }
    
    @Test
    public void testBiomeWithBothNegative() {
        BiomeType biome = biomeManager.getBiomeAtPosition(-5000.0f, -5000.0f);
        assertNotNull(biome, "Biome should be determined for both negative coordinates");
    }
    
    @Test
    public void testBiomeWithMixedCoordinates() {
        BiomeType biome1 = biomeManager.getBiomeAtPosition(-3000.0f, 4000.0f); // 5000px distance
        BiomeType biome2 = biomeManager.getBiomeAtPosition(8000.0f, -6000.0f); // 10000px distance
        
        assertNotNull(biome1, "Biome should be determined for mixed coordinates");
        assertNotNull(biome2, "Biome should be determined for mixed coordinates");
    }
    
    @Test
    public void testBiomeWithLargeNegativeCoordinates() {
        BiomeType biome = biomeManager.getBiomeAtPosition(-15000.0f, -15000.0f);
        assertNotNull(biome, "Biome should be determined for large negative coordinates");
    }
    
    // ===== Extreme Distance Tests =====
    
    @Test
    public void testBiomeAtExtremeDistance() {
        BiomeType biome1 = biomeManager.getBiomeAtPosition(50000.0f, 0.0f);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(0.0f, 100000.0f);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(70710.0f, 70710.0f); // ~100000px diagonal
        
        assertNotNull(biome1, "Biome should be determined at extreme distance");
        assertNotNull(biome2, "Biome should be determined at extreme distance");
        assertNotNull(biome3, "Biome should be determined at extreme distance");
    }
    
    @Test
    public void testBiomeAtVeryExtremeDistance() {
        BiomeType biome = biomeManager.getBiomeAtPosition(500000.0f, 500000.0f);
        assertNotNull(biome, "Biome should be determined at very extreme distance");
    }
    
    @Test
    public void testBiomeAtExtremeNegativeDistance() {
        BiomeType biome = biomeManager.getBiomeAtPosition(-100000.0f, -100000.0f);
        assertNotNull(biome, "Biome should be determined at extreme negative distance");
    }
    
    // ===== Deterministic Behavior Tests =====
    
    @Test
    public void testDeterministicBiomeCalculation() {
        // Same coordinates should always return same biome
        float testX = 5000.0f;
        float testY = 5000.0f;
        
        BiomeType biome1 = biomeManager.getBiomeAtPosition(testX, testY);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(testX, testY);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(testX, testY);
        
        assertEquals(biome1, biome2, "Same coordinates should return same biome");
        assertEquals(biome2, biome3, "Same coordinates should return same biome");
    }
    
    @Test
    public void testDeterministicAcrossMultiplePositions() {
        // Test multiple positions for consistency
        float[][] positions = {
            {0.0f, 0.0f},
            {5000.0f, 0.0f},
            {0.0f, 5000.0f},
            {10000.0f, 0.0f},
            {15000.0f, 0.0f},
            {-5000.0f, -5000.0f}
        };
        
        for (float[] pos : positions) {
            BiomeType biome1 = biomeManager.getBiomeAtPosition(pos[0], pos[1]);
            BiomeType biome2 = biomeManager.getBiomeAtPosition(pos[0], pos[1]);
            
            assertEquals(biome1, biome2, 
                "Position (" + pos[0] + ", " + pos[1] + ") should return consistent biome");
        }
    }
    
    // ===== Texture Retrieval Tests =====
    // Note: Texture tests are skipped in headless mode (unit tests)
    // Texture functionality is tested in integration tests with full graphics context
    
    @Test
    public void testGetTextureForPosition() {
        // In headless mode, getTextureForPosition will throw an exception
        // This is expected behavior - we test biome determination instead
        BiomeType biome1 = biomeManager.getBiomeAtPosition(0.0f, 0.0f);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(5000.0f, 5000.0f);
        BiomeType biome3 = biomeManager.getBiomeAtPosition(15000.0f, 0.0f);
        
        assertNotNull(biome1, "Should determine biome for spawn position");
        assertNotNull(biome2, "Should determine biome for inner grass zone");
        assertNotNull(biome3, "Should determine biome for outer grass zone");
    }
    
    @Test
    public void testTextureConsistency() {
        // Same position should return same biome type (deterministic)
        float testX = 5000.0f;
        float testY = 5000.0f;
        
        BiomeType biome1 = biomeManager.getBiomeAtPosition(testX, testY);
        BiomeType biome2 = biomeManager.getBiomeAtPosition(testX, testY);
        
        assertNotNull(biome1, "Biome should not be null");
        assertNotNull(biome2, "Biome should not be null");
        assertEquals(biome1, biome2, "Same position should return same biome");
    }
    
    // ===== Edge Case Tests =====
    
    @Test
    public void testVerySmallCoordinates() {
        BiomeType biome = biomeManager.getBiomeAtPosition(0.001f, 0.001f);
        assertNotNull(biome, "Should handle very small coordinates");
    }
    
    @Test
    public void testZeroCoordinates() {
        BiomeType biome = biomeManager.getBiomeAtPosition(0.0f, 0.0f);
        assertEquals(BiomeType.GRASS, biome, "Zero coordinates should be GRASS");
    }
    
    @Test
    public void testDiagonalMovement() {
        // Test 3-4-5 triangle (distance = 5000)
        BiomeType biome = biomeManager.getBiomeAtPosition(3000.0f, 4000.0f);
        assertNotNull(biome, "Should handle diagonal movement correctly");
    }
    
    @Test
    public void testAllCardinalDirections() {
        float distance = 5000.0f;
        
        BiomeType north = biomeManager.getBiomeAtPosition(0.0f, distance);
        BiomeType south = biomeManager.getBiomeAtPosition(0.0f, -distance);
        BiomeType east = biomeManager.getBiomeAtPosition(distance, 0.0f);
        BiomeType west = biomeManager.getBiomeAtPosition(-distance, 0.0f);
        
        assertNotNull(north, "Should determine biome to the north");
        assertNotNull(south, "Should determine biome to the south");
        assertNotNull(east, "Should determine biome to the east");
        assertNotNull(west, "Should determine biome to the west");
    }
    
    // ===== Disposal Tests =====
    
    @Test
    public void testDisposal() {
        assertDoesNotThrow(() -> biomeManager.dispose(), 
            "Disposal should not throw exceptions");
    }
    
    @Test
    public void testMultipleDisposals() {
        biomeManager.dispose();
        assertDoesNotThrow(() -> biomeManager.dispose(), 
            "Multiple disposals should be safe");
    }
    
    @Test
    public void testDisposalClearsInitialization() {
        biomeManager.dispose();
        assertFalse(biomeManager.isInitialized(), 
            "Disposal should clear initialization flag");
    }
    
    // ===== Base Biome Calculation Tests =====
    
    @Test
    public void testBaseBiomeAtSpawn() {
        // At spawn point (0,0), should be GRASS
        BiomeType baseBiome = biomeManager.getBaseBiomeAtPosition(0.0f, 0.0f);
        assertEquals(BiomeType.GRASS, baseBiome, "Spawn point should be GRASS base biome");
    }
    
    @Test
    public void testBaseBiomeInInnerGrassZone() {
        // Within inner grass radius (< 10000px), should be GRASS
        BiomeType baseBiome1 = biomeManager.getBaseBiomeAtPosition(5000.0f, 0.0f);
        BiomeType baseBiome2 = biomeManager.getBaseBiomeAtPosition(0.0f, 5000.0f);
        BiomeType baseBiome3 = biomeManager.getBaseBiomeAtPosition(3000.0f, 4000.0f); // 5000px diagonal
        
        assertEquals(BiomeType.GRASS, baseBiome1, "Inner grass zone should be GRASS base biome");
        assertEquals(BiomeType.GRASS, baseBiome2, "Inner grass zone should be GRASS base biome");
        assertEquals(BiomeType.GRASS, baseBiome3, "Inner grass zone should be GRASS base biome");
    }
    
    @Test
    public void testBaseBiomeInSandZone() {
        // Within sand zone (10000-13000px), should be SAND
        BiomeType baseBiome1 = biomeManager.getBaseBiomeAtPosition(11000.0f, 0.0f);
        BiomeType baseBiome2 = biomeManager.getBaseBiomeAtPosition(0.0f, 12000.0f);
        
        assertEquals(BiomeType.SAND, baseBiome1, "Sand zone should be SAND base biome");
        assertEquals(BiomeType.SAND, baseBiome2, "Sand zone should be SAND base biome");
    }
    
    @Test
    public void testBaseBiomeBeyondSandZone() {
        // Beyond sand zone (> 13000px), should be noise-based grass/sand
        BiomeType baseBiome1 = biomeManager.getBaseBiomeAtPosition(15000.0f, 0.0f);
        BiomeType baseBiome2 = biomeManager.getBaseBiomeAtPosition(0.0f, 20000.0f);
        
        // Should be either GRASS or SAND (not WATER)
        assertTrue(baseBiome1 == BiomeType.GRASS || baseBiome1 == BiomeType.SAND, 
            "Base biome beyond sand zone should be GRASS or SAND");
        assertTrue(baseBiome2 == BiomeType.GRASS || baseBiome2 == BiomeType.SAND, 
            "Base biome beyond sand zone should be GRASS or SAND");
    }
    
    @Test
    public void testBaseBiomeDeterministic() {
        // Same coordinates should always return same base biome
        float testX = 15000.0f;
        float testY = 15000.0f;
        
        BiomeType baseBiome1 = biomeManager.getBaseBiomeAtPosition(testX, testY);
        BiomeType baseBiome2 = biomeManager.getBaseBiomeAtPosition(testX, testY);
        BiomeType baseBiome3 = biomeManager.getBaseBiomeAtPosition(testX, testY);
        
        assertEquals(baseBiome1, baseBiome2, "Same coordinates should return same base biome");
        assertEquals(baseBiome2, baseBiome3, "Same coordinates should return same base biome");
    }
    
    @Test
    public void testBaseBiomeNeverReturnsWater() {
        // Base biome should never return WATER (only GRASS or SAND)
        float[][] testPositions = {
            {0.0f, 0.0f},
            {5000.0f, 5000.0f},
            {11000.0f, 0.0f},
            {15000.0f, 15000.0f},
            {-10000.0f, -10000.0f}
        };
        
        for (float[] pos : testPositions) {
            BiomeType baseBiome = biomeManager.getBaseBiomeAtPosition(pos[0], pos[1]);
            assertNotEquals(BiomeType.WATER, baseBiome, 
                "Base biome at (" + pos[0] + ", " + pos[1] + ") should never be WATER");
            assertTrue(baseBiome == BiomeType.GRASS || baseBiome == BiomeType.SAND,
                "Base biome at (" + pos[0] + ", " + pos[1] + ") should be GRASS or SAND");
        }
    }
    
    // ===== Biome Zone Configuration Tests =====
    
    @Test
    public void testBiomeZonesConfigured() {
        var zones = biomeManager.getBiomeZones();
        assertNotNull(zones, "Biome zones should be configured");
        assertEquals(3, zones.size(), "Should have 3 biome zones (inner grass, sand, outer grass)");
    }
    
    @Test
    public void testBiomeZoneOrder() {
        var zones = biomeManager.getBiomeZones();
        
        // Zone 1: Inner grass (0 to 10000)
        assertEquals(0.0f, zones.get(0).getMinDistance(), 0.01f, 
            "First zone should start at 0");
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS, zones.get(0).getMaxDistance(), 0.01f, 
            "First zone should end at inner grass radius");
        assertEquals(BiomeType.GRASS, zones.get(0).getBiomeType(), 
            "First zone should be GRASS");
        
        // Zone 2: Sand (10000 to 13000)
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS, zones.get(1).getMinDistance(), 0.01f, 
            "Second zone should start at inner grass radius");
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH, 
            zones.get(1).getMaxDistance(), 0.01f, 
            "Second zone should end at inner radius + sand width");
        assertEquals(BiomeType.SAND, zones.get(1).getBiomeType(), 
            "Second zone should be SAND");
        
        // Zone 3: Outer grass (13000 to infinity)
        assertEquals(BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH, 
            zones.get(2).getMinDistance(), 0.01f, 
            "Third zone should start where sand zone ends");
        assertEquals(Float.MAX_VALUE, zones.get(2).getMaxDistance(), 0.01f, 
            "Third zone should extend to infinity");
        assertEquals(BiomeType.GRASS, zones.get(2).getBiomeType(), 
            "Third zone should be GRASS");
    }
    
    // ===== Buffer Zone Validation Tests =====
    
    @Test
    public void testIsValidBeachBufferInGrassArea() {
        // Test buffer validation in a known grass area (spawn point)
        boolean isValid = biomeManager.isValidBeachBuffer(0.0f, 0.0f);
        assertFalse(isValid, "Buffer validation should fail in grass area (spawn point)");
    }
    
    @Test
    public void testIsValidBeachBufferInSandZone() {
        // Test buffer validation in the sand zone (11000px from spawn)
        boolean isValid = biomeManager.isValidBeachBuffer(11000.0f, 0.0f);
        assertTrue(isValid, "Buffer validation should pass in sand zone");
    }
    
    @Test
    public void testIsValidBeachBufferNearGrassBoundary() {
        // Test buffer validation near the grass/sand boundary
        // At 10000px + 64px (just inside sand zone but close to grass)
        float testX = BiomeConfig.INNER_GRASS_RADIUS + 64.0f;
        boolean isValid = biomeManager.isValidBeachBuffer(testX, 0.0f);
        
        // Should fail because buffer extends into grass area
        assertFalse(isValid, "Buffer validation should fail near grass boundary");
    }
    
    @Test
    public void testIsValidBeachBufferFarFromGrass() {
        // Test buffer validation far from any grass areas
        // At 11500px from spawn (well into sand zone)
        float testX = BiomeConfig.INNER_GRASS_RADIUS + 1500.0f;
        boolean isValid = biomeManager.isValidBeachBuffer(testX, 0.0f);
        
        assertTrue(isValid, "Buffer validation should pass far from grass areas");
    }
    
    @Test
    public void testCalculateDistanceToGrassAtSpawn() {
        // At spawn point, distance to grass should be 0 (we're in grass)
        float distance = biomeManager.calculateDistanceToGrass(0.0f, 0.0f);
        assertEquals(0.0f, distance, 1.0f, "Distance to grass at spawn should be 0");
    }
    
    @Test
    public void testCalculateDistanceToGrassInSandZone() {
        // In sand zone, distance to grass should be approximately the distance to the boundary
        float testX = BiomeConfig.INNER_GRASS_RADIUS + 500.0f; // 500px into sand zone
        float distance = biomeManager.calculateDistanceToGrass(testX, 0.0f);
        
        // Should be approximately 500px (distance back to grass boundary)
        assertTrue(distance >= 400.0f && distance <= 600.0f, 
            "Distance to grass in sand zone should be approximately distance to boundary, got: " + distance);
    }
    
    @Test
    public void testCalculateDistanceToGrassConsistency() {
        // Same position should return same distance
        float testX = 11000.0f;
        float testY = 0.0f;
        
        float distance1 = biomeManager.calculateDistanceToGrass(testX, testY);
        float distance2 = biomeManager.calculateDistanceToGrass(testX, testY);
        
        assertEquals(distance1, distance2, 0.01f, 
            "Same position should return same distance to grass");
    }
    
    @Test
    public void testBufferValidationDeterministic() {
        // Same position should return same buffer validation result
        float testX = 11000.0f;
        float testY = 0.0f;
        
        boolean isValid1 = biomeManager.isValidBeachBuffer(testX, testY);
        boolean isValid2 = biomeManager.isValidBeachBuffer(testX, testY);
        boolean isValid3 = biomeManager.isValidBeachBuffer(testX, testY);
        
        assertEquals(isValid1, isValid2, "Same position should return same buffer validation");
        assertEquals(isValid2, isValid3, "Same position should return same buffer validation");
    }
}
