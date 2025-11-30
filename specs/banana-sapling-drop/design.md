# Design Document: Banana Tree Dual Item Drops

## Overview

This design extends the existing single-item drop system for BananaTree to support dual-item drops. Currently, BananaTree only drops a Banana when destroyed. This implementation will add BananaSapling as a second drop, following the established patterns used by AppleTree (Apple + AppleSapling), BambooTree (BambooStack + BambooSapling), and SmallTree (TreeSapling + WoodStack). The items will be positioned with a small horizontal offset to make them visually distinct and collectible.

The design maintains consistency with the existing codebase architecture:
- Item classes follow the same structure as other items (Apple, Banana, etc.)
- Rendering uses the same batch.draw() pattern with specified dimensions
- Pickup detection uses the same collision detection logic
- Multiplayer synchronization follows the existing item spawn/pickup messaging system
- Inventory integration follows the established ItemType enum pattern

## Architecture

### Component Interaction Flow

```
Player attacks BananaTree
    ↓
BananaTree.attack() returns true (health <= 0)
    ↓
Player.attackNearbyTargets() detects destruction
    ↓
[Single-Player Mode]                    [Multiplayer Mode]
    ↓                                       ↓
Create Banana at (x, y)                Server handles destruction
Create BananaSapling at (x+8, y)       Server spawns items
Add to collections                     Broadcasts ItemSpawnMessage
    ↓                                       ↓
MyGdxGame.render()                     Client receives messages
    ↓                                   Adds items to collections
drawBananas()                               ↓
drawBananaSaplings()                     MyGdxGame.render()
    ↓                                       ↓
Player walks over items                drawBananas()
    ↓                                   drawBananaSaplings()
checkBananaPickups()                        ↓
checkBananaSaplingPickups()               Player walks over items
    ↓                                       ↓
Remove from collections                checkBananaPickups()
Add to inventory                       checkBananaSaplingPickups()
Dispose textures                            ↓
                                       Send ItemPickupMessage
                                       Server broadcasts removal
                                       Client removes from collections
                                       Client adds to inventory
```

## Components and Interfaces

### 1. BananaSapling Item Class

**Location:** `src/main/java/wagemaker/uk/items/BananaSapling.java`

**Status:** Already exists

**Current Implementation:**
- Extracts texture from sprite sheet at (192, 192) with 64x64 dimensions
- Stores position (x, y)
- Provides getters for texture and position
- Implements dispose() for texture cleanup

**No changes needed** - The existing implementation is correct.

### 2. MyGdxGame Class Modifications

**Location:** `src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

**Changes Required:**

#### Verify BananaSapling Collection Exists
```java
Map<String, BananaSapling> bananaSaplings;
```

#### Verify Collection Initialization in create()
```java
bananaSaplings = new HashMap<>();
```

#### Add Rendering Method
```java
private void drawBananaSaplings() {
    float camX = camera.position.x;
    float camY = camera.position.y;
    float viewWidth = viewport.getWorldWidth() / 2;
    float viewHeight = viewport.getWorldHeight() / 2;
    
    for (BananaSapling bananaSapling : bananaSaplings.values()) {
        if (Math.abs(bananaSapling.getX() - camX) < viewWidth && 
            Math.abs(bananaSapling.getY() - camY) < viewHeight) {
            batch.draw(bananaSapling.getTexture(), bananaSapling.getX(), bananaSapling.getY(), 32, 32);
        }
    }
}
```

#### Call Rendering Method in render()
```java
drawBananaSaplings(); // After drawBananas()
```

### 3. Player Class Modifications

**Location:** `src/main/java/wagemaker/uk/player/Player.java`

**Changes Required:**

#### Add BananaSapling Collection Reference
```java
private Map<String, BananaSapling> bananaSaplings;
```

#### Add Setter Method
```java
public void setBananaSaplings(Map<String, BananaSapling> bananaSaplings) {
    this.bananaSaplings = bananaSaplings;
}
```

#### Modify BananaTree Destruction Block

Add dual-item drop logic when BananaTree is destroyed:
```java
if (destroyed) {
    // Spawn Banana at tree position
    bananas.put(targetKey, new Banana(targetBananaTree.getX(), targetBananaTree.getY()));
    
    // Spawn BananaSapling offset by 8 pixels horizontally
    bananaSaplings.put(targetKey + "-bananasapling", 
        new BananaSapling(targetBananaTree.getX() + 8, targetBananaTree.getY()));
    
    System.out.println("Banana tree destroyed! Banana dropped at: " + 
        targetBananaTree.getX() + ", " + targetBananaTree.getY());
    System.out.println("BananaSapling dropped at: " + 
        (targetBananaTree.getX() + 8) + ", " + targetBananaTree.getY());
    
    // Register for respawn before removing
    if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
        wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
        wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
        if (respawnManager != null) {
            respawnManager.registerDestruction(
                targetKey,
                wagemaker.uk.respawn.ResourceType.TREE,
                targetBananaTree.getX(),
                targetBananaTree.getY(),
                wagemaker.uk.network.TreeType.BANANA
            );
        }
    }
    
    targetBananaTree.dispose();
    bananaTrees.remove(targetKey);
    clearedPositions.put(targetKey, true);
}
```

#### Add Pickup Check Method
```java
private void checkBananaSaplingPickups() {
    if (bananaSaplings != null) {
        for (Map.Entry<String, BananaSapling> entry : bananaSaplings.entrySet()) {
            BananaSapling bananaSapling = entry.getValue();
            String bananaSaplingKey = entry.getKey();
            
            // Check if player is close enough to pick up (32px range)
            float dx = Math.abs((x + 32) - (bananaSapling.getX() + 16)); // 32x32 item, center at +16
            float dy = Math.abs((y + 32) - (bananaSapling.getY() + 16));
            
            if (dx <= 32 && dy <= 32) {
                pickupBananaSapling(bananaSaplingKey);
                break;
            }
        }
    }
}

