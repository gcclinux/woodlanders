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
 * Property-based test for navigation mode exclusivity.
 * **Feature: fence-navigation-input-fix, Property 4: Mode Exclusivity**
 * **Validates: Requirements 5.2, 5.3**
 * 
 * Tests that for any active special navigation mode, other navigation modes
 * should be blocked from activation.
 */
public class ModeExclusivityPropertyTest {
    
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
     * Property 4: Mode Exclusivity
     * For any active special navigation mode, other navigation modes should be blocked from activation.
     * This ensures only one special navigation mode can be active at a time.
     */
    @Test
    public void testModeExclusivity() {
        Random random = new Random(42); // Fixed seed for reproducible tests
        
        // Test with 100 random scenarios
        for (int i = 0; i < 100; i++) {
            try {
                // Create a new player for each test iteration
                OrthographicCamera camera = new OrthographicCamera();
                Player player = new Player(0, 0, camera);
                
                // Test exclusivity between inventory and fence building modes
                testInventoryFenceExclusivity(player, i);
                
                // Test that targeting mode can override other modes
                testTargetingModeOverride(player, i);
                
                // Test that normal mode can always be activated
                testNormalModeAlwaysAllowed(player, i);
                
            } catch (Exception e) {
                fail(String.format("Trial %d failed with exception: %s", i, e.getMessage()));
            }
        }
    }
    
    /**
     * Test that inventory navigation and fence building navigation are mutually exclusive.
     */
    private void testInventoryFenceExclusivity(Player player, int trialNumber) {
        // Start in normal mode
        assertEquals(NavigationMode.NORMAL, player.getCurrentNavigationMode(),
            String.format("Trial %d: Player should start in NORMAL mode", trialNumber));
        
        // Activate inventory navigation mode
        boolean inventoryActivated = player.requestNavigationMode(NavigationMode.INVENTORY);
        assertTrue(inventoryActivated,
            String.format("Trial %d: Inventory navigation should be activated from NORMAL mode", trialNumber));
        assertEquals(NavigationMode.INVENTORY, player.getCurrentNavigationMode(),
            String.format("Trial %d: Player should be in INVENTORY mode", trialNumber));
        
        // Try to activate fence building mode while inventory is active - should be blocked
        boolean fenceActivated = player.requestNavigationMode(NavigationMode.FENCE_BUILDING);
        assertFalse(fenceActivated,
            String.format("Trial %d: Fence building should be blocked when inventory navigation is active", trialNumber));
        assertEquals(NavigationMode.INVENTORY, player.getCurrentNavigationMode(),
            String.format("Trial %d: Player should remain in INVENTORY mode", trialNumber));
        
        // Return to normal mode
        boolean normalActivated = player.requestNavigationMode(NavigationMode.NORMAL);
        assertTrue(normalActivated,
            String.format("Trial %d: NORMAL mode should always be activatable", trialNumber));
        assertEquals(NavigationMode.NORMAL, player.getCurrentNavigationMode(),
            String.format("Trial %d: Player should be in NORMAL mode", trialNumber));
        
        // Now activate fence building mode
        fenceActivated = player.requestNavigationMode(NavigationMode.FENCE_BUILDING);
        assertTrue(fenceActivated,
            String.format("Trial %d: Fence building should be activated from NORMAL mode", trialNumber));
        assertEquals(NavigationMode.FENCE_BUILDING, player.getCurrentNavigationMode(),
            String.format("Trial %d: Player should be in FENCE_BUILDING mode", trialNumber));
        
        // Try to activate inventory navigation while fence building is active - should be blocked
        inventoryActivated = player.requestNavigationMode(NavigationMode.INVENTORY);
        assertFalse(inventoryActivated,
            String.format("Trial %d: Inventory navigation should be blocked when fence building is active", trialNumber));
        assertEquals(NavigationMode.FENCE_BUILDING, player.getCurrentNavigationMode(),
            String.format("Trial %d: Player should remain in FENCE_BUILDING mode", trialNumber));
    }
    
