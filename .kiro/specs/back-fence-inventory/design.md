# Design Document: BackFence Inventory Integration

## Overview

This design integrates the BackFence item into the existing inventory system by following established patterns used for other inventory items, particularly the recently implemented FrontFence and LeftFence. The implementation involves adding BackFence support to the ItemType enum, Inventory class, network messages, message handlers, InventoryManager, and InventoryRenderer, along with necessary network synchronization for multiplayer support.

This design incorporates lessons learned from both the LeftFence and FrontFence implementations, particularly around task ordering and explicit message handler updates.

## Architecture

The BackFence inventory integration follows the existing layered architecture:

1. **Data Layer**: ItemType enum and Inventory class store BackFence state
2. **Network Layer**: Inventory update and sync messages include BackFence count
3. **Message Handler Layer**: GameMessageHandler and ClientConnection process BackFence data
4. **Management Layer**: InventoryManager handles BackFence collection, removal, and synchronization
5. **Presentation Layer**: InventoryRenderer displays the BackFence icon and count

The integration is additive and follows the exact patterns established by existing items (Apple, Banana, LeftFence, FrontFence, etc.).

## Components and Interfaces

### ItemType Enum Extension

Add `BACK_FENCE` to the ItemType enum:

```java
public enum ItemType {
    // ... existing items ...
    LEFT_FENCE(false, 0, false),
    FRONT_FENCE(false, 0, false),
    BACK_FENCE(false, 0, false);  // New item
    
    // ... existing methods ...
}
```

### Inventory Class Extension

Add BackFence storage and management methods to the Inventory class:

```java
public class Inventory {
    // ... existing fields ...
    private int backFenceCount;
    
    // Constructor initialization
    public Inventory() {
        // ... existing initializations ...
        this.backFenceCount = 0;
    }
    
    // BackFence methods (following existing pattern)
    public int getBackFenceCount() { return backFenceCount; }
    public void setBackFenceCount(int count) { this.backFenceCount = Math.max(0, count); }
    public void addBackFence(int amount) { this.backFenceCount += amount; }
    public boolean removeBackFence(int amount) {
        if (backFenceCount >= amount) {
            backFenceCount -= amount;
            return true;
        }
        return false;
    }
    
    // Update clear() method
    public void clear() {
        // ... existing clears ...
        this.backFenceCount = 0;
    }
}
```

### Network Message Extension

Update network messages to include BackFence count:

**InventoryUpdateMessage:**
```java
public class InventoryUpdateMessage extends NetworkMessage {
    // ... existing fields ...
    public int backFenceCount;
    
    public InventoryUpdateMessage(int appleCount, int bananaCount, ..., int frontFenceCount, int backFenceCount) {
        super(MessageType.INVENTORY_UPDATE);
        // ... existing assignments ...
        this.backFenceCount = backFenceCount;
    }
}
```

**InventorySyncMessage:**
```java
public class InventorySyncMessage extends NetworkMessage {
    // ... existing fields ...
    public int backFenceCount;
    
    public InventorySyncMessage(int appleCount, int bananaCount, ..., int frontFenceCount, int backFenceCount) {
        super(MessageType.INVENTORY_SYNC);
        // ... existing assignments ...
        this.backFenceCount = backFenceCount;
    }
}
```

### Message Handler Updates

**GameMessageHandler:**
```java
private void handleInventorySyncMessage(InventorySyncMessage msg) {
    inventoryManager.syncFromServer(
        msg.appleCount,
        msg.bananaCount,
        // ... existing parameters ...
        msg.frontFenceCount,
        msg.backFenceCount  // New parameter
    );
}
```

**ClientConnection:**
```java
// Update all call sites creating InventoryUpdateMessage
new InventoryUpdateMessage(
    inventory.getAppleCount(),
    inventory.getBananaCount(),
    // ... existing parameters ...
    inventory.getFrontFenceCount(),
    inventory.getBackFenceCount()  // New parameter
);
```

### InventoryManager Extension

Add BackFence handling to InventoryManager:

