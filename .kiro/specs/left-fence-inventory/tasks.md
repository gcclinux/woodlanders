# Implementation Plan

- [x] 1. Add LEFT_FENCE to ItemType enum
  - Add LEFT_FENCE entry to the ItemType enum with properties: restoresHealth=false, healthRestore=0, reducesHunger=false
  - Position it after BANANA_SAPLING in the enum definition
  - _Requirements: 4.1_

- [x] 2. Extend Inventory class with LeftFence support
  - Add private int leftFenceCount field
  - Initialize leftFenceCount to 0 in constructor
  - Implement getLeftFenceCount() method
  - Implement setLeftFenceCount(int count) method with Math.max(0, count) validation
  - Implement addLeftFence(int amount) method
  - Implement removeLeftFence(int amount) method with availability check
  - Update clear() method to reset leftFenceCount to 0
  - _Requirements: 1.1, 4.2_

- [ ]* 2.1 Write property test for LeftFence collection increases count
  - **Property 1: LeftFence collection increases count**
  - **Validates: Requirements 1.1**

- [x] 3. Update InventoryManager to handle LeftFence
  - Add LEFT_FENCE case to collectItem() switch statement
  - Add LEFT_FENCE case to addItemToInventory() switch statement calling inventory.addLeftFence(amount)
  - Update sendInventoryUpdate() to include leftFenceCount in InventoryUpdateMessage
  - Add leftFenceCount parameter to syncFromServer() method signature
  - Update syncFromServer() to call inventory.setLeftFenceCount(leftFenceCount)
  - Add case 10 to getSelectedItemType() returning ItemType.LEFT_FENCE
  - Add case 10 to checkAndAutoDeselect() checking inventory.getLeftFenceCount()
  - _Requirements: 1.1, 2.1, 2.2, 2.3, 3.2, 3.4_

- [ ]* 3.1 Write property test for multiplayer collection triggers sync
  - **Property 3: Multiplayer collection triggers sync**
  - **Validates: Requirements 2.1**

- [ ]* 3.2 Write property test for server sync updates local count
  - **Property 4: Server sync updates local count**
  - **Validates: Requirements 2.2**

- [ ]* 3.3 Write property test for multiplayer removal triggers sync
  - **Property 5: Multiplayer removal triggers sync**
  - **Validates: Requirements 2.3**

- [ ]* 3.4 Write property test for slot 10 selection returns LEFT_FENCE
  - **Property 6: Slot 10 selection returns LEFT_FENCE**
  - **Validates: Requirements 3.2**

- [ ]* 3.5 Write property test for zero count triggers auto-deselect
  - **Property 7: Zero count triggers auto-deselect**
  - **Validates: Requirements 3.4**

- [x] 4. Update network messages to include LeftFence count
  - Add leftFenceCount field to InventoryUpdateMessage class
  - Update InventoryUpdateMessage constructor to accept leftFenceCount parameter
  - Add leftFenceCount field to InventorySyncMessage class (if it exists)
  - Update InventorySyncMessage constructor to accept leftFenceCount parameter
  - Update all call sites creating InventoryUpdateMessage to include leftFenceCount
  - Update message serialization/deserialization to handle leftFenceCount field
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 5. Extend InventoryRenderer to display LeftFence
  - Add private Texture leftFenceIcon field
  - Update loadItemIcons() to extract LeftFence icon from sprite sheet at coordinates (256, 192) with size 32x128
  - Scale or crop the extracted texture to 32x32 for inventory display
  - Update render() method to add 11th slot for LeftFence at position after BananaSapling (slot 10)
  - Call renderSlot() for LeftFence with leftFenceIcon, inventory.getLeftFenceCount(), and selectedSlot == 10
  - Update dispose() method to dispose leftFenceIcon texture
  - _Requirements: 1.2, 1.3, 1.4, 4.3_

- [ ]* 5.1 Write property test for displayed count matches inventory count
  - **Property 2: Displayed count matches inventory count**
  - **Validates: Requirements 1.2**

- [ ]* 5.2 Write unit tests for LeftFence rendering
  - Test that LeftFence icon is extracted from correct coordinates (256, 192)
  - Test that LeftFence icon is rendered at 32x32 pixels
  - Test that LeftFence is positioned at slot index 10
  - Test that zero count displays 0 in the UI
  - Test that selection highlights slot 10 with golden border
  - Test that deselection removes the highlight
  - _Requirements: 1.3, 1.4, 1.5, 3.1, 3.3, 4.3_

- [x] 6. Update InventoryRenderer panel width for 11 slots
  - Update PANEL_WIDTH calculation to accommodate 11 slots instead of 10
  - Verify that the panel renders correctly with the additional slot
  - _Requirements: 4.3_

- [x] 7. Checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
