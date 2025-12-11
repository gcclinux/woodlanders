package wagemaker.uk.fence;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import java.awt.Point;

/**
 * Unit tests for fence placement and removal operations.
 * Tests placement validation logic, material consumption and return,
 * and collision boundary updates.
 * 
 * Note: These tests focus on the logic and validation aspects rather than
 * actual fence piece creation to avoid LibGDX graphics dependencies.
 */
public class FencePlacementRemovalOperationsTest {
    
    private FencePlacementValidator validator;
    private FenceMaterialProvider materialProvider;
    private FenceGrid grid;
    
    @Before
    public void setUp() {
        // Create real components
        grid = new FenceGrid();
        FenceStructureManager structureManager = new FenceStructureManager(grid);
        
        // Create mock material provider
        materialProvider = mock(FenceMaterialProvider.class);
        
        // Create validator with mock material provider
        validator = new FencePlacementValidator(grid, materialProvider, structureManager);
    }
    
    @Test
    public void testPlacementValidationWithSufficientMaterials() {
        // Arrange
        Point gridPos = new Point(5, 5);
        when(materialProvider.hasEnoughMaterials(FenceMaterialType.WOOD, 1)).thenReturn(true);
        
        // Act
        FencePlacementValidator.ValidationResult result = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "test_player");
        
