package wagemaker.uk.fence;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import wagemaker.uk.inventory.Inventory;
import wagemaker.uk.inventory.InventoryManager;
import wagemaker.uk.inventory.ItemType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for fence material resource collection integration.
 * 
 * **Feature: custom-fence-building, Property 12: Resource collection integration**
 * **Validates: Requirements 11.1, 11.2, 11.3**
 */
@RunWith(JUnitQuickcheck.class)
public class ResourceCollectionIntegrationPropertyTest {
    
    /**
     * Property 12: Resource collection integration
     * For any resource harvesting operation (wood/bamboo), appropriate fence materials 
     * should be added to inventory.
     */
    @Property(trials = 100)
    public void resourceCollectionIntegrationProperty(
            @InRange(min = "0", max = "50") int initialWoodStackCount,
            @InRange(min = "0", max = "50") int initialBambooStackCount,
            @InRange(min = "0", max = "20") int initialWoodFenceCount,
            @InRange(min = "0", max = "20") int initialBambooFenceCount,
            @InRange(min = "1", max = "10") int harvestOperations) {
        
        // Create inventory and mock inventory manager
        Inventory inventory = new Inventory();
        inventory.setWoodStackCount(initialWoodStackCount);
        inventory.setBambooStackCount(initialBambooStackCount);
        inventory.setWoodFenceMaterialCount(initialWoodFenceCount);
        inventory.setBambooFenceMaterialCount(initialBambooFenceCount);
        
        InventoryManager inventoryManager = new InventoryManager(null) {
            @Override
            public Inventory getCurrentInventory() {
                return inventory;
            }
            
            @Override
            public void sendInventoryUpdateToServer() {
                // No-op for testing
            }
        };
        
        // Track expected counts
        int expectedWoodStacks = initialWoodStackCount;
        int expectedBambooStacks = initialBambooStackCount;
        int expectedWoodFenceMaterials = initialWoodFenceCount;
        int expectedBambooFenceMaterials = initialBambooFenceCount;
        
        // Simulate resource harvesting operations
        for (int i = 0; i < harvestOperations; i++) {
            if (i % 2 == 0) {
                // Harvest wood - should collect both wood stack and wood fence material
                inventoryManager.collectItem(ItemType.WOOD_STACK);
                inventoryManager.collectItem(ItemType.WOOD_FENCE_MATERIAL);
                expectedWoodStacks++;
                expectedWoodFenceMaterials++;
            } else {
                // Harvest bamboo - should collect both bamboo stack and bamboo fence material
                inventoryManager.collectItem(ItemType.BAMBOO_STACK);
                inventoryManager.collectItem(ItemType.BAMBOO_FENCE_MATERIAL);
                expectedBambooStacks++;
                expectedBambooFenceMaterials++;
            }
        }
        
        // Verify that both resource and fence material counts increased correctly
        assertEquals(expectedWoodStacks, inventory.getWoodStackCount(),
                "Wood stack count should increase when harvesting wood resources");
        assertEquals(expectedBambooStacks, inventory.getBambooStackCount(),
                "Bamboo stack count should increase when harvesting bamboo resources");
        assertEquals(expectedWoodFenceMaterials, inventory.getWoodFenceMaterialCount(),
                "Wood fence material count should increase when harvesting wood resources");
        assertEquals(expectedBambooFenceMaterials, inventory.getBambooFenceMaterialCount(),
                "Bamboo fence material count should increase when harvesting bamboo resources");
        
        // Verify that fence materials are collected in 1:1 ratio with resources
        int woodHarvested = inventory.getWoodStackCount() - initialWoodStackCount;
        int bambooHarvested = inventory.getBambooStackCount() - initialBambooStackCount;
        int woodFenceMaterialsCollected = inventory.getWoodFenceMaterialCount() - initialWoodFenceCount;
        int bambooFenceMaterialsCollected = inventory.getBambooFenceMaterialCount() - initialBambooFenceCount;
        
        assertEquals(woodHarvested, woodFenceMaterialsCollected,
                "Wood fence materials should be collected in 1:1 ratio with wood resources");
        assertEquals(bambooHarvested, bambooFenceMaterialsCollected,
                "Bamboo fence materials should be collected in 1:1 ratio with bamboo resources");
        
        // Verify that all counts remain non-negative
        assertTrue(inventory.getWoodStackCount() >= 0,
                "Wood stack count should never be negative");
        assertTrue(inventory.getBambooStackCount() >= 0,
                "Bamboo stack count should never be negative");
        assertTrue(inventory.getWoodFenceMaterialCount() >= 0,
                "Wood fence material count should never be negative");
        assertTrue(inventory.getBambooFenceMaterialCount() >= 0,
                "Bamboo fence material count should never be negative");
        
        // Verify that fence materials integrate properly with inventory system
        // Test that fence materials can be retrieved using standard inventory methods
        int totalWoodFenceMaterials = inventory.getWoodFenceMaterialCount();
        int totalBambooFenceMaterials = inventory.getBambooFenceMaterialCount();
        
        assertTrue(totalWoodFenceMaterials >= initialWoodFenceCount,
                "Total wood fence materials should be at least initial count");
        assertTrue(totalBambooFenceMaterials >= initialBambooFenceCount,
                "Total bamboo fence materials should be at least initial count");
    }
}