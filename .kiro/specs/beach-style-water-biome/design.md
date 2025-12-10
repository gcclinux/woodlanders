# Design Document: Beach-Style Water Biome System

## Overview

This design transforms the existing random water biome system into a realistic beach-style coastal environment. Water will only spawn within sand biome areas, maintaining a minimum 64-pixel buffer from grass areas to create natural beach transitions. The system redistributes biomes to 50% grass and 50% sand base areas, with 40% of sand areas containing water, resulting in approximately 50% grass, 30% sand, and 20% water coverage.

The implementation extends the existing biome architecture by adding a two-phase biome calculation: first determining base biome areas (grass vs sand), then applying water overlay within eligible sand zones. This approach maintains compatibility with existing systems while creating more realistic coastal environments.

## Architecture

### High-Level Components

The beach-style water system builds upon the existing biome architecture with enhanced calculation logic:

```
BiomeType (enum)
    ├── GRASS (50% base coverage)
    ├── SAND (50% base coverage)  
    └── WATER (40% of sand areas)

BiomeManager (enhanced)
    ├── getBiomeAtPosition() - Two-phase calculation (base + water overlay)
    ├── getBaseBiomeAtPosition() - New method for grass/sand determination
    ├── isEligibleForWater() - New method checking sand area + buffer requirements
    ├── isInWaterPatch() - Enhanced to only check within sand areas
    └── calculateDistanceToGrass() - New method for buffer zone validation

BiomeConfig (extended)
    ├── SAND_BASE_THRESHOLD - New threshold for 50/50 grass/sand split
    ├── WATER_IN_SAND_THRESHOLD - New threshold for 40% water coverage in sand
    ├── BEACH_BUFFER_DISTANCE - 128px minimum distance from grass
    └── Existing water texture and color configurations

BiomeTextureGenerator
    └── generateWaterTexture() - Unchanged, continues to generate blue water textures

Player/ResourceSpawning/Puddles
    └── Existing collision and spawn logic unchanged - water is still water regardless of location
```

### Two-Phase Biome Calculation

The new system uses a two-phase approach:

1. **Phase 1: Base Biome Determination**
   - Calculate grass vs sand using new 50/50 distribution
   - Use noise-based generation with adjusted thresholds

2. **Phase 2: Water Overlay Application**
   - Only consider sand areas from Phase 1
   - Apply buffer zone validation (128px from grass)
   - Use secondary noise for water placement within eligible sand areas

## Components and Interfaces

### 1. BiomeConfig Extensions

Add beach-specific configuration constants:

```java
public class BiomeConfig {
    // Existing constants...
    
    // ========== BEACH-STYLE BIOME CONFIGURATION ==========
    
    /**
     * Threshold for base biome determination (grass vs sand).
     * Adjusted to achieve 50% grass, 50% sand base distribution.
     * 
     * Gameplay impact: High
     * Target: 50/50 split between grass and sand base areas
     * Default: 0.5 (noise values > 0.5 = sand, <= 0.5 = grass)
     */
    public static final float SAND_BASE_THRESHOLD = 0.5f;
    
    /**
     * Threshold for water placement within sand areas.
     * Controls what percentage of sand areas contain water.
     * 
     * Gameplay impact: High
     * Target: 40% of sand areas contain water
     * Default: 0.6 (noise values > 0.6 = water within sand)
     */
    public static final float WATER_IN_SAND_THRESHOLD = 0.6f;
    
    /**
     * Minimum distance in pixels between water and grass biomes.
     * Creates realistic beach buffer zones.
     * 
     * Gameplay impact: High
     * Visual impact: High
     * Default: 64.0f (1 block for better water contiguity)
     */
    public static final float BEACH_BUFFER_DISTANCE = 128.0f;
    
    /**
     * Scale factor for base biome noise generation.
     * Controls the size of grass vs sand regions.
     * 
     * Performance impact: Low
     * Visual impact: Medium
     * Default: 0.0003f (creates large biome regions)
     */
    public static final float BASE_BIOME_NOISE_SCALE = 0.0003f;
    
    /**
     * Scale factor for water-in-sand noise generation.
     * Controls the size of water areas within sand biomes.
     * 
     * Performance impact: Low
     * Visual impact: Medium
     * Default: 0.0008f (creates medium-sized water areas)
     */
    public static final float WATER_NOISE_SCALE = 0.0008f;
    
    /**
     * Random seed for base biome (grass/sand) generation.
     * Using a fixed seed ensures consistent base biome layout.
     * 
     * Visual impact: High
     * Default: 11111 (different from existing seeds to avoid conflicts)
     */
    public static final int BASE_BIOME_SEED = 11111;
    
    /**
     * Random seed for water-in-sand generation.
     * Using a fixed seed ensures consistent water placement within sand.
     * 
     * Visual impact: High
     * Default: 22222 (different from base biome seed)
     */
    public static final int WATER_IN_SAND_SEED = 22222;
    
    // Keep existing water texture seeds and colors unchanged
    // public static final int TEXTURE_SEED_WATER = 98765;
    // public static final float[] WATER_BASE_COLOR = {0.1f, 0.3f, 0.6f, 1.0f};
    // ... etc
}
```

