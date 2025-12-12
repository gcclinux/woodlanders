package wagemaker.uk.fence;

/**
 * Enumeration of the 8 distinct fence piece types used to construct rectangular enclosures.
 * The pieces are arranged in clockwise order starting from the top-left corner.
 */
public enum FencePieceType {
    FENCE_BACK_LEFT(0, 0, "Top-left corner piece"),        // Index 0 → (0,0)
    FENCE_BACK(64, 0, "Top edge piece"),                   // Index 1 → (64,0)
    FENCE_BACK_RIGHT(128, 0, "Top-right corner piece"),    // Index 2 → (128,0)
    FENCE_MIDDLE_RIGHT(128, 64, "Right edge piece"),       // Index 3 → (128,64) - FIXED
    FENCE_FRONT_RIGHT(128, 128, "Bottom-right corner piece"), // Index 4 → (128,128) - FIXED
    FENCE_FRONT(64, 128, "Bottom edge piece"),             // Index 5 → (64,128) - FIXED
    FENCE_FRONT_LEFT(0, 128, "Bottom-left corner piece"),  // Index 6 → (0,128) - FIXED
    FENCE_MIDDLE_LEFT(0, 64, "Left edge piece");           // Index 7 → (0,64) - FIXED

    private final int textureX;
    private final int textureY;
    private final String description;

    FencePieceType(int textureX, int textureY, String description) {
        this.textureX = textureX;
        this.textureY = textureY;
        this.description = description;
    }

    /**
     * Gets the X coordinate of this piece type in the texture atlas.
     * @return X coordinate in pixels
     */
    public int getTextureX() {
        return textureX;
    }

    /**
     * Gets the Y coordinate of this piece type in the texture atlas.
     * @return Y coordinate in pixels
     */
    public int getTextureY() {
        return textureY;
    }

    /**
     * Gets a human-readable description of this piece type.
     * @return Description string
     */
    public String getDescription() {
        return description;
    }

    /**
     * Checks if this piece type is a corner piece.
     * @return true if this is a corner piece, false otherwise
     */
    public boolean isCornerPiece() {
        return this == FENCE_BACK_LEFT || this == FENCE_BACK_RIGHT || 
               this == FENCE_FRONT_RIGHT || this == FENCE_FRONT_LEFT;
    }

    /**
     * Checks if this piece type is an edge piece.
     * @return true if this is an edge piece, false otherwise
     */
    public boolean isEdgePiece() {
        return this == FENCE_BACK || this == FENCE_MIDDLE_RIGHT || 
               this == FENCE_FRONT || this == FENCE_MIDDLE_LEFT;
    }
}