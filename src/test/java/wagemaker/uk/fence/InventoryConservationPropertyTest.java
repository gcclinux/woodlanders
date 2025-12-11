package wagemaker.uk.fence;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import wagemaker.uk.inventory.Inventory;
import wagemaker.uk.inventory.InventoryManager;
import wagemaker.uk.player.Player;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for fence material inventory conservation.
 * 
 * **Feature: custom-fence-building, Property 4: Inventory conservation**
 * **Validates: Requirements 2.3, 3.1, 3.3, 11.4, 11.5**
 */
@RunWith(JUnitQuickcheck.class)
public class InventoryConservationPropertyTest {
    
    /**
     * Property 4: Inventory conservation
     * For any sequence of fence placement and removal operations, the total materials 
     * in inventory plus materials used in placed fences should remain constant.
     */
    @Property(trials = 100)
    public void inventoryConservationProperty(
            @InRange(min = "0", max = "50") int initialWoodCount,
            @InRange(min = "0", max = "50") int initialBambooCount,
            @InRange(min = "1", max = "10") int operationCount) {
        
        // Create inventory and material provider
        Inventory inventory = new Inventory();
        inventory.setWoodFenceMaterialCount(initialWoodCount);
        inventory.setBambooFenceMaterialCount(initialBambooCount);
        
        // Create a mock inventory manager for testing
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
        
        FenceMaterialProvider materialProvider = new InventoryFenceMaterialProvider(inventoryManager);
        
        // Track total materials at start
        int totalWoodStart = initialWoodCount;
        int totalBambooStart = initialBambooCount;
        
        // Simulate fence operations (placement and removal)
        int woodUsedInFences = 0;
        int bambooUsedInFences = 0;
        
        for (int i = 0; i < operationCount; i++) {
            // Alternate between wood and bamboo operations
            FenceMaterialType materialType = (i % 2 == 0) ? FenceMaterialType.WOOD : FenceMaterialType.BAMBOO;
            
            // Determine operation type based on current state
            boolean canPlace = materialProvider.hasEnoughMaterials(materialType, 1);
            boolean canRemove = (materialType == FenceMaterialType.WOOD) ? 
                    woodUsedInFences > 0 : bambooUsedInFences > 0;
            
            if (canPlace && (!canRemove || i % 3 == 0)) {
                // Place fence (consume materials)
                materialProvider.consumeMaterials(materialType, 1);
                if (materialType == FenceMaterialType.WOOD) {
                    woodUsedInFences++;
                } else {
                    bambooUsedInFences++;
                }
            } else if (canRemove) {
                // Remove fence (return materials)
                materialProvider.returnMaterials(materialType, 1);
                if (materialType == FenceMaterialType.WOOD) {
                    woodUsedInFences--;
                } else {
                    bambooUsedInFences--;
                }
            }
        }
        
        // Verify conservation: total materials should remain constant
        int totalWoodEnd = inventory.getWoodFenceMaterialCount() + woodUsedInFences;
        int totalBambooEnd = inventory.getBambooFenceMaterialCount() + bambooUsedInFences;
        
        assertEquals(totalWoodStart, totalWoodEnd,
                "Total wood materials (inventory + used in fences) should remain constant");
        assertEquals(totalBambooStart, totalBambooEnd,
                "Total bamboo materials (inventory + used in fences) should remain constant");
        
        // Verify that inventory counts are non-negative
        assertTrue(inventory.getWoodFenceMaterialCount() >= 0,
                "Wood fence material count should never be negative");
        assertTrue(inventory.getBambooFenceMaterialCount() >= 0,
                "Bamboo fence material count should never be negative");
        
        // Verify that used materials counts are non-negative
        assertTrue(woodUsedInFences >= 0,
                "Wood materials used in fences should never be negative");
        assertTrue(bambooUsedInFences >= 0,
                "Bamboo materials used in fences should never be negative");
        
        // Verify that material provider reports correct counts
        assertEquals(inventory.getWoodFenceMaterialCount(), 
                materialProvider.getMaterialCount(FenceMaterialType.WOOD),
                "Material provider should report correct wood count");
        assertEquals(inventory.getBambooFenceMaterialCount(), 
                materialProvider.getMaterialCount(FenceMaterialType.BAMBOO),
                "Material provider should report correct bamboo count");
    }
}