package wagemaker.uk.biome;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BiomeType enum.
 * Verifies that all biome types are defined and have correct display names.
 * 
 * Requirements: 5.1 (WATER biome type extension)
 */
public class BiomeTypeTest {
    
    @Test
    public void testGrassDisplayName() {
        assertEquals("grass", BiomeType.GRASS.getDisplayName(), 
            "GRASS display name should be 'grass'");
    }
    
    @Test
    public void testSandDisplayName() {
        assertEquals("sand", BiomeType.SAND.getDisplayName(), 
            "SAND display name should be 'sand'");
    }
    
    @Test
    public void testWaterDisplayName() {
        assertEquals("water", BiomeType.WATER.getDisplayName(), 
            "WATER display name should be 'water'");
    }
    
    @Test
    public void testAllBiomeTypesExist() {
        BiomeType[] types = BiomeType.values();
        assertEquals(3, types.length, "Should have exactly 3 biome types");
        
        // Verify all expected types exist
        boolean hasGrass = false;
        boolean hasSand = false;
        boolean hasWater = false;
        
        for (BiomeType type : types) {
            if (type == BiomeType.GRASS) hasGrass = true;
            if (type == BiomeType.SAND) hasSand = true;
            if (type == BiomeType.WATER) hasWater = true;
        }
        
        assertTrue(hasGrass, "GRASS biome type should exist");
        assertTrue(hasSand, "SAND biome type should exist");
        assertTrue(hasWater, "WATER biome type should exist");
    }
    
    @Test
    public void testWaterConstantExists() {
        assertNotNull(BiomeType.WATER, "WATER constant should exist");
    }
    
    @Test
    public void testBiomeTypeEnumOrder() {
        BiomeType[] types = BiomeType.values();
        assertEquals(BiomeType.GRASS, types[0], "First biome type should be GRASS");
        assertEquals(BiomeType.SAND, types[1], "Second biome type should be SAND");
        assertEquals(BiomeType.WATER, types[2], "Third biome type should be WATER");
    }
}
