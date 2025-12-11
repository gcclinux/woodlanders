package wagemaker.uk.network;

import wagemaker.uk.fence.FencePieceType;
import wagemaker.uk.fence.FenceMaterialType;

/**
 * Network message for fence placement operations.
 * Sent when a player places a fence piece in building mode.
 */
public class FencePlaceMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String fenceId;
    private int gridX;
    private int gridY;
    private FencePieceType pieceType;
    private FenceMaterialType materialType;
    private String playerId;
    
    /**
     * Default constructor for serialization.
     */
    public FencePlaceMessage() {
        super();
    }
    
    /**
     * Creates a new fence place message.
     * @param senderId The ID of the sender
     * @param fenceId The unique ID of the fence piece
     * @param gridX The grid X coordinate
     * @param gridY The grid Y coordinate
     * @param pieceType The type of fence piece being placed
     * @param materialType The material type of the fence
     * @param playerId The ID of the player placing the fence
     */
    public FencePlaceMessage(String senderId, String fenceId, int gridX, int gridY, 
                           FencePieceType pieceType, FenceMaterialType materialType, String playerId) {
        super(senderId);
        this.fenceId = fenceId;
        this.gridX = gridX;
        this.gridY = gridY;
        this.pieceType = pieceType;
        this.materialType = materialType;
        this.playerId = playerId;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.FENCE_PLACE;
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
     * Gets the ID of the player placing the fence.
     * @return The player ID
     */
    public String getPlayerId() {
        return playerId;
    }
    
    /**
     * Sets the ID of the player placing the fence.
     * @param playerId The player ID
     */
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    @Override
    public String toString() {
        return "FencePlaceMessage{" +
                "fenceId='" + fenceId + '\'' +
                ", gridX=" + gridX +
                ", gridY=" + gridY +
                ", pieceType=" + pieceType +
                ", materialType=" + materialType +
                ", playerId='" + playerId + '\'' +
                ", senderId='" + getSenderId() + '\'' +
                ", timestamp=" + getTimestamp() +
                '}';
    }
}