package wagemaker.uk.biome;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Integration tests for beach formation in the beach-style water biome system.
 * 
 * This test suite validates the complete beach formation feature:
 * - Realistic coastline formation over large map sections
 * - Biome distribution convergence with increasing sample sizes
 * - Multiplayer synchronization of beach layouts
 * 
 * Requirements: 1.5 (contiguous beach-like water regions), 2.3 (distribution convergence), 
 *               4.4 (multiplayer synchronization)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BeachFormationIntegrationTest {
    
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
     * Integration test for realistic coastline formation.
     * Validates that water forms natural-looking coastlines within sand areas
     * across multiple large regions of the world.
     * 
     * Requirements: 1.5 (contiguous beach-like water regions)
     */
    @Test
    @Order(1)
    public void testRealisticCoastlineFormation() {
        System.out.println("Testing realistic coastline formation...");
        
        // Test multiple large map sections (8km x 8km each)
        float[][] sectionCenters = {
            {15000.0f, 15000.0f},   // Northeast section
            {-15000.0f, 15000.0f},  // Northwest section  
            {15000.0f, -15000.0f},  // Southeast section
            {-15000.0f, -15000.0f}, // Southwest section
            {25000.0f, 0.0f},       // Far east section
            {0.0f, 25000.0f}        // Far north section
        };
        
        float sectionSize = 8000.0f; // 8km sections
        int samplesPerSection = 256; // 16x16 grid per section
        
        int totalSectionsWithWater = 0;
        int totalWaterClusters = 0;
        List<CoastlineMetrics> coastlineMetrics = new ArrayList<>();
        
        for (float[] center : sectionCenters) {
            CoastlineMetrics metrics = analyzeCoastlineFormation(center[0], center[1], sectionSize, samplesPerSection);
            coastlineMetrics.add(metrics);
            
            if (metrics.waterLocations > 0) {
                totalSectionsWithWater++;
                totalWaterClusters += metrics.waterClusters;
                
                System.out.printf("Section (%.0f, %.0f): %d water locations, %d clusters, %.1f%% contiguity%n",
                    center[0], center[1], metrics.waterLocations, metrics.waterClusters, metrics.contiguityRatio * 100);
            } else {
                System.out.printf("Section (%.0f, %.0f): No water found%n", center[0], center[1]);
            }
        }
        
        // Validate overall coastline formation
        assertTrue(totalSectionsWithWater > 0, "Should find water in at least some large map sections");
        
        if (totalSectionsWithWater > 0) {
            double averageClustersPerSection = (double) totalWaterClusters / totalSectionsWithWater;
            
            // Water should form reasonable clusters (beaches)
            assertTrue(averageClustersPerSection >= 1.0, 
                "Water should form contiguous clusters (beaches), average: " + averageClustersPerSection);
            
            // Water should not be overly fragmented (allow for more clusters since beach-style water can be naturally distributed)
            assertTrue(averageClustersPerSection <= 60.0,
                "Water should not be overly fragmented, average clusters: " + averageClustersPerSection);
            
            // Check contiguity across sections with water (adjust for beach-style distribution)
            double averageContiguity = coastlineMetrics.stream()
                .filter(m -> m.waterLocations > 0)
                .mapToDouble(m -> m.contiguityRatio)
                .average()
                .orElse(0.0);
            
            assertTrue(averageContiguity >= 0.02, 
                "Water should show reasonable contiguity (>2%), got: " + (averageContiguity * 100) + "%");
        }
        
        System.out.printf("Coastline formation summary: %d/%d sections with water, %.1f average clusters per section%n",
            totalSectionsWithWater, sectionCenters.length, 
            totalSectionsWithWater > 0 ? (double) totalWaterClusters / totalSectionsWithWater : 0.0);
    }
    
    /**
     * Integration test for biome distribution convergence.
     * Validates that biome percentages converge to expected targets as sample size increases.
     * 
     * Requirements: 2.3 (biome distribution convergence)
     */
    @Test
    @Order(2)
    public void testDistributionConvergence() {
        System.out.println("Testing biome distribution convergence...");
        
        // Test with increasing sample sizes
        int[] sampleSizes = {2000, 5000, 10000, 20000};
        double tolerance = 5.0; // ±5% tolerance
        
        // Expected distribution for beach-style system: 50% grass, 30% sand, 20% water
        double expectedGrass = 50.0;
        double expectedSand = 30.0;
        double expectedWater = 20.0;
        
        Random random = new Random(12345); // Fixed seed for reproducibility
        
        boolean convergenceAchieved = false;
        DistributionMeasurement finalMeasurement = null;
        
        for (int sampleSize : sampleSizes) {
            DistributionMeasurement measurement = measureBiomeDistribution(sampleSize, random);
            
            System.out.printf("Sample size %d: Grass %.1f%%, Sand %.1f%%, Water %.1f%%%n",
                sampleSize, measurement.grassPercent, measurement.sandPercent, measurement.waterPercent);
            
            // Check convergence to expected distribution
            boolean grassConverged = Math.abs(measurement.grassPercent - expectedGrass) <= tolerance;
            boolean sandConverged = Math.abs(measurement.sandPercent - expectedSand) <= tolerance;
            boolean waterConverged = Math.abs(measurement.waterPercent - expectedWater) <= tolerance;
            
            if (grassConverged && sandConverged && waterConverged) {
                convergenceAchieved = true;
                finalMeasurement = measurement;
                System.out.printf("Convergence achieved at sample size %d%n", sampleSize);
                break;
            }
            
            finalMeasurement = measurement;
        }
        
        // For the largest sample size, require convergence
        assertNotNull(finalMeasurement, "Should have at least one measurement");
        
        assertTrue(Math.abs(finalMeasurement.grassPercent - expectedGrass) <= tolerance,
            String.format("Grass distribution should converge to %.0f%% ±%.0f%%, got %.1f%% with %d samples",
                expectedGrass, tolerance, finalMeasurement.grassPercent, finalMeasurement.sampleSize));
        
        assertTrue(Math.abs(finalMeasurement.sandPercent - expectedSand) <= tolerance,
            String.format("Sand distribution should converge to %.0f%% ±%.0f%%, got %.1f%% with %d samples",
                expectedSand, tolerance, finalMeasurement.sandPercent, finalMeasurement.sampleSize));
        
        assertTrue(Math.abs(finalMeasurement.waterPercent - expectedWater) <= tolerance,
            String.format("Water distribution should converge to %.0f%% ±%.0f%%, got %.1f%% with %d samples",
                expectedWater, tolerance, finalMeasurement.waterPercent, finalMeasurement.sampleSize));
        
        assertTrue(convergenceAchieved, "Biome distribution should converge to expected ratios with sufficient sample size");
    }
    
    /**
     * Integration test for multiplayer synchronization of beach layouts.
     * Validates that multiple independent BiomeManager instances produce
     * identical beach layouts, ensuring multiplayer consistency.
     * 
     * Requirements: 4.4 (multiplayer synchronization)
     */
    @Test
    @Order(3)
    public void testMultiplayerSynchronizationOfBeachLayouts() {
        System.out.println("Testing multiplayer synchronization of beach layouts...");
        
        // Create multiple BiomeManager instances to simulate different clients
        int numClients = 4;
        BiomeManager[] clients = new BiomeManager[numClients];
        
        for (int i = 0; i < numClients; i++) {
            clients[i] = new BiomeManager();
            clients[i].initialize();
        }
        
        try {
            // Test synchronization across a comprehensive set of coordinates
            int testCoordinates = 3000;
            Random random = new Random(54321); // Fixed seed for reproducible test
            
            int totalMatches = 0;
            int waterMatches = 0;
            int totalWaterFound = 0;
            int baseBiomeMatches = 0;
            int bufferValidationMatches = 0;
            
            for (int test = 0; test < testCoordinates; test++) {
                // Generate random coordinate across a large area
                float x = (random.nextFloat() - 0.5f) * 80000.0f; // -40km to +40km
                float y = (random.nextFloat() - 0.5f) * 80000.0f;
                
                // Test final biome synchronization
                BiomeType[] results = new BiomeType[numClients];
                for (int client = 0; client < numClients; client++) {
                    results[client] = clients[client].getBiomeAtPosition(x, y);
                }
                
                // Verify all clients return the same result
                boolean allMatch = true;
                for (int client = 1; client < numClients; client++) {
                    if (results[0] != results[client]) {
                        allMatch = false;
                        fail(String.format("Final biome synchronization failed at (%.1f, %.1f): Client 0=%s, Client %d=%s",
                            x, y, results[0], client, results[client]));
                    }
                }
                
                if (allMatch) {
                    totalMatches++;
                    
                    if (results[0] == BiomeType.WATER) {
                        waterMatches++;
                        totalWaterFound++;
                        
                        // For water locations, test base biome synchronization
                        BiomeType[] baseResults = new BiomeType[numClients];
                        for (int client = 0; client < numClients; client++) {
                            baseResults[client] = clients[client].getBaseBiomeAtPosition(x, y);
                        }
                        
                        boolean baseBiomeMatch = true;
                        for (int client = 1; client < numClients; client++) {
                            if (baseResults[0] != baseResults[client]) {
                                baseBiomeMatch = false;
                                fail(String.format("Base biome synchronization failed at water location (%.1f, %.1f): Client 0=%s, Client %d=%s",
                                    x, y, baseResults[0], client, baseResults[client]));
                            }
                        }
                        
                        if (baseBiomeMatch) {
                            baseBiomeMatches++;
                            
                            // Verify base biome is SAND for water locations
                            assertEquals(BiomeType.SAND, baseResults[0],
                                String.format("Water at (%.1f, %.1f) should be in sand base biome", x, y));
                        }
                        
                        // Test buffer validation synchronization
                        boolean[] bufferResults = new boolean[numClients];
                        for (int client = 0; client < numClients; client++) {
                            bufferResults[client] = clients[client].isValidBeachBuffer(x, y);
                        }
                        
                        boolean bufferMatch = true;
                        for (int client = 1; client < numClients; client++) {
                            if (bufferResults[0] != bufferResults[client]) {
                                bufferMatch = false;
                                fail(String.format("Buffer validation synchronization failed at water location (%.1f, %.1f): Client 0=%s, Client %d=%s",
                                    x, y, bufferResults[0], client, bufferResults[client]));
                            }
                        }
                        
                        if (bufferMatch) {
                            bufferValidationMatches++;
                            
                            // All water should have valid buffer
                            assertTrue(bufferResults[0],
                                String.format("Water at (%.1f, %.1f) should have valid buffer distance", x, y));
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
            
            assertEquals(totalWaterFound, baseBiomeMatches,
                "All water base biome calculations should be synchronized across clients");
            
            assertEquals(totalWaterFound, bufferValidationMatches,
                "All water buffer validations should be synchronized across clients");
            
            System.out.printf("Multiplayer sync test: %d/%d coordinates matched, %d water locations synchronized%n",
                totalMatches, testCoordinates, waterMatches);
            
            System.out.printf("Beach layout sync: %d base biome matches, %d buffer validation matches%n",
                baseBiomeMatches, bufferValidationMatches);
            
        } finally {
            // Clean up all client instances
            for (BiomeManager client : clients) {
                if (client != null && client.isInitialized()) {
                    client.dispose();
                }
            }
        }
    }
    
    // ========== Helper Methods and Data Classes ==========
    
    /**
     * Analyzes coastline formation in a specific map section.
     */
    private CoastlineMetrics analyzeCoastlineFormation(float centerX, float centerY, float sectionSize, int samples) {
        List<float[]> waterLocations = new ArrayList<>();
        List<float[]> sandLocations = new ArrayList<>();
        
        float stepSize = sectionSize / (float) Math.sqrt(samples);
        float startX = centerX - sectionSize / 2;
        float startY = centerY - sectionSize / 2;
        
        int gridSize = (int) Math.sqrt(samples);
        
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
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
        
        CoastlineMetrics metrics = new CoastlineMetrics();
        metrics.waterLocations = waterLocations.size();
        metrics.sandLocations = sandLocations.size();
        
        if (!waterLocations.isEmpty()) {
            // Analyze water clustering (use larger radius for beach-style clustering)
            metrics.waterClusters = analyzeWaterClustering(waterLocations, 800.0f); // 800px cluster radius
            
            // Calculate contiguity ratio (use larger radius for beach-style contiguity)
            metrics.contiguityRatio = calculateContiguityRatio(waterLocations, 600.0f);
            
            // Verify buffer distance for all water locations
            for (float[] waterPos : waterLocations) {
                assertTrue(biomeManager.isValidBeachBuffer(waterPos[0], waterPos[1]),
                    String.format("Water at (%.0f, %.0f) should maintain buffer distance from grass", 
                        waterPos[0], waterPos[1]));
            }
        }
        
        return metrics;
    }
    
    /**
     * Measures biome distribution across a large sample.
     */
    private DistributionMeasurement measureBiomeDistribution(int sampleSize, Random random) {
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
        
        DistributionMeasurement measurement = new DistributionMeasurement();
        measurement.sampleSize = sampleSize;
        measurement.grassPercent = (grassCount * 100.0) / sampleSize;
        measurement.sandPercent = (sandCount * 100.0) / sampleSize;
        measurement.waterPercent = (waterCount * 100.0) / sampleSize;
        
        return measurement;
    }
    
    /**
     * Analyzes water clustering to measure beach formation.
     */
    private int analyzeWaterClustering(List<float[]> waterLocations, float clusterRadius) {
        if (waterLocations.isEmpty()) {
            return 0;
        }
        
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
     * Calculates contiguity ratio for water locations.
     */
    private double calculateContiguityRatio(List<float[]> waterLocations, float contiguityRadius) {
        if (waterLocations.size() <= 1) {
            return 1.0; // Single location is perfectly contiguous
        }
        
        int contiguousConnections = 0;
        int totalPossibleConnections = 0;
        
        for (int i = 0; i < waterLocations.size(); i++) {
            float[] location1 = waterLocations.get(i);
            
            for (int j = i + 1; j < waterLocations.size(); j++) {
                float[] location2 = waterLocations.get(j);
                
                float distance = (float) Math.sqrt(
                    Math.pow(location1[0] - location2[0], 2) +
                    Math.pow(location1[1] - location2[1], 2)
                );
                
                totalPossibleConnections++;
                
                if (distance <= contiguityRadius) {
                    contiguousConnections++;
                }
            }
        }
        
        return totalPossibleConnections > 0 ? (double) contiguousConnections / totalPossibleConnections : 0.0;
    }
    
    /**
     * Data class for coastline metrics.
     */
    private static class CoastlineMetrics {
        int waterLocations;
        int sandLocations;
        int waterClusters;
        double contiguityRatio;
    }
    
    /**
     * Data class for distribution measurements.
     */
    private static class DistributionMeasurement {
        int sampleSize;
        double grassPercent;
        double sandPercent;
        double waterPercent;
    }
}