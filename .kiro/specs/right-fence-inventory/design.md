# Design Document: RightFence Inventory Integration

## Overview

This design integrates the RightFence item into the existing inventory system by following established patterns used for other inventory items, particularly the recently implemented BackFence, FrontFence, and LeftFence. The implementation involves adding RightFence support to the ItemType enum, Inventory class, network messages, message handlers, InventoryManager, and InventoryRenderer, along with necessary network synchronization for multiplayer support.

This design incorporates lessons learned from previous fence implementations, particularly around task ordering and explicit message handler updates.

## Architecture

The RightFence inventory integration follows the existing layered architecture:

1. **Data Layer**: ItemType enum and Inventory class store RightFence state
2. **Network Layer**: Inventory update and sync messages include RightFence count
3. **Message Handler Layer**: GameMessageHandler and ClientConnection process RightFence data
4. **Management Layer**: InventoryManager handles RightFence collection, removal, and synchronization
5. **Presentation Layer**: InventoryRenderer displays the RightFence icon and count

The integration is additive and follows the exact patterns established by existing items (Apple, Banana, LeftFence, FrontFence, BackFence, etc.).

## Components and Interfaces

### ItemType Enum Extension

Add `RIGHT_FENCE` to the ItemType enum:

```java
public enum ItemType {
    // ... existing items ...
    LEFT_FENCE(false, 0, false),
    FRONT_FENCE(false, 0, false),
    BACK_FENCE(false, 0, false),
    RIGHT_FENCE(false, 0, false);  // New item
    
    // ... existing methods ...
}
```

### Inventory Class Extension

Add RightFence storage and management methods to the Inventory class:

```java
public class Inventory {
    // ... existing fields ...
    private int rightFenceCount;
    
    // Constructor initialization
    public Inventory() {
        // ... existing initializations ...
        this.rightFenceCount = 0;
    }
    
    // RightFence methods (following existing pattern)
    public int getRightFenceCount() { return rightFenceCount; }
    public void setRightFenceCount(int count) { this.rightFenceCount = Math.max(0, count); }
    public void addRightFence(int amount) { this.rightFenceCount += amount; }
    public boolean removeRightFence(int amount) {
        if (rightFenceCount >= amount) {
            rightFenceCount -= amount;
            return true;
        }
        return false;
    }
    
    // Update clear() method
    public void clear() {
        // ... existing clears ...
        this.rightFenceCount = 0;
    }
}
```

### Network Message Extension

Update network messages to include RightFence count:

**InventoryUpdateMessage:**
```java
public class InventoryUpdateMessage extends NetworkMessage {
    // ... existing fields ...
    public int rightFenceCount;
    
    public InventoryUpdateMessage(int appleCount, int bananaCount, ..., int backFenceCount, int rightFenceCount) {
        super(MessageType.INVENTORY_UPDATE);
        // ... existing assignments ...
        this.rightFenceCount = rightFenceCount;
    }
}
```

**InventorySyncMessage:**
```java
public class InventorySyncMessage extends NetworkMessage {
    // ... existing fields ...
    public int rightFenceCount;
    
    public InventorySyncMessage(int appleCount, int bananaCount, ..., int backFenceCount, int rightFenceCount) {
        super(MessageType.INVENTORY_SYNC);
        // ... existing assignments ...
        this.rightFenceCount = rightFenceCount;
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
        msg.backFenceCount,
        msg.rightFenceCount  // New parameter
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
    inventory.getBackFenceCount(),
    inventory.getRightFenceCount()  // New parameter
);
```

### InventoryManager Extension

Add RightFence handling to InventoryManager:

1. **collectItem()**: Add RIGHT_FENCE case to the switch statement
2. **addItemToInventory()**: Add RIGHT_FENCE case to call `inventory.addRightFence(amount)`
3. **sendInventoryUpdate()**: Include rightFenceCount in the InventoryUpdateMessage
4. **syncFromServer()**: Add rightFenceCount parameter and update inventory
5. **getSelectedItemType()**: Add case 13 returning ItemType.RIGHT_FENCE
6. **checkAndAutoDeselect()**: Add case 13 checking rightFenceCount

