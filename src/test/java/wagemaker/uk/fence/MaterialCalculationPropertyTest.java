package wagemaker.uk.fence;

import com.badlogic.gdx.math.Rectangle;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for material calculation accuracy in fence building.
 * 
 * **Feature: custom-fence-building, Property 9: Material calculation accuracy**
 * **Validates: Requirements 14.1, 14.5**
 */
@RunWith(JUnitQuickcheck.class)
public class MaterialCalculationPropertyTest {
    
    /**
     * Property 9: Material calculation accuracy
     * For any rectangular area definition, the calculated material requirements 
     * should match the actual pieces needed for construction.
     * 
     * This property tests that:
     * 1. Material calculation matches the perimeter formula: 2 * (width + height - 2)
     * 2. The number of pieces in the enclosure sequence equals the calculated requirement
     * 3. Material calculation is consistent across different rectangle sizes
     * 4. Edge cases (minimum size rectangles) are handled correctly
     */
    @Property(trials = 100)
    public void materialCalculationAccuracyProperty(
            @InRange(minInt = 2, maxInt = 50) int width,
            @InRange(minInt = 2, maxInt = 50) int height) {
        
        // Test material calculation using Rectangle bounds
        Rectangle bounds = new Rectangle(0, 0, width, height);
        int calculatedMaterials = FencePieceFactory.calculateMaterialRequirement(bounds);
        
        // Test material calculation using width/height directly
        int calculatedMaterialsDirect = FencePieceFactory.calculateMaterialRequirement(width, height);
        
        // Both methods should give the same result
        assertEquals(calculatedMaterials, calculatedMaterialsDirect,
            "Material calculation should be consistent between Rectangle and direct methods");
        
        // Test against the mathematical formula for rectangle perimeter
        int expectedMaterials = 2 * (width + height) - 4;
        assertEquals(expectedMaterials, calculatedMaterials,
            "Material calculation should match perimeter formula: 2 * (width + height) - 4");
        
        // Test that the enclosure sequence length matches the calculated requirement
        FencePieceType[] sequence = FencePieceFactory.getEnclosureSequence(bounds);
        assertEquals(calculatedMaterials, sequence.length,
            "Enclosure sequence length should match calculated material requirement");
        
        // Verify the sequence is valid for the given dimensions
        assertTrue(FencePieceFactory.validateEnclosureSequence(sequence, width, height),
            "Generated sequence should be valid for the given dimensions");
        
        // Test corner and edge piece counts
        int expectedCorners = 4; // Always 4 corners for any rectangle
        int expectedEdges = calculatedMaterials - 4; // Remaining pieces are edges
        
        int actualCorners = 0;
        int actualEdges = 0;
        
        for (FencePieceType type : sequence) {
            if (type.isCornerPiece()) {
                actualCorners++;
            } else if (type.isEdgePiece()) {
                actualEdges++;
            }
        }
        
        assertEquals(expectedCorners, actualCorners,
            "Should have exactly 4 corner pieces regardless of rectangle size");
        assertEquals(expectedEdges, actualEdges,
            "Should have correct number of edge pieces");
        
        // Test that material calculation is monotonic (larger rectangles need more materials)
        if (width > 2 || height > 2) {
            int smallerMaterials = FencePieceFactory.calculateMaterialRequirement(2, 2);
            assertTrue(calculatedMaterials >= smallerMaterials,
                "Larger rectangles should require at least as many materials as smaller ones");
        }
    }
    
