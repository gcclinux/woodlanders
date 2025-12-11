package wagemaker.uk.fence;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for FenceEnclosureData.
 * Tests data validation, serialization structure, and error handling.
 */
public class FenceEnclosureDataTest {
    
    @Test
    public void testDefaultConstructor() {
        // Act
        FenceEnclosureData data = new FenceEnclosureData();
        
        // Assert
        assertNotNull("Piece data should be initialized", data.pieceData);
        assertTrue("Piece data should be empty initially", data.pieceData.isEmpty());
        assertEquals("Should have 0 pieces initially", 0, data.getPieceCount());
    }
    
    @Test
    public void testConstructorWithParameters() {
        // Arrange
        Rectangle bounds = new Rectangle(0, 0, 5, 5);
        FenceMaterialType materialType = FenceMaterialType.WOOD;
        String ownerId = "test_player";
        long creationTime = System.currentTimeMillis();
        
        List<FencePiece> pieces = new ArrayList<>();
        FencePiece mockPiece1 = createMockFencePiece(0, 0, FencePieceType.FENCE_BACK_LEFT);
        FencePiece mockPiece2 = createMockFencePiece(64, 0, FencePieceType.FENCE_BACK);
        pieces.add(mockPiece1);
        pieces.add(mockPiece2);
        
        // Act
        FenceEnclosureData data = new FenceEnclosureData(bounds, materialType, ownerId, creationTime, pieces);
        
        // Assert
        assertNotNull("Bounds should not be null", data.bounds);
        assertEquals("Bounds should match", bounds, data.bounds);
        assertEquals("Material type should match", materialType, data.materialType);
        assertEquals("Owner ID should match", ownerId, data.ownerId);
        assertEquals("Creation time should match", creationTime, data.creationTime);
        assertEquals("Should have 2 pieces", 2, data.getPieceCount());
        assertNotNull("Piece data should not be null", data.pieceData);
        assertEquals("Piece data size should match", 2, data.pieceData.size());
    }
    
    @Test
    public void testIsValidWithValidData() {
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        
        // Act
        boolean isValid = data.isValid();
        
        // Assert
        assertTrue("Valid data should pass validation", isValid);
    }
    
    @Test
    public void testIsValidWithNullBounds() {
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        data.bounds = null;
        
        // Act
        boolean isValid = data.isValid();
        
        // Assert
        assertFalse("Data with null bounds should be invalid", isValid);
    }
    
    @Test
    public void testIsValidWithNullMaterialType() {
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        data.materialType = null;
        
        // Act
        boolean isValid = data.isValid();
        
        // Assert
        assertFalse("Data with null material type should be invalid", isValid);
    }
    
    @Test
    public void testIsValidWithNullPieceData() {
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        data.pieceData = null;
        
        // Act
        boolean isValid = data.isValid();
        
        // Assert
        assertFalse("Data with null piece data should be invalid", isValid);
    }
    
    @Test
    public void testIsValidWithZeroWidthBounds() {
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        data.bounds = new Rectangle(0, 0, 0, 5); // Zero width
        
        // Act
        boolean isValid = data.isValid();
        
        // Assert
        assertFalse("Data with zero width bounds should be invalid", isValid);
    }
    
    @Test
    public void testIsValidWithZeroHeightBounds() {
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        data.bounds = new Rectangle(0, 0, 5, 0); // Zero height
        
        // Act
        boolean isValid = data.isValid();
        
        // Assert
        assertFalse("Data with zero height bounds should be invalid", isValid);
    }
    
    @Test
    public void testIsValidWithNegativeCreationTime() {
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        data.creationTime = -1;
        
        // Act
        boolean isValid = data.isValid();
        
        // Assert
        assertFalse("Data with negative creation time should be invalid", isValid);
    }
    
    @Test
    public void testIsValidWithZeroCreationTime() {
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        data.creationTime = 0;
        
        // Act
        boolean isValid = data.isValid();
        
        // Assert
        assertFalse("Data with zero creation time should be invalid", isValid);
    }
    
    @Test
    public void testIsValidWithNullPieceInData() {
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        data.pieceData.add(null); // Add null piece
        
        // Act
        boolean isValid = data.isValid();
        
        // Assert
        assertFalse("Data with null piece should be invalid", isValid);
    }
    
    @Test
    public void testIsValidWithInvalidPieceData() {
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        
        FenceEnclosureData.FencePieceData invalidPiece = new FenceEnclosureData.FencePieceData();
        invalidPiece.type = null; // Invalid - null type
        data.pieceData.add(invalidPiece);
        
        // Act
        boolean isValid = data.isValid();
        
        // Assert
        assertFalse("Data with invalid piece data should be invalid", isValid);
    }
    
    @Test
    public void testGetPieceCountEmpty() {
        // Arrange
        FenceEnclosureData data = new FenceEnclosureData();
        
        // Act
        int count = data.getPieceCount();
        
        // Assert
        assertEquals("Empty data should have 0 pieces", 0, count);
    }
    