### InventoryRenderer Extension

Add RightFence rendering to InventoryRenderer:

1. **Texture Field**: Add `private Texture rightFenceIcon;`
2. **PANEL_WIDTH**: Update calculation to accommodate 14 slots instead of 13
3. **loadItemIcons()**: Extract RightFence icon from sprite sheet at (298, 192) with size 22x128, scaled to 32x32
4. **render()**: Add 14th slot rendering for RightFence after BackFence
5. **dispose()**: Dispose rightFenceIcon texture

## Data Models

### RightFence Item Properties

- **Type**: RIGHT_FENCE
- **Restores Health**: false
- **Health Restore Amount**: 0
- **Reduces Hunger**: false
- **Sprite Coordinates**: (298, 192) with size 22x128
- **Inventory Icon Size**: 32x32 (scaled from source)
- **Inventory Slot Index**: 13 (after BackFence at index 12)

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
Slot 12: BackFence
Slot 13: RightFence (NEW)
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*


Based on the acceptance criteria analysis, the following correctness properties must hold:

**Property 1: RightFence collection increases count**
*For any* initial RightFence count, when a player collects a RightFence item, the resulting count should be exactly one greater than the initial count.
**Validates: Requirements 1.1**

**Property 2: Displayed count matches inventory count**
*For any* RightFence count value in the inventory, the rendered UI count should match the inventory's stored count.
**Validates: Requirements 1.2**

**Property 3: Multiplayer collection triggers sync**
*For any* RightFence collection event in multiplayer mode with an active server connection, an inventory update message should be sent to the server.
**Validates: Requirements 2.1**

**Property 4: Server sync updates local count**
*For any* server sync message containing a RightFence count, after synchronization the local inventory's RightFence count should match the server's value.
**Validates: Requirements 2.2**

**Property 5: Multiplayer removal triggers sync**
*For any* RightFence removal event in multiplayer mode with an active server connection, an inventory update message should be sent to the server.
**Validates: Requirements 2.3**

**Property 6: Slot 13 selection returns RIGHT_FENCE**
*For any* inventory state where slot 13 is selected, calling getSelectedItemType() should return ItemType.RIGHT_FENCE.
**Validates: Requirements 3.2**

**Property 7: Zero count triggers auto-deselect**
*For any* sequence of operations where the RightFence count reaches zero while slot 13 is selected, the slot should be automatically deselected.
**Validates: Requirements 3.4**

## Error Handling

The RightFence inventory integration follows existing error handling patterns:

1. **Negative Count Prevention**: The `setRightFenceCount()` method uses `Math.max(0, count)` to prevent negative counts
2. **Insufficient Items**: The `removeRightFence()` method returns false if attempting to remove more items than available
3. **Null Safety**: All methods check for null inventory references before operations
4. **Network Disconnection**: Inventory updates are only sent when `gameClient.isConnected()` returns true

## Testing Strategy

### Unit Tests

Unit tests will verify specific behaviors and edge cases:

1. **Zero Count Handling**: Verify that inventory displays 0 when no RightFence items are collected
2. **Texture Extraction**: Verify that the RightFence icon is extracted from correct sprite sheet coordinates (298, 192)
3. **Icon Rendering Size**: Verify that the RightFence icon is rendered at 32x32 pixels
4. **Slot Position**: Verify that RightFence is positioned at slot index 13
5. **ItemType Configuration**: Verify that RIGHT_FENCE has restoresHealth=false, healthRestore=0, reducesHunger=false
6. **Selection Highlight**: Verify that selecting slot 13 renders the golden highlight border
7. **Deselection**: Verify that deselecting slot 13 removes the highlight

