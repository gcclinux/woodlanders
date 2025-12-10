package wagemaker.uk.biome;

import com.badlogic.gdx.graphics.Texture;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Core biome management system that determines which biome applies at any world coordinate.
 * This class handles biome zone configuration, distance calculations, texture caching,
 * and provides the main API for querying biome information.
 * 
 * The BiomeManager is responsible for:
 * - Calculating distance from spawn point (0,0)
 * - Determining which biome zone applies at any coordinate
 * - Caching and providing textures for each biome type
 * - Managing texture lifecycle and cleanup
 * 
 * Requirements: 1.2 (distance calculation), 1.3 (seamless rendering), 1.5 (mode consistency),
 *               2.1 (performance), 2.2 (consistent depth), 4.1 (coordinate-based), 4.2 (deterministic)
 */
public class BiomeManager {
    
    private final List<BiomeZone> biomeZones;
    private final Map<BiomeType, Texture> textureCache;
    private final BiomeTextureGenerator textureGenerator;
    private boolean initialized;
    @SuppressWarnings("unused")
    private final Random noiseRandom;
    private boolean headlessMode;
    
    // Beach-style biome calculation - new noise generators
    private Random baseBiomeRandom;
    private Random waterInSandRandom;
    
    // ========== PERFORMANCE OPTIMIZATION CACHES ==========
    
    /**
     * Spatial cache for buffer zone validation results.
     * Key: encoded coordinate (x,y) -> Value: validation result
     * Cache size is limited to prevent memory issues.
     */
    private final Map<Long, Boolean> bufferValidationCache;
    
    /**
     * Cache for base biome calculation results.
     * Key: encoded coordinate (x,y) -> Value: BiomeType
     * Used to avoid recalculating base biomes during buffer validation.
     */
    private final Map<Long, BiomeType> baseBiomeCache;
    
    /**
     * Maximum size for performance caches to prevent memory issues.
     * When cache exceeds this size, it will be cleared.
     */
    private static final int MAX_CACHE_SIZE = BiomeConfig.MAX_PERFORMANCE_CACHE_SIZE;
    
    /**
     * Grid size for spatial caching in pixels.
     * Buffer validation results are cached per grid cell.
     */
    private static final float CACHE_GRID_SIZE = BiomeConfig.PERFORMANCE_CACHE_GRID_SIZE;
    
    /**
     * Creates a new BiomeManager instance.
     * Call initialize() after construction to set up biome zones and generate textures.
     */
    public BiomeManager() {
        this.biomeZones = new ArrayList<>();
        this.textureCache = new HashMap<>();
        this.textureGenerator = new BiomeTextureGenerator();
        this.initialized = false;
        this.noiseRandom = new Random(54321); // Fixed seed for consistent noise pattern
        this.headlessMode = false;
        
        // Initialize performance caches
        this.bufferValidationCache = new HashMap<>();
        this.baseBiomeCache = new HashMap<>();
    }
    
    /**
     * Initializes the biome system by setting up zones and generating textures.
     * This method must be called before using any other BiomeManager methods.
     * 
     * Initialization steps:
     * 1. Create default biome zone configuration
     * 2. Generate textures for each biome type (only if on OpenGL thread)
     * 3. Cache textures for fast lookup
     * 
     * Requirements: 1.1 (multiple biome zones), 3.1 (configurable thresholds)
     */
    public void initialize() {
        if (initialized) {
            return; // Already initialized
        }
        
        // Set up biome zones
        initializeBiomeZones();
        
        // Initialize new noise generators for beach-style biome calculation
        initializeNoiseGenerators();
        
        // Generate and cache textures for each biome type
        // Skip texture generation if not on OpenGL thread (e.g., server-side or tests)
        // Check if we're on the main rendering thread by checking thread name
        String threadName = Thread.currentThread().getName();
        boolean isRenderThread = threadName.contains("LWJGL") || threadName.equals("main");
        
        if (com.badlogic.gdx.Gdx.graphics != null && isRenderThread) {
            try {
                generateAndCacheTextures();
            } catch (Exception e) {
                // Texture generation failed - enter headless mode
                System.err.println("BiomeManager: Texture generation failed, entering headless mode: " + e.getMessage());
                headlessMode = true;
            }
        } else {
            // Not on OpenGL thread (e.g., server-side background thread or tests)
            // Biome zones are still initialized and functional
            headlessMode = true;
        }
        
        initialized = true;
    }
    
    /**
     * Gets the appropriate texture for a given world position.
     * This is the main method used by the rendering system.
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return The texture to use at this position, or grass texture as fallback
     * 
     * Requirements: 1.3 (seamless rendering), 2.1 (performance), 4.1 (coordinate-based)
     */
    public Texture getTextureForPosition(float worldX, float worldY) {
        if (!initialized) {
            throw new IllegalStateException("BiomeManager must be initialized before use. Call initialize() first.");
        }
        
        if (headlessMode) {
            throw new IllegalStateException("Cannot get textures in headless mode (unit tests). Use getBiomeAtPosition() instead.");
        }
        
        BiomeType biomeType = getBiomeAtPosition(worldX, worldY);
        Texture texture = textureCache.get(biomeType);
        
        // Fallback to grass texture if somehow missing
        if (texture == null) {
            texture = textureCache.get(BiomeType.GRASS);
        }
        
        return texture;
    }
    
