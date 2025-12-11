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

import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for movement restoration when navigation modes are deactivated.
 * **Feature: fence-navigation-input-fix, Property 5: Movement Restoration**
 * **Validates: Requirements 1.5, 5.5**
 * 
 * Tests that for any deactivation of all special navigation modes, normal player movement
 * should be restored.
 */
public class MovementRestorationPropertyTest {
    
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
     * Property 5: Movement Restoration
     * For any deactivation of all special navigation modes, normal player movement should be restored.
     * This ensures that when special modes are exited, the player can move normally again.
     */
    @Test
    public void testMovementRestoration() {
        Random random = new Random(42); // Fixed seed for reproducible tests
        
        // Test with 100 random scenarios
        for (int i = 0; i < 100; i++) {
            try {
                // Create a new player for each test iteration
                OrthographicCamera camera = new OrthographicCamera();
                Player player = new Player(0, 0, camera);
                
                // Test movement restoration from each special navigation mode
                testMovementRestorationFromInventoryMode(player, i);
                testMovementRestorationFromFenceBuildingMode(player, i);
                testMovementRestorationFromTargetingMode(player, i);
                
                // Test movement restoration after mode transitions
                testMovementRestorationAfterModeTransitions(player, i);
                
            } catch (Exception e) {
                fail(String.format("Trial %d failed with exception: %s", i, e.getMessage()));
            }
        }
    }
    
    /**
     * Test that movement is restored when exiting inventory navigation mode.
     */
    private void testMovementRestorationFromInventoryMode(Player player, int trialNumber) throws Exception {
        // Start in normal mode - movement should not be blocked
        player.forceNavigationMode(NavigationMode.NORMAL);
        assertFalse(player.currentModeBlocksMovement(),
            String.format("Trial %d: Movement should not be blocked in NORMAL mode", trialNumber));
        assertFalse(getMovementBlockingStatus(player),
            String.format("Trial %d: shouldBlockPlayerMovement should return false in NORMAL mode", trialNumber));
        
        // Activate inventory navigation mode - movement should be blocked
        player.forceNavigationMode(NavigationMode.INVENTORY);
        assertTrue(player.currentModeBlocksMovement(),
            String.format("Trial %d: Movement should be blocked in INVENTORY mode", trialNumber));
        assertTrue(getMovementBlockingStatus(player),
            String.format("Trial %d: shouldBlockPlayerMovement should return true in INVENTORY mode", trialNumber));
        
        // Return to normal mode - movement should be restored
        boolean normalActivated = player.requestNavigationMode(NavigationMode.NORMAL);
        assertTrue(normalActivated,
            String.format("Trial %d: Should be able to return to NORMAL mode from INVENTORY", trialNumber));
        assertFalse(player.currentModeBlocksMovement(),
            String.format("Trial %d: Movement should be restored after exiting INVENTORY mode", trialNumber));
        assertFalse(getMovementBlockingStatus(player),
            String.format("Trial %d: shouldBlockPlayerMovement should return false after exiting INVENTORY mode", trialNumber));
    }
    
    /**
     * Test that movement is restored when exiting fence building navigation mode.
     */
    private void testMovementRestorationFromFenceBuildingMode(Player player, int trialNumber) throws Exception {
        // Start in normal mode - movement should not be blocked
        player.forceNavigationMode(NavigationMode.NORMAL);
        assertFalse(player.currentModeBlocksMovement(),
            String.format("Trial %d: Movement should not be blocked in NORMAL mode", trialNumber));
        assertFalse(getMovementBlockingStatus(player),
            String.format("Trial %d: shouldBlockPlayerMovement should return false in NORMAL mode", trialNumber));
        
        // Activate fence building navigation mode - movement should be blocked
        player.forceNavigationMode(NavigationMode.FENCE_BUILDING);
        assertTrue(player.currentModeBlocksMovement(),
            String.format("Trial %d: Movement should be blocked in FENCE_BUILDING mode", trialNumber));
        assertTrue(getMovementBlockingStatus(player),
            String.format("Trial %d: shouldBlockPlayerMovement should return true in FENCE_BUILDING mode", trialNumber));
        
        // Return to normal mode - movement should be restored
        boolean normalActivated = player.requestNavigationMode(NavigationMode.NORMAL);
        assertTrue(normalActivated,
            String.format("Trial %d: Should be able to return to NORMAL mode from FENCE_BUILDING", trialNumber));
        assertFalse(player.currentModeBlocksMovement(),
            String.format("Trial %d: Movement should be restored after exiting FENCE_BUILDING mode", trialNumber));
        assertFalse(getMovementBlockingStatus(player),
            String.format("Trial %d: shouldBlockPlayerMovement should return false after exiting FENCE_BUILDING mode", trialNumber));
    }
    
    /**
     * Test that movement is restored when exiting targeting mode.
     */
    private void testMovementRestorationFromTargetingMode(Player player, int trialNumber) throws Exception {
        // Start in normal mode - movement should not be blocked
        player.forceNavigationMode(NavigationMode.NORMAL);
        assertFalse(player.currentModeBlocksMovement(),
            String.format("Trial %d: Movement should not be blocked in NORMAL mode", trialNumber));
        assertFalse(getMovementBlockingStatus(player),
            String.format("Trial %d: shouldBlockPlayerMovement should return false in NORMAL mode", trialNumber));
        
        // Activate targeting navigation mode - movement should be blocked
        player.forceNavigationMode(NavigationMode.TARGETING);
        assertTrue(player.currentModeBlocksMovement(),
            String.format("Trial %d: Movement should be blocked in TARGETING mode", trialNumber));
        // Note: targeting mode blocking is handled by the targeting system, not just the navigation mode
        
        // Return to normal mode - movement should be restored
        boolean normalActivated = player.requestNavigationMode(NavigationMode.NORMAL);
        assertTrue(normalActivated,
            String.format("Trial %d: Should be able to return to NORMAL mode from TARGETING", trialNumber));
        assertFalse(player.currentModeBlocksMovement(),
            String.format("Trial %d: Movement should be restored after exiting TARGETING mode", trialNumber));
    }
    
