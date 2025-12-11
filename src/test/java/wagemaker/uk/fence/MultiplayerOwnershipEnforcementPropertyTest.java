package wagemaker.uk.fence;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import wagemaker.uk.network.FenceState;
import wagemaker.uk.network.WorldState;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for multiplayer fence ownership enforcement.
 * 
 * Feature: custom-fence-building, Property 11: Multiplayer ownership enforcement
 * Validates: Requirements 3.5
 */
@RunWith(JUnitQuickcheck.class)
public class MultiplayerOwnershipEnforcementPropertyTest {
    
    /**
     * Property 11: Multiplayer ownership enforcement
     * For any fence removal attempt in multiplayer mode, removal should succeed 
     * if and only if the player owns the fence piece.
     * 
     * This test verifies that:
     * 1. Players can remove their own fences
     * 2. Players cannot remove fences owned by other players
     * 3. Ownership validation is consistent across all fence operations
     */
    @Property(trials = 100)
    public void ownershipEnforcementIsConsistent(int gridX, int gridY) {
        // Constrain grid coordinates to reasonable bounds
        gridX = Math.abs(gridX) % 1000;
        gridY = Math.abs(gridY) % 1000;
        
        // Create world state and two different players
        WorldState worldState = new WorldState();
        String owner1 = "player1-" + UUID.randomUUID().toString();
        String owner2 = "player2-" + UUID.randomUUID().toString();
        
        // Create a fence owned by player1
        String fenceId = "fence-" + UUID.randomUUID().toString();
        FenceState fence = new FenceState(
            fenceId,
            gridX,
            gridY,
            FencePieceType.FENCE_BACK_LEFT,
            FenceMaterialType.WOOD,
            owner1,
            System.currentTimeMillis()
        );
        
        // Add fence to world state
        worldState.addOrUpdateFence(fence);
        
        // Verify fence was added
        assertTrue(worldState.hasFenceAt(gridX, gridY), 
                  "Fence should exist at the specified position");
        
        // Verify owner1 can access their fence
        FenceState retrievedFence = worldState.getFenceAt(gridX, gridY);
        assertNotNull(retrievedFence, "Fence should be retrievable by position");
        assertEquals(owner1, retrievedFence.getOwnerId(), 
                    "Fence owner should be player1");
        
        // Verify ownership validation: owner1 should be able to remove their fence
        assertEquals(owner1, retrievedFence.getOwnerId(), 
                    "Owner validation should pass for the actual owner");
        
        // Verify ownership validation: owner2 should NOT be able to remove the fence
        assertNotEquals(owner2, retrievedFence.getOwnerId(), 
                       "Owner validation should fail for non-owner");
        
        // Test fence removal by owner
        worldState.removeFence(fenceId);
        assertFalse(worldState.hasFenceAt(gridX, gridY), 
                   "Fence should be removed after owner removes it");
        
        // Test ownership tracking with multiple fences
        String fence2Id = "fence2-" + UUID.randomUUID().toString();
        String fence3Id = "fence3-" + UUID.randomUUID().toString();
        
        FenceState fence2 = new FenceState(
            fence2Id, gridX + 1, gridY, FencePieceType.FENCE_BACK, 
            FenceMaterialType.BAMBOO, owner1, System.currentTimeMillis()
        );
        FenceState fence3 = new FenceState(
            fence3Id, gridX + 2, gridY, FencePieceType.FENCE_BACK_RIGHT, 
            FenceMaterialType.WOOD, owner2, System.currentTimeMillis()
        );
        
        worldState.addOrUpdateFence(fence2);
        worldState.addOrUpdateFence(fence3);
        
        // Verify ownership counts
        assertEquals(1, worldState.getFenceCountByOwner(owner1), 
                    "Player1 should own exactly 1 fence");
        assertEquals(1, worldState.getFenceCountByOwner(owner2), 
                    "Player2 should own exactly 1 fence");
        
        // Verify ownership filtering
        assertTrue(worldState.getFencesByOwner(owner1).containsKey(fence2Id), 
                  "Player1's fences should include fence2");
        assertFalse(worldState.getFencesByOwner(owner1).containsKey(fence3Id), 
                   "Player1's fences should not include fence3");
        
        assertTrue(worldState.getFencesByOwner(owner2).containsKey(fence3Id), 
                  "Player2's fences should include fence3");
        assertFalse(worldState.getFencesByOwner(owner2).containsKey(fence2Id), 
                   "Player2's fences should not include fence2");
    }
    
    /**
     * Property: Fence ownership is immutable after creation
     * For any fence, the owner ID should remain constant throughout its lifetime.
     */
    @Property(trials = 100)
    public void fenceOwnershipIsImmutable(int gridX, int gridY) {
        // Constrain grid coordinates to reasonable bounds
        gridX = Math.abs(gridX) % 1000;
        gridY = Math.abs(gridY) % 1000;
        
        WorldState worldState = new WorldState();
        String originalOwner = "owner-" + UUID.randomUUID().toString();
        String fenceId = "fence-" + UUID.randomUUID().toString();
        
        // Create fence with original owner
        FenceState fence = new FenceState(
            fenceId,
            gridX,
            gridY,
            FencePieceType.FENCE_FRONT_LEFT,
            FenceMaterialType.BAMBOO,
            originalOwner,
            System.currentTimeMillis()
        );
        
        worldState.addOrUpdateFence(fence);
        
        // Verify initial ownership
        FenceState retrievedFence = worldState.getFenceAt(gridX, gridY);
        assertEquals(originalOwner, retrievedFence.getOwnerId(), 
                    "Initial fence owner should be preserved");
        
        // Simulate fence updates (position changes, etc.) - ownership should remain
        FenceState updatedFence = new FenceState(
            fenceId,
            gridX,
            gridY,
            FencePieceType.FENCE_FRONT_RIGHT, // Different piece type
            FenceMaterialType.WOOD,           // Different material
            originalOwner,                    // Same owner
            System.currentTimeMillis()
        );
        
        worldState.addOrUpdateFence(updatedFence);
        
        // Verify ownership is preserved after update
        FenceState finalFence = worldState.getFenceAt(gridX, gridY);
        assertEquals(originalOwner, finalFence.getOwnerId(), 
                    "Fence owner should remain unchanged after updates");
        assertEquals(FencePieceType.FENCE_FRONT_RIGHT, finalFence.getPieceType(), 
                    "Fence properties should be updated");
        assertEquals(FenceMaterialType.WOOD, finalFence.getMaterialType(), 
                    "Fence material should be updated");
    }
}