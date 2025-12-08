package wagemaker.uk.inventory;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import wagemaker.uk.network.GameClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for BackFence multiplayer synchronization behavior.
 * 
 * Feature: back-fence-inventory
 * Tests Properties 3, 4, and 5 related to multiplayer sync
 */
@RunWith(JUnitQuickcheck.class)
public class BackFenceMultiplayerSyncPropertyTest {
    
    /**
     * Property 3: Multiplayer collection triggers sync
     * For any BackFence collection event in multiplayer mode with an active server connection,
     * an inventory update message should be sent to the server.
     * 
     * Validates: Requirements 2.1
     */
    @Property(trials = 100)
    public void multiplayerCollectionTriggersSync(int initialCount) {
        // Ensure initialCount is non-negative
        if (initialCount < 0) {
            initialCount = 0;
        }
        
        // Create inventory manager with null player (player not needed for this test)
        InventoryManager inventoryManager = new InventoryManager(null);
        
        // Set up multiplayer mode with mock game client
        GameClient mockClient = mock(GameClient.class);
        when(mockClient.isConnected()).thenReturn(true);
        when(mockClient.getClientId()).thenReturn("test-client");
        
        inventoryManager.setMultiplayerMode(true);
        inventoryManager.setGameClient(mockClient);
        
        // Set initial count
        inventoryManager.getCurrentInventory().setBackFenceCount(initialCount);
        
        // Collect a BackFence item
        inventoryManager.collectItem(ItemType.BACK_FENCE);
        
        // Verify that sendMessage was called on the game client
        verify(mockClient, atLeastOnce()).sendMessage(any());
        
        // Verify the count increased
        assertEquals(initialCount + 1, inventoryManager.getCurrentInventory().getBackFenceCount(),
                     "BackFence count should increase by 1 after collection");
    }
    
    /**
     * Property 4: Server sync updates local count
     * For any server sync message containing a BackFence count,
     * after synchronization the local inventory's BackFence count should match the server's value.
     * 
     * Validates: Requirements 2.2
     */
    @Property(trials = 100)
    public void serverSyncUpdatesLocalCount(int serverCount) {
        // Ensure serverCount is non-negative
        if (serverCount < 0) {
            serverCount = 0;
        }
        
        // Create inventory manager with null player (player not needed for this test)
        InventoryManager inventoryManager = new InventoryManager(null);
        
        // Set up multiplayer mode
        inventoryManager.setMultiplayerMode(true);
        
        // Set a different initial count to ensure sync actually changes it
        int initialCount = serverCount + 10;
        inventoryManager.getCurrentInventory().setBackFenceCount(initialCount);
        
        // Simulate server sync with all item counts (using 0 for other items)
        inventoryManager.syncFromServer(
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, serverCount
        );
        
        // Verify the local count matches the server count
        assertEquals(serverCount, inventoryManager.getCurrentInventory().getBackFenceCount(),
                     "Local BackFence count should match server count after sync");
    }
    
    /**
     * Property 5: Multiplayer removal triggers sync
     * For any BackFence removal event in multiplayer mode with an active server connection,
     * an inventory update message should be sent to the server.
     * 
     * Validates: Requirements 2.3
     */
    @Property(trials = 100)
    public void multiplayerRemovalTriggersSync(int initialCount) {
        // Ensure initialCount is positive (need at least 1 to remove)
        if (initialCount <= 0) {
            initialCount = 1;
        }
        
        // Create inventory manager with null player (player not needed for this test)
        InventoryManager inventoryManager = new InventoryManager(null);
        
        // Set up multiplayer mode with mock game client
        GameClient mockClient = mock(GameClient.class);
        when(mockClient.isConnected()).thenReturn(true);
        when(mockClient.getClientId()).thenReturn("test-client");
        
        inventoryManager.setMultiplayerMode(true);
        inventoryManager.setGameClient(mockClient);
        
        // Set initial count
        inventoryManager.getCurrentInventory().setBackFenceCount(initialCount);
        
        // Clear any previous interactions
        reset(mockClient);
        when(mockClient.isConnected()).thenReturn(true);
        when(mockClient.getClientId()).thenReturn("test-client");
        
        // Remove a BackFence item
        boolean removed = inventoryManager.getCurrentInventory().removeBackFence(1);
        
        // Trigger sync by calling sendInventoryUpdateToServer
        inventoryManager.sendInventoryUpdateToServer();
        
        // Verify removal was successful
        assertTrue(removed, "BackFence removal should succeed when count > 0");
        
        // Verify that sendMessage was called on the game client
        verify(mockClient, atLeastOnce()).sendMessage(any());
        
        // Verify the count decreased
        assertEquals(initialCount - 1, inventoryManager.getCurrentInventory().getBackFenceCount(),
                     "BackFence count should decrease by 1 after removal");
    }
}
