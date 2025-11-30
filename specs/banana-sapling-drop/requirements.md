# Requirements Document

## Introduction

This feature implements a dual-item drop system for BananaTree destruction. When a BananaTree is destroyed, it will drop two distinct items (Banana and BananaSapling) at the tree's position, positioned a few pixels apart. This extends the existing single-item drop pattern to support multiple item drops from a single tree, following the same pattern used by AppleTree (Apple + AppleSapling), BambooTree (BambooStack + BambooSapling), and SmallTree (TreeSapling + WoodStack).

## Glossary

- **Game System**: The Woodlanders game application that manages game state, rendering, and player interactions
- **BananaTree**: A tree entity in the game world that can be attacked and destroyed by players, rendered at 128x128 pixels
- **Banana**: An item entity representing a banana fruit, extracted from the sprite sheet at coordinates (64, 128) with 64x64 dimensions
- **BananaSapling**: An item entity representing a young banana tree sapling, extracted from the sprite sheet at coordinates (192, 192) with 64x64 dimensions
- **Item Drop**: The process of spawning item entities at a tree's position when the tree is destroyed
- **Render Size**: The visual dimensions at which an item is displayed on screen (32x32 pixels for both items)
- **Sprite Sheet**: The texture atlas file (assets.png) containing all game sprites
- **Player Class**: The class responsible for handling player actions including tree attacks in single-player mode
- **MyGdxGame Class**: The main game class responsible for rendering items and managing game state
- **Multiplayer Mode**: Game mode where tree destruction is handled by the server
- **Single-Player Mode**: Game mode where tree destruction is handled locally by the Player class
- **ItemType Enum**: Network enumeration defining item types for multiplayer synchronization
- **PlayerState**: Network class representing a player's state including position, health, and inventory counts for multiplayer synchronization
- **Inventory System**: The player inventory system that stores collected items and persists them in save files

## Requirements

### Requirement 1

**User Story:** As a player, I want BananaTree to drop two items when destroyed, so that I can collect both Banana and BananaSapling resources

#### Acceptance Criteria

1. WHEN a BananaTree health reaches zero, THE Game System SHALL spawn one Banana item at the tree's base position
2. WHEN a BananaTree health reaches zero, THE Game System SHALL spawn one BananaSapling item at the tree's base position offset by 8 pixels horizontally from the Banana
3. WHERE the game is in single-player mode, THE Player Class SHALL create both item instances and add them to the game's item collections when a BananaTree is destroyed
4. WHERE the game is in multiplayer mode, THE Game System SHALL handle banana item spawning through the server's item spawn messaging system
5. WHEN banana items are spawned, THE Game System SHALL use unique identifiers for each item based on the tree's position key with suffixes "-banana" and "-bananasapling"

### Requirement 2

**User Story:** As a player, I want banana items to be visually distinct and properly sized, so that I can easily identify and collect them

#### Acceptance Criteria

1. THE MyGdxGame Class SHALL render Banana items at 32x32 pixels on screen
2. THE MyGdxGame Class SHALL render BananaSapling items at 32x32 pixels on screen
3. THE Banana Class SHALL extract its texture from sprite sheet coordinates (64, 128) with source dimensions 64x64
4. THE BananaSapling Class SHALL extract its texture from sprite sheet coordinates (192, 192) with source dimensions 64x64
5. THE MyGdxGame Class SHALL maintain separate collections for Banana and BananaSapling items to enable independent rendering and collision detection

### Requirement 3

**User Story:** As a player, I want to be able to pick up banana items by walking over them, so that I can collect the resources

#### Acceptance Criteria

1. WHEN the player's collision box overlaps with a Banana item, THE Player Class SHALL remove the Banana from the game world
2. WHEN the player's collision box overlaps with a BananaSapling item, THE Player Class SHALL remove the BananaSapling from the game world
3. THE Player Class SHALL check for banana item pickups during each update cycle using the same collision detection pattern as other items
4. WHEN a banana item is picked up in multiplayer mode, THE Game System SHALL send an item pickup message to the server
5. THE Player Class SHALL dispose of banana item textures when items are picked up to prevent memory leaks
6. THE Player Class SHALL use a 32-pixel pickup range from the player's center to the item's center

### Requirement 4

**User Story:** As a player, I want BananaSapling items to be added to my inventory when picked up, so that I can use them for planting later

#### Acceptance Criteria

1. WHEN a BananaSapling item is picked up, THE Inventory System SHALL add one BananaSapling to the player's inventory
2. THE Inventory System SHALL support the BananaSapling item type in the ItemType enumeration
3. THE Inventory System SHALL display the BananaSapling count in the inventory UI
4. THE Inventory System SHALL allow the player to select BananaSapling from the inventory for planting
5. WHEN the player saves the game, THE World Save System SHALL persist the BananaSapling inventory count
6. WHEN the player loads a saved game, THE World Save System SHALL restore the BananaSapling inventory count

### Requirement 5

**User Story:** As a multiplayer client, I want BananaSapling items to synchronize correctly across all players, so that everyone sees the same game state

#### Acceptance Criteria

1. THE ItemType Network Enum SHALL include a BANANA_SAPLING entry for network synchronization
2. WHEN a BananaSapling is spawned in multiplayer mode, THE Game Server SHALL broadcast an ItemSpawnMessage with type BANANA_SAPLING
3. WHEN a BananaSapling is picked up in multiplayer mode, THE Game Server SHALL broadcast an ItemPickupMessage for the BananaSapling
4. WHEN a client receives a BananaSapling spawn message, THE Game Client SHALL create a BananaSapling instance at the specified position
5. WHEN a client receives a BananaSapling pickup message, THE Game Client SHALL remove the BananaSapling from the local collection
