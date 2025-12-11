package wagemaker.uk.fence;

/**
 * Enum representing different types of fence materials that can be used for fence construction.
 * Each material type has display properties and icon information for UI integration.
 */
public enum FenceMaterialType {
    WOOD("Wood Fence Material", "fence_wood_icon.png"),
    BAMBOO("Bamboo Fence Material", "fence_bamboo_icon.png");
    
    private final String displayName;
    private final String iconPath;
    
    FenceMaterialType(String displayName, String iconPath) {
        this.displayName = displayName;
        this.iconPath = iconPath;
    }
    
    /**
     * Get the display name for this fence material type.
     * @return The human-readable display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the icon path for this fence material type.
     * @return The path to the icon texture file
     */
    public String getIconPath() {
        return iconPath;
    }
}