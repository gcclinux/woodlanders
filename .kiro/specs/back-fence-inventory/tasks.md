# Implementation Plan: BackFence Inventory Integration

## Overview
This implementation adds BackFence item support to the inventory system, following the proven pattern from LeftFence and FrontFence integration with improved task ordering based on lessons learned.

---

- [-] 1. Add BACK_FENCE to ItemType enum



  - Add BACK_FENCE entry to the ItemType enum with properties: restoresHealth=false, healthRestore=0, reducesHunger=false
  - Position it after FRONT_FENCE in the enum definition
  - _Requirements: 4.1_

- [ ] 2. Extend Inventory class with BackFence support
  - Add private int backFenceCount field
  - Initialize backFenceCount to 0 in constructor
  - Implement getBackFenceCount() method
  - Implement setBackFenceCount(int count) method with Math.max(0, count) validation
  - Implement addBackFence(int amount) method
  - Implement removeBackFence(int amount) method with availability check
  - Update clear() method to reset backFenceCount to 0
  - _Requirements: 1.1, 4.2_

- [ ] 2.1 Write property test for BackFence collection increases count
  - **Property 1: BackFence collection increases count**
  - **Validates: Requirements 1.1**

- [ ] 3. Update network messages to include BackFence count
  - Add backFenceCount field to InventoryUpdateMessage class
  - Update InventoryUpdateMessage constructor to accept backFenceCount parameter
  - Add backFenceCount field to InventorySyncMessage class
  - Update InventorySyncMessage constructor to accept backFenceCount parameter
  - Update message serialization/deserialization to handle backFenceCount field
  - _Requirements: 2.1, 2.2, 2.3_

- [ ] 4. Update message handlers to process BackFence data
  - Update GameMessageHandler.handleInventorySyncMessage() to extract backFenceCount from InventorySyncMessage
  - Update ClientConnection to pass backFenceCount parameter to inventoryManager.syncFromServer()
  - Update all call sites creating InventoryUpdateMessage to include backFenceCount
  - Verify message routing includes the new field throughout the network stack
  - _Requirements: 2.2_

- [ ] 5. Update InventoryManager to handle BackFence
  - Add BACK_FENCE case to collectItem() switch statement
  - Add BACK_FENCE case to addItemToInventory() switch statement calling inventory.addBackFence(amount)
  - Update sendInventoryUpdate() to include backFenceCount in InventoryUpdateMessage
  - Update syncFromServer() method signature to accept backFenceCount parameter
  - Update syncFromServer() to call inventory.setBackFenceCount(backFenceCount)
  - Add case 12 to getSelectedItemType() returning ItemType.BACK_FENCE
  - Add case 12 to checkAndAutoDeselect() checking inventory.getBackFenceCount()
  - _Requirements: 1.1, 2.1, 2.2, 2.3, 3.2, 3.4_

- [ ] 5.1 Write property test for multiplayer collection triggers sync
  - **Property 3: Multiplayer collection triggers sync**
  - **Validates: Requirements 2.1**

- [ ] 5.2 Write property test for server sync updates local count
  - **Property 4: Server sync updates local count**
  - **Validates: Requirements 2.2**

- [ ] 5.3 Write property test for multiplayer removal triggers sync
  - **Property 5: Multiplayer removal triggers sync**
  - **Validates: Requirements 2.3**

- [ ] 5.4 Write property test for slot 12 selection returns BACK_FENCE
  - **Property 6: Slot 12 selection returns BACK_FENCE**
  - **Validates: Requirements 3.2**

- [ ] 5.5 Write property test for zero count triggers auto-deselect
  - **Property 7: Zero count triggers auto-deselect**
  - **Validates: Requirements 3.4**

- [ ] 6. Extend InventoryRenderer to display BackFence
  - Add private Texture backFenceIcon field
  - Update PANEL_WIDTH calculation to accommodate 13 slots instead of 12
  - Update loadItemIcons() to extract BackFence icon from sprite sheet at coordinates (64, 320) with size 64x64
  - Scale or crop the extracted texture to 32x32 for inventory display
  - Update render() method to add 13th slot for BackFence at position after FrontFence (slot 12)
  - Call renderSlot() for BackFence with backFenceIcon, inventory.getBackFenceCount(), and selectedSlot == 12
  - Update dispose() method to dispose backFenceIcon texture
  - _Requirements: 1.2, 1.3, 1.4, 4.3_

- [ ] 6.1 Write property test for displayed count matches inventory count
  - **Property 2: Displayed count matches inventory count**
  - **Validates: Requirements 1.2**

- [ ] 6.2 Write unit tests for BackFence rendering
  - Test that BackFence icon is extracted from correct coordinates (64, 320)
  - Test that BackFence icon is rendered at 32x32 pixels
  - Test that BackFence is positioned at slot index 12
  - Test that zero count displays 0 in the UI
  - Test that selection highlights slot 12 with golden border
  - Test that deselection removes the highlight
  - _Requirements: 1.3, 1.4, 1.5, 3.1, 3.3, 4.3_

- [ ] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.

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
- BackFence texture: (64, 320) with size 64x64 from sprite sheet
- Inventory icon: Scale to 32x32 for display
- Slot position: Index 12 (after FrontFence at index 11)

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
Slot 12: BackFence (NEW)
```

**Critical Success Factors:**
1. Complete Task 3 (network messages) before Task 5 (InventoryManager)
2. Update ALL message handler call sites in Task 4
3. Test compilation after each task to catch dependency issues early
4. Run checkpoint tests to verify no regressions
