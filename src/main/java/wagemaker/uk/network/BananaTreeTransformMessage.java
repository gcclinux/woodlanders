package wagemaker.uk.network;

public class BananaTreeTransformMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private String plantedBananaTreeId;
    private String bananaTreeId;
    private float x;
    private float y;
    
    public BananaTreeTransformMessage() {
        super();
    }
    
    public BananaTreeTransformMessage(String playerId, String plantedBananaTreeId, String bananaTreeId, float x, float y) {
        super(playerId);
        this.playerId = playerId;
        this.plantedBananaTreeId = plantedBananaTreeId;
        this.bananaTreeId = bananaTreeId;
        this.x = x;
        this.y = y;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.BANANA_TREE_TRANSFORM;
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
    
    public String getBananaTreeId() {
        return bananaTreeId;
    }
    
    public void setBananaTreeId(String bananaTreeId) {
        this.bananaTreeId = bananaTreeId;
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
