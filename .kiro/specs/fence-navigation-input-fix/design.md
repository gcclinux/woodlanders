# Fence Navigation Input Fix Design

## Overview

This design addresses critical input handling issues in the fence building system where LEFT/RIGHT arrow keys continue to move the player instead of selecting fence pieces when fence navigation mode is active. The solution involves restructuring input processing order, implementing proper input isolation, and ensuring stable state management.

## Architecture

The fix involves three main components:

1. **Input Processing Pipeline**: Restructured to prioritize navigation mode input over player movement
2. **State Management System**: Improved fence navigation mode lifecycle management
3. **Input Isolation Layer**: Prevents conflicting input interpretations between different modes

## Components and Interfaces

### Input Processing Pipeline

```java
// New input processing order in Player.update()
1. Handle special navigation modes (fence, inventory)
2. Process targeting system input (A/W/D/S when targeting active)
3. Process normal player movement (only when no special modes active)
4. Handle context-sensitive actions (spacebar)
```

### State Management System

```java
// Enhanced fence navigation state tracking
private boolean fenceNavigationMode = false;
private boolean fenceNavigationStable = false; // Prevents immediate deactivation
private int framesSinceFenceActivation = 0; // Frame counter for stability
```

### Input Isolation Layer

```java
// Centralized input mode detection
private boolean isSpecialNavigationActive() {
    return inventoryNavigationMode || fenceNavigationMode;
}

private boolean shouldBlockPlayerMovement() {
    return isSpecialNavigationActive() || targetingSystem.isActive();
}
```

## Data Models

### Navigation Mode State

```java
public enum NavigationMode {
    NORMAL,           // Standard player movement
    INVENTORY,        // Inventory selection mode
    FENCE_BUILDING,   // Fence piece selection mode
    TARGETING         // Targeting cursor active
}

private NavigationMode currentNavigationMode = NavigationMode.NORMAL;
```

### Input Event Priority

```java
public enum InputPriority {
    HIGH(1),     // Special navigation modes (fence, inventory)
    MEDIUM(2),   // Targeting system
    LOW(3);      // Normal player movement
    
    private final int level;
}
```

## Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Input Isolation
*For any* active fence navigation mode, LEFT/RIGHT arrow key presses should select fence pieces and not move the player
**Validates: Requirements 1.1, 1.2, 1.3**

### Property 2: State Stability
*For any* fence navigation mode activation, the mode should remain active for at least one complete frame before allowing deactivation
**Validates: Requirements 2.1, 2.2, 2.3**

### Property 3: Targeting Control
*For any* active fence navigation mode with targeting enabled, A/W/D/S key presses should move the targeting cursor and not move the player
**Validates: Requirements 3.1, 3.2**

### Property 4: Mode Exclusivity
*For any* active special navigation mode, other navigation modes should be blocked from activation
**Validates: Requirements 5.2, 5.3**

### Property 5: Movement Restoration
*For any* deactivation of all special navigation modes, normal player movement should be restored
**Validates: Requirements 1.5, 5.5**

## Error Handling

### Input Conflict Resolution

1. **Priority-Based Processing**: Higher priority input modes override lower priority ones
2. **State Validation**: Verify navigation mode state before processing input
3. **Graceful Degradation**: Fall back to normal movement if navigation modes fail

### State Corruption Prevention

1. **Frame-Based Stability**: Prevent state changes within the same frame
2. **Validation Checks**: Verify state consistency before mode transitions
3. **Recovery Mechanisms**: Reset to normal mode if invalid states detected

## Testing Strategy

### Unit Testing

- Test input processing order with different navigation modes active
- Verify state transitions between navigation modes
- Test input isolation for each navigation mode type
- Validate targeting system integration with fence navigation

### Property-Based Testing

- Generate random sequences of key presses and verify correct input routing
- Test navigation mode stability across multiple frame updates
- Verify input isolation properties hold across all possible input combinations
- Test state transition properties with random mode activation/deactivation sequences

The testing strategy uses **fast-check** for JavaScript/TypeScript property-based testing, configured to run a minimum of 100 iterations per property test. Each property-based test will be tagged with comments referencing the specific correctness property from this design document.

## Implementation Plan

### Phase 1: Input Processing Restructure

1. Move fence navigation input handling before player movement processing
2. Implement input isolation checks in movement logic
3. Add frame-based stability for fence navigation mode

### Phase 2: State Management Enhancement

1. Implement NavigationMode enum and state tracking
2. Add validation checks for mode transitions
3. Implement priority-based input processing

### Phase 3: Integration and Testing

1. Integrate enhanced state management with existing systems
2. Add comprehensive logging for debugging
3. Implement property-based tests for input handling

### Phase 4: Visual Feedback Improvements

1. Ensure fence piece selection panel shows immediately when mode activates
2. Add clear visual indicators for active navigation mode
3. Implement smooth transitions between different input modes