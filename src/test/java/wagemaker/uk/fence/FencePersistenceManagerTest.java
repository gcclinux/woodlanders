package wagemaker.uk.fence;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.badlogic.gdx.math.Rectangle;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for FencePersistenceManager.
 * Tests fence structure serialization and deserialization, JSON serialization,
 * and data validation and error handling.
 */
public class FencePersistenceManagerTest {
    
    private FenceStructureManager structureManager;
    private FenceGrid grid;
    
    @Before
    public void setUp() {
        grid = new FenceGrid();
        structureManager = new FenceStructureManager(grid);
    }
    
    @After
    public void tearDown() {
        if (structureManager != null) {
            structureManager.dispose();
        }
    }
    
    @Test
    public void testSerializeEmptyStructureManager() {
        // Act
        List<FenceEnclosureData> result = FencePersistenceManager.serializeFenceStructures(structureManager);
        
        // Assert
        assertNotNull("Serialization result should not be null", result);
        assertTrue("Empty structure manager should produce empty serialization", result.isEmpty());
    }
    
    @Test
    public void testSerializeNullStructureManager() {
        // Act
        List<FenceEnclosureData> result = FencePersistenceManager.serializeFenceStructures(null);
        
        // Assert
        assertNotNull("Serialization result should not be null for null input", result);
        assertTrue("Null structure manager should produce empty serialization", result.isEmpty());
    }
    
    @Test
    public void testSerializeStructureWithIncompleteStructures() {
        // Arrange - add some incomplete fence pieces
        Point pos1 = new Point(5, 5);
        Point pos2 = new Point(6, 5);
        Point pos3 = new Point(7, 5);
        
        // Mock fence pieces since we can't create real ones without LibGDX
        FencePiece mockPiece1 = createMockFencePiece(pos1, FencePieceType.FENCE_BACK_LEFT);
        FencePiece mockPiece2 = createMockFencePiece(pos2, FencePieceType.FENCE_BACK);
        FencePiece mockPiece3 = createMockFencePiece(pos3, FencePieceType.FENCE_BACK_RIGHT);
        
        // Add pieces to structure manager
        structureManager.addFencePieceForRestore(pos1, mockPiece1);
        structureManager.addFencePieceForRestore(pos2, mockPiece2);
        structureManager.addFencePieceForRestore(pos3, mockPiece3);
        
        // Act
        List<FenceEnclosureData> result = FencePersistenceManager.serializeFenceStructures(structureManager);
        
        // Assert
        assertNotNull("Serialization result should not be null", result);
        assertFalse("Should have serialized data for incomplete structures", result.isEmpty());
        
        // Should have one enclosure containing the incomplete pieces
        assertEquals("Should have one enclosure for incomplete pieces", 1, result.size());
        
        FenceEnclosureData enclosureData = result.get(0);
        assertTrue("Enclosure data should be valid", enclosureData.isValid());
        assertEquals("Should have 3 pieces", 3, enclosureData.getPieceCount());
    }
    
    @Test
    public void testDeserializeEmptyData() {
        // Arrange
        List<FenceEnclosureData> emptyData = new ArrayList<>();
        
        // Act
        int result = FencePersistenceManager.deserializeFenceStructures(emptyData, structureManager);
        
        // Assert
        assertEquals("Should return 0 for empty data", 0, result);
        assertTrue("Structure manager should remain empty", structureManager.isEmpty());
    }
    
    @Test
    public void testDeserializeNullData() {
        // Act
        int result = FencePersistenceManager.deserializeFenceStructures(null, structureManager);
        
        // Assert
        assertEquals("Should return 0 for null data", 0, result);
        assertTrue("Structure manager should remain empty", structureManager.isEmpty());
    }
    
    @Test
    public void testDeserializeWithNullStructureManager() {
        // Arrange
        List<FenceEnclosureData> data = createValidTestData();
        
        // Act
        int result = FencePersistenceManager.deserializeFenceStructures(data, null);
        
        // Assert
        assertEquals("Should return 0 for null structure manager", 0, result);
    }
    
