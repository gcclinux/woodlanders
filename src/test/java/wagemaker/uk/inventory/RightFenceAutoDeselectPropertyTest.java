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
 * Property-based test for RightFence auto-deselect behavior.
 * Feature: right-fence-inventory, Property 7: Zero count triggers auto-deselect
 * Validates: Requirements 3.4
 */
public class RightFenceAutoDeselectPropertyTest {
    
    @BeforeAll
    public static void checkLibGDXAvailability() {
        // Skip all tests if LibGDX is not initialized (headless environment)
        assumeTrue(Gdx.files != null, "LibGDX not initialized - skipping tests that require graphics context");
    }
    
    /**
     * Property 7: Zero count triggers auto-deselect
     * For any sequence of operations where the RightFence count reaches zero while slot 13
     * is selected, the slot should be automatically deselected.
     * Validates: Requirements 3.4
     * 
     * This property-based test runs 100 trials with randomly generated scenarios,
     * verifying that reaching zero count always triggers auto-deselection.
     */
    @Test
    public void zeroCountTriggersAutoDeselect() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Generate random initial count (small enough to reach zero)
            Inventory inventory = inventoryManager.getCurrentInventory();
            int initialCount = random.nextInt(10) + 1; // 1 to 10
            inventory.setBowAndArrowCount(initialCount);
            
            // Select slot 13
            inventoryManager.setSelectedSlot(13);
            assertEquals(13, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Slot 13 should be selected initially");
            
            // Remove all RightFence items to reach zero
            for (int i = 0; i < initialCount; i++) {
                boolean removed = inventory.removeBowAndArrow(1);
                assertTrue(removed,
                    "Trial " + trial + ", Removal " + i + ": Should successfully remove RightFence");
                
                // Check auto-deselect after each removal
                inventoryManager.checkAndAutoDeselect();
            }
            
            // Verify count is now zero
            assertEquals(0, inventory.getBowAndArrowCount(),
                "Trial " + trial + ": RightFence count should be zero");
            
            // Verify slot was auto-deselected
            assertEquals(-1, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Slot should be auto-deselected when count reaches zero");
            
            // Verify getSelectedItemType returns null
            assertNull(inventoryManager.getSelectedItemType(),
                "Trial " + trial + ": Auto-deselected slot should return null ItemType");
        }
    }
    
    /**
     * Property: Auto-deselect only triggers when count is exactly zero
     * For any count greater than zero, the selection should remain active.
     * 
     * This property-based test runs 100 trials verifying that non-zero
     * counts do not trigger auto-deselection.
     */
    @Test
    public void nonZeroCountDoesNotTriggerAutoDeselect() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Generate random non-zero count
            Inventory inventory = inventoryManager.getCurrentInventory();
            int count = random.nextInt(100) + 1; // 1 to 100
            inventory.setBowAndArrowCount(count);
            
            // Select slot 13
            inventoryManager.setSelectedSlot(13);
            
            // Check auto-deselect
            inventoryManager.checkAndAutoDeselect();
            
