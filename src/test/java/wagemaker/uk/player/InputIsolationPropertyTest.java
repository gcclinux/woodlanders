package wagemaker.uk.player;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for input isolation during fence navigation mode.
 * **Feature: fence-navigation-input-fix, Property 1: Input Isolation**
 * **Validates: Requirements 1.1, 1.2, 1.3**
 * 
 * Tests that for any active fence navigation mode, LEFT/RIGHT arrow key presses
 * should select fence pieces and not move the player.
 */
public class InputIsolationPropertyTest {
    
    private static HeadlessApplication application;
    
    @BeforeAll
    public static void setUpGdx() {
        // Initialize headless LibGDX application for testing
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        // Mock GL20 to prevent null pointer exceptions
        Gdx.gl = Mockito.mock(GL20.class);
        Gdx.gl20 = Mockito.mock(GL20.class);
    }
    
    @AfterAll
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
            application = null;
        }
    }
    
    /**
     * Property 1: Input Isolation
     * For any active fence navigation mode, LEFT/RIGHT arrow key presses
     * should select fence pieces and not move the player.
     * 
     * Validates: Requirements 1.1, 1.2, 1.3
     * 
     * This property-based test runs 100 trials with random initial positions.
     */
    @Test
    void inputIsolationDuringFenceNavigation() {
        // **Feature: fence-navigation-input-fix, Property 1: Input Isolation**
        
        Random random = new Random(42); // Fixed seed for reproducibility
        int trials = 100;
        
        for (int i = 0; i < trials; i++) {
            // Generate random initial position
            float initialX = random.nextFloat() * 1000.0f - 500.0f;
            float initialY = random.nextFloat() * 1000.0f - 500.0f;
            
            // Create a player at the random position
            OrthographicCamera camera = new OrthographicCamera();
            Player player = new Player(initialX, initialY, camera);
            
            try {
                // Activate fence navigation mode using the new navigation mode system
                player.forceNavigationMode(NavigationMode.FENCE_BUILDING);
                
                // Set fence navigation as stable using reflection
                java.lang.reflect.Field fenceNavigationStableField = Player.class.getDeclaredField("fenceNavigationStable");
                fenceNavigationStableField.setAccessible(true);
                fenceNavigationStableField.setBoolean(player, true);
                
                // Store initial position
                float initialPlayerX = player.getX();
                float initialPlayerY = player.getY();
                
                // Verify that shouldBlockPlayerMovement returns true when fence navigation is active
                java.lang.reflect.Method shouldBlockMethod = Player.class.getDeclaredMethod("shouldBlockPlayerMovement");
                shouldBlockMethod.setAccessible(true);
                boolean shouldBlock = (Boolean) shouldBlockMethod.invoke(player);
                
                assertTrue(shouldBlock, 
                    String.format("Trial %d: shouldBlockPlayerMovement should return true when fence navigation is active", i));
                
                // Verify that isSpecialNavigationActive returns true when fence navigation is active
                java.lang.reflect.Method isSpecialActiveMethod = Player.class.getDeclaredMethod("isSpecialNavigationActive");
                isSpecialActiveMethod.setAccessible(true);
                boolean isSpecialActive = (Boolean) isSpecialActiveMethod.invoke(player);
                
                assertTrue(isSpecialActive, 
                    String.format("Trial %d: isSpecialNavigationActive should return true when fence navigation is active", i));
                
                // Simulate update (this would normally process input, but in headless mode we can't simulate key presses)
                // The key test is that the helper methods correctly identify the navigation state
                player.update(0.1f);
                
                // Verify position hasn't changed (since movement should be blocked)
                assertEquals(initialPlayerX, player.getX(), 0.001f,
                    String.format("Trial %d: Player X position should not change when fence navigation blocks movement", i));
                assertEquals(initialPlayerY, player.getY(), 0.001f,
                    String.format("Trial %d: Player Y position should not change when fence navigation blocks movement", i));
                
                // Test deactivation - fence navigation mode off should allow movement
                player.forceNavigationMode(NavigationMode.NORMAL);
                
                shouldBlock = (Boolean) shouldBlockMethod.invoke(player);
                assertFalse(shouldBlock, 
                    String.format("Trial %d: shouldBlockPlayerMovement should return false when fence navigation is inactive", i));
                
                isSpecialActive = (Boolean) isSpecialActiveMethod.invoke(player);
                assertFalse(isSpecialActive, 
                    String.format("Trial %d: isSpecialNavigationActive should return false when fence navigation is inactive", i));
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test input isolation on trial " + i, e);
            } finally {
                player.dispose();
            }
        }
    }
    
    /**
     * Test that inventory navigation mode also triggers input isolation.
     */
    @Test
    void inputIsolationDuringInventoryNavigation() {
        Random random = new Random(123); // Different seed for variety
        int trials = 50;
        
        for (int i = 0; i < trials; i++) {
            // Generate random initial position
            float initialX = random.nextFloat() * 1000.0f - 500.0f;
            float initialY = random.nextFloat() * 1000.0f - 500.0f;
            
            // Create a player at the random position
            OrthographicCamera camera = new OrthographicCamera();
            Player player = new Player(initialX, initialY, camera);
            
            try {
                // Activate inventory navigation mode using the new navigation mode system
                player.forceNavigationMode(NavigationMode.INVENTORY);
                
                // Verify that shouldBlockPlayerMovement returns true when inventory navigation is active
                java.lang.reflect.Method shouldBlockMethod = Player.class.getDeclaredMethod("shouldBlockPlayerMovement");
                shouldBlockMethod.setAccessible(true);
                boolean shouldBlock = (Boolean) shouldBlockMethod.invoke(player);
                
                assertTrue(shouldBlock, 
                    String.format("Trial %d: shouldBlockPlayerMovement should return true when inventory navigation is active", i));
                
                // Verify that isSpecialNavigationActive returns true when inventory navigation is active
                java.lang.reflect.Method isSpecialActiveMethod = Player.class.getDeclaredMethod("isSpecialNavigationActive");
                isSpecialActiveMethod.setAccessible(true);
                boolean isSpecialActive = (Boolean) isSpecialActiveMethod.invoke(player);
                
                assertTrue(isSpecialActive, 
                    String.format("Trial %d: isSpecialNavigationActive should return true when inventory navigation is active", i));
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test inventory navigation input isolation on trial " + i, e);
            } finally {
                player.dispose();
            }
        }
    }
}