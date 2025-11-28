# Woodlanders - Java Classes Reference

Complete reference of all Java classes in the Woodlanders project, organized by package.

## Core Game (wagemaker.uk.gdx)

| Class | Purpose |
|-------|---------|
| **MyGdxGame** | Main game loop, rendering pipeline, game state management |
| **GameMessageHandler** | Processes incoming network messages during gameplay |

## Desktop Launcher (wagemaker.uk.desktop)

| Class | Purpose |
|-------|---------|
| **DesktopLauncher** | Entry point for desktop application |

## Player System (wagemaker.uk.player)

| Class | Purpose |
|-------|---------|
| **Player** | Local player with movement, animation, combat, health |
| **RemotePlayer** | Remote player representation in multiplayer |

## World Generation (wagemaker.uk.biome)

| Class | Purpose |
|-------|---------|
| **BiomeManager** | Generates and manages biome zones |
| **BiomeType** | Enum for grass and sand biomes |
| **BiomeTextureGenerator** | Dynamic texture generation per biome |
| **BiomeConfig** | Biome configuration parameters |
| **BiomeZone** | Represents a biome zone with distance-based intensity |

## Tree System (wagemaker.uk.trees)

| Class | Purpose |
|-------|---------|
| **SmallTree** | Small decorative trees |
| **AppleTree** | Fruit-bearing trees with apple drops |
| **BananaTree** | Fruit-bearing trees with banana drops |
| **BambooTree** | Bamboo with unique collision |
| **CoconutTree** | Coconut trees |
| **Cactus** | Environmental hazard with damage |

## Inventory & Items (wagemaker.uk.inventory, wagemaker.uk.items)

| Class | Purpose |
|-------|---------|
| **Inventory** | 6-slot inventory data structure |
| **InventoryManager** | Inventory operations and auto-consumption |
| **ItemType** | Item type enumeration |
| **Apple** | Consumable apple item |
| **Banana** | Consumable banana item |
| **BambooSapling** | Plantable bamboo sapling item |
| **BabyTree** | Plantable baby tree item |
| **BambooStack** | Resource bamboo stack item |
| **WoodStack** | Resource wood stack item |
| **PalmFiber** | Resource palm fiber item |
| **Pebble** | Resource pebble item |

## Planting System (wagemaker.uk.planting)

| Class | Purpose |
|-------|---------|
| **PlantingSystem** | Core planting logic and validation |
| **PlantedBamboo** | Planted bamboo entity with growth timer |
| **PlantedTree** | Planted tree entity with growth timer |

## Targeting System (wagemaker.uk.targeting)

| Class | Purpose |
|-------|---------|
| **TargetingSystem** | Tile-based targeting with WASD movement |
| **TargetingMode** | Enum for targeting modes (ADJACENT, RANGE) |
| **TargetingCallback** | Interface for targeting confirmation/cancellation |
| **TargetValidator** | Validates target positions |
| **PlantingTargetValidator** | Validates planting target positions |
| **TargetIndicatorRenderer** | Renders visual targeting indicator |
| **Direction** | Enum for movement directions |

## Networking (wagemaker.uk.network)

### Core Network Classes
| Class | Purpose |
|-------|---------|
| **GameServer** | Multiplayer server implementation |
| **GameClient** | Multiplayer client implementation |
| **ClientConnection** | Individual client connection handler |
| **NetworkMessage** | Base message class |
| **MessageType** | Enum of 25+ message types |
| **MessageHandler** | Interface for message handling |
| **DefaultMessageHandler** | Default message handler implementation |

### Network State Classes
| Class | Purpose |
|-------|---------|
| **PlayerState** | Network player data structure |
| **TreeState** | Network tree data structure |
| **ItemState** | Network item data structure |
| **StoneState** | Network stone data structure |
| **PlantedBambooState** | Network planted bamboo data structure |
| **PlantedTreeState** | Network planted tree data structure |
| **WorldState** | Complete world state structure |
| **WorldStateUpdate** | Incremental world state updates |

### Network Message Types
| Class | Purpose |
|-------|---------|
| **PlayerMovementMessage** | Player position and direction updates |
| **PlayerJoinMessage** | Player joining the game |
| **PlayerLeaveMessage** | Player leaving the game |
| **PlayerHealthUpdateMessage** | Player health changes |
| **PlayerHungerUpdateMessage** | Player hunger changes |
| **PlayerRespawnMessage** | Player respawn event |
| **AttackActionMessage** | Attack on trees/stones/players |
| **ItemPickupMessage** | Item pickup confirmation |
| **ItemSpawnMessage** | Item spawning in world |
| **ItemConsumptionMessage** | Item consumption event |
| **TreeHealthUpdateMessage** | Tree health changes |
| **TreeDestroyedMessage** | Tree destruction |
| **TreeCreatedMessage** | Tree creation |
| **TreeRemovalMessage** | Ghost tree removal |
| **TreePlantMessage** | Tree planting action |
| **TreeTransformMessage** | Planted tree transformation |
| **StoneHealthUpdateMessage** | Stone health changes |
| **StoneDestroyedMessage** | Stone destruction |
| **StoneCreatedMessage** | Stone creation |
| **BambooPlantMessage** | Bamboo planting action |
| **BambooTransformMessage** | Planted bamboo transformation to tree |
| **InventoryUpdateMessage** | Inventory changes |
| **InventorySyncMessage** | Inventory synchronization |
| **ConnectionAcceptedMessage** | Connection acceptance |
| **ConnectionRejectedMessage** | Connection rejection |
| **HeartbeatMessage** | Connection keepalive |
| **PingMessage** | Latency measurement request |
| **PongMessage** | Latency measurement response |
| **PositionCorrectionMessage** | Server position correction |
| **WorldStateMessage** | Complete world state sync |
| **WorldStateUpdateMessage** | Incremental world state updates |
| **ResourceRespawnMessage** | Resource respawn event |
| **RespawnStateMessage** | Respawn state synchronization |
| **FreeWorldActivationMessage** | Free World mode activation |