### 2. BiomeManager Enhancement

Completely redesign the biome calculation logic for beach-style generation:

```java
public class BiomeManager {
    // Existing fields...
    private SimplexNoise baseBiomeNoise;  // For grass/sand determination
    private SimplexNoise waterInSandNoise; // For water placement in sand
    
    /**
     * Initialize noise generators for two-phase biome calculation.
     */
    public void initialize() {
        // Existing initialization...
        
        // Initialize new noise generators
        baseBiomeNoise = new SimplexNoise(BiomeConfig.BASE_BIOME_SEED);
        waterInSandNoise = new SimplexNoise(BiomeConfig.WATER_IN_SAND_SEED);
        
        // Generate and cache textures
        generateAndCacheTextures();
    }
    
    /**
     * Two-phase biome determination: base biome + water overlay.
     * Phase 1: Determine if location is grass or sand base
     * Phase 2: If sand, check if water should be placed there
     */
    public BiomeType getBiomeAtPosition(float worldX, float worldY) {
        // Phase 1: Determine base biome (grass or sand)
        BiomeType baseBiome = getBaseBiomeAtPosition(worldX, worldY);
        
        // If base is grass, water cannot spawn there
        if (baseBiome == BiomeType.GRASS) {
            return BiomeType.GRASS;
        }
        
        // Phase 2: Check if water should spawn in this sand area
        if (baseBiome == BiomeType.SAND && isEligibleForWater(worldX, worldY)) {
            return BiomeType.WATER;
        }
        
        // Default to sand (base biome)
        return BiomeType.SAND;
    }
    
    /**
     * Determines base biome type (grass or sand) using 50/50 distribution.
     * This replaces the old grass/sand/water random distribution.
     */
    private BiomeType getBaseBiomeAtPosition(float worldX, float worldY) {
        float distance = calculateDistanceFromSpawn(worldX, worldY);
        
        // Inner grass zone (unchanged from existing system)
        if (distance < BiomeConfig.INNER_GRASS_RADIUS) {
            return BiomeType.GRASS;
        }
        
        // Sand zone (unchanged from existing system)
        if (distance < BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH) {
            return BiomeType.SAND;
        }
        
        // Beyond sand zone: use noise for 50/50 grass/sand distribution
        float noiseValue = baseBiomeNoise.noise(
            worldX * BiomeConfig.BASE_BIOME_NOISE_SCALE,
            worldY * BiomeConfig.BASE_BIOME_NOISE_SCALE
        );
        
        // Normalize noise to 0-1 range
        float normalizedNoise = noiseValue * 0.5f + 0.5f;
        
        // 50/50 split: > 0.5 = sand, <= 0.5 = grass
        return normalizedNoise > BiomeConfig.SAND_BASE_THRESHOLD ? 
               BiomeType.SAND : BiomeType.GRASS;
    }
    
    /**
     * Checks if a sand location is eligible for water placement.
     * Requirements:
     * 1. Location must be in a sand base biome
     * 2. Must be at least 128px away from any grass area
     * 3. Must pass the water-in-sand noise threshold (40% coverage)
     */
    private boolean isEligibleForWater(float worldX, float worldY) {
        // Requirement 1: Must be in sand base biome (already checked by caller)
        
        // Requirement 2: Check buffer distance from grass
        if (!isValidBeachBuffer(worldX, worldY)) {
            return false;
        }
        
        // Requirement 3: Check water-in-sand noise threshold
        float waterNoise = waterInSandNoise.noise(
            worldX * BiomeConfig.WATER_NOISE_SCALE,
            worldY * BiomeConfig.WATER_NOISE_SCALE
        );
        
        // Normalize to 0-1 range
        float normalizedWaterNoise = waterNoise * 0.5f + 0.5f;
        
        // 40% of sand areas should have water
        return normalizedWaterNoise > BiomeConfig.WATER_IN_SAND_THRESHOLD;
    }
    
    /**
     * Validates that a location maintains the required buffer distance from grass.
     * Checks a 64-pixel radius around the location for any grass biomes.
     */
    private boolean isValidBeachBuffer(float worldX, float worldY) {
        float bufferDistance = BiomeConfig.BEACH_BUFFER_DISTANCE;
        int samplePoints = 16; // Check 16 points around the circle
        
        for (int i = 0; i < samplePoints; i++) {
            float angle = (float) (2 * Math.PI * i / samplePoints);
            float checkX = worldX + bufferDistance * (float) Math.cos(angle);
            float checkY = worldY + bufferDistance * (float) Math.sin(angle);
            
            // If any point in the buffer zone is grass, reject this location
            if (getBaseBiomeAtPosition(checkX, checkY) == BiomeType.GRASS) {
                return false;
            }
        }
        
        return true; // No grass found in buffer zone
    }
    
    /**
     * Calculates the minimum distance from a point to the nearest grass biome.
     * Used for debugging and validation purposes.
     */
    private float calculateDistanceToGrass(float worldX, float worldY) {
        float searchRadius = BiomeConfig.BEACH_BUFFER_DISTANCE * 2; // Search wider area
        float minDistance = Float.MAX_VALUE;
        int sampleResolution = 32; // Check every 32 pixels
        
        for (float dx = -searchRadius; dx <= searchRadius; dx += sampleResolution) {
            for (float dy = -searchRadius; dy <= searchRadius; dy += sampleResolution) {
                float checkX = worldX + dx;
                float checkY = worldY + dy;
                
                if (getBaseBiomeAtPosition(checkX, checkY) == BiomeType.GRASS) {
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
                    minDistance = Math.min(minDistance, distance);
                }
            }
        }
        
        return minDistance;
    }
    
    // Keep existing methods unchanged:
    // - generateAndCacheTextures()
    // - calculateDistanceFromSpawn()
    // - getTextureForBiome()
    // etc.
}
```

