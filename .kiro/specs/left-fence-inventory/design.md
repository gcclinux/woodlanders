# Design Document: LeftFence Inventory Integration

## Overview

This design integrates the LeftFence item into the existing inventory system by following established patterns used for other inventory items. The implementation involves adding LeftFence support to the ItemType enum, Inventory class, InventoryManager, and InventoryRenderer, along with necessary network synchronization for multiplayer support.

## Architecture

The LeftFence inventory integration follows the existing layered architecture:

1. **Data Layer**: ItemType enum and Inventory class store LeftFence state
2. **Management Layer**: InventoryManager handles LeftFence collection, removal, and synchronization
3. **Presentation Layer**: InventoryRenderer displays the LeftFence icon and count
4. **Network Layer**: Inventory update and sync messages include LeftFence count

The integration is additive and follows the exact patterns established by existing items (Apple, Banana, BananaSapling, etc.).

## Components and Interfaces

### ItemType Enum Extension

Add `LEFT_FENCE` to the ItemType enum:

```java
public enum ItemType {
    // ... existing items ...
    BANANA_SAPLING(false, 0, false),
    LEFT_FENCE(false, 0, false);  // New item
    
    // ... existing methods ...
}
```

### Inventory Class Extension

Add LeftFence storage and management methods to the Inventory class:

```java
public class Inventory {
    // ... existing fields ...
    private int leftFenceCount;
    
    // Constructor initialization
    public Inventory() {
        // ... existing initializations ...
        this.leftFenceCount = 0;
    }
    
    // LeftFence methods (following existing pattern)
    public int getLeftFenceCount() { return leftFenceCount; }
    public void setLeftFenceCount(int count) { this.leftFenceCount = Math.max(0, count); }
    public void addLeftFence(int amount) { this.leftFenceCount += amount; }
    public boolean removeLeftFence(int amount) {
        if (leftFenceCount >= amount) {
            leftFenceCount -= amount;
            return true;
        }
        return false;
    }
    
    // Update clear() method
    public void clear() {
        // ... existing clears ...
        this.leftFenceCount = 0;
    }
}
```

### InventoryManager Extension

Add LeftFence handling to InventoryManager:

1. **collectItem()**: Add LEFT_FENCE case to the switch statement
2. **addItemToInventory()**: Add LEFT_FENCE case to call `inventory.addLeftFence(amount)`
3. **sendInventoryUpdate()**: Include leftFenceCount in the InventoryUpdateMessage
4. **syncFromServer()**: Add leftFenceCount parameter and update inventory
5. **getSelectedItemType()**: Add case 10 returning ItemType.LEFT_FENCE
6. **checkAndAutoDeselect()**: Add case 10 checking leftFenceCount

### InventoryRenderer Extension

Add LeftFence rendering to InventoryRenderer:

1. **Texture Field**: Add `private Texture leftFenceIcon;`
2. **loadItemIcons()**: Extract LeftFence icon from sprite sheet at (256, 192) with size 32x128, scaled to 32x32
3. **render()**: Add 11th slot rendering for LeftFence after BananaSapling
4. **dispose()**: Dispose leftFenceIcon texture

### Network Message Extension

Update network messages to include LeftFence count:

1. **InventoryUpdateMessage**: Add leftFenceCount field and parameter
2. **InventorySyncMessage**: Add leftFenceCount field and parameter

## Data Models

### LeftFence Item Properties

- **Type**: LEFT_FENCE
- **Restores Health**: false
- **Health Restore Amount**: 0
- **Reduces Hunger**: false
- **Sprite Coordinates**: (256, 192) with size 32x128
- **Inventory Icon Size**: 32x32 (scaled from source)
- **Inventory Slot Index**: 10 (after BananaSapling at index 9)

### Inventory Slot Layout

```
Slot 0: Apple
Slot 1: Banana
Slot 2: BambooSapling
Slot 3: BambooStack
Slot 4: TreeSapling
Slot 5: WoodStack
Slot 6: Pebble
Slot 7: PalmFiber
Slot 8: AppleSapling
Slot 9: BananaSapling
Slot 10: LeftFence (NEW)
```

## 
Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

Based on the acceptance criteria analysis, the following correctness properties must hold:

**Property 1: LeftFence collection increases count**
*For any* initial LeftFence count, when a player collects a LeftFence item, the resulting count should be exactly one greater than the initial count.
**Validates: Requirements 1.1**

**Property 2: Displayed count matches inventory count**
*For any* LeftFence count value in the inventory, the rendered UI count should match the inventory's stored count.
**Validates: Requirements 1.2**

**Property 3: Multiplayer collection triggers sync**
*For any* LeftFence collection event in multiplayer mode with an active server connection, an inventory update message should be sent to the server.
**Validates: Requirements 2.1**

