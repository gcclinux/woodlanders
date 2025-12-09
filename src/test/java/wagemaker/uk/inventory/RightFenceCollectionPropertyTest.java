package wagemaker.uk.inventory;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property-based test for RightFence collection behavior.
 * 
 * Feature: right-fence-inventory, Property 1: RightFence collection increases count
 * Validates: Requirements 1.1
 */
@RunWith(JUnitQuickcheck.class)
public class RightFenceCollectionPropertyTest {
    
    /**
     * Property 1: RightFence collection increases count
     * For any initial RightFence count, when a player collects a RightFence item,
     * the resulting count should be exactly one greater than the initial count.
     */
    @Property(trials = 100)
    public void rightFenceCollectionIncreasesCount(int initialCount) {
        // Ensure initialCount is non-negative (valid inventory state)
        if (initialCount < 0) {
            initialCount = 0;
        }
        
        // Create inventory and set initial state
        Inventory inventory = new Inventory();
        inventory.setRightFenceCount(initialCount);
        
        // Simulate collecting one RightFence item
        inventory.addRightFence(1);
        
        // Verify count increased by exactly 1
        int expectedCount = initialCount + 1;
        int actualCount = inventory.getRightFenceCount();
        
        assertEquals(expectedCount, actualCount, 
                     "RightFence count should increase by 1 after collection");
    }
}
