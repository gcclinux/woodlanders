package wagemaker.uk.inventory;

import com.badlogic.gdx.Gdx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import wagemaker.uk.player.Player;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

/**
 * Property-based test for RightFence server sync.
 * Feature: right-fence-inventory, Property 4: Server sync updates local count
 * Validates: Requirements 2.2
 */
public class RightFenceServerSyncPropertyTest {
    
    @BeforeAll
    public static void checkLibGDXAvailability() {
        // Skip all tests if LibGDX is not initialized (headless environment)
        assumeTrue(Gdx.files != null, "LibGDX not initialized - skipping tests that require graphics context");
    }
    
    /**
     * Property 4: Server sync updates local count
     * For any server sync message containing a RightFence count, after synchronization
     * the local inventory's RightFence count should match the server's value.
     * Validates: Requirements 2.2
     * 
     * This property-based test runs 100 trials with randomly generated server counts,
     * verifying that the local inventory always matches the server's authoritative value.
     */
    @Test
    public void serverSyncUpdatesLocalRightFenceCount() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Set up multiplayer mode
            inventoryManager.setMultiplayerMode(true);
            
            // Generate random server count
            int serverRightFenceCount = random.nextInt(200); // 0 to 199
            
            // Set a different initial local count to verify sync overwrites it
            int initialLocalCount = serverRightFenceCount + random.nextInt(50) + 1; // Different from server
            inventoryManager.getCurrentInventory().setRightFenceCount(initialLocalCount);
            
            // Verify initial state is different
            assertNotEquals(serverRightFenceCount, inventoryManager.getCurrentInventory().getRightFenceCount(),
                "Trial " + trial + ": Initial local count should differ from server count");
            
            // Simulate server sync with all item counts (using 0 for other items)
            inventoryManager.syncFromServer(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, serverRightFenceCount
            );
            
            // Verify the local count matches the server count
            assertEquals(serverRightFenceCount, inventoryManager.getCurrentInventory().getRightFenceCount(),
                "Trial " + trial + ": Local RightFence count should match server count after sync " +
                "(server=" + serverRightFenceCount + ", local=" + inventoryManager.getCurrentInventory().getRightFenceCount() + ")");
        }
    }
    
    /**
     * Property: Server sync overwrites local count regardless of difference
     * For any pair of local and server counts, the server count should always win.
     * 
     * This property-based test runs 100 trials with various combinations of
     * local and server counts to verify the server is always authoritative.
     */
    @Test
    public void serverSyncIsAlwaysAuthoritative() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Set up multiplayer mode
            inventoryManager.setMultiplayerMode(true);
            
            // Generate random local and server counts
            int localCount = random.nextInt(200);
            int serverCount = random.nextInt(200);
            
            // Set initial local count
            inventoryManager.getCurrentInventory().setRightFenceCount(localCount);
            
            // Simulate server sync
            inventoryManager.syncFromServer(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, serverCount
            );
            
            // Verify the local count matches the server count (server wins)
            assertEquals(serverCount, inventoryManager.getCurrentInventory().getRightFenceCount(),
                "Trial " + trial + ": Server count should overwrite local count " +
                "(local=" + localCount + ", server=" + serverCount + ")");
        }
    }
    
    /**
     * Property: Server sync does not affect single-player mode
     * For any server sync call in single-player mode, the inventory should not be updated.
     * 
     * This property-based test runs 100 trials verifying that single-player
     * mode ignores server sync messages.
     */
    @Test
    public void serverSyncDoesNotAffectSinglePlayerMode() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Ensure single-player mode (default)
            inventoryManager.setMultiplayerMode(false);
            
            // Set initial local count
            int initialCount = random.nextInt(100);
            inventoryManager.getCurrentInventory().setRightFenceCount(initialCount);
            
            // Attempt server sync with different count
            int serverCount = initialCount + random.nextInt(50) + 1; // Different from initial
            inventoryManager.syncFromServer(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, serverCount
            );
            
            // Verify the local count was NOT changed (single-player mode ignores sync)
            assertEquals(initialCount, inventoryManager.getCurrentInventory().getRightFenceCount(),
                "Trial " + trial + ": Single-player mode should ignore server sync " +
                "(initial=" + initialCount + ", server=" + serverCount + ")");
        }
    }
    
    /**
     * Property: Server sync with zero count clears inventory
     * For any initial local count, syncing with server count of 0 should clear the inventory.
     * 
     * This property-based test runs 100 trials verifying that zero counts
     * from the server properly clear the local inventory.
     */
    @Test
    public void serverSyncWithZeroClearsInventory() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Set up multiplayer mode
            inventoryManager.setMultiplayerMode(true);
            
            // Set initial local count (non-zero)
            int initialCount = random.nextInt(100) + 1; // 1 to 100
            inventoryManager.getCurrentInventory().setRightFenceCount(initialCount);
            
            // Simulate server sync with zero count
            inventoryManager.syncFromServer(
                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
            );
            
            // Verify the local count is now zero
            assertEquals(0, inventoryManager.getCurrentInventory().getRightFenceCount(),
                "Trial " + trial + ": Server sync with zero should clear local inventory " +
                "(initial=" + initialCount + ")");
        }
    }
}
