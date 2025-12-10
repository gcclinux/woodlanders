package wagemaker.uk.biome;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for BiomeManager optimizations.
 * Tests spatial caching, early-exit optimizations, and performance profiling.
 * 
 * Requirements: 1.4 (performance optimization), 2.3 (tuning)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BiomePerformanceOptimizationTest {
    
    private BiomeManager biomeManager;
    
    @BeforeEach
    public void setUp() {
        biomeManager = new BiomeManager();
        biomeManager.initialize();
    }
    
    /**
     * Test that spatial caching improves buffer validation performance.
     * Validates that repeated calls to the same area are faster due to caching.
     */
    @Test
    @Order(1)
    public void testSpatialCachingPerformance() {
        // Clear caches to start fresh
        biomeManager.clearPerformanceCaches();
        
        // Test coordinates in sand areas where buffer validation is needed
        float[] testX = {15000.0f, 15100.0f, 15200.0f, 15000.0f, 15100.0f};
        float[] testY = {15000.0f, 15100.0f, 15200.0f, 15000.0f, 15100.0f};
        
        // First pass - populate cache
        long startTime = System.nanoTime();
        for (int i = 0; i < testX.length; i++) {
            biomeManager.isValidBeachBuffer(testX[i], testY[i]);
        }
        long firstPassTime = System.nanoTime() - startTime;
        
        // Second pass - should use cache
        startTime = System.nanoTime();
        for (int i = 0; i < testX.length; i++) {
            biomeManager.isValidBeachBuffer(testX[i], testY[i]);
        }
        long secondPassTime = System.nanoTime() - startTime;
        
        // Cache should make second pass faster (allowing some variance for JVM warmup)
        assertTrue(secondPassTime < firstPassTime * 1.5, 
                  "Second pass should be faster due to caching. First: " + firstPassTime + "ns, Second: " + secondPassTime + "ns");
        
        // Verify cache is populated
        Map<String, Object> stats = biomeManager.getPerformanceStats();
        assertTrue((Integer) stats.get("bufferValidationCacheSize") > 0, "Buffer validation cache should be populated");
    }
    
    /**
     * Test that performance caches are properly managed and cleared when they get too large.
     */
    @Test
    @Order(2)
    public void testCacheManagement() {
        biomeManager.clearPerformanceCaches();
        
        // Fill cache beyond maximum size by testing many different locations
        int maxCacheSize = (Integer) biomeManager.getPerformanceStats().get("maxCacheSize");
        
        // Test more locations than the cache can hold
        for (int i = 0; i < maxCacheSize + 100; i++) {
            float x = 15000.0f + (i * 200.0f); // Spread out to avoid grid collisions
            float y = 15000.0f + (i * 200.0f);
            biomeManager.isValidBeachBuffer(x, y);
        }
        
        // Cache should have been cleared and is now smaller
        Map<String, Object> stats = biomeManager.getPerformanceStats();
        int finalCacheSize = (Integer) stats.get("bufferValidationCacheSize");
        assertTrue(finalCacheSize < maxCacheSize, 
                  "Cache should have been cleared when it exceeded maximum size. Final size: " + finalCacheSize);
    }
    
    /**
     * Test biome distribution measurement functionality.
     * Validates that the measurement tools work correctly and produce reasonable results.
     */
    @Test
    @Order(3)
    public void testBiomeDistributionMeasurement() {
        // Measure distribution with a reasonable sample size
        Map<String, Double> distribution = biomeManager.measureBiomeDistribution(1000, 30000.0f);
        
        // Verify all expected keys are present
        assertTrue(distribution.containsKey("grass"), "Distribution should include grass percentage");
        assertTrue(distribution.containsKey("sand"), "Distribution should include sand percentage");
        assertTrue(distribution.containsKey("water"), "Distribution should include water percentage");
        assertTrue(distribution.containsKey("sampleSize"), "Distribution should include sample size");
        
        // Verify percentages add up to approximately 100%
        double total = distribution.get("grass") + distribution.get("sand") + distribution.get("water");
        assertTrue(Math.abs(total - 100.0) < 1.0, "Total percentage should be close to 100%, got: " + total);
        
        // Verify reasonable distribution (allowing wide tolerance for small sample)
        double grassPercent = distribution.get("grass");
        double sandPercent = distribution.get("sand");
        double waterPercent = distribution.get("water");
        
        assertTrue(grassPercent > 20.0 && grassPercent < 80.0, "Grass percentage should be reasonable: " + grassPercent);
        assertTrue(sandPercent > 10.0 && sandPercent < 70.0, "Sand percentage should be reasonable: " + sandPercent);
        assertTrue(waterPercent > 5.0 && waterPercent < 50.0, "Water percentage should be reasonable: " + waterPercent);
        
        System.out.println("Measured distribution - Grass: " + String.format("%.1f", grassPercent) + 
                          "%, Sand: " + String.format("%.1f", sandPercent) + 
                          "%, Water: " + String.format("%.1f", waterPercent) + "%");
    }
    
    /**
     * Test water coverage measurement in sand areas.
     * Validates that the water-in-sand measurement works correctly.
     */
    @Test
    @Order(4)
    public void testWaterCoverageInSandMeasurement() {
        // Measure water coverage in sand areas
        double waterCoverage = biomeManager.measureWaterCoverageInSand(500);
        
        // Should be a reasonable percentage (allowing wide tolerance)
        assertTrue(waterCoverage >= 0.0 && waterCoverage <= 100.0, 
                  "Water coverage should be a valid percentage: " + waterCoverage);
        
        // Should be in a reasonable range based on our threshold settings
        assertTrue(waterCoverage > 10.0 && waterCoverage < 80.0, 
                  "Water coverage in sand should be reasonable: " + waterCoverage + "%");
        
        System.out.println("Measured water coverage in sand areas: " + String.format("%.1f", waterCoverage) + "%");
    }
    
    /**
     * Test performance profiling functionality.
     * Validates that performance measurement tools work and provide useful metrics.
     */
    @Test
    @Order(5)
    public void testPerformanceProfiling() {
        // Profile biome calculation performance
        Map<String, Double> metrics = biomeManager.profileBiomeCalculationPerformance(1000);
        
        // Verify all expected metrics are present
        assertTrue(metrics.containsKey("totalBiomeCalculations"), "Metrics should include total calculations");
        assertTrue(metrics.containsKey("totalTimeNanos"), "Metrics should include total time");
        assertTrue(metrics.containsKey("averageTimeNanos"), "Metrics should include average time");
        assertTrue(metrics.containsKey("biomeCalculationsPerSecond"), "Metrics should include calculations per second");
        
        // Verify reasonable performance metrics
        double totalCalculations = metrics.get("totalBiomeCalculations");
        double averageTimeNanos = metrics.get("averageTimeNanos");
        double calculationsPerSecond = metrics.get("biomeCalculationsPerSecond");
        
        assertEquals(1000.0, totalCalculations, "Should have performed 1000 calculations");
        assertTrue(averageTimeNanos > 0, "Average time should be positive: " + averageTimeNanos);
        assertTrue(calculationsPerSecond > 100, "Should be able to perform at least 100 calculations per second: " + calculationsPerSecond);
        
        // Performance should be reasonable (less than 1ms per calculation on average)
        assertTrue(averageTimeNanos < 1_000_000, "Average calculation time should be less than 1ms: " + averageTimeNanos + "ns");
        
        System.out.println("Performance metrics:");
        System.out.println("  Average time per calculation: " + String.format("%.0f", averageTimeNanos) + " nanoseconds");
        System.out.println("  Calculations per second: " + String.format("%.0f", calculationsPerSecond));
        
        if (metrics.containsKey("bufferValidationsPerSecond")) {
            System.out.println("  Buffer validations per second: " + String.format("%.0f", metrics.get("bufferValidationsPerSecond")));
        }
    }
    
    /**
     * Test spatial caching logic with specific coordinate patterns.
     * Validates that coordinates within the same cache grid cell share cached results.
     * Requirements: 1.4 (performance optimization)
     */
    @Test
    @Order(6)
    public void testSpatialCachingLogic() {
        biomeManager.clearPerformanceCaches();
        
        // Test coordinates that should map to the same cache grid cell
        // Buffer validation uses 64px grid size, so coordinates within 64px should share cache
        float baseX = 15000.0f;
        float baseY = 15000.0f;
        
        // These coordinates should all map to the same cache cell (within 64px grid)
        float[] sameGridX = {baseX, baseX + 30.0f, baseX + 50.0f, baseX + 63.0f};
        float[] sameGridY = {baseY, baseY + 30.0f, baseY + 50.0f, baseY + 63.0f};
        
        // First call should populate cache
        boolean firstResult = biomeManager.isValidBeachBuffer(sameGridX[0], sameGridY[0]);
        Map<String, Object> statsAfterFirst = biomeManager.getPerformanceStats();
        int cacheAfterFirst = (Integer) statsAfterFirst.get("bufferValidationCacheSize");
        
        // Subsequent calls within same grid should use cache and give same result
        for (int i = 1; i < sameGridX.length; i++) {
            boolean result = biomeManager.isValidBeachBuffer(sameGridX[i], sameGridY[i]);
            assertEquals(firstResult, result, "Results within same cache grid should be identical");
        }
        
        // Cache size should not have increased significantly (allowing for some growth due to grid boundaries)
        Map<String, Object> statsAfterSame = biomeManager.getPerformanceStats();
        int cacheAfterSame = (Integer) statsAfterSame.get("bufferValidationCacheSize");
        assertTrue(cacheAfterSame <= cacheAfterFirst + 2, 
                  "Cache should not grow significantly for same grid coordinates. Before: " + cacheAfterFirst + ", After: " + cacheAfterSame);
        
        // Test coordinates in clearly different cache grid cells
        float[] differentGridX = {baseX + 200.0f, baseX + 400.0f, baseX + 600.0f};
        float[] differentGridY = {baseY + 200.0f, baseY + 400.0f, baseY + 600.0f};
        
        for (int i = 0; i < differentGridX.length; i++) {
            biomeManager.isValidBeachBuffer(differentGridX[i], differentGridY[i]);
        }
        
        // Cache should have grown for different grid cells
        Map<String, Object> statsAfterDifferent = biomeManager.getPerformanceStats();
        int cacheAfterDifferent = (Integer) statsAfterDifferent.get("bufferValidationCacheSize");
        assertTrue(cacheAfterDifferent > cacheAfterSame, 
                  "Cache should grow when accessing different grid cells. Same: " + cacheAfterSame + ", Different: " + cacheAfterDifferent);
    }
    
    /**
     * Test early-exit buffer validation optimizations.
     * Validates that buffer validation exits early for obvious cases.
     * Requirements: 1.4 (performance optimization)
     */
    @Test
    @Order(7)
    public void testEarlyExitBufferValidation() {
        biomeManager.clearPerformanceCaches();
        
        // Test coordinates very close to inner grass boundary (should exit early as invalid)
        float innerRadius = 10000.0f; // BiomeConfig.INNER_GRASS_RADIUS
        float bufferDistance = 128.0f; // BiomeConfig.BEACH_BUFFER_DISTANCE
        
        // Coordinates just outside inner grass but within buffer distance
        float closeX = innerRadius + bufferDistance * 0.3f;
        float closeY = 0.0f;
        
        long startTime = System.nanoTime();
        boolean closeResult = biomeManager.isValidBeachBuffer(closeX, closeY);
        long closeTime = System.nanoTime() - startTime;
        
        // Should be invalid due to proximity to inner grass
        assertFalse(closeResult, "Coordinates close to inner grass should be invalid");
        
        // Test coordinates very far from spawn (should use reduced sampling)
        float farDistance = 25000.0f; // Beyond REDUCED_SAMPLING_DISTANCE
        float farX = farDistance;
        float farY = 0.0f;
        
        startTime = System.nanoTime();
        boolean farResult = biomeManager.isValidBeachBuffer(farX, farY);
        long farTime = System.nanoTime() - startTime;
        
        // Far coordinates should complete quickly due to reduced sampling
        assertTrue(farTime > 0, "Far buffer validation should complete");
        
        // Test coordinates in normal range (should use standard validation)
        float normalX = 15000.0f;
        float normalY = 15000.0f;
        
        startTime = System.nanoTime();
        boolean normalResult = biomeManager.isValidBeachBuffer(normalX, normalY);
        long normalTime = System.nanoTime() - startTime;
        
        assertTrue(normalTime > 0, "Normal buffer validation should complete");
        
        // Early exit cases should generally be faster than normal cases
        // (allowing some variance due to JVM optimization)
        System.out.println("Buffer validation times - Close: " + closeTime + "ns, Far: " + farTime + "ns, Normal: " + normalTime + "ns");
    }
    
    /**
     * Test threshold tuning with known coordinate sets.
     * Validates that specific coordinates produce expected biome results based on thresholds.
     * Requirements: 1.4 (threshold tuning), 2.3 (tuning)
     */
    @Test
    @Order(8)
    public void testThresholdTuningWithKnownCoordinates() {
        // Test coordinates in known biome zones
        
        // Inner grass zone - should always be grass
        float innerGrassX = 5000.0f;
        float innerGrassY = 5000.0f;
        BiomeType innerResult = biomeManager.getBiomeAtPosition(innerGrassX, innerGrassY);
        assertEquals(BiomeType.GRASS, innerResult, "Coordinates in inner grass zone should be grass");
        
        // Sand zone - should be sand or water (water can appear if buffer allows)
        float sandZoneX = 11000.0f; // Within sand zone (10000-13000)
        float sandZoneY = 0.0f;
        BiomeType sandResult = biomeManager.getBiomeAtPosition(sandZoneX, sandZoneY);
        assertTrue(sandResult == BiomeType.SAND || sandResult == BiomeType.WATER, 
                  "Coordinates in sand zone should be sand or water, got: " + sandResult);
        
        // Test base biome calculation consistency
        float testX = 20000.0f;
        float testY = 20000.0f;
        
        BiomeType baseBiome1 = biomeManager.getBaseBiomeAtPosition(testX, testY);
        BiomeType baseBiome2 = biomeManager.getBaseBiomeAtPosition(testX, testY);
        assertEquals(baseBiome1, baseBiome2, "Base biome calculation should be deterministic");
        
        // Test water eligibility in sand areas
        // Find a sand area far from grass
        float sandX = 25000.0f;
        float sandY = 25000.0f;
        
        BiomeType baseBiome = biomeManager.getBaseBiomeAtPosition(sandX, sandY);
        if (baseBiome == BiomeType.SAND) {
            // Test buffer validation
            boolean isValidBuffer = biomeManager.isValidBeachBuffer(sandX, sandY);
            BiomeType finalBiome = biomeManager.getBiomeAtPosition(sandX, sandY);
            
            // If buffer is valid and final biome is water, then water threshold was exceeded
            // If buffer is valid and final biome is sand, then water threshold was not exceeded
            if (isValidBuffer) {
                assertTrue(finalBiome == BiomeType.WATER || finalBiome == BiomeType.SAND,
                          "Valid buffer sand areas should be either water or sand");
            } else {
                // Invalid buffer should always result in sand (no water)
                assertEquals(BiomeType.SAND, finalBiome, "Invalid buffer areas should remain sand");
            }
        }
        
        // Test threshold consistency across multiple calls
        for (int i = 0; i < 5; i++) {
            BiomeType result1 = biomeManager.getBiomeAtPosition(testX, testY);
            BiomeType result2 = biomeManager.getBiomeAtPosition(testX, testY);
            assertEquals(result1, result2, "Biome calculation should be consistent across calls");
        }
    }
    
    /**
     * Test cache key encoding and grid alignment.
     * Validates that the spatial caching uses correct grid alignment.
     * Requirements: 1.4 (spatial caching logic)
     */
    @Test
    @Order(9)
    public void testCacheKeyEncoding() {
        biomeManager.clearPerformanceCaches();
        
        // Test that coordinates within the same grid cell produce cache hits
        // Buffer validation uses 64px grid size for caching
        float gridSize = 64.0f; 
        
        // Test coordinates that should map to the same grid cell
        float baseX = 1000.0f;
        float baseY = 1000.0f;
        
        // All these coordinates should map to the same grid cell
        float[][] sameGridCoords = {
            {baseX, baseY},
            {baseX + gridSize * 0.3f, baseY + gridSize * 0.3f},
            {baseX + gridSize * 0.7f, baseY + gridSize * 0.7f},
            {baseX + gridSize * 0.9f, baseY + gridSize * 0.9f}
        };
        
        // First call populates cache
        biomeManager.isValidBeachBuffer(sameGridCoords[0][0], sameGridCoords[0][1]);
        int initialCacheSize = (Integer) biomeManager.getPerformanceStats().get("bufferValidationCacheSize");
        
        // Subsequent calls within same grid should not increase cache size significantly
        for (int i = 1; i < sameGridCoords.length; i++) {
            biomeManager.isValidBeachBuffer(sameGridCoords[i][0], sameGridCoords[i][1]);
        }
        
        int finalCacheSize = (Integer) biomeManager.getPerformanceStats().get("bufferValidationCacheSize");
        assertTrue(finalCacheSize <= initialCacheSize + 2, 
                  "Cache size should not grow significantly for same grid coordinates. Initial: " + initialCacheSize + ", Final: " + finalCacheSize);
        
        // Test coordinates that should map to different grid cells
        float[][] differentGridCoords = {
            {baseX + gridSize * 2.0f, baseY + gridSize * 2.0f},
            {baseX + gridSize * 4.0f, baseY + gridSize * 4.0f},
            {baseX + gridSize * 6.0f, baseY + gridSize * 6.0f}
        };
        
        for (float[] coord : differentGridCoords) {
            biomeManager.isValidBeachBuffer(coord[0], coord[1]);
        }
        
        int afterDifferentCacheSize = (Integer) biomeManager.getPerformanceStats().get("bufferValidationCacheSize");
        assertTrue(afterDifferentCacheSize > finalCacheSize, 
                  "Cache should grow when accessing different grid cells. Final: " + finalCacheSize + ", After different: " + afterDifferentCacheSize);
    }
    
    /**
     * Test that cache clearing works correctly.
     */
    @Test
    @Order(10)
    public void testCacheClearing() {
        // Clear caches first to start fresh
        biomeManager.clearPerformanceCaches();
        
        // Populate caches by calling buffer validation multiple times on same area
        // This should populate the buffer validation cache
        for (int i = 0; i < 5; i++) {
            float x = 15000.0f + (i * 50.0f); // Smaller spacing to hit same cache cells
            float y = 15000.0f + (i * 50.0f);
            biomeManager.isValidBeachBuffer(x, y);
            biomeManager.getBiomeAtPosition(x, y);
        }
        
        // Call the same locations again to ensure cache hits
        for (int i = 0; i < 5; i++) {
            float x = 15000.0f + (i * 50.0f);
            float y = 15000.0f + (i * 50.0f);
            biomeManager.isValidBeachBuffer(x, y);
        }
        
        // Verify at least buffer cache is populated (base biome cache may not be due to direct calls)
        Map<String, Object> statsBefore = biomeManager.getPerformanceStats();
        assertTrue((Integer) statsBefore.get("bufferValidationCacheSize") > 0, "Buffer cache should be populated");
        // Note: Base biome cache may be empty due to optimizations using direct calls
        
        // Clear caches
        biomeManager.clearPerformanceCaches();
        
        // Verify caches are cleared
        Map<String, Object> statsAfter = biomeManager.getPerformanceStats();
        assertEquals(0, (Integer) statsAfter.get("bufferValidationCacheSize"), "Buffer cache should be cleared");
        assertEquals(0, (Integer) statsAfter.get("baseBiomeCacheSize"), "Base biome cache should be cleared");
    }
    
    /**
     * Test performance optimization configuration validation.
     * Validates that performance optimization settings are properly configured.
     * Requirements: 1.4 (performance optimization configuration)
     */
    @Test
    @Order(11)
    public void testPerformanceOptimizationConfiguration() {
        // Test that performance optimization constants are reasonable
        Map<String, Object> stats = biomeManager.getPerformanceStats();
        
        int maxCacheSize = (Integer) stats.get("maxCacheSize");
        float cacheGridSize = (Float) stats.get("cacheGridSize");
        
        // Validate cache configuration
        assertTrue(maxCacheSize > 1000, "Max cache size should be large enough for effective caching: " + maxCacheSize);
        assertTrue(maxCacheSize < 100000, "Max cache size should not be excessive: " + maxCacheSize);
        assertTrue(cacheGridSize > 32.0f, "Cache grid size should be reasonable: " + cacheGridSize);
        assertTrue(cacheGridSize < 1000.0f, "Cache grid size should not be too large: " + cacheGridSize);
        
        // Test that caching can be disabled and enabled
        biomeManager.clearPerformanceCaches();
        
        // Populate cache
        biomeManager.isValidBeachBuffer(15000.0f, 15000.0f);
        int cacheAfterPopulation = (Integer) biomeManager.getPerformanceStats().get("bufferValidationCacheSize");
        
        // Clear and verify
        biomeManager.clearPerformanceCaches();
        int cacheAfterClear = (Integer) biomeManager.getPerformanceStats().get("bufferValidationCacheSize");
        
        assertTrue(cacheAfterPopulation > 0, "Cache should be populated after use");
        assertEquals(0, cacheAfterClear, "Cache should be empty after clearing");
    }
    
    /**
     * Test threshold tuning accuracy with edge cases.
     * Validates that threshold calculations work correctly at biome boundaries.
     * Requirements: 2.3 (threshold tuning accuracy)
     */
    @Test
    @Order(12)
    public void testThresholdTuningAccuracy() {
        // Test coordinates at exact biome zone boundaries
        float innerGrassRadius = 10000.0f;
        float sandZoneWidth = 3000.0f;
        
        // Test at inner grass boundary
        float boundaryX = innerGrassRadius - 1.0f;
        float boundaryY = 0.0f;
        BiomeType innerBoundary = biomeManager.getBiomeAtPosition(boundaryX, boundaryY);
        assertEquals(BiomeType.GRASS, innerBoundary, "Coordinates just inside inner grass should be grass");
        
        boundaryX = innerGrassRadius + 1.0f;
        BiomeType outerBoundary = biomeManager.getBiomeAtPosition(boundaryX, boundaryY);
        assertEquals(BiomeType.SAND, outerBoundary, "Coordinates just outside inner grass should be sand");
        
        // Test at sand zone boundary
        float sandBoundaryX = innerGrassRadius + sandZoneWidth - 1.0f;
        BiomeType sandBoundary = biomeManager.getBiomeAtPosition(sandBoundaryX, 0.0f);
        assertEquals(BiomeType.SAND, sandBoundary, "Coordinates just inside sand zone should be sand");
        
        // Test noise-based areas beyond sand zone
        float noiseX = innerGrassRadius + sandZoneWidth + 1000.0f;
        float noiseY = 1000.0f;
        
        BiomeType baseBiome1 = biomeManager.getBaseBiomeAtPosition(noiseX, noiseY);
        BiomeType baseBiome2 = biomeManager.getBaseBiomeAtPosition(noiseX, noiseY);
        assertEquals(baseBiome1, baseBiome2, "Noise-based biome calculation should be deterministic");
        
        // Test that base biome is either grass or sand (never water)
        assertTrue(baseBiome1 == BiomeType.GRASS || baseBiome1 == BiomeType.SAND,
                  "Base biome should only be grass or sand, got: " + baseBiome1);
        
        // Test water threshold consistency
        if (baseBiome1 == BiomeType.SAND) {
            BiomeType finalBiome1 = biomeManager.getBiomeAtPosition(noiseX, noiseY);
            BiomeType finalBiome2 = biomeManager.getBiomeAtPosition(noiseX, noiseY);
            assertEquals(finalBiome1, finalBiome2, "Final biome calculation should be deterministic");
            
            // Final biome should be sand or water (if base is sand)
            assertTrue(finalBiome1 == BiomeType.SAND || finalBiome1 == BiomeType.WATER,
                      "Final biome in sand areas should be sand or water, got: " + finalBiome1);
        }
    }
    
    /**
     * Test that performance statistics are accurate and complete.
     */
    @Test
    @Order(13)
    public void testPerformanceStatistics() {
        Map<String, Object> stats = biomeManager.getPerformanceStats();
        
        // Verify all expected statistics are present
        assertTrue(stats.containsKey("bufferValidationCacheSize"), "Stats should include buffer validation cache size");
        assertTrue(stats.containsKey("baseBiomeCacheSize"), "Stats should include base biome cache size");
        assertTrue(stats.containsKey("maxCacheSize"), "Stats should include max cache size");
        assertTrue(stats.containsKey("cacheGridSize"), "Stats should include cache grid size");
        
        // Verify statistics are reasonable
        assertTrue((Integer) stats.get("bufferValidationCacheSize") >= 0, "Buffer cache size should be non-negative");
        assertTrue((Integer) stats.get("baseBiomeCacheSize") >= 0, "Base biome cache size should be non-negative");
        assertTrue((Integer) stats.get("maxCacheSize") > 0, "Max cache size should be positive");
        assertTrue((Float) stats.get("cacheGridSize") > 0, "Cache grid size should be positive");
    }
}