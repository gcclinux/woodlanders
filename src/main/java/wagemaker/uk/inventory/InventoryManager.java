package wagemaker.uk.inventory;

import wagemaker.uk.player.Player;
import wagemaker.uk.network.GameClient;
import wagemaker.uk.network.InventoryUpdateMessage;

/**
 * Central manager for inventory operations.
 * Manages separate inventories for single-player and multiplayer modes,
 * handles item collection with health-based routing, and provides auto-consumption logic.
 */
public class InventoryManager {
    private Inventory singleplayerInventory;
    private Inventory multiplayerInventory;
    private Player player;
    private boolean isMultiplayerMode;
    private GameClient gameClient;
    private int selectedSlot; // 0-9 for slots, -1 for no selection
    
    /**
     * Create a new InventoryManager for the given player.
     * @param player The player whose inventory this manager controls
     */
    public InventoryManager(Player player) {
        this.player = player;
        this.singleplayerInventory = new Inventory();
        this.multiplayerInventory = new Inventory();
        this.isMultiplayerMode = false;
        this.selectedSlot = -1; // No selection by default
    }
    
    /**
     * Switch between single-player and multiplayer mode.
     * @param isMultiplayer true for multiplayer mode, false for single-player mode
     */
    public void setMultiplayerMode(boolean isMultiplayer) {
        this.isMultiplayerMode = isMultiplayer;
    }
    
    /**
     * Get the currently active inventory based on game mode.
     * @return The active inventory (single-player or multiplayer)
     */
    public Inventory getCurrentInventory() {
        return isMultiplayerMode ? multiplayerInventory : singleplayerInventory;
    }
    
    /**
     * Get the single-player inventory.
     * @return The single-player inventory instance
     */
    public Inventory getSingleplayerInventory() {
        return singleplayerInventory;
    }
    
    /**
     * Get the multiplayer inventory.
     * @return The multiplayer inventory instance
     */
    public Inventory getMultiplayerInventory() {
        return multiplayerInventory;
    }
    
    /**
     * Check if currently in multiplayer mode.
     * @return true if in multiplayer mode, false otherwise
     */
    public boolean isMultiplayerMode() {
        return isMultiplayerMode;
    }
    
    /**
     * Set the game client for network synchronization.
     * @param gameClient The game client instance
     */
    public void setGameClient(GameClient gameClient) {
        this.gameClient = gameClient;
    }
    
    /**
     * Collect an item and add it to the current inventory.
     * @param type The type of item being collected
     */
    public void collectItem(ItemType type) {
        if (type == null) {
            return;
        }
        
        // Always add to inventory storage
        addItemToInventory(type, 1);
    }
    
    /**
     * Add an item to the current inventory.
     * @param type The type of item to add
     * @param amount The quantity to add
     */
    private void addItemToInventory(ItemType type, int amount) {
        Inventory inventory = getCurrentInventory();
        
        switch (type) {
            case APPLE:
                inventory.addApple(amount);
                break;
            case BANANA:
                inventory.addBanana(amount);
                break;
            case APPLE_SAPLING:
                inventory.addAppleSapling(amount);
                break;
            case BANANA_SAPLING:
                inventory.addBananaSapling(amount);
                break;
            case BABY_BAMBOO:
                inventory.addBambooSapling(amount);
                break;
            case BAMBOO_STACK:
                inventory.addBambooStack(amount);
                break;
            case BABY_TREE:
                inventory.addTreeSapling(amount);
                break;
            case WOOD_STACK:
                inventory.addWoodStack(amount);
                break;
            case PEBBLE:
                inventory.addPebble(amount);
                break;
            case PALM_FIBER:
                inventory.addPalmFiber(amount);
                break;
            case FISH:
                inventory.addFish(amount);
                break;
            case FRONT_FENCE:
                inventory.addFrontFence(amount);
                break;
            case BACK_FENCE:
                inventory.addBackFence(amount);
                break;
            case BOW_AND_ARROW:
                inventory.addBowAndArrow(amount);
                break;
            case WOOD_FENCE_MATERIAL:
                inventory.addWoodFenceMaterial(amount);
                break;
            case BAMBOO_FENCE_MATERIAL:
                inventory.addBambooFenceMaterial(amount);
                break;
        }
        
        // Send inventory update to server in multiplayer mode
        sendInventoryUpdate();
    }
    
