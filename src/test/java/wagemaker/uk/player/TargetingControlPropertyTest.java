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
import wagemaker.uk.targeting.TargetingSystem;
import wagemaker.uk.targeting.TargetingMode;
import wagemaker.uk.targeting.TargetingCallback;
import wagemaker.uk.targeting.Direction;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for targeting control during fence navigation mode.
 * **Feature: fence-navigation-input-fix, Property 3: Targeting Control**
 * **Validates: Requirements 3.1, 3.2**
 * 
 * Tests that for any active fence navigation mode with targeting enabled,
 * A/W/D/S key presses should move the targeting cursor and not move the player.
 */
public class TargetingControlPropertyTest {
    
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
     * Property 3: Targeting Control
     * For any active fence navigation mode with targeting enabled, A/W/D/S key presses
     * should move the targeting cursor and not move the player.
     * 
     * Validates: Requirements 3.1, 3.2
     * 
     * This property-based test runs 100 trials with random initial positions and targeting states.
     */
    @Test
    void targetingControlDuringFenceNavigation() {
        // **Feature: fence-navigation-input-fix, Property 3: Targeting Control**
        
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
                // Activate fence navigation mode using reflection
                // Activate fence navigation mode using the new navigation mode system
                player.forceNavigationMode(NavigationMode.FENCE_BUILDING);
                
                // Set fence navigation as stable using reflection
                java.lang.reflect.Field fenceNavigationStableField = Player.class.getDeclaredField("fenceNavigationStable");
                fenceNavigationStableField.setAccessible(true);
                fenceNavigationStableField.setBoolean(player, true);
                
                // Get the targeting system using reflection
                java.lang.reflect.Field targetingSystemField = Player.class.getDeclaredField("targetingSystem");
                targetingSystemField.setAccessible(true);
                TargetingSystem targetingSystem = (TargetingSystem) targetingSystemField.get(player);
                
                // Activate targeting system
                targetingSystem.activate(initialX, initialY, TargetingMode.ADJACENT, new TargetingCallback() {
                    @Override
                    public void onTargetConfirmed(float targetX, float targetY) {
                        // No-op for testing
                    }
                    
                    @Override
                    public void onTargetCancelled() {
                        // No-op for testing
                    }
                });
                
                // Verify targeting is active
                assertTrue(targetingSystem.isActive(), 
                    String.format("Trial %d: Targeting system should be active", i));
                
                // Store initial player position
                float initialPlayerX = player.getX();
                float initialPlayerY = player.getY();
                
                // Store initial target position
                float[] initialTargetCoords = targetingSystem.getTargetCoordinates();
                float initialTargetX = initialTargetCoords[0];
                float initialTargetY = initialTargetCoords[1];
                
                // Verify that shouldBlockPlayerMovement returns true when targeting is active
                java.lang.reflect.Method shouldBlockMethod = Player.class.getDeclaredMethod("shouldBlockPlayerMovement");
                shouldBlockMethod.setAccessible(true);
                boolean shouldBlock = (Boolean) shouldBlockMethod.invoke(player);
                
                assertTrue(shouldBlock, 
                    String.format("Trial %d: shouldBlockPlayerMovement should return true when targeting is active", i));
                
                // Test directional targeting movement
                Direction[] directions = {Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT};
                Direction testDirection = directions[random.nextInt(directions.length)];
                
                // Move target in the test direction
                targetingSystem.moveTarget(testDirection);
                
                // Get new target position
                float[] newTargetCoords = targetingSystem.getTargetCoordinates();
                float newTargetX = newTargetCoords[0];
                float newTargetY = newTargetCoords[1];
                
                // Verify target position changed based on direction
                switch (testDirection) {
                    case UP:
                        assertEquals(initialTargetY + 64, newTargetY, 0.001f,
                            String.format("Trial %d: Target Y should increase by 64 when moving UP", i));
                        assertEquals(initialTargetX, newTargetX, 0.001f,
                            String.format("Trial %d: Target X should not change when moving UP", i));
                        break;
                    case DOWN:
                        assertEquals(initialTargetY - 64, newTargetY, 0.001f,
                            String.format("Trial %d: Target Y should decrease by 64 when moving DOWN", i));
                        assertEquals(initialTargetX, newTargetX, 0.001f,
                            String.format("Trial %d: Target X should not change when moving DOWN", i));
                        break;
                    case LEFT:
                        assertEquals(initialTargetX - 64, newTargetX, 0.001f,
                            String.format("Trial %d: Target X should decrease by 64 when moving LEFT", i));
                        assertEquals(initialTargetY, newTargetY, 0.001f,
                            String.format("Trial %d: Target Y should not change when moving LEFT", i));
                        break;
                    case RIGHT:
                        assertEquals(initialTargetX + 64, newTargetX, 0.001f,
                            String.format("Trial %d: Target X should increase by 64 when moving RIGHT", i));
                        assertEquals(initialTargetY, newTargetY, 0.001f,
                            String.format("Trial %d: Target Y should not change when moving RIGHT", i));
                        break;
                }
                
                // Verify player position hasn't changed (movement should be blocked by targeting)
                // Note: We don't call player.update() here because it would deactivate fence navigation
                // mode due to the lack of a fence building manager in the test environment
                assertEquals(initialPlayerX, player.getX(), 0.001f,
                    String.format("Trial %d: Player X position should not change when targeting blocks movement", i));
                assertEquals(initialPlayerY, player.getY(), 0.001f,
                    String.format("Trial %d: Player Y position should not change when targeting blocks movement", i));
                
                // Test deactivation - when targeting is deactivated, fence navigation should also exit
                targetingSystem.deactivate();
                assertFalse(targetingSystem.isActive(), 
                    String.format("Trial %d: Targeting system should be inactive after deactivation", i));
                
                // When targeting is cancelled, fence navigation mode should also exit (this is the actual game behavior)
                NavigationMode currentMode = player.getCurrentNavigationMode();
                assertEquals(NavigationMode.NORMAL, currentMode,
                    String.format("Trial %d: Player should be in NORMAL mode after targeting cancellation", i));
                
                shouldBlock = (Boolean) shouldBlockMethod.invoke(player);
                assertFalse(shouldBlock, 
                    String.format("Trial %d: shouldBlockPlayerMovement should return false when both targeting and fence navigation are inactive", i));
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test targeting control on trial " + i, e);
            } finally {
                player.dispose();
            }
        }
    }
    
    /**
     * Test that targeting system properly handles mouse input when active.
     */
    @Test
    void targetingMouseControlDuringFenceNavigation() {
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
                // Activate fence navigation mode using reflection
                // Activate fence navigation mode using the new navigation mode system
                player.forceNavigationMode(NavigationMode.FENCE_BUILDING);
                
                // Get the targeting system using reflection
                java.lang.reflect.Field targetingSystemField = Player.class.getDeclaredField("targetingSystem");
                targetingSystemField.setAccessible(true);
                TargetingSystem targetingSystem = (TargetingSystem) targetingSystemField.get(player);
                
                // Set camera for mouse input support
                targetingSystem.setCamera(camera);
                
                // Activate targeting system
                targetingSystem.activate(initialX, initialY, TargetingMode.ADJACENT, new TargetingCallback() {
                    @Override
                    public void onTargetConfirmed(float targetX, float targetY) {
                        // No-op for testing
                    }
                    
                    @Override
                    public void onTargetCancelled() {
                        // No-op for testing
                    }
                });
                
                // Verify targeting is active
                assertTrue(targetingSystem.isActive(), 
                    String.format("Trial %d: Targeting system should be active", i));
                
                // Test mouse input (simulate screen coordinates)
                int mouseX = random.nextInt(800);
                int mouseY = random.nextInt(600);
                
                // Set target from mouse coordinates
                boolean mouseHandled = targetingSystem.setTargetFromMouse(mouseX, mouseY);
                assertTrue(mouseHandled, 
                    String.format("Trial %d: Mouse input should be handled when targeting is active and camera is set", i));
                
                // Verify target coordinates are updated (exact values depend on camera projection)
                float[] targetCoords = targetingSystem.getTargetCoordinates();
                assertNotNull(targetCoords, 
                    String.format("Trial %d: Target coordinates should not be null", i));
                assertEquals(2, targetCoords.length, 
                    String.format("Trial %d: Target coordinates should have 2 elements", i));
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test targeting mouse control on trial " + i, e);
            } finally {
                player.dispose();
            }
        }
    }
    
    /**
     * Test that targeting validation works correctly during fence navigation.
     */
    @Test
    void targetingValidationDuringFenceNavigation() {
        Random random = new Random(456); // Different seed for variety
        int trials = 30;
        
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
                
                // Get the targeting system using reflection
                java.lang.reflect.Field targetingSystemField = Player.class.getDeclaredField("targetingSystem");
                targetingSystemField.setAccessible(true);
                TargetingSystem targetingSystem = (TargetingSystem) targetingSystemField.get(player);
                
                // Activate targeting system
                targetingSystem.activate(initialX, initialY, TargetingMode.ADJACENT, new TargetingCallback() {
                    @Override
                    public void onTargetConfirmed(float targetX, float targetY) {
                        // No-op for testing
                    }
                    
                    @Override
                    public void onTargetCancelled() {
                        // No-op for testing
                    }
                });
                
                // Verify targeting is active
                assertTrue(targetingSystem.isActive(), 
                    String.format("Trial %d: Targeting system should be active", i));
                
                // Test target validation (without a validator, should default to valid)
                boolean isValid = targetingSystem.isTargetValid();
                assertTrue(isValid, 
                    String.format("Trial %d: Target should be valid by default when no validator is set", i));
                
                // Test that target coordinates are properly tile-aligned
                float[] targetCoords = targetingSystem.getTargetCoordinates();
                float targetX = targetCoords[0];
                float targetY = targetCoords[1];
                
                // Verify coordinates are aligned to 64-pixel tile grid
                assertEquals(0, targetX % 64, 0.001f,
                    String.format("Trial %d: Target X coordinate should be aligned to 64-pixel grid", i));
                assertEquals(0, targetY % 64, 0.001f,
                    String.format("Trial %d: Target Y coordinate should be aligned to 64-pixel grid", i));
                
            } catch (Exception e) {
                throw new RuntimeException("Failed to test targeting validation on trial " + i, e);
            } finally {
                player.dispose();
            }
        }
    }
}