        // Assert
        assertTrue("Placement validation should succeed with valid conditions", result.isValid());
        assertNull("No error message should be present for valid placement", result.getErrorMessage());
    }
    
    @Test
    public void testPlacementValidationWithInsufficientMaterials() {
        // Arrange
        Point gridPos = new Point(3, 7);
        when(materialProvider.hasEnoughMaterials(FenceMaterialType.WOOD, 1)).thenReturn(false);
        when(materialProvider.getMaterialCount(FenceMaterialType.WOOD)).thenReturn(0);
        
        // Act
        FencePlacementValidator.ValidationResult result = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "test_player");
        
        // Assert
        assertFalse("Placement validation should fail with insufficient materials", result.isValid());
        assertNotNull("Error message should be present for invalid placement", result.getErrorMessage());
        assertTrue("Error message should mention insufficient materials", 
                  result.getErrorMessage().contains("Insufficient"));
    }
    
    @Test
    public void testPlacementValidationOnOccupiedPosition() {
        // Arrange
        Point gridPos = new Point(2, 4);
        when(materialProvider.hasEnoughMaterials(FenceMaterialType.WOOD, 1)).thenReturn(true);
        
        // Mark position as occupied
        grid.setOccupied(gridPos);
        
        // Act - try to validate placement at occupied position
        FencePlacementValidator.ValidationResult result = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "test_player");
        
        // Assert
        assertFalse("Placement validation should fail on occupied position", result.isValid());
        assertNotNull("Error message should be present for occupied position", result.getErrorMessage());
        assertTrue("Error message should mention occupied position", 
                  result.getErrorMessage().contains("occupied"));
    }
    
    @Test
    public void testRemovalValidationWithExistingFence() {
        // Arrange
        Point gridPos = new Point(8, 3);
        
        // Mark position as occupied (simulating existing fence)
        grid.setOccupied(gridPos);
        
        // Act
        FencePlacementValidator.ValidationResult result = 
            validator.validateRemoval(gridPos, "test_player");
        
        // Assert
        assertTrue("Removal validation should succeed with existing fence", result.isValid());
        assertNull("No error message should be present for valid removal", result.getErrorMessage());
    }
    
    @Test
    public void testRemovalValidationFromEmptyPosition() {
        // Arrange
        Point gridPos = new Point(1, 9);
        // Position is not occupied (default state)
        
        // Act
        FencePlacementValidator.ValidationResult result = 
            validator.validateRemoval(gridPos, "test_player");
        
        // Assert
        assertFalse("Removal validation should fail from empty position", result.isValid());
        assertNotNull("Error message should be present for empty position removal", result.getErrorMessage());
        assertTrue("Error message should mention no fence piece exists", 
                  result.getErrorMessage().contains("No fence piece exists"));
    }
    
    @Test
    public void testMaterialAvailabilityChecking() {
        // Arrange
        Point gridPos = new Point(6, 2);
        
        // Test with sufficient materials
        when(materialProvider.hasEnoughMaterials(FenceMaterialType.WOOD, 1)).thenReturn(true);
        FencePlacementValidator.ValidationResult result1 = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "test_player");
        assertTrue("Should validate successfully with sufficient materials", result1.isValid());
        
        // Test with insufficient materials
        when(materialProvider.hasEnoughMaterials(FenceMaterialType.WOOD, 1)).thenReturn(false);
        when(materialProvider.getMaterialCount(FenceMaterialType.WOOD)).thenReturn(0);
        FencePlacementValidator.ValidationResult result2 = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "test_player");
        assertFalse("Should fail validation with insufficient materials", result2.isValid());
        
        // Verify material provider was called
        verify(materialProvider, times(2)).hasEnoughMaterials(FenceMaterialType.WOOD, 1);
    }
    
    @Test
    public void testGridPositionManagement() {
        // Arrange
        Point gridPos1 = new Point(4, 6);
        Point gridPos2 = new Point(5, 6);
        
        // Initially both positions should be unoccupied
        assertFalse("Position 1 should initially be unoccupied", grid.isOccupied(gridPos1));
        assertFalse("Position 2 should initially be unoccupied", grid.isOccupied(gridPos2));
        
        // Act - occupy positions
        grid.setOccupied(gridPos1);
        grid.setOccupied(gridPos2);
        
        // Assert - both positions are occupied
        assertTrue("Position 1 should be occupied", grid.isOccupied(gridPos1));
        assertTrue("Position 2 should be occupied", grid.isOccupied(gridPos2));
        
        // Act - free first position
        grid.setUnoccupied(gridPos1);
        
        // Assert - only second position remains occupied
        assertFalse("Position 1 should be unoccupied", grid.isOccupied(gridPos1));
        assertTrue("Position 2 should remain occupied", grid.isOccupied(gridPos2));
    }
    
    @Test
    public void testValidationWithDifferentMaterialTypes() {
        // Arrange
        Point gridPos = new Point(0, 0);
        
        // Test wood material validation
        when(materialProvider.hasEnoughMaterials(FenceMaterialType.WOOD, 1)).thenReturn(true);
        FencePlacementValidator.ValidationResult woodResult = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "test_player");
        assertTrue("Wood placement should validate successfully", woodResult.isValid());
        
        // Test bamboo material validation
        when(materialProvider.hasEnoughMaterials(FenceMaterialType.BAMBOO, 1)).thenReturn(true);
        FencePlacementValidator.ValidationResult bambooResult = 
            validator.validatePlacement(gridPos, FenceMaterialType.BAMBOO, "test_player");
        assertTrue("Bamboo placement should validate successfully", bambooResult.isValid());
        
        // Verify both material types were checked
        verify(materialProvider).hasEnoughMaterials(FenceMaterialType.WOOD, 1);
        verify(materialProvider).hasEnoughMaterials(FenceMaterialType.BAMBOO, 1);
    }
    
    @Test
    public void testMultipleValidationOperations() {
        // Arrange
        Point[] positions = {
            new Point(1, 1), new Point(2, 1), new Point(3, 1),
            new Point(1, 2), new Point(2, 2), new Point(3, 2)
        };
        when(materialProvider.hasEnoughMaterials(FenceMaterialType.WOOD, 1)).thenReturn(true);
        
        // Act - validate placement for multiple positions
        for (Point pos : positions) {
            FencePlacementValidator.ValidationResult result = 
                validator.validatePlacement(pos, FenceMaterialType.WOOD, "test_player");
            assertTrue("Each placement validation should succeed for position " + pos, result.isValid());
            
            // Simulate placing by marking position as occupied
            grid.setOccupied(pos);
        }
        
        // Act - validate removal for half the positions
        int toRemove = positions.length / 2;
        for (int i = 0; i < toRemove; i++) {
            Point pos = positions[i];
            FencePlacementValidator.ValidationResult result = 
                validator.validateRemoval(pos, "test_player");
            assertTrue("Each removal validation should succeed for position " + pos, result.isValid());
            
            // Simulate removing by marking position as unoccupied
            grid.setUnoccupied(pos);
        }
        
        // Verify final state
        int expectedOccupied = positions.length - toRemove;
        int actualOccupied = 0;
        for (Point pos : positions) {
            if (grid.isOccupied(pos)) {
                actualOccupied++;
            }
        }
        assertEquals("Should have correct number of occupied positions", 
                    expectedOccupied, actualOccupied);
        
        // Verify material provider was called for each validation
        verify(materialProvider, times(positions.length)).hasEnoughMaterials(FenceMaterialType.WOOD, 1);
    }
    
    @Test
    public void testValidatorOwnershipSettings() {
        // Arrange
        Point gridPos = new Point(7, 8);
        when(materialProvider.hasEnoughMaterials(FenceMaterialType.WOOD, 1)).thenReturn(true);
        
        // Test with ownership validation disabled (default)
        assertFalse("Ownership validation should be disabled by default", 
                   validator.isOwnershipValidationEnabled());
        
        FencePlacementValidator.ValidationResult result1 = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "player1");
        assertTrue("Placement should succeed with ownership validation disabled", result1.isValid());
        
        // Test enabling ownership validation
        validator.setOwnershipValidationEnabled(true);
        assertTrue("Ownership validation should be enabled", 
                  validator.isOwnershipValidationEnabled());
        
        FencePlacementValidator.ValidationResult result2 = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, "player2");
        assertTrue("Placement should still succeed with valid player ID", result2.isValid());
        
        // Test with null player ID when ownership validation is enabled
        FencePlacementValidator.ValidationResult result3 = 
            validator.validatePlacement(gridPos, FenceMaterialType.WOOD, null);
        assertFalse("Placement should fail with null player ID when ownership validation is enabled", 
                   result3.isValid());
    }
}