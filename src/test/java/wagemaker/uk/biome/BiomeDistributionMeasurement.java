package wagemaker.uk.biome;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Measurement tool for biome distribution thresholds.
 * This test measures the actual distribution of biomes across a large sample
 * of world coordinates to verify that threshold values produce the desired
 * distribution (50% grass, 35% sand, 15% water).
 * 
 * This is not a pass/fail test, but a measurement tool to guide threshold tuning.
 * 
 * Requirements: 1.4 - Allocate biome distribution as 50% grass, 35% sand, and 15% water
 */
public class BiomeDistributionMeasurement {
    
    private BiomeManager biomeManager;
    
    @BeforeEach
    public void setUp() {
        biomeManager = new BiomeManager();
        biomeManager.initialize();
    }
    
    /**
     * Measures biome distribution across a large sample of random world coordinates.
     * Samples 50,000 coordinates spread across a large area to get accurate distribution.
     */
    @Test
    public void measureBiomeDistribution() throws Exception {
        int sampleSize = 50000;
        Map<BiomeType, Integer> counts = new HashMap<>();
        counts.put(BiomeType.GRASS, 0);
        counts.put(BiomeType.SAND, 0);
        counts.put(BiomeType.WATER, 0);
        
        Random random = new Random(12345); // Fixed seed for reproducibility
        
        // Sample coordinates in a large area (20000x20000 pixels)
        // This covers spawn area, sand zones, and far areas
        float minCoord = -10000;
        float maxCoord = 10000;
        
        System.out.println("\n=== BIOME DISTRIBUTION MEASUREMENT ===");
        System.out.println("Sample size: " + sampleSize);
        System.out.println("Sample area: " + minCoord + " to " + maxCoord + " (both X and Y)");
        System.out.println("Current thresholds:");
        System.out.println("  - WATER_NOISE_THRESHOLD: " + BiomeConfig.WATER_NOISE_THRESHOLD);
        System.out.println("  - Sand threshold (hardcoded): 0.50");
        System.out.println();
        
        // Sample random coordinates
        for (int i = 0; i < sampleSize; i++) {
            float x = minCoord + random.nextFloat() * (maxCoord - minCoord);
            float y = minCoord + random.nextFloat() * (maxCoord - minCoord);
            
            BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
            counts.put(biome, counts.get(biome) + 1);
        }
        
        // Calculate percentages
        double grassPercent = (counts.get(BiomeType.GRASS) * 100.0) / sampleSize;
        double sandPercent = (counts.get(BiomeType.SAND) * 100.0) / sampleSize;
        double waterPercent = (counts.get(BiomeType.WATER) * 100.0) / sampleSize;
        
        System.out.println("=== RESULTS ===");
        System.out.println("Grass: " + counts.get(BiomeType.GRASS) + " (" + String.format("%.2f", grassPercent) + "%)");
        System.out.println("Sand:  " + counts.get(BiomeType.SAND) + " (" + String.format("%.2f", sandPercent) + "%)");
        System.out.println("Water: " + counts.get(BiomeType.WATER) + " (" + String.format("%.2f", waterPercent) + "%)");
        System.out.println();
        
        System.out.println("=== TARGET DISTRIBUTION ===");
        System.out.println("Grass: 50% (±5%)");
        System.out.println("Sand:  35% (±5%)");
        System.out.println("Water: 15% (±5%)");
        System.out.println();
        
        System.out.println("=== DEVIATION FROM TARGET ===");
        System.out.println("Grass: " + String.format("%+.2f", grassPercent - 50.0) + "%");
        System.out.println("Sand:  " + String.format("%+.2f", sandPercent - 35.0) + "%");
        System.out.println("Water: " + String.format("%+.2f", waterPercent - 15.0) + "%");
        System.out.println();
        
        // Provide tuning recommendations
        System.out.println("=== TUNING RECOMMENDATIONS ===");
        if (Math.abs(waterPercent - 15.0) > 2.0) {
            if (waterPercent < 15.0) {
                double newThreshold = BiomeConfig.WATER_NOISE_THRESHOLD - 0.02;
                System.out.println("Water coverage is LOW. Consider DECREASING WATER_NOISE_THRESHOLD to ~" + String.format("%.2f", newThreshold));
            } else {
                double newThreshold = BiomeConfig.WATER_NOISE_THRESHOLD + 0.02;
                System.out.println("Water coverage is HIGH. Consider INCREASING WATER_NOISE_THRESHOLD to ~" + String.format("%.2f", newThreshold));
            }
        } else {
            System.out.println("Water threshold is well-tuned!");
        }
        
        if (Math.abs(sandPercent - 35.0) > 2.0) {
            if (sandPercent < 35.0) {
                System.out.println("Sand coverage is LOW. Consider DECREASING sand threshold (currently 0.53)");
            } else {
                System.out.println("Sand coverage is HIGH. Consider INCREASING sand threshold (currently 0.53)");
            }
        } else {
            System.out.println("Sand threshold is well-tuned!");
        }
        System.out.println("=====================================\n");
        
        // Also write to file for easier viewing
        java.io.PrintWriter writer = new java.io.PrintWriter("biome-distribution-results.txt");
        writer.println("=== BIOME DISTRIBUTION MEASUREMENT ===");
        writer.println("Sample size: " + sampleSize);
        writer.println("Current thresholds:");
        writer.println("  - WATER_NOISE_THRESHOLD: " + BiomeConfig.WATER_NOISE_THRESHOLD);
        writer.println("  - Sand threshold (hardcoded): 0.50");
        writer.println();
        writer.println("=== RESULTS ===");
        writer.println("Grass: " + counts.get(BiomeType.GRASS) + " (" + String.format("%.2f", grassPercent) + "%)");
        writer.println("Sand:  " + counts.get(BiomeType.SAND) + " (" + String.format("%.2f", sandPercent) + "%)");
        writer.println("Water: " + counts.get(BiomeType.WATER) + " (" + String.format("%.2f", waterPercent) + "%)");
        writer.println();
        writer.println("=== DEVIATION FROM TARGET ===");
        writer.println("Grass: " + String.format("%+.2f", grassPercent - 50.0) + "%");
        writer.println("Sand:  " + String.format("%+.2f", sandPercent - 35.0) + "%");
        writer.println("Water: " + String.format("%+.2f", waterPercent - 15.0) + "%");
        writer.close();
    }
    