### 3. Backward Compatibility

Ensure existing systems continue to work without modification:

```java
// Player collision - no changes needed
public class Player {
    private boolean wouldCollide(float newX, float newY) {
        // Existing collision logic unchanged
        // Water is still water, regardless of being in sand areas
        BiomeType biomeAtPosition = biomeManager.getBiomeAtPosition(
            playerCenterX, playerCenterY
        );
        
        if (biomeAtPosition == BiomeType.WATER) {
            return true; // Block movement into water
        }
        
        // ... rest of collision logic unchanged
    }
}

// Resource spawning - no changes needed
private boolean isValidSpawnLocation(float x, float y) {
    // Existing spawn validation unchanged
    // Water areas are still invalid for spawning
    BiomeType biomeType = biomeManager.getBiomeAtPosition(x, y);
    if (biomeType == BiomeType.WATER) {
        return false; // Don't spawn in water
    }
    
    // ... rest of validation logic unchanged
}

// Puddle system - no changes needed
public void createPuddle(float x, float y) {
    // Existing puddle logic unchanged
    // Water areas still prevent puddle creation
    BiomeType biomeType = biomeManager.getBiomeAtPosition(x, y);
    if (biomeType == BiomeType.WATER) {
        return; // Don't create puddles in water
    }
    
    // ... rest of puddle logic unchanged
}
```

