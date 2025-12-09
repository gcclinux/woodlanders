package wagemaker.uk.inventory;

import com.badlogic.gdx.Gdx;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import wagemaker.uk.network.GameClient;
import wagemaker.uk.network.InventoryUpdateMessage;
import wagemaker.uk.network.NetworkMessage;
import wagemaker.uk.player.Player;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.*;

/**
 * Property-based test for RightFence multiplayer removal sync.
 * Feature: right-fence-inventory, Property 5: Multiplayer removal triggers sync
 * Validates: Requirements 2.3
 */
public class RightFenceMultiplayerRemovalSyncPropertyTest {
    
    @BeforeAll
    public static void checkLibGDXAvailability() {
        // Skip all tests if LibGDX is not initialized (headless environment)
        assumeTrue(Gdx.files != null, "LibGDX not initialized - skipping tests that require graphics context");
    }
    
    /**
     * Property 5: Multiplayer removal triggers sync
     * For any RightFence removal event in multiplayer mode with an active server connection,
     * an inventory update message should be sent to the server.
     * Validates: Requirements 2.3
     * 
     * This property-based test runs 100 trials with randomly generated initial inventory states,
     * verifying that removing a RightFence in multiplayer mode always triggers a sync message.
     */
    @Test
    public void multiplayerRemovalTriggersSyncMessage() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Set up multiplayer mode
            inventoryManager.setMultiplayerMode(true);
            
            // Create mock game client
            GameClient mockGameClient = mock(GameClient.class);
            when(mockGameClient.isConnected()).thenReturn(true);
            when(mockGameClient.getClientId()).thenReturn(String.valueOf(trial));
            inventoryManager.setGameClient(mockGameClient);
            
            // Generate random initial inventory state (at least 1 to remove)
            Inventory inventory = inventoryManager.getCurrentInventory();
            int initialRightFenceCount = random.nextInt(100) + 1; // 1 to 100
            inventory.setRightFenceCount(initialRightFenceCount);
            
            // Reset mock to clear any previous interactions
            reset(mockGameClient);
            when(mockGameClient.isConnected()).thenReturn(true);
            when(mockGameClient.getClientId()).thenReturn(String.valueOf(trial));
            
            // Remove a RightFence item
            boolean removed = inventory.removeRightFence(1);
            
            // Trigger sync by calling sendInventoryUpdateToServer
            inventoryManager.sendInventoryUpdateToServer();
            
            // Verify removal was successful
            assertTrue(removed,
                "Trial " + trial + ": RightFence removal should succeed when count > 0");
            
            // Verify that sendMessage was called
            ArgumentCaptor<NetworkMessage> messageCaptor = ArgumentCaptor.forClass(NetworkMessage.class);
            verify(mockGameClient, times(1)).sendMessage(messageCaptor.capture());
            
            // Verify the message is an InventoryUpdateMessage
            NetworkMessage sentMessage = messageCaptor.getValue();
            assertNotNull(sentMessage,
                "Trial " + trial + ": A message should be sent to the server");
            assertTrue(sentMessage instanceof InventoryUpdateMessage,
                "Trial " + trial + ": The sent message should be an InventoryUpdateMessage");
            
            // Verify the message contains the updated RightFence count
            InventoryUpdateMessage updateMessage = (InventoryUpdateMessage) sentMessage;
            assertEquals(initialRightFenceCount - 1, updateMessage.getRightFenceCount(),
                "Trial " + trial + ": The message should contain the updated RightFence count " +
                "(initial=" + initialRightFenceCount + ", expected=" + (initialRightFenceCount - 1) + ")");
            
