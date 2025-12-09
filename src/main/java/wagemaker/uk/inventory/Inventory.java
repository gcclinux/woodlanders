package wagemaker.uk.inventory;

/**
 * Represents a player's inventory storage for collected items.
 * Maintains separate counts for each item type and provides methods to add/remove items.
 * Prevents item counts from becoming negative.
 */
public class Inventory {
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
    
    public Inventory() {
        this.appleCount = 0;
        this.bananaCount = 0;
        this.appleSaplingCount = 0;
        this.bananaSaplingCount = 0;
        this.bambooSaplingCount = 0;
        this.bambooStackCount = 0;
        this.treeSaplingCount = 0;
        this.woodStackCount = 0;
        this.pebbleCount = 0;
        this.palmFiberCount = 0;
        this.fishCount = 0;
        this.frontFenceCount = 0;
        this.backFenceCount = 0;
        this.bowAndArrowCount = 0;
    }
    
    // Apple methods
    public int getAppleCount() {
        return appleCount;
    }
    
    public void setAppleCount(int count) {
        this.appleCount = Math.max(0, count);
    }
    
    public void addApple(int amount) {
        this.appleCount += amount;
    }
    
    public boolean removeApple(int amount) {
        if (appleCount >= amount) {
            appleCount -= amount;
            return true;
        }
        return false;
    }
    
    // Banana methods
    public int getBananaCount() {
        return bananaCount;
    }
    
    public void setBananaCount(int count) {
        this.bananaCount = Math.max(0, count);
    }
    
    public void addBanana(int amount) {
        this.bananaCount += amount;
    }
    
    public boolean removeBanana(int amount) {
        if (bananaCount >= amount) {
            bananaCount -= amount;
            return true;
        }
        return false;
    }
    
    // AppleSapling methods
    public int getAppleSaplingCount() {
        return appleSaplingCount;
    }
    
    public void setAppleSaplingCount(int count) {
        this.appleSaplingCount = Math.max(0, count);
    }
    
    public void addAppleSapling(int amount) {
        this.appleSaplingCount += amount;
    }
    
    public boolean removeAppleSapling(int amount) {
        if (appleSaplingCount >= amount) {
            appleSaplingCount -= amount;
            return true;
        }
        return false;
    }
    
    // BananaSapling methods
    public int getBananaSaplingCount() {
        return bananaSaplingCount;
    }
    
    public void setBananaSaplingCount(int count) {
        this.bananaSaplingCount = Math.max(0, count);
    }
    
    public void addBananaSapling(int amount) {
        this.bananaSaplingCount += amount;
    }
    
    public boolean removeBananaSapling(int amount) {
        if (bananaSaplingCount >= amount) {
            bananaSaplingCount -= amount;
            return true;
        }
        return false;
    }
    
    // BambooSapling methods
    public int getBambooSaplingCount() {
        return bambooSaplingCount;
    }
    
    public void setBambooSaplingCount(int count) {
        this.bambooSaplingCount = Math.max(0, count);
    }
    
    public void addBambooSapling(int amount) {
        this.bambooSaplingCount += amount;
    }
    
    public boolean removeBambooSapling(int amount) {
        if (bambooSaplingCount >= amount) {
            bambooSaplingCount -= amount;
            return true;
        }
        return false;
    }
    
    // BambooStack methods
    public int getBambooStackCount() {
        return bambooStackCount;
    }
    
    public void setBambooStackCount(int count) {
        this.bambooStackCount = Math.max(0, count);
    }
    
    public void addBambooStack(int amount) {
        this.bambooStackCount += amount;
    }
    
    public boolean removeBambooStack(int amount) {
        if (bambooStackCount >= amount) {
            bambooStackCount -= amount;
            return true;
        }
        return false;
    }
    
    // TreeSapling methods
    public int getTreeSaplingCount() {
        return treeSaplingCount;
    }
    
    public void setTreeSaplingCount(int count) {
        this.treeSaplingCount = Math.max(0, count);
    }
    
    public void addTreeSapling(int amount) {
        this.treeSaplingCount += amount;
    }
    