    /**
     * Property test for edge cases and boundary conditions in material calculation.
     */
    @Property(trials = 100)
    public void materialCalculationEdgeCasesProperty() {
        // Test minimum size rectangle (2x2)
        int minMaterials = FencePieceFactory.calculateMaterialRequirement(2, 2);
        assertEquals(4, minMaterials, "2x2 rectangle should require exactly 4 pieces");
        
        // Test that sequence for minimum rectangle contains all corner pieces
        FencePieceType[] minSequence = FencePieceFactory.getEnclosureSequence(new Rectangle(0, 0, 2, 2));
        assertEquals(4, minSequence.length, "2x2 rectangle sequence should have 4 pieces");
        
        int corners = 0;
        int edges = 0;
        for (FencePieceType type : minSequence) {
            if (type.isCornerPiece()) corners++;
            if (type.isEdgePiece()) edges++;
        }
        assertEquals(4, corners, "2x2 rectangle should have 4 corner pieces");
        assertEquals(0, edges, "2x2 rectangle should have 0 edge pieces");
        
        // Test specific known cases
        assertEquals(8, FencePieceFactory.calculateMaterialRequirement(3, 3), 
            "3x3 rectangle should require 8 pieces");
        assertEquals(12, FencePieceFactory.calculateMaterialRequirement(4, 4), 
            "4x4 rectangle should require 12 pieces");
        assertEquals(10, FencePieceFactory.calculateMaterialRequirement(3, 4), 
            "3x4 rectangle should require 10 pieces");
        
        // Test rectangular vs square requirements
        int square4x4 = FencePieceFactory.calculateMaterialRequirement(4, 4);
        int rect2x6 = FencePieceFactory.calculateMaterialRequirement(2, 6);
        assertEquals(square4x4, rect2x6, 
            "4x4 square and 2x6 rectangle should require same materials (both perimeter 12)");
    }
    
    /**
     * Property test for material calculation consistency with piece type determination.
     */
    @Property(trials = 100)
    public void materialCalculationConsistencyProperty(
            @InRange(minInt = 2, maxInt = 20) int width,
            @InRange(minInt = 2, maxInt = 20) int height) {
        
        int totalMaterials = FencePieceFactory.calculateMaterialRequirement(width, height);
        
        // Count pieces by iterating through all perimeter positions
        int countedPieces = 0;
        
        // Top edge
        for (int x = 0; x < width; x++) {
            FencePieceType type = FencePieceFactory.determinePieceTypeForPosition(x, 0, width, height);
            assertNotNull(type, "Should determine valid piece type for top edge position");
            countedPieces++;
        }
        
        // Right edge (excluding corners already counted)
        for (int y = 1; y < height - 1; y++) {
            FencePieceType type = FencePieceFactory.determinePieceTypeForPosition(width - 1, y, width, height);
            assertNotNull(type, "Should determine valid piece type for right edge position");
            countedPieces++;
        }
        
        // Bottom edge (excluding corners already counted)
        for (int x = width - 1; x >= 0; x--) {
            FencePieceType type = FencePieceFactory.determinePieceTypeForPosition(x, height - 1, width, height);
            assertNotNull(type, "Should determine valid piece type for bottom edge position");
            countedPieces++;
        }
        
        // Left edge (excluding corners already counted)
        for (int y = height - 2; y > 0; y--) {
            FencePieceType type = FencePieceFactory.determinePieceTypeForPosition(0, y, width, height);
            assertNotNull(type, "Should determine valid piece type for left edge position");
            countedPieces++;
        }
        
        assertEquals(totalMaterials, countedPieces,
            "Material calculation should match the count of perimeter positions");
    }
    
    /**
     * Property test for invalid input handling in material calculation.
     */
    @Property(trials = 50)
    public void materialCalculationInvalidInputProperty(
            @InRange(minInt = -10, maxInt = 1) int invalidWidth,
            @InRange(minInt = -10, maxInt = 1) int invalidHeight) {
        
        // Test that invalid dimensions throw appropriate exceptions
        assertThrows(IllegalArgumentException.class, () -> {
            FencePieceFactory.calculateMaterialRequirement(invalidWidth, 3);
        }, "Should throw exception for invalid width");
        
        assertThrows(IllegalArgumentException.class, () -> {
            FencePieceFactory.calculateMaterialRequirement(3, invalidHeight);
        }, "Should throw exception for invalid height");
        
        assertThrows(IllegalArgumentException.class, () -> {
            FencePieceFactory.calculateMaterialRequirement(invalidWidth, invalidHeight);
        }, "Should throw exception for both invalid dimensions");
        
        // Test Rectangle-based method with invalid bounds
        Rectangle invalidBounds = new Rectangle(0, 0, invalidWidth, invalidHeight);
        assertThrows(IllegalArgumentException.class, () -> {
            FencePieceFactory.calculateMaterialRequirement(invalidBounds);
        }, "Should throw exception for invalid Rectangle bounds");
    }
}