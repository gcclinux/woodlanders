# Implementation Plan

- [x] 1. Add BananaSapling collection to MyGdxGame
  - Add `Map<String, BananaSapling> bananaSaplings` field declaration with other item collections
  - Initialize `bananaSaplings = new HashMap<>()` in create() method
  - Wire bananaSaplings collection to player with `player.setBananaSaplings(bananaSaplings)` in create()
  - _Requirements: 2.5_

- [x] 2. Implement BananaSapling rendering in MyGdxGame
  - Create `drawBananaSaplings()` method with viewport culling logic
  - Render BananaSapling items at 32x32 pixels using batch.draw()
  - Call `drawBananaSaplings()` in render() method after `drawBananas()`
  - _Requirements: 2.2_

- [x] 3. Add BananaSapling support to Player class
  - Add BananaSapling import statement at top of Player.java
  - Add `Map<String, BananaSapling> bananaSaplings` field declaration
  - Implement `setBananaSaplings()` setter method
  - _Requirements: 1.3_

- [x] 4. Implement dual-item drop logic for BananaTree destruction
  - Modify the BananaTree destruction block in Player's attack handling
  - Keep existing Banana spawn at tree's base position with key `targetKey`
  - Add BananaSapling spawn at position offset by 8 pixels horizontally using key `targetKey + "-bananasapling"`
  - Add BananaSapling to bananaSaplings collection
  - Add console logging for BananaSapling drop
  - _Requirements: 1.1, 1.2, 1.5_

- [x] 5. Implement BananaSapling pickup detection and handling
  - Create `checkBananaSaplingPickups()` method in Player class that iterates bananaSaplings and checks collision
  - Create `pickupBananaSapling()` method that handles single-player pickup (dispose, remove, add to inventory) and multiplayer pickup (send message)
  - Add call to `checkBananaSaplingPickups()` in Player's `update()` method after `checkBananaPickups()`
  - _Requirements: 3.2, 3.3, 3.4, 3.5, 4.1_

- [x] 6. Add BANANA_SAPLING to inventory ItemType enum
  - Open src/main/java/wagemaker/uk/inventory/ItemType.java
  - Add BANANA_SAPLING entry after BANANA with parameters (false, 0, false)
  - _Requirements: 4.2_

- [x] 7. Add BANANA_SAPLING to network ItemType enum
  - Open src/main/java/wagemaker/uk/network/ItemType.java
  - Add BANANA_SAPLING entry after BANANA
  - _Requirements: 5.1_

- [x] 8. Add BananaSapling inventory support to Inventory class
  - Open src/main/java/wagemaker/uk/inventory/Inventory.java
  - Add `bananaSaplingCount` field
  - Implement getBananaSaplingCount(), setBananaSaplingCount(), addBananaSapling(), and removeBananaSapling() methods
  - _Requirements: 4.1_

- [x] 9. Update InventoryManager for BananaSapling
  - Open src/main/java/wagemaker/uk/inventory/InventoryManager.java
  - Add BANANA_SAPLING case to addItemToInventory() method
  - Update syncFromServer() method signature to include bananaSaplingCount parameter
  - Add bananaSaplingCount sync logic in syncFromServer()
  - Update getSelectedItemType() to return BANANA_SAPLING for slot 9
  - Update checkAndAutoDeselect() to check bananaSaplingCount for slot 9
  - _Requirements: 4.1, 5.2, 5.3_

- [x] 10. Update InventorySyncMessage for BananaSapling
  - Open src/main/java/wagemaker/uk/network/InventorySyncMessage.java
  - Add bananaSaplingCount field to message
  - Update constructor and getters
  - _Requirements: 5.2_

- [x] 11. Update InventoryUpdateMessage for BananaSapling
  - Open src/main/java/wagemaker/uk/network/InventoryUpdateMessage.java
  - Add bananaSaplingCount field to message
  - Update constructor and getters
  - _Requirements: 5.2_