## Data Models

### BiomeType Enum

```java
public enum BiomeType {
    GRASS,   // 50% base coverage - default terrain and inner spawn area
    SAND,    // 50% base coverage - beach and desert areas (30% final after water overlay)
    WATER;   // 40% of sand areas - beach water only (20% final coverage)
}
```

### BiomeDistribution

The new beach-style generation system achieves the following distribution:

**Base Biome Distribution (Phase 1):**
- **Grass**: 50% of world (includes inner spawn area + noise-based areas)
- **Sand**: 50% of world (includes sand zone + noise-based areas)

**Final Distribution (After Water Overlay):**
- **Grass**: 50% of world (unchanged from base)
- **Sand**: 30% of world (60% of original sand areas remain sand)
- **Water**: 20% of world (40% of original sand areas become water)

**Spatial Constraints:**
- Water only appears within sand base areas
- Minimum 128px buffer between water and grass
- Contiguous beach-like water regions within sand zones

## 
Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

After analyzing the acceptance criteria and eliminating redundancy, the following properties provide comprehensive coverage:

**Property 1: Water only spawns in sand areas**
*For any* world coordinate where getBiomeAtPosition returns WATER, the base biome at that coordinate should be SAND
**Validates: Requirements 1.1**

**Property 2: Biome type exhaustiveness**
*For any* world coordinate, getBiomeAtPosition should return exactly one of the three valid BiomeType enum values (GRASS, SAND, or WATER)
**Validates: Requirements 1.2**

**Property 3: Water maintains buffer distance from grass**
*For any* world coordinate where getBiomeAtPosition returns WATER, all coordinates within a 64-pixel radius should not have base biome type GRASS
**Validates: Requirements 1.3**

**Property 4: Water coverage in sand areas**
*For any* large sample of sand base biome coordinates (n > 1000), approximately 40% should contain water biomes (within ±5% tolerance)
**Validates: Requirements 1.4**

**Property 5: Water contiguity in sand**
*For any* water biome coordinate, at least 25% of coordinates within a 200-pixel radius that are also in sand base areas should be water biomes
**Validates: Requirements 1.5**

**Property 6: Base biome distribution**
*For any* large sample of world coordinates outside spawn zones (n > 10000), approximately 50% should be grass base biome and 50% should be sand base biome (within ±5% tolerance)
**Validates: Requirements 2.1**

**Property 7: Final biome distribution**
*For any* large sample of world coordinates (n > 10000), the final distribution should be approximately 50% grass, 30% sand, and 20% water (within ±5% tolerance)
**Validates: Requirements 2.3**

**Property 8: Water collision blocking**
*For any* world coordinate where getBiomeAtPosition returns WATER, attempting to move the player to that coordinate should result in wouldCollide returning true
**Validates: Requirements 4.1**

**Property 9: Resource spawn exclusion from water**
*For any* spawned resource (tree, rock, item), the biome type at the spawn coordinate should not be WATER
**Validates: Requirements 4.2**

**Property 10: Puddle exclusion from water**
*For any* created puddle, the biome type at the puddle coordinate should not be WATER
**Validates: Requirements 4.3**

