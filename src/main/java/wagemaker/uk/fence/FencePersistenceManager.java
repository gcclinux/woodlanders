package wagemaker.uk.fence;

import com.badlogic.gdx.math.Rectangle;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.awt.Point;
import java.io.*;
import java.util.*;

/**
 * Manages persistence operations for fence structures.
 * Handles serialization, deserialization, validation, and integration with the world save system.
 * Supports both binary and JSON serialization formats.
 */
public class FencePersistenceManager {
    
    /** JSON object mapper for JSON serialization */
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    
    static {
        JSON_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        JSON_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
    
    /**
     * Serializes fence structures to a list of FenceEnclosureData objects.
     * 
     * @param structureManager The fence structure manager containing fence data
     * @return List of serializable fence enclosure data
     */
    public static List<FenceEnclosureData> serializeFenceStructures(FenceStructureManager structureManager) {
        if (structureManager == null) {
            return new ArrayList<>();
        }
        
        List<FenceEnclosureData> serializedData = new ArrayList<>();
        
        // Serialize complete enclosures
        List<FenceEnclosure> enclosures = structureManager.getEnclosures();
        for (FenceEnclosure enclosure : enclosures) {
            try {
                FenceEnclosureData data = enclosure.serialize();
                if (data.isValid()) {
                    serializedData.add(data);
                } else {
                    System.err.println("WARNING: Invalid fence enclosure data, skipping: " + enclosure);
                }
            } catch (Exception e) {
                System.err.println("ERROR: Failed to serialize fence enclosure: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Serialize incomplete structures as individual pieces
        Map<Point, FencePiece> allPieces = structureManager.getAllFencePieces();
        Set<Point> incompletePieces = structureManager.getIncompleteStructurePositions();
        
        if (!incompletePieces.isEmpty()) {
            // Group incomplete pieces into a single "incomplete" enclosure for easier management
            List<FencePiece> pieces = new ArrayList<>();
            Rectangle bounds = calculateBoundsForIncomplete(incompletePieces, allPieces);
            
            for (Point pos : incompletePieces) {
                FencePiece piece = allPieces.get(pos);
                if (piece != null) {
                    pieces.add(piece);
                }
            }
            
            if (!pieces.isEmpty()) {
                FenceEnclosureData incompleteData = new FenceEnclosureData(
                    bounds, 
                    FenceMaterialType.WOOD, // Default material for incomplete structures
                    null, // No owner for incomplete structures
                    System.currentTimeMillis(),
                    pieces
                );
                
                if (incompleteData.isValid()) {
                    serializedData.add(incompleteData);
                }
            }
        }
        
        System.out.println("Serialized " + serializedData.size() + " fence structures (" + 
                          enclosures.size() + " complete, " + incompletePieces.size() + " incomplete pieces)");
        
        return serializedData;
    }
    
    /**
     * Deserializes fence structures and restores them to a structure manager.
     * 
     * @param serializedData List of fence enclosure data to deserialize
     * @param structureManager The structure manager to restore data to
     * @return Number of successfully restored structures
     */
    public static int deserializeFenceStructures(List<FenceEnclosureData> serializedData, 
                                               FenceStructureManager structureManager) {
        if (serializedData == null || structureManager == null) {
            return 0;
        }
        
        int restoredCount = 0;
        int errorCount = 0;
        
        // Clear existing structures
        structureManager.clear();
        
        for (FenceEnclosureData data : serializedData) {
            try {
                if (!data.isValid()) {
                    System.err.println("WARNING: Invalid fence enclosure data, skipping");
                    errorCount++;
                    continue;
                }
                
                // Restore fence pieces to the structure manager
                for (FenceEnclosureData.FencePieceData pieceData : data.pieceData) {
                    if (!pieceData.isValid()) {
                        System.err.println("WARNING: Invalid fence piece data, skipping: " + pieceData);
                        continue;
                    }
                    
                    // Convert world coordinates to grid coordinates
                    Point gridPos = structureManager.getGrid().worldToGrid(pieceData.x, pieceData.y);
                    
                    // Create and place the fence piece
                    try {
                        FencePiece piece = FencePieceFactory.createPiece(pieceData.type, pieceData.x, pieceData.y);
                        
                        // Create the fence piece and add it for restoration
                        boolean added = structureManager.addFencePieceForRestore(gridPos, piece);
                        if (!added) {
                            System.err.println("WARNING: Failed to restore fence piece at " + gridPos);
                            piece.dispose(); // Clean up if not added
                        }
                        
                    } catch (Exception e) {
                        System.err.println("ERROR: Failed to restore fence piece at " + gridPos + ": " + e.getMessage());
                        errorCount++;
                    }
                }
                
                restoredCount++;
                
            } catch (Exception e) {
                System.err.println("ERROR: Failed to deserialize fence structure: " + e.getMessage());
                e.printStackTrace();
                errorCount++;
            }
        }
        
        // Rebuild enclosures and connections after all pieces are restored
        try {
            rebuildStructureConnections(structureManager);
        } catch (Exception e) {
            System.err.println("ERROR: Failed to rebuild structure connections: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("Restored " + restoredCount + " fence structures with " + errorCount + " errors");
        
        return restoredCount;
    }
    
    /**
     * Serializes fence structures to JSON format.
     * 
     * @param structureManager The fence structure manager
     * @return JSON string representation of fence structures
     * @throws JsonProcessingException if JSON serialization fails
     */
    public static String serializeFenceStructuresToJson(FenceStructureManager structureManager) 
            throws JsonProcessingException {
        List<FenceEnclosureData> data = serializeFenceStructures(structureManager);
        return JSON_MAPPER.writeValueAsString(data);
    }
    
    /**
     * Deserializes fence structures from JSON format.
     * 
     * @param jsonData JSON string containing fence structure data
     * @param structureManager The structure manager to restore data to
     * @return Number of successfully restored structures
     * @throws JsonProcessingException if JSON deserialization fails
     */
    public static int deserializeFenceStructuresFromJson(String jsonData, 
                                                       FenceStructureManager structureManager) 
            throws JsonProcessingException {
        if (jsonData == null || jsonData.trim().isEmpty()) {
            return 0;
        }
        
        FenceEnclosureData[] dataArray = JSON_MAPPER.readValue(jsonData, FenceEnclosureData[].class);
        List<FenceEnclosureData> dataList = Arrays.asList(dataArray);
        
        return deserializeFenceStructures(dataList, structureManager);
    }
    
    /**
     * Validates fence structure data integrity.
     * 
     * @param serializedData List of fence enclosure data to validate
     * @return ValidationResult containing validation status and details
     */
    public static ValidationResult validateFenceStructureData(List<FenceEnclosureData> serializedData) {
        ValidationResult result = new ValidationResult();
        
        if (serializedData == null) {
            result.addError("Fence structure data is null");
            return result;
        }
        
        for (int i = 0; i < serializedData.size(); i++) {
            FenceEnclosureData data = serializedData.get(i);
            
            if (data == null) {
                result.addError("Fence enclosure data at index " + i + " is null");
                continue;
            }
            
            if (!data.isValid()) {
                result.addError("Fence enclosure data at index " + i + " is invalid");
                continue;
            }
            
            // Validate piece data
            for (int j = 0; j < data.pieceData.size(); j++) {
                FenceEnclosureData.FencePieceData pieceData = data.pieceData.get(j);
                if (pieceData == null || !pieceData.isValid()) {
                    result.addWarning("Invalid piece data at enclosure " + i + ", piece " + j);
                }
            }
            
            result.incrementValidStructures();
        }
        
        return result;
    }
    
    /**
     * Calculates bounding rectangle for incomplete fence pieces.
     * 
     * @param incompletePieces Set of grid positions for incomplete pieces
     * @param allPieces Map of all fence pieces
     * @return Bounding rectangle containing all incomplete pieces
     */
    private static Rectangle calculateBoundsForIncomplete(Set<Point> incompletePieces, 
                                                        Map<Point, FencePiece> allPieces) {
        if (incompletePieces.isEmpty()) {
            return new Rectangle(0, 0, 1, 1);
        }
        
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        
        for (Point pos : incompletePieces) {
            minX = Math.min(minX, pos.x);
            maxX = Math.max(maxX, pos.x);
            minY = Math.min(minY, pos.y);
            maxY = Math.max(maxY, pos.y);
        }
        
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
    
    /**
     * Rebuilds structure connections and enclosures after deserialization.
     * 
     * @param structureManager The structure manager to rebuild
     */
    private static void rebuildStructureConnections(FenceStructureManager structureManager) {
        // Update connections for all pieces
        Map<Point, FencePiece> allPieces = structureManager.getAllFencePieces();
        
        for (Point pos : allPieces.keySet()) {
            structureManager.updateConnections(pos);
        }
        
        // The structure manager will automatically detect and create enclosures
        // during the connection update process
    }
    
    /**
     * Result of fence structure data validation.
     */
    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private int validStructures = 0;
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public void incrementValidStructures() {
            validStructures++;
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }
        
        public List<String> getWarnings() {
            return new ArrayList<>(warnings);
        }
        
        public int getValidStructures() {
            return validStructures;
        }
        
        public boolean hasWarnings() {
            return !warnings.isEmpty();
        }
        
        @Override
        public String toString() {
            return String.format("ValidationResult[valid=%d, errors=%d, warnings=%d]",
                               validStructures, errors.size(), warnings.size());
        }
    }
}