package wagemaker.uk.fence;

import wagemaker.uk.inventory.InventoryManager;

/**
 * Implementation of FenceMaterialProvider that integrates with the existing inventory system.
 * Provides fence material operations by delegating to the InventoryManager.
 */
public class InventoryFenceMaterialProvider implements FenceMaterialProvider {
    private final InventoryManager inventoryManager;
    
    /**
     * Create a new InventoryFenceMaterialProvider.
     * @param inventoryManager The inventory manager to use for material operations
     */
    public InventoryFenceMaterialProvider(InventoryManager inventoryManager) {
        if (inventoryManager == null) {
            throw new IllegalArgumentException("InventoryManager cannot be null");
        }
        this.inventoryManager = inventoryManager;
    }
    
    @Override
    public boolean hasEnoughMaterials(FenceMaterialType type, int count) {
        if (type == null || count < 0) {
            return false;
        }
        
        int availableCount = getMaterialCount(type);
        return availableCount >= count;
    }
    
    @Override
    public void consumeMaterials(FenceMaterialType type, int count) {
        if (type == null) {
            throw new IllegalArgumentException("FenceMaterialType cannot be null");
        }
        
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        
        if (count == 0) {
            return; // Nothing to consume
        }
        
        if (!hasEnoughMaterials(type, count)) {
            throw new IllegalArgumentException("Insufficient materials: need " + count + 
                    " " + type + " but only have " + getMaterialCount(type));
        }
        
        // Consume materials from inventory
        switch (type) {
            case WOOD:
                if (!inventoryManager.getCurrentInventory().removeWoodFenceMaterial(count)) {
                    throw new IllegalStateException("Failed to consume wood fence materials");
                }
                break;
            case BAMBOO:
                if (!inventoryManager.getCurrentInventory().removeBambooFenceMaterial(count)) {
                    throw new IllegalStateException("Failed to consume bamboo fence materials");
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown fence material type: " + type);
        }
        
        // Trigger inventory update for multiplayer synchronization
        inventoryManager.sendInventoryUpdateToServer();
    }
    
    @Override
    public void returnMaterials(FenceMaterialType type, int count) {
        if (type == null) {
            throw new IllegalArgumentException("FenceMaterialType cannot be null");
        }
        
        if (count < 0) {
            throw new IllegalArgumentException("Count cannot be negative");
        }
        
        if (count == 0) {
            return; // Nothing to return
        }
        
        // Return materials to inventory
        switch (type) {
            case WOOD:
                inventoryManager.getCurrentInventory().addWoodFenceMaterial(count);
                break;
            case BAMBOO:
                inventoryManager.getCurrentInventory().addBambooFenceMaterial(count);
                break;
            default:
                throw new IllegalArgumentException("Unknown fence material type: " + type);
        }
        
        // Trigger inventory update for multiplayer synchronization
        inventoryManager.sendInventoryUpdateToServer();
    }
    
    @Override
    public int getMaterialCount(FenceMaterialType type) {
        if (type == null) {
            return 0;
        }
        
        switch (type) {
            case WOOD:
                return inventoryManager.getCurrentInventory().getWoodFenceMaterialCount();
            case BAMBOO:
                return inventoryManager.getCurrentInventory().getBambooFenceMaterialCount();
            default:
                return 0;
        }
    }
}