### Property-Based Tests

Property-based tests will verify universal properties across many inputs using a Java property-based testing library (e.g., jqwik or QuickCheck for Java). Each test will run a minimum of 100 iterations.

1. **Property 1 Test**: Generate random initial counts, simulate collection, verify count increases by 1
2. **Property 2 Test**: Generate random counts, verify rendered count matches inventory count
3. **Property 3 Test**: Generate random collection events in multiplayer mode, verify sync messages sent
4. **Property 4 Test**: Generate random server sync messages, verify local count matches server count
5. **Property 5 Test**: Generate random removal events in multiplayer mode, verify sync messages sent
6. **Property 6 Test**: Generate random inventory states with slot 13 selected, verify getSelectedItemType() returns RIGHT_FENCE
7. **Property 7 Test**: Generate random operation sequences leading to zero count while selected, verify auto-deselect

Each property-based test will be tagged with a comment in the format:
`// Feature: right-fence-inventory, Property {number}: {property_text}`

### Integration Tests

Integration tests will verify end-to-end workflows:

1. **Collection to Display**: Collect RightFence items and verify they appear in inventory UI
2. **Multiplayer Synchronization**: Collect RightFence in multiplayer, verify server receives update and other clients sync
3. **Selection and Usage**: Select RightFence slot, verify targeting system activates (if applicable)

## Implementation Notes

### Sprite Sheet Extraction

The RightFence texture is located at coordinates (298, 192) with size 22x128 in the sprite sheet. The extraction process should:

1. Extract the 22x128 texture from the sprite sheet
2. Scale to 32x32 for inventory display
3. Maintain aspect ratio and visual clarity

### Task Ordering (Lessons Learned)

Based on previous fence implementations, the following task order is critical:

1. **Data Model First** (ItemType, Inventory) - Foundation layer
2. **Network Messages Second** (InventoryUpdateMessage, InventorySyncMessage) - Contract definition
3. **Message Handlers Third** (GameMessageHandler, ClientConnection) - Message routing
4. **Business Logic Fourth** (InventoryManager) - Uses all above components
5. **UI Last** (InventoryRenderer) - Presentation layer

This order prevents compilation errors and ensures each component has its dependencies available.

### Network Message Updates

Both `InventoryUpdateMessage` and `InventorySyncMessage` classes need to be updated to include the `rightFenceCount` field. This requires:

1. Adding the field to the message class
2. Updating the constructor to accept the new parameter
3. Updating serialization/deserialization logic
4. Updating ALL call sites that create these messages (critical!)

### Backward Compatibility

Since this adds a new field to network messages, consider:

1. Version compatibility between clients with and without RightFence support
2. Default value handling for missing fields in older message formats
3. Server-side validation of RightFence counts

## Dependencies

- LibGDX graphics library for texture handling
- Existing inventory system components (Inventory, InventoryManager, InventoryRenderer)
- Network messaging system (GameClient, InventoryUpdateMessage, InventorySyncMessage)
- Message handlers (GameMessageHandler, ClientConnection)
- Sprite sheet asset (sprites/assets.png)

## Performance Considerations

The RightFence integration has minimal performance impact:

1. **Memory**: Adds one integer field per inventory instance (~4 bytes)
2. **Rendering**: Adds one additional texture and render call per frame (negligible)
3. **Network**: Adds one integer field to inventory sync messages (~4 bytes per message)

These additions are consistent with existing item patterns and should not cause performance degradation.

## Lessons Learned from Previous Fence Implementations

1. ✅ **Network messages must be updated before InventoryManager** to avoid compilation errors
2. ✅ **Message handlers need explicit updates** - don't assume they'll be caught implicitly
3. ✅ **Panel width changes should be grouped with UI rendering** for logical cohesion
4. ✅ **Test after each major component** to catch integration issues early
5. ✅ **Follow dependency order strictly**: Data → Network → Handlers → Logic → UI