    @Test
    public void testSerializeDeserializeRoundTrip() {
        // Arrange - create test data
        Point pos1 = new Point(2, 3);
        Point pos2 = new Point(3, 3);
        
        FencePiece mockPiece1 = createMockFencePiece(pos1, FencePieceType.FENCE_BACK_LEFT);
        FencePiece mockPiece2 = createMockFencePiece(pos2, FencePieceType.FENCE_BACK);
        
        structureManager.addFencePieceForRestore(pos1, mockPiece1);
        structureManager.addFencePieceForRestore(pos2, mockPiece2);
        
        // Act - serialize only (deserialization requires LibGDX graphics context)
        List<FenceEnclosureData> serialized = FencePersistenceManager.serializeFenceStructures(structureManager);
        
        // Assert serialization
        assertNotNull("Serialized data should not be null", serialized);
        assertFalse("Should have serialized data", serialized.isEmpty());
        assertEquals("Should have one enclosure for incomplete pieces", 1, serialized.size());
        
        FenceEnclosureData enclosureData = serialized.get(0);
        assertTrue("Enclosure data should be valid", enclosureData.isValid());
        assertEquals("Should have 2 pieces", 2, enclosureData.getPieceCount());
        
        // Verify piece data (order may vary based on internal map ordering)
        boolean hasBackLeft = false;
        boolean hasBack = false;
        for (FenceEnclosureData.FencePieceData pieceData : enclosureData.pieceData) {
            if (pieceData.type == FencePieceType.FENCE_BACK_LEFT) {
                hasBackLeft = true;
            } else if (pieceData.type == FencePieceType.FENCE_BACK) {
                hasBack = true;
            }
        }
        assertTrue("Should contain FENCE_BACK_LEFT piece", hasBackLeft);
        assertTrue("Should contain FENCE_BACK piece", hasBack);
    }
    
    @Test
    public void testJsonSerializationEmpty() throws JsonProcessingException {
        // Act
        String json = FencePersistenceManager.serializeFenceStructuresToJson(structureManager);
        
        // Assert
        assertNotNull("JSON result should not be null", json);
        assertEquals("Empty structure should serialize to empty array", "[ ]", json);
    }
    
    @Test
    public void testJsonSerializationWithData() throws JsonProcessingException {
        // Arrange
        Point pos = new Point(1, 1);
        FencePiece mockPiece = createMockFencePiece(pos, FencePieceType.FENCE_BACK_LEFT);
        structureManager.addFencePieceForRestore(pos, mockPiece);
        
        // Act
        String json = FencePersistenceManager.serializeFenceStructuresToJson(structureManager);
        
        // Assert
        assertNotNull("JSON result should not be null", json);
        assertFalse("JSON should not be empty", json.trim().isEmpty());
        assertTrue("JSON should contain array structure", json.contains("["));
        assertTrue("JSON should contain object structure", json.contains("{"));
    }
    
    @Test
    public void testJsonDeserializationEmpty() throws JsonProcessingException {
        // Act
        int result = FencePersistenceManager.deserializeFenceStructuresFromJson("[]", structureManager);
        
        // Assert
        assertEquals("Should return 0 for empty JSON array", 0, result);
        assertTrue("Structure manager should remain empty", structureManager.isEmpty());
    }
    
    @Test
    public void testJsonDeserializationNull() throws JsonProcessingException {
        // Act
        int result = FencePersistenceManager.deserializeFenceStructuresFromJson(null, structureManager);
        
        // Assert
        assertEquals("Should return 0 for null JSON", 0, result);
        assertTrue("Structure manager should remain empty", structureManager.isEmpty());
    }
    
    @Test
    public void testJsonDeserializationEmptyString() throws JsonProcessingException {
        // Act
        int result = FencePersistenceManager.deserializeFenceStructuresFromJson("", structureManager);
        
        // Assert
        assertEquals("Should return 0 for empty JSON string", 0, result);
        assertTrue("Structure manager should remain empty", structureManager.isEmpty());
    }
    
    @Test(expected = JsonProcessingException.class)
    public void testJsonDeserializationInvalidJson() throws JsonProcessingException {
        // Act - should throw JsonProcessingException
        FencePersistenceManager.deserializeFenceStructuresFromJson("invalid json", structureManager);
    }
    
    @Test
    public void testValidateValidData() {
        // Arrange
        List<FenceEnclosureData> validData = createValidTestData();
        
        // Act
        FencePersistenceManager.ValidationResult result = 
            FencePersistenceManager.validateFenceStructureData(validData);
        
        // Assert
        assertNotNull("Validation result should not be null", result);
        assertTrue("Valid data should pass validation", result.isValid());
        assertFalse("Valid data should not have warnings", result.hasWarnings());
        assertEquals("Should have correct number of valid structures", 1, result.getValidStructures());
        assertTrue("Should have no errors", result.getErrors().isEmpty());
    }
    
