# Targeting Validation Debug

## Issue Description
After planting saplings, the targeting system shows different validation behavior:

- **PlantedTree & PlantedBamboo**: Target shows RED (invalid) when hovering over occupied tile ✓ CORRECT
- **PlantedBananaTree & PlantedAppleTree**: Target shows WHITE (valid) even after planting ✗ INCORRECT

## Changes Made

### 1. Added updateTargetingValidator() calls
**File**: `src/main/java/wagemaker/uk/player/Player.java`

Added calls to `updateTargetingValidator()` in:
- `setPlantedBananaTrees()` method
- `setPlantedAppleTrees()` method

This ensures the validator is updated when these maps are set.

### 2. Added Debug Logging
**File**: `src/main/java/wagemaker/uk/targeting/PlantingTargetValidator.java`

Added extensive debug logging in `isTileOccupied()` method to track:
- When tiles are detected as occupied
- Map sizes for plantedBananaTrees and plantedAppleTrees
- Keys being searched for
- Keys actually present in the maps
- Proximity check results

## Debug Output to Watch For

When testing, look for these console messages:

```
[VALIDATOR] Tile occupied by planted bamboo at: planted-bamboo-X-Y
[VALIDATOR] Tile occupied by planted tree at: planted-tree-X-Y
[VALIDATOR] Tile occupied by planted banana tree at: planted-banana-tree-X-Y
[VALIDATOR] Tile occupied by planted apple tree at: planted-apple-tree-X-Y
[VALIDATOR] Checking tile at (X, Y)
[VALIDATOR] - plantedBananaTrees map size: N
[VALIDATOR] - plantedAppleTrees map size: N
[VALIDATOR] - Looking for banana key: planted-banana-tree-X-Y
[VALIDATOR] - Looking for apple key: planted-apple-tree-X-Y
[VALIDATOR] - Banana tree keys in map: [key1, key2, ...]
[VALIDATOR] - Apple tree keys in map: [key1, key2, ...]
[VALIDATOR] Tile occupied by nearby tree (proximity check)
[VALIDATOR] Tile is NOT occupied - valid for planting
```

## Testing Steps

1. **Test Banana Sapling**:
   - Select banana sapling (slot 9)
   - Plant it on grass
   - Move mouse back over the planted sapling
   - **Expected**: Target should show RED
   - **Check console**: Should see "Tile occupied by planted banana tree"

2. **Test Apple Sapling**:
   - Select apple sapling (slot 8)
   - Plant it on grass
   - Move mouse back over the planted sapling
   - **Expected**: Target should show RED
   - **Check console**: Should see "Tile occupied by planted apple tree"

3. **Compare with Bamboo**:
   - Select bamboo sapling (slot 2)
   - Plant it on sand
   - Move mouse back over the planted bamboo
   - **Expected**: Target should show RED (this already works)
   - **Check console**: Should see "Tile occupied by planted bamboo"

4. **Compare with Tree**:
   - Select tree sapling (slot 4)
   - Plant it on grass
   - Move mouse back over the planted tree
   - **Expected**: Target should show RED (this already works)
   - **Check console**: Should see "Tile occupied by planted tree"

## Possible Root Causes

If the debug output shows the maps are empty or don't contain the expected keys:

1. **Map Reference Issue**: The validator might not have the correct map reference
2. **Key Mismatch**: The key format used for planting might differ from validation
3. **Timing Issue**: The validator might be checking before the map is updated
4. **Null Map**: The plantedBananaTrees or plantedAppleTrees maps might be null

If the debug output shows the correct keys but validation still fails:

1. **Coordinate Snapping**: The coordinates might not be snapping to the same grid
2. **Float Precision**: Float-to-int conversion might cause key mismatches
3. **Validator Recreation**: The validator might be recreated without the fruit tree maps

## Next Steps

After running the game with debug logging:

1. Plant a banana sapling and note the console output
2. Move mouse over the planted sapling and note the validation output
3. Compare the keys - they should match exactly
4. If keys don't match, investigate coordinate snapping in PlantingSystem vs PlantingTargetValidator
5. If maps are null/empty, investigate when setPlantedFruitTreeMaps() is called

## Cleanup

Once the issue is identified and fixed, remove the debug logging from `PlantingTargetValidator.java` to reduce console noise.
