package wagemaker.uk.player;

/**
 * Enumeration representing different navigation input modes for the player.
 * Each mode has a priority level to determine which mode takes precedence
 * when multiple modes might be requested simultaneously.
 */
public enum NavigationMode {
    /**
     * Normal player movement mode - standard arrow key movement.
     * Lowest priority mode.
     */
    NORMAL(3, "Normal Movement"),
    
    /**
     * Inventory navigation mode - arrow keys navigate inventory slots.
     * Medium priority mode.
     */
    INVENTORY(2, "Inventory Navigation"),
    
    /**
     * Fence building navigation mode - LEFT/RIGHT select fence pieces, A/W/D/S control targeting.
     * High priority mode.
     */
    FENCE_BUILDING(1, "Fence Building"),
    
    /**
     * Targeting mode - A/W/D/S keys move targeting cursor.
     * Highest priority mode.
     */
    TARGETING(0, "Targeting");
    
    private final int priority;
    private final String description;
    
    NavigationMode(int priority, String description) {
        this.priority = priority;
        this.description = description;
    }
    
    /**
     * Get the priority level of this navigation mode.
     * Lower numbers indicate higher priority.
     * @return The priority level (0 = highest priority)
     */
    public int getPriority() {
        return priority;
    }
    
    /**
     * Get a human-readable description of this navigation mode.
     * @return The description string
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this mode has higher priority than another mode.
     * @param other The other navigation mode to compare against
     * @return true if this mode has higher priority (lower priority number)
     */
    public boolean hasHigherPriorityThan(NavigationMode other) {
        return this.priority < other.priority;
    }
    
    /**
     * Check if this mode blocks normal player movement.
     * @return true if this mode should block normal player movement
     */
    public boolean blocksPlayerMovement() {
        return this != NORMAL;
    }
}