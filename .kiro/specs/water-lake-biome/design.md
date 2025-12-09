# Design Document: Water/Lake Biome System

## Overview

This design introduces a third biome type (WATER) to the existing grass and sand biome system. Water biomes will appear as realistic blue lake areas distributed throughout the game world using the existing noise-based generation approach. The system includes collision detection to prevent player movement into water, spawn restrictions for resources, and deterministic generation for multiplayer synchronization.

The implementation extends the existing biome architecture without breaking current functionality, maintaining the same noise-based generation patterns and texture management approach used for grass and sand biomes.

## Architecture

### High-Level Components

The water biome feature integrates into the existing biome system architecture:

```
BiomeType (enum)
    ├── GRASS
    ├── SAND
    └── WATER (new)

BiomeManager
    ├── getBiomeAtPosition() - Extended to return WATER
    ├── isInWaterPatch() - New method for water detection
    └── generateWaterTexture() - Delegates to BiomeTextureGenerator

BiomeTextureGenerator
    └── generateWaterTexture() - New method for water texture

Player
    └── wouldCollide() - Extended to check water biomes

ResourceSpawning (various classes)
    └── spawn validation - Extended to reject water locations
```

### Integration Points

1. **BiomeType Enumeration**: Add WATER constant
2. **BiomeManager**: Extend biome calculation logic to include water patches
3. **BiomeTextureGenerator**: Add water texture generation
4. **BiomeConfig**: Add water-specific configuration parameters
5. **Player Collision**: Extend wouldCollide() to check biome type
6. **Resource Spawning**: Add biome validation before spawning resources
7. **Puddle System**: Prevent puddle creation in water biomes

## Components and Interfaces

### 1. BiomeType Extension

```java
public enum BiomeType {
    GRASS,
    SAND,
    WATER;  // New biome type
    
    public String getDisplayName() {
        return name().toLowerCase();
    }
}
```

### 2. BiomeConfig Extensions

Add water-specific configuration constants:

```java
public class BiomeConfig {
    // Existing constants...
    
    // ========== WATER BIOME CONFIGURATION ==========
    
    /**
     * Random seed for water texture generation.
     * Using a fixed seed ensures consistent water appearance.
     */
    public static final int TEXTURE_SEED_WATER = 98765;
    
    /**
     * Base water color (deep blue) - RGBA components.
     */
    public static final float[] WATER_BASE_COLOR = {0.1f, 0.3f, 0.6f, 1.0f};
    
    /**
     * Light water color (surface reflections) - RGBA components.
     */
    public static final float[] WATER_LIGHT_COLOR = {0.3f, 0.5f, 0.8f, 1.0f};
    
    /**
     * Dark water color (depth/shadows) - RGBA components.
     */
    public static final float[] WATER_DARK_COLOR = {0.05f, 0.2f, 0.4f, 1.0f};
    
    /**
     * Threshold for water generation in noise-based system.
     * Lower values = more water coverage.
     * Target: ~15% water coverage
     */
    public static final float WATER_NOISE_THRESHOLD = 0.75f;
}
```

### 3. BiomeManager Extensions

Extend the BiomeManager to support water biome generation:

