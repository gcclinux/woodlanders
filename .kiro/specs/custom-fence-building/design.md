# Design Document: Custom Fence Building System

## Overview

The Custom Fence Building System enables players to construct rectangular fence enclosures of any size using an 8-piece fence component system. The system integrates with existing game mechanics including inventory management, collision detection, multiplayer synchronization, and world persistence. Players can enter a dedicated building mode to place and remove fence segments using cursor-based interaction, with real-time visual feedback and automatic piece selection.

## Architecture

The system follows a modular architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Fence Building System                    │
├─────────────────────────────────────────────────────────────┤
│  UI Layer          │  Building Mode  │  Visual Feedback    │
│  - Building UI     │  - Mode Toggle  │  - Preview System   │
│  - Material Count  │  - Input Handle │  - Grid Overlay     │
│  - Instructions    │  - Cursor Mode  │  - Sound Effects    │
├─────────────────────────────────────────────────────────────┤
│  Logic Layer       │  Fence Manager  │  Placement Logic    │
│  - Piece Selection │  - Structure    │  - Validation       │
│  - Size Calculation│    Management   │  - Auto-Connection  │
│  - Material Calc   │  - Persistence  │  - Collision Update │
├─────────────────────────────────────────────────────────────┤
│  Integration Layer │  Inventory      │  Multiplayer        │
│  - Material System │  - Item Types   │  - Network Messages │
│  - Collision System│  - Collection   │  - State Sync       │
│  - World Save/Load │  - Consumption  │  - Event Broadcast  │
├─────────────────────────────────────────────────────────────┤
│  Data Layer        │  Fence Pieces   │  World Data         │
│  - 8 Fence Types   │  - Textures     │  - Structure Storage│
│  - Position Data   │  - Rendering    │  - Save Format      │
│  - Connection Info │  - Disposal     │  - Load Validation  │
└─────────────────────────────────────────────────────────────┘
```

## Components and Interfaces

### Core Components

#### 1. FenceBuildingManager
Central coordinator for all fence building operations.

```java
public class FenceBuildingManager {
    private boolean buildingModeActive;
    private FenceStructureManager structureManager;
    private FencePlacementValidator validator;
    private FencePreviewRenderer previewRenderer;
    
    public void toggleBuildingMode();
    public boolean placeFenceSegment(int gridX, int gridY);
    public boolean removeFenceSegment(int gridX, int gridY);
    public void calculateEnclosureRequirements(Rectangle area);
}
```

#### 2. FenceStructureManager
Manages fence structure data and persistence.

```java
public class FenceStructureManager {
    private Map<Point, FencePiece> placedFences;
    private List<FenceEnclosure> enclosures;
    
    public void addFencePiece(Point position, FencePieceType type);
    public void removeFencePiece(Point position);
    public FencePieceType determinePieceType(Point position);
    public void updateConnections(Point position);
}
```

#### 3. FencePieceFactory
Creates and manages fence piece instances.

```java
public class FencePieceFactory {
    public static FencePiece createPiece(FencePieceType type, float x, float y);
    public static FencePieceType[] getEnclosureSequence(Rectangle bounds);
    public static int calculateMaterialRequirement(Rectangle bounds);
}
```

### Fence Piece Hierarchy

```java
public abstract class FencePiece {
    protected float x, y;
    protected Texture texture;
    protected FencePieceType type;
    
    public abstract void render(SpriteBatch batch);
    public abstract Rectangle getCollisionBounds();
    public void dispose();
}

public enum FencePieceType {
    FENCE_BACK_LEFT,    // Top-left corner
    FENCE_BACK,         // Top edge
    FENCE_BACK_RIGHT,   // Top-right corner
    FENCE_MIDDLE_RIGHT, // Right edge
    FENCE_FRONT_RIGHT,  // Bottom-right corner
    FENCE_FRONT,        // Bottom edge
    FENCE_FRONT_LEFT,   // Bottom-left corner
    FENCE_MIDDLE_LEFT   // Left edge
}
```

### Integration Interfaces

#### 1. Inventory Integration
```java
public interface FenceMaterialProvider {
    boolean hasEnoughMaterials(FenceMaterialType type, int count);
    void consumeMaterials(FenceMaterialType type, int count);
    void returnMaterials(FenceMaterialType type, int count);
    int getMaterialCount(FenceMaterialType type);
}
```

#### 2. Multiplayer Integration
```java
public class FenceNetworkMessage extends NetworkMessage {
    public enum FenceAction { PLACE, REMOVE, SYNC }
    private FenceAction action;
    private Point position;
    private FencePieceType pieceType;
    private String playerId;
}
```

## Data Models

### FenceEnclosure
Represents a complete rectangular fence structure.

```java
public class FenceEnclosure {
    private Rectangle bounds;
    private List<FencePiece> pieces;
    private FenceMaterialType materialType;
    private String ownerId; // For multiplayer ownership
    private long creationTime;
    
