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
 * Property-based test for RightFence multiplayer collection sync.
 * Feature: right-fence-inventory, Property 3: Multiplayer collection triggers sync
 * Validates: Requirements 2.1
 */
public class RightFenceMultiplayerCollectionSyncPropertyTest {
    
    @BeforeAll
    public static void checkLibGDXAvailability() {
        // Skip all tests if LibGDX is not initialized (headless environment)
        assumeTrue(Gdx.files != null, "LibGDX not initialized - skipping tests that require graphics context");
    }
    
    /**
     * Property 3: Multiplayer collection triggers sync
     * For any RightFence collection event in multiplayer mode with an active server connection,
     * an inventory update message should be sent to the server.
     * Validates: Requirements 2.1
     * 
     * This property-based test runs 100 trials with randomly generated initial inventory states,
     * verifying that collecting a RightFence in multiplayer mode always triggers a sync message.
     */
    @Test
    public void multiplayerCollectionTriggersSyncMessage() {
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
            when(mockGameClient.getClientId()).thenReturn(String.valueOf(trial)); // Use trial number as client ID
            inventoryManager.setGameClient(mockGameClient);
            
            // Generate random initial inventory state
            Inventory inventory = inventoryManager.getCurrentInventory();
            int initialRightFenceCount = random.nextInt(100); // 0 to 99
            inventory.setRightFenceCount(initialRightFenceCount);
            
            // Also set random counts for other items to ensure message includes all fields
            inventory.setAppleCount(random.nextInt(50));
            inventory.setBananaCount(random.nextInt(50));
            inventory.setBambooSaplingCount(random.nextInt(30));
            inventory.setLeftFenceCount(random.nextInt(20));
            inventory.setFrontFenceCount(random.nextInt(20));
            inventory.setBackFenceCount(random.nextInt(20));
            
            // Reset mock to clear any previous interactions
            reset(mockGameClient);
            when(mockGameClient.isConnected()).thenReturn(true);
            when(mockGameClient.getClientId()).thenReturn(String.valueOf(trial));
            
            // Collect a RightFence item
            inventoryManager.collectItem(ItemType.RIGHT_FENCE);
            
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
            assertEquals(initialRightFenceCount + 1, updateMessage.getRightFenceCount(),
                "Trial " + trial + ": The message should contain the updated RightFence count " +
                "(initial=" + initialRightFenceCount + ", expected=" + (initialRightFenceCount + 1) + ")");
            
            // Verify the inventory was actually updated
            assertEquals(initialRightFenceCount + 1, inventory.getRightFenceCount(),
                "Trial " + trial + ": The inventory should be updated with the new RightFence count");
        }
    }
    
    /**
     * Property: No sync message sent in single-player mode
     * For any RightFence collection event in single-player mode,
     * no inventory update message should be sent.
     * 
     * This property-based test runs 100 trials verifying that single-player
     * mode does not trigger network synchronization.
     */
    @Test
    public void singlePlayerCollectionDoesNotTriggerSync() {
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
            
            // Generate random initial inventory state
            Inventory inventory = inventoryManager.getCurrentInventory();
            int initialRightFenceCount = random.nextInt(100);
            inventory.setRightFenceCount(initialRightFenceCount);
            
            // Collect a RightFence item
            inventoryManager.collectItem(ItemType.RIGHT_FENCE);
            
            // Verify that sendMessage was NOT called
            verify(mockGameClient, never()).sendMessage(any(NetworkMessage.class));
            
            // Verify the inventory was still updated
            assertEquals(initialRightFenceCount + 1, inventory.getRightFenceCount(),
                "Trial " + trial + ": The inventory should be updated even in single-player mode");
        }
    }
    
    /**
     * Property: No sync message sent when disconnected
     * For any RightFence collection event in multiplayer mode when disconnected,
     * no inventory update message should be sent.
     * 
     * This property-based test runs 100 trials verifying that disconnected
     * clients do not attempt to send sync messages.
     */
    @Test
    public void disconnectedMultiplayerCollectionDoesNotTriggerSync() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Set up multiplayer mode
            inventoryManager.setMultiplayerMode(true);
            
            // Create mock game client that is NOT connected
            GameClient mockGameClient = mock(GameClient.class);
            when(mockGameClient.isConnected()).thenReturn(false);
            inventoryManager.setGameClient(mockGameClient);
            
            // Generate random initial inventory state
            Inventory inventory = inventoryManager.getCurrentInventory();
            int initialRightFenceCount = random.nextInt(100);
            inventory.setRightFenceCount(initialRightFenceCount);
            
            // Collect a RightFence item
            inventoryManager.collectItem(ItemType.RIGHT_FENCE);
            
            // Verify that sendMessage was NOT called
            verify(mockGameClient, never()).sendMessage(any(NetworkMessage.class));
            
            // Verify the inventory was still updated
            assertEquals(initialRightFenceCount + 1, inventory.getRightFenceCount(),
                "Trial " + trial + ": The inventory should be updated even when disconnected");
        }
    }
}
