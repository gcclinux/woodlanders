package wagemaker.uk.player;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import wagemaker.uk.biome.BiomeManager;
import wagemaker.uk.biome.BiomeType;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Property-based test for adjacent movement freedom near water.
 * 
 * Feature: water-lake-biome, Property 7: Adjacent movement freedom
 * Validates: Requirements 2.5
 */
@RunWith(JUnitQuickcheck.class)
public class AdjacentMovementFreedomPropertyTest {
    
    private static HeadlessApplication application;
    private static boolean graphicsAvailable = true;
    
    @BeforeClass
    public static void setUpGdx() {
        try {
            // Initialize headless LibGDX application for testing
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            application = new HeadlessApplication(new ApplicationAdapter() {}, config);
            
            // Mock GL20 to prevent null pointer exceptions
            Gdx.gl = Mockito.mock(GL20.class);
            Gdx.gl20 = Mockito.mock(GL20.class);
        } catch (Exception e) {
            graphicsAvailable = false;
        }
    }
    
    @AfterClass
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
            application = null;
        }
    }
    
    /**
     * Property 7: Adjacent movement freedom
     * For any player position adjacent to water (within 100 pixels), movement in 
     * directions that do not lead to water coordinates should succeed 
     * (wouldCollide returns false).
     * 
     * Validates: Requirements 2.5
     */
    @Property(trials = 100)
    public void adjacentNonWaterMovementAllowed(float worldX, float worldY) {
        // Skip test if graphics context is not available (headless mode)
        Assume.assumeTrue("Skipping test - graphics context not available", graphicsAvailable);
        
        // Constrain coordinates to reasonable range
        worldX = Math.abs(worldX % 10000.0f);
        worldY = Math.abs(worldY % 10000.0f);
        
        // Create BiomeManager and initialize
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        // Check if this coordinate is water
        BiomeType biomeType = biomeManager.getBiomeAtPosition(worldX, worldY);
        
        // Only test water coordinates
        if (biomeType != BiomeType.WATER) {
            return; // Skip non-water coordinates
        }
        
        // Create a player at a safe position
        OrthographicCamera camera = new OrthographicCamera();
        Player player = new Player(0, 0, camera);
        player.setBiomeManager(biomeManager);
        
        try {
            // Test positions adjacent to water (within 100 pixels)
            // We'll test 4 directions: up, down, left, right
            float[] offsets = {-100, 100}; // Test both sides
            
            for (float xOffset : offsets) {
                for (float yOffset : offsets) {
                    // Calculate adjacent position
                    float adjacentX = worldX + xOffset;
                    float adjacentY = worldY + yOffset;
                    
                    // Check if adjacent position is NOT water
                    BiomeType adjacentBiome = biomeManager.getBiomeAtPosition(adjacentX, adjacentY);
                    
                    if (adjacentBiome != BiomeType.WATER) {
                        // This is a non-water position adjacent to water
                        // Calculate player position that would place center at this coordinate
                        float playerX = adjacentX - 32;
                        float playerY = adjacentY - 32;
                        
                        // Access private wouldCollide method using reflection
                        Method wouldCollideMethod = Player.class.getDeclaredMethod("wouldCollide", float.class, float.class);
                        wouldCollideMethod.setAccessible(true);
                        
                        // Test collision detection
                        boolean collides = (boolean) wouldCollideMethod.invoke(player, playerX, playerY);
                        
                        // Verify that non-water adjacent positions allow movement
                        assertFalse(collides, 
                            String.format("Non-water position (%.2f, %.2f) adjacent to water (%.2f, %.2f) should allow movement", 
                                adjacentX, adjacentY, worldX, worldY));
                    }
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to test adjacent movement at (" + worldX + ", " + worldY + ")", e);
        } finally {
            player.dispose();
            biomeManager.dispose();
        }
    }
}