1. **collectItem()**: Add BACK_FENCE case to the switch statement
2. **addItemToInventory()**: Add BACK_FENCE case to call `inventory.addBackFence(amount)`
3. **sendInventoryUpdate()**: Include backFenceCount in the InventoryUpdateMessage
4. **syncFromServer()**: Add backFenceCount parameter and update inventory
5. **getSelectedItemType()**: Add case 12 returning ItemType.BACK_FENCE
6. **checkAndAutoDeselect()**: Add case 12 checking backFenceCount

### InventoryRenderer Extension

Add BackFence rendering to InventoryRenderer:

1. **Texture Field**: Add `private Texture backFenceIcon;`
2. **PANEL_WIDTH**: Update calculation to accommodate 13 slots
3. **loadItemIcons()**: Extract BackFence icon from sprite sheet at (64, 320) with size 64x64, scaled to 32x32
4. **render()**: Add 13th slot rendering for BackFence after FrontFence
5. **dispose()**: Dispose backFenceIcon texture

## Data Models

### BackFence Item Properties

- **Type**: BACK_FENCE
- **Restores Health**: false
- **Health Restore Amount**: 0
- **Reduces Hunger**: false
- **Sprite Coordinates**: (64, 320) with size 64x64
- **Inventory Icon Size**: 32x32 (scaled from source)
- **Inventory Slot Index**: 12 (after FrontFence at index 11)

### Inventory Slot Layout

```
Slot 0:  Apple
Slot 1:  Banana
Slot 2:  BambooSapling
Slot 3:  BambooStack
Slot 4:  TreeSapling
Slot 5:  WoodStack
Slot 6:  Pebble
Slot 7:  PalmFiber
Slot 8:  AppleSapling
Slot 9:  BananaSapling
Slot 10: LeftFence
Slot 11: FrontFence
Slot 12: BackFence (NEW)
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


Based on the acceptance criteria analysis, the following correctness properties must hold:

**Property 1: BackFence collection increases count**
*For any* initial BackFence count, when a player collects a BackFence item, the resulting count should be exactly one greater than the initial count.
**Validates: Requirements 1.1**

**Property 2: Displayed count matches inventory count**
*For any* BackFence count value in the inventory, the rendered UI count should match the inventory's stored count.
**Validates: Requirements 1.2**

**Property 3: Multiplayer collection triggers sync**
*For any* BackFence collection event in multiplayer mode with an active server connection, an inventory update message should be sent to the server.
**Validates: Requirements 2.1**

**Property 4: Server sync updates local count**
*For any* server sync message containing a BackFence count, after synchronization the local inventory's BackFence count should match the server's value.
**Validates: Requirements 2.2**

**Property 5: Multiplayer removal triggers sync**
*For any* BackFence removal event in multiplayer mode with an active server connection, an inventory update message should be sent to the server.
**Validates: Requirements 2.3**

**Property 6: Slot 12 selection returns BACK_FENCE**
*For any* inventory state where slot 12 is selected, calling getSelectedItemType() should return ItemType.BACK_FENCE.
**Validates: Requirements 3.2**

**Property 7: Zero count triggers auto-deselect**
*For any* sequence of operations where the BackFence count reaches zero while slot 12 is selected, the slot should be automatically deselected.
**Validates: Requirements 3.4**

## Error Handling

The BackFence inventory integration follows existing error handling patterns:

1. **Negative Count Prevention**: The `setBackFenceCount()` method uses `Math.max(0, count)` to prevent negative counts
2. **Insufficient Items**: The `removeBackFence()` method returns false if attempting to remove more items than available
3. **Null Safety**: All methods check for null inventory references before operations
4. **Network Disconnection**: Inventory updates are only sent when `gameClient.isConnected()` returns true

## Testing Strategy

### Unit Tests

Unit tests will verify specific behaviors and edge cases:

1. **Zero Count Handling**: Verify that inventory displays 0 when no BackFence items are collected
2. **Texture Extraction**: Verify that the BackFence icon is extracted from correct sprite sheet coordinates (64, 320)
3. **Icon Rendering Size**: Verify that the BackFence icon is rendered at 32x32 pixels
4. **Slot Position**: Verify that BackFence is positioned at slot index 12
5. **ItemType Configuration**: Verify that BACK_FENCE has restoresHealth=false, healthRestore=0, reducesHunger=false
6. **Selection Highlight**: Verify that selecting slot 12 renders the golden highlight border
7. **Deselection**: Verify that deselecting slot 12 removes the highlight

### Property-Based Tests

Property-based tests will verify universal properties across many inputs using a Java property-based testing library (e.g., jqwik or QuickCheck for Java). Each test will run a minimum of 100 iterations.

1. **Property 1 Test**: Generate random initial counts, simulate collection, verify count increases by 1
2. **Property 2 Test**: Generate random counts, verify rendered count matches inventory count
3. **Property 3 Test**: Generate random collection events in multiplayer mode, verify sync messages sent
4. **Property 4 Test**: Generate random server sync messages, verify local count matches server count
5. **Property 5 Test**: Generate random removal events in multiplayer mode, verify sync messages sent
6. **Property 6 Test**: Generate random inventory states with slot 12 selected, verify getSelectedItemType() returns BACK_FENCE
7. **Property 7 Test**: Generate random operation sequences leading to zero count while selected, verify auto-deselect

Each property-based test will be tagged with a comment in the format:
`// Feature: back-fence-inventory, Property {number}: {property_text}`

