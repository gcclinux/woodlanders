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
 * Property-based test for RightFence slot selection.
 * Feature: right-fence-inventory, Property 6: Slot 13 selection returns RIGHT_FENCE
 * Validates: Requirements 3.2
 */
public class RightFenceSlotSelectionPropertyTest {
    
    @BeforeAll
    public static void checkLibGDXAvailability() {
        // Skip all tests if LibGDX is not initialized (headless environment)
        assumeTrue(Gdx.files != null, "LibGDX not initialized - skipping tests that require graphics context");
    }
    
    /**
     * Property 6: Slot 13 selection returns RIGHT_FENCE
     * For any inventory state where slot 13 is selected, calling getSelectedItemType()
     * should return ItemType.RIGHT_FENCE.
     * Validates: Requirements 3.2
     * 
     * This property-based test runs 100 trials with randomly generated inventory states,
     * verifying that slot 13 always returns RIGHT_FENCE.
     */
    @Test
    public void slot13SelectionReturnsRightFence() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Generate random inventory state
            Inventory inventory = inventoryManager.getCurrentInventory();
            inventory.setRightFenceCount(random.nextInt(100));
            inventory.setAppleCount(random.nextInt(50));
            inventory.setBananaCount(random.nextInt(50));
            inventory.setLeftFenceCount(random.nextInt(20));
            inventory.setFrontFenceCount(random.nextInt(20));
            inventory.setBackFenceCount(random.nextInt(20));
            
            // Select slot 13
            inventoryManager.setSelectedSlot(13);
            
