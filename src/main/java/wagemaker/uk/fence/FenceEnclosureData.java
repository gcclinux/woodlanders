package wagemaker.uk.fence;

import com.badlogic.gdx.math.Rectangle;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Serializable data class for fence enclosures.
 * Used for saving and loading fence structures to/from persistent storage.
 * Implements Serializable for binary serialization and provides JSON-compatible structure.
 */
public class FenceEnclosureData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /** Bounding rectangle in grid coordinates */
    public Rectangle bounds;
    
    /** Material type used for the enclosure */
    public FenceMaterialType materialType;
    
    /** Owner ID for multiplayer ownership */
    public String ownerId;
    
    /** Creation timestamp */
    public long creationTime;
    
    /** List of piece positions and types */
    public List<FencePieceData> pieceData;
    
    /**
     * Default constructor for serialization frameworks.
     */
    public FenceEnclosureData() {
        this.pieceData = new ArrayList<>();
    }
    
    /**
     * Creates fence enclosure data from an existing enclosure.
     * 
     * @param bounds Bounding rectangle
     * @param materialType Material type
     * @param ownerId Owner ID
     * @param creationTime Creation timestamp
     * @param pieces List of fence pieces
     */
    public FenceEnclosureData(Rectangle bounds, FenceMaterialType materialType, 
                             String ownerId, long creationTime, List<FencePiece> pieces) {
        this.bounds = new Rectangle(bounds);
        this.materialType = materialType;
        this.ownerId = ownerId;
        this.creationTime = creationTime;
        this.pieceData = new ArrayList<>();
        
        // Convert fence pieces to serializable data
        for (FencePiece piece : pieces) {
            pieceData.add(new FencePieceData(piece.getX(), piece.getY(), piece.getType(), piece.getOwnerId()));
        }
    }
    
    /**
     * Reconstructs a FenceEnclosure from this data.
     * 
     * @return New FenceEnclosure instance
     */
    public FenceEnclosure toFenceEnclosure() {
        List<FencePiece> pieces = new ArrayList<>();
        
        // Recreate fence pieces from data
        for (FencePieceData data : pieceData) {
            FencePiece piece = FencePieceFactory.createPiece(data.type, data.x, data.y, data.ownerId);
            pieces.add(piece);
        }
        
        return new FenceEnclosure(bounds, pieces, materialType, ownerId);
    }
    
    /**
     * Validates the fence enclosure data integrity.
     * 
     * @return true if the data is valid, false otherwise
     */
    public boolean isValid() {
        if (bounds == null || materialType == null || pieceData == null) {
            return false;
        }
        
        if (bounds.width <= 0 || bounds.height <= 0) {
            return false;
        }
        
        if (creationTime <= 0) {
            return false;
        }
        
        // Validate piece data
        for (FencePieceData data : pieceData) {
            if (data == null || data.type == null) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Gets the number of fence pieces in this enclosure.
     * 
     * @return Number of pieces
     */
    public int getPieceCount() {
        return pieceData != null ? pieceData.size() : 0;
    }
    
    /**
     * Serializable data for individual fence pieces.
     * Implements Serializable for binary serialization.
     */
    public static class FencePieceData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public float x, y;
        public FencePieceType type;
        public String ownerId;
        
        /**
         * Default constructor for serialization.
         */
        public FencePieceData() {}
        
        /**
         * Creates fence piece data.
         * 
         * @param x World X coordinate
         * @param y World Y coordinate
         * @param type Fence piece type
         */
        public FencePieceData(float x, float y, FencePieceType type) {
            this(x, y, type, null);
        }

        /**
         * Creates fence piece data with owner.
         * 
         * @param x World X coordinate
         * @param y World Y coordinate
         * @param type Fence piece type
         * @param ownerId Owner ID
         */
        public FencePieceData(float x, float y, FencePieceType type, String ownerId) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.ownerId = ownerId;
        }
        
        /**
         * Validates this piece data.
         * 
         * @return true if valid, false otherwise
         */
        public boolean isValid() {
            return type != null;
        }
        
        @Override
        public String toString() {
            return String.format("FencePieceData[%.1f,%.1f,%s]", x, y, type);
        }
    }
}