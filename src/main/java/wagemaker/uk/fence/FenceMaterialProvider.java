package wagemaker.uk.fence;

/**
 * Interface for providing fence material operations.
 * Integrates with the existing inventory management system to handle fence material availability,
 * consumption, and return operations.
 */
public interface FenceMaterialProvider {
    
    /**
     * Check if enough materials of the specified type are available.
     * @param type The type of fence material to check
     * @param count The number of materials needed
     * @return true if enough materials are available, false otherwise
     */
    boolean hasEnoughMaterials(FenceMaterialType type, int count);
    
    /**
     * Consume the specified amount of fence materials.
     * This method should only be called after checking availability with hasEnoughMaterials.
     * @param type The type of fence material to consume
     * @param count The number of materials to consume
     * @throws IllegalArgumentException if insufficient materials are available
     */
    void consumeMaterials(FenceMaterialType type, int count);
    
    /**
     * Return fence materials to the inventory (e.g., when removing fence pieces).
     * @param type The type of fence material to return
     * @param count The number of materials to return
     */
    void returnMaterials(FenceMaterialType type, int count);
    
    /**
     * Get the current count of available materials of the specified type.
     * @param type The type of fence material to query
     * @return The current count of available materials
     */
    int getMaterialCount(FenceMaterialType type);
}