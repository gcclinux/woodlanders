package wagemaker.uk.fence;

import com.badlogic.gdx.math.Rectangle;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete rectangular fence enclosure.
 * Contains all fence pieces that form a closed rectangular structure.
 */
public class FenceEnclosure {
    
    /** Bounding rectangle of the enclosure in grid coordinates */
    private final Rectangle bounds;
    
    /** List of fence pieces that make up this enclosure */
    private final List<FencePiece> pieces;
    
    /** Material type used for this enclosure */
    private final FenceMaterialType materialType;
    
    /** Owner ID for multiplayer ownership tracking */
    private String ownerId;
    
    /** Creation timestamp */
    private final long creationTime;
    
    /**
     * Creates a new fence enclosure.
     * 
     * @param bounds Bounding rectangle in grid coordinates
     * @param pieces List of fence pieces forming the enclosure
     * @param materialType Material type used for construction
     */
    public FenceEnclosure(Rectangle bounds, List<FencePiece> pieces, FenceMaterialType materialType) {
        this.bounds = new Rectangle(bounds);
        this.pieces = new ArrayList<>(pieces);
        this.materialType = materialType;
        this.creationTime = System.currentTimeMillis();
    }
    
    /**
     * Creates a new fence enclosure with owner information.
     * 
     * @param bounds Bounding rectangle in grid coordinates
     * @param pieces List of fence pieces forming the enclosure
     * @param materialType Material type used for construction
     * @param ownerId ID of the player who created this enclosure
     */
    public FenceEnclosure(Rectangle bounds, List<FencePiece> pieces, 
                         FenceMaterialType materialType, String ownerId) {
        this(bounds, pieces, materialType);
        this.ownerId = ownerId;
    }
    
    /**
     * Checks if this enclosure is complete (has all required pieces).
     * 
     * @return true if the enclosure has the correct number of pieces for its size
     */
    public boolean isComplete() {
        int expectedPieces = FencePieceFactory.calculateMaterialRequirement(bounds);
        return pieces.size() == expectedPieces;
    }
    
    /**
     * Gets the collision bounds for this enclosure.
     * Returns a list of rectangles representing the collision areas.
     * 
     * @return List of collision rectangles
     */
    public List<Rectangle> getCollisionBounds() {
        List<Rectangle> collisionBounds = new ArrayList<>();
        
        for (FencePiece piece : pieces) {
            collisionBounds.add(piece.getCollisionBounds());
        }
        
        return collisionBounds;
    }
    
    /**
     * Checks if a grid position is part of this enclosure.
     * 
     * @param gridPos Grid position to check
     * @return true if the position is part of this enclosure
     */
    public boolean containsPosition(Point gridPos) {
        for (FencePiece piece : pieces) {
            // Convert piece world coordinates to grid coordinates
            Point pieceGridPos = new Point(
                (int) Math.floor(piece.getX() / FenceGrid.GRID_SIZE),
                (int) Math.floor(piece.getY() / FenceGrid.GRID_SIZE)
            );
            
            if (pieceGridPos.equals(gridPos)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets the bounding rectangle of this enclosure.
     * 
     * @return Rectangle representing the bounds in grid coordinates
     */
    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }
    
    /**
     * Gets all fence pieces in this enclosure.
     * 
     * @return List of fence pieces (defensive copy)
     */
    public List<FencePiece> getPieces() {
        return new ArrayList<>(pieces);
    }
    
    /**
     * Gets the material type used for this enclosure.
     * 
     * @return FenceMaterialType
     */
    public FenceMaterialType getMaterialType() {
        return materialType;
    }
    
    /**
     * Gets the owner ID of this enclosure.
     * 
     * @return Owner ID, or null if no owner is set
     */
    public String getOwnerId() {
        return ownerId;
    }
    
    /**
     * Sets the owner ID of this enclosure.
     * 
     * @param ownerId New owner ID
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    /**
     * Gets the creation timestamp of this enclosure.
     * 
     * @return Creation time in milliseconds since epoch
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * Gets the width of this enclosure in grid units.
     * 
     * @return Width in grid units
     */
    public int getWidth() {
        return (int) bounds.width;
    }
    
    /**
     * Gets the height of this enclosure in grid units.
     * 
     * @return Height in grid units
     */
    public int getHeight() {
        return (int) bounds.height;
    }
    
    /**
     * Gets the area enclosed by this fence structure in grid units.
     * This is the interior area, not including the fence pieces themselves.
     * 
     * @return Interior area in grid units
     */
    public int getInteriorArea() {
        return Math.max(0, (getWidth() - 2) * (getHeight() - 2));
    }
    
    /**
     * Gets the perimeter length of this enclosure in grid units.
     * 
     * @return Perimeter length
     */
    public int getPerimeter() {
        return pieces.size();
    }
    
    /**
     * Serializes this enclosure to a data object for persistence.
     * 
     * @return FenceEnclosureData containing serializable information
     */
    public FenceEnclosureData serialize() {
        return new FenceEnclosureData(bounds, materialType, ownerId, creationTime, pieces);
    }
    
    /**
     * Disposes of all resources used by this enclosure.
     * Disposes of all fence pieces.
     */
    public void dispose() {
        for (FencePiece piece : pieces) {
            piece.dispose();
        }
        pieces.clear();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FenceEnclosure that = (FenceEnclosure) obj;
        return bounds.equals(that.bounds) && 
               materialType == that.materialType &&
               creationTime == that.creationTime;
    }
    
    @Override
    public int hashCode() {
        return java.util.Objects.hash(bounds, materialType, creationTime);
    }
    
    @Override
    public String toString() {
        return String.format("FenceEnclosure[%dx%d, %s, %d pieces, owner=%s]",
                           getWidth(), getHeight(), materialType, pieces.size(), ownerId);
    }
}