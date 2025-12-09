package wagemaker.uk.network;

/**
 * Message sent when a player's inventory changes.
 * Sent from client to server when items are collected or consumed.
 */
public class InventoryUpdateMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private int appleCount;
    private int bananaCount;
    private int appleSaplingCount;
    private int bananaSaplingCount;
    private int bambooSaplingCount;
    private int bambooStackCount;
    private int treeSaplingCount;
    private int woodStackCount;
    private int pebbleCount;
    private int palmFiberCount;
    private int fishCount;
    private int frontFenceCount;
    private int backFenceCount;
    private int bowAndArrowCount;
    
    public InventoryUpdateMessage() {
        super();
    }
    
    public InventoryUpdateMessage(String senderId, String playerId, 
                                   int appleCount, int bananaCount, int appleSaplingCount, int bananaSaplingCount,
                                   int bambooSaplingCount, int bambooStackCount, 
                                   int treeSaplingCount, int woodStackCount, int pebbleCount, int palmFiberCount,
                                   int fishCount, int frontFenceCount, int backFenceCount, int bowAndArrowCount) {
        super(senderId);
        this.playerId = playerId;
        this.appleCount = appleCount;
        this.bananaCount = bananaCount;
        this.appleSaplingCount = appleSaplingCount;
        this.bananaSaplingCount = bananaSaplingCount;
        this.bambooSaplingCount = bambooSaplingCount;
        this.bambooStackCount = bambooStackCount;
        this.treeSaplingCount = treeSaplingCount;
        this.woodStackCount = woodStackCount;
        this.pebbleCount = pebbleCount;
        this.palmFiberCount = palmFiberCount;
        this.fishCount = fishCount;
        this.frontFenceCount = frontFenceCount;
        this.backFenceCount = backFenceCount;
        this.bowAndArrowCount = bowAndArrowCount;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.INVENTORY_UPDATE;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public int getAppleCount() {
        return appleCount;
    }
    
    public int getBananaCount() {
        return bananaCount;
    }
    
    public int getAppleSaplingCount() {
        return appleSaplingCount;
    }
    
    public int getBananaSaplingCount() {
        return bananaSaplingCount;
    }
    
    public int getBambooSaplingCount() {
        return bambooSaplingCount;
    }
    
    public int getBambooStackCount() {
        return bambooStackCount;
    }
    
    public int getTreeSaplingCount() {
        return treeSaplingCount;
    }
    
    public int getWoodStackCount() {
        return woodStackCount;
    }
    
    public int getPebbleCount() {
        return pebbleCount;
    }
    
    public int getPalmFiberCount() {
        return palmFiberCount;
    }
    
    public int getFishCount() {
        return fishCount;
    }
    
    public int getFrontFenceCount() {
        return frontFenceCount;
    }
    
    public int getBackFenceCount() {
        return backFenceCount;
    }
    
    public int getBowAndArrowCount() {
        return bowAndArrowCount;
    }
}
