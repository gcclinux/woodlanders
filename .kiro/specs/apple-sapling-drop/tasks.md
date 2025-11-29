# Implementation Plan

- [x] 1. Add AppleSapling collection to MyGdxGame
  - Add `Map<String, AppleSapling> appleSaplings` field declaration with other item collections
  - Initialize `appleSaplings = new HashMap<>()` in create() method
  - Wire appleSaplings collection to player with `player.setAppleSaplings(appleSaplings)` in create()
  - _Requirements: 2.5_

- [x] 2. Implement AppleSapling rendering in MyGdxGame
  - Create `drawAppleSaplings()` method with viewport culling logic
  - Render AppleSapling items at 32x32 pixels using batch.draw()
  - Call `drawAppleSaplings()` in render() method after `drawApples()`
  - _Requirements: 2.2_

- [x] 3. Add AppleSapling support to Player class
  - Add AppleSapling import statement at top of Player.java
  - Add `Map<String, AppleSapling> appleSaplings` field declaration
  - Implement `setAppleSaplings()` setter method
  - _Requirements: 1.3_

- [x] 4. Implement dual-item drop logic for AppleTree destruction
  - Modify the AppleTree destruction block in Player's attack handling (around line 932)
  - Keep existing Apple spawn at tree's base position with key `targetKey`
  - Add AppleSapling spawn at position offset by 8 pixels horizontally using key `targetKey + "-applesapling"`
  - Add AppleSapling to appleSaplings collection
  - Add console logging for AppleSapling drop
  - _Requirements: 1.1, 1.2, 1.5_

- [x] 5. Implement AppleSapling pickup detection and handling
  - Create `checkAppleSaplingPickups()` method in Player class that iterates appleSaplings and checks collision
  - Create `pickupAppleSapling()` method that handles single-player pickup (dispose, remove, add to inventory) and multiplayer pickup (send message)
  - Add call to `checkAppleSaplingPickups()` in Player's `update()` method after `checkApplePickups()`
  - _Requirements: 3.2, 3.3, 3.4, 3.5, 4.1_

- [x] 6. Add APPLE_SAPLING to inventory ItemType enum
  - Open src/main/java/wagemaker/uk/inventory/ItemType.java
  - Add APPLE_SAPLING entry after APPLE with parameters (false, 0, false)
  - _Requirements: 4.2_

- [x] 7. Add APPLE_SAPLING to network ItemType enum
  - Open src/main/java/wagemaker/uk/network/ItemType.java
  - Add APPLE_SAPLING entry after APPLE
  - _Requirements: 5.1_

- [x] 8. Add AppleSapling inventory support to Inventory class
  - Open src/main/java/wagemaker/uk/inventory/Inventory.java
  - Add `appleSaplingCount` field
  - Implement getAppleSaplingCount(), setAppleSaplingCount(), addAppleSapling(), and removeAppleSapling() methods
  - _Requirements: 4.1_

- [x] 9. Update InventoryManager for AppleSapling
  - Open src/main/java/wagemaker/uk/inventory/InventoryManager.java
  - Add APPLE_SAPLING case to addItemToInventory() method
  - Update syncFromServer() method signature to include appleSaplingCount parameter
  - Add appleSaplingCount sync logic in syncFromServer()
  - Update getSelectedItemType() to return APPLE_SAPLING for slot 8
  - Update checkAndAutoDeselect() to check appleSaplingCount for slot 8
  - _Requirements: 4.1, 5.2, 5.3_

- [x] 10. Update InventorySyncMessage for AppleSapling
  - Open src/main/java/wagemaker/uk/network/InventorySyncMessage.java
  - Add appleSaplingCount field to message
  - Update constructor and getters
  - _Requirements: 5.2_

- [x] 11. Update InventoryUpdateMessage for AppleSapling
  - Open src/main/java/wagemaker/uk/network/InventoryUpdateMessage.java
  - Add appleSaplingCount field to message
  - Update constructor and getters
  - _Requirements: 5.2_