```java
public class BiomeManager {
    // Existing fields...
    
    /**
     * Determines which biome type applies at a given world position.
     * Priority order: Water > Sand > Grass
     */
    public BiomeType getBiomeAtPosition(float worldX, float worldY) {
        // Check for water first (highest priority)
        if (isInWaterPatch(worldX, worldY)) {
            return BiomeType.WATER;
        }
        
        // Check for sand
        if (isInSandPatch(worldX, worldY)) {
            return BiomeType.SAND;
        }
        
        // Default to grass
        return BiomeType.GRASS;
    }
    
    /**
     * Checks if a position is within a water patch.
     * Uses multi-octave noise to create organic lake shapes.
     * Target distribution: ~15% water coverage
     */
    private boolean isInWaterPatch(float worldX, float worldY) {
        float distance = calculateDistanceFromSpawn(worldX, worldY);
        
        // Don't spawn water too close to spawn (keep spawn area grass)
        if (distance < 1500) {
            return false;
        }
        
        // Use multi-octave noise for organic lake shapes
        float noiseScale1 = 0.0002f;  // Large features (lake locations)
        float noiseScale2 = 0.0008f;  // Medium features (lake shapes)
        float noiseScale3 = 0.002f;   // Small features (shoreline detail)
        
        // Sample noise at different scales
        float noise1 = simplexNoise(worldX * noiseScale1, worldY * noiseScale1);
        float noise2 = simplexNoise(worldX * noiseScale2, worldY * noiseScale2);
        float noise3 = simplexNoise(worldX * noiseScale3, worldY * noiseScale3);
        
        // Combine noise octaves
        float combinedNoise = noise1 * 0.5f + noise2 * 0.35f + noise3 * 0.15f;
        
        // Normalize to 0-1 range
        float waterProbability = combinedNoise * 0.5f + 0.5f;
        
        // Threshold for water (0.75 = ~15% coverage)
        return waterProbability > BiomeConfig.WATER_NOISE_THRESHOLD;
    }
    
    /**
     * Generates textures for all biome types including water.
     */
    private void generateAndCacheTextures() {
        // Existing grass and sand generation...
        
        // Generate water texture
        Texture waterTexture = textureGenerator.generateWaterTexture();
        textureCache.put(BiomeType.WATER, waterTexture);
    }
}
```

### 4. BiomeTextureGenerator Extension

Add water texture generation method:

```java
public class BiomeTextureGenerator {
    // Existing methods...
    
    /**
     * Generates a realistic water texture with wave patterns and reflections.
     * Creates blue water with light reflections and depth variations.
     */
    public Texture generateWaterTexture() {
        Pixmap waterPixmap = new Pixmap(
            BiomeConfig.TEXTURE_SIZE, 
            BiomeConfig.TEXTURE_SIZE, 
            Pixmap.Format.RGBA8888
        );
        Random waterRandom = new Random(BiomeConfig.TEXTURE_SEED_WATER);
        
        // Water colors from config
        float[] baseColor = BiomeConfig.WATER_BASE_COLOR;
        float[] lightColor = BiomeConfig.WATER_LIGHT_COLOR;
        float[] darkColor = BiomeConfig.WATER_DARK_COLOR;
        
        // Fill with base water color
        waterPixmap.setColor(baseColor[0], baseColor[1], baseColor[2], baseColor[3]);
        waterPixmap.fill();
        
        // Add water patterns
        addWaterWaves(waterPixmap, waterRandom, baseColor, lightColor);
        addWaterReflections(waterPixmap, waterRandom, lightColor);
        addWaterDepth(waterPixmap, waterRandom, darkColor);
        
        Texture texture = createTextureFromPixmap(waterPixmap);
        waterPixmap.dispose();
        return texture;
    }
    
    /**
     * Adds wave patterns to water texture.
     */
    private void addWaterWaves(Pixmap pixmap, Random random, 
                                float[] baseColor, float[] lightColor) {
        // Create horizontal wave patterns
        for (int y = 0; y < BiomeConfig.TEXTURE_SIZE; y++) {
            if (y % 8 == 0 || y % 8 == 1) {
                for (int x = 0; x < BiomeConfig.TEXTURE_SIZE; x++) {
                    if (random.nextFloat() > 0.3f) {
                        // Lighter wave line
                        pixmap.setColor(
                            lightColor[0], 
                            lightColor[1], 
                            lightColor[2], 
                            lightColor[3]
                        );
                        pixmap.drawPixel(x, y);
                    }
                }
            }
        }
    }
    
    /**
     * Adds light reflections to water surface.
     */
    private void addWaterReflections(Pixmap pixmap, Random random, float[] lightColor) {
        // Add scattered light reflections
        int reflectionCount = 15 + random.nextInt(10);
        for (int i = 0; i < reflectionCount; i++) {
            int x = random.nextInt(BiomeConfig.TEXTURE_SIZE);
            int y = random.nextInt(BiomeConfig.TEXTURE_SIZE);
            
            pixmap.setColor(lightColor[0], lightColor[1], lightColor[2], lightColor[3]);
            pixmap.drawPixel(x, y);
            
            // Extend reflection slightly
            if (random.nextFloat() > 0.5f && x + 1 < BiomeConfig.TEXTURE_SIZE) {
                pixmap.drawPixel(x + 1, y);
            }
        }
    }
    
    /**
     * Adds depth variations to water texture.
     */
    private void addWaterDepth(Pixmap pixmap, Random random, float[] darkColor) {
        // Add darker patches for depth variation
        for (int x = 0; x < BiomeConfig.TEXTURE_SIZE; x++) {
            for (int y = 0; y < BiomeConfig.TEXTURE_SIZE; y++) {
                float noise = random.nextFloat();
                
                if (noise > 0.85f) {
                    // Darker depth areas
                    pixmap.setColor(darkColor[0], darkColor[1], darkColor[2], darkColor[3]);
                    pixmap.drawPixel(x, y);
                }
            }
        }
    }
}
```