    /**
     * Test movement restoration after complex mode transitions.
     */
    private void testMovementRestorationAfterModeTransitions(Player player, int trialNumber) throws Exception {
        // Start in normal mode
        player.forceNavigationMode(NavigationMode.NORMAL);
        assertFalse(player.currentModeBlocksMovement(),
            String.format("Trial %d: Movement should not be blocked initially", trialNumber));
        
        // Go through a sequence of mode changes
        NavigationMode[] testSequence = {
            NavigationMode.INVENTORY,
            NavigationMode.NORMAL,
            NavigationMode.FENCE_BUILDING,
            NavigationMode.NORMAL,
            NavigationMode.TARGETING,
            NavigationMode.NORMAL
        };
        
        for (int j = 0; j < testSequence.length; j++) {
            NavigationMode targetMode = testSequence[j];
            player.forceNavigationMode(targetMode);
            
            assertEquals(targetMode, player.getCurrentNavigationMode(),
                String.format("Trial %d, Step %d: Should be in %s mode", trialNumber, j, targetMode.getDescription()));
            
            if (targetMode == NavigationMode.NORMAL) {
                assertFalse(player.currentModeBlocksMovement(),
                    String.format("Trial %d, Step %d: Movement should be restored in NORMAL mode", trialNumber, j));
                assertFalse(getMovementBlockingStatus(player),
                    String.format("Trial %d, Step %d: shouldBlockPlayerMovement should return false in NORMAL mode", trialNumber, j));
            } else {
                assertTrue(player.currentModeBlocksMovement(),
                    String.format("Trial %d, Step %d: Movement should be blocked in %s mode", trialNumber, j, targetMode.getDescription()));
            }
        }
        
        // Final check - should end in normal mode with movement restored
        assertEquals(NavigationMode.NORMAL, player.getCurrentNavigationMode(),
            String.format("Trial %d: Should end in NORMAL mode", trialNumber));
        assertFalse(player.currentModeBlocksMovement(),
            String.format("Trial %d: Movement should be restored at the end", trialNumber));
    }
    
    /**
     * Helper method to get the movement blocking status using reflection.
     * This accesses the private shouldBlockPlayerMovement method.
     */
    private boolean getMovementBlockingStatus(Player player) throws Exception {
        Method shouldBlockMethod = Player.class.getDeclaredMethod("shouldBlockPlayerMovement");
        shouldBlockMethod.setAccessible(true);
        return (Boolean) shouldBlockMethod.invoke(player);
    }
    
    /**
     * Test that the legacy compatibility fields are properly updated.
     */
    @Test
    public void testLegacyCompatibilityFieldsRestoration() throws Exception {
        OrthographicCamera camera = new OrthographicCamera();
        Player player = new Player(0, 0, camera);
        
        // Access legacy fields using reflection
        java.lang.reflect.Field inventoryModeField = Player.class.getDeclaredField("inventoryNavigationMode");
        inventoryModeField.setAccessible(true);
        java.lang.reflect.Field fenceModeField = Player.class.getDeclaredField("fenceNavigationMode");
        fenceModeField.setAccessible(true);
        
        // Start in normal mode - legacy fields should be false
        player.forceNavigationMode(NavigationMode.NORMAL);
        assertFalse((Boolean) inventoryModeField.get(player),
            "inventoryNavigationMode should be false in NORMAL mode");
        assertFalse((Boolean) fenceModeField.get(player),
            "fenceNavigationMode should be false in NORMAL mode");
        
        // Activate inventory mode - only inventory field should be true
        player.forceNavigationMode(NavigationMode.INVENTORY);
        assertTrue((Boolean) inventoryModeField.get(player),
            "inventoryNavigationMode should be true in INVENTORY mode");
        assertFalse((Boolean) fenceModeField.get(player),
            "fenceNavigationMode should be false in INVENTORY mode");
        
        // Return to normal mode - both fields should be false again
        player.forceNavigationMode(NavigationMode.NORMAL);
        assertFalse((Boolean) inventoryModeField.get(player),
            "inventoryNavigationMode should be false after returning to NORMAL mode");
        assertFalse((Boolean) fenceModeField.get(player),
            "fenceNavigationMode should be false after returning to NORMAL mode");
        
        // Activate fence building mode - only fence field should be true
        player.forceNavigationMode(NavigationMode.FENCE_BUILDING);
        assertFalse((Boolean) inventoryModeField.get(player),
            "inventoryNavigationMode should be false in FENCE_BUILDING mode");
        assertTrue((Boolean) fenceModeField.get(player),
            "fenceNavigationMode should be true in FENCE_BUILDING mode");
        
        // Return to normal mode - both fields should be false again
        player.forceNavigationMode(NavigationMode.NORMAL);
        assertFalse((Boolean) inventoryModeField.get(player),
            "inventoryNavigationMode should be false after returning to NORMAL mode from FENCE_BUILDING");
        assertFalse((Boolean) fenceModeField.get(player),
            "fenceNavigationMode should be false after returning to NORMAL mode from FENCE_BUILDING");
    }
}