- [x] 11.5. Update PlayerState for AppleSapling
  - Open src/main/java/wagemaker/uk/network/PlayerState.java
  - Add `appleSaplingCount` field to player state
  - Implement getAppleSaplingCount() and setAppleSaplingCount() methods
  - Update ClientConnection to use appleSaplingCount in InventoryUpdateMessage and InventorySyncMessage constructors
  - Update GameMessageHandler to pass appleSaplingCount to syncFromServer() method
  - _Requirements: 5.2, 5.3_

- [x] 12. Update multiplayer item spawn handling for AppleSapling
  - Locate server-side tree destruction handler
  - Add AppleTree destruction case with dual-item spawn logic
  - Broadcast ItemSpawnMessage for Apple with type APPLE
  - Broadcast ItemSpawnMessage for AppleSapling with type APPLE_SAPLING
  - Locate client-side ItemSpawnMessage handler
  - Add APPLE_SAPLING case to create AppleSapling instances from spawn messages
  - Add AppleSapling to appleSaplings collection
  - _Requirements: 5.2, 5.4_

- [x] 13. Update multiplayer item pickup handling for AppleSapling
  - Locate server-side ItemPickupMessage handler
  - Verify it handles APPLE_SAPLING pickup messages generically
  - Locate client-side pickup message handler
  - Add APPLE_SAPLING case to remove AppleSapling from collection and add to inventory
  - Verify inventory synchronization for APPLE_SAPLING items
  - _Requirements: 5.3, 5.5, 4.1_

- [x] 14. Update world save/load support for AppleSapling inventory
  - Open src/main/java/wagemaker/uk/world/WorldSaveData.java
  - Add appleSaplingCount field to save data
  - Update save logic to include appleSaplingCount
  - Update load logic to restore appleSaplingCount
  - _Requirements: 4.5, 4.6_

- [x] 15. Update InventoryRenderer for AppleSapling display
  - Open src/main/java/wagemaker/uk/ui/InventoryRenderer.java
  - Add AppleSapling rendering at slot 8
  - Extract AppleSapling texture from sprite sheet at (192, 254)
  - Display appleSaplingCount next to AppleSapling icon
  - _Requirements: 4.3_

- [x] 16. Test single-player dual-drop functionality
  - Start single-player game
  - Attack and destroy AppleTree
  - Verify both Apple and AppleSapling spawn 8 pixels apart
  - Verify items render at 32x32 pixels
  - Verify console logs show correct drop positions
  - Walk over Apple and verify pickup
  - Walk over AppleSapling and verify pickup + inventory increase
  - Check inventory UI shows AppleSapling count
  - Save game and verify AppleSapling count persists
  - Load game and verify AppleSapling count restored
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 3.1, 3.2, 4.1, 4.3, 4.5, 4.6_

- [x] 17. Test multiplayer dual-drop synchronization
  - Start server and connect 2+ clients
  - Client attacks AppleTree until destroyed
  - Verify both items spawn on all clients
  - Verify items render correctly on all clients
  - Verify items are positioned correctly on all clients
  - Client picks up Apple
  - Verify removal on all clients
  - Client picks up AppleSapling
  - Verify removal on all clients
  - Verify inventory synchronization across clients
  - Test with multiple players attacking different AppleTrees simultaneously
  - _Requirements: 1.4, 5.2, 5.3, 5.4, 5.5_

- [x] 18. Add AppleSapling to Free World mode item grants
  - Open src/main/java/wagemaker/uk/freeworld/FreeWorldManager.java
  - Add `inventory.setAppleSaplingCount(250)` to grantFreeWorldItems() method
  - Verify AppleSapling is granted alongside other items when Free World mode is activated
  - Test in both single-player and multiplayer Free World mode
  - _Requirements: 4.1_
