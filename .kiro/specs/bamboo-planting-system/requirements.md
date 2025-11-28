# Requirements Document

## Introduction

This feature enables players to plant bamboo sapling items from their inventory onto sand tiles. When planted, bamboo sapling remains visible for 120 seconds before transforming into a bamboo tree. The system manages inventory deduction, tile validation, and the growth lifecycle of planted bamboo.

## Glossary

- **Player**: The user-controlled character in the game
- **Inventory System**: The component that manages the player's collected items
- **Bamboo Sapling**: A plantable item (item ID 3) that can be placed on sand tiles
- **Sand Tile**: A 64x64 pixel game tile with sand terrain type
- **Bamboo Tree**: The mature form of bamboo sapling that appears after the growth period
- **Planting System**: The component responsible for placing bamboo sapling and managing growth
- **Growth Timer**: A 120-second countdown that triggers transformation from bamboo sapling to bamboo tree

## Requirements

### Requirement 1

**User Story:** As a player, I want to plant bamboo sapling on sand tiles, so that I can grow bamboo trees in sandy areas

#### Acceptance Criteria

1. WHEN the Player presses the "p" key, THE Planting System SHALL check if the selected inventory item is bamboo sapling (item 3)
2. WHEN the Player presses the "p" key with bamboo sapling selected, THE Planting System SHALL verify the Player is standing on a sand tile
3. WHEN the Player presses the "p" key on a valid sand tile with bamboo sapling selected, THE Planting System SHALL place one bamboo sapling at the current 64x64 tile location
4. WHEN bamboo sapling is successfully planted, THE Inventory System SHALL deduct one bamboo sapling from the Player's inventory
5. IF the Player presses "p" without bamboo sapling selected, THEN THE Planting System SHALL take no action

### Requirement 2

**User Story:** As a player, I want planted bamboo sapling to prevent duplicate planting, so that I cannot place multiple bamboo in the same location

#### Acceptance Criteria

1. WHEN the Player attempts to plant bamboo sapling, THE Planting System SHALL check if the target sand tile already contains bamboo sapling or a bamboo tree
2. IF the target sand tile is occupied by bamboo sapling or a bamboo tree, THEN THE Planting System SHALL prevent planting and take no action
3. WHEN a sand tile is empty, THE Planting System SHALL allow bamboo sapling placement

### Requirement 3

**User Story:** As a player, I want bamboo sapling to grow into bamboo trees automatically, so that I can see my planted bamboo mature over time

#### Acceptance Criteria

1. WHEN bamboo sapling is planted, THE Growth Timer SHALL start a 120-second countdown for that specific bamboo instance
2. WHEN the Growth Timer reaches 120 seconds, THE Planting System SHALL remove the bamboo sapling from the tile
3. WHEN the bamboo sapling is removed after 120 seconds, THE Planting System SHALL spawn a bamboo tree at the same tile location
4. WHILE the Growth Timer is active, THE Planting System SHALL maintain the bamboo sapling's visibility on the sand tile

### Requirement 4

**User Story:** As a player, I want to plant multiple bamboo sapling items consecutively, so that I can efficiently create bamboo groves

#### Acceptance Criteria

1. WHEN the Player moves to a different sand tile after planting, THE Planting System SHALL allow another planting action if bamboo sapling remains in inventory
2. WHEN the Player presses "p" on multiple consecutive sand tiles, THE Planting System SHALL place bamboo sapling on each valid tile
3. IF the Inventory System has zero bamboo sapling remaining, THEN THE Planting System SHALL prevent further planting actions
4. WHEN the Player plants bamboo sapling, THE Planting System SHALL provide immediate visual feedback showing the planted bamboo

### Requirement 5

**User Story:** As a player, I want clear feedback when planting fails, so that I understand why I cannot plant bamboo

#### Acceptance Criteria

1. IF the Player is not standing on a sand tile, THEN THE Planting System SHALL prevent planting
2. IF the Player has zero bamboo sapling in inventory, THEN THE Planting System SHALL prevent planting
3. IF the target tile already contains bamboo sapling or a bamboo tree, THEN THE Planting System SHALL prevent planting
4. WHEN planting is prevented, THE Planting System SHALL maintain the current game state without changes
