# Game Features

## Player Character
- **Animated Human Sprite**: 64x64 pixel character with skin, black hair, blue shirt, brown pants, and black shoes
- **Walking Animation**: Arms and legs animate with opposite motion (realistic walking pattern)
- **Smooth Movement**: 200 pixels/second movement speed with arrow key controls
- **Camera Following**: Camera centers on player and follows movement
- **Health System**: Player health can be damaged and restored through gameplay

## World System
- **Infinite World**: Minecraft-like infinite terrain generation
- **Grass Background**: Seamless tiled grass texture covering the entire world
- **Fixed Viewport**: 800x600 camera view regardless of screen size
- **Chunk-Based Rendering**: Only renders visible areas for optimal performance

## Environmental Hazards
- **Cactus Damage**: Walking into cacti damages the player, adding environmental danger to exploration
- **Collision Detection**: Cacti have collision boxes that trigger damage on contact

## Tree System
- **Three Tree Types**:
  - **Regular Trees**: 64x64 brown trunk with green leaves
  - **Apple Trees**: 128x128 larger trees with red apples scattered on leaves - restore player health when harvested
  - **Banana Trees**: Restore player health when harvested
- **Random Generation**: 5% spawn chance per grass tile with distribution across tree types
- **Collision Detection**: Players cannot walk through trees (optimized collision boxes)
- **Deterministic Placement**: Same trees appear in same locations every time
- **Destructible**: Players can destroy trees by attacking them
- **Health Regeneration**: Trees that are damaged but not fully destroyed will slowly regain health over time

## Combat & Interaction System
- **Tree Chopping**: Attack trees with spacebar key
- **Health System**: Each tree has 100 health, loses 10 per attack (10 hits to destroy)
- **Attack Range**: 
  - **Regular Trees**: 64px in all directions from center (128x128 attack area)
  - **Apple Trees**: 64px left/right, 128px up/down from center
- **Health Bars**: 
  - Appear above trees when attacked (3-second visibility)
  - Green background with red overlay showing damage percentage
  - Half the width of each tree type (32px for trees, 64px for apple trees)
- **Permanent Destruction**: Destroyed trees never regenerate
- **Collision Removal**: Destroyed trees no longer block movement
- **Health Restoration**: Apple and banana trees provide health recovery when harvested

## Tile Targeting System
- **Visual Indicator**: White circular indicator (16x16 pixels, 70% opacity) shows target tile location
- **Automatic Activation**: Targeting activates automatically when a placeable item is selected from inventory
- **Persistent Targeting**: Indicator remains visible as long as an item is selected, allowing multiple placements
- **Tile-Based Movement**: Target moves in 64-pixel increments aligned to the game's tile grid
- **WASD Controls**: 
  - **A**: Move target left
  - **W**: Move target up
  - **D**: Move target right
  - **S**: Move target down
- **Placement Actions**:
  - **Spacebar**: Context-sensitive - plants item when targeting is active
  - **P Key**: Alternative planting key
- **Target Validation**: Indicator changes color based on validity:
  - **White**: Valid placement location
  - **Red**: Invalid placement location (occupied tile, wrong biome, etc.)
- **Cancellation**: Press ESC to cancel targeting without placing
- **Deactivation**: Press the item key again to deselect and hide targeting
- **Client-Side Only**: Target indicator is not visible to other players in multiplayer
- **Coordinate Synchronization**: Planted items appear at exact same coordinates for all clients
- **Server Validation**: Server validates placement coordinates before accepting

## Planting System
- **Bamboo Sapling Planting**: Plant bamboo sapling on sand tiles using the targeting system
- **Banana Sapling Planting**: Plant banana sapling on grass tiles using the targeting system (inventory slot 5)
- **Tree Sapling Planting**: Plant tree saplings on grass tiles for regular trees
- **Growth Mechanics**: 
  - Planted bamboo grows over 120 seconds into a harvestable bamboo tree
  - Planted banana trees grow over 120 seconds into harvestable banana trees
  - Planted regular trees grow over 120 seconds into harvestable small trees
- **Biome Restrictions**: 
  - Bamboo saplings: Sand biome only
  - Banana saplings: Grass biome only
  - Tree saplings: Grass biome only
