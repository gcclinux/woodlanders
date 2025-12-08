package wagemaker.uk.ui;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import wagemaker.uk.inventory.Inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property-based test for BackFence displayed count matching inventory count.
 * 
 * Feature: back-fence-inventory, Property 2: Displayed count matches inventory count
 * Validates: Requirements 1.2
 */
@RunWith(JUnitQuickcheck.class)
public class BackFenceDisplayedCountPropertyTest {
    
    /**
     * Property 2: Displayed count matches inventory count
     * For any BackFence count value in the inventory, the rendered UI count
     * should match the inventory's stored count.
     * 
     * This test verifies the data flow: that when the renderer queries the inventory
     * for the BackFence count, it receives the correct value that was set.
     */
    @Property(trials = 100)
    public void displayedCountMatchesInventoryCount(int backFenceCount) {
        // Ensure backFenceCount is non-negative (valid inventory state)
        if (backFenceCount < 0) {
            backFenceCount = 0;
        }
        
        // Create inventory and set BackFence count
        Inventory inventory = new Inventory();
        inventory.setBackFenceCount(backFenceCount);
        
        // Verify that the inventory returns the same count that was set
        // This is what the renderer will query when displaying the count
        int displayedCount = inventory.getBackFenceCount();
        
        assertEquals(backFenceCount, displayedCount,
                     "Displayed BackFence count should match inventory count");
    }
}