- [x] 11.5. Update PlayerState for BananaSapling
  - Open src/main/java/wagemaker/uk/network/PlayerState.java
  - Add `bananaSaplingCount` field to player state
  - Implement getBananaSaplingCount() and setBananaSaplingCount() methods
  - Update ClientConnection to use bananaSaplingCount in InventoryUpdateMessage and InventorySyncMessage constructors
  - Update GameMessageHandler to pass bananaSaplingCount to syncFromServer() method
  - _Requirements: 5.2, 5.3_

- [x] 12. Update multiplayer item spawn handling for BananaSapling
  - Locate server-side tree destruction handler in ClientConnection
  - Add BananaTree destruction case with dual-item spawn logic
  - Broadcast ItemSpawnMessage for Banana with type BANANA
  - Broadcast ItemSpawnMessage for BananaSapling with type BANANA_SAPLING
  - Locate client-side ItemSpawnMessage handler in MyGdxGame
  - Add BANANA_SAPLING case to create BananaSapling instances from spawn messages
  - Add BananaSapling to bananaSaplings collection
  - _Requirements: 5.2, 5.4_

- [x] 13. Update multiplayer item pickup handling for BananaSapling
  - Locate server-side ItemPickupMessage handler
  - Verify it handles BANANA_SAPLING pickup messages generically
  - Locate client-side pickup message handler in MyGdxGame
  - Add BANANA_SAPLING case to remove BananaSapling from collection and add to inventory
  - Verify inventory synchronization for BANANA_SAPLING items
  - _Requirements: 5.3, 5.5, 4.1_

- [x] 14. Update world save/load support for BananaSapling inventory
  - Open src/main/java/wagemaker/uk/world/WorldSaveData.java
  - Add bananaSaplingCount field to save data
  - Update save logic in WorldSaveManager to include bananaSaplingCount
  - Update load logic to restore bananaSaplingCount
  - _Requirements: 4.5, 4.6_

- [x] 15. Update InventoryRenderer for BananaSapling display
  - Open src/main/java/wagemaker/uk/ui/InventoryRenderer.java
  - Add BananaSapling rendering at slot 9
  - Extract BananaSapling texture from sprite sheet at (192, 192)
  - Display bananaSaplingCount next to BananaSapling icon
  - _Requirements: 4.3_

- [ ] 16. Test single-player dual-drop functionality
  - Start single-player game
  - Attack and destroy BananaTree
  - Verify both Banana and BananaSapling spawn 8 pixels apart
  - Verify items render at 32x32 pixels
  - Verify console logs show correct drop positions
  - Walk over Banana and verify pickup
  - Walk over BananaSapling and verify pickup + inventory increase
  - Check inventory UI shows BananaSapling count
  - Save game and verify BananaSapling count persists
  - Load game and verify BananaSapling count restored
  - _Requirements: 1.1, 1.2, 2.1, 2.2, 3.1, 3.2, 4.1, 4.3, 4.5, 4.6_

- [ ] 17. Test multiplayer dual-drop synchronization
  - Start server and connect 2+ clients
  - Client attacks BananaTree until destroyed
  - Verify both items spawn on all clients
  - Verify items render correctly on all clients
  - Verify items are positioned correctly on all clients
  - Client picks up Banana
  - Verify removal on all clients
  - Client picks up BananaSapling
  - Verify removal on all clients
  - Verify inventory synchronization across clients
  - Test with multiple players attacking different BananaTrees simultaneously
  - _Requirements: 1.4, 5.2, 5.3, 5.4, 5.5_

- [x] 18. Add BananaSapling to Free World mode item grants
  - Open src/main/java/wagemaker/uk/freeworld/FreeWorldManager.java
  - Add `inventory.setBananaSaplingCount(250)` to grantFreeWorldItems() method
  - Verify BananaSapling is granted alongside other items when Free World mode is activated
  - Test in both single-player and multiplayer Free World mode
  - _Requirements: 4.1_
