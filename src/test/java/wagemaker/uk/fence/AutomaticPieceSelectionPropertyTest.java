package wagemaker.uk.fence;

import com.badlogic.gdx.math.Rectangle;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import java.awt.Point;
import static org.junit.Assert.*;

/**
 * Property-based tests for automatic fence piece selection correctness.
 * **Feature: custom-fence-building, Property 5: Automatic piece selection correctness**
 * **Validates: Requirements 2.2, 4.1, 5.2, 14.2**
 */
@RunWith(JUnitQuickcheck.class)
public class AutomaticPieceSelectionPropertyTest {

    /**
     * Property: Corner pieces should be correctly identified at rectangle corners.
     */
    @Property(trials = 100)
    public void cornerPiecesAreCorrectlyIdentified(
            @InRange(min = "2", max = "10") int width,
            @InRange(min = "2", max = "10") int height) {
        
        // Test all four corners
        FencePieceType topLeft = FencePieceFactory.determinePieceTypeForPosition(0, 0, width, height);
        FencePieceType topRight = FencePieceFactory.determinePieceTypeForPosition(width - 1, 0, width, height);
        FencePieceType bottomRight = FencePieceFactory.determinePieceTypeForPosition(width - 1, height - 1, width, height);
        FencePieceType bottomLeft = FencePieceFactory.determinePieceTypeForPosition(0, height - 1, width, height);
        
        // Verify corner pieces are correct
        assertEquals("Top-left corner should be FENCE_BACK_LEFT", FencePieceType.FENCE_BACK_LEFT, topLeft);
        assertEquals("Top-right corner should be FENCE_BACK_RIGHT", FencePieceType.FENCE_BACK_RIGHT, topRight);
        assertEquals("Bottom-right corner should be FENCE_FRONT_RIGHT", FencePieceType.FENCE_FRONT_RIGHT, bottomRight);
        assertEquals("Bottom-left corner should be FENCE_FRONT_LEFT", FencePieceType.FENCE_FRONT_LEFT, bottomLeft);
        
        // Verify they are identified as corner pieces
        assertTrue("Top-left should be a corner piece", topLeft.isCornerPiece());
        assertTrue("Top-right should be a corner piece", topRight.isCornerPiece());
        assertTrue("Bottom-right should be a corner piece", bottomRight.isCornerPiece());
        assertTrue("Bottom-left should be a corner piece", bottomLeft.isCornerPiece());
    }
    
    /**
     * Property: Edge pieces should be correctly identified on rectangle edges.
     */
    @Property(trials = 100)
    public void edgePiecesAreCorrectlyIdentified(
            @InRange(min = "3", max = "10") int width,
            @InRange(min = "3", max = "10") int height) {
        
        // Test edge pieces (only valid for rectangles larger than 2x2)
        if (width > 2) {
            // Top edge (middle piece)
            FencePieceType topEdge = FencePieceFactory.determinePieceTypeForPosition(1, 0, width, height);
            assertEquals("Top edge should be FENCE_BACK", FencePieceType.FENCE_BACK, topEdge);
            assertTrue("Top edge should be an edge piece", topEdge.isEdgePiece());
            
            // Bottom edge (middle piece)
            FencePieceType bottomEdge = FencePieceFactory.determinePieceTypeForPosition(1, height - 1, width, height);
            assertEquals("Bottom edge should be FENCE_FRONT", FencePieceType.FENCE_FRONT, bottomEdge);
            assertTrue("Bottom edge should be an edge piece", bottomEdge.isEdgePiece());
        }
        
        if (height > 2) {
            // Right edge (middle piece)
            FencePieceType rightEdge = FencePieceFactory.determinePieceTypeForPosition(width - 1, 1, width, height);
            assertEquals("Right edge should be FENCE_MIDDLE_RIGHT", FencePieceType.FENCE_MIDDLE_RIGHT, rightEdge);
            assertTrue("Right edge should be an edge piece", rightEdge.isEdgePiece());
            
            // Left edge (middle piece)
            FencePieceType leftEdge = FencePieceFactory.determinePieceTypeForPosition(0, 1, width, height);
            assertEquals("Left edge should be FENCE_MIDDLE_LEFT", FencePieceType.FENCE_MIDDLE_LEFT, leftEdge);
            assertTrue("Left edge should be an edge piece", leftEdge.isEdgePiece());
        }
    }
    
