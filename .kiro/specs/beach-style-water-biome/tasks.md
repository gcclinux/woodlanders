# Implementation Plan

- [x] 1. Set up beach-style biome configuration

  - Add new BiomeConfig constants for beach-style generation (SAND_BASE_THRESHOLD, WATER_IN_SAND_THRESHOLD, BEACH_BUFFER_DISTANCE, etc.)
  - Add new noise seeds for base biome and water-in-sand generation
  - Maintain backward compatibility with existing water texture and color constants
  - _Requirements: 1.1, 2.1, 3.1_

- [x] 1.1 Write property test for biome configuration validation

  - **Property: Configuration constants validation**
  - **Validates: Requirements 5.4**

- [x] 2. Implement base biome calculation system

  - Create getBaseBiomeAtPosition() method for grass/sand determination using 50/50 distribution
  - Initialize new SimplexNoise generators for base biome calculation
  - Implement noise-based grass/sand distribution beyond spawn zones
  - _Requirements: 2.1, 2.3_

- [x] 2.1 Write property test for base biome distribution

  - **Property 6: Base biome distribution**
  - **Validates: Requirements 2.1**

- [x] 3. Implement buffer zone validation system

  - Create isValidBeachBuffer() method to check 64px radius around potential water locations
  - Implement calculateDistanceToGrass() method for debugging and validation
  - Add circular sampling logic to detect grass biomes within buffer zone
  - _Requirements: 1.3, 3.1_

- [x] 3.1 Write property test for buffer zone validation

  - **Property 3: Water maintains buffer distance from grass**
  - **Validates: Requirements 1.3**

- [x] 4. Implement water-in-sand placement system

  - Create isEligibleForWater() method combining buffer validation and noise threshold
  - Add water-in-sand noise generation for 40% coverage within sand areas
  - Implement water placement logic that only operates within sand base biomes
  - _Requirements: 1.1, 1.4, 1.5_

- [x] 4.1 Write property test for water-only-in-sand constraint

  - **Property 1: Water only spawns in sand areas**
  - **Validates: Requirements 1.1**

- [x] 4.2 Write property test for water coverage in sand areas

  - **Property 4: Water coverage in sand areas**
  - **Validates: Requirements 1.4**

- [x] 4.3 Write property test for water contiguity

  - **Property 5: Water contiguity in sand**
  - **Validates: Requirements 1.5**

- [x] 5. Integrate two-phase biome calculation

  - Replace existing getBiomeAtPosition() with two-phase calculation (base biome + water overlay)
  - Ensure water can only appear where base biome is sand
  - Maintain existing biome type enumeration and texture handling
  - _Requirements: 1.1, 1.2, 2.5_

- [x] 5.1 Write property test for biome type exhaustiveness


  - **Property 2: Biome type exhaustiveness**
  - **Validates: Requirements 1.2**

- [x] 5.2 Write property test for final biome distribution


  - **Property 7: Final biome distribution**
  - **Validates: Requirements 2.3**

- [x] 6. Checkpoint - Ensure all tests pass

  - Ensure all tests pass, ask the user if questions arise.

- [x] 7. Validate backward compatibility with existing systems

  - Verify collision detection still works with beach-style water placement
  - Verify resource spawning continues to avoid water areas regardless of location
  - Verify puddle system continues to avoid water areas
  - Verify multiplayer synchronization maintains deterministic biome calculation
  - _Requirements: 4.1, 4.2, 4.3, 4.4_

- [x] 7.1 Write property test for water collision blocking

  - **Property 8: Water collision blocking**
  - **Validates: Requirements 4.1**

- [x] 7.2 Write property test for resource spawn exclusion

  - **Property 9: Resource spawn exclusion from water**
  - **Validates: Requirements 4.2**

- [x] 7.3 Write property test for puddle exclusion

  - **Property 10: Puddle exclusion from water**
  - **Validates: Requirements 4.3**

- [x] 7.4 Write property test for biome calculation determinism

  - **Property 11: Biome calculation determinism**
  - **Validates: Requirements 4.4**

- [x] 8. Performance optimization and tuning

  - Implement spatial caching for buffer zone validation results
  - Add early-exit optimizations for buffer zone checking
  - Tune noise thresholds based on actual distribution measurements
  - Profile biome calculation performance with new two-phase system
  - _Requirements: 1.4, 2.3_

- [x] 8.1 Write unit tests for performance optimizations

  - Create unit tests for spatial caching logic
  - Write unit tests for early-exit buffer validation
  - Test threshold tuning with known coordinate sets
  - _Requirements: 1.4, 2.3_

- [x] 9. Integration testing and validation

  - Test beach formation over large map sections
  - Validate biome distribution convergence with increasing sample sizes
  - Verify multiplayer clients see identical beach layouts
  - Test system performance under typical gameplay loads
  - _Requirements: 1.5, 2.3, 4.4_

- [x] 9.1 Write integration tests for beach formation

  - Create integration test for realistic coastline formation
  - Write integration test for distribution convergence
  - Test multiplayer synchronization of beach layouts
  - _Requirements: 1.5, 2.3, 4.4_

- [x] 10. Final checkpoint - Ensure all tests pass





  - Ensure all tests pass, ask the user if questions arise.