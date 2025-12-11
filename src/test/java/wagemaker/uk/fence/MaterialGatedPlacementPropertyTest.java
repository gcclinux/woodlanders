package wagemaker.uk.fence;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import java.awt.Point;
import static org.junit.Assert.*;

/**
 * Property-based tests for material-gated placement functionality.
 * **Feature: custom-fence-building, Property 3: Material-gated placement**
 * **Validates: Requirements 2.1, 2.4, 13.5**
 */
@RunWith(JUnitQuickcheck.class)
public class MaterialGatedPlacementPropertyTest {

    /**
     * Property: For any fence placement attempt, placement should succeed if and only if 
     * sufficient materials are available.
     */
    @Property(trials = 100)
    public void placementSucceedsOnlyWithSufficientMaterials(
            @InRange(min = "0", max = "20") int availableMaterials,
            @InRange(min = "1", max = "5") int requiredMaterials,
            @InRange(min = "0", max = "10") int gridX,
            @InRange(min = "0", max = "10") int gridY) {
        
        // Create a mock material provider with the specified available materials
        MockFenceMaterialProvider materialProvider = new MockFenceMaterialProvider();
        materialProvider.setMaterialCount(FenceMaterialType.WOOD, availableMaterials);
        
        // Create validator components
        FenceGrid grid = new FenceGrid();
        FenceStructureManager structureManager = new FenceStructureManager(grid);
        FencePlacementValidator validator = new FencePlacementValidator(grid, materialProvider, structureManager);
        
        Point gridPos = new Point(gridX, gridY);
        
        // Test single piece placement
        FencePlacementValidator.ValidationResult result = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "testPlayer");
        
        // Placement should succeed if and only if we have enough materials
        boolean shouldSucceed = availableMaterials >= 1; // Single piece requires 1 material
        assertEquals("Placement validation should match material availability", 
                    shouldSucceed, result.isValid());
        
