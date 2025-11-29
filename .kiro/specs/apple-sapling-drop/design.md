# Design Document: Apple Tree Dual Item Drops

## Overview

This design extends the existing single-item drop system for AppleTree to support dual-item drops. Currently, AppleTree only drops an Apple when destroyed. This implementation will add AppleSapling as a second drop, following the established patterns used by BambooTree (drops BambooStack + BambooSapling) and SmallTree (drops TreeSapling + WoodStack). The items will be positioned with a small horizontal offset to make them visually distinct and collectible.

The design maintains consistency with the existing codebase architecture:
- Item classes follow the same structure as other items (Apple, Banana, etc.)
- Rendering uses the same batch.draw() pattern with specified dimensions
- Pickup detection uses the same collision detection logic
- Multiplayer synchronization follows the existing item spawn/pickup messaging system
- Inventory integration follows the established ItemType enum pattern

## Architecture

### Component Interaction Flow

```
Player attacks AppleTree
    ↓
AppleTree.attack() returns true (health <= 0)
    ↓
Player.attackNearbyTargets() detects destruction
    ↓
[Single-Player Mode]                    [Multiplayer Mode]
    ↓                                       ↓
Create Apple at (x, y)                 Server handles destruction
Create AppleSapling at (x+8, y)        Server spawns items
Add to collections                     Broadcasts ItemSpawnMessage
    ↓                                       ↓
MyGdxGame.render()                     Client receives messages
    ↓                                   Adds items to collections
drawApples()                                ↓
drawAppleSaplings()                      MyGdxGame.render()
    ↓                                       ↓
Player walks over items                drawApples()
    ↓                                   drawAppleSaplings()
checkApplePickups()                         ↓
checkAppleSaplingPickups()                Player walks over items
    ↓                                       ↓
Remove from collections                checkApplePickups()
Add to inventory                       checkAppleSaplingPickups()
Dispose textures                            ↓
                                       Send ItemPickupMessage
                                       Server broadcasts removal
                                       Client removes from collections
                                       Client adds to inventory
```

## Components and Interfaces

### 1. AppleSapling Item Class

**Location:** `src/main/java/wagemaker/uk/items/AppleSapling.java`

**Status:** Already exists

**Current Implementation:**
- Extracts texture from sprite sheet at (192, 254) with 64x64 dimensions
- Stores position (x, y)
- Provides getters for texture and position
- Implements dispose() for texture cleanup

**No changes needed** - The existing implementation is correct.

### 2. MyGdxGame Class Modifications