    public boolean removeTreeSapling(int amount) {
        if (treeSaplingCount >= amount) {
            treeSaplingCount -= amount;
            return true;
        }
        return false;
    }
    
    // WoodStack methods
    public int getWoodStackCount() {
        return woodStackCount;
    }
    
    public void setWoodStackCount(int count) {
        this.woodStackCount = Math.max(0, count);
    }
    
    public void addWoodStack(int amount) {
        this.woodStackCount += amount;
    }
    
    public boolean removeWoodStack(int amount) {
        if (woodStackCount >= amount) {
            woodStackCount -= amount;
            return true;
        }
        return false;
    }
    
    // Pebble methods
    public int getPebbleCount() {
        return pebbleCount;
    }
    
    public void setPebbleCount(int count) {
        this.pebbleCount = Math.max(0, count);
    }
    
    public void addPebble(int amount) {
        this.pebbleCount += amount;
    }
    
    public boolean removePebble(int amount) {
        if (pebbleCount >= amount) {
            pebbleCount -= amount;
            return true;
        }
        return false;
    }
    
    // PalmFiber methods
    public int getPalmFiberCount() {
        return palmFiberCount;
    }
    
    public void setPalmFiberCount(int count) {
        this.palmFiberCount = Math.max(0, count);
    }
    
    public void addPalmFiber(int amount) {
        this.palmFiberCount += amount;
    }
    
    public boolean removePalmFiber(int amount) {
        if (palmFiberCount >= amount) {
            palmFiberCount -= amount;
            return true;
        }
        return false;
    }
    
    // Fish methods
    public int getFishCount() {
        return fishCount;
    }
    
    public void setFishCount(int count) {
        this.fishCount = Math.max(0, count);
    }
    
    public void addFish(int amount) {
        this.fishCount += amount;
    }
    
    public boolean removeFish(int amount) {
        if (fishCount >= amount) {
            fishCount -= amount;
            return true;
        }
        return false;
    }
    
    // FrontFence methods
    public int getFrontFenceCount() {
        return frontFenceCount;
    }
    
    public void setFrontFenceCount(int count) {
        this.frontFenceCount = Math.max(0, count);
    }
    
    public void addFrontFence(int amount) {
        this.frontFenceCount += amount;
    }
    
    public boolean removeFrontFence(int amount) {
        if (frontFenceCount >= amount) {
            frontFenceCount -= amount;
            return true;
        }
        return false;
    }
    
    // BackFence methods
    public int getBackFenceCount() {
        return backFenceCount;
    }
    
    public void setBackFenceCount(int count) {
        this.backFenceCount = Math.max(0, count);
    }
    
    public void addBackFence(int amount) {
        this.backFenceCount += amount;
    }
    
    public boolean removeBackFence(int amount) {
        if (backFenceCount >= amount) {
            backFenceCount -= amount;
            return true;
        }
        return false;
    }
    
    // BowAndArrow methods
    public int getBowAndArrowCount() {
        return bowAndArrowCount;
    }
    
    public void setBowAndArrowCount(int count) {
        this.bowAndArrowCount = Math.max(0, count);
    }
    
    public void addBowAndArrow(int amount) {
        this.bowAndArrowCount += amount;
    }
    
    public boolean removeBowAndArrow(int amount) {
        if (bowAndArrowCount >= amount) {
            bowAndArrowCount -= amount;
            return true;
        }
        return false;
    }
    
    /**
     * Clear all items from inventory, resetting all counts to 0.
     */
    public void clear() {
        this.appleCount = 0;
        this.bananaCount = 0;
        this.appleSaplingCount = 0;
        this.bananaSaplingCount = 0;
        this.bambooSaplingCount = 0;
        this.bambooStackCount = 0;
        this.treeSaplingCount = 0;
        this.woodStackCount = 0;
        this.pebbleCount = 0;
        this.palmFiberCount = 0;
        this.fishCount = 0;
        this.frontFenceCount = 0;
        this.backFenceCount = 0;
        this.bowAndArrowCount = 0;
    }
}
