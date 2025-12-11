package wagemaker.uk.fence;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.graphics.GL20;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for fence piece creation and rectangular enclosure completeness.
 * 
 * **Feature: custom-fence-building, Property 8: Rectangular enclosure completeness**
 * **Validates: Requirements 4.3, 5.3, 5.4, 14.2**
 */
@RunWith(JUnitQuickcheck.class)
public class FencePieceCreationPropertyTest {
    
    private static Application application;
    
    @BeforeClass
    public static void setUpClass() {
        // Set up headless LibGDX application for testing
        application = new HeadlessApplication(new ApplicationAdapter() {});
        Gdx.gl = Mockito.mock(GL20.class);
    }
    
    @AfterClass
    public static void tearDownClass() {
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Property 8: Rectangular enclosure completeness
     * For any rectangular enclosure, the structure should use exactly 8 pieces 
     * in clockwise order with proper corner and edge piece types.
     * 
     * This property tests that:
     * 1. All 8 fence piece types can be created successfully
     * 2. Each piece type has the correct classification (corner vs edge)
     * 3. Pieces maintain their position and type information correctly
     * 4. The clockwise ordering of piece types is preserved
     */
    @Property(trials = 100)
    public void rectangularEnclosureCompletenessProperty(
            @InRange(minFloat = 0.0f, maxFloat = 1000.0f) float x,
            @InRange(minFloat = 0.0f, maxFloat = 1000.0f) float y) {
        
        // Test that all 8 fence piece types can be created
        FencePiece[] pieces = new FencePiece[8];
        FencePieceType[] expectedOrder = {
            FencePieceType.FENCE_BACK_LEFT,
            FencePieceType.FENCE_BACK,
            FencePieceType.FENCE_BACK_RIGHT,
            FencePieceType.FENCE_MIDDLE_RIGHT,
            FencePieceType.FENCE_FRONT_RIGHT,
            FencePieceType.FENCE_FRONT,
            FencePieceType.FENCE_FRONT_LEFT,
            FencePieceType.FENCE_MIDDLE_LEFT
        };
        
        // Create all 8 pieces in clockwise order
        pieces[0] = new FenceBackLeftPiece(x, y);
        pieces[1] = new FenceBackPiece(x + 64, y);
        pieces[2] = new FenceBackRightPiece(x + 128, y);
        pieces[3] = new FenceMiddleRightPiece(x + 128, y - 64);
        pieces[4] = new FenceFrontRightPiece(x + 128, y - 128);
        pieces[5] = new FenceFrontPiece(x + 64, y - 128);
        pieces[6] = new FenceFrontLeftPiece(x, y - 128);
        pieces[7] = new FenceMiddleLeftPiece(x, y - 64);
        
        try {
            // Verify all pieces were created successfully
            for (int i = 0; i < 8; i++) {
                assertNotNull(pieces[i], "Fence piece " + i + " should be created successfully");
                assertEquals(expectedOrder[i], pieces[i].getType(), 
                    "Piece " + i + " should have correct type in clockwise order");
            }
            
            // Verify corner pieces are correctly identified
            assertTrue(pieces[0].getType().isCornerPiece(), "FENCE_BACK_LEFT should be corner piece");
            assertTrue(pieces[2].getType().isCornerPiece(), "FENCE_BACK_RIGHT should be corner piece");
            assertTrue(pieces[4].getType().isCornerPiece(), "FENCE_FRONT_RIGHT should be corner piece");
            assertTrue(pieces[6].getType().isCornerPiece(), "FENCE_FRONT_LEFT should be corner piece");
            
            // Verify edge pieces are correctly identified
            assertTrue(pieces[1].getType().isEdgePiece(), "FENCE_BACK should be edge piece");
            assertTrue(pieces[3].getType().isEdgePiece(), "FENCE_MIDDLE_RIGHT should be edge piece");
            assertTrue(pieces[5].getType().isEdgePiece(), "FENCE_FRONT should be edge piece");
            assertTrue(pieces[7].getType().isEdgePiece(), "FENCE_MIDDLE_LEFT should be edge piece");
            
            // Verify exactly 4 corner pieces and 4 edge pieces
            int cornerCount = 0, edgeCount = 0;
            for (FencePiece piece : pieces) {
                if (piece.getType().isCornerPiece()) cornerCount++;
                if (piece.getType().isEdgePiece()) edgeCount++;
            }
            assertEquals(4, cornerCount, "Should have exactly 4 corner pieces");
            assertEquals(4, edgeCount, "Should have exactly 4 edge pieces");
            
            // Verify position information is preserved
            for (int i = 0; i < 8; i++) {
                assertTrue(pieces[i].getX() >= x && pieces[i].getX() <= x + 128,
                    "Piece " + i + " X coordinate should be within enclosure bounds");
                assertTrue(pieces[i].getY() >= y - 128 && pieces[i].getY() <= y,
                    "Piece " + i + " Y coordinate should be within enclosure bounds");
            }
            
            // Verify collision bounds are properly set
            for (FencePiece piece : pieces) {
                assertNotNull(piece.getCollisionBounds(), "Collision bounds should not be null");
                assertEquals(64, piece.getCollisionBounds().width, 0.1f, 
                    "Collision bounds width should be 64 pixels");
                assertEquals(64, piece.getCollisionBounds().height, 0.1f, 
                    "Collision bounds height should be 64 pixels");
            }
            
            // Verify texture coordinates are unique for each piece type
            for (int i = 0; i < 8; i++) {
                for (int j = i + 1; j < 8; j++) {
                    FencePieceType typeI = pieces[i].getType();
                    FencePieceType typeJ = pieces[j].getType();
                    
                    boolean sameCoords = (typeI.getTextureX() == typeJ.getTextureX() && 
                                        typeI.getTextureY() == typeJ.getTextureY());
                    assertFalse(sameCoords, 
                        "Piece types " + typeI + " and " + typeJ + " should have different texture coordinates");
                }
            }
            
            // Verify piece equality and hash code consistency
            for (int i = 0; i < 8; i++) {
                FencePiece piece1 = pieces[i];
                FencePiece piece2 = createPieceOfType(piece1.getType(), piece1.getX(), piece1.getY());
                
                assertEquals(piece1, piece2, "Pieces with same type and position should be equal");
                assertEquals(piece1.hashCode(), piece2.hashCode(), 
                    "Equal pieces should have same hash code");
                
                piece2.dispose();
            }
            
        } finally {
            // Clean up resources
            for (FencePiece piece : pieces) {
                if (piece != null) {
                    piece.dispose();
                }
            }
        }
    }
    
    /**
     * Helper method to create a fence piece of the specified type.
     */
    private FencePiece createPieceOfType(FencePieceType type, float x, float y) {
        switch (type) {
            case FENCE_BACK_LEFT: return new FenceBackLeftPiece(x, y);
            case FENCE_BACK: return new FenceBackPiece(x, y);
            case FENCE_BACK_RIGHT: return new FenceBackRightPiece(x, y);
            case FENCE_MIDDLE_RIGHT: return new FenceMiddleRightPiece(x, y);
            case FENCE_FRONT_RIGHT: return new FenceFrontRightPiece(x, y);
            case FENCE_FRONT: return new FenceFrontPiece(x, y);
            case FENCE_FRONT_LEFT: return new FenceFrontLeftPiece(x, y);
            case FENCE_MIDDLE_LEFT: return new FenceMiddleLeftPiece(x, y);
            default: throw new IllegalArgumentException("Unknown fence piece type: " + type);
        }
    }
    
    /**
     * Additional property test to verify fence piece type enumeration completeness.
     */
    @Property(trials = 100)
    public void fencePieceTypeEnumerationCompleteness() {
        // Verify all 8 fence piece types exist
        FencePieceType[] allTypes = FencePieceType.values();
        assertEquals(8, allTypes.length, "Should have exactly 8 fence piece types");
        
        // Verify each type has valid texture coordinates
        for (FencePieceType type : allTypes) {
            assertTrue(type.getTextureX() >= 0, "Texture X coordinate should be non-negative");
            assertTrue(type.getTextureY() >= 0, "Texture Y coordinate should be non-negative");
            assertNotNull(type.getDescription(), "Description should not be null");
            assertFalse(type.getDescription().isEmpty(), "Description should not be empty");
        }
        
        // Verify corner/edge classification is mutually exclusive and complete
        for (FencePieceType type : allTypes) {
            boolean isCorner = type.isCornerPiece();
            boolean isEdge = type.isEdgePiece();
            
            // Each piece should be either corner or edge, but not both
            assertTrue(isCorner || isEdge, "Each piece should be either corner or edge");
            assertFalse(isCorner && isEdge, "Piece cannot be both corner and edge");
        }
    }
}