    @Test
    public void testValidateNullData() {
        // Act
        FencePersistenceManager.ValidationResult result = 
            FencePersistenceManager.validateFenceStructureData(null);
        
        // Assert
        assertNotNull("Validation result should not be null", result);
        assertFalse("Null data should fail validation", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertEquals("Should have 0 valid structures", 0, result.getValidStructures());
    }
    
    @Test
    public void testValidateInvalidData() {
        // Arrange
        List<FenceEnclosureData> invalidData = new ArrayList<>();
        
        // Add null entry
        invalidData.add(null);
        
        // Add invalid entry
        FenceEnclosureData invalidEntry = new FenceEnclosureData();
        invalidEntry.bounds = null; // Invalid - null bounds
        invalidData.add(invalidEntry);
        
        // Act
        FencePersistenceManager.ValidationResult result = 
            FencePersistenceManager.validateFenceStructureData(invalidData);
        
        // Assert
        assertNotNull("Validation result should not be null", result);
        assertFalse("Invalid data should fail validation", result.isValid());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        assertEquals("Should have 0 valid structures", 0, result.getValidStructures());
        assertTrue("Should have at least 2 errors", result.getErrors().size() >= 2);
    }
    
    @Test
    public void testValidateDataWithMixedResults() {
        // Arrange - create data with both valid and invalid entries
        List<FenceEnclosureData> mixedData = new ArrayList<>();
        
        // Add a valid enclosure
        FenceEnclosureData validEntry = createValidTestData().get(0);
        mixedData.add(validEntry);
        
        // Add an invalid enclosure
        FenceEnclosureData invalidEntry = new FenceEnclosureData();
        invalidEntry.bounds = null; // Invalid - null bounds
        mixedData.add(invalidEntry);
        
        // Act
        FencePersistenceManager.ValidationResult result = 
            FencePersistenceManager.validateFenceStructureData(mixedData);
        
        // Assert
        assertNotNull("Validation result should not be null", result);
        assertFalse("Data should be invalid due to invalid entry", result.isValid());
        assertEquals("Should have 1 valid structure", 1, result.getValidStructures());
        assertFalse("Should have errors", result.getErrors().isEmpty());
        // May or may not have warnings depending on implementation
    }
    
    @Test
    public void testValidationResultToString() {
        // Arrange
        FencePersistenceManager.ValidationResult result = new FencePersistenceManager.ValidationResult();
        result.addError("Test error");
        result.addWarning("Test warning");
        result.incrementValidStructures();
        
        // Act
        String toString = result.toString();
        
        // Assert
        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain valid count", toString.contains("valid=1"));
        assertTrue("toString should contain error count", toString.contains("errors=1"));
        assertTrue("toString should contain warning count", toString.contains("warnings=1"));
    }
    
    /**
     * Creates a mock FencePiece for testing purposes.
     */
    private FencePiece createMockFencePiece(Point gridPos, FencePieceType type) {
        FencePiece mockPiece = mock(FencePiece.class);
        
        // Convert grid to world coordinates
        float worldX = gridPos.x * FenceGrid.GRID_SIZE;
        float worldY = gridPos.y * FenceGrid.GRID_SIZE;
        
        when(mockPiece.getX()).thenReturn(worldX);
        when(mockPiece.getY()).thenReturn(worldY);
        when(mockPiece.getType()).thenReturn(type);
        when(mockPiece.getCollisionBounds()).thenReturn(new Rectangle(worldX, worldY, 
                                                                     FenceGrid.GRID_SIZE, 
                                                                     FenceGrid.GRID_SIZE));
        
        return mockPiece;
    }
    
    /**
     * Creates valid test data for testing.
     */
    private List<FenceEnclosureData> createValidTestData() {
        List<FenceEnclosureData> data = new ArrayList<>();
        
        FenceEnclosureData entry = new FenceEnclosureData();
        entry.bounds = new Rectangle(0, 0, 3, 3);
        entry.materialType = FenceMaterialType.WOOD;
        entry.ownerId = "test_player";
        entry.creationTime = System.currentTimeMillis();
        entry.pieceData = new ArrayList<>();
        
        // Add valid piece data
        entry.pieceData.add(new FenceEnclosureData.FencePieceData(0, 0, FencePieceType.FENCE_BACK_LEFT));
        entry.pieceData.add(new FenceEnclosureData.FencePieceData(64, 0, FencePieceType.FENCE_BACK));
        entry.pieceData.add(new FenceEnclosureData.FencePieceData(128, 0, FencePieceType.FENCE_BACK_RIGHT));
        
        data.add(entry);
        return data;
    }
}