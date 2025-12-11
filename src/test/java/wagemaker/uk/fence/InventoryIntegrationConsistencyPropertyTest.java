package wagemaker.uk.fence;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import wagemaker.uk.inventory.Inventory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for fence material inventory integration consistency.
 * 
 * **Feature: custom-fence-building, Property 10: Inventory integration consistency**
 * **Validates: Requirements 12.1, 12.2, 12.4**
 */
@RunWith(JUnitQuickcheck.class)
public class InventoryIntegrationConsistencyPropertyTest {
    
    /**
     * Property 10: Inventory integration consistency
     * For any fence material operation, the system should use standard inventory operations 
     * and trigger appropriate UI updates.
     */
    @Property(trials = 100)
    public void fenceMaterialInventoryIntegrationConsistency(
            @InRange(min = "0", max = "100") int initialWoodCount,
            @InRange(min = "0", max = "100") int initialBambooCount,
            @InRange(min = "1", max = "10") int addAmount) {
        
        // Create inventory directly to test fence material integration
        Inventory inventory = new Inventory();
        
        // Set initial fence material counts
        inventory.setWoodFenceMaterialCount(initialWoodCount);
        inventory.setBambooFenceMaterialCount(initialBambooCount);
        
        // Test direct inventory operations work correctly
        inventory.addWoodFenceMaterial(addAmount);
        assertEquals(initialWoodCount + addAmount, inventory.getWoodFenceMaterialCount(),
                "Direct wood fence material addition should work consistently");
        
        inventory.addBambooFenceMaterial(addAmount);
        assertEquals(initialBambooCount + addAmount, inventory.getBambooFenceMaterialCount(),
                "Direct bamboo fence material addition should work consistently");
        
        // Test removal operations work correctly
        boolean woodRemoved = inventory.removeWoodFenceMaterial(1);
        assertTrue(woodRemoved, "Wood fence material removal should succeed when materials are available");
        assertEquals(initialWoodCount + addAmount - 1, inventory.getWoodFenceMaterialCount(),
                "Wood fence material count should decrease after removal");
        
        boolean bambooRemoved = inventory.removeBambooFenceMaterial(1);
        assertTrue(bambooRemoved, "Bamboo fence material removal should succeed when materials are available");
        assertEquals(initialBambooCount + addAmount - 1, inventory.getBambooFenceMaterialCount(),
                "Bamboo fence material count should decrease after removal");
        
        // Test that removal fails when insufficient materials
        int currentWoodCount = inventory.getWoodFenceMaterialCount();
        int currentBambooCount = inventory.getBambooFenceMaterialCount();
        
        boolean excessiveWoodRemoval = inventory.removeWoodFenceMaterial(currentWoodCount + 1);
        assertFalse(excessiveWoodRemoval, "Wood fence material removal should fail when insufficient materials");
        assertEquals(currentWoodCount, inventory.getWoodFenceMaterialCount(),
                "Wood fence material count should remain unchanged after failed removal");
        
        boolean excessiveBambooRemoval = inventory.removeBambooFenceMaterial(currentBambooCount + 1);
        assertFalse(excessiveBambooRemoval, "Bamboo fence material removal should fail when insufficient materials");
        assertEquals(currentBambooCount, inventory.getBambooFenceMaterialCount(),
                "Bamboo fence material count should remain unchanged after failed removal");
        
        // Test that counts never go negative
        inventory.setWoodFenceMaterialCount(-5);
        assertEquals(0, inventory.getWoodFenceMaterialCount(),
                "Wood fence material count should never be negative");
        
        inventory.setBambooFenceMaterialCount(-3);
        assertEquals(0, inventory.getBambooFenceMaterialCount(),
                "Bamboo fence material count should never be negative");
        
        // Test that fence materials integrate with inventory clear operation
        inventory.clear();
        assertEquals(0, inventory.getWoodFenceMaterialCount(),
                "Wood fence material count should be 0 after inventory clear");
        assertEquals(0, inventory.getBambooFenceMaterialCount(),
                "Bamboo fence material count should be 0 after inventory clear");
    }
}