### Integration Tests

Integration tests will verify end-to-end workflows:

1. **Collection to Display**: Collect BackFence items and verify they appear in inventory UI
2. **Multiplayer Synchronization**: Collect BackFence in multiplayer, verify server receives update and other clients sync
3. **Selection and Usage**: Select BackFence slot, verify targeting system activates (if applicable)

## Implementation Notes

### Sprite Sheet Extraction

The BackFence texture is located at coordinates (64, 320) with size 64x64 in the sprite sheet. The extraction process should:

1. Extract the full 64x64 texture from the sprite sheet
2. Scale to 32x32 for inventory display
3. Maintain aspect ratio and visual clarity

### Task Ordering (Lessons Learned)

Based on the LeftFence and FrontFence implementations, the following task order is critical:

1. **Data Model First** (ItemType, Inventory) - Foundation layer
2. **Network Messages Second** (InventoryUpdateMessage, InventorySyncMessage) - Contract definition
3. **Message Handlers Third** (GameMessageHandler, ClientConnection) - Message routing
4. **Business Logic Fourth** (InventoryManager) - Uses all above components
5. **UI Last** (InventoryRenderer) - Presentation layer

This order prevents compilation errors and ensures each component has its dependencies available.

### Network Message Updates

Both `InventoryUpdateMessage` and `InventorySyncMessage` classes need to be updated to include the `backFenceCount` field. This requires:

1. Adding the field to the message class
2. Updating the constructor to accept the new parameter
3. Updating serialization/deserialization logic
4. Updating ALL call sites that create these messages (critical!)

### Backward Compatibility

Since this adds a new field to network messages, consider:

1. Version compatibility between clients with and without BackFence support
2. Default value handling for missing fields in older message formats
3. Server-side validation of BackFence counts

## Dependencies

- LibGDX graphics library for texture handling
- Existing inventory system components (Inventory, InventoryManager, InventoryRenderer)
- Network messaging system (GameClient, InventoryUpdateMessage, InventorySyncMessage)
- Message handlers (GameMessageHandler, ClientConnection)
- Sprite sheet asset (sprites/assets.png)

## Performance Considerations

The BackFence integration has minimal performance impact:

1. **Memory**: Adds one integer field per inventory instance (~4 bytes)
2. **Rendering**: Adds one additional texture and render call per frame (negligible)
3. **Network**: Adds one integer field to inventory sync messages (~4 bytes per message)

These additions are consistent with existing item patterns and should not cause performance degradation.

## Lessons Learned from LeftFence and FrontFence

1. ✅ **Network messages must be updated before InventoryManager** to avoid compilation errors
2. ✅ **Message handlers need explicit updates** - don't assume they'll be caught implicitly
3. ✅ **Panel width changes should be grouped with UI rendering** for logical cohesion
4. ✅ **Test after each major component** to catch integration issues early
5. ✅ **Follow dependency order strictly**: Data → Network → Handlers → Logic → UI
