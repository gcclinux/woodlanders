package wagemaker.uk.inventory;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for BackFence slot selection behavior.
 * 
 * Feature: back-fence-inventory
 * Tests Properties 6 and 7 related to slot selection
 */
@RunWith(JUnitQuickcheck.class)
public class BackFenceSlotSelectionPropertyTest {
    
    /**
     * Property 6: Slot 12 selection returns BACK_FENCE
     * For any inventory state where slot 12 is selected,
     * calling getSelectedItemType() should return ItemType.BACK_FENCE.
     * 
     * Validates: Requirements 3.2
     */
    @Property(trials = 100)
    public void slot12SelectionReturnsBackFence(int backFenceCount) {
        // Ensure backFenceCount is non-negative
        if (backFenceCount < 0) {
            backFenceCount = 0;
        }
        
        // Create inventory manager with null player (player not needed for this test)
        InventoryManager inventoryManager = new InventoryManager(null);
        
        // Set BackFence count (any valid count)
        inventoryManager.getCurrentInventory().setBackFenceCount(backFenceCount);
        
        // Select slot 12
        inventoryManager.setSelectedSlot(12);
        
        // Verify that getSelectedItemType returns BACK_FENCE
        ItemType selectedType = inventoryManager.getSelectedItemType();
        assertEquals(ItemType.BACK_FENCE, selectedType,
                     "Slot 12 selection should return ItemType.BACK_FENCE");
        
        // Verify the slot is actually selected
        assertEquals(12, inventoryManager.getSelectedSlot(),
                     "Selected slot should be 12");
    }
    
    /**
     * Property 7: Zero count triggers auto-deselect
     * For any sequence of operations where the BackFence count reaches zero
     * while slot 12 is selected, the slot should be automatically deselected.
     * 
     * Validates: Requirements 3.4
     */
    @Property(trials = 100)
    public void zeroCountTriggersAutoDeselect(int initialCount) {
        // Ensure initialCount is positive (need at least 1 to test removal to zero)
        if (initialCount <= 0) {
            initialCount = 1;
        }
        
        // Create inventory manager with null player (player not needed for this test)
        InventoryManager inventoryManager = new InventoryManager(null);
        
        // Set initial BackFence count
        inventoryManager.getCurrentInventory().setBackFenceCount(initialCount);
        
        // Select slot 12
        inventoryManager.setSelectedSlot(12);
        
        // Verify slot is selected
        assertEquals(12, inventoryManager.getSelectedSlot(),
                     "Slot 12 should be selected initially");
        
        // Remove all BackFence items to reach zero count
        inventoryManager.getCurrentInventory().removeBackFence(initialCount);
        
        // Trigger auto-deselect check
        inventoryManager.checkAndAutoDeselect();
        
        // Verify slot is auto-deselected
        assertEquals(-1, inventoryManager.getSelectedSlot(),
                     "Slot should be auto-deselected when BackFence count reaches zero");
        
        // Verify count is actually zero
        assertEquals(0, inventoryManager.getCurrentInventory().getBackFenceCount(),
                     "BackFence count should be zero");
    }
}
