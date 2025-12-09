# Implementation Plan: RightFence Inventory Integration

## Overview
This implementation adds RightFence item support to the inventory system, following the proven pattern from LeftFence, FrontFence, and BackFence integration with improved task ordering based on lessons learned.

---

- [x] 1. Add RIGHT_FENCE to ItemType enum

  - Add RIGHT_FENCE entry to the ItemType enum with properties: restoresHealth=false, healthRestore=0, reducesHunger=false
  - Position it after BACK_FENCE in the enum definition
  - _Requirements: 4.1_

- [x] 2. Extend Inventory class with RightFence support

  - Add private int rightFenceCount field
  - Initialize rightFenceCount to 0 in constructor
  - Implement getRightFenceCount() method
  - Implement setRightFenceCount(int count) method with Math.max(0, count) validation
  - Implement addRightFence(int amount) method
  - Implement removeRightFence(int amount) method with availability check
  - Update clear() method to reset rightFenceCount to 0
  - _Requirements: 1.1, 4.2_

- [x] 2.1 Write property test for RightFence collection increases count

  - **Property 1: RightFence collection increases count**
  - **Validates: Requirements 1.1**

- [x] 3. Update network messages to include RightFence count

  - Add rightFenceCount field to InventoryUpdateMessage class
  - Update InventoryUpdateMessage constructor to accept rightFenceCount parameter
  - Add rightFenceCount field to InventorySyncMessage class
  - Update InventorySyncMessage constructor to accept rightFenceCount parameter
  - Update message serialization/deserialization to handle rightFenceCount field
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 4. Update message handlers to process RightFence data

  - Update GameMessageHandler.handleInventorySyncMessage() to extract rightFenceCount from InventorySyncMessage
  - Update ClientConnection to pass rightFenceCount parameter to inventoryManager.syncFromServer()
  - Update all call sites creating InventoryUpdateMessage to include rightFenceCount
  - Verify message routing includes the new field throughout the network stack
  - _Requirements: 2.2_

- [x] 5. Update InventoryManager to handle RightFence

  - Add RIGHT_FENCE case to collectItem() switch statement
  - Add RIGHT_FENCE case to addItemToInventory() switch statement calling inventory.addRightFence(amount)
  - Update sendInventoryUpdate() to include rightFenceCount in InventoryUpdateMessage
  - Update syncFromServer() method signature to accept rightFenceCount parameter
  - Update syncFromServer() to call inventory.setRightFenceCount(rightFenceCount)
  - Add case 13 to getSelectedItemType() returning ItemType.RIGHT_FENCE
  - Add case 13 to checkAndAutoDeselect() checking inventory.getRightFenceCount()
  - _Requirements: 1.1, 2.1, 2.2, 2.3, 3.2, 3.4_

- [x] 5.1 Write property test for multiplayer collection triggers sync


  - **Property 3: Multiplayer collection triggers sync**
  - **Validates: Requirements 2.1**

- [x] 5.2 Write property test for server sync updates local count


  - **Property 4: Server sync updates local count**
  - **Validates: Requirements 2.2**

- [x] 5.3 Write property test for multiplayer removal triggers sync


  - **Property 5: Multiplayer removal triggers sync**
  - **Validates: Requirements 2.3**

- [x] 5.4 Write property test for slot 13 selection returns RIGHT_FENCE


  - **Property 6: Slot 13 selection returns RIGHT_FENCE**
  - **Validates: Requirements 3.2**

- [x] 5.5 Write property test for zero count triggers auto-deselect


  - **Property 7: Zero count triggers auto-deselect**
  - **Validates: Requirements 3.4**

- [x] 6. Extend InventoryRenderer to display RightFence

  - Add private Texture rightFenceIcon field
  - Update PANEL_WIDTH calculation to accommodate 14 slots instead of 13
  - Update loadItemIcons() to extract RightFence icon from sprite sheet at coordinates (298, 192) with size 22x128
  - Scale or crop the extracted texture to 32x32 for inventory display
  - Update render() method to add 14th slot for RightFence at position after BackFence (slot 13)
  - Call renderSlot() for RightFence with rightFenceIcon, inventory.getRightFenceCount(), and selectedSlot == 13
  - Update dispose() method to dispose rightFenceIcon texture
  - _Requirements: 1.2, 1.3, 1.4, 4.3_

- [x] 6.1 Write property test for displayed count matches inventory count


  - **Property 2: Displayed count matches inventory count**
  - **Validates: Requirements 1.2**

- [x] 6.2 Write unit tests for RightFence rendering

  - Test that RightFence icon is extracted from correct coordinates (298, 192)
  - Test that RightFence icon is rendered at 32x32 pixels
  - Test that RightFence is positioned at slot index 13
  - Test that zero count displays 0 in the UI
  - Test that selection highlights slot 13 with golden border
  - Test that deselection removes the highlight
  - _Requirements: 1.3, 1.4, 1.5, 3.1, 3.3, 4.3_

- [x] 7. Checkpoint - Ensure all tests pass

  - Ensure all tests pass, ask the user if questions arise.

- [x] 8. Add RightFence to Free World mode





  - Update FreeWorldManager.grantFreeWorldItems() to include inventory.setRightFenceCount(250)
  - Position RightFence after BackFence in the item grant list
  - Verify that Free World mode grants 250 RightFence items to players
  - _Requirements: 1.1_

---

## Key Improvements from Previous Implementations

### ✅ **Corrected Task Order:**
1. **Network messages (Task 3) now come BEFORE InventoryManager (Task 5)** - This prevents compilation errors since InventoryManager depends on the updated message signatures
2. **Task 4 explicitly handles message handler updates** (GameMessageHandler, ClientConnection)
3. **Panel width merged into Task 6** - UI changes are now grouped together logically

### 📋 **Dependency Flow:**
```
Data Model (Tasks 1-2)
    ↓
Network Contract (Task 3)
    ↓
Message Handlers (Task 4)
    ↓
Business Logic (Task 5)
    ↓
UI Presentation (Task 6)
    ↓
Verification (Task 7)
```

### 🎯 **Implementation Notes:**

**Sprite Coordinates:**
- RightFence texture: (298, 192) with size 22x128 from sprite sheet
- Inventory icon: Scale to 32x32 for display
- Slot position: Index 13 (after BackFence at index 12)

**Slot Layout After Implementation:**
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

**Critical Success Factors:**
1. Complete Task 3 (network messages) before Task 5 (InventoryManager)
2. Update ALL message handler call sites in Task 4
3. Test compilation after each task to catch dependency issues early
4. Run checkpoint tests to verify no regressions
