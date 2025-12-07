# Requirements Document

## Introduction

This feature adds the LeftFence item to the player inventory system, enabling players to collect, store, and manage LeftFence items alongside existing inventory items. The LeftFence item already exists as a world object but is not currently integrated into the inventory system.

## Glossary

- **LeftFence**: A fence item that can be collected and stored in the player's inventory
- **Inventory System**: The game system that manages player-collected items, including storage, rendering, and network synchronization
- **ItemType**: An enumeration defining all collectible item types in the game
- **Inventory Slot**: A visual UI element displaying an item icon and count in the inventory panel
- **Sprite Sheet**: The texture atlas (assets.png) containing all item icons at specific coordinates

## Requirements

### Requirement 1

**User Story:** As a player, I want to collect LeftFence items and see them in my inventory, so that I can track how many LeftFence items I have gathered.

#### Acceptance Criteria

1. WHEN a player collects a LeftFence item THEN the Inventory System SHALL add one LeftFence to the player's inventory count
2. WHEN the LeftFence count changes THEN the Inventory System SHALL display the updated count in the inventory UI
3. WHEN rendering the LeftFence icon THEN the Inventory System SHALL extract the texture from coordinates (256, 192) with size 32x128 from the Sprite Sheet
4. WHEN displaying the LeftFence in inventory THEN the Inventory System SHALL render it as a 32x32 icon in the inventory slot
5. WHEN a player has zero LeftFence items THEN the Inventory System SHALL display a count of 0 in the LeftFence inventory slot

### Requirement 2

**User Story:** As a player in multiplayer mode, I want my LeftFence inventory to synchronize with the server, so that my LeftFence count is consistent across all clients.

#### Acceptance Criteria

1. WHEN a player collects a LeftFence in multiplayer mode THEN the Inventory System SHALL send an inventory update message to the server
2. WHEN the server sends an inventory sync message THEN the Inventory System SHALL update the LeftFence count to match the server's authoritative value
3. WHEN a player removes a LeftFence from inventory THEN the Inventory System SHALL send an inventory update message to the server

### Requirement 3

**User Story:** As a player, I want to select the LeftFence item from my inventory, so that I can use it for placement or other actions.

#### Acceptance Criteria

1. WHEN a player selects the LeftFence inventory slot THEN the Inventory System SHALL highlight the LeftFence slot with a golden border
2. WHEN the LeftFence slot is selected THEN the Inventory System SHALL return ItemType.LEFT_FENCE as the selected item type
3. WHEN a player deselects the LeftFence slot THEN the Inventory System SHALL remove the highlight from the LeftFence slot
4. WHEN the LeftFence count reaches zero THEN the Inventory System SHALL automatically deselect the LeftFence slot

### Requirement 4

**User Story:** As a developer, I want the LeftFence item to follow the same patterns as existing inventory items, so that the codebase remains consistent and maintainable.

#### Acceptance Criteria

1. WHEN adding LeftFence to ItemType enum THEN the Inventory System SHALL define it with restoresHealth=false, healthRestore=0, and reducesHunger=false
2. WHEN implementing LeftFence inventory methods THEN the Inventory System SHALL provide add, remove, get, and set methods following the same pattern as existing items
3. WHEN positioning the LeftFence slot THEN the Inventory System SHALL place it immediately after the BananaSapling slot (slot index 10)