- **Tile Occupation Check**: Cannot plant on tiles already occupied by trees or other objects
- **Inventory Integration**: Automatically deducts sapling from inventory on successful planting
- **Visual Feedback**: Planted sapling sprite appears immediately at target location
- **Growth Timer Preservation**: Growth progress is saved and restored when loading worlds
- **Multiplayer Sync**: All planted items synchronized across all connected clients
- **Network Validation**: Server validates planting location, biome, range, and inventory before accepting
- **State Rollback**: Failed network operations rollback local state to maintain consistency
- **Grid Snapping**: All planted items snap to 64x64 tile grid for consistent placement

## Controls
- **Arrow Keys**: Move character (Up/Down/Left/Right)
- **Spacebar**: Context-sensitive action:
  - **No item selected**: Attack nearby trees
  - **Item selected (targeting active)**: Plant item at target location
- **A/W/D/S**: Move targeting indicator (when targeting is active)
- **P Key**: Plant item at target location (when targeting is active)
- **1-6 Keys**: Select/deselect inventory slots (toggle selection)
  - Selecting a placeable item activates targeting
  - Deselecting an item deactivates targeting
- **ESC**: Cancel targeting or open/close menu
- **Tab**: Toggle inventory display
- **Fullscreen**: Maintains proper scaling and collision detection

## Multiplayer Features
- **Dedicated Server**: Run standalone server for multiplayer gameplay
- **Client Connection**: Connect to servers via IP address and port
- **Player Names**: Customizable player names (minimum 3 characters)
- **Server Configuration**: Configurable via server.properties file
- **Network Synchronization**: Real-time player position and action updates

## Fence Building System
- **Custom Fence Construction**: Build rectangular fence enclosures of any size using collected materials
- **8-Piece Fence System**: Complete fence structures using corner and edge pieces in clockwise sequence
- **Material Types**: Wood and bamboo fence materials with distinct textures
- **Building Mode**: Dedicated building mode activated with B key
- **Fence Piece Selection**: Visual fence piece selector showing all 8 fence types with arrow key navigation
- **Dual UI System**: Fence selection panel appears above inventory when fence building mode is active
- **Grid-Based Placement**: 64x64 pixel grid system for precise fence positioning
- **Automatic Piece Selection**: System automatically selects correct fence piece type based on position
- **Visual Feedback**: Grid overlay, material count display, fence piece selection, and placement previews
- **Interactive Selection**: Press B key to activate fence selection mode, use left/right arrows to select fence pieces
- **Targeting Integration**: Fence selection automatically activates targeting system for precise placement
- **Click-to-Build**: Left-click to place fence segments, right-click to remove
- **Material Collection**: Fence materials automatically collected when harvesting trees and bamboo
- **Inventory Integration**: Fence materials work with existing inventory system and display in Free World mode
- **Collision Detection**: Fence structures create collision boundaries that block movement
- **World Persistence**: Fence structures save and load with world data
- **Multiplayer Synchronization**: Fence building synchronized across all clients
- **Ownership System**: Players can only remove fence pieces they placed in multiplayer
- **Material Conservation**: Removing fence pieces returns materials to inventory
- **Performance Optimization**: Efficient rendering with texture atlasing and viewport culling

## Technical Features
- **Collision Detection**: Precise collision boxes for trees, cacti, and player
- **Individual Attack Ranges**: Each tree type has unique attack collision detection
- **Health Bar Rendering**: Dynamic health visualization using ShapeRenderer
- **Health Regeneration System**: Damaged trees slowly recover health over time
- **Environmental Damage**: Cactus collision detection and damage system
- **Tile Grid System**: 64x64 pixel tile-based world with coordinate snapping
- **Targeting Validation**: Real-time validation of target positions with visual feedback
- **Context-Sensitive Input**: Spacebar adapts behavior based on game state (attack vs plant)
- **Client-Side Rendering**: Targeting indicators rendered locally without network transmission
- **Server Authority**: Server validates all placement actions before accepting
- **State Synchronization**: Planted objects synchronized across all clients with coordinate consistency
- **Memory Management**: Proper texture disposal and cleanup
- **Performance Optimization**: Only processes visible objects
- **Modular Design**: Separate classes for Player, Tree, AppleTree, TargetingSystem, and environmental objects
- **Infinite Generation**: Dynamic world expansion without performance loss
- **Cleared Position Tracking**: Prevents tree regeneration at destroyed locations