    @Test
    public void testGetPieceCountWithPieces() {
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        
        // Act
        int count = data.getPieceCount();
        
        // Assert
        assertEquals("Should have correct piece count", 3, count);
    }
    
    @Test
    public void testGetPieceCountWithNullPieceData() {
        // Arrange
        FenceEnclosureData data = new FenceEnclosureData();
        data.pieceData = null;
        
        // Act
        int count = data.getPieceCount();
        
        // Assert
        assertEquals("Null piece data should return 0 count", 0, count);
    }
    
    @Test
    public void testToFenceEnclosure() {
        // Note: This test is skipped because toFenceEnclosure() requires LibGDX graphics context
        // which is not available in unit tests. The functionality is tested
        // through integration tests that run with proper LibGDX initialization.
        
        // Arrange
        FenceEnclosureData data = createValidFenceEnclosureData();
        
        // Verify the data structure is correct for conversion
        assertNotNull("Data should not be null", data);
        assertTrue("Data should be valid", data.isValid());
        assertEquals("Should have correct material type", FenceMaterialType.BAMBOO, data.materialType);
        assertEquals("Should have correct owner ID", "test_player", data.ownerId);
        assertEquals("Should have correct number of pieces", 3, data.getPieceCount());
        
        // The actual toFenceEnclosure test would require LibGDX graphics context
        // In a real implementation, this would be tested in integration tests
    }
    
    @Test
    public void testFencePieceDataDefaultConstructor() {
        // Act
        FenceEnclosureData.FencePieceData pieceData = new FenceEnclosureData.FencePieceData();
        
        // Assert
        assertEquals("X should be 0 by default", 0.0f, pieceData.x, 0.001f);
        assertEquals("Y should be 0 by default", 0.0f, pieceData.y, 0.001f);
        assertNull("Type should be null by default", pieceData.type);
    }
    
    @Test
    public void testFencePieceDataConstructorWithParameters() {
        // Arrange
        float x = 128.5f;
        float y = 256.7f;
        FencePieceType type = FencePieceType.FENCE_MIDDLE_RIGHT;
        
        // Act
        FenceEnclosureData.FencePieceData pieceData = new FenceEnclosureData.FencePieceData(x, y, type);
        
        // Assert
        assertEquals("X should match", x, pieceData.x, 0.001f);
        assertEquals("Y should match", y, pieceData.y, 0.001f);
        assertEquals("Type should match", type, pieceData.type);
    }
    
    @Test
    public void testFencePieceDataIsValidWithValidData() {
        // Arrange
        FenceEnclosureData.FencePieceData pieceData = new FenceEnclosureData.FencePieceData(
            100.0f, 200.0f, FencePieceType.FENCE_FRONT_LEFT);
        
        // Act
        boolean isValid = pieceData.isValid();
        
        // Assert
        assertTrue("Valid piece data should pass validation", isValid);
    }
    
    @Test
    public void testFencePieceDataIsValidWithNullType() {
        // Arrange
        FenceEnclosureData.FencePieceData pieceData = new FenceEnclosureData.FencePieceData(
            100.0f, 200.0f, null);
        
        // Act
        boolean isValid = pieceData.isValid();
        
        // Assert
        assertFalse("Piece data with null type should be invalid", isValid);
    }
    
    @Test
    public void testFencePieceDataToString() {
        // Arrange
        FenceEnclosureData.FencePieceData pieceData = new FenceEnclosureData.FencePieceData(
            64.0f, 128.0f, FencePieceType.FENCE_BACK);
        
        // Act
        String toString = pieceData.toString();
        
        // Assert
        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain X coordinate", toString.contains("64.0"));
        assertTrue("toString should contain Y coordinate", toString.contains("128.0"));
        assertTrue("toString should contain piece type", toString.contains("FENCE_BACK"));
    }
    
    /**
     * Creates a valid FenceEnclosureData for testing.
     */
    private FenceEnclosureData createValidFenceEnclosureData() {
        FenceEnclosureData data = new FenceEnclosureData();
        data.bounds = new Rectangle(0, 0, 3, 3);
        data.materialType = FenceMaterialType.BAMBOO;
        data.ownerId = "test_player";
        data.creationTime = System.currentTimeMillis();
        data.pieceData = new ArrayList<>();
        
        // Add valid piece data
        data.pieceData.add(new FenceEnclosureData.FencePieceData(0, 0, FencePieceType.FENCE_BACK_LEFT));
        data.pieceData.add(new FenceEnclosureData.FencePieceData(64, 0, FencePieceType.FENCE_BACK));
        data.pieceData.add(new FenceEnclosureData.FencePieceData(128, 0, FencePieceType.FENCE_BACK_RIGHT));
        
        return data;
    }
    
    /**
     * Creates a mock FencePiece for testing.
     */
    private FencePiece createMockFencePiece(float x, float y, FencePieceType type) {
        FencePiece mockPiece = mock(FencePiece.class);
        when(mockPiece.getX()).thenReturn(x);
        when(mockPiece.getY()).thenReturn(y);
        when(mockPiece.getType()).thenReturn(type);
        return mockPiece;
    }
}