**Location:** `src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

**Changes Required:**

#### Add AppleSapling Collection
```java
Map<String, AppleSapling> appleSaplings;
```

#### Initialize Collection in create()
```java
appleSaplings = new HashMap<>();
```

#### Add Rendering Method
```java
private void drawAppleSaplings() {
    float camX = camera.position.x;
    float camY = camera.position.y;
    float viewWidth = viewport.getWorldWidth() / 2;
    float viewHeight = viewport.getWorldHeight() / 2;
    
    for (AppleSapling appleSapling : appleSaplings.values()) {
        if (Math.abs(appleSapling.getX() - camX) < viewWidth && 
            Math.abs(appleSapling.getY() - camY) < viewHeight) {
            batch.draw(appleSapling.getTexture(), appleSapling.getX(), appleSapling.getY(), 32, 32);
        }
    }
}
```

#### Call Rendering Method in render()
```java
drawAppleSaplings(); // After drawApples()
```

### 3. Player Class Modifications

**Location:** `src/main/java/wagemaker/uk/player/Player.java`

**Changes Required:**

#### Add AppleSapling Collection Reference
```java
private Map<String, AppleSapling> appleSaplings;
```

#### Add Setter Method
```java
public void setAppleSaplings(Map<String, AppleSapling> appleSaplings) {
    this.appleSaplings = appleSaplings;
}
```

#### Modify AppleTree Destruction Block (around line 932)

Replace the current apple drop code with:
```java
if (destroyed) {
    // Spawn Apple at tree position
    apples.put(targetKey, new Apple(targetAppleTree.getX(), targetAppleTree.getY()));
    
    // Spawn AppleSapling offset by 8 pixels horizontally
    appleSaplings.put(targetKey + "-applesapling", 
        new AppleSapling(targetAppleTree.getX() + 8, targetAppleTree.getY()));
    
    System.out.println("Apple tree destroyed! Apple dropped at: " + 
        targetAppleTree.getX() + ", " + targetAppleTree.getY());
    System.out.println("AppleSapling dropped at: " + 
        (targetAppleTree.getX() + 8) + ", " + targetAppleTree.getY());
    
    // Register for respawn before removing
    if (respawnManager != null) {
        respawnManager.registerResourceDestruction(
            targetKey,
            wagemaker.uk.respawn.ResourceType.TREE,
            targetAppleTree.getX(),
            targetAppleTree.getY(),
            128,
            wagemaker.uk.network.TreeType.APPLE
        );
    }
    
    targetAppleTree.dispose();
    appleTrees.remove(targetKey);
    clearedPositions.put(targetKey, true);
}
```

#### Add Pickup Check Method
```java
private void checkAppleSaplingPickups() {
    if (appleSaplings != null) {
        for (Map.Entry<String, AppleSapling> entry : appleSaplings.entrySet()) {
            AppleSapling appleSapling = entry.getValue();
            String appleSaplingKey = entry.getKey();
            
            // Check if player is close enough to pick up (32px range)
            float dx = Math.abs((x + 32) - (appleSapling.getX() + 16)); // 32x32 item, center at +16
            float dy = Math.abs((y + 32) - (appleSapling.getY() + 16));
            
            if (dx <= 32 && dy <= 32) {
                pickupAppleSapling(appleSaplingKey);
                break;
            }
        }
    }
}

private void pickupAppleSapling(String appleSaplingKey) {
    if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
        gameClient.sendItemPickup(appleSaplingKey);
    } else {
        // Single-player mode: handle locally
        System.out.println("AppleSapling picked up!");
        
        if (appleSaplings.containsKey(appleSaplingKey)) {
            AppleSapling appleSapling = appleSaplings.get(appleSaplingKey);
            appleSapling.dispose();
            appleSaplings.remove(appleSaplingKey);
            
            // Add to inventory
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.APPLE_SAPLING);
            }
            
            System.out.println("AppleSapling removed from game and added to inventory");
        }
    }
}
```

#### Call Pickup Check in update()
```java
checkAppleSaplingPickups(); // After checkApplePickups()
```

#### Wire Collection in MyGdxGame.create()
```java
player.setAppleSaplings(appleSaplings); // After player.setApples()
```

### 4. Inventory ItemType Enum Modifications

**Location:** `src/main/java/wagemaker/uk/inventory/ItemType.java`

**Changes Required:**

Add APPLE_SAPLING entry:
```java
APPLE_SAPLING(false, 0, false), // After APPLE entry
```

### 5. Network ItemType Enum Modifications

**Location:** `src/main/java/wagemaker/uk/network/ItemType.java`

**Changes Required:**

Add APPLE_SAPLING entry:
```java
APPLE_SAPLING, // After APPLE entry
```

### 5.5. PlayerState Class Modifications

**Location:** `src/main/java/wagemaker/uk/network/PlayerState.java`

**Changes Required:**

Add AppleSapling count field and methods:
```java
private int appleSaplingCount;

public int getAppleSaplingCount() {
    return appleSaplingCount;
}

