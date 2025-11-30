package wagemaker.uk.network;

public class BananaTreePlantMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String plantedBananaTreeId;
    private float x;
    private float y;
    
    public BananaTreePlantMessage() {
        super();
    }
    
    public BananaTreePlantMessage(String playerId, String plantedBananaTreeId, float x, float y) {
        super(playerId);
        this.playerId = playerId;
        this.plantedBananaTreeId = plantedBananaTreeId;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.BANANA_TREE_PLANT;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public String getPlantedBananaTreeId() {
        return plantedBananaTreeId;
    }
    
    public void setPlantedBananaTreeId(String plantedBananaTreeId) {
        this.plantedBananaTreeId = plantedBananaTreeId;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
}
