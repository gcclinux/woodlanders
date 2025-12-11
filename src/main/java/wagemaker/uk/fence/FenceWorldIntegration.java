package wagemaker.uk.fence;

import com.badlogic.gdx.utils.Disposable;

/**
 * Integration class that handles fence system lifecycle events related to world loading/unloading.
 * Ensures proper cleanup of fence resources when worlds are changed or the game exits.
 */
public class FenceWorldIntegration implements Disposable {
    
    /** Flag indicating if the integration is active */
    private boolean active = false;
    
    /** The main fence building manager */
    private FenceBuildingManager buildingManager;
    
    /** Resource manager for cleanup operations */
    private final FenceResourceManager resourceManager;
    
    /**
     * Creates a new fence world integration.
     */
    public FenceWorldIntegration() {
        this.resourceManager = FenceResourceManager.getInstance();
        this.active = true;
    }
    
    /**
     * Sets the fence building manager to integrate with.
     * 
     * @param buildingManager The building manager to integrate
     */
    public void setBuildingManager(FenceBuildingManager buildingManager) {
        this.buildingManager = buildingManager;
    }
    
    /**
     * Called when a world is being loaded.
     * Prepares the fence system for the new world.
     * 
     * @param worldName Name of the world being loaded
     */
    public void onWorldLoad(String worldName) {
        if (!active) {
            return;
        }
        
        System.out.println("Fence system: Loading world " + worldName);
        
        // Ensure building mode is disabled during world load
        if (buildingManager != null && buildingManager.isBuildingModeActive()) {
            buildingManager.toggleBuildingMode();
        }
        
        // The fence structures will be loaded by the persistence system
        System.out.println("Fence system ready for world: " + worldName);
    }
    
    /**
     * Called when a world is being unloaded.
     * Performs cleanup of all fence resources.
     * 
     * @param worldName Name of the world being unloaded
     */
    public void onWorldUnload(String worldName) {
        if (!active) {
            return;
        }
        
        System.out.println("Fence system: Unloading world " + worldName);
        
        // Exit building mode if active
        if (buildingManager != null && buildingManager.isBuildingModeActive()) {
            buildingManager.toggleBuildingMode();
        }
        
        // Perform comprehensive cleanup
        resourceManager.cleanupOnWorldUnload();
        
        System.out.println("Fence system: World " + worldName + " unloaded successfully");
    }
    
    /**
     * Called when the game is shutting down.
     * Performs final cleanup of all fence resources.
     */
    public void onGameShutdown() {
        if (!active) {
            return;
        }
        
        System.out.println("Fence system: Game shutdown initiated");
        
        // Exit building mode if active
        if (buildingManager != null && buildingManager.isBuildingModeActive()) {
            buildingManager.toggleBuildingMode();
        }
        
        // Dispose of building manager
        if (buildingManager != null) {
            buildingManager.dispose();
            buildingManager = null;
        }
        
        // Final resource cleanup
        resourceManager.dispose();
        
        active = false;
        System.out.println("Fence system: Shutdown complete");
    }
    
    /**
     * Forces immediate cleanup of fence resources.
     * Useful for memory management in low-memory situations.
     */
    public void forceCleanup() {
        if (!active) {
            return;
        }
        
        System.out.println("Fence system: Forcing immediate cleanup");
        resourceManager.forceCleanup();
    }
    
    /**
     * Gets memory usage statistics for the fence system.
     * 
     * @return Memory usage information
     */
    public FenceResourceManager.MemoryStats getMemoryStats() {
        return resourceManager.getMemoryStats();
    }
    
    /**
     * Checks if the integration is active.
     * 
     * @return true if active, false otherwise
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Gets the building manager associated with this integration.
     * 
     * @return The building manager, or null if none set
     */
    public FenceBuildingManager getBuildingManager() {
        return buildingManager;
    }
    
    @Override
    public void dispose() {
        onGameShutdown();
    }
    
    @Override
    public String toString() {
        FenceResourceManager.MemoryStats stats = getMemoryStats();
        return String.format("FenceWorldIntegration[active=%s, %s]", active, stats);
    }
}