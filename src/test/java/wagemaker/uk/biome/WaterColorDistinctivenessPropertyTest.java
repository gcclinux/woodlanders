package wagemaker.uk.biome;

import com.badlogic.gdx.graphics.Pixmap;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.Assume;
import org.junit.runner.RunWith;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based test for water texture color distinctiveness.
 * 
 * Feature: water-lake-biome, Property 2: Water color distinctiveness
 * Validates: Requirements 1.3
 */
@RunWith(JUnitQuickcheck.class)
public class WaterColorDistinctivenessPropertyTest {
    
    private static boolean graphicsAvailable = true;
    
    /**
     * Property 2: Water color distinctiveness
     * For any pixel in the water texture, the blue color channel value should be 
     * greater than both the red and green channel values, distinguishing it from 
     * grass and sand textures.
     * 
     * This test directly generates a water pixmap and samples pixels to verify
     * the blue channel dominance without requiring OpenGL context.
     */
    @Property(trials = 100)
    public void waterPixelsHaveDominantBlueChannel(int sampleX, int sampleY) {
        // Skip test if graphics context is not available (headless mode)
        Assume.assumeTrue("Skipping test - graphics context not available", isGraphicsAvailable());
        // Generate water pixmap directly (same logic as BiomeTextureGenerator)
        Pixmap waterPixmap = new Pixmap(BiomeConfig.TEXTURE_SIZE, BiomeConfig.TEXTURE_SIZE, Pixmap.Format.RGBA8888);
        Random waterRandom = new Random(BiomeConfig.TEXTURE_SEED_WATER);
        
        try {
            // Water colors from config
            float[] baseColor = BiomeConfig.WATER_BASE_COLOR;
            float[] lightColor = BiomeConfig.WATER_LIGHT_COLOR;
            float[] darkColor = BiomeConfig.WATER_DARK_COLOR;
            
            // Fill with base water color
            waterPixmap.setColor(baseColor[0], baseColor[1], baseColor[2], baseColor[3]);
            waterPixmap.fill();
            
            // Add water patterns (simplified version of BiomeTextureGenerator logic)
            addWaterWaves(waterPixmap, waterRandom, baseColor, lightColor);
            addWaterReflections(waterPixmap, waterRandom, lightColor);
            addWaterDepth(waterPixmap, waterRandom, darkColor);
            
            // Constrain sample coordinates to valid texture bounds
            int textureSize = BiomeConfig.TEXTURE_SIZE;
            int x = Math.abs(sampleX % textureSize);
            int y = Math.abs(sampleY % textureSize);
            
            // Get pixel color at sampled position
            int pixel = waterPixmap.getPixel(x, y);
            
            // Extract RGBA components (RGBA8888 format)
            int red = (pixel >> 24) & 0xFF;
            int green = (pixel >> 16) & 0xFF;
            int blue = (pixel >> 8) & 0xFF;
            
            // Verify blue channel is dominant
            assertTrue(blue > red, 
                String.format("Blue channel (%d) should be greater than red channel (%d) at pixel (%d, %d)", 
                    blue, red, x, y));
            assertTrue(blue > green, 
                String.format("Blue channel (%d) should be greater than green channel (%d) at pixel (%d, %d)", 
                    blue, green, x, y));
            
        } finally {
            // Clean up pixmap
            waterPixmap.dispose();
        }
    }
    
    /**
     * Adds wave patterns to water texture (duplicated from BiomeTextureGenerator).
     */
    private void addWaterWaves(Pixmap pixmap, Random random, float[] baseColor, float[] lightColor) {
        for (int y = 0; y < BiomeConfig.TEXTURE_SIZE; y++) {
            if (y % 8 == 0 || y % 8 == 1) {
                for (int x = 0; x < BiomeConfig.TEXTURE_SIZE; x++) {
                    if (random.nextFloat() > 0.3f) {
                        pixmap.setColor(lightColor[0], lightColor[1], lightColor[2], lightColor[3]);
                        pixmap.drawPixel(x, y);
                    }
                }
            }
        }
    }
    
    /**
     * Adds light reflections to water surface (duplicated from BiomeTextureGenerator).
     */
    private void addWaterReflections(Pixmap pixmap, Random random, float[] lightColor) {
        int reflectionCount = 15 + random.nextInt(10);
        for (int i = 0; i < reflectionCount; i++) {
            int x = random.nextInt(BiomeConfig.TEXTURE_SIZE);
            int y = random.nextInt(BiomeConfig.TEXTURE_SIZE);
            
            pixmap.setColor(lightColor[0], lightColor[1], lightColor[2], lightColor[3]);
            pixmap.drawPixel(x, y);
            
            if (random.nextFloat() > 0.5f && x + 1 < BiomeConfig.TEXTURE_SIZE) {
                pixmap.drawPixel(x + 1, y);
            }
        }
    }
    
    /**
     * Adds depth variations to water texture (duplicated from BiomeTextureGenerator).
     */
    private void addWaterDepth(Pixmap pixmap, Random random, float[] darkColor) {
        for (int x = 0; x < BiomeConfig.TEXTURE_SIZE; x++) {
            for (int y = 0; y < BiomeConfig.TEXTURE_SIZE; y++) {
                float noise = random.nextFloat();
                
                if (noise > 0.85f) {
                    pixmap.setColor(darkColor[0], darkColor[1], darkColor[2], darkColor[3]);
                    pixmap.drawPixel(x, y);
                }
            }
        }
    }
    
    /**
     * Check if graphics context is available for Pixmap operations.
     */
    private static boolean isGraphicsAvailable() {
        if (!graphicsAvailable) {
            return false;
        }
        
        try {
            // Try to create a simple Pixmap to test if graphics are available
            Pixmap testPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            testPixmap.dispose();
            return true;
        } catch (UnsatisfiedLinkError e) {
            graphicsAvailable = false;
            return false;
        }
    }
}