### 5. Player Collision Extension

Extend the Player class to check for water collisions:

```java
public class Player {
    // Existing fields...
    
    private boolean wouldCollide(float newX, float newY) {
        // Existing tree and stone collision checks...
        
        // Check collision with water biomes
        if (biomeManager != null && biomeManager.isInitialized()) {
            // Check player center position (player is 64x64, center is +32)
            float playerCenterX = newX + 32;
            float playerCenterY = newY + 32;
            
            BiomeType biomeAtPosition = biomeManager.getBiomeAtPosition(
                playerCenterX, 
                playerCenterY
            );
            
            if (biomeAtPosition == BiomeType.WATER) {
                return true; // Block movement into water
            }
        }
        
        return false;
    }
}
```

### 6. Resource Spawn Validation

Add biome validation to resource spawning systems. Example for tree spawning:

```java
// In MyGdxGame or relevant spawning class
private boolean isValidSpawnLocation(float x, float y) {
    // Check if location is in water
    if (biomeManager != null && biomeManager.isInitialized()) {
        BiomeType biomeType = biomeManager.getBiomeAtPosition(x, y);
        if (biomeType == BiomeType.WATER) {
            return false; // Don't spawn in water
        }
    }
    
    // Existing validation checks...
    return true;
}
```

### 7. Puddle System Integration

Extend puddle creation to check for water biomes:

```java
// In PuddleManager or relevant puddle creation code
public void createPuddle(float x, float y) {
    // Check if location is in water
    if (biomeManager != null && biomeManager.isInitialized()) {
        BiomeType biomeType = biomeManager.getBiomeAtPosition(x, y);
        if (biomeType == BiomeType.WATER) {
            return; // Don't create puddles in water
        }
    }
    
    // Existing puddle creation logic...
}
```

## Data Models

### BiomeType Enum

```java
public enum BiomeType {
    GRASS,   // 50% coverage - default terrain
    SAND,    // 35% coverage - desert areas
    WATER;   // 15% coverage - lakes and water bodies
}
```

### BiomeDistribution

The noise-based generation system will be tuned to achieve the following distribution:

- **Grass**: 50% of world (default/fallback)
- **Sand**: 35% of world (existing sand threshold: 0.45)
- **Water**: 15% of world (new water threshold: 0.75)

The priority order for biome determination is: Water > Sand > Grass

This ensures water takes precedence when noise values indicate water, then sand, with grass as the default.

## 
Correctness Properties

*A property is a characteristic or behavior that should hold true across all valid executions of a system-essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

After analyzing the acceptance criteria, several properties are redundant and can be consolidated. The following properties provide comprehensive coverage without duplication:

**Property 1: Biome type exhaustiveness**
*For any* world coordinate, getBiomeAtPosition should return exactly one of the three valid BiomeType enum values (GRASS, SAND, or WATER)
**Validates: Requirements 1.2**

**Property 2: Water color distinctiveness**
*For any* pixel in the water texture, the blue color channel value should be greater than both the red and green channel values, distinguishing it from grass and sand textures
**Validates: Requirements 1.3**