**Property 11: Biome calculation determinism**
*For any* world coordinate, calling getBiomeAtPosition multiple times with the same coordinates should always return the identical BiomeType value
**Validates: Requirements 4.4**

## Error Handling

### Invalid Buffer Zone Calculations

- **Scenario**: Buffer zone validation encounters edge cases at world boundaries
- **Handling**: Treat out-of-bounds coordinates as grass for conservative water placement
- **Recovery**: Water placement becomes more restrictive near world edges, ensuring safety

### Noise Generation Failures

- **Scenario**: Simplex noise generators fail to initialize or return invalid values
- **Handling**: Fall back to deterministic grid-based patterns using coordinate hashing
- **Recovery**: Biome generation continues with less organic but still functional patterns

### Base Biome Calculation Inconsistencies

- **Scenario**: Base biome and final biome calculations produce conflicting results
- **Handling**: Log warning and prioritize base biome calculation for consistency
- **Recovery**: System maintains deterministic behavior, water placement may be reduced

### Performance Degradation

- **Scenario**: Buffer zone validation becomes too expensive for real-time calculation
- **Handling**: Implement spatial caching for frequently queried areas
- **Recovery**: Cache biome results for 256x256 pixel chunks, refresh as needed

## Testing Strategy

### Unit Testing

Unit tests will verify specific examples and integration points:

1. **BiomeConfig Test**: Verify new beach-specific constants are defined and within valid ranges
2. **Base Biome Calculation Test**: Verify getBaseBiomeAtPosition() returns correct grass/sand distribution for known coordinates
3. **Buffer Zone Validation Test**: Verify isValidBeachBuffer() correctly identifies valid/invalid locations
4. **Two-Phase Integration Test**: Verify getBiomeAtPosition() correctly combines base biome + water overlay
5. **Backward Compatibility Test**: Verify existing collision, spawning, and texture systems work unchanged

### Property-Based Testing

Property-based tests will verify universal properties across random inputs using JUnit-Quickcheck:

1. **Property Test 1: Water only spawns in sand areas** (Property 1)
   - Generate random water coordinates
   - Verify base biome at each coordinate is SAND
   - Run 100+ iterations

2. **Property Test 2: Biome type exhaustiveness** (Property 2)
   - Generate random world coordinates
   - Verify getBiomeAtPosition returns GRASS, SAND, or WATER
   - Run 100+ iterations

3. **Property Test 3: Water maintains buffer distance from grass** (Property 3)
   - Generate random water coordinates
   - Sample 16 points in 64px radius around each
   - Verify no sampled points have base biome GRASS
   - Run 100+ iterations

4. **Property Test 4: Water coverage in sand areas** (Property 4)
   - Generate 1000+ random sand base biome coordinates
   - Count how many contain water
   - Verify 35-45% contain water (40% ±5%)
   - Run 10+ iterations with different coordinate sets

5. **Property Test 5: Water contiguity in sand** (Property 5)
   - Generate random water coordinates
   - Sample coordinates within 200px radius that are in sand base areas
   - Verify ≥30% of sand-area samples are also water
   - Run 100+ iterations

6. **Property Test 6: Base biome distribution** (Property 6)
   - Generate 10,000+ random coordinates outside spawn zones
   - Count grass vs sand base biomes
   - Verify 45-55% grass, 45-55% sand
   - Run 10+ iterations with different coordinate sets

7. **Property Test 7: Final biome distribution** (Property 7)
   - Generate 10,000+ random coordinates
   - Count final biome types (grass, sand, water)
   - Verify 45-55% grass, 25-35% sand, 15-25% water
   - Run 10+ iterations with different coordinate sets

8. **Property Test 8: Water collision blocking** (Property 8)
   - Generate random water coordinates
   - Verify wouldCollide(waterX, waterY) returns true
   - Run 100+ iterations