    /**
     * Determines which biome type applies at a given world position.
     * Uses two-phase beach-style calculation: base biome + water overlay.
     * Phase 1: Determine if location is grass or sand base
     * Phase 2: If sand, check if water should be placed there
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return The biome type at this position
     * 
     * Requirements: 1.1 (water only in sand), 1.2 (biome type exhaustiveness), 1.3 (buffer distance), 4.1 (coordinate-based), 4.2 (deterministic)
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
     * This is the foundation for beach-style biome calculation where water
     * can only spawn within sand base areas.
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return The base biome type (GRASS or SAND only)
     * 
     * Requirements: 2.1 (50% grass, 50% sand base distribution), 2.3 (noise-based distribution)
     */
    public BiomeType getBaseBiomeAtPosition(float worldX, float worldY) {
        float distance = calculateDistanceFromSpawn(worldX, worldY);
        
        // Inner grass zone (unchanged from existing system)
        if (distance <= BiomeConfig.INNER_GRASS_RADIUS) {
            return BiomeType.GRASS;
        }
        
        // Sand zone (unchanged from existing system)
        if (distance < BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH) {
            return BiomeType.SAND;
        }
        
        // Beyond sand zone: use noise for 50/50 grass/sand distribution
        float noiseValue = baseBiomeNoise(
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
     * Checks if a position is within a sand patch.
     * Uses multi-octave noise to create organic, irregular sand patches
     * scattered throughout the world at various distances from spawn.
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return true if position is in sand, false otherwise
     */
    private boolean isInSandPatch(float worldX, float worldY) {
        float distance = calculateDistanceFromSpawn(worldX, worldY);
        
        // Don't spawn sand too close to spawn (keep spawn area grass)
        if (distance < 1000) {
            return false;
        }
        
        // Use multi-octave noise to create organic sand patches
        // Scale coordinates for noise sampling
        float noiseScale1 = 0.00015f; // Large features (major patch locations)
        float noiseScale2 = 0.0006f;  // Medium features (patch shapes)
        float noiseScale3 = 0.0015f;  // Small features (edges and details)
        
        // Sample noise at different scales
        float noise1 = simplexNoise(worldX * noiseScale1, worldY * noiseScale1);
        float noise2 = simplexNoise(worldX * noiseScale2, worldY * noiseScale2);
        float noise3 = simplexNoise(worldX * noiseScale3, worldY * noiseScale3);
        
        // Combine noise octaves with different weights
        float combinedNoise = noise1 * 0.5f + noise2 * 0.35f + noise3 * 0.15f;
        
        // Add periodic variation based on distance to create "rings" of varying sand density
        // This creates areas with more/less sand as you travel outward
        float distancePhase = (float) Math.sin(distance * 0.0003f) * 0.05f;
        
        // Normalize combined noise to 0-1 range and add distance variation
        float sandProbability = (combinedNoise * 0.5f + 0.5f) + distancePhase;
        
        // Threshold for sand (adjust to control sand coverage)
        // 0.50 targets roughly 35% sand coverage (tuned based on distribution testing)
        // Tuning results: 36.85% sand coverage (within ±5% tolerance)
        return sandProbability > 0.50f;
    }
    
    /**
     * Checks if a position is within a water patch.
     * Uses multi-octave noise to create organic lake shapes.
     * Target distribution: ~15% water coverage with smaller, more varied shapes
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return true if position is in water, false otherwise
     * 
     * Requirements: 1.4 (biome distribution), 1.5 (contiguous lake regions), 4.1 (coordinate-based)
     */
    private boolean isInWaterPatch(float worldX, float worldY) {
        float distance = calculateDistanceFromSpawn(worldX, worldY);
        
        // Don't spawn water too close to spawn (keep spawn area grass)
        if (distance < 1500) {
            return false;
        }
        
        // Use multi-octave noise for organic lake shapes
        // Higher frequency values create smaller, more varied water bodies
        float noiseScale1 = 0.0003f;   // Large features (lake locations) - increased for smaller lakes
        float noiseScale2 = 0.0012f;   // Medium features (lake shapes) - increased for more variation
        float noiseScale3 = 0.0025f;   // Small features (shoreline detail) - increased for irregular edges
        
        // Sample noise at different scales
        float noise1 = simplexNoise(worldX * noiseScale1, worldY * noiseScale1);
        float noise2 = simplexNoise(worldX * noiseScale2, worldY * noiseScale2);
        float noise3 = simplexNoise(worldX * noiseScale3, worldY * noiseScale3);
        
        // Combine noise octaves with more balanced weights for varied shapes
        float combinedNoise = noise1 * 0.4f + noise2 * 0.35f + noise3 * 0.25f;
        
        // Normalize to 0-1 range
        float waterProbability = combinedNoise * 0.5f + 0.5f;
        
        // Threshold for water - adjusted to maintain ~15% coverage with new noise scales
        return waterProbability > BiomeConfig.WATER_NOISE_THRESHOLD;
    }
    
    /**
     * Calculates the Euclidean distance from the spawn point (0,0) to a given position.
     * 
     * Formula: distance = sqrt(x² + y²)
     * 
     * @param x The x-coordinate in world space
     * @param y The y-coordinate in world space
     * @return The distance from spawn point in pixels
     * 
     * Requirements: 1.2 (distance calculation), 4.1 (coordinate-based)
     */
    private float calculateDistanceFromSpawn(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }
    
    /**
     * Calculates a noise-based offset to add natural variation to biome boundaries.
     * Uses multiple octaves of simplex-like noise to create organic, wavy boundaries
     * instead of perfect circles.
     * 
     * The noise is deterministic based on world coordinates, ensuring consistent
     * biome boundaries across multiple game sessions.
     * 
     * @param x The x-coordinate in world space
     * @param y The y-coordinate in world space
     * @return A noise offset value to add to the distance calculation (typically -1500 to +1500)
     * 
     * Requirements: 1.4 (natural variation), 4.2 (deterministic)
     */
    @SuppressWarnings("unused")
    private float calculateNoiseOffset(float x, float y) {
        // Use multiple octaves of noise for more natural variation
        // Scale down coordinates for larger noise features
        float scale1 = 0.0003f; // Large-scale features (wavy boundaries)
        float scale2 = 0.001f;  // Medium-scale features (smaller indentations)
        float scale3 = 0.003f;  // Small-scale features (fine detail)
        
        // Calculate noise at different scales
        float noise1 = simplexNoise(x * scale1, y * scale1) * 1000.0f;  // ±1000px variation
        float noise2 = simplexNoise(x * scale2, y * scale2) * 400.0f;   // ±400px variation
        float noise3 = simplexNoise(x * scale3, y * scale3) * 100.0f;   // ±100px variation
        
        // Combine octaves for natural-looking boundaries
        return noise1 + noise2 + noise3;
    }
    
    /**
     * Simple 2D simplex-like noise function for creating natural variation.
     * This is a simplified noise implementation that provides deterministic
     * pseudo-random values based on coordinates.
     * 
     * @param x The x-coordinate (scaled)
     * @param y The y-coordinate (scaled)
     * @return A noise value between -1.0 and 1.0
     */
    private float simplexNoise(float x, float y) {
        // Use a hash-based approach for deterministic noise
        // This creates smooth, continuous variation across the world
        
        // Get integer coordinates
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);
        
        // Get fractional parts
        float xf = x - xi;
        float yf = y - yi;
        
        // Smooth interpolation (smoothstep function)
        float u = xf * xf * (3.0f - 2.0f * xf);
        float v = yf * yf * (3.0f - 2.0f * yf);
        
        // Get noise values at grid corners
        float n00 = hash2D(xi, yi);
        float n10 = hash2D(xi + 1, yi);
        float n01 = hash2D(xi, yi + 1);
        float n11 = hash2D(xi + 1, yi + 1);
        
        // Bilinear interpolation
        float nx0 = lerp(n00, n10, u);
        float nx1 = lerp(n01, n11, u);
        
        return lerp(nx0, nx1, v);
    }
    
    /**
     * Hash function to generate deterministic pseudo-random values for noise.
     * 
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @return A pseudo-random value between -1.0 and 1.0
     */
    private float hash2D(int x, int y) {
        // Use a simple hash function for deterministic randomness
        int n = x + y * 57;
        n = (n << 13) ^ n;
        int nn = (n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff;
        return 1.0f - ((float) nn / 1073741824.0f);
    }
    
    /**
     * Linear interpolation between two values.
     * 
     * @param a The first value
     * @param b The second value
     * @param t The interpolation factor (0.0 to 1.0)
     * @return The interpolated value
     */
    private float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }
    
    /**
     * Initializes noise generators for beach-style biome calculation.
     * Sets up separate Random instances for base biome and water-in-sand generation
     * using fixed seeds to ensure deterministic biome layouts.
     * 
     * Requirements: 2.1 (deterministic base biome calculation), 1.1 (deterministic water placement)
     */
    private void initializeNoiseGenerators() {
        // Initialize base biome noise generator (grass/sand determination)
        baseBiomeRandom = new Random(BiomeConfig.BASE_BIOME_SEED);
        
        // Initialize water-in-sand noise generator
        waterInSandRandom = new Random(BiomeConfig.WATER_IN_SAND_SEED);
    }
    
    /**
     * Generates noise for base biome calculation using the base biome Random instance.
     * This creates deterministic noise patterns for grass/sand distribution.
     * 
     * @param x The scaled x-coordinate for noise sampling
     * @param y The scaled y-coordinate for noise sampling
     * @return A noise value between -1.0 and 1.0
     * 
     * Requirements: 2.1 (deterministic base biome calculation), 2.3 (noise-based distribution)
     */
    private float baseBiomeNoise(float x, float y) {
        // Get integer coordinates
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);
        
        // Get fractional parts
        float xf = x - xi;
        float yf = y - yi;
        
        // Smooth interpolation (smoothstep function)
        float u = xf * xf * (3.0f - 2.0f * xf);
        float v = yf * yf * (3.0f - 2.0f * yf);
        
        // Get noise values at grid corners using base biome hash
        float n00 = baseBiomeHash2D(xi, yi);
        float n10 = baseBiomeHash2D(xi + 1, yi);
        float n01 = baseBiomeHash2D(xi, yi + 1);
        float n11 = baseBiomeHash2D(xi + 1, yi + 1);
        
        // Bilinear interpolation
        float nx0 = lerp(n00, n10, u);
        float nx1 = lerp(n01, n11, u);
        
        return lerp(nx0, nx1, v);
    }
    
