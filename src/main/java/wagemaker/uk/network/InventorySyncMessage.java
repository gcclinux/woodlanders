package wagemaker.uk.network;

/**
 * Message sent from server to client to synchronize inventory state.
 * Used for periodic sync to prevent desync issues.
 */
public class InventorySyncMessage extends NetworkMessage {
    private static final long serialVersionUID = 1L;
    
    private String playerId;
    private int appleCount;
    private int bananaCount;
    private int appleSaplingCount;
    private int bambooSaplingCount;
    private int bambooStackCount;
    private int treeSaplingCount;
    private int woodStackCount;
    private int pebbleCount;
    private int palmFiberCount;
    
    public InventorySyncMessage() {
        super();
    }
    
    public InventorySyncMessage(String senderId, String playerId, 
                                 int appleCount, int bananaCount, int appleSaplingCount,
                                 int bambooSaplingCount, int bambooStackCount, 
                                 int treeSaplingCount, int woodStackCount, int pebbleCount, int palmFiberCount) {
        super(senderId);
        this.playerId = playerId;
        this.appleCount = appleCount;
        this.bananaCount = bananaCount;
        this.appleSaplingCount = appleSaplingCount;
        this.bambooSaplingCount = bambooSaplingCount;
        this.bambooStackCount = bambooStackCount;
        this.treeSaplingCount = treeSaplingCount;
        this.woodStackCount = woodStackCount;
        this.pebbleCount = pebbleCount;
        this.palmFiberCount = palmFiberCount;
    }
    
    @Override
    public MessageType getType() {
        return MessageType.INVENTORY_SYNC;
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
}