public void setAppleSaplingCount(int appleSaplingCount) {
    this.appleSaplingCount = appleSaplingCount;
}
```

**Rationale:** PlayerState stores inventory counts for multiplayer synchronization. When the server broadcasts inventory updates or syncs player state, it needs to include the AppleSapling count so all clients have consistent inventory data.

**Additional Changes Required:**

After adding appleSaplingCount to PlayerState, update ClientConnection.java to include the new field when constructing InventoryUpdateMessage and InventorySyncMessage instances:
- Locate all `new InventoryUpdateMessage(...)` calls in ClientConnection
- Add `playerState.getAppleSaplingCount()` parameter after `playerState.getBananaCount()`
- Locate all `new InventorySyncMessage(...)` calls in ClientConnection  
- Add `playerState.getAppleSaplingCount()` parameter after `playerState.getBananaCount()`

### 6. Inventory Class Modifications

**Location:** `src/main/java/wagemaker/uk/inventory/Inventory.java`

**Changes Required:**

Add AppleSapling count field and methods:
```java
private int appleSaplingCount = 0;

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
```

### 7. InventoryManager Class Modifications

**Location:** `src/main/java/wagemaker/uk/inventory/InventoryManager.java`

**Changes Required:**

#### Update addItemToInventory() method
Add case for APPLE_SAPLING:
```java
case APPLE_SAPLING:
    inventory.addAppleSapling(amount);
    break;