private void pickupBananaSapling(String bananaSaplingKey) {
    if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
        gameClient.sendItemPickup(bananaSaplingKey);
    } else {
        // Single-player mode: handle locally
        System.out.println("BananaSapling picked up!");
        
        if (bananaSaplings.containsKey(bananaSaplingKey)) {
            BananaSapling bananaSapling = bananaSaplings.get(bananaSaplingKey);
            bananaSapling.dispose();
            bananaSaplings.remove(bananaSaplingKey);
            
            // Add to inventory
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.BANANA_SAPLING);
            }
            
            System.out.println("BananaSapling removed from game and added to inventory");
        }
    }
}
```

#### Call Pickup Check in update()
```java
checkBananaSaplingPickups(); // After checkBananaPickups()
```

#### Wire Collection in MyGdxGame.create()
```java
player.setBananaSaplings(bananaSaplings); // After player.setBananas()
```

### 4. Inventory ItemType Enum Modifications

**Location:** `src/main/java/wagemaker/uk/inventory/ItemType.java`

**Changes Required:**

Add BANANA_SAPLING entry:
```java
BANANA_SAPLING(false, 0, false), // After BANANA entry
```

### 5. Network ItemType Enum Modifications

**Location:** `src/main/java/wagemaker/uk/network/ItemType.java`

**Changes Required:**

Add BANANA_SAPLING entry:
```java
BANANA_SAPLING, // After BANANA entry
```

### 5.5. PlayerState Class Modifications

**Location:** `src/main/java/wagemaker/uk/network/PlayerState.java`

**Changes Required:**

Add BananaSapling count field and methods:
```java
private int bananaSaplingCount;

public int getBananaSaplingCount() {
    return bananaSaplingCount;
}

public void setBananaSaplingCount(int bananaSaplingCount) {
    this.bananaSaplingCount = bananaSaplingCount;
}
```

**Rationale:** PlayerState stores inventory counts for multiplayer synchronization. When the server broadcasts inventory updates or syncs player state, it needs to include the BananaSapling count so all clients have consistent inventory data.

### 6. Inventory Class Modifications

**Location:** `src/main/java/wagemaker/uk/inventory/Inventory.java`

**Changes Required:**

Add BananaSapling count field and methods:
```java
private int bananaSaplingCount = 0;

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
```

### 7. InventoryManager Class Modifications

**Location:** `src/main/java/wagemaker/uk/inventory/InventoryManager.java`

**Changes Required:**

#### Update addItemToInventory() method
Add case for BANANA_SAPLING:
```java
case BANANA_SAPLING:
    inventory.addBananaSapling(amount);
    break;
