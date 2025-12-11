package wagemaker.uk.freeworld;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import wagemaker.uk.inventory.Inventory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FreeWorldManager class.
 * Tests Free World mode activation and item granting functionality.
 */
public class FreeWorldManagerTest {
    
    private Inventory inventory;
    
    @BeforeEach
    public void setUp() {
        // Reset Free World state before each test
        FreeWorldManager.deactivateFreeWorld();
        
        // Create a fresh inventory for testing
        inventory = new Inventory();
    }
    
    @Test
    public void testFreeWorldActivation() {
        assertFalse(FreeWorldManager.isFreeWorldActive(), "Free World should be inactive initially");
        
        FreeWorldManager.activateFreeWorld();
        assertTrue(FreeWorldManager.isFreeWorldActive(), "Free World should be active after activation");
        
        FreeWorldManager.deactivateFreeWorld();
        assertFalse(FreeWorldManager.isFreeWorldActive(), "Free World should be inactive after deactivation");
    }
    
    @Test
    public void testGrantFreeWorldItemsIncludesFenceMaterials() {
        // Verify initial state
        assertEquals(0, inventory.getWoodFenceMaterialCount(), "Wood fence materials should start at 0");
        assertEquals(0, inventory.getBambooFenceMaterialCount(), "Bamboo fence materials should start at 0");
        
        // Grant Free World items
        FreeWorldManager.grantFreeWorldItems(inventory);
        
        // Verify fence materials are granted
        assertEquals(250, inventory.getWoodFenceMaterialCount(), "Wood fence materials should be granted 250");
        assertEquals(250, inventory.getBambooFenceMaterialCount(), "Bamboo fence materials should be granted 250");
    }
    
    @Test
    public void testGrantFreeWorldItemsIncludesAllStandardItems() {
        // Grant Free World items
        FreeWorldManager.grantFreeWorldItems(inventory);
        
        // Verify all standard items are granted
        assertEquals(250, inventory.getAppleCount(), "Apples should be granted 250");
        assertEquals(250, inventory.getAppleSaplingCount(), "Apple saplings should be granted 250");
        assertEquals(250, inventory.getBananaCount(), "Bananas should be granted 250");
        assertEquals(250, inventory.getBananaSaplingCount(), "Banana saplings should be granted 250");
        assertEquals(250, inventory.getBambooSaplingCount(), "Bamboo saplings should be granted 250");
        assertEquals(250, inventory.getBambooStackCount(), "Bamboo stacks should be granted 250");
        assertEquals(250, inventory.getTreeSaplingCount(), "Tree saplings should be granted 250");
        assertEquals(250, inventory.getWoodStackCount(), "Wood stacks should be granted 250");
        assertEquals(250, inventory.getPebbleCount(), "Pebbles should be granted 250");
        assertEquals(250, inventory.getPalmFiberCount(), "Palm fibers should be granted 250");
        assertEquals(250, inventory.getFishCount(), "Fish should be granted 250");
        assertEquals(250, inventory.getFrontFenceCount(), "Front fences should be granted 250");
        assertEquals(250, inventory.getBackFenceCount(), "Back fences should be granted 250");
        assertEquals(250, inventory.getBowAndArrowCount(), "Bow and arrows should be granted 250");
    }
    
    @Test
    public void testGrantFreeWorldItemsWithNullInventory() {
        // Should not throw exception with null inventory
        assertDoesNotThrow(() -> FreeWorldManager.grantFreeWorldItems(null), 
                          "Should handle null inventory gracefully");
    }
    
    @Test
    public void testFreeWorldStatePreservation() {
        // Test that Free World state is preserved across multiple operations
        FreeWorldManager.activateFreeWorld();
        assertTrue(FreeWorldManager.isFreeWorldActive());
        
        // Grant items multiple times
        FreeWorldManager.grantFreeWorldItems(inventory);
        assertTrue(FreeWorldManager.isFreeWorldActive(), "Free World should remain active after granting items");
        
        FreeWorldManager.grantFreeWorldItems(inventory);
        assertTrue(FreeWorldManager.isFreeWorldActive(), "Free World should remain active after multiple grants");
        
        // Verify items are still at 250 (not accumulated)
        assertEquals(250, inventory.getWoodFenceMaterialCount(), "Wood fence materials should remain at 250");
        assertEquals(250, inventory.getBambooFenceMaterialCount(), "Bamboo fence materials should remain at 250");
    }
}