**Property 3: Biome distribution convergence**
*For any* large sample of random world coordinates (n > 10000), the distribution of biome types should converge to approximately 50% grass, 35% sand, and 15% water (within ±5% tolerance)
**Validates: Requirements 1.4**

**Property 4: Water contiguity**
*For any* water biome tile, at least 40% of its adjacent tiles (within 200 pixel radius) should also be water biome tiles, ensuring lake-like clustering
**Validates: Requirements 1.5**

**Property 5: Water collision blocking**
*For any* world coordinate where getBiomeAtPosition returns WATER, attempting to move the player to that coordinate should result in wouldCollide returning true
**Validates: Requirements 2.1, 2.3**

**Property 6: Collision consistency**
*For any* impassable terrain type (water, tree, stone), the collision response should be identical: player position remains unchanged and wouldCollide returns true
**Validates: Requirements 2.4**

**Property 7: Adjacent movement freedom**
*For any* player position adjacent to water (within 100 pixels), movement in directions that do not lead to water coordinates should succeed (wouldCollide returns false)
**Validates: Requirements 2.5**

**Property 8: Resource spawn exclusion**
*For any* spawned resource (tree, rock, item, or puddle), the biome type at the spawn coordinate should not be WATER
**Validates: Requirements 3.1, 3.2, 3.3, 3.4**

**Property 9: Spawn retry success**
*For any* resource spawn attempt that encounters water, the system should eventually find a valid non-water location within a reasonable number of retries (max 100 attempts)
**Validates: Requirements 3.5**

**Property 10: Biome calculation determinism**
*For any* world coordinate, calling getBiomeAtPosition multiple times with the same coordinates should always return the identical BiomeType value
**Validates: Requirements 4.1, 4.2, 4.4**

## Error Handling

### Invalid Biome Queries

- **Scenario**: BiomeManager queried before initialization
- **Handling**: Throw IllegalStateException with clear message
- **Recovery**: Ensure initialize() is called during game startup

### Texture Generation Failures

- **Scenario**: Water texture generation fails (OpenGL context issues)
- **Handling**: Enter headless mode, biome logic continues without textures
- **Recovery**: Log error, allow server-side operation without rendering

### Collision Detection Edge Cases

- **Scenario**: Player position exactly on biome boundary
- **Handling**: Use player center point (x+32, y+32) for consistent biome determination
- **Recovery**: No recovery needed, deterministic behavior

### Resource Spawn Failures

- **Scenario**: Cannot find non-water location after max retries
- **Handling**: Log warning and skip spawn for this resource
- **Recovery**: Resource will spawn in next cycle or different location

## Testing Strategy

### Unit Testing

Unit tests will verify specific examples and integration points:

1. **BiomeType Enum Test**: Verify WATER constant exists and getDisplayName() returns "water"
2. **BiomeConfig Test**: Verify water color constants are defined and within valid RGBA ranges
3. **Texture Generation Test**: Verify generateWaterTexture() creates a 64x64 texture with blue-dominant colors
4. **Collision Integration Test**: Verify wouldCollide() returns true for known water coordinates
5. **Backward Compatibility Test**: Verify grass and sand biomes still function correctly after adding water

### Property-Based Testing

Property-based tests will verify universal properties across random inputs using a PBT library (JUnit-Quickcheck for Java):

1. **Property Test 1: Biome type exhaustiveness** (Property 1)
   - Generate random world coordinates
   - Verify getBiomeAtPosition returns GRASS, SAND, or WATER
   - Run 100+ iterations

2. **Property Test 2: Water color distinctiveness** (Property 2)
   - Sample random pixels from water texture
   - Verify blue channel > red channel AND blue channel > green channel
   - Run 100+ iterations

3. **Property Test 3: Biome distribution convergence** (Property 3)
   - Generate 10,000+ random coordinates
   - Count biome type occurrences
   - Verify distribution: 45-55% grass, 30-40% sand, 10-20% water
   - Run 10+ iterations with different random seeds

4. **Property Test 4: Water contiguity** (Property 4)
   - Generate random water coordinates
   - Sample adjacent tiles within 200px radius
   - Verify ≥40% are also water
   - Run 100+ iterations

