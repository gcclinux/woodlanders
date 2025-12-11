package wagemaker.uk.network;

import wagemaker.uk.fence.FencePieceType;
import wagemaker.uk.fence.FenceMaterialType;
import java.io.Serializable;

/**
 * Network-serializable representation of a fence piece state.
 * Used for synchronizing fence structures across multiplayer clients.
 */
public class FenceState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String fenceId;
    private int gridX;
    private int gridY;
    private FencePieceType pieceType;
    private FenceMaterialType materialType;
    private String ownerId;
    private long creationTime;
    
    /**
     * Default constructor for serialization.
     */
    public FenceState() {
    }
    
    /**
     * Creates a new fence state.
     * @param fenceId The unique ID of the fence piece
     * @param gridX The grid X coordinate
     * @param gridY The grid Y coordinate
     * @param pieceType The type of fence piece
     * @param materialType The material type of the fence
     * @param ownerId The ID of the player who owns this fence
     * @param creationTime The timestamp when this fence was created
     */
    public FenceState(String fenceId, int gridX, int gridY, FencePieceType pieceType, 
                     FenceMaterialType materialType, String ownerId, long creationTime) {
        this.fenceId = fenceId;
        this.gridX = gridX;
        this.gridY = gridY;
        this.pieceType = pieceType;
        this.materialType = materialType;
        this.ownerId = ownerId;
        this.creationTime = creationTime;
    }
    
    /**
     * Gets the unique ID of the fence piece.
     * @return The fence piece ID
     */
    public String getFenceId() {
        return fenceId;
    }
    
    /**
     * Sets the unique ID of the fence piece.
     * @param fenceId The fence piece ID
     */
    public void setFenceId(String fenceId) {
        this.fenceId = fenceId;
    }
    
    /**
     * Gets the grid X coordinate.
     * @return The grid X coordinate
     */
    public int getGridX() {
        return gridX;
    }
    
    /**
     * Sets the grid X coordinate.
     * @param gridX The grid X coordinate
     */
    public void setGridX(int gridX) {
        this.gridX = gridX;
    }
    
    /**
     * Gets the grid Y coordinate.
     * @return The grid Y coordinate
     */
    public int getGridY() {
        return gridY;
    }
    
    /**
     * Sets the grid Y coordinate.
     * @param gridY The grid Y coordinate
     */
    public void setGridY(int gridY) {
        this.gridY = gridY;
    }
    
    /**
     * Gets the fence piece type.
     * @return The fence piece type
     */
    public FencePieceType getPieceType() {
        return pieceType;
    }
    
    /**
     * Sets the fence piece type.
     * @param pieceType The fence piece type
     */
    public void setPieceType(FencePieceType pieceType) {
        this.pieceType = pieceType;
    }
    
    /**
     * Gets the fence material type.
     * @return The fence material type
     */
    public FenceMaterialType getMaterialType() {
        return materialType;
    }
    
    /**
     * Sets the fence material type.
     * @param materialType The fence material type
     */
    public void setMaterialType(FenceMaterialType materialType) {
        this.materialType = materialType;
    }
    
    /**
     * Gets the ID of the player who owns this fence.
     * @return The owner ID
     */
    public String getOwnerId() {
        return ownerId;
    }
    
    /**
     * Sets the ID of the player who owns this fence.
     * @param ownerId The owner ID
     */
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    
    /**
     * Gets the timestamp when this fence was created.
     * @return The creation timestamp
     */
    public long getCreationTime() {
        return creationTime;
    }
    
    /**
     * Sets the timestamp when this fence was created.
     * @param creationTime The creation timestamp
     */
    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }
    
    @Override
    public String toString() {
        return "FenceState{" +
                "fenceId='" + fenceId + '\'' +
                ", gridX=" + gridX +
                ", gridY=" + gridY +
                ", pieceType=" + pieceType +
                ", materialType=" + materialType +
                ", ownerId='" + ownerId + '\'' +
                ", creationTime=" + creationTime +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        FenceState that = (FenceState) o;
        
        return fenceId != null ? fenceId.equals(that.fenceId) : that.fenceId == null;
    }
    
    @Override
    public int hashCode() {
        return fenceId != null ? fenceId.hashCode() : 0;
    }
}