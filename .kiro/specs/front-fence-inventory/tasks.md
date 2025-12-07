# Implementation Plan: FrontFence Inventory Integration

## Overview
This implementation adds FrontFence item support to the inventory system, following the proven pattern from LeftFence integration with improved task ordering based on lessons learned.

---

- [ ] 1. Add FRONT_FENCE to ItemType enum
  - Add FRONT_FENCE entry to the ItemType enum with properties: restoresHealth=false, healthRestore=0, reducesHunger=false
  - Position it after LEFT_FENCE in the enum definition
  - _Requirements: 4.1_

- [ ] 2. Extend Inventory class with FrontFence support
  - Add private int frontFenceCount field
  - Initialize frontFenceCount to 0 in constructor
  - Implement getFrontFenceCount() method
  - Implement setFrontFenceCount(int count) method with Math.max(0, count) validation
  - Implement addFrontFence(int amount) method
  - Implement removeFrontFence(int amount) method with availability check
  - Update clear() method to reset frontFenceCount to 0
  - _Requirements: 1.1, 4.2_

- [ ]* 2.1 Write property test for FrontFence collection increases count
  - **Property 1: FrontFence collection increases count**
  - **Validates: Requirements 1.1**

- [ ] 3. Update network messages to include FrontFence count
  - Add frontFenceCount field to InventoryUpdateMessage class
  - Update InventoryUpdateMessage constructor to accept frontFenceCount parameter
  - Add frontFenceCount field to InventorySyncMessage class
  - Update InventorySyncMessage constructor to accept frontFenceCount parameter
  - Update message serialization/deserialization to handle frontFenceCount field
  - _Requirements: 2.1, 2.2, 2.3_

- [ ] 4. Update message handlers to process FrontFence data
  - Update GameMessageHandler.handleInventorySyncMessage() to extract frontFenceCount from InventorySyncMessage
  - Update ClientConnection to pass frontFenceCount parameter to inventoryManager.syncFromServer()
  - Update all call sites creating InventoryUpdateMessage to include frontFenceCount
  - Verify message routing includes the new field throughout the network stack
  - _Requirements: 2.2_

- [ ] 5. Update InventoryManager to handle FrontFence
  - Add FRONT_FENCE case to collectItem() switch statement
  - Add FRONT_FENCE case to addItemToInventory() switch statement calling inventory.addFrontFence(amount)
  - Update sendInventoryUpdate() to include frontFenceCount in InventoryUpdateMessage
  - Update syncFromServer() method signature to accept frontFenceCount parameter
  - Update syncFromServer() to call inventory.setFrontFenceCount(frontFenceCount)
  - Add case 11 to getSelectedItemType() returning ItemType.FRONT_FENCE
  - Add case 11 to checkAndAutoDeselect() checking inventory.getFrontFenceCount()
  - _Requirements: 1.1, 2.1, 2.2, 2.3, 3.2, 3.4_

- [ ]* 5.1 Write property test for multiplayer collection triggers sync
  - **Property 3: Multiplayer collection triggers sync**
  - **Validates: Requirements 2.1**

- [ ]* 5.2 Write property test for server sync updates local count
  - **Property 4: Server sync updates local count**
  - **Validates: Requirements 2.2**

- [ ]* 5.3 Write property test for multiplayer removal triggers sync
  - **Property 5: Multiplayer removal triggers sync**
  - **Validates: Requirements 2.3**

- [ ]* 5.4 Write property test for slot 11 selection returns FRONT_FENCE
  - **Property 6: Slot 11 selection returns FRONT_FENCE**
  - **Validates: Requirements 3.2**

- [ ]* 5.5 Write property test for zero count triggers auto-deselect
  - **Property 7: Zero count triggers auto-deselect**
  - **Validates: Requirements 3.4**

- [ ] 6. Extend InventoryRenderer to display FrontFence
  - Add private Texture frontFenceIcon field
  - Update PANEL_WIDTH calculation to accommodate 12 slots instead of 11
  - Update loadItemIcons() to extract FrontFence icon from sprite sheet at coordinates (0, 320) with size 64x64
  - Scale or crop the extracted texture to 32x32 for inventory display
  - Update render() method to add 12th slot for FrontFence at position after LeftFence (slot 11)
  - Call renderSlot() for FrontFence with frontFenceIcon, inventory.getFrontFenceCount(), and selectedSlot == 11
  - Update dispose() method to dispose frontFenceIcon texture
  - _Requirements: 1.2, 1.3, 1.4, 4.3_

- [ ]* 6.1 Write property test for displayed count matches inventory count
  - **Property 2: Displayed count matches inventory count**
  - **Validates: Requirements 1.2**

- [ ]* 6.2 Write unit tests for FrontFence rendering
  - Test that FrontFence icon is extracted from correct coordinates (0, 320)
  - Test that FrontFence icon is rendered at 32x32 pixels
  - Test that FrontFence is positioned at slot index 11
  - Test that zero count displays 0 in the UI
  - Test that selection highlights slot 11 with golden border
  - Test that deselection removes the highlight
  - _Requirements: 1.3, 1.4, 1.5, 3.1, 3.3, 4.3_

- [ ] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

---

## Key Improvements from LeftFence Implementation

### ✅ **Corrected Task Order:**
1. **Network messages (Task 3) now come BEFORE InventoryManager (Task 5)** - This prevents compilation errors since InventoryManager depends on the updated message signatures
2. **New Task 4 added** - Explicitly handles message handler updates (GameMessageHandler, ClientConnection) which was implicit before
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
- FrontFence texture: (0, 320) with size 64x64 from sprite sheet
- Inventory icon: Scale to 32x32 for display
- Slot position: Index 11 (after LeftFence at index 10)

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
Slot 11: FrontFence (NEW)
```

**Critical Success Factors:**
1. Complete Task 3 (network messages) before Task 5 (InventoryManager)
2. Update ALL message handler call sites in Task 4
3. Test compilation after each task to catch dependency issues early
4. Run checkpoint tests to verify no regressions