    public boolean isComplete();
    public List<Rectangle> getCollisionBounds();
    public FenceEnclosureData serialize();
}
```

### FenceGrid
Manages the grid system for fence placement.

```java
public class FenceGrid {
    private static final int GRID_SIZE = 64; // 64x64 pixel grid
    private Set<Point> occupiedPositions;
    
    public Point worldToGrid(float worldX, float worldY);
    public Vector2 gridToWorld(Point gridPos);
    public boolean isValidPlacement(Point gridPos);
    public List<Point> getAdjacentPositions(Point gridPos);
}
```

### Material System Integration

```java
public enum FenceMaterialType {
    WOOD("Wood Fence Material", "fence_wood_icon.png"),
    BAMBOO("Bamboo Fence Material", "fence_bamboo_icon.png");
    
    private final String displayName;
    private final String iconPath;
}

public class FenceMaterialItem extends InventoryItem {
    private FenceMaterialType materialType;
    private int stackSize = 64; // Max stack size
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*
Based on the requirements analysis, the following properties ensure system correctness:

**Property 1: Building mode UI consistency**
*For any* building mode state change, the UI elements (grid overlay, material count, instructions) should be visible when active and hidden when inactive
**Validates: Requirements 1.1, 1.2, 1.3, 1.5**

**Property 2: Input mode exclusivity**
*For any* game state, normal player actions should be disabled when in building mode and enabled when in normal mode
**Validates: Requirements 1.4, 13.3, 13.4**

**Property 3: Material-gated placement**
*For any* fence placement attempt, placement should succeed if and only if sufficient materials are available
**Validates: Requirements 2.1, 2.4, 13.5**

**Property 4: Inventory conservation**
*For any* sequence of fence placement and removal operations, the total materials in inventory plus materials used in placed fences should remain constant
**Validates: Requirements 2.3, 3.1, 3.3, 11.4, 11.5**

**Property 5: Automatic piece selection correctness**
*For any* fence placement in a rectangular pattern, the system should select the correct piece type based on position in the 8-piece clockwise sequence
**Validates: Requirements 2.2, 4.1, 5.2, 14.2**

**Property 6: Connection consistency**
*For any* fence structure modification (placement or removal), adjacent pieces should maintain proper visual connections
**Validates: Requirements 2.5, 3.2, 4.2, 4.3**

**Property 7: Collision boundary synchronization**
*For any* fence structure change, collision boundaries should be updated immediately to match the current fence layout
**Validates: Requirements 10.3, 10.4, 10.5**

**Property 8: Rectangular enclosure completeness**
*For any* complete rectangular enclosure, the structure should use exactly 8 pieces in clockwise order with proper corner and edge piece types
**Validates: Requirements 4.3, 5.3, 5.4, 14.2**

**Property 9: Material calculation accuracy**
*For any* rectangular area definition, the calculated material requirements should match the actual pieces needed for construction
**Validates: Requirements 14.1, 14.5**

**Property 10: Inventory integration consistency**
*For any* fence material operation, the system should use standard inventory operations and trigger appropriate UI updates
**Validates: Requirements 12.1, 12.2, 12.4**

**Property 11: Multiplayer ownership enforcement**
*For any* fence removal attempt in multiplayer mode, removal should succeed if and only if the player owns the fence piece
**Validates: Requirements 3.5**

**Property 12: Resource collection integration**
*For any* resource harvesting operation (wood/bamboo), appropriate fence materials should be added to inventory
**Validates: Requirements 11.1, 11.2, 11.3**

## Error Handling

### Input Validation
- **Invalid Grid Positions**: Prevent placement outside valid grid boundaries
- **Insufficient Materials**: Block placement and show clear error messages
- **Collision Conflicts**: Prevent placement where collision boundaries would overlap incorrectly
- **Ownership Violations**: Block unauthorized removal in multiplayer mode

### Asset Management
- **Missing Textures**: Use placeholder textures and log warnings for missing fence piece assets
- **Texture Loading Failures**: Graceful degradation with colored rectangles as fallbacks
- **Memory Management**: Proper disposal of fence piece textures when structures are removed

### Network Error Handling
- **Connection Loss**: Queue fence operations for retry when connection is restored
- **Desynchronization**: Implement fence state reconciliation between clients
- **Concurrent Modifications**: Use timestamp-based conflict resolution for simultaneous edits

### Data Persistence Errors
- **Save Failures**: Retry save operations and notify player of persistence issues
- **Corrupted Data**: Validate fence data on load and remove invalid structures
- **Version Compatibility**: Handle fence data format changes between game versions

## Testing Strategy

### Unit Testing Approach
The system will use comprehensive unit tests to verify specific functionality:

- **Fence Piece Creation**: Test each of the 8 fence piece types for proper texture loading and positioning
- **Grid Calculations**: Verify world-to-grid and grid-to-world coordinate conversions
- **Material Calculations**: Test material requirement calculations for various rectangle sizes
- **Collision Boundary Generation**: Verify collision rectangles are generated correctly for each piece type
- **Inventory Integration**: Test material addition, consumption, and return operations
- **Network Message Serialization**: Verify fence network messages serialize and deserialize correctly

### Property-Based Testing Approach
The system will use property-based testing with **QuickCheck for Java** to verify universal properties across all valid inputs. Each property-based test will run a minimum of 100 iterations to ensure comprehensive coverage.

Property-based tests will focus on:

- **Invariant Properties**: Material conservation, UI state consistency, collision boundary integrity
- **Round-trip Properties**: Fence structure serialization/deserialization, coordinate conversions
- **Metamorphic Properties**: Relationship between rectangle size and material requirements
- **Error Condition Properties**: Proper handling of invalid inputs, insufficient materials, missing assets

Each property-based test will be tagged with comments explicitly referencing the correctness property from this design document using the format: **Feature: custom-fence-building, Property X: [property description]**

### Integration Testing
- **Multiplayer Synchronization**: Test fence building across multiple connected clients
- **World Persistence**: Verify fence structures save and load correctly
- **Collision System Integration**: Test fence collision with player movement and other game objects
- **Inventory System Integration**: Verify fence materials work with existing inventory mechanics

### Performance Testing
- **Large Fence Structures**: Test performance with hundreds of fence pieces
- **Collision Detection**: Verify collision performance with complex fence layouts
- **Network Bandwidth**: Test network message efficiency for large fence operations
- **Memory Usage**: Monitor memory consumption for fence texture management

## Implementation Notes

### Rendering Optimization
- **Texture Atlasing**: Combine all fence piece textures into a single atlas for efficient rendering
- **Batch Rendering**: Render all fence pieces in a single draw call using sprite batching
- **Culling**: Only render fence pieces visible in the current camera viewport
- **Level of Detail**: Use simplified rendering for distant fence structures

### Network Optimization
- **Message Batching**: Combine multiple fence operations into single network messages
- **Delta Compression**: Only send changes rather than full fence structure state
- **Priority Queuing**: Prioritize fence updates based on player proximity
- **Conflict Resolution**: Use deterministic algorithms for resolving concurrent modifications

### Memory Management
- **Texture Sharing**: Share fence piece textures between multiple instances
- **Lazy Loading**: Load fence textures only when needed
- **Garbage Collection**: Properly dispose of fence resources when structures are removed
- **Pooling**: Use object pools for frequently created/destroyed fence components

### Scalability Considerations
- **Grid Partitioning**: Divide large worlds into grid sectors for efficient fence management
- **Streaming**: Load/unload fence structures based on player proximity
- **Database Integration**: Store fence structures in database for persistent multiplayer worlds
- **Caching**: Cache frequently accessed fence structure data for performance