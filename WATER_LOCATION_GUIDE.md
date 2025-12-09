# Water Biome Location Guide

## Why You Haven't Seen Water Yet

The water biome system is working correctly, but water **only spawns beyond 1500 pixels from the spawn point (0,0)**. This is by design to ensure the spawn area remains safe and accessible.

## How Far Do You Need to Walk?

### Distance Reference
- **1500 pixels** = approximately 23-24 player character widths (player is 64x64 pixels)
- This is roughly **2-3 screen widths** of walking in one direction
- At normal walking speed, this takes about **1-2 minutes** of continuous walking

### Where to Find Water

Water appears in **lake-like clusters** beyond the 1500px exclusion zone. Here's where to look:

1. **Walk in any direction from spawn** for 2-3 minutes
2. **Look for blue areas** - water has a distinct deep blue color
3. **Water forms lakes** - you'll see contiguous blue regions, not scattered tiles
4. **Coverage**: About 16.5% of the world beyond 1500px is water

## Quick Test Coordinates

To quickly verify water is working, you can teleport or walk to these approximate areas:

### Guaranteed Water Zones (based on noise generation)
The water generation uses deterministic noise, so these general areas should have water:

- **North**: Around (0, 2500) and beyond
- **South**: Around (0, -2500) and beyond  
- **East**: Around (2500, 0) and beyond
- **West**: Around (-2500, 0) and beyond

## Visual Identification

When you find water, you'll know because:
- ✅ **Blue color** - distinct from green grass and beige sand
- ✅ **Cannot walk into it** - collision detection blocks movement
- ✅ **No trees or rocks** - resources don't spawn in water
- ✅ **Lake-like shapes** - contiguous regions, not scattered tiles

## Distribution Stats

Based on 50,000 sample measurements:
- **Grass**: 46.60% of the world
- **Sand**: 36.85% of the world
- **Water**: 16.54% of the world

## Exclusion Zones

For reference, here are the biome exclusion zones:
- **Water**: Does not spawn within 1500px of spawn (0,0)
- **Sand**: Does not spawn within 1000px of spawn (0,0)
- **Grass**: Always available at spawn

## Troubleshooting

If you still don't see water after walking 2000+ pixels from spawn:

1. **Check your distance**: Press F3 or check coordinates to verify you're beyond 1500px from (0,0)
2. **Try different directions**: Water is distributed randomly, so try walking in different directions
3. **Look for blue**: Water is very blue - if you see only green (grass) or beige (sand), keep walking
4. **Check the build**: Ensure you're running the latest build with water biome enabled

## Technical Details

The water generation uses:
- **Noise threshold**: 0.53 (in BiomeConfig.WATER_NOISE_THRESHOLD)
- **Noise scales**: Very low frequency (0.00005f, 0.0002f, 0.0008f) for large lake formations
- **Priority**: Water > Sand > Grass (water takes precedence when noise conditions are met)
- **Deterministic**: Same coordinates always produce the same biome (multiplayer sync)

## Recommendation

**Walk approximately 2500-3000 pixels from spawn in any direction** to reliably encounter water biomes. The further you go, the more likely you are to find water lakes.
