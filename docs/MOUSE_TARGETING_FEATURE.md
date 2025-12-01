# Mouse Targeting Feature

## Overview
Added mouse support to the targeting system, allowing players to use mouse movement and left-click in addition to keyboard controls (A/W/D/S and spacebar).

## Changes Made

### 1. TargetingSystem.java
**Location**: `src/main/java/wagemaker/uk/targeting/TargetingSystem.java`

#### Added:
- **Camera reference**: Added `OrthographicCamera camera` field to support screen-to-world coordinate conversion
- **setCamera() method**: Allows setting the camera reference for mouse input
- **setTargetFromMouse() method**: Converts mouse screen coordinates to world coordinates and updates target position
  - Automatically snaps to 64x64 tile grid
  - Enforces maximum range if configured
  - Validates target position after update

### 2. Player.java
**Location**: `src/main/java/wagemaker/uk/player/Player.java`

#### Modified:
- **Constructor**: Now sets camera reference on targeting system initialization
- **handleTargetingInput() method**: Enhanced to support mouse input
  - Mouse movement updates target cursor position in real-time
  - Left mouse click plants item at target location (same as spacebar/P key)
  - Keyboard controls (A/W/D/S) still work alongside mouse

## Usage

### Keyboard Controls (Original)
- **A/W/D/S**: Move targeting cursor by one tile
- **P or SPACEBAR**: Plant item at target location
- **ESC**: Cancel targeting

### Mouse Controls (New)
- **Mouse Movement**: Move targeting cursor to follow mouse position (snapped to tile grid)
- **Left Click**: Plant item at target location
- **ESC**: Cancel targeting

## Technical Details

### Coordinate Conversion
- Uses LibGDX's `camera.unproject()` to convert screen coordinates to world coordinates
- Screen Y is inverted (0 at top) vs world Y (0 at bottom), handled automatically by unproject()

### Tile Snapping
- All mouse positions are snapped to 64x64 tile grid using `snapToTileGrid()`
- Ensures consistent placement regardless of input method

### Range Enforcement
- If `maxRange` is configured, mouse targeting respects the same range limits as keyboard
- Positions beyond range are clamped to the boundary

### Validation
- Target validation occurs after every mouse movement
- Visual feedback (red/green indicator) updates in real-time

## Benefits

1. **Faster targeting**: Mouse allows direct positioning without multiple key presses
2. **More intuitive**: Point-and-click is familiar to most players
3. **Backward compatible**: Keyboard controls still work exactly as before
4. **Consistent behavior**: Mouse and keyboard share the same validation and range logic

## Testing Recommendations

1. Test mouse targeting with different camera zoom levels
2. Verify tile snapping works correctly at screen edges
3. Test left-click planting with valid and invalid targets
4. Confirm keyboard and mouse can be used interchangeably
5. Test with maximum range configured (if applicable)
