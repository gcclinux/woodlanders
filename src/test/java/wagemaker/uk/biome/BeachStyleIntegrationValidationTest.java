package wagemaker.uk.biome;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Comprehensive integration tests for the beach-style water biome system.
 * 
 * This test suite validates the complete beach-style water biome feature across
 * multiple systems and scenarios:
 * - Beach formation over large map sections
 * - Biome distribution convergence with increasing sample sizes
 * - Multiplayer client synchronization
 * - System performance under typical gameplay loads
 * 
 * Requirements: 1.5 (beach formation), 2.3 (distribution convergence), 4.4 (multiplayer sync)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BeachStyleIntegrationValidationTest {
    
    private BiomeManager biomeManager;
    
    @BeforeEach
    public void setUp() {
        biomeManager = new BiomeManager();
        biomeManager.initialize();
    }
    
    @AfterEach
    public void tearDown() {
        if (biomeManager != null && biomeManager.isInitialized()) {
            biomeManager.dispose();
        }
    }
    
    /**
     * Test beach formation over large map sections.
     * Validates that water forms realistic coastlines within sand areas across
     * multiple large regions of the world.
     * 
     * Requirements: 1.5 (contiguous beach-like water regions)
     */
    @Test
    @Order(1)
    public void testBeachFormationOverLargeMapSections() {
        System.out.println("Testing beach formation over large map sections...");
        
        // Test multiple large map sections (10km x 10km each)
        float[][] sectionCenters = {
            {20000.0f, 20000.0f},   // Northeast section
            {-20000.0f, 20000.0f},  // Northwest section  
            {20000.0f, -20000.0f},  // Southeast section
            {-20000.0f, -20000.0f}, // Southwest section
            {30000.0f, 0.0f},       // Far east section
            {0.0f, 30000.0f}        // Far north section
        };
        
        float sectionSize = 10000.0f; // 10km sections
        int samplesPerSection = 400; // 20x20 grid per section
        
        int totalSections = 0;
        int sectionsWithWater = 0;
        int totalWaterClusters = 0;
        
        for (float[] center : sectionCenters) {
            totalSections++;
            
            // Sample the section in a grid pattern
            List<float[]> waterLocations = new ArrayList<>();
            List<float[]> sandLocations = new ArrayList<>();
            
            float stepSize = sectionSize / (float) Math.sqrt(samplesPerSection);
            float startX = center[0] - sectionSize / 2;
            float startY = center[1] - sectionSize / 2;
            
            for (int i = 0; i < Math.sqrt(samplesPerSection); i++) {
                for (int j = 0; j < Math.sqrt(samplesPerSection); j++) {
                    float x = startX + i * stepSize;
                    float y = startY + j * stepSize;
                    
                    BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
                    BiomeType baseBiome = biomeManager.getBaseBiomeAtPosition(x, y);
                    
                    if (biome == BiomeType.WATER) {
                        waterLocations.add(new float[]{x, y});
                        // Verify water only appears in sand base areas
                        assertEquals(BiomeType.SAND, baseBiome,
                            String.format("Water at (%.0f, %.0f) should only appear in sand base areas", x, y));
                    } else if (biome == BiomeType.SAND) {
                        sandLocations.add(new float[]{x, y});
                    }
                }
            }
            
            if (!waterLocations.isEmpty()) {
                sectionsWithWater++;
                
                // Analyze water clustering (contiguity)
                int clusters = analyzeWaterClustering(waterLocations, 500.0f); // 500px cluster radius
                totalWaterClusters += clusters;
                
                // Verify buffer distance from grass for all water locations
                for (float[] waterPos : waterLocations) {
                    assertTrue(biomeManager.isValidBeachBuffer(waterPos[0], waterPos[1]),
                        String.format("Water at (%.0f, %.0f) should maintain buffer distance from grass", 
                            waterPos[0], waterPos[1]));
                }
                
                System.out.printf("Section (%.0f, %.0f): %d water locations, %d clusters, %d sand locations%n",
                    center[0], center[1], waterLocations.size(), clusters, sandLocations.size());
            } else {
                System.out.printf("Section (%.0f, %.0f): No water found, %d sand locations%n",
                    center[0], center[1], sandLocations.size());
            }
        }
        
        // Validate overall beach formation
        assertTrue(sectionsWithWater > 0, "Should find water in at least some large map sections");
        
        if (sectionsWithWater > 0) {
            double averageClustersPerSection = (double) totalWaterClusters / sectionsWithWater;
            assertTrue(averageClustersPerSection >= 1.0, 
                "Water should form contiguous clusters (beaches), average: " + averageClustersPerSection);
            
            // Water should not be too fragmented (reasonable cluster count)
            // Allow for more clusters since beach-style water can be naturally distributed
            assertTrue(averageClustersPerSection <= 15.0,
                "Water should not be overly fragmented, average clusters: " + averageClustersPerSection);
        }
        
        System.out.printf("Beach formation summary: %d/%d sections with water, %.1f average clusters per section%n",
            sectionsWithWater, totalSections, sectionsWithWater > 0 ? (double) totalWaterClusters / sectionsWithWater : 0.0);
    }
    
    /**
     * Test biome distribution convergence with increasing sample sizes.
     * Validates that biome percentages converge to expected targets as sample size increases.
     * 
     * Requirements: 2.3 (biome distribution convergence)
     */
    @Test
    @Order(2)
    public void testBiomeDistributionConvergenceWithIncreasingSampleSizes() {
        System.out.println("Testing biome distribution convergence with increasing sample sizes...");
        
        // Test with increasing sample sizes
        int[] sampleSizes = {1000, 2500, 5000, 10000, 20000};
        double tolerance = 5.0; // ±5% tolerance
        
        // Expected distribution for beach-style system: 50% grass, 30% sand, 20% water
        double expectedGrass = 50.0;
        double expectedSand = 30.0;
        double expectedWater = 20.0;
        
        Random random = new Random(12345); // Fixed seed for reproducibility
        
        boolean convergenceAchieved = false;
        
        for (int sampleSize : sampleSizes) {
            int grassCount = 0;
            int sandCount = 0;
            int waterCount = 0;
            
            // Sample coordinates from a large area beyond spawn zones
            for (int i = 0; i < sampleSize; i++) {
                // Sample from -30km to +30km area (avoiding spawn zones)
                float x = (random.nextFloat() - 0.5f) * 60000.0f;
                float y = (random.nextFloat() - 0.5f) * 60000.0f;
                
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
            
            // Calculate percentages
            double grassPercent = (grassCount * 100.0) / sampleSize;
            double sandPercent = (sandCount * 100.0) / sampleSize;
            double waterPercent = (waterCount * 100.0) / sampleSize;
            
            System.out.printf("Sample size %d: Grass %.1f%%, Sand %.1f%%, Water %.1f%%%n",
                sampleSize, grassPercent, sandPercent, waterPercent);
            
            // Check convergence to expected distribution
            boolean grassConverged = Math.abs(grassPercent - expectedGrass) <= tolerance;
            boolean sandConverged = Math.abs(sandPercent - expectedSand) <= tolerance;
            boolean waterConverged = Math.abs(waterPercent - expectedWater) <= tolerance;
            
            if (grassConverged && sandConverged && waterConverged) {
                convergenceAchieved = true;
                System.out.printf("Convergence achieved at sample size %d%n", sampleSize);
                break;
            }
            
            // For largest sample size, require convergence
            if (sampleSize == sampleSizes[sampleSizes.length - 1]) {
                assertTrue(grassConverged,
                    String.format("Grass distribution should converge to %.0f%% ±%.0f%%, got %.1f%% with %d samples",
                        expectedGrass, tolerance, grassPercent, sampleSize));
                
                assertTrue(sandConverged,
                    String.format("Sand distribution should converge to %.0f%% ±%.0f%%, got %.1f%% with %d samples",
                        expectedSand, tolerance, sandPercent, sampleSize));
                
                assertTrue(waterConverged,
                    String.format("Water distribution should converge to %.0f%% ±%.0f%%, got %.1f%% with %d samples",
                        expectedWater, tolerance, waterPercent, sampleSize));
            }
        }
        
        assertTrue(convergenceAchieved, "Biome distribution should converge to expected ratios with sufficient sample size");
    }
    
    /**
     * Test that multiplayer clients see identical beach layouts.
     * Validates deterministic biome calculation across multiple independent BiomeManager instances.
     * 
     * Requirements: 4.4 (multiplayer synchronization)
     */
    @Test
    @Order(3)
    public void testMultiplayerClientsIdenticalBeachLayouts() {
        System.out.println("Testing multiplayer client synchronization...");
        
        // Create multiple BiomeManager instances to simulate different clients
        int numClients = 4;
        BiomeManager[] clients = new BiomeManager[numClients];
        
        for (int i = 0; i < numClients; i++) {
            clients[i] = new BiomeManager();
            clients[i].initialize();
        }
        
        try {
            // Test synchronization across a large number of coordinates
            int testCoordinates = 2000;
            Random random = new Random(54321); // Fixed seed for reproducible test
            
            int totalMatches = 0;
            int waterMatches = 0;
            int totalWaterFound = 0;
            
            for (int test = 0; test < testCoordinates; test++) {
                // Generate random coordinate across a large area
                float x = (random.nextFloat() - 0.5f) * 80000.0f; // -40km to +40km
                float y = (random.nextFloat() - 0.5f) * 80000.0f;
                
                // Get biome from all clients
                BiomeType[] results = new BiomeType[numClients];
                for (int client = 0; client < numClients; client++) {
                    results[client] = clients[client].getBiomeAtPosition(x, y);
                }
                
                // Verify all clients return the same result
                boolean allMatch = true;
                for (int client = 1; client < numClients; client++) {
                    if (results[0] != results[client]) {
                        allMatch = false;
                        fail(String.format("Client synchronization failed at (%.1f, %.1f): Client 0=%s, Client %d=%s",
                            x, y, results[0], client, results[client]));
                    }
                }
                
                if (allMatch) {
                    totalMatches++;
                    
                    if (results[0] == BiomeType.WATER) {
                        waterMatches++;
                        totalWaterFound++;
                        
                        // For water locations, also verify buffer validation synchronization
                        for (int client = 0; client < numClients; client++) {
                            boolean bufferValid = clients[client].isValidBeachBuffer(x, y);
                            assertTrue(bufferValid, 
                                String.format("Water at (%.1f, %.1f) should have valid buffer on all clients", x, y));
                        }
                    }
                }
            }
            
            // Verify perfect synchronization
            assertEquals(testCoordinates, totalMatches, 
                "All coordinates should match across all clients");
            
            // Verify we found some water locations to test
            assertTrue(totalWaterFound > 0, 
                "Should find some water locations to validate synchronization");
            
            assertEquals(totalWaterFound, waterMatches,
                "All water locations should be synchronized across clients");
            
            System.out.printf("Multiplayer sync test: %d/%d coordinates matched, %d water locations synchronized%n",
                totalMatches, testCoordinates, waterMatches);
            
            // Test base biome synchronization specifically
            int baseBiomeTests = 500;
            int baseBiomeMatches = 0;
            
            for (int test = 0; test < baseBiomeTests; test++) {
                float x = (random.nextFloat() - 0.5f) * 60000.0f;
                float y = (random.nextFloat() - 0.5f) * 60000.0f;
                
                BiomeType[] baseResults = new BiomeType[numClients];
                for (int client = 0; client < numClients; client++) {
                    baseResults[client] = clients[client].getBaseBiomeAtPosition(x, y);
                }
                
                // Verify all clients return the same base biome
                boolean baseMatch = true;
                for (int client = 1; client < numClients; client++) {
                    if (baseResults[0] != baseResults[client]) {
                        baseMatch = false;
                        fail(String.format("Base biome synchronization failed at (%.1f, %.1f): Client 0=%s, Client %d=%s",
                            x, y, baseResults[0], client, baseResults[client]));
                    }
                }
                
                if (baseMatch) {
                    baseBiomeMatches++;
                }
            }
            
            assertEquals(baseBiomeTests, baseBiomeMatches,
                "All base biome calculations should be synchronized across clients");
            
            System.out.printf("Base biome sync test: %d/%d base biome calculations matched%n",
                baseBiomeMatches, baseBiomeTests);
            
        } finally {
            // Clean up all client instances
            for (BiomeManager client : clients) {
                if (client != null && client.isInitialized()) {
                    client.dispose();
                }
            }
        }
    }
    
    /**
     * Test system performance under typical gameplay loads.
     * Validates that the beach-style biome system performs adequately under realistic usage patterns.
     * 
     * Requirements: Performance under typical gameplay loads
     */
    @Test
    @Order(4)
    public void testSystemPerformanceUnderTypicalGameplayLoads() {
        System.out.println("Testing system performance under typical gameplay loads...");
        
        // Simulate typical gameplay scenarios
        
        // Scenario 1: Player movement (frequent biome queries in small area)
        testPlayerMovementPerformance();
        
        // Scenario 2: World generation (batch biome queries over large area)
        testWorldGenerationPerformance();
        
        // Scenario 3: Multiplayer synchronization (concurrent access)
        testMultiplayerConcurrentPerformance();
        
        // Scenario 4: Resource spawning (scattered biome queries with validation)
        testResourceSpawningPerformance();
        
        System.out.println("All performance tests completed successfully");
    }
    
    /**
     * Test performance during player movement simulation.
     * Simulates a player moving through different biome areas.
     */
    private void testPlayerMovementPerformance() {
        System.out.println("  Testing player movement performance...");
        
        // Simulate player moving through world at 5 blocks/second for 60 seconds
        int movementQueries = 300; // 5 queries per second * 60 seconds
        float playerSpeed = 320.0f; // 5 blocks * 64 pixels per block
        
        long startTime = System.nanoTime();
        
        // Start player at spawn and move in expanding spiral
        float x = 0.0f;
        float y = 0.0f;
        float angle = 0.0f;
        float radius = 0.0f;
        
        for (int i = 0; i < movementQueries; i++) {
            // Update player position (spiral movement)
            angle += 0.1f;
            radius += 2.0f;
            x = radius * (float) Math.cos(angle);
            y = radius * (float) Math.sin(angle);
            
            // Query biome at player position (typical gameplay query)
            BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
            assertNotNull(biome, "Biome query should always return a valid result");
        }
        
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double averageTimeMs = (totalTime / 1_000_000.0) / movementQueries;
        double queriesPerSecond = (movementQueries * 1_000_000_000.0) / totalTime;
        
        // Performance requirements for player movement
        assertTrue(averageTimeMs < 1.0, 
            "Player movement queries should average < 1ms, got: " + String.format("%.3f", averageTimeMs) + "ms");
        
        assertTrue(queriesPerSecond > 1000, 
            "Should handle > 1000 queries/second for player movement, got: " + String.format("%.0f", queriesPerSecond));
        
        System.out.printf("    Player movement: %.3f ms average, %.0f queries/second%n", 
            averageTimeMs, queriesPerSecond);
    }
    
    /**
     * Test performance during world generation simulation.
     * Simulates generating biome data for large world chunks.
     */
    private void testWorldGenerationPerformance() {
        System.out.println("  Testing world generation performance...");
        
        // Simulate generating a 1000x1000 pixel world chunk (typical chunk size)
        int chunkSize = 1000;
        int sampleResolution = 50; // Sample every 50 pixels
        int totalQueries = (chunkSize / sampleResolution) * (chunkSize / sampleResolution);
        
        long startTime = System.nanoTime();
        
        // Generate biome data for chunk centered at (20000, 20000)
        float chunkCenterX = 20000.0f;
        float chunkCenterY = 20000.0f;
        float chunkStartX = chunkCenterX - chunkSize / 2.0f;
        float chunkStartY = chunkCenterY - chunkSize / 2.0f;
        
        int grassCount = 0, sandCount = 0, waterCount = 0;
        
        for (int i = 0; i < chunkSize; i += sampleResolution) {
            for (int j = 0; j < chunkSize; j += sampleResolution) {
                float x = chunkStartX + i;
                float y = chunkStartY + j;
                
                BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
                
                switch (biome) {
                    case GRASS: grassCount++; break;
                    case SAND: sandCount++; break;
                    case WATER: waterCount++; break;
                }
            }
        }
        
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double averageTimeMs = (totalTime / 1_000_000.0) / totalQueries;
        double queriesPerSecond = (totalQueries * 1_000_000_000.0) / totalTime;
        
        // Performance requirements for world generation
        assertTrue(averageTimeMs < 0.5, 
            "World generation queries should average < 0.5ms, got: " + String.format("%.3f", averageTimeMs) + "ms");
        
        assertTrue(queriesPerSecond > 2000, 
            "Should handle > 2000 queries/second for world generation, got: " + String.format("%.0f", queriesPerSecond));
        
        System.out.printf("    World generation: %.3f ms average, %.0f queries/second (%d grass, %d sand, %d water)%n", 
            averageTimeMs, queriesPerSecond, grassCount, sandCount, waterCount);
    }
    
    /**
     * Test performance under concurrent multiplayer access.
     * Simulates multiple players querying biomes simultaneously.
     */
    private void testMultiplayerConcurrentPerformance() {
        System.out.println("  Testing multiplayer concurrent performance...");
        
        int numThreads = 4; // Simulate 4 concurrent players
        int queriesPerThread = 500;
        
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);
        
        long[] threadTimes = new long[numThreads];
        
        for (int thread = 0; thread < numThreads; thread++) {
            final int threadId = thread;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    
                    long threadStartTime = System.nanoTime();
                    Random random = new Random(threadId * 1000); // Different seed per thread
                    
                    for (int query = 0; query < queriesPerThread; query++) {
                        // Each thread queries different area of world
                        float baseX = threadId * 10000.0f;
                        float baseY = threadId * 10000.0f;
                        float x = baseX + (random.nextFloat() - 0.5f) * 5000.0f;
                        float y = baseY + (random.nextFloat() - 0.5f) * 5000.0f;
                        
                        BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
                        assertNotNull(biome, "Concurrent biome query should return valid result");
                    }
                    
                    threadTimes[threadId] = System.nanoTime() - threadStartTime;
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        // Start all threads simultaneously
        long overallStartTime = System.nanoTime();
        startLatch.countDown();
        
        try {
            // Wait for all threads to complete
            assertTrue(endLatch.await(10, TimeUnit.SECONDS), 
                "Concurrent performance test should complete within 10 seconds");
            
            long overallEndTime = System.nanoTime();
            long overallTime = overallEndTime - overallStartTime;
            
            // Calculate performance metrics
            double overallTimeMs = overallTime / 1_000_000.0;
            double totalQueries = numThreads * queriesPerThread;
            double overallQueriesPerSecond = (totalQueries * 1_000_000_000.0) / overallTime;
            
            // Performance requirements for concurrent access
            assertTrue(overallTimeMs < 5000, 
                "Concurrent test should complete in < 5 seconds, took: " + String.format("%.0f", overallTimeMs) + "ms");
            
            assertTrue(overallQueriesPerSecond > 500, 
                "Should handle > 500 concurrent queries/second, got: " + String.format("%.0f", overallQueriesPerSecond));
            
            // Check individual thread performance
            for (int i = 0; i < numThreads; i++) {
                double threadTimeMs = threadTimes[i] / 1_000_000.0;
                double threadQueriesPerSecond = (queriesPerThread * 1_000_000_000.0) / threadTimes[i];
                
                assertTrue(threadTimeMs < 3000, 
                    "Thread " + i + " should complete in < 3 seconds, took: " + String.format("%.0f", threadTimeMs) + "ms");
            }
            
            System.out.printf("    Concurrent access: %.0f ms total, %.0f queries/second across %d threads%n", 
                overallTimeMs, overallQueriesPerSecond, numThreads);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            fail("Concurrent performance test was interrupted");
        } finally {
            executor.shutdown();
        }
    }
    
    /**
     * Test performance during resource spawning simulation.
     * Simulates the resource spawning system checking biome validity.
     */
    private void testResourceSpawningPerformance() {
        System.out.println("  Testing resource spawning performance...");
        
        // Simulate resource spawning system checking 1000 potential spawn locations
        int spawnAttempts = 1000;
        Random random = new Random(98765);
        
        long startTime = System.nanoTime();
        
        int validSpawns = 0;
        int waterRejections = 0;
        int bufferValidations = 0;
        
        for (int attempt = 0; attempt < spawnAttempts; attempt++) {
            // Generate random spawn location
            float x = (random.nextFloat() - 0.5f) * 50000.0f;
            float y = (random.nextFloat() - 0.5f) * 50000.0f;
            
            // Check biome type (typical resource spawning validation)
            BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
            
            if (biome == BiomeType.WATER) {
                waterRejections++;
            } else {
                validSpawns++;
                
                // For sand areas, also test buffer validation (more expensive operation)
                if (biome == BiomeType.SAND) {
                    boolean validBuffer = biomeManager.isValidBeachBuffer(x, y);
                    bufferValidations++;
                    // Buffer validation result doesn't affect spawn validity for resources
                }
            }
        }
        
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        double averageTimeMs = (totalTime / 1_000_000.0) / spawnAttempts;
        double attemptsPerSecond = (spawnAttempts * 1_000_000_000.0) / totalTime;
        
        // Performance requirements for resource spawning
        assertTrue(averageTimeMs < 2.0, 
            "Resource spawn validation should average < 2ms, got: " + String.format("%.3f", averageTimeMs) + "ms");
        
        assertTrue(attemptsPerSecond > 500, 
            "Should handle > 500 spawn validations/second, got: " + String.format("%.0f", attemptsPerSecond));
        
        // Verify we tested both scenarios
        assertTrue(validSpawns > 0, "Should find some valid spawn locations");
        assertTrue(waterRejections > 0, "Should encounter some water areas for rejection testing");
        
        System.out.printf("    Resource spawning: %.3f ms average, %.0f validations/second (%d valid, %d water rejected, %d buffer checks)%n", 
            averageTimeMs, attemptsPerSecond, validSpawns, waterRejections, bufferValidations);
    }
    
    /**
     * Helper method to analyze water clustering for beach formation validation.
     * Groups nearby water locations into clusters to measure contiguity.
     * 
     * @param waterLocations List of water coordinate pairs
     * @param clusterRadius Maximum distance between locations in the same cluster
     * @return Number of distinct water clusters found
     */
    private int analyzeWaterClustering(List<float[]> waterLocations, float clusterRadius) {
        if (waterLocations.isEmpty()) {
            return 0;
        }
        
        // Use simple clustering algorithm
        List<List<float[]>> clusters = new ArrayList<>();
        
        for (float[] location : waterLocations) {
            boolean addedToCluster = false;
            
            // Try to add to existing cluster
            for (List<float[]> cluster : clusters) {
                // Check if location is within cluster radius of any location in cluster
                for (float[] clusterLocation : cluster) {
                    float distance = (float) Math.sqrt(
                        Math.pow(location[0] - clusterLocation[0], 2) +
                        Math.pow(location[1] - clusterLocation[1], 2)
                    );
                    
                    if (distance <= clusterRadius) {
                        cluster.add(location);
                        addedToCluster = true;
                        break;
                    }
                }
                
                if (addedToCluster) {
                    break;
                }
            }
            
            // Create new cluster if not added to existing one
            if (!addedToCluster) {
                List<float[]> newCluster = new ArrayList<>();
                newCluster.add(location);
                clusters.add(newCluster);
            }
        }
        
        return clusters.size();
    }
    
    /**
     * Test comprehensive beach-style system integration.
     * Validates that all components work together correctly across multiple scenarios.
     */
    @Test
    @Order(5)
    public void testComprehensiveBeachStyleSystemIntegration() {
        System.out.println("Testing comprehensive beach-style system integration...");
        
        // Test 1: Verify water only appears in sand base areas across large sample
        testWaterOnlyInSandIntegration();
        
        // Test 2: Verify buffer distance maintenance across different regions
        testBufferDistanceIntegration();
        
        // Test 3: Verify biome distribution consistency across multiple measurements
        testDistributionConsistencyIntegration();
        
        // Test 4: Verify system determinism across multiple sessions
        testSystemDeterminismIntegration();
        
        System.out.println("Comprehensive integration test completed successfully");
    }
    
    private void testWaterOnlyInSandIntegration() {
        System.out.println("  Testing water-only-in-sand integration...");
        
        Random random = new Random(11111);
        int testSamples = 2000;
        int waterFound = 0;
        int waterInSandCount = 0;
        
        for (int i = 0; i < testSamples; i++) {
            float x = (random.nextFloat() - 0.5f) * 60000.0f;
            float y = (random.nextFloat() - 0.5f) * 60000.0f;
            
            BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
            BiomeType baseBiome = biomeManager.getBaseBiomeAtPosition(x, y);
            
            if (biome == BiomeType.WATER) {
                waterFound++;
                assertEquals(BiomeType.SAND, baseBiome,
                    String.format("Water at (%.0f, %.0f) must be in sand base area", x, y));
                waterInSandCount++;
            }
        }
        
        assertTrue(waterFound > 0, "Should find water locations in integration test");
        assertEquals(waterFound, waterInSandCount, "All water should be in sand base areas");
        
        System.out.printf("    Water-in-sand: %d/%d water locations verified in sand base areas%n", 
            waterInSandCount, waterFound);
    }
    
    private void testBufferDistanceIntegration() {
        System.out.println("  Testing buffer distance integration...");
        
        Random random = new Random(22222);
        int testSamples = 1000;
        int waterFound = 0;
        int bufferValidCount = 0;
        
        for (int i = 0; i < testSamples; i++) {
            float x = (random.nextFloat() - 0.5f) * 50000.0f;
            float y = (random.nextFloat() - 0.5f) * 50000.0f;
            
            BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
            
            if (biome == BiomeType.WATER) {
                waterFound++;
                boolean validBuffer = biomeManager.isValidBeachBuffer(x, y);
                assertTrue(validBuffer, 
                    String.format("Water at (%.0f, %.0f) must maintain buffer distance", x, y));
                bufferValidCount++;
            }
        }
        
        assertTrue(waterFound > 0, "Should find water locations for buffer testing");
        assertEquals(waterFound, bufferValidCount, "All water should maintain buffer distance");
        
        System.out.printf("    Buffer distance: %d/%d water locations maintain proper buffer%n", 
            bufferValidCount, waterFound);
    }
    
    private void testDistributionConsistencyIntegration() {
        System.out.println("  Testing distribution consistency integration...");
        
        // Measure distribution multiple times with same parameters
        int measurements = 3;
        int sampleSize = 5000;
        double tolerance = 2.0; // Tighter tolerance for consistency
        
        double[] grassPercentages = new double[measurements];
        double[] sandPercentages = new double[measurements];
        double[] waterPercentages = new double[measurements];
        
        for (int measurement = 0; measurement < measurements; measurement++) {
            Map<String, Double> distribution = biomeManager.measureBiomeDistribution(sampleSize, 40000.0f);
            grassPercentages[measurement] = distribution.get("grass");
            sandPercentages[measurement] = distribution.get("sand");
            waterPercentages[measurement] = distribution.get("water");
        }
        
        // Check consistency across measurements
        for (int i = 1; i < measurements; i++) {
            assertTrue(Math.abs(grassPercentages[0] - grassPercentages[i]) <= tolerance,
                String.format("Grass distribution should be consistent: %.1f%% vs %.1f%%", 
                    grassPercentages[0], grassPercentages[i]));
            
            assertTrue(Math.abs(sandPercentages[0] - sandPercentages[i]) <= tolerance,
                String.format("Sand distribution should be consistent: %.1f%% vs %.1f%%", 
                    sandPercentages[0], sandPercentages[i]));
            
            assertTrue(Math.abs(waterPercentages[0] - waterPercentages[i]) <= tolerance,
                String.format("Water distribution should be consistent: %.1f%% vs %.1f%%", 
                    waterPercentages[0], waterPercentages[i]));
        }
        
        System.out.printf("    Distribution consistency: Grass %.1f±%.1f%%, Sand %.1f±%.1f%%, Water %.1f±%.1f%%%n",
            grassPercentages[0], Math.abs(grassPercentages[0] - grassPercentages[1]),
            sandPercentages[0], Math.abs(sandPercentages[0] - sandPercentages[1]),
            waterPercentages[0], Math.abs(waterPercentages[0] - waterPercentages[1]));
    }
    
    private void testSystemDeterminismIntegration() {
        System.out.println("  Testing system determinism integration...");
        
        // Create multiple BiomeManager instances and verify identical results
        BiomeManager manager1 = new BiomeManager();
        BiomeManager manager2 = new BiomeManager();
        
        manager1.initialize();
        manager2.initialize();
        
        try {
            Random random = new Random(33333);
            int testCoordinates = 1000;
            int matches = 0;
            
            for (int i = 0; i < testCoordinates; i++) {
                float x = (random.nextFloat() - 0.5f) * 80000.0f;
                float y = (random.nextFloat() - 0.5f) * 80000.0f;
                
                BiomeType result1 = manager1.getBiomeAtPosition(x, y);
                BiomeType result2 = manager2.getBiomeAtPosition(x, y);
                
                assertEquals(result1, result2, 
                    String.format("Determinism failed at (%.0f, %.0f): %s vs %s", x, y, result1, result2));
                matches++;
            }
            
            assertEquals(testCoordinates, matches, "All coordinates should produce identical results");
            
            System.out.printf("    System determinism: %d/%d coordinates matched across instances%n", 
                matches, testCoordinates);
            
        } finally {
            manager1.dispose();
            manager2.dispose();
        }
    }
}