```

#### Update syncFromServer() method
Add bananaSaplingCount parameter and sync logic:
```java
public void syncFromServer(int appleCount, int bananaCount, int appleSaplingCount, 
                          int bananaSaplingCount, int bambooSaplingCount, int bambooStackCount, 
                          int treeSaplingCount, int woodStackCount, 
                          int pebbleCount, int palmFiberCount) {
    // ... existing code ...
    inventory.setBananaSaplingCount(bananaSaplingCount);
    // ... rest of sync logic ...
}
```

#### Update getSelectedItemType() method
Add case for BananaSapling slot (slot 9):
```java
case 9: return ItemType.BANANA_SAPLING;
```

#### Update checkAndAutoDeselect() method
Add case for BananaSapling:
```java
case 9: itemCount = inventory.getBananaSaplingCount(); break;
```

## Data Models

### Item Positioning

**Banana Position:**
- X: `bananaTree.getX()` (tree's base X coordinate)
- Y: `bananaTree.getY()` (tree's base Y coordinate)

**BananaSapling Position:**
- X: `bananaTree.getX() + 8` (8 pixels right of Banana)
- Y: `bananaTree.getY()` (same Y as Banana)

**Rationale:** 8-pixel horizontal offset provides visual separation while keeping both items close to the tree's base position. This makes both items easily discoverable and collectible, and matches the pattern used by AppleTree, BambooTree, and SmallTree.

### Item Identifiers

Items use the tree's position key with suffixes:
- Banana: `"{x},{y}"` (existing pattern)
- BananaSapling: `"{x},{y}-bananasapling"`

**Example:** Tree at (256, 512) produces:
- `"256,512"` (Banana)
- `"256,512-bananasapling"` (BananaSapling)

This ensures unique identifiers and maintains traceability to the source tree.

### Render Dimensions

Both items render at **32x32 pixels** on screen:
- Source texture: 64x64 from sprite sheet
- Rendered size: 32x32 (50% scale)
- Collision center: +16 pixels from item position

This matches the existing item rendering approach and provides appropriate visual size for ground items.

### Inventory Storage

BananaSapling items are stored in the inventory system:
- Field: `bananaSaplingCount` (integer)
- ItemType: `BANANA_SAPLING`
- Network ItemType: `BANANA_SAPLING`
- Inventory slot: 9 (after APPLE_SAPLING)

### World Save Data

BananaSapling count must be persisted in save files:
- Save: Include `bananaSaplingCount` in WorldSaveData
- Load: Restore `bananaSaplingCount` from WorldSaveData

## Error Handling

### Null Safety

All item collection access includes null checks:
```java
if (bananaSaplings != null) {
    // Access collection
}
```

This prevents NullPointerException if collections aren't initialized.

### Texture Disposal

Items must be disposed when removed:
```java
bananaSapling.dispose();
bananaSaplings.remove(key);
```

Failure to dispose causes memory leaks as textures remain in GPU memory.

### Multiplayer Synchronization

In multiplayer mode:
- Client sends pickup request via `gameClient.sendItemPickup(key)`
- Server validates and broadcasts removal
- Client waits for server confirmation before removing item

This prevents desync where items appear picked up locally but still exist on server.

### Inventory Integration

When picking up BananaSapling:
- Check if inventoryManager is not null before calling collectItem()
- Use correct ItemType enum value (BANANA_SAPLING)
- Inventory automatically handles count increment and persistence

## Testing Strategy

### Manual Testing Checklist

**Single-Player Mode:**
1. Start single-player game
2. Find and attack a BananaTree until destroyed
3. Verify two items spawn at tree position
4. Verify items are positioned 8 pixels apart horizontally
5. Verify items render at 32x32 pixels
6. Walk over Banana - verify pickup
7. Walk over BananaSapling - verify pickup and inventory increase
8. Verify console logs show correct positions and pickup messages
9. Save game and verify BananaSapling count persists
10. Load game and verify BananaSapling count restored

**Multiplayer Mode:**
1. Start server and connect client
2. Client attacks BananaTree until destroyed
3. Verify both items spawn on client
4. Verify items appear on other connected clients
5. Client picks up Banana
6. Verify removal on all clients
7. Client picks up BananaSapling
8. Verify removal on all clients
9. Verify inventory synchronization across clients

**Visual Verification:**
1. Compare Banana sprite to expected sprite sheet coordinates (64, 128)
2. Compare BananaSapling sprite to expected sprite sheet coordinates (192, 192)
3. Verify 8-pixel horizontal spacing between items
4. Verify items render at appropriate size (32x32)

## Implementation Notes

### Code Consistency

The implementation maintains consistency with existing patterns:
- Item classes match Apple/AppleSapling structure exactly
- Rendering follows the same batch.draw() pattern
- Pickup detection uses identical collision logic
- Multiplayer messaging reuses existing ItemPickupMessage system
- Inventory integration follows established ItemType enum pattern

### Performance Considerations

**Texture Loading:**
- Each item creates its own texture from sprite sheet
- Consider texture caching if many items spawn simultaneously
- Current approach matches existing Apple/Banana implementation

**Collision Detection:**
- Pickup checks iterate all items each frame
- Acceptable for small-to-medium item counts
- Breaks after first pickup to limit iterations

**Rendering:**
- Viewport culling prevents off-screen rendering
- Only items within camera view are drawn
- Matches existing optimization strategy