    /**
     * Hash function for base biome noise generation.
     * Uses the base biome seed to generate deterministic pseudo-random values.
     * 
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @return A pseudo-random value between -1.0 and 1.0
     * 
     * Requirements: 2.1 (deterministic base biome calculation)
     */
    private float baseBiomeHash2D(int x, int y) {
        // Use base biome seed in hash calculation for deterministic results
        int n = x + y * 57 + BiomeConfig.BASE_BIOME_SEED;
        n = (n << 13) ^ n;
        int nn = (n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff;
        return 1.0f - ((float) nn / 1073741824.0f);
    }
    
    /**
     * Generates noise for water-in-sand calculation using the water-in-sand Random instance.
     * This creates deterministic noise patterns for water placement within sand areas.
     * 
     * @param x The scaled x-coordinate for noise sampling
     * @param y The scaled y-coordinate for noise sampling
     * @return A noise value between -1.0 and 1.0
     * 
     * Requirements: 1.1 (deterministic water placement), 1.4 (water coverage in sand)
     */
    private float waterInSandNoise(float x, float y) {
        // Get integer coordinates
        int xi = (int) Math.floor(x);
        int yi = (int) Math.floor(y);
        
        // Get fractional parts
        float xf = x - xi;
        float yf = y - yi;
        
        // Smooth interpolation (smoothstep function)
        float u = xf * xf * (3.0f - 2.0f * xf);
        float v = yf * yf * (3.0f - 2.0f * yf);
        
        // Get noise values at grid corners using water-in-sand hash
        float n00 = waterInSandHash2D(xi, yi);
        float n10 = waterInSandHash2D(xi + 1, yi);
        float n01 = waterInSandHash2D(xi, yi + 1);
        float n11 = waterInSandHash2D(xi + 1, yi + 1);
        
        // Bilinear interpolation
        float nx0 = lerp(n00, n10, u);
        float nx1 = lerp(n01, n11, u);
        
        return lerp(nx0, nx1, v);
    }
    
    /**
     * Hash function for water-in-sand noise generation.
     * Uses the water-in-sand seed to generate deterministic pseudo-random values.
     * 
     * @param x The x grid coordinate
     * @param y The y grid coordinate
     * @return A pseudo-random value between -1.0 and 1.0
     * 
     * Requirements: 1.1 (deterministic water placement), 1.4 (water coverage in sand)
     */
    private float waterInSandHash2D(int x, int y) {
        // Use water-in-sand seed in hash calculation for deterministic results
        int n = x + y * 57 + BiomeConfig.WATER_IN_SAND_SEED;
        n = (n << 13) ^ n;
        int nn = (n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff;
        return 1.0f - ((float) nn / 1073741824.0f);
    }
    
    /**
     * Checks if a sand location is eligible for water placement.
     * Requirements:
     * 1. Location must be in a sand base biome (already checked by caller)
     * 2. Must be at least 128px away from any grass area
     * 3. Must pass the water-in-sand noise threshold (40% coverage)
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return true if eligible for water placement, false otherwise
     * 
     * Requirements: 1.1 (water only in sand), 1.3 (buffer distance), 1.4 (40% coverage)
     */
    private boolean isEligibleForWater(float worldX, float worldY) {
        // Requirement 1: Must be in sand base biome (already checked by caller)
        
        // Requirement 2: Check buffer distance from grass
        if (!isValidBeachBuffer(worldX, worldY)) {
            return false;
        }
        
        // Requirement 3: Check water-in-sand noise threshold
        float waterNoise = waterInSandNoise(
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
     * Uses spatial caching and early-exit optimizations for improved performance.
     * 
     * Performance optimizations:
     * 1. Spatial caching: Cache results per 128x128 pixel grid cells
     * 2. Early-exit: Stop checking once grass is found
     * 3. Coarse-to-fine: Check cardinal directions first, then full circle
     * 4. Cache management: Clear cache when it gets too large
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return true if location is valid (no grass within buffer), false otherwise
     * 
     * Requirements: 1.3 (water maintains buffer distance from grass), 3.1 (buffer zone validation)
     */
    public boolean isValidBeachBuffer(float worldX, float worldY) {
        // Performance optimization: Check spatial cache first (if enabled)
        // Use a smaller grid size for buffer validation to maintain precision
        float bufferCacheGridSize = 64.0f; // Smaller than buffer distance for accuracy
        if (BiomeConfig.ENABLE_BUFFER_VALIDATION_CACHE) {
            long cacheKey = encodeCacheKey(worldX, worldY, bufferCacheGridSize);
            Boolean cachedResult = bufferValidationCache.get(cacheKey);
            if (cachedResult != null) {
                return cachedResult;
            }
        }
        
        // Performance optimization: Early exit for obvious cases (if enabled)
        float distanceFromSpawn = calculateDistanceFromSpawn(worldX, worldY);
        boolean result;
        
        if (BiomeConfig.ENABLE_EARLY_EXIT_OPTIMIZATION) {
            // If we're very close to the inner grass boundary, likely invalid
            if (distanceFromSpawn < BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.BEACH_BUFFER_DISTANCE * 0.5f) {
                result = false;
                if (BiomeConfig.ENABLE_BUFFER_VALIDATION_CACHE) {
                    long cacheKey = encodeCacheKey(worldX, worldY, bufferCacheGridSize);
                    cacheResult(cacheKey, result);
                }
                return result;
            }
            
            // If we're very far from any possible grass areas, use reduced sampling
            if (distanceFromSpawn > BiomeConfig.REDUCED_SAMPLING_DISTANCE) {
                result = isValidBeachBufferFarFromSpawn(worldX, worldY);
                if (BiomeConfig.ENABLE_BUFFER_VALIDATION_CACHE) {
                    long cacheKey = encodeCacheKey(worldX, worldY, bufferCacheGridSize);
                    cacheResult(cacheKey, result);
                }
                return result;
            }
        }
        
        // Standard buffer validation with optimizations
        result = isValidBeachBufferStandard(worldX, worldY);
        if (BiomeConfig.ENABLE_BUFFER_VALIDATION_CACHE) {
            long cacheKey = encodeCacheKey(worldX, worldY, bufferCacheGridSize);
            cacheResult(cacheKey, result);
        }
        return result;
    }
    
    /**
     * Optimized buffer validation for locations far from spawn.
     * Uses reduced sampling since grass patches are less likely.
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return true if location is valid (no grass within buffer), false otherwise
     */
    private boolean isValidBeachBufferFarFromSpawn(float worldX, float worldY) {
        float bufferDistance = BiomeConfig.BEACH_BUFFER_DISTANCE;
        
        // Performance optimization: Check only 8 cardinal/diagonal directions first
        int[] angles = {0, 45, 90, 135, 180, 225, 270, 315}; // degrees
        
        for (int angleDegrees : angles) {
            float angleRadians = (float) Math.toRadians(angleDegrees);
            float checkX = worldX + bufferDistance * (float) Math.cos(angleRadians);
            float checkY = worldY + bufferDistance * (float) Math.sin(angleRadians);
            
            // Early exit: If any cardinal direction has grass, reject immediately
            if (getBaseBiomeAtPosition(checkX, checkY) == BiomeType.GRASS) {
                return false;
            }
        }
        
        // If cardinal directions are clear, do a more thorough check
        // Use more sample points to ensure we don't miss grass areas
        int samplePoints = 12; // Increased from 8 for better accuracy
        for (int i = 0; i < samplePoints; i++) {
            float angle = (float) (2 * Math.PI * i / samplePoints);
            float checkX = worldX + bufferDistance * (float) Math.cos(angle);
            float checkY = worldY + bufferDistance * (float) Math.sin(angle);
            
            // Early exit: If any point has grass, reject immediately
            if (getBaseBiomeAtPosition(checkX, checkY) == BiomeType.GRASS) {
                return false;
            }
        }
        
        return true; // No grass found in reduced sampling
    }
    
    /**
     * Standard buffer validation with full sampling and optimizations.
     * Used for locations near spawn or in areas where grass is more likely.
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return true if location is valid (no grass within buffer), false otherwise
     */
    private boolean isValidBeachBufferStandard(float worldX, float worldY) {
        float bufferDistance = BiomeConfig.BEACH_BUFFER_DISTANCE;
        float distanceFromSpawn = calculateDistanceFromSpawn(worldX, worldY);
        
        // Performance optimization: Check cardinal directions first (most likely to find grass)
        float[] cardinalAngles = {0, (float) Math.PI / 2, (float) Math.PI, 3 * (float) Math.PI / 2};
        
        for (float angle : cardinalAngles) {
            float checkX = worldX + bufferDistance * (float) Math.cos(angle);
            float checkY = worldY + bufferDistance * (float) Math.sin(angle);
            
            // Early exit: If any cardinal direction has grass, reject immediately
            if (getBaseBiomeAtPosition(checkX, checkY) == BiomeType.GRASS) {
                return false;
            }
        }
        
        // Check full circle with optimized sampling
        int samplePoints = 16; // Restored to original value for accuracy
        for (int i = 0; i < samplePoints; i++) {
            float angle = (float) (2 * Math.PI * i / samplePoints);
            float checkX = worldX + bufferDistance * (float) Math.cos(angle);
            float checkY = worldY + bufferDistance * (float) Math.sin(angle);
            
            // Early exit: If any point in the buffer zone is grass, reject immediately
            if (getBaseBiomeAtPosition(checkX, checkY) == BiomeType.GRASS) {
                return false;
            }
        }
        
        // Check intermediate distances with reduced sampling
        float halfBuffer = bufferDistance * 0.5f; // Restored to original value
        int innerSamplePoints = 8; // Restored to original value
        
        for (int i = 0; i < innerSamplePoints; i++) {
            float angle = (float) (2 * Math.PI * i / innerSamplePoints);
            float checkX = worldX + halfBuffer * (float) Math.cos(angle);
            float checkY = worldY + halfBuffer * (float) Math.sin(angle);
            
            // Early exit: If any point in the inner buffer zone is grass, reject immediately
            if (getBaseBiomeAtPosition(checkX, checkY) == BiomeType.GRASS) {
                return false;
            }
        }
        
        // Special handling for locations near the inner grass boundary
        if (distanceFromSpawn < BiomeConfig.INNER_GRASS_RADIUS + bufferDistance + 100.0f) {
            // Check points along the line toward spawn with optimized sampling
            float directionX = -worldX / distanceFromSpawn; // Unit vector toward spawn
            float directionY = -worldY / distanceFromSpawn;
            
            // Check at various distances toward spawn
            for (float checkDistance = bufferDistance * 0.25f; checkDistance <= bufferDistance; checkDistance += 4.0f) {
                float checkX = worldX + directionX * checkDistance;
                float checkY = worldY + directionY * checkDistance;
                
                // Early exit: If grass found toward spawn, reject immediately
                if (getBaseBiomeAtPosition(checkX, checkY) == BiomeType.GRASS) {
                    return false;
                }
            }
        }
        
        return true; // No grass found in buffer zone
    }
    
    /**
     * Calculates the minimum distance from a point to the nearest grass biome.
     * Used for debugging and validation purposes to understand buffer zone violations.
     * This method performs a comprehensive search in a wider area around the point.
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return The minimum distance to grass in pixels, or Float.MAX_VALUE if no grass found
     * 
     * Requirements: 1.3 (buffer zone validation), 3.1 (debugging support)
     */
    public float calculateDistanceToGrass(float worldX, float worldY) {
        // If we're already in grass, return 0
        if (getBaseBiomeAtPosition(worldX, worldY) == BiomeType.GRASS) {
            return 0.0f;
        }
        
        float distanceFromSpawn = calculateDistanceFromSpawn(worldX, worldY);
        
        // For points in the sand zone, we know grass is at the inner boundary
        if (distanceFromSpawn >= BiomeConfig.INNER_GRASS_RADIUS && 
            distanceFromSpawn < BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH) {
            // We're in the sand zone - grass is at the inner boundary
            return distanceFromSpawn - BiomeConfig.INNER_GRASS_RADIUS;
        }
        
        // For points beyond the sand zone, we need to search more carefully
        // Use a larger search radius for points far from spawn
        float searchRadius = Math.max(BiomeConfig.BEACH_BUFFER_DISTANCE * 4, 1000.0f);
        float minDistance = Float.MAX_VALUE;
        int sampleResolution = 32; // Check every 32 pixels for reasonable performance
        
        // Search in a grid pattern around the point
        for (float dx = -searchRadius; dx <= searchRadius; dx += sampleResolution) {
            for (float dy = -searchRadius; dy <= searchRadius; dy += sampleResolution) {
                float checkX = worldX + dx;
                float checkY = worldY + dy;
                
                // Skip points outside the search radius (circular search area)
                float distanceFromCenter = (float) Math.sqrt(dx * dx + dy * dy);
                if (distanceFromCenter > searchRadius) {
                    continue;
                }
                
                // Check if this point is grass
                if (getBaseBiomeAtPosition(checkX, checkY) == BiomeType.GRASS) {
                    minDistance = Math.min(minDistance, distanceFromCenter);
                    
                    // Early exit optimization - if we found grass very close, no need to search further
                    if (minDistance < sampleResolution) {
                        return minDistance;
                    }
                }
            }
        }
        
        // If no grass found in the search area, try a more targeted search
        // Check along the direction toward spawn (where grass is most likely to be)
        if (minDistance == Float.MAX_VALUE && distanceFromSpawn > BiomeConfig.INNER_GRASS_RADIUS) {
            // We're outside the inner grass zone, so grass should be toward spawn
            float directionX = -worldX / distanceFromSpawn; // Unit vector toward spawn
            float directionY = -worldY / distanceFromSpawn;
            
            // Search along the line toward spawn with larger steps
            for (float step = sampleResolution; step <= searchRadius * 2; step += sampleResolution) {
                float checkX = worldX + directionX * step;
                float checkY = worldY + directionY * step;
                
                if (getBaseBiomeAtPosition(checkX, checkY) == BiomeType.GRASS) {
                    return step;
                }
            }
        }
        
        return minDistance;
    }
    
    // ========== PERFORMANCE OPTIMIZATION METHODS ==========
    
    /**
     * Encodes world coordinates into a cache key for spatial caching.
     * Coordinates are snapped to a grid to enable spatial locality.
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @param gridSize The size of the cache grid in pixels
     * @return A long key representing the grid cell
     */
    private long encodeCacheKey(float worldX, float worldY, float gridSize) {
        int gridX = (int) Math.floor(worldX / gridSize);
        int gridY = (int) Math.floor(worldY / gridSize);
        
        // Combine grid coordinates into a single long key
        return ((long) gridX << 32) | (gridY & 0xFFFFFFFFL);
    }
    
    /**
     * Caches a buffer validation result with automatic cache size management.
     * 
     * @param cacheKey The cache key for the result
     * @param result The validation result to cache
     */
    private void cacheResult(long cacheKey, boolean result) {
        // Performance optimization: Clear cache if it gets too large
        if (bufferValidationCache.size() >= MAX_CACHE_SIZE) {
            bufferValidationCache.clear();
            // Also clear base biome cache to maintain consistency
            baseBiomeCache.clear();
        }
        
        bufferValidationCache.put(cacheKey, result);
    }
    
    /**
     * Gets base biome at position with caching for improved performance.
     * This reduces redundant base biome calculations during buffer validation.
     * 
     * @param worldX The x-coordinate in world space
     * @param worldY The y-coordinate in world space
     * @return The base biome type at this position
     */
    private BiomeType getCachedBaseBiomeAtPosition(float worldX, float worldY) {
        // Use a much smaller grid size for base biome caching to maintain accuracy
        // Buffer validation requires high precision, so we use 32-pixel grid cells
        float precisionGridSize = 32.0f;
        long cacheKey = encodeCacheKey(worldX, worldY, precisionGridSize);
        
        BiomeType cachedBiome = baseBiomeCache.get(cacheKey);
        if (cachedBiome != null) {
            return cachedBiome;
        }
        
        // Calculate and cache the result
        BiomeType biome = getBaseBiomeAtPosition(worldX, worldY);
        
        // Performance optimization: Clear cache if it gets too large
        if (baseBiomeCache.size() >= MAX_CACHE_SIZE) {
            baseBiomeCache.clear();
        }
        
        baseBiomeCache.put(cacheKey, biome);
        return biome;
    }
    
    /**
     * Clears all performance caches.
     * Useful for testing or when memory usage needs to be reduced.
     */
    public void clearPerformanceCaches() {
        bufferValidationCache.clear();
        baseBiomeCache.clear();
    }
    
    /**
     * Gets performance statistics for monitoring and tuning.
     * 
     * @return A map containing cache sizes and hit rates
     */
    public Map<String, Object> getPerformanceStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("bufferValidationCacheSize", bufferValidationCache.size());
        stats.put("baseBiomeCacheSize", baseBiomeCache.size());
        stats.put("maxCacheSize", MAX_CACHE_SIZE);
        stats.put("cacheGridSize", CACHE_GRID_SIZE);
        return stats;
    }
    
    /**
     * Initializes the default biome zone configuration.
     * 
     * Default zones:
     * - Zone 1: 0 to 10000px → GRASS (inner spawn area)
     * - Zone 2: 10000 to 13000px → SAND (desert ring)
     * - Zone 3: 13000+ → GRASS (outer areas)
     * 
     * Requirements: 1.1 (multiple biome zones), 3.1 (configurable thresholds)
     */
    private void initializeBiomeZones() {
        biomeZones.clear();
        
        // Zone 1: Inner grass zone (spawn area)
        biomeZones.add(new BiomeZone(
            0.0f,
            BiomeConfig.INNER_GRASS_RADIUS,
            BiomeType.GRASS
        ));
        
        // Zone 2: Sand zone (desert ring)
        float sandZoneEnd = BiomeConfig.INNER_GRASS_RADIUS + BiomeConfig.SAND_ZONE_WIDTH;
        biomeZones.add(new BiomeZone(
            BiomeConfig.INNER_GRASS_RADIUS,
            sandZoneEnd,
            BiomeType.SAND
        ));
        
        // Zone 3: Outer grass zone (far areas)
        biomeZones.add(new BiomeZone(
            sandZoneEnd,
            Float.MAX_VALUE,
            BiomeType.GRASS
        ));
    }
    
    /**
     * Generates textures for all biome types and caches them.
     * This is called during initialization to prepare all textures.
     * 
     * Requirements: 1.4 (natural variation), 2.1 (performance)
     */
    private void generateAndCacheTextures() {
        // Generate grass texture
        Texture grassTexture = textureGenerator.generateGrassTexture();
        textureCache.put(BiomeType.GRASS, grassTexture);
        
        // Generate sand texture
        Texture sandTexture = textureGenerator.generateSandTexture();
        textureCache.put(BiomeType.SAND, sandTexture);
        
        // Generate water texture
        Texture waterTexture = textureGenerator.generateWaterTexture();
        textureCache.put(BiomeType.WATER, waterTexture);
    }
    
    /**
     * Disposes of all cached textures and cleans up resources.
     * This method should be called when the BiomeManager is no longer needed,
     * typically in the game's dispose() method.
     * 
     * After calling dispose(), this BiomeManager instance should not be used again.
     * 
     * Requirements: Resource cleanup, memory management
     */
    public void dispose() {
        // Dispose all cached textures
        for (Texture texture : textureCache.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        
        textureCache.clear();
        biomeZones.clear();
        
        // Clear performance caches
        clearPerformanceCaches();
        
        initialized = false;
    }
    
    /**
     * Checks if the BiomeManager has been initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Gets the list of configured biome zones.
     * Useful for debugging and testing.
     * 
     * @return An unmodifiable view of the biome zones
     */
    public List<BiomeZone> getBiomeZones() {
        return new ArrayList<>(biomeZones);
    }
    
    // ========== PERFORMANCE TUNING AND MEASUREMENT ==========
    
    /**
     * Measures actual biome distribution over a sample area for tuning purposes.
     * This method is used to validate that the current thresholds produce the desired distribution.
     * 
     * @param sampleSize The number of random coordinates to sample
     * @param maxDistance The maximum distance from spawn to sample (0 for unlimited)
     * @return A map containing the measured distribution percentages
     */
    public Map<String, Double> measureBiomeDistribution(int sampleSize, float maxDistance) {
        Random random = new Random(12345); // Fixed seed for reproducible measurements
        
        int grassCount = 0;
        int sandCount = 0;
        int waterCount = 0;
        
        for (int i = 0; i < sampleSize; i++) {
            // Generate random coordinates
            float x, y;
            if (maxDistance > 0) {
                // Sample within a circle of maxDistance
                float angle = random.nextFloat() * 2 * (float) Math.PI;
                float distance = random.nextFloat() * maxDistance;
                x = distance * (float) Math.cos(angle);
                y = distance * (float) Math.sin(angle);
            } else {
                // Sample from a large area around spawn
                float range = 50000.0f; // 50km radius
                x = (random.nextFloat() - 0.5f) * 2 * range;
                y = (random.nextFloat() - 0.5f) * 2 * range;
            }
            
            // Get biome at this position
            BiomeType biome = getBiomeAtPosition(x, y);
            
            switch (biome) {
                case GRASS:
                    grassCount++;
                    break;
                case SAND:
                    sandCount++;
                    break;
                case WATER:
                    waterCount++;
                    break;
            }
        }
        
        // Calculate percentages
        Map<String, Double> distribution = new HashMap<>();
        distribution.put("grass", (grassCount * 100.0) / sampleSize);
        distribution.put("sand", (sandCount * 100.0) / sampleSize);
        distribution.put("water", (waterCount * 100.0) / sampleSize);
        distribution.put("sampleSize", (double) sampleSize);
        distribution.put("maxDistance", (double) maxDistance);
        
        return distribution;
    }
    
    /**
     * Measures water coverage within sand areas specifically.
     * This helps tune the WATER_IN_SAND_THRESHOLD to achieve the target 40% coverage.
     * 
     * @param sampleSize The number of sand coordinates to sample
     * @return The percentage of sand areas that contain water
     */
    public double measureWaterCoverageInSand(int sampleSize) {
        Random random = new Random(54321); // Fixed seed for reproducible measurements
        
        int sandSamples = 0;
        int waterInSandCount = 0;
        int attempts = 0;
        int maxAttempts = sampleSize * 10; // Prevent infinite loops
        
        while (sandSamples < sampleSize && attempts < maxAttempts) {
            attempts++;
            
            // Generate random coordinates
            float range = 30000.0f; // 30km radius
            float x = (random.nextFloat() - 0.5f) * 2 * range;
            float y = (random.nextFloat() - 0.5f) * 2 * range;
            
            // Only count coordinates that are in sand base biome
            BiomeType baseBiome = getBaseBiomeAtPosition(x, y);
            if (baseBiome == BiomeType.SAND) {
                sandSamples++;
                
                // Check if this sand location has water
                BiomeType finalBiome = getBiomeAtPosition(x, y);
                if (finalBiome == BiomeType.WATER) {
                    waterInSandCount++;
                }
            }
        }
        
        if (sandSamples == 0) {
            return 0.0;
        }
        
        return (waterInSandCount * 100.0) / sandSamples;
    }
    
    /**
     * Profiles biome calculation performance by measuring average calculation time.
     * This helps identify performance bottlenecks and validate optimizations.
     * 
     * @param sampleSize The number of biome calculations to time
     * @return A map containing performance metrics in nanoseconds and operations per second
     */
    public Map<String, Double> profileBiomeCalculationPerformance(int sampleSize) {
        Random random = new Random(98765); // Fixed seed for reproducible measurements
        
        // Warm up the JVM and caches
        for (int i = 0; i < 1000; i++) {
            float x = (random.nextFloat() - 0.5f) * 20000.0f;
            float y = (random.nextFloat() - 0.5f) * 20000.0f;
            getBiomeAtPosition(x, y);
        }
        
        // Clear caches to measure worst-case performance
        clearPerformanceCaches();
        
        // Measure biome calculation performance
        long startTime = System.nanoTime();
        
        for (int i = 0; i < sampleSize; i++) {
            float x = (random.nextFloat() - 0.5f) * 50000.0f;
            float y = (random.nextFloat() - 0.5f) * 50000.0f;
            getBiomeAtPosition(x, y);
        }
        
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        
        // Measure buffer validation performance specifically
        clearPerformanceCaches();
        long bufferStartTime = System.nanoTime();
        
        int bufferValidations = 0;
        for (int i = 0; i < sampleSize / 4; i++) { // Test fewer buffer validations
            float x = (random.nextFloat() - 0.5f) * 30000.0f;
            float y = (random.nextFloat() - 0.5f) * 30000.0f;
            
            // Only test buffer validation for sand areas
            if (getBaseBiomeAtPosition(x, y) == BiomeType.SAND) {
                isValidBeachBuffer(x, y);
                bufferValidations++;
            }
        }
        
        long bufferEndTime = System.nanoTime();
        long bufferTotalTime = bufferEndTime - bufferStartTime;
        
        // Calculate metrics
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("totalBiomeCalculations", (double) sampleSize);
        metrics.put("totalTimeNanos", (double) totalTime);
        metrics.put("averageTimeNanos", (double) totalTime / sampleSize);
        metrics.put("biomeCalculationsPerSecond", (sampleSize * 1_000_000_000.0) / totalTime);
        
        if (bufferValidations > 0) {
            metrics.put("bufferValidations", (double) bufferValidations);
            metrics.put("bufferTotalTimeNanos", (double) bufferTotalTime);
            metrics.put("averageBufferTimeNanos", (double) bufferTotalTime / bufferValidations);
            metrics.put("bufferValidationsPerSecond", (bufferValidations * 1_000_000_000.0) / bufferTotalTime);
        }
        
        // Include cache statistics
        Map<String, Object> cacheStats = getPerformanceStats();
        metrics.put("bufferCacheSize", ((Integer) cacheStats.get("bufferValidationCacheSize")).doubleValue());
        metrics.put("baseBiomeCacheSize", ((Integer) cacheStats.get("baseBiomeCacheSize")).doubleValue());
        
        return metrics;
    }
}