9. **Property Test 9: Resource spawn exclusion from water** (Property 9)
   - Generate random resource spawn coordinates
   - Verify none have BiomeType.WATER
   - Run 100+ iterations across all resource types

10. **Property Test 10: Puddle exclusion from water** (Property 10)
    - Generate random puddle coordinates
    - Verify none have BiomeType.WATER
    - Run 100+ iterations

11. **Property Test 11: Biome calculation determinism** (Property 11)
    - Generate random coordinates
    - Call getBiomeAtPosition() 10 times for each coordinate
    - Verify all 10 calls return identical BiomeType
    - Run 100+ iterations

### Integration Testing

Integration tests will verify the complete feature in realistic scenarios:

1. **Beach Formation Test**: Verify water forms realistic coastlines within sand areas over large map sections
2. **Buffer Zone Test**: Verify no water appears within 64px of grass over 10,000 sample points
3. **Distribution Convergence Test**: Verify biome percentages converge to targets over increasingly large sample sizes
4. **Multiplayer Sync Test**: Verify two clients see identical beach layouts
5. **Performance Test**: Verify biome calculation performance remains acceptable with buffer zone validation

### Testing Framework

- **Unit Tests**: JUnit 5
- **Property-Based Tests**: JUnit-Quickcheck (https://pholser.github.io/junit-quickcheck/)
- **Performance Tests**: JMH (Java Microbenchmark Harness)
- **Test Coverage Target**: 85%+ line coverage for new code

## Implementation Notes

### Biome Distribution Tuning

The beach-style distribution will be achieved through:

1. **Base Biome Thresholds**:
   - Sand base threshold: 0.5 (50/50 split between grass and sand)
   - Base biome noise scale: 0.0003f (large regions)

2. **Water Overlay Thresholds**:
   - Water in sand threshold: 0.6 (40% of sand areas get water)
   - Water noise scale: 0.0008f (medium-sized water areas)

3. **Buffer Zone Validation**:
   - Buffer distance: 64.0f pixels (1 block)
   - Sample points: 16 around circumference for validation

These values may require fine-tuning during implementation based on actual distribution measurements and visual quality assessment.

### Performance Considerations

- **Biome Lookup**: Two-phase calculation adds ~50% overhead but remains O(1)
- **Buffer Validation**: O(16) additional noise samples per water location
- **Caching Strategy**: Consider caching buffer validation results for frequently accessed areas
- **Memory**: No additional texture memory required, reuses existing water textures

### Spatial Optimization

To optimize buffer zone validation:

1. **Coarse-to-Fine**: Check 4 cardinal directions first, then full circle if needed
2. **Early Exit**: Stop checking once grass is found in buffer zone
3. **Spatial Hashing**: Cache buffer validation results for 64x64 pixel grid cells
4. **Lazy Evaluation**: Only validate buffer when water noise threshold is exceeded

### Migration Strategy

1. **Phase 1**: Add new BiomeConfig constants and noise generators
2. **Phase 2**: Implement getBaseBiomeAtPosition() method
3. **Phase 3**: Implement buffer zone validation logic
4. **Phase 4**: Replace getBiomeAtPosition() with two-phase calculation
5. **Phase 5**: Testing and threshold tuning
6. **Phase 6**: Performance optimization and caching

Existing saves will automatically see the new beach-style layout without requiring data migration, as biomes are calculated on-demand from coordinates.

## Future Enhancements

Potential future improvements (out of scope for this design):

- **Tidal Zones**: Water levels that change over time within beach areas
- **Beach Resources**: Special items that spawn only in sand areas near water
- **Coastal Erosion**: Dynamic beach boundaries that shift over long time periods
- **River Systems**: Flowing water that connects beach areas across sand regions
- **Beach Structures**: Docks, piers, or other coastal buildings
- **Marine Life**: Fish or other creatures that appear in beach water areas
- **Weather Effects**: Storms that affect beach areas differently than inland regions
- **Sand Castles**: Player-buildable structures unique to beach environments