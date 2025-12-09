# Implementation Plan

- [x] 1. Extend BiomeType enumeration with WATER constant
  - Add WATER to the BiomeType enum in BiomeType.java
  - Verify getDisplayName() returns "water" for the new type
  - _Requirements: 5.1_

- [x] 2. Add water configuration to BiomeConfig
  - Add TEXTURE_SEED_WATER constant (value: 98765)
  - Add WATER_BASE_COLOR constant (deep blue: 0.1, 0.3, 0.6, 1.0)
  - Add WATER_LIGHT_COLOR constant (light blue: 0.3, 0.5, 0.8, 1.0)
  - Add WATER_DARK_COLOR constant (dark blue: 0.05, 0.2, 0.4, 1.0)
  - Add WATER_NOISE_THRESHOLD constant (value: 0.75 for ~15% coverage)
  - _Requirements: 1.4, 5.4_

- [x] 3. Implement water texture generation in BiomeTextureGenerator

  - Create generateWaterTexture() method
  - Implement addWaterWaves() helper method for wave patterns
  - Implement addWaterReflections() helper method for surface reflections
  - Implement addWaterDepth() helper method for depth variations
  - Use BiomeConfig water color constants
  - _Requirements: 1.1, 1.3_

- [x] 3.1 Write property test for water texture generation

  - **Property 2: Water color distinctiveness**
  - **Validates: Requirements 1.3**

- [-] 4. Extend BiomeManager to support water biome generation

  - Implement isInWaterPatch() method using multi-octave noise
  - Update getBiomeAtPosition() to check for water first (priority: Water > Sand > Grass)
  - Add water texture generation to generateAndCacheTextures()
  - Ensure water doesn't spawn within 1500px of spawn point
  - _Requirements: 1.2, 1.4, 1.5, 4.1_

- [x] 4.1 Write property test for biome type exhaustiveness


  - **Property 1: Biome type exhaustiveness**
  - **Validates: Requirements 1.2**

- [x] 4.2 Write property test for biome distribution


  - **Property 3: Biome distribution convergence**
  - **Validates: Requirements 1.4**

- [x] 4.3 Write property test for water contiguity


  - **Property 4: Water contiguity**
  - **Validates: Requirements 1.5**

- [x] 4.4 Write property test for biome determinism

  - **Property 10: Biome calculation determinism**
  - **Validates: Requirements 4.1, 4.2, 4.4**

- [x] 5. Checkpoint - Ensure all tests pass

  - Ensure all tests pass, ask the user if questions arise.

- [x] 6. Implement water collision detection in Player class



  - Extend wouldCollide() method to check biome type at target position
  - Use player center point (x+32, y+32) for biome lookup
  - Return true if biome type is WATER to block movement
  - _Requirements: 2.1, 2.2, 2.3_

- [x] 6.1 Write property test for water collision blocking


  - **Property 5: Water collision blocking**
  - **Validates: Requirements 2.1, 2.3**

- [x] 6.2 Write property test for collision consistency


  - **Property 6: Collision consistency**
  - **Validates: Requirements 2.4**

- [x] 6.3 Write property test for adjacent movement freedom


  - **Property 7: Adjacent movement freedom**
  - **Validates: Requirements 2.5**

- [x] 7. Add biome validation to tree spawning system

  - Locate tree spawn logic in MyGdxGame or relevant class
  - Add isValidSpawnLocation() check before spawning trees
  - Verify biome type is not WATER before spawning
  - Implement retry logic to find alternative location if water detected
  - _Requirements: 3.1, 3.5_

- [x] 8. Add biome validation to stone/rock spawning system

  - Locate stone spawn logic in MyGdxGame or relevant class
  - Add biome type check before spawning stones
  - Verify biome type is not WATER before spawning
  - Implement retry logic to find alternative location if water detected
  - _Requirements: 3.2, 3.5_

- [x] 9. Add biome validation to item spawning system

  - Locate item spawn logic (apples, saplings, wood stacks, etc.)
  - Add biome type check before spawning items
  - Verify biome type is not WATER before spawning
  - Implement retry logic to find alternative location if water detected
  - _Requirements: 3.3, 3.5_

- [x] 10. Add biome validation to puddle creation system

  - Locate puddle creation logic in PuddleManager
  - Add biome type check before creating puddles
  - Verify biome type is not WATER before creating puddles
  - Skip puddle creation if location is water
  - _Requirements: 3.4_

- [x] 10.1 Write property test for resource spawn exclusion


  - **Property 8: Resource spawn exclusion**
  - **Validates: Requirements 3.1, 3.2, 3.3, 3.4**

- [x] 10.2 Write property test for spawn retry success


  - **Property 9: Spawn retry success**
  - **Validates: Requirements 3.5**

- [x] 11. Checkpoint - Ensure all tests pass


  - Ensure all tests pass, ask the user if questions arise.

- [x] 12. Write integration tests for complete feature

  - Test multiplayer synchronization (two clients see identical water locations)
  - Test resource spawning (verify no resources in water over 1000 attempts)
  - Test player movement (verify cannot walk into water, can walk parallel)
  - Test puddle system (verify no puddles in water during rain)
  - _Requirements: 1.1, 2.1, 3.1, 3.2, 3.3, 3.4, 4.1_

- [x] 13. Write unit tests for backward compatibility

  - Verify grass biome still functions correctly
  - Verify sand biome still functions correctly
  - Verify existing biome zones remain unchanged
  - _Requirements: 5.4, 5.5_

- [x] 14. Tune biome distribution thresholds

  - Run distribution measurement with current thresholds
  - Adjust WATER_NOISE_THRESHOLD if needed to achieve ~15% water coverage
  - Verify sand threshold (0.45) still produces ~35% sand coverage
  - Document final threshold values in BiomeConfig
  - _Requirements: 1.4_

- [x] 15. Final checkpoint - Ensure all tests pass
  - Ensure all tests pass, ask the user if questions arise.