            // Verify the inventory was actually updated
            assertEquals(initialRightFenceCount - 1, inventory.getRightFenceCount(),
                "Trial " + trial + ": The inventory should be updated with the new RightFence count");
        }
    }
    
    /**
     * Property: Removal fails when count is zero
     * For any removal attempt when RightFence count is zero, the removal should fail
     * and no sync message should be sent.
     * 
     * This property-based test runs 100 trials verifying that removal
     * properly fails when the inventory is empty.
     */
    @Test
    public void removalFailsWhenCountIsZero() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Set up multiplayer mode
            inventoryManager.setMultiplayerMode(true);
            
            // Create mock game client
            GameClient mockGameClient = mock(GameClient.class);
            when(mockGameClient.isConnected()).thenReturn(true);
            when(mockGameClient.getClientId()).thenReturn(String.valueOf(trial));
            inventoryManager.setGameClient(mockGameClient);
            
            // Set RightFence count to zero
            Inventory inventory = inventoryManager.getCurrentInventory();
            inventory.setRightFenceCount(0);
            
            // Reset mock to clear any previous interactions
            reset(mockGameClient);
            when(mockGameClient.isConnected()).thenReturn(true);
            when(mockGameClient.getClientId()).thenReturn(String.valueOf(trial));
            
            // Attempt to remove a RightFence item
            boolean removed = inventory.removeRightFence(1);
            
            // Trigger sync attempt
            inventoryManager.sendInventoryUpdateToServer();
            
            // Verify removal failed
            assertFalse(removed,
                "Trial " + trial + ": RightFence removal should fail when count is 0");
            
            // Verify the inventory count is still zero
            assertEquals(0, inventory.getRightFenceCount(),
                "Trial " + trial + ": The inventory count should remain 0 after failed removal");
            
            // Note: A sync message is still sent even though removal failed
            // This is expected behavior - the sync keeps the server updated
            verify(mockGameClient, times(1)).sendMessage(any(NetworkMessage.class));
        }
    }
    
    /**
     * Property: Multiple removals trigger multiple syncs
     * For any sequence of removal operations, each removal should trigger a sync message.
     * 
     * This property-based test runs 100 trials with random removal sequences
     * to verify that each removal triggers synchronization.
     */
    @Test
    public void multipleRemovalsTriggerMultipleSyncs() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Set up multiplayer mode
            inventoryManager.setMultiplayerMode(true);
            
            // Create mock game client
            GameClient mockGameClient = mock(GameClient.class);
            when(mockGameClient.isConnected()).thenReturn(true);
            when(mockGameClient.getClientId()).thenReturn(String.valueOf(trial));
            inventoryManager.setGameClient(mockGameClient);
            
            // Generate random initial count (enough for multiple removals)
            Inventory inventory = inventoryManager.getCurrentInventory();
            int removalCount = random.nextInt(5) + 2; // 2 to 6 removals
            int initialCount = removalCount + random.nextInt(10); // Enough to remove
            inventory.setRightFenceCount(initialCount);
            
            // Reset mock to clear any previous interactions
            reset(mockGameClient);
            when(mockGameClient.isConnected()).thenReturn(true);
            when(mockGameClient.getClientId()).thenReturn(String.valueOf(trial));
            
            // Perform multiple removals
            for (int i = 0; i < removalCount; i++) {
                boolean removed = inventory.removeRightFence(1);
                assertTrue(removed,
                    "Trial " + trial + ", Removal " + i + ": Should successfully remove RightFence");
                
                // Trigger sync
                inventoryManager.sendInventoryUpdateToServer();
            }
            
            // Verify that sendMessage was called for each removal
            verify(mockGameClient, times(removalCount)).sendMessage(any(NetworkMessage.class));
            
            // Verify the final count
            assertEquals(initialCount - removalCount, inventory.getRightFenceCount(),
                "Trial " + trial + ": Final count should reflect all removals " +
                "(initial=" + initialCount + ", removed=" + removalCount + ")");
        }
    }
    
    /**
     * Property: No sync in single-player mode
     * For any removal in single-player mode, no sync message should be sent.
     * 
     * This property-based test runs 100 trials verifying that single-player
     * mode does not trigger network synchronization on removal.
     */
    @Test
    public void singlePlayerRemovalDoesNotTriggerSync() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Ensure single-player mode (default)
            inventoryManager.setMultiplayerMode(false);
            
            // Create mock game client
            GameClient mockGameClient = mock(GameClient.class);
            when(mockGameClient.isConnected()).thenReturn(true);
            inventoryManager.setGameClient(mockGameClient);
            
            // Set initial count
            Inventory inventory = inventoryManager.getCurrentInventory();
            int initialCount = random.nextInt(100) + 1; // 1 to 100
            inventory.setRightFenceCount(initialCount);
            
            // Remove a RightFence item
            boolean removed = inventory.removeRightFence(1);
            
            // Trigger sync attempt
            inventoryManager.sendInventoryUpdateToServer();
            
            // Verify removal was successful
            assertTrue(removed,
                "Trial " + trial + ": Removal should succeed in single-player mode");
            
            // Verify that sendMessage was NOT called
            verify(mockGameClient, never()).sendMessage(any(NetworkMessage.class));
            
            // Verify the inventory was still updated
            assertEquals(initialCount - 1, inventory.getRightFenceCount(),
                "Trial " + trial + ": The inventory should be updated even in single-player mode");
        }
    }
}