### Network Enums
| Class | Purpose |
|-------|---------|
| **Direction** | Movement directions (UP, DOWN, LEFT, RIGHT) |
| **TreeType** | Tree type enumeration |
| **ItemType** | Item type enumeration |

## World Persistence (wagemaker.uk.world)

| Class | Purpose |
|-------|---------|
| **WorldSaveManager** | Save/load operations |
| **WorldSaveData** | Complete world state structure |
| **WorldSaveInfo** | Save metadata |

## Weather System (wagemaker.uk.weather)

| Class | Purpose |
|-------|---------|
| **DynamicRainManager** | Rain event management and scheduling |
| **RainSystem** | Rain particle system |
| **RainRenderer** | Rain rendering |
| **RainZoneManager** | Rain zone tracking |
| **RainZone** | Individual rain zone |
| **RainParticle** | Rain particle entity |
| **RainConfig** | Rain configuration |
| **PuddleManager** | Water puddle management |
| **PuddleRenderer** | Water puddle rendering |
| **PuddleState** | Puddle state tracking |
| **PuddleConfig** | Puddle configuration |
| **WaterPuddle** | Individual water puddle entity |

## Resource Respawn System (wagemaker.uk.respawn)

| Class | Purpose |
|-------|---------|
| **RespawnManager** | Resource respawn timer system |
| **RespawnEntry** | Respawn timer entry |
| **RespawnIndicator** | Visual respawn indicator |
| **RespawnConfig** | Respawn configuration |
| **ResourceType** | Enum for resource types (TREE, STONE) |

## Game Objects (wagemaker.uk.objects)

| Class | Purpose |
|-------|---------|
| **Stone** | Stone/rock entity with health |

## User Interface (wagemaker.uk.ui)

| Class | Purpose |
|-------|---------|
| **GameMenu** | Main in-game menu system |
| **MultiplayerMenu** | Multiplayer connection options |
| **PlayerProfileMenu** | Player profile and customization |
| **ConnectDialog** | Server connection dialog |
| **ServerHostDialog** | Server hosting dialog |
| **WorldSaveDialog** | World save dialog |
| **WorldLoadDialog** | World load dialog |
| **WorldManageDialog** | World management dialog |
| **LanguageDialog** | Language selection dialog |
| **FontSelectionDialog** | Font selection dialog |
| **CharacterSelectionDialog** | Character sprite selection dialog |
| **ControlsDialog** | Game controls display dialog |
| **PlayerLocationDialog** | Player location information dialog |
| **ErrorDialog** | Error message dialog |
| **Compass** | Navigation compass UI |
| **HealthBarUI** | Health and hunger bar display |
| **InventoryRenderer** | Inventory display |
| **ConnectionQualityIndicator** | Network status display |
| **FontManager** | Font loading and management |
| **FontChangeListener** | Font change notifications |
| **FontType** | Enum for font types |

## Localization (wagemaker.uk.localization)

| Class | Purpose |
|-------|---------|
| **LocalizationManager** | Multi-language support |
| **LanguageChangeListener** | Language change notifications |

## Client Configuration (wagemaker.uk.client)

| Class | Purpose |
|-------|---------|
| **PlayerConfig** | Player configuration management |

## Birds System (wagemaker.uk.birds)

| Class | Purpose |
|-------|---------|
| **Bird** | Individual bird entity with flight behavior |
| **BirdFormation** | V-shape bird formation |
| **BirdFormationManager** | Manages bird formation spawning and lifecycle |
| **SpawnBoundary** | Enum for spawn boundaries (TOP, BOTTOM, LEFT, RIGHT) |
| **SpawnPoint** | Bird spawn point data structure |

## Free World System (wagemaker.uk.freeworld)

| Class | Purpose |
|-------|---------|
| **FreeWorldManager** | Manages Free World mode activation and item grants |

## Server (wagemaker.uk.server)

| Class | Purpose |
|-------|---------|
| **DedicatedServerLauncher** | Standalone server entry point |
| **ServerConfig** | Server configuration management |
| **ServerLogger** | Server logging |
| **ServerMonitor** | Server statistics and monitoring |

## Class Count Summary

- **Total Classes**: 145+
- **Network Messages**: 33+
- **UI Components**: 20
- **Tree Types**: 6
- **Item Types**: 8
- **Game Systems**: 11 major systems

## Key Architectural Patterns

### Message-Driven Architecture
- 25+ network message types for complete state synchronization
- Server-authoritative validation
- Client prediction for responsiveness

### Chunk-Based Rendering
- Only visible chunks rendered
- Optimized collision detection
- Spatial partitioning

### Deferred Operations
- Thread-safe OpenGL operations
- Network thread queues operations for render thread
- Lock-free concurrent queue

### Separate State Management
- Independent singleplayer/multiplayer inventories
- Separate position saves
- Prevents data corruption

### Deterministic World Generation
- Seeded random for multiplayer consistency
- Same coordinates always produce same trees
- Biome-aware generation
