package wagemaker.uk.network;

import org.junit.jupiter.api.Test;
import wagemaker.uk.fence.FencePieceType;
import wagemaker.uk.fence.FenceMaterialType;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for fence network message serialization and deserialization.
 * Tests message handling in existing network system.
 */
public class FenceNetworkMessageTest {
    
    @Test
    public void testFencePlaceMessageSerialization() throws IOException, ClassNotFoundException {
        // Create a fence place message
        FencePlaceMessage original = new FencePlaceMessage(
            "server",
            "fence-123",
            10,
            20,
            FencePieceType.FENCE_BACK_LEFT,
            FenceMaterialType.WOOD,
            "player-456"
        );
        
        // Serialize the message
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        // Deserialize the message
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FencePlaceMessage deserialized = (FencePlaceMessage) ois.readObject();
        ois.close();
        
        // Verify all fields are preserved
        assertEquals(original.getSenderId(), deserialized.getSenderId());
        assertEquals(original.getFenceId(), deserialized.getFenceId());
        assertEquals(original.getGridX(), deserialized.getGridX());
        assertEquals(original.getGridY(), deserialized.getGridY());
        assertEquals(original.getPieceType(), deserialized.getPieceType());
        assertEquals(original.getMaterialType(), deserialized.getMaterialType());
        assertEquals(original.getPlayerId(), deserialized.getPlayerId());
        assertEquals(MessageType.FENCE_PLACE, deserialized.getType());
    }
    
    @Test
    public void testFenceRemoveMessageSerialization() throws IOException, ClassNotFoundException {
        // Create a fence remove message
        FenceRemoveMessage original = new FenceRemoveMessage(
            "client-789",
            "fence-456",
            15,
            25,
            FenceMaterialType.BAMBOO,
            "player-789"
        );
        
        // Serialize the message
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        // Deserialize the message
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FenceRemoveMessage deserialized = (FenceRemoveMessage) ois.readObject();
        ois.close();
        
        // Verify all fields are preserved
        assertEquals(original.getSenderId(), deserialized.getSenderId());
        assertEquals(original.getFenceId(), deserialized.getFenceId());
        assertEquals(original.getGridX(), deserialized.getGridX());
        assertEquals(original.getGridY(), deserialized.getGridY());
        assertEquals(original.getMaterialType(), deserialized.getMaterialType());
        assertEquals(original.getPlayerId(), deserialized.getPlayerId());
        assertEquals(MessageType.FENCE_REMOVE, deserialized.getType());
    }
    
    @Test
    public void testFenceSyncMessageSerialization() throws IOException, ClassNotFoundException {
        // Create fence states for sync message
        Map<String, FenceState> fenceStates = new HashMap<>();
        
        FenceState fence1 = new FenceState(
            "fence-1",
            0, 0,
            FencePieceType.FENCE_BACK_LEFT,
            FenceMaterialType.WOOD,
            "player-1",
            System.currentTimeMillis()
        );
        
        FenceState fence2 = new FenceState(
            "fence-2",
            1, 0,
            FencePieceType.FENCE_BACK,
            FenceMaterialType.BAMBOO,
            "player-2",
            System.currentTimeMillis()
        );
        
        fenceStates.put("fence-1", fence1);
        fenceStates.put("fence-2", fence2);
        
        // Create sync message
        FenceSyncMessage original = new FenceSyncMessage("server", fenceStates);
        
        // Serialize the message
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        // Deserialize the message
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FenceSyncMessage deserialized = (FenceSyncMessage) ois.readObject();
        ois.close();
        
        // Verify message properties
        assertEquals(original.getSenderId(), deserialized.getSenderId());
        assertEquals(original.getFenceCount(), deserialized.getFenceCount());
        assertEquals(MessageType.FENCE_SYNC, deserialized.getType());
        
        // Verify fence states are preserved
        Map<String, FenceState> deserializedFences = deserialized.getFenceStates();
        assertEquals(2, deserializedFences.size());
        
        FenceState deserializedFence1 = deserializedFences.get("fence-1");
        assertNotNull(deserializedFence1);
        assertEquals(fence1.getFenceId(), deserializedFence1.getFenceId());
        assertEquals(fence1.getGridX(), deserializedFence1.getGridX());
        assertEquals(fence1.getGridY(), deserializedFence1.getGridY());
        assertEquals(fence1.getPieceType(), deserializedFence1.getPieceType());
        assertEquals(fence1.getMaterialType(), deserializedFence1.getMaterialType());
        assertEquals(fence1.getOwnerId(), deserializedFence1.getOwnerId());
        
        FenceState deserializedFence2 = deserializedFences.get("fence-2");
        assertNotNull(deserializedFence2);
        assertEquals(fence2.getFenceId(), deserializedFence2.getFenceId());
        assertEquals(fence2.getGridX(), deserializedFence2.getGridX());
        assertEquals(fence2.getGridY(), deserializedFence2.getGridY());
        assertEquals(fence2.getPieceType(), deserializedFence2.getPieceType());
        assertEquals(fence2.getMaterialType(), deserializedFence2.getMaterialType());
        assertEquals(fence2.getOwnerId(), deserializedFence2.getOwnerId());
    }
    
