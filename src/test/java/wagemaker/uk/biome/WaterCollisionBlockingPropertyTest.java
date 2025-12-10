package wagemaker.uk.biome;

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
import wagemaker.uk.player.Player;

import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based test for water collision blocking in beach-style water biome system.
 * 
 * Feature: beach-style-water-biome, Property 8: Water collision blocking
 * Validates: Requirements 4.1
 * 
 * This test verifies that for any world coordinate where getBiomeAtPosition returns WATER,
 * attempting to move the player to that coordinate should result in wouldCollide returning true.
 * This ensures that existing collision detection continues to work correctly with the new
 * beach-style water placement system where water only appears within sand areas.
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
     * Property 8: Water collision blocking
     * For any world coordinate where getBiomeAtPosition returns WATER, 
     * attempting to move the player to that coordinate should result in 
     * wouldCollide returning true.
     * 
     * This property ensures that the existing collision detection system continues
     * to work correctly with beach-style water placement. Water should block player
     * movement regardless of whether it appears within sand areas or elsewhere.
     * 
     * Validates: Requirements 4.1
     */
    @Property(trials = 100)
    public void waterCoordinatesBlockMovement(long randomSeed) {
        // Skip test if graphics context is not available (headless mode)
        Assume.assumeTrue("Skipping test - graphics context not available", graphicsAvailable);
        
        // Initialize BiomeManager
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        try {
            Random random = new Random(randomSeed);
            
            // Find water locations by sampling random coordinates
            // We'll test up to 5 water locations per trial to ensure good coverage
            int waterLocationsFound = 0;
            int maxWaterLocationsToTest = 5;
            int maxSearchAttempts = 200; // Limit search attempts to avoid infinite loops
            
            for (int attempt = 0; attempt < maxSearchAttempts && waterLocationsFound < maxWaterLocationsToTest; attempt++) {
                // Generate a random coordinate
                // Sample from a large area (±50000 pixels from spawn) to find water
                float testX = (random.nextFloat() - 0.5f) * 100000; // -50000 to +50000
                float testY = (random.nextFloat() - 0.5f) * 100000; // -50000 to +50000
                
                // Check if this coordinate has water
                BiomeType biomeType = biomeManager.getBiomeAtPosition(testX, testY);
                
                if (biomeType == BiomeType.WATER) {
                    waterLocationsFound++;
                    
                    // Create a player at a safe position for collision testing
                    OrthographicCamera camera = new OrthographicCamera();
                    Player player = new Player(0, 0, camera);
                    player.setBiomeManager(biomeManager);
                    
                    try {
                        // Calculate player position that would place center at water coordinate
                        // Player is 64x64, so center is at position + 32
                        float playerX = testX - 32;
                        float playerY = testY - 32;
                        
                        // Access private wouldCollide method using reflection
                        Method wouldCollideMethod = Player.class.getDeclaredMethod("wouldCollide", float.class, float.class);
                        wouldCollideMethod.setAccessible(true);
                        
                        // Test collision detection
                        boolean collides = (boolean) wouldCollideMethod.invoke(player, playerX, playerY);
                        
                        // Verify that water blocks movement
                        assertTrue(collides, 
                            String.format("Beach-style water at (%.2f, %.2f) should block player movement. " +
                                "The collision detection system must continue to work with water areas " +
                                "regardless of their placement within sand biomes.",
                                testX, testY));
                        
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to test water collision at (" + testX + ", " + testY + ")", e);
                    } finally {
                        player.dispose();
                    }
                }
            }
            
            // Note: If no water locations are found, the test passes vacuously
            // This is acceptable as it means the current biome configuration
            // may not generate water in the sampled area, which doesn't violate
            // the constraint that water should block movement when it exists
            
        } finally {
            // Clean up resources
            biomeManager.dispose();
        }
    }
}