    /**
     * Property: Piece selection should be consistent for the same position and context.
     */
    @Property(trials = 100)
    public void pieceSelectionIsConsistent(
            @InRange(min = "2", max = "8") int width,
            @InRange(min = "2", max = "8") int height,
            @InRange(min = "0", max = "7") int x,
            @InRange(min = "0", max = "7") int y) {
        
        // Only test positions that are on the perimeter
        if (x >= width || y >= height) return;
        if (x != 0 && x != width - 1 && y != 0 && y != height - 1) return;
        
        // Get piece type multiple times - should be consistent
        FencePieceType type1 = FencePieceFactory.determinePieceTypeForPosition(x, y, width, height);
        FencePieceType type2 = FencePieceFactory.determinePieceTypeForPosition(x, y, width, height);
        FencePieceType type3 = FencePieceFactory.determinePieceTypeForPosition(x, y, width, height);
        
        assertEquals("Piece selection should be consistent (first vs second call)", type1, type2);
        assertEquals("Piece selection should be consistent (second vs third call)", type2, type3);
    }
    
    /**
     * Property: The sequence validation should accept correct sequences.
     */
    @Property(trials = 100)
    public void sequenceValidationAcceptsCorrectSequences(
            @InRange(min = "2", max = "8") int width,
            @InRange(min = "2", max = "8") int height) {
        
        // Get the correct sequence
        FencePieceType[] correctSequence = FencePieceFactory.getEnclosureSequence(new Rectangle(0, 0, width, height));
        
        // Validation should accept the correct sequence
        boolean isValid = FencePieceFactory.validateEnclosureSequence(correctSequence, width, height);
        assertTrue("Correct sequence should be valid", isValid);
    }
    
    /**
     * Property: All positions in a rectangular perimeter should have valid piece types.
     */
    @Property(trials = 100)
    public void allPerimeterPositionsHaveValidPieceTypes(
            @InRange(min = "2", max = "6") int width,
            @InRange(min = "2", max = "6") int height) {
        
        // Test all perimeter positions
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Skip interior positions
                if (x != 0 && x != width - 1 && y != 0 && y != height - 1) {
                    continue;
                }
                
                FencePieceType pieceType = FencePieceFactory.determinePieceTypeForPosition(x, y, width, height);
                
                // Piece type should not be null
                assertNotNull("Piece type should not be null at position (" + x + ", " + y + ")", pieceType);
                
                // Piece type should be either a corner or edge piece
                boolean isValidType = pieceType.isCornerPiece() || pieceType.isEdgePiece();
                assertTrue("Piece type should be either corner or edge at position (" + x + ", " + y + ")", isValidType);
            }
        }
    }
    
    /**
     * Property: Enclosure sequences should have the correct length.
     */
    @Property(trials = 100)
    public void enclosureSequencesHaveCorrectLength(
            @InRange(min = "2", max = "10") int width,
            @InRange(min = "2", max = "10") int height) {
        
        Rectangle bounds = new Rectangle(0, 0, width, height);
        FencePieceType[] sequence = FencePieceFactory.getEnclosureSequence(bounds);
        int expectedLength = FencePieceFactory.calculateMaterialRequirement(bounds);
        
        assertEquals("Sequence length should match material requirement", expectedLength, sequence.length);
    }
    
    /**
     * Property: Material calculation should be consistent with perimeter formula.
     */
    @Property(trials = 100)
    public void materialCalculationMatchesPerimeterFormula(
            @InRange(min = "2", max = "15") int width,
            @InRange(min = "2", max = "15") int height) {
        
        int calculated = FencePieceFactory.calculateMaterialRequirement(width, height);
        int expected = 2 * (width + height) - 4; // Perimeter formula
        
        assertEquals("Material calculation should match perimeter formula", expected, calculated);
    }
}