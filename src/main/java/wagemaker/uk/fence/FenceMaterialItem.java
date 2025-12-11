package wagemaker.uk.fence;

import com.badlogic.gdx.graphics.Texture;
import wagemaker.uk.inventory.ItemType;

/**
 * Represents a fence material item that can be stored in inventory and used for fence construction.
 * Integrates with the existing inventory system and provides stacking functionality.
 */
public class FenceMaterialItem {
    private final FenceMaterialType materialType;
    private final ItemType itemType;
    private final int maxStackSize;
    private Texture iconTexture;
    
    /**
     * Create a new fence material item.
     * @param materialType The type of fence material (wood or bamboo)
     */
    public FenceMaterialItem(FenceMaterialType materialType) {
        this.materialType = materialType;
        this.maxStackSize = 64; // Standard stack size for materials
        
        // Map fence material types to inventory item types
        switch (materialType) {
            case WOOD:
                this.itemType = ItemType.WOOD_FENCE_MATERIAL;
                break;
            case BAMBOO:
                this.itemType = ItemType.BAMBOO_FENCE_MATERIAL;
                break;
            default:
                throw new IllegalArgumentException("Unknown fence material type: " + materialType);
        }
        
        loadIconTexture();
    }
    
    /**
     * Load the icon texture for this fence material.
     * Uses placeholder texture if the specified icon is not found.
     */
    private void loadIconTexture() {
        try {
            iconTexture = new Texture(materialType.getIconPath());
        } catch (Exception e) {
            System.err.println("Warning: Could not load fence material icon: " + materialType.getIconPath());
            // Create a simple colored placeholder texture
            iconTexture = createPlaceholderTexture();
        }
    }
    
    /**
     * Create a placeholder texture for missing icons.
     * @return A simple colored texture as fallback
     */
    private Texture createPlaceholderTexture() {
        // This would create a simple colored rectangle as placeholder
        // For now, return null and handle gracefully in rendering
        return null;
    }
    
    /**
     * Get the fence material type.
     * @return The fence material type
     */
    public FenceMaterialType getMaterialType() {
        return materialType;
    }
    
    /**
     * Get the corresponding inventory item type.
     * @return The ItemType for inventory integration
     */
    public ItemType getItemType() {
        return itemType;
    }
    
    /**
     * Get the maximum stack size for this material.
     * @return The maximum number of items that can be stacked together
     */
    public int getMaxStackSize() {
        return maxStackSize;
    }
    
    /**
     * Get the display name for this fence material.
     * @return The human-readable display name
     */
    public String getDisplayName() {
        return materialType.getDisplayName();
    }
    
    /**
     * Get the icon texture for UI display.
     * @return The icon texture, or null if not available
     */
    public Texture getIconTexture() {
        return iconTexture;
    }
    
    /**
     * Check if this material can be stacked with another material.
     * @param other The other fence material item
     * @return true if they can be stacked together
     */
    public boolean canStackWith(FenceMaterialItem other) {
        return other != null && this.materialType == other.materialType;
    }
    
    /**
     * Dispose of resources used by this fence material item.
     */
    public void dispose() {
        if (iconTexture != null) {
            iconTexture.dispose();
            iconTexture = null;
        }
    }
}