5. **Property Test 5: Water collision blocking** (Property 5)
   - Generate random water coordinates
   - Verify wouldCollide(waterX, waterY) returns true
   - Run 100+ iterations

6. **Property Test 6: Collision consistency** (Property 6)
   - Generate random impassable coordinates (water, tree, stone)
   - Verify all return true from wouldCollide()
   - Run 100+ iterations

7. **Property Test 7: Adjacent movement freedom** (Property 7)
   - Generate random positions adjacent to water
   - Test movement in non-water directions
   - Verify wouldCollide() returns false for valid directions
   - Run 100+ iterations

8. **Property Test 8: Resource spawn exclusion** (Property 8)
   - Generate random resource spawn coordinates
   - Verify none have BiomeType.WATER
   - Run 100+ iterations across all resource types

9. **Property Test 9: Spawn retry success** (Property 9)
   - Simulate spawn attempts in water-heavy areas
   - Verify valid location found within 100 retries
   - Run 100+ iterations

10. **Property Test 10: Biome calculation determinism** (Property 10)
    - Generate random coordinates
    - Call getBiomeAtPosition() 10 times for each coordinate
    - Verify all 10 calls return identical BiomeType
    - Run 100+ iterations

### Integration Testing

Integration tests will verify the complete feature in realistic scenarios:

1. **Multiplayer Sync Test**: Verify two clients see water in identical locations
2. **Resource Spawn Test**: Verify trees, rocks, and items don't spawn in water over 1000 spawn attempts
3. **Player Movement Test**: Verify player cannot walk into water but can walk parallel to shoreline
4. **Puddle System Test**: Verify puddles don't appear in water biomes during rain

### Testing Framework

- **Unit Tests**: JUnit 5
- **Property-Based Tests**: JUnit-Quickcheck (https://pholser.github.io/junit-quickcheck/)
- **Mocking**: Mockito (for isolating components)
- **Test Coverage Target**: 80%+ line coverage for new code

## Implementation Notes

### Biome Distribution Tuning

The target distribution (50% grass, 35% sand, 15% water) will be achieved by:

1. **Water threshold**: 0.75 (noise values > 0.75 = water)
2. **Sand threshold**: 0.45 (noise values > 0.45 and not water = sand)
3. **Grass**: Default (all remaining areas)

These thresholds may require fine-tuning during implementation based on actual distribution measurements.

### Performance Considerations

- **Biome Lookup**: O(1) calculation using noise functions, no performance impact
- **Texture Generation**: One-time cost during initialization, cached for reuse
- **Collision Detection**: Single additional biome check per movement attempt, negligible overhead
- **Memory**: One additional 64x64 RGBA texture (~16KB), minimal impact

### Multiplayer Synchronization

Deterministic biome generation is ensured by:

1. **Fixed Seeds**: All noise functions use fixed seeds defined in BiomeConfig
2. **Coordinate-Based**: Biome type depends only on world coordinates, not time or random state
3. **No Network Messages**: Biome data is never transmitted, each client calculates independently
4. **Identical Logic**: All clients run identical biome calculation code

### Migration Strategy

1. **Phase 1**: Add WATER to BiomeType enum
2. **Phase 2**: Extend BiomeManager with water detection logic
3. **Phase 3**: Add water texture generation
4. **Phase 4**: Integrate collision detection
5. **Phase 5**: Add resource spawn validation
6. **Phase 6**: Testing and distribution tuning

Existing saves and multiplayer sessions will automatically see water biomes without requiring data migration, as biomes are calculated on-demand from coordinates.

## Future Enhancements

Potential future improvements (out of scope for this design):

- **Fishing**: Allow players to fish in water biomes
- **Boats**: Add boat items for water traversal
- **Water Depth**: Multiple water depths with visual variation
- **Rivers**: Flowing water connecting lakes
- **Bridges**: Craftable structures to cross water
- **Water Creatures**: Fish, frogs, or other aquatic entities
- **Seasonal Changes**: Ice in winter, dried lakes in summer
