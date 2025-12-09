# Requirements Document: RightFence Inventory Integration

## Introduction

This feature adds the RightFence item to the player inventory system, enabling players to collect, store, and manage RightFence items alongside existing inventory items. The RightFence item already exists as a world object but is not currently integrated into the inventory system.

## Glossary

- **RightFence**: A fence item that can be collected and stored in the player's inventory
- **Inventory System**: The game system that manages player-collected items, including storage, rendering, and network synchronization
- **ItemType**: An enumeration defining all collectible item types in the game
- **Inventory Slot**: A visual UI element displaying an item icon and count in the inventory panel
- **Sprite Sheet**: The texture atlas (assets.png) containing all item icons at specific coordinates

## Requirements

### Requirement 1

**User Story:** As a player, I want to collect RightFence items and see them in my inventory, so that I can track how many RightFence items I have gathered.

#### Acceptance Criteria

1. WHEN a player collects a RightFence item THEN the Inventory System SHALL add one RightFence to the player's inventory count
2. WHEN the RightFence count changes THEN the Inventory System SHALL display the updated count in the inventory UI
3. WHEN rendering the RightFence icon THEN the Inventory System SHALL extract the texture from coordinates (298, 192) with size 22x128 from the Sprite Sheet
4. WHEN displaying the RightFence in inventory THEN the Inventory System SHALL render it as a 32x32 icon in the inventory slot
5. WHEN a player has zero RightFence items THEN the Inventory System SHALL display a count of 0 in the RightFence inventory slot

### Requirement 2

**User Story:** As a player in multiplayer mode, I want my RightFence inventory to synchronize with the server, so that my RightFence count is consistent across all clients.

#### Acceptance Criteria

1. WHEN a player collects a RightFence in multiplayer mode THEN the Inventory System SHALL send an inventory update message to the server
2. WHEN the server sends an inventory sync message THEN the Inventory System SHALL update the RightFence count to match the server's authoritative value
3. WHEN a player removes a RightFence from inventory THEN the Inventory System SHALL send an inventory update message to the server

### Requirement 3

**User Story:** As a player, I want to select the RightFence item from my inventory, so that I can use it for placement or other actions.

#### Acceptance Criteria

1. WHEN a player selects the RightFence inventory slot THEN the Inventory System SHALL highlight the RightFence slot with a golden border
2. WHEN the RightFence slot is selected THEN the Inventory System SHALL return ItemType.RIGHT_FENCE as the selected item type
3. WHEN a player deselects the RightFence slot THEN the Inventory System SHALL remove the highlight from the RightFence slot
4. WHEN the RightFence count reaches zero THEN the Inventory System SHALL automatically deselect the RightFence slot

### Requirement 4

**User Story:** As a developer, I want the RightFence item to follow the same patterns as existing inventory items, so that the codebase remains consistent and maintainable.

#### Acceptance Criteria

1. WHEN adding RightFence to ItemType enum THEN the Inventory System SHALL define it with restoresHealth=false, healthRestore=0, and reducesHunger=false
2. WHEN implementing RightFence inventory methods THEN the Inventory System SHALL provide add, remove, get, and set methods following the same pattern as existing items
3. WHEN positioning the RightFence slot THEN the Inventory System SHALL place it immediately after the BackFence slot (slot index 13)
