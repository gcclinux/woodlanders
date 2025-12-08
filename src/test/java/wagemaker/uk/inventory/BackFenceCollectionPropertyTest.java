package wagemaker.uk.inventory;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Property-based test for BackFence collection behavior.
 * 
 * Feature: back-fence-inventory, Property 1: BackFence collection increases count
 * Validates: Requirements 1.1
 */
@RunWith(JUnitQuickcheck.class)
public class BackFenceCollectionPropertyTest {
    
    /**
     * Property 1: BackFence collection increases count
     * For any initial BackFence count, when a player collects a BackFence item,
     * the resulting count should be exactly one greater than the initial count.
     */
    @Property(trials = 100)
    public void backFenceCollectionIncreasesCount(int initialCount) {
        // Ensure initialCount is non-negative (valid inventory state)
        if (initialCount < 0) {
            initialCount = 0;
        }
        
        // Create inventory and set initial state
        Inventory inventory = new Inventory();
        inventory.setBackFenceCount(initialCount);
        
        // Simulate collecting one BackFence item
        inventory.addBackFence(1);
        
        // Verify count increased by exactly 1
        int expectedCount = initialCount + 1;
        int actualCount = inventory.getBackFenceCount();
        
        assertEquals(expectedCount, actualCount, 
                     "BackFence count should increase by 1 after collection");
    }
}