    /**
     * Test that targeting mode can override other modes due to its highest priority.
     */
    private void testTargetingModeOverride(Player player, int trialNumber) {
        // Start with inventory mode active
        player.forceNavigationMode(NavigationMode.INVENTORY);
        assertEquals(NavigationMode.INVENTORY, player.getCurrentNavigationMode(),
            String.format("Trial %d: Player should be in INVENTORY mode", trialNumber));
        
        // Targeting mode should be able to override inventory mode
        boolean targetingActivated = player.requestNavigationMode(NavigationMode.TARGETING);
        assertTrue(targetingActivated,
            String.format("Trial %d: Targeting mode should override inventory mode", trialNumber));
        assertEquals(NavigationMode.TARGETING, player.getCurrentNavigationMode(),
            String.format("Trial %d: Player should be in TARGETING mode", trialNumber));
        
        // Reset to fence building mode
        player.forceNavigationMode(NavigationMode.FENCE_BUILDING);
        assertEquals(NavigationMode.FENCE_BUILDING, player.getCurrentNavigationMode(),
            String.format("Trial %d: Player should be in FENCE_BUILDING mode", trialNumber));
        
        // Targeting mode should be able to override fence building mode
        targetingActivated = player.requestNavigationMode(NavigationMode.TARGETING);
        assertTrue(targetingActivated,
            String.format("Trial %d: Targeting mode should override fence building mode", trialNumber));
        assertEquals(NavigationMode.TARGETING, player.getCurrentNavigationMode(),
            String.format("Trial %d: Player should be in TARGETING mode", trialNumber));
    }
    
    /**
     * Test that normal mode can always be activated from any other mode.
     */
    private void testNormalModeAlwaysAllowed(Player player, int trialNumber) {
        NavigationMode[] allModes = NavigationMode.values();
        
        for (NavigationMode startMode : allModes) {
            // Force the player into the start mode
            player.forceNavigationMode(startMode);
            assertEquals(startMode, player.getCurrentNavigationMode(),
                String.format("Trial %d: Player should be in %s mode", trialNumber, startMode.getDescription()));
            
            // Normal mode should always be activatable
            boolean normalActivated = player.requestNavigationMode(NavigationMode.NORMAL);
            assertTrue(normalActivated,
                String.format("Trial %d: NORMAL mode should be activatable from %s mode", trialNumber, startMode.getDescription()));
            assertEquals(NavigationMode.NORMAL, player.getCurrentNavigationMode(),
                String.format("Trial %d: Player should be in NORMAL mode after transition from %s", trialNumber, startMode.getDescription()));
        }
    }
    
    /**
     * Test helper methods for mode exclusivity checking.
     */
    @Test
    public void testModeExclusivityHelperMethods() {
        OrthographicCamera camera = new OrthographicCamera();
        Player player = new Player(0, 0, camera);
        
        // Test in normal mode
        assertEquals(NavigationMode.NORMAL, player.getCurrentNavigationMode());
        assertTrue(player.canActivateInventoryNavigation());
        assertTrue(player.canActivateFenceNavigation());
        assertFalse(player.isAnySpecialNavigationModeActive());
        
        // Test in inventory mode
        player.forceNavigationMode(NavigationMode.INVENTORY);
        assertTrue(player.canActivateInventoryNavigation()); // Can stay in same mode
        assertFalse(player.canActivateFenceNavigation()); // Cannot activate fence while in inventory
        assertTrue(player.isAnySpecialNavigationModeActive());
        
        // Test in fence building mode
        player.forceNavigationMode(NavigationMode.FENCE_BUILDING);
        assertFalse(player.canActivateInventoryNavigation()); // Cannot activate inventory while in fence
        assertTrue(player.canActivateFenceNavigation()); // Can stay in same mode
        assertTrue(player.isAnySpecialNavigationModeActive());
        
        // Test in targeting mode
        player.forceNavigationMode(NavigationMode.TARGETING);
        assertFalse(player.canActivateInventoryNavigation()); // Cannot activate inventory while targeting
        assertFalse(player.canActivateFenceNavigation()); // Cannot activate fence while targeting
        assertTrue(player.isAnySpecialNavigationModeActive());
    }
    
    /**
     * Test rejection reason messages.
     */
    @Test
    public void testNavigationModeRejectionReasons() {
        OrthographicCamera camera = new OrthographicCamera();
        Player player = new Player(0, 0, camera);
        
        // Test rejection reasons when in inventory mode
        player.forceNavigationMode(NavigationMode.INVENTORY);
        
        String reason = player.getNavigationModeRejectionReason(NavigationMode.FENCE_BUILDING);
        assertTrue(reason.contains("exclusivity") || reason.contains("blocked"),
            "Rejection reason should mention exclusivity or blocking");
        
        reason = player.getNavigationModeRejectionReason(NavigationMode.INVENTORY);
        assertTrue(reason.contains("already active"),
            "Rejection reason should mention mode is already active");
    }
}