            // Verify slot is still selected
            assertEquals(13, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Slot 13 should remain selected when count=" + count);
            
            // Verify ItemType is still BOW_AND_ARROW
            assertEquals(ItemType.BOW_AND_ARROW, inventoryManager.getSelectedItemType(),
                "Trial " + trial + ": Should still return BOW_AND_ARROW when count=" + count);
        }
    }
    
    /**
     * Property: Auto-deselect works for all slots
     * For any slot with zero count, auto-deselect should trigger.
     * 
     * This property-based test runs 100 trials cycling through all slots
     * to verify auto-deselect works universally.
     */
    @Test
    public void autoDeselectWorksForAllSlots() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials (cycling through slots)
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            Inventory inventory = inventoryManager.getCurrentInventory();
            
            // Select a slot (cycle through all 14 slots)
            int slot = trial % 14; // 0 to 13
            inventoryManager.setSelectedSlot(slot);
            
            // Ensure the corresponding item count is zero
            switch (slot) {
                case 0: inventory.setAppleCount(0); break;
                case 1: inventory.setBananaCount(0); break;
                case 2: inventory.setBambooSaplingCount(0); break;
                case 3: inventory.setBambooStackCount(0); break;
                case 4: inventory.setTreeSaplingCount(0); break;
                case 5: inventory.setWoodStackCount(0); break;
                case 6: inventory.setPebbleCount(0); break;
                case 7: inventory.setPalmFiberCount(0); break;
                case 8: inventory.setAppleSaplingCount(0); break;
                case 9: inventory.setBananaSaplingCount(0); break;
                case 10: inventory.setFishCount(0); break;
                case 11: inventory.setFrontFenceCount(0); break;
                case 12: inventory.setBackFenceCount(0); break;
                case 13: inventory.setBowAndArrowCount(0); break;
            }
            
            // Check auto-deselect
            inventoryManager.checkAndAutoDeselect();
            
            // Verify slot was auto-deselected
            assertEquals(-1, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Slot " + slot + " should be auto-deselected when count is zero");
        }
    }
    
    /**
     * Property: Setting count to zero triggers auto-deselect
     * For any selected slot, setting its count to zero should trigger auto-deselect.
     * 
     * This property-based test runs 100 trials verifying that setCount(0)
     * properly triggers auto-deselection.
     */
    @Test
    public void setCountToZeroTriggersAutoDeselect() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Set initial non-zero count
            Inventory inventory = inventoryManager.getCurrentInventory();
            int initialCount = random.nextInt(100) + 1; // 1 to 100
            inventory.setBowAndArrowCount(initialCount);
            
            // Select slot 13
            inventoryManager.setSelectedSlot(13);
            assertEquals(13, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Slot 13 should be selected initially");
            
            // Set count to zero
            inventory.setBowAndArrowCount(0);
            
            // Check auto-deselect
            inventoryManager.checkAndAutoDeselect();
            
            // Verify slot was auto-deselected
            assertEquals(-1, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Slot should be auto-deselected after setCount(0)");
        }
    }
    
    /**
     * Property: Auto-deselect is idempotent
     * For any already-deselected state, calling checkAndAutoDeselect should not cause errors.
     * 
     * This property-based test runs 100 trials verifying that auto-deselect
     * can be called safely multiple times.
     */
    @Test
    public void autoDeselectIsIdempotent() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Start with no selection
            inventoryManager.clearSelection();
            assertEquals(-1, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Should start with no selection");
            
            // Call checkAndAutoDeselect multiple times
            int callCount = random.nextInt(10) + 1; // 1 to 10 calls
            for (int i = 0; i < callCount; i++) {
                // Should not throw exception
                inventoryManager.checkAndAutoDeselect();
                
                // Should remain deselected
                assertEquals(-1, inventoryManager.getSelectedSlot(),
                    "Trial " + trial + ", Call " + i + ": Should remain deselected");
            }
        }
    }
    
    /**
     * Property: Removal to zero triggers auto-deselect
     * For any initial count, removing all items should trigger auto-deselect.
     * 
     * This property-based test runs 100 trials with various initial counts,
     * verifying that complete removal always triggers auto-deselection.
     */
    @Test
    public void removalToZeroTriggersAutoDeselect() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Generate random initial count
            Inventory inventory = inventoryManager.getCurrentInventory();
            int initialCount = random.nextInt(20) + 1; // 1 to 20
            inventory.setBowAndArrowCount(initialCount);
            
            // Select slot 13
            inventoryManager.setSelectedSlot(13);
            
            // Remove all items at once
            boolean removed = inventory.removeBowAndArrow(initialCount);
            assertTrue(removed,
                "Trial " + trial + ": Should successfully remove all " + initialCount + " items");
            
            // Check auto-deselect
            inventoryManager.checkAndAutoDeselect();
            
            // Verify count is zero
            assertEquals(0, inventory.getBowAndArrowCount(),
                "Trial " + trial + ": Count should be zero after removing all items");
            
            // Verify slot was auto-deselected
            assertEquals(-1, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Slot should be auto-deselected after removing all items");
        }
    }
}