    /**
     * Measures distribution using the same sampling strategy as the property test.
     * Samples from 2000px to 50000px radius (outside exclusion zones).
     */
    @Test
    public void measureDistributionLikePropertyTest() throws Exception {
        int sampleSize = 10000;
        Map<BiomeType, Integer> counts = new HashMap<>();
        counts.put(BiomeType.GRASS, 0);
        counts.put(BiomeType.SAND, 0);
        counts.put(BiomeType.WATER, 0);
        
        Random random = new Random(12345);
        
        System.out.println("\n=== PROPERTY TEST SAMPLING STRATEGY ===");
        System.out.println("Sample size: " + sampleSize);
        System.out.println("Sample area: 2000px to 50000px radius (outside exclusion zones)");
        System.out.println();
        
        for (int i = 0; i < sampleSize; i++) {
            float angle = random.nextFloat() * 2.0f * (float) Math.PI;
            float distance = 2000 + random.nextFloat() * 48000;
            
            float worldX = distance * (float) Math.cos(angle);
            float worldY = distance * (float) Math.sin(angle);
            
            BiomeType biome = biomeManager.getBiomeAtPosition(worldX, worldY);
            counts.put(biome, counts.get(biome) + 1);
        }
        
        double grassPercent = (counts.get(BiomeType.GRASS) * 100.0) / sampleSize;
        double sandPercent = (counts.get(BiomeType.SAND) * 100.0) / sampleSize;
        double waterPercent = (counts.get(BiomeType.WATER) * 100.0) / sampleSize;
        
        System.out.println("=== RESULTS ===");
        System.out.println("Grass: " + counts.get(BiomeType.GRASS) + " (" + String.format("%.2f", grassPercent) + "%)");
        System.out.println("Sand:  " + counts.get(BiomeType.SAND) + " (" + String.format("%.2f", sandPercent) + "%)");
        System.out.println("Water: " + counts.get(BiomeType.WATER) + " (" + String.format("%.2f", waterPercent) + "%)");
        System.out.println();
        
        System.out.println("=== DEVIATION FROM TARGET ===");
        System.out.println("Grass: " + String.format("%+.2f", grassPercent - 50.0) + "%");
        System.out.println("Sand:  " + String.format("%+.2f", sandPercent - 35.0) + "%");
        System.out.println("Water: " + String.format("%+.2f", waterPercent - 15.0) + "%");
        System.out.println("=========================================\n");
    }
    
    /**
     * Measures distribution in different regions to verify consistency.
     */
    @Test
    public void measureRegionalDistribution() {
        int samplesPerRegion = 10000;
        
        System.out.println("\n=== REGIONAL DISTRIBUTION MEASUREMENT ===");
        System.out.println("Samples per region: " + samplesPerRegion);
        System.out.println();
        
        // Test different regions
        measureRegion("Near Spawn (0-2000px)", 0, 2000, samplesPerRegion);
        measureRegion("Mid Range (2000-5000px)", 2000, 5000, samplesPerRegion);
        measureRegion("Far Range (5000-10000px)", 5000, 10000, samplesPerRegion);
        
        System.out.println("=========================================\n");
    }
    
    private void measureRegion(String regionName, float minDist, float maxDist, int sampleSize) {
        Map<BiomeType, Integer> counts = new HashMap<>();
        counts.put(BiomeType.GRASS, 0);
        counts.put(BiomeType.SAND, 0);
        counts.put(BiomeType.WATER, 0);
        
        Random random = new Random(12345);
        
        for (int i = 0; i < sampleSize; i++) {
            // Generate random angle
            double angle = random.nextDouble() * 2 * Math.PI;
            // Generate random distance in range
            float distance = minDist + random.nextFloat() * (maxDist - minDist);
            
            // Convert to coordinates
            float x = (float) (Math.cos(angle) * distance);
            float y = (float) (Math.sin(angle) * distance);
            
            BiomeType biome = biomeManager.getBiomeAtPosition(x, y);
            counts.put(biome, counts.get(biome) + 1);
        }
        
        double grassPercent = (counts.get(BiomeType.GRASS) * 100.0) / sampleSize;
        double sandPercent = (counts.get(BiomeType.SAND) * 100.0) / sampleSize;
        double waterPercent = (counts.get(BiomeType.WATER) * 100.0) / sampleSize;
        
        System.out.println("Region: " + regionName);
        System.out.println("  Grass: " + String.format("%.2f", grassPercent) + "%");
        System.out.println("  Sand:  " + String.format("%.2f", sandPercent) + "%");
        System.out.println("  Water: " + String.format("%.2f", waterPercent) + "%");
        System.out.println();
    }
}
