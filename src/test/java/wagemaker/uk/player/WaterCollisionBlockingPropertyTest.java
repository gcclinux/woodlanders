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

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based test for water collision blocking.
 * 
 * Feature: water-lake-biome, Property 5: Water collision blocking
 * Validates: Requirements 2.1, 2.3
 */
@RunWith(JUnitQuickcheck.class)
public class WaterCollisionBlockingPropertyTest {
    
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
     * Property 5: Water collision blocking
     * For any world coordinate where getBiomeAtPosition returns WATER, 
     * attempting to move the player to that coordinate should result in 
     * wouldCollide returning true.
     * 
     * Validates: Requirements 2.1, 2.3
     */
    @Property(trials = 100)
    public void waterCoordinatesBlockMovement(float worldX, float worldY) {
        // Skip test if graphics context is not available (headless mode)
        Assume.assumeTrue("Skipping test - graphics context not available", graphicsAvailable);
        
        // Constrain coordinates to reasonable range to avoid overflow
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
            // Calculate player position that would place center at water coordinate
            // Player is 64x64, so center is at position + 32
            float playerX = worldX - 32;
            float playerY = worldY - 32;
            
            // Access private wouldCollide method using reflection
            Method wouldCollideMethod = Player.class.getDeclaredMethod("wouldCollide", float.class, float.class);
            wouldCollideMethod.setAccessible(true);
            
            // Test collision detection
            boolean collides = (boolean) wouldCollideMethod.invoke(player, playerX, playerY);
            
            // Verify that water blocks movement
            assertTrue(collides, 
                String.format("Water at (%.2f, %.2f) should block player movement", worldX, worldY));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to test water collision at (" + worldX + ", " + worldY + ")", e);
        } finally {
            player.dispose();
            biomeManager.dispose();
        }
    }
}