            // Verify slot 13 is selected
            assertEquals(13, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Selected slot should be 13");
            
            // Verify getSelectedItemType returns RIGHT_FENCE
            ItemType selectedType = inventoryManager.getSelectedItemType();
            assertEquals(ItemType.RIGHT_FENCE, selectedType,
                "Trial " + trial + ": Slot 13 should return ItemType.RIGHT_FENCE");
        }
    }
    
    /**
     * Property: Slot 13 is valid and does not clear selection
     * For any attempt to select slot 13, the selection should succeed
     * and not be cleared.
     * 
     * This property-based test runs 100 trials verifying that slot 13
     * is a valid selectable slot.
     */
    @Test
    public void slot13IsValidSelection() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Start with a different slot selected
            int initialSlot = random.nextInt(13); // 0 to 12
            inventoryManager.setSelectedSlot(initialSlot);
            
            // Select slot 13
            inventoryManager.setSelectedSlot(13);
            
            // Verify slot 13 is now selected (not cleared to -1)
            assertEquals(13, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Slot 13 should be a valid selection");
            
            // Verify it returns RIGHT_FENCE
            assertEquals(ItemType.RIGHT_FENCE, inventoryManager.getSelectedItemType(),
                "Trial " + trial + ": Slot 13 should return RIGHT_FENCE");
        }
    }
    
    /**
     * Property: All slots return correct item types
     * For any slot from 0 to 13, the correct ItemType should be returned.
     * 
     * This property-based test runs 100 trials cycling through all slots
     * to verify the complete slot-to-ItemType mapping.
     */
    @Test
    public void allSlotsReturnCorrectItemTypes() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Expected mapping
        ItemType[] expectedTypes = {
            ItemType.APPLE,           // Slot 0
            ItemType.BANANA,          // Slot 1
            ItemType.BABY_BAMBOO,     // Slot 2
            ItemType.BAMBOO_STACK,    // Slot 3
            ItemType.BABY_TREE,       // Slot 4
            ItemType.WOOD_STACK,      // Slot 5
            ItemType.PEBBLE,          // Slot 6
            ItemType.PALM_FIBER,      // Slot 7
            ItemType.APPLE_SAPLING,   // Slot 8
            ItemType.BANANA_SAPLING,  // Slot 9
            ItemType.LEFT_FENCE,      // Slot 10
            ItemType.FRONT_FENCE,     // Slot 11
            ItemType.BACK_FENCE,      // Slot 12
            ItemType.RIGHT_FENCE      // Slot 13
        };
        
        // Run 100 trials (cycling through slots)
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Select a slot (cycle through all slots)
            int slot = trial % 14; // 0 to 13
            inventoryManager.setSelectedSlot(slot);
            
            // Verify the correct ItemType is returned
            ItemType selectedType = inventoryManager.getSelectedItemType();
            assertEquals(expectedTypes[slot], selectedType,
                "Trial " + trial + ": Slot " + slot + " should return " + expectedTypes[slot]);
        }
    }
    
    /**
     * Property: Invalid slots clear selection
     * For any slot number outside the valid range (0-13), the selection
     * should be cleared (set to -1) and getSelectedItemType should return null.
     * 
     * This property-based test runs 100 trials with invalid slot numbers
     * to verify proper boundary handling.
     */
    @Test
    public void invalidSlotsClearSelection() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // First select a valid slot
            inventoryManager.setSelectedSlot(13);
            assertEquals(13, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Initial selection should be slot 13");
            
            // Generate an invalid slot number (outside 0-13)
            int invalidSlot;
            if (random.nextBoolean()) {
                // Negative number
                invalidSlot = -(random.nextInt(100) + 1); // -1 to -100
            } else {
                // Number > 13
                invalidSlot = 14 + random.nextInt(100); // 14 to 113
            }
            
            // Attempt to select invalid slot
            inventoryManager.setSelectedSlot(invalidSlot);
            
            // Verify selection was cleared
            assertEquals(-1, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Invalid slot " + invalidSlot + " should clear selection");
            
            // Verify getSelectedItemType returns null
            assertNull(inventoryManager.getSelectedItemType(),
                "Trial " + trial + ": Cleared selection should return null ItemType");
        }
    }
    
    /**
     * Property: Slot 13 selection persists across operations
     * For any sequence of inventory operations, if slot 13 is selected,
     * it should remain selected (unless explicitly changed or auto-deselected).
     * 
     * This property-based test runs 100 trials with random inventory
     * operations to verify selection persistence.
     */
    @Test
    public void slot13SelectionPersistsAcrossOperations() {
        Random random = new Random(42); // Fixed seed for reproducibility
        
        // Run 100 trials
        for (int trial = 0; trial < 100; trial++) {
            // Create mock player and inventory manager
            Player mockPlayer = mock(Player.class);
            InventoryManager inventoryManager = new InventoryManager(mockPlayer);
            
            // Set initial RightFence count (non-zero to avoid auto-deselect)
            Inventory inventory = inventoryManager.getCurrentInventory();
            inventory.setRightFenceCount(random.nextInt(50) + 10); // 10 to 59
            
            // Select slot 13
            inventoryManager.setSelectedSlot(13);
            
            // Perform random inventory operations
            int operationCount = random.nextInt(10) + 1; // 1 to 10 operations
            for (int op = 0; op < operationCount; op++) {
                int operation = random.nextInt(3);
                switch (operation) {
                    case 0:
                        // Add items to other slots
                        inventory.setAppleCount(random.nextInt(50));
                        break;
                    case 1:
                        // Add RightFence items
                        inventory.addRightFence(random.nextInt(10) + 1);
                        break;
                    case 2:
                        // Query inventory state
                        int count = inventory.getRightFenceCount();
                        assertTrue(count > 0, "RightFence count should remain positive");
                        break;
                }
            }
            
            // Verify slot 13 is still selected
            assertEquals(13, inventoryManager.getSelectedSlot(),
                "Trial " + trial + ": Slot 13 should remain selected after operations");
            
            // Verify it still returns RIGHT_FENCE
            assertEquals(ItemType.RIGHT_FENCE, inventoryManager.getSelectedItemType(),
                "Trial " + trial + ": Slot 13 should still return RIGHT_FENCE");
        }
    }
}