**Property 4: Server sync updates local count**
*For any* server sync message containing a LeftFence count, after synchronization the local inventory's LeftFence count should match the server's value.
**Validates: Requirements 2.2**

**Property 5: Multiplayer removal triggers sync**
*For any* LeftFence removal event in multiplayer mode with an active server connection, an inventory update message should be sent to the server.
**Validates: Requirements 2.3**

**Property 6: Slot 10 selection returns LEFT_FENCE**
*For any* inventory state where slot 10 is selected, calling getSelectedItemType() should return ItemType.LEFT_FENCE.
**Validates: Requirements 3.2**

**Property 7: Zero count triggers auto-deselect**
*For any* sequence of operations where the LeftFence count reaches zero while slot 10 is selected, the slot should be automatically deselected.
**Validates: Requirements 3.4**

## Error Handling

The LeftFence inventory integration follows existing error handling patterns:

1. **Negative Count Prevention**: The `setLeftFenceCount()` method uses `Math.max(0, count)` to prevent negative counts
2. **Insufficient Items**: The `removeLeftFence()` method returns false if attempting to remove more items than available
3. **Null Safety**: All methods check for null inventory references before operations
4. **Network Disconnection**: Inventory updates are only sent when `gameClient.isConnected()` returns true

## Testing Strategy

### Unit Tests

Unit tests will verify specific behaviors and edge cases:

1. **Zero Count Handling**: Verify that inventory displays 0 when no LeftFence items are collected
2. **Texture Extraction**: Verify that the LeftFence icon is extracted from correct sprite sheet coordinates (256, 192)
3. **Icon Rendering Size**: Verify that the LeftFence icon is rendered at 32x32 pixels
4. **Slot Position**: Verify that LeftFence is positioned at slot index 10
5. **ItemType Configuration**: Verify that LEFT_FENCE has restoresHealth=false, healthRestore=0, reducesHunger=false
6. **Selection Highlight**: Verify that selecting slot 10 renders the golden highlight border
7. **Deselection**: Verify that deselecting slot 10 removes the highlight

### Property-Based Tests

Property-based tests will verify universal properties across many inputs using a Java property-based testing library (e.g., jqwik or QuickCheck for Java). Each test will run a minimum of 100 iterations.

1. **Property 1 Test**: Generate random initial counts, simulate collection, verify count increases by 1
2. **Property 2 Test**: Generate random counts, verify rendered count matches inventory count
3. **Property 3 Test**: Generate random collection events in multiplayer mode, verify sync messages sent
4. **Property 4 Test**: Generate random server sync messages, verify local count matches server count
5. **Property 5 Test**: Generate random removal events in multiplayer mode, verify sync messages sent
6. **Property 6 Test**: Generate random inventory states with slot 10 selected, verify getSelectedItemType() returns LEFT_FENCE
7. **Property 7 Test**: Generate random operation sequences leading to zero count while selected, verify auto-deselect

Each property-based test will be tagged with a comment in the format:
`// Feature: left-fence-inventory, Property {number}: {property_text}`

### Integration Tests

Integration tests will verify end-to-end workflows:

1. **Collection to Display**: Collect LeftFence items and verify they appear in inventory UI
2. **Multiplayer Synchronization**: Collect LeftFence in multiplayer, verify server receives update and other clients sync
3. **Selection and Usage**: Select LeftFence slot, verify targeting system activates (if applicable)

## Implementation Notes

### Sprite Sheet Extraction

The LeftFence texture is located at coordinates (256, 192) with size 32x128 in the sprite sheet. However, the inventory requires a 32x32 icon. The extraction process should:

1. Extract the full 32x128 texture from the sprite sheet
2. Scale or crop to 32x32 for inventory display
3. Maintain aspect ratio and visual clarity

### Network Message Updates

Both `InventoryUpdateMessage` and `InventorySyncMessage` classes need to be updated to include the `leftFenceCount` field. This requires:

1. Adding the field to the message class
2. Updating the constructor to accept the new parameter
3. Updating serialization/deserialization logic
4. Updating all call sites that create these messages

### Backward Compatibility

Since this adds a new field to network messages, consider:

1. Version compatibility between clients with and without LeftFence support
2. Default value handling for missing fields in older message formats
3. Server-side validation of LeftFence counts

## Dependencies

- LibGDX graphics library for texture handling
- Existing inventory system components (Inventory, InventoryManager, InventoryRenderer)
- Network messaging system (GameClient, InventoryUpdateMessage, InventorySyncMessage)
- Sprite sheet asset (sprites/assets.png)

## Performance Considerations

The LeftFence integration has minimal performance impact:

1. **Memory**: Adds one integer field per inventory instance (~4 bytes)
2. **Rendering**: Adds one additional texture and render call per frame (negligible)
3. **Network**: Adds one integer field to inventory sync messages (~4 bytes per message)

These additions are consistent with existing item patterns and should not cause performance degradation.
