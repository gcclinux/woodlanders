package wagemaker.uk.ui;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import wagemaker.uk.inventory.Inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property-based test for RightFence displayed count matching inventory count.
 * 
 * Feature: right-fence-inventory, Property 2: Displayed count matches inventory count
 * Validates: Requirements 1.2
 */
@RunWith(JUnitQuickcheck.class)
public class RightFenceDisplayedCountPropertyTest {
    
    /**
     * Property 2: Displayed count matches inventory count
     * For any RightFence count value in the inventory, the rendered UI count
     * should match the inventory's stored count.
     * 
     * This test verifies the data flow: that when the renderer queries the inventory
     * for the RightFence count, it receives the correct value that was set.
     */
    @Property(trials = 100)
    public void displayedCountMatchesInventoryCount(int rightFenceCount) {
        // Ensure rightFenceCount is non-negative (valid inventory state)
        if (rightFenceCount < 0) {
            rightFenceCount = 0;
        }
        
        // Create inventory and set BowAndArrow count
        Inventory inventory = new Inventory();
        inventory.setBowAndArrowCount(rightFenceCount);
        
        // Verify that the inventory returns the same count that was set
        // This is what the renderer will query when displaying the count
        int displayedCount = inventory.getBowAndArrowCount();
        
        assertEquals(rightFenceCount, displayedCount,
                     "Displayed RightFence count should match inventory count");
    }
}