        if (!shouldSucceed) {
            assertTrue("Error message should mention insufficient materials", 
                      result.getErrorMessage().toLowerCase().contains("insufficient"));
        }
    }
    
    /**
     * Property: Material consumption should only occur after successful validation and placement.
     */
    @Property(trials = 100)
    public void materialConsumptionOnlyAfterValidation(
            @InRange(min = "0", max = "10") int initialMaterials,
            @InRange(min = "0", max = "10") int gridX,
            @InRange(min = "0", max = "10") int gridY) {
        
        // Create a mock material provider
        MockFenceMaterialProvider materialProvider = new MockFenceMaterialProvider();
        materialProvider.setMaterialCount(FenceMaterialType.WOOD, initialMaterials);
        
        // Create validator components
        FenceGrid grid = new FenceGrid();
        FenceStructureManager structureManager = new FenceStructureManager(grid);
        FencePlacementValidator validator = new FencePlacementValidator(grid, materialProvider, structureManager);
        
        Point gridPos = new Point(gridX, gridY);
        
        // Validate placement (should not consume materials)
        FencePlacementValidator.ValidationResult result = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "testPlayer");
        
        // Materials should not be consumed during validation
        assertEquals("Materials should not be consumed during validation", 
                    initialMaterials, materialProvider.getMaterialCount(FenceMaterialType.WOOD));
        
        // If validation succeeded, simulate actual placement by consuming materials
        if (result.isValid() && initialMaterials >= 1) {
            materialProvider.consumeMaterials(FenceMaterialType.WOOD, 1);
            assertEquals("Materials should be consumed after successful placement", 
                        initialMaterials - 1, materialProvider.getMaterialCount(FenceMaterialType.WOOD));
        }
    }
    
    /**
     * Property: Validation should fail consistently when materials are insufficient.
     */
    @Property(trials = 100)
    public void validationFailsConsistentlyWithInsufficientMaterials(
            @InRange(min = "0", max = "0") int availableMaterials, // Always 0 materials
            @InRange(min = "0", max = "8") int gridX,
            @InRange(min = "0", max = "8") int gridY) {
        
        // Create a mock material provider with no materials
        MockFenceMaterialProvider materialProvider = new MockFenceMaterialProvider();
        materialProvider.setMaterialCount(FenceMaterialType.WOOD, availableMaterials);
        
        // Create validator components
        FenceGrid grid = new FenceGrid();
        FenceStructureManager structureManager = new FenceStructureManager(grid);
        FencePlacementValidator validator = new FencePlacementValidator(grid, materialProvider, structureManager);
        
        Point gridPos = new Point(gridX, gridY);
        
        // Multiple validation attempts should all fail consistently
        for (int i = 0; i < 3; i++) {
            FencePlacementValidator.ValidationResult result = 
                validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "testPlayer");
            
            assertFalse("Validation should fail with insufficient materials (attempt " + (i + 1) + ")", 
                       result.isValid());
            assertNotNull("Error message should be provided", result.getErrorMessage());
            assertTrue("Error message should mention insufficient materials", 
                      result.getErrorMessage().toLowerCase().contains("insufficient"));
        }
    }
    
    /**
     * Property: Different material types should be validated independently.
     */
    @Property(trials = 100)
    public void differentMaterialTypesValidatedIndependently(
            @InRange(min = "0", max = "10") int woodCount,
            @InRange(min = "0", max = "10") int bambooCount,
            @InRange(min = "0", max = "8") int gridX,
            @InRange(min = "0", max = "8") int gridY) {
        
        // Create a mock material provider with different amounts of each material
        MockFenceMaterialProvider materialProvider = new MockFenceMaterialProvider();
        materialProvider.setMaterialCount(FenceMaterialType.WOOD, woodCount);
        materialProvider.setMaterialCount(FenceMaterialType.BAMBOO, bambooCount);
        
        // Create validator components
        FenceGrid grid = new FenceGrid();
        FenceStructureManager structureManager = new FenceStructureManager(grid);
        FencePlacementValidator validator = new FencePlacementValidator(grid, materialProvider, structureManager);
        
        Point gridPos = new Point(gridX, gridY);
        
        // Test wood placement
        FencePlacementValidator.ValidationResult woodResult = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "testPlayer");
        boolean woodShouldSucceed = woodCount >= 1;
        assertEquals("Wood placement validation should match wood availability", 
                    woodShouldSucceed, woodResult.isValid());
        
        // Test bamboo placement at a different position (to avoid position conflicts)
        Point bambooPos = new Point(gridX + 1, gridY);
        FencePlacementValidator.ValidationResult bambooResult = 
            validator.validatePlacement(bambooPos, FenceMaterialType.BAMBOO, "testPlayer");
        boolean bambooShouldSucceed = bambooCount >= 1;
        assertEquals("Bamboo placement validation should match bamboo availability", 
                    bambooShouldSucceed, bambooResult.isValid());
    }
    
    /**
     * Property: Occupied positions should be rejected regardless of material availability.
     */
    @Property(trials = 100)
    public void occupiedPositionsRejectedRegardlessOfMaterials(
            @InRange(min = "1", max = "20") int availableMaterials,
            @InRange(min = "0", max = "8") int gridX,
            @InRange(min = "0", max = "8") int gridY) {
        
        // Create a mock material provider with plenty of materials
        MockFenceMaterialProvider materialProvider = new MockFenceMaterialProvider();
        materialProvider.setMaterialCount(FenceMaterialType.WOOD, availableMaterials);
        
        // Create validator components
        FenceGrid grid = new FenceGrid();
        FenceStructureManager structureManager = new FenceStructureManager(grid);
        FencePlacementValidator validator = new FencePlacementValidator(grid, materialProvider, structureManager);
        
        Point gridPos = new Point(gridX, gridY);
        
        // Mark the position as occupied
        grid.setOccupied(gridPos);
        
        // Validation should fail even with sufficient materials
        FencePlacementValidator.ValidationResult result = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "testPlayer");
        
        assertFalse("Placement should fail on occupied positions regardless of materials", 
                   result.isValid());
        assertTrue("Error message should mention occupied position", 
                  result.getErrorMessage().toLowerCase().contains("occupied"));
    }
    
    /**
     * Property: Blocked positions should be rejected regardless of material availability.
     */
    @Property(trials = 100)
    public void blockedPositionsRejectedRegardlessOfMaterials(
            @InRange(min = "1", max = "20") int availableMaterials,
            @InRange(min = "0", max = "8") int gridX,
            @InRange(min = "0", max = "8") int gridY) {
        
        // Create a mock material provider with plenty of materials
        MockFenceMaterialProvider materialProvider = new MockFenceMaterialProvider();
        materialProvider.setMaterialCount(FenceMaterialType.WOOD, availableMaterials);
        
        // Create validator components
        FenceGrid grid = new FenceGrid();
        FenceStructureManager structureManager = new FenceStructureManager(grid);
        FencePlacementValidator validator = new FencePlacementValidator(grid, materialProvider, structureManager);
        
        Point gridPos = new Point(gridX, gridY);
        
        // Mark the position as blocked
        grid.setBlocked(gridPos);
        
        // Validation should fail even with sufficient materials
        FencePlacementValidator.ValidationResult result = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "testPlayer");
        
        assertFalse("Placement should fail on blocked positions regardless of materials", 
                   result.isValid());
        assertTrue("Error message should mention blocked or invalid position", 
                  result.getErrorMessage().toLowerCase().contains("blocked") || 
                  result.getErrorMessage().toLowerCase().contains("invalid"));
    }
    
    /**
     * Mock implementation of FenceMaterialProvider for testing.
     */
    private static class MockFenceMaterialProvider implements FenceMaterialProvider {
        private int woodCount = 0;
        private int bambooCount = 0;
        
        public void setMaterialCount(FenceMaterialType materialType, int count) {
            switch (materialType) {
                case WOOD:
                    woodCount = count;
                    break;
                case BAMBOO:
                    bambooCount = count;
                    break;
            }
        }
        
        @Override
        public boolean hasEnoughMaterials(FenceMaterialType materialType, int count) {
            return getMaterialCount(materialType) >= count;
        }
        
        @Override
        public void consumeMaterials(FenceMaterialType materialType, int count) {
            if (!hasEnoughMaterials(materialType, count)) {
                throw new IllegalStateException("Not enough materials to consume");
            }
            
            switch (materialType) {
                case WOOD:
                    woodCount -= count;
                    break;
                case BAMBOO:
                    bambooCount -= count;
                    break;
            }
        }
        
        @Override
        public void returnMaterials(FenceMaterialType materialType, int count) {
            switch (materialType) {
                case WOOD:
                    woodCount += count;
                    break;
                case BAMBOO:
                    bambooCount += count;
                    break;
            }
        }
        
        @Override
        public int getMaterialCount(FenceMaterialType materialType) {
            switch (materialType) {
                case WOOD:
                    return woodCount;
                case BAMBOO:
                    return bambooCount;
                default:
                    return 0;
            }
        }
    }
}