```

#### Update syncFromServer() method
Add appleSaplingCount parameter and sync logic:
```java
public void syncFromServer(int appleCount, int bananaCount, int appleSaplingCount, 
                          int bambooSaplingCount, int bambooStackCount, 
                          int treeSaplingCount, int woodStackCount, 
                          int pebbleCount, int palmFiberCount) {
    // ... existing code ...
    inventory.setAppleSaplingCount(appleSaplingCount);
    // ... rest of sync logic ...
}
```

#### Update getSelectedItemType() method
Add case for AppleSapling slot (assuming slot 8):
```java
case 8: return ItemType.APPLE_SAPLING;
```

#### Update checkAndAutoDeselect() method
Add case for AppleSapling:
```java
case 8: itemCount = inventory.getAppleSaplingCount(); break;
```

## Data Models

### Item Positioning

**Apple Position:**
- X: `appleTree.getX()` (tree's base X coordinate)
- Y: `appleTree.getY()` (tree's base Y coordinate)

**AppleSapling Position:**
- X: `appleTree.getX() + 8` (8 pixels right of Apple)
- Y: `appleTree.getY()` (same Y as Apple)

**Rationale:** 8-pixel horizontal offset provides visual separation while keeping both items close to the tree's base position. This makes both items easily discoverable and collectible, and matches the pattern used by BambooTree and SmallTree.

### Item Identifiers

Items use the tree's position key with suffixes:
- Apple: `"{x},{y}"` (existing pattern)
- AppleSapling: `"{x},{y}-applesapling"`

**Example:** Tree at (256, 512) produces:
- `"256,512"` (Apple)
- `"256,512-applesapling"` (AppleSapling)

This ensures unique identifiers and maintains traceability to the source tree.

### Render Dimensions

Both items render at **32x32 pixels** on screen:
- Source texture: 64x64 from sprite sheet
- Rendered size: 32x32 (50% scale)
- Collision center: +16 pixels from item position

This matches the existing item rendering approach and provides appropriate visual size for ground items.

## Data Models

### Inventory Storage

AppleSapling items are stored in the inventory system:
- Field: `appleSaplingCount` (integer)
- ItemType: `APPLE_SAPLING`
- Network ItemType: `APPLE_SAPLING`
- Inventory slot: 8 (after PALM_FIBER)

### World Save Data

AppleSapling count must be persisted in save files:
- Save: Include `appleSaplingCount` in WorldSaveData
- Load: Restore `appleSaplingCount` from WorldSaveData

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Acceptance Criteria Testing Prework:

1.1 WHEN an AppleTree health reaches zero, THE Game System SHALL spawn one Apple item at the tree's base position
  Thoughts: This is testing that when any AppleTree is destroyed, an Apple item is spawned at the correct position. We can generate random AppleTrees, destroy them, and verify an Apple appears at the tree's position.
  Testable: yes - property

1.2 WHEN an AppleTree health reaches zero, THE Game System SHALL spawn one AppleSapling item at the tree's base position offset by 8 pixels horizontally from the Apple
  Thoughts: This is testing that when any AppleTree is destroyed, an AppleSapling item is spawned at the correct offset position. We can generate random AppleTrees, destroy them, and verify an AppleSapling appears at position + 8 pixels.
  Testable: yes - property

1.3 WHERE the game is in single-player mode, THE Player Class SHALL create both item instances and add them to the game's item collections when an AppleTree is destroyed
  Thoughts: This is testing that in single-player mode, both items are created and added to collections. We can destroy a tree and verify both collections contain the expected items.
  Testable: yes - example

1.4 WHERE the game is in multiplayer mode, THE Game System SHALL handle apple item spawning through the server's item spawn messaging system
  Thoughts: This is testing multiplayer synchronization. This requires a running server and client, which is integration testing.
  Testable: no

1.5 WHEN apple items are spawned, THE Game System SHALL use unique identifiers for each item based on the tree's position key with suffixes "-apple" and "-applesapling"
  Thoughts: This is testing that item keys follow the correct naming pattern. We can destroy trees and verify the keys in the collections match the expected format.
  Testable: yes - property

2.1 THE MyGdxGame Class SHALL render Apple items at 32x32 pixels on screen
  Thoughts: This is testing rendering behavior. We would need to inspect the batch.draw() call parameters, which is difficult to test without mocking.
  Testable: no

2.2 THE MyGdxGame Class SHALL render AppleSapling items at 32x32 pixels on screen
  Thoughts: This is testing rendering behavior. Same as 2.1.
  Testable: no

2.3 THE Apple Class SHALL extract its texture from sprite sheet coordinates (0, 128) with source dimensions 64x64
  Thoughts: This is testing that the Apple class uses the correct sprite sheet coordinates. This is already implemented and verified.
  Testable: edge-case

2.4 THE AppleSapling Class SHALL extract its texture from sprite sheet coordinates (192, 254) with source dimensions 64x64
  Thoughts: This is testing that the AppleSapling class uses the correct sprite sheet coordinates. This is already implemented and verified.
  Testable: edge-case

2.5 THE MyGdxGame Class SHALL maintain separate collections for Apple and AppleSapling items to enable independent rendering and collision detection
  Thoughts: This is testing that collections exist and are separate. This is a structural requirement that can be verified by code inspection.
  Testable: no

3.1 WHEN the player's collision box overlaps with an Apple item, THE Player Class SHALL remove the Apple from the game world
  Thoughts: This is testing pickup collision detection for Apples. We can place a player near an Apple and verify it gets removed.
  Testable: yes - property

3.2 WHEN the player's collision box overlaps with an AppleSapling item, THE Player Class SHALL remove the AppleSapling from the game world
  Thoughts: This is testing pickup collision detection for AppleSaplings. We can place a player near an AppleSapling and verify it gets removed.
  Testable: yes - property

3.3 THE Player Class SHALL check for apple item pickups during each update cycle using the same collision detection pattern as other items
  Thoughts: This is testing that pickup checks happen every frame. This is a structural requirement.
  Testable: no

3.4 WHEN an apple item is picked up in multiplayer mode, THE Game System SHALL send an item pickup message to the server
  Thoughts: This is testing multiplayer message sending. This requires network infrastructure.
  Testable: no

3.5 THE Player Class SHALL dispose of apple item textures when items are picked up to prevent memory leaks
  Thoughts: This is testing that dispose() is called. We can verify this by checking that the texture is disposed after pickup.
  Testable: yes - property

3.6 THE Player Class SHALL use a 32-pixel pickup range from the player's center to the item's center
  Thoughts: This is testing the pickup range distance. We can place items at various distances and verify pickup only occurs within 32 pixels.
  Testable: yes - property

4.1 WHEN an AppleSapling item is picked up, THE Inventory System SHALL add one AppleSapling to the player's inventory
  Thoughts: This is testing inventory integration. We can pick up an AppleSapling and verify the inventory count increases by 1.
  Testable: yes - property

4.2 THE Inventory System SHALL support the AppleSapling item type in the ItemType enumeration
  Thoughts: This is a structural requirement that can be verified by code inspection.
  Testable: no

4.3 THE Inventory System SHALL display the AppleSapling count in the inventory UI
  Thoughts: This is testing UI rendering. This requires visual verification.
  Testable: no

4.4 THE Inventory System SHALL allow the player to select AppleSapling from the inventory for planting
  Thoughts: This is testing inventory selection and planting integration. This is out of scope for this feature.
  Testable: no

4.5 WHEN the player saves the game, THE World Save System SHALL persist the AppleSapling inventory count
  Thoughts: This is testing save/load functionality. We can save a game with AppleSaplings in inventory and verify the count is saved.
  Testable: yes - property

4.6 WHEN the player loads a saved game, THE World Save System SHALL restore the AppleSapling inventory count
  Thoughts: This is testing save/load functionality. We can load a saved game and verify the AppleSapling count is restored.
  Testable: yes - property

5.1 THE ItemType Network Enum SHALL include an APPLE_SAPLING entry for network synchronization
  Thoughts: This is a structural requirement that can be verified by code inspection.
  Testable: no

5.2 WHEN an AppleSapling is spawned in multiplayer mode, THE Game Server SHALL broadcast an ItemSpawnMessage with type APPLE_SAPLING
  Thoughts: This is testing multiplayer message broadcasting. This requires network infrastructure.
  Testable: no

5.3 WHEN an AppleSapling is picked up in multiplayer mode, THE Game Server SHALL broadcast an ItemPickupMessage for the AppleSapling
  Thoughts: This is testing multiplayer message broadcasting. This requires network infrastructure.
  Testable: no

5.4 WHEN a client receives an AppleSapling spawn message, THE Game Client SHALL create an AppleSapling instance at the specified position
  Thoughts: This is testing multiplayer message handling. This requires network infrastructure.
  Testable: no

5.5 WHEN a client receives an AppleSapling pickup message, THE Game Client SHALL remove the AppleSapling from the local collection
  Thoughts: This is testing multiplayer message handling. This requires network infrastructure.
  Testable: no

### Property Reflection

After reviewing all testable properties, I've identified the following redundancies:

- Properties 1.1 and 1.2 can be combined into a single property that verifies both items spawn with correct positioning
- Properties 3.1 and 3.2 can be combined into a single property that verifies pickup collision detection works for both items
- Properties 4.5 and 4.6 can be combined into a single round-trip property for save/load

### Correctness Properties

Property 1: Dual item spawn with correct positioning
*For any* AppleTree, when the tree is destroyed, both an Apple and an AppleSapling should spawn, with the Apple at the tree's base position and the AppleSapling offset by 8 pixels horizontally
**Validates: Requirements 1.1, 1.2**

Property 2: Item key naming convention
*For any* AppleTree at position (x, y), when destroyed, the Apple key should be "{x},{y}" and the AppleSapling key should be "{x},{y}-applesapling"
**Validates: Requirements 1.5**

Property 3: Pickup collision detection
*For any* player position and item position, when the distance between player center and item center is <= 32 pixels, the item should be picked up and removed from the game world
**Validates: Requirements 3.1, 3.2, 3.6**

Property 4: Texture disposal on pickup
*For any* item, when picked up, the texture should be disposed to prevent memory leaks
**Validates: Requirements 3.5**

Property 5: Inventory integration
*For any* AppleSapling pickup, the inventory AppleSapling count should increase by 1
**Validates: Requirements 4.1**

Property 6: Save/load round trip
*For any* inventory state with AppleSapling count N, saving then loading should restore the AppleSapling count to N
**Validates: Requirements 4.5, 4.6**

## Error Handling

### Null Safety

All item collection access includes null checks:
```java
if (appleSaplings != null) {
    // Access collection
}
```

This prevents NullPointerException if collections aren't initialized.

### Texture Disposal

Items must be disposed when removed:
```java
appleSapling.dispose();
appleSaplings.remove(key);
```

Failure to dispose causes memory leaks as textures remain in GPU memory.

### Multiplayer Synchronization

In multiplayer mode:
- Client sends pickup request via `gameClient.sendItemPickup(key)`
- Server validates and broadcasts removal
- Client waits for server confirmation before removing item

This prevents desync where items appear picked up locally but still exist on server.

### Inventory Integration

When picking up AppleSapling:
- Check if inventoryManager is not null before calling collectItem()
- Use correct ItemType enum value (APPLE_SAPLING)
- Inventory automatically handles count increment and persistence

## Testing Strategy

### Unit Testing

Unit tests will verify:
- AppleSapling class correctly extracts texture from sprite sheet
- Item positioning calculations are correct (base position + 8 pixel offset)
- Pickup collision detection works within 32-pixel range
- Inventory count increases when AppleSapling is picked up
- Texture disposal is called on pickup

### Property-Based Testing

Property-based tests will verify:
- **Property 1**: Dual item spawn with correct positioning holds for all AppleTree positions
- **Property 2**: Item key naming convention holds for all tree positions
- **Property 3**: Pickup collision detection works for all player/item position combinations within range
- **Property 4**: Texture disposal occurs for all item pickups
- **Property 5**: Inventory integration works for all pickup scenarios
- **Property 6**: Save/load round trip preserves AppleSapling count for all inventory states

We'll use JUnit with QuickCheck for Java as the property-based testing library. Each property test should run a minimum of 100 iterations to ensure comprehensive coverage.

### Integration Testing

Integration tests will verify:
- AppleTree destruction spawns both items in single-player mode
- Items render correctly on screen
- Player can walk over and pick up both items
- Inventory UI displays AppleSapling count
- Save/load functionality preserves AppleSapling inventory
- Multiplayer synchronization works across clients

### Manual Testing Checklist

**Single-Player Mode:**
1. Start single-player game
2. Find and attack an AppleTree until destroyed
3. Verify two items spawn at tree position
4. Verify items are positioned 8 pixels apart horizontally
5. Verify items render at 32x32 pixels
6. Walk over Apple - verify pickup
7. Walk over AppleSapling - verify pickup and inventory increase
8. Verify console logs show correct positions and pickup messages
9. Save game and verify AppleSapling count persists
10. Load game and verify AppleSapling count restored

**Multiplayer Mode:**
1. Start server and connect client
2. Client attacks AppleTree until destroyed
3. Verify both items spawn on client
4. Verify items appear on other connected clients
5. Client picks up Apple
6. Verify removal on all clients
7. Client picks up AppleSapling
8. Verify removal on all clients
9. Verify inventory synchronization across clients

**Visual Verification:**
1. Compare Apple sprite to expected sprite sheet coordinates (0, 128)
2. Compare AppleSapling sprite to expected sprite sheet coordinates (192, 254)
3. Verify 8-pixel horizontal spacing between items
4. Verify items render at appropriate size (32x32)

### Edge Cases

**Rapid Pickup:**
- Player walks over both items quickly
- Expected: Both items picked up, no crashes
- Handled by: `break` statement in pickup loops (one item per frame)

**Tree Destruction During Multiplayer Lag:**
- Network delay between destruction and item spawn
- Expected: Items eventually appear when message arrives
- Handled by: Pending item spawn queue in MyGdxGame

**Memory Management:**
- Multiple apple trees destroyed
- Expected: All textures properly disposed
- Handled by: dispose() calls in pickup methods

**Inventory Overflow:**
- AppleSapling count exceeds integer max value
- Expected: Count capped at reasonable limit
- Handled by: Inventory validation logic

## Implementation Notes

### Code Consistency

The implementation maintains consistency with existing patterns:
- Item classes match Apple/Banana structure exactly
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

### Future Enhancements

**Potential Improvements:**
1. Configurable item offset distance (currently hardcoded to 8 pixels)
2. Random offset direction (currently always horizontal right)
3. Visual pickup animation or sound effect
4. AppleSapling planting system integration
5. Different growth rates for planted AppleSaplings vs TreeSaplings

These enhancements are out of scope for the current implementation but could be added later.
