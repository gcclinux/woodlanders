package wagemaker.uk.network;

import wagemaker.uk.fence.FenceMaterialType;

/**
 * Network message for fence removal operations.
 * Sent when a player removes a fence piece in building mode.
 */
public class FenceRemoveMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String fenceId;
    private int gridX;
    private int gridY;
    private FenceMaterialType materialType;
    private String playerId;
    
    /**
     * Default constructor for serialization.
     */
    public FenceRemoveMessage() {
        super();
    }
    
    /**
     * Creates a new fence remove message.
     * @param senderId The ID of the sender
     * @param fenceId The unique ID of the fence piece being removed
     * @param gridX The grid X coordinate
     * @param gridY The grid Y coordinate
     * @param materialType The material type of the fence (for inventory return)
     * @param playerId The ID of the player removing the fence
     */
    public FenceRemoveMessage(String senderId, String fenceId, int gridX, int gridY, 
                            FenceMaterialType materialType, String playerId) {
        super(senderId);
        this.fenceId = fenceId;
        this.gridX = gridX;
        this.gridY = gridY;
        this.materialType = materialType;
        this.playerId = playerId;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.FENCE_REMOVE;
    }
    
    /**
     * Gets the unique ID of the fence piece being removed.
     * @return The fence piece ID
     */
    public String getFenceId() {
        return fenceId;
    }
    
    /**
     * Sets the unique ID of the fence piece being removed.
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
     * Gets the ID of the player removing the fence.
     * @return The player ID
     */
    public String getPlayerId() {
        return playerId;
    }
    
    /**
     * Sets the ID of the player removing the fence.
     * @param playerId The player ID
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    @Override
    public String toString() {
        return "FenceRemoveMessage{" +
                "fenceId='" + fenceId + '\'' +
                ", gridX=" + gridX +
                ", gridY=" + gridY +
                ", materialType=" + materialType +
                ", playerId='" + playerId + '\'' +
                ", senderId='" + getSenderId() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}