    @Test
    public void testFenceStateSerialization() throws IOException, ClassNotFoundException {
        // Create a fence state
        FenceState original = new FenceState(
            "fence-test",
            100,
            200,
            FencePieceType.FENCE_FRONT_RIGHT,
            FenceMaterialType.WOOD,
            "owner-test",
            1234567890L
        );
        
        // Serialize the fence state
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();
        
        // Deserialize the fence state
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FenceState deserialized = (FenceState) ois.readObject();
        ois.close();
        
        // Verify all fields are preserved
        assertEquals(original.getFenceId(), deserialized.getFenceId());
        assertEquals(original.getGridX(), deserialized.getGridX());
        assertEquals(original.getGridY(), deserialized.getGridY());
        assertEquals(original.getPieceType(), deserialized.getPieceType());
        assertEquals(original.getMaterialType(), deserialized.getMaterialType());
        assertEquals(original.getOwnerId(), deserialized.getOwnerId());
        assertEquals(original.getCreationTime(), deserialized.getCreationTime());
    }
    
    @Test
    public void testMessageHandlerIntegration() {
        // Create a test message handler
        TestMessageHandler handler = new TestMessageHandler();
        
        // Test fence place message handling
        FencePlaceMessage placeMessage = new FencePlaceMessage(
            "client",
            "fence-place-test",
            5, 10,
            FencePieceType.FENCE_MIDDLE_LEFT,
            FenceMaterialType.BAMBOO,
            "player-test"
        );
        
        handler.handleMessage(placeMessage);
        assertTrue(handler.fencePlaceHandled);
        assertEquals("fence-place-test", handler.lastFenceId);
        
        // Test fence remove message handling
        FenceRemoveMessage removeMessage = new FenceRemoveMessage(
            "client",
            "fence-remove-test",
            7, 12,
            FenceMaterialType.WOOD,
            "player-test"
        );
        
        handler.handleMessage(removeMessage);
        assertTrue(handler.fenceRemoveHandled);
        assertEquals("fence-remove-test", handler.lastFenceId);
        
        // Test fence sync message handling
        Map<String, FenceState> fences = new HashMap<>();
        fences.put("sync-fence", new FenceState(
            "sync-fence", 0, 0, FencePieceType.FENCE_BACK, 
            FenceMaterialType.WOOD, "owner", System.currentTimeMillis()
        ));
        
        FenceSyncMessage syncMessage = new FenceSyncMessage("server", fences);
        handler.handleMessage(syncMessage);
        assertTrue(handler.fenceSyncHandled);
        assertEquals(1, handler.lastSyncCount);
    }
    
    /**
     * Test message handler that tracks which fence messages were handled.
     */
    private static class TestMessageHandler extends DefaultMessageHandler {
        boolean fencePlaceHandled = false;
        boolean fenceRemoveHandled = false;
        boolean fenceSyncHandled = false;
        String lastFenceId = null;
        int lastSyncCount = 0;
        
        @Override
        protected void handleFencePlace(FencePlaceMessage message) {
            fencePlaceHandled = true;
            lastFenceId = message.getFenceId();
        }
        
        @Override
        protected void handleFenceRemove(FenceRemoveMessage message) {
            fenceRemoveHandled = true;
            lastFenceId = message.getFenceId();
        }
        
        @Override
        protected void handleFenceSync(FenceSyncMessage message) {
            fenceSyncHandled = true;
            lastSyncCount = message.getFenceCount();
        }
    }
    
    @Test
    public void testWorldStateIntegration() {
        // Test fence state management in WorldState
        WorldState worldState = new WorldState();
        
        // Test adding fences
        FenceState fence1 = new FenceState(
            "world-fence-1",
            50, 60,
            FencePieceType.FENCE_BACK_LEFT,
            FenceMaterialType.WOOD,
            "player-world",
            System.currentTimeMillis()
        );
        
        worldState.addOrUpdateFence(fence1);
        assertTrue(worldState.hasFenceAt(50, 60));
        assertEquals(fence1, worldState.getFenceAt(50, 60));
        assertEquals(fence1, worldState.getFence("world-fence-1"));
        
        // Test fence ownership queries
        assertEquals(1, worldState.getFenceCountByOwner("player-world"));
        assertEquals(0, worldState.getFenceCountByOwner("other-player"));
        
        Map<String, FenceState> ownedFences = worldState.getFencesByOwner("player-world");
        assertEquals(1, ownedFences.size());
        assertTrue(ownedFences.containsKey("world-fence-1"));
        
        // Test removing fences
        worldState.removeFence("world-fence-1");
        assertFalse(worldState.hasFenceAt(50, 60));
        assertNull(worldState.getFenceAt(50, 60));
        assertNull(worldState.getFence("world-fence-1"));
        assertEquals(0, worldState.getFenceCountByOwner("player-world"));
    }
}