    /**
     * Send inventory update to server in multiplayer mode.
     * Only sends if connected to a server and in multiplayer mode.
     */
    private void sendInventoryUpdate() {
        if (isMultiplayerMode && gameClient != null && gameClient.isConnected()) {
            Inventory inventory = getCurrentInventory();
            
            InventoryUpdateMessage message = new InventoryUpdateMessage(
                gameClient.getClientId(),
                gameClient.getClientId(),
                inventory.getAppleCount(),
                inventory.getBananaCount(),
                inventory.getAppleSaplingCount(),
                inventory.getBananaSaplingCount(),
                inventory.getBambooSaplingCount(),
                inventory.getBambooStackCount(),
                inventory.getTreeSaplingCount(),
                inventory.getWoodStackCount(),
                inventory.getPebbleCount(),
                inventory.getPalmFiberCount(),
                inventory.getFishCount(),
                inventory.getFrontFenceCount(),
                inventory.getBackFenceCount(),
                inventory.getBowAndArrowCount(),
                inventory.getWoodFenceMaterialCount(),
                inventory.getBambooFenceMaterialCount()
            );
            
            gameClient.sendMessage(message);
        }
        
        // Auto-deselect if selected item count reaches 0
        checkAndAutoDeselect();
    }
    
    /**
     * Public method to send inventory update to server.
     * Used when inventory is modified outside of InventoryManager (e.g., planting system).
     */
    public void sendInventoryUpdateToServer() {
        sendInventoryUpdate();
    }
    
    /**
     * Update inventory from server sync message.
     * Used to synchronize inventory state with authoritative server.
     * @param appleCount The apple count from server
     * @param bananaCount The banana count from server
     * @param appleSaplingCount The apple sapling count from server
     * @param bambooSaplingCount The baby bamboo count from server
     * @param bambooStackCount The bamboo stack count from server
     * @param treeSaplingCount The baby tree count from server
     * @param woodStackCount The wood stack count from server
     * @param pebbleCount The pebble count from server
     * @param palmFiberCount The palm fiber count from server
     * @param fishCount The fish count from server
     * @param frontFenceCount The front fence count from server
     * @param backFenceCount The back fence count from server
     * @param bowAndArrowCount The bow and arrow count from server
     */
    public void syncFromServer(int appleCount, int bananaCount, int appleSaplingCount, int bananaSaplingCount,
                                int bambooSaplingCount, int bambooStackCount, int treeSaplingCount, int woodStackCount, 
                                int pebbleCount, int palmFiberCount, int fishCount, int frontFenceCount, int backFenceCount, int bowAndArrowCount,
                                int woodFenceMaterialCount, int bambooFenceMaterialCount) {
        if (!isMultiplayerMode) {
            return; // Only sync in multiplayer mode
        }
        
        Inventory inventory = getCurrentInventory();
        inventory.setAppleCount(appleCount);
        inventory.setBananaCount(bananaCount);
        inventory.setAppleSaplingCount(appleSaplingCount);
        inventory.setBananaSaplingCount(bananaSaplingCount);
        inventory.setBambooSaplingCount(bambooSaplingCount);
        inventory.setBambooStackCount(bambooStackCount);
        inventory.setTreeSaplingCount(treeSaplingCount);
        inventory.setWoodStackCount(woodStackCount);
        inventory.setPebbleCount(pebbleCount);
        inventory.setPalmFiberCount(palmFiberCount);
        inventory.setFishCount(fishCount);
        inventory.setFrontFenceCount(frontFenceCount);
        inventory.setBackFenceCount(backFenceCount);
        inventory.setBowAndArrowCount(bowAndArrowCount);
        inventory.setWoodFenceMaterialCount(woodFenceMaterialCount);
        inventory.setBambooFenceMaterialCount(bambooFenceMaterialCount);
        
        System.out.println("Inventory synced from server: Apples=" + appleCount +
                         ", Bananas=" + bananaCount +
                         ", AppleSapling=" + appleSaplingCount +
                         ", BananaSapling=" + bananaSaplingCount +
                         ", BambooSapling=" + bambooSaplingCount +
                         ", BambooStack=" + bambooStackCount +
                         ", TreeSapling=" + treeSaplingCount +
                         ", WoodStack=" + woodStackCount +
                         ", Pebbles=" + pebbleCount +
                         ", PalmFibers=" + palmFiberCount +
                         ", Fish=" + fishCount +
                         ", FrontFence=" + frontFenceCount +
                         ", BackFence=" + backFenceCount +
                         ", BowAndArrow=" + bowAndArrowCount +
                         ", WoodFenceMaterial=" + woodFenceMaterialCount +
                         ", BambooFenceMaterial=" + bambooFenceMaterialCount);
    }
    
    /**
     * Set the selected inventory slot.
     * @param slot The slot index (0-13 for valid slots, any other value clears selection)
     */
    public void setSelectedSlot(int slot) {
        if (slot >= 0 && slot <= 15) {
            this.selectedSlot = slot;
        } else {
            this.selectedSlot = -1; // Clear selection
        }
    }
    
    /**
     * Get the currently selected inventory slot.
     * @return The selected slot index (0-15), or -1 if no slot is selected
     */
    public int getSelectedSlot() {
        return selectedSlot;
    }
    
