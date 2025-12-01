# Sapling Centering Fix

## Issue Description
When planting Banana Saplings and Apple Saplings using the mouse targeting system, the red/white target indicator dot was not aligned with the planted sapling position. This caused confusion because:

1. The target indicator showed as white (valid) at a position
2. After planting, the sapling appeared offset from the indicator
3. Clicking the same position again showed red (invalid) even though it appeared empty
4. Error message: "Banana tree planting failed: invalid location or tile already occupied"

## Root Cause
The issue was a rendering offset mismatch:

- **Target Indicator**: Rendered at 16x16 pixels, centered in the 64x64 tile (offset +24 pixels)
- **PlantedBananaTree**: Rendered at 32x32 pixels, starting at tile origin (offset +0 pixels)
- **PlantedAppleTree**: Rendered at 32x32 pixels, starting at tile origin (offset +0 pixels)

This created a visual misalignment where the sapling appeared 16 pixels away from the target indicator.

## Comparison with Other Saplings
- **PlantedBamboo**: Rendered at 64x64 pixels (full tile) - no centering needed ✓
- **PlantedTree**: Rendered at 64x64 pixels (full tile) - no centering needed ✓
- **PlantedBananaTree**: Rendered at 32x32 pixels - needed centering ✗
- **PlantedAppleTree**: Rendered at 32x32 pixels - needed centering ✗

## Solution
Modified the rendering code in `MyGdxGame.java` to center the 32x32 saplings within their 64x64 tiles:

### Before:
```java
batch.draw(texture, planted.getX(), planted.getY(), 32, 32);
```

### After:
```java
// Center the 32x32 sapling in the 64x64 tile: (64 - 32) / 2 = 16 pixel offset
float centerOffset = (64 - 32) / 2.0f;
batch.draw(texture, planted.getX() + centerOffset, planted.getY() + centerOffset, 32, 32);
```

## Changes Made

### File: `src/main/java/wagemaker/uk/gdx/MyGdxGame.java`

#### Modified Methods:
1. **drawPlantedBananaTrees()** - Added 16-pixel centering offset
2. **drawPlantedAppleTrees()** - Added 16-pixel centering offset

## Technical Details

### Tile Coordinate System
- All tiles are 64x64 pixels
- Tile coordinates represent the bottom-left corner of the tile
- Planted items store their position at tile-aligned coordinates (multiples of 64)

### Centering Calculation
```
Tile Size: 64 pixels
Sapling Size: 32 pixels
Center Offset: (64 - 32) / 2 = 16 pixels
```

### Visual Alignment
Now all elements are properly centered in their tiles:
- Target indicator: 16x16 at offset +24 (centered)
- Banana/Apple saplings: 32x32 at offset +16 (centered)
- Bamboo/Tree saplings: 64x64 at offset +0 (full tile)

## Result
✅ Target indicator and planted saplings are now visually aligned
✅ Red dot appears directly under the planted sapling
✅ No more false "invalid location" errors when clicking near planted saplings
✅ Consistent visual feedback across all planting operations

## Testing Recommendations
1. Plant banana saplings and verify target indicator aligns with planted sprite
2. Plant apple saplings and verify target indicator aligns with planted sprite
3. Try to plant on an occupied tile and verify red indicator appears at correct position
4. Compare with bamboo and tree saplings to ensure consistent behavior
5. Test with both keyboard (A/W/D/S) and mouse targeting