    /**
     * Clear the current inventory selection.
     * Also deactivates the targeting system if it's currently active.
     */
    public void clearSelection() {
        this.selectedSlot = -1;
        
        // Deactivate targeting system when selection is cleared
        if (player != null && player.getTargetingSystem() != null && player.getTargetingSystem().isActive()) {
            player.getTargetingSystem().deactivate();
        }
    }
    
    /**
     * Get the item type for the currently selected slot.
     * @return The ItemType for the selected slot, or null if no slot is selected
     */
    public ItemType getSelectedItemType() {
        switch (selectedSlot) {
            case 0: return ItemType.APPLE;
            case 1: return ItemType.BANANA;
            case 2: return ItemType.BABY_BAMBOO;
            case 3: return ItemType.BAMBOO_STACK;
            case 4: return ItemType.BABY_TREE;
            case 5: return ItemType.WOOD_STACK;
            case 6: return ItemType.PEBBLE;
            case 7: return ItemType.PALM_FIBER;
            case 8: return ItemType.APPLE_SAPLING;
            case 9: return ItemType.BANANA_SAPLING;
            case 10: return ItemType.FISH;
            case 11: return ItemType.FRONT_FENCE;
            case 12: return ItemType.BACK_FENCE;
            case 13: return ItemType.BOW_AND_ARROW;
            case 14: return ItemType.WOOD_FENCE_MATERIAL;
            case 15: return ItemType.BAMBOO_FENCE_MATERIAL;
            default: return null;
        }
    }
    
    /**
     * Try to consume the currently selected item.
     * Handles apple consumption (restore 10% health) and banana consumption (reduce 5% hunger).
     * Removes the item from inventory if consumption is successful.
     * Requirements: 1.2, 2.1
     * 
     * @param player The player consuming the item
     * @return true if item was consumed successfully, false otherwise
     */
    public boolean tryConsumeSelectedItem(Player player) {
        if (player == null) {
            return false;
        }
        
        int selectedSlot = getSelectedSlot();
        ItemType itemType = getSelectedItemType();
        
        if (itemType == null) {
            return false;
        }
        
        Inventory inventory = getCurrentInventory();
        
        // Handle apple consumption
        if (itemType == ItemType.APPLE) {
            if (inventory.removeApple(1)) {
                // Restore 10% health (capped at 100%)
                float newHealth = Math.min(100, player.getHealth() + 10);
                player.setHealth(newHealth);
                
                // Send inventory update in multiplayer
                sendInventoryUpdate();
                
                // Auto-deselect if item count reaches 0
                checkAndAutoDeselect();
                return true;
            }
        }
        // Handle banana consumption
        else if (itemType == ItemType.BANANA) {
            if (inventory.removeBanana(1)) {
                // Reduce 5% hunger (minimum 0%)
                float newHunger = Math.max(0, player.getHunger() - 5);
                player.setHunger(newHunger);
                
                // Send inventory update in multiplayer
                sendInventoryUpdate();
                
                // Auto-deselect if item count reaches 0
                checkAndAutoDeselect();
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if the currently selected item has 0 count and auto-deselect if so.
     * Also notifies the player to update the targeting system when auto-deselection occurs.
     */
    public void checkAndAutoDeselect() {
        if (selectedSlot == -1) {
            return; // No selection to check
        }
        
        Inventory inventory = getCurrentInventory();
        int itemCount = 0;
        
        switch (selectedSlot) {
            case 0: itemCount = inventory.getAppleCount(); break;
            case 1: itemCount = inventory.getBananaCount(); break;
            case 2: itemCount = inventory.getBambooSaplingCount(); break;
            case 3: itemCount = inventory.getBambooStackCount(); break;
            case 4: itemCount = inventory.getTreeSaplingCount(); break;
            case 5: itemCount = inventory.getWoodStackCount(); break;
            case 6: itemCount = inventory.getPebbleCount(); break;
            case 7: itemCount = inventory.getPalmFiberCount(); break;
            case 8: itemCount = inventory.getAppleSaplingCount(); break;
            case 9: itemCount = inventory.getBananaSaplingCount(); break;
            case 10: itemCount = inventory.getFishCount(); break;
            case 11: itemCount = inventory.getFrontFenceCount(); break;
            case 12: itemCount = inventory.getBackFenceCount(); break;
            case 13: itemCount = inventory.getBowAndArrowCount(); break;
            case 14: itemCount = inventory.getWoodFenceMaterialCount(); break;
            case 15: itemCount = inventory.getBambooFenceMaterialCount(); break;
        }
        
        if (itemCount == 0) {
            clearSelection();
            
            // Notify player to deactivate targeting system when auto-deselection occurs
            if (player != null && player.getTargetingSystem() != null && player.getTargetingSystem().isActive()) {
                player.getTargetingSystem().deactivate();
                System.out.println("Auto-deselected item and deactivated targeting system");
            }
        }
    }
}

