package wagemaker.uk.fence;

import java.awt.Point;
import java.util.List;

/**
 * Validates fence placement operations for the custom fence building system.
 * Handles validation logic for grid position availability, material requirements,
 * and ownership validation for multiplayer mode.
 */
public class FencePlacementValidator {
    
    /** Grid system for position validation */
    private final FenceGrid grid;
    
    /** Material provider for checking material availability */
    private final FenceMaterialProvider materialProvider;
    
    /** Structure manager for checking existing structures */
    private final FenceStructureManager structureManager;
    
    /** Flag to enable/disable ownership validation for multiplayer */
    private boolean ownershipValidationEnabled;
    
    /**
     * Creates a new fence placement validator.
     * 
     * @param grid The fence grid system
     * @param materialProvider Provider for material availability checking
     * @param structureManager Manager for existing fence structures
     */
    public FencePlacementValidator(FenceGrid grid, FenceMaterialProvider materialProvider, 
                                  FenceStructureManager structureManager) {
        this.grid = grid;
        this.materialProvider = materialProvider;
        this.structureManager = structureManager;
        this.ownershipValidationEnabled = false;
    }
    
    /**
     * Validates whether a fence piece can be placed at the specified position.
     * 
     * @param gridPos Grid position to validate
     * @param materialType Type of material to use
     * @param playerId ID of the player attempting placement (for multiplayer)
     * @return ValidationResult containing the result and any error messages
     */
    public ValidationResult validatePlacement(Point gridPos, FenceMaterialType materialType, String playerId) {
        // Check grid position availability
        ValidationResult gridResult = validateGridPosition(gridPos);
        if (!gridResult.isValid()) {
            return gridResult;
        }
        
        // Check material availability
        ValidationResult materialResult = validateMaterialAvailability(materialType, 1);
        if (!materialResult.isValid()) {
            return materialResult;
        }
        
        // Check ownership if enabled (multiplayer mode)
        if (ownershipValidationEnabled) {
            ValidationResult ownershipResult = validateOwnership(gridPos, playerId);
            if (!ownershipResult.isValid()) {
                return ownershipResult;
            }
        }
        
        // All validations passed
        return ValidationResult.valid();
    }
    
    /**
     * Validates whether a fence piece can be removed from the specified position.
     * 
     * @param gridPos Grid position to validate
     * @param playerId ID of the player attempting removal (for multiplayer)
     * @return ValidationResult containing the result and any error messages
     */
    public ValidationResult validateRemoval(Point gridPos, String playerId) {
        // Check if there's actually a fence piece at this position
        boolean isOccupied = grid.isOccupied(gridPos);
        System.out.println("DEBUG: validateRemoval at (" + gridPos.x + ", " + gridPos.y + ") - isOccupied: " + isOccupied);
        if (!isOccupied) {
            return ValidationResult.invalid("No fence piece exists at position (" + gridPos.x + ", " + gridPos.y + ")");
        }
        
        // Check ownership if enabled (multiplayer mode)
        if (ownershipValidationEnabled) {
            ValidationResult ownershipResult = validateRemovalOwnership(gridPos, playerId);
            if (!ownershipResult.isValid()) {
                return ownershipResult;
            }
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Validates grid position availability.
     * 
     * @param gridPos Grid position to check
     * @return ValidationResult for grid position
     */
    private ValidationResult validateGridPosition(Point gridPos) {
        if (grid.isOccupied(gridPos)) {
            return ValidationResult.invalid("Position (" + gridPos.x + ", " + gridPos.y + ") is already occupied");
        }
        
        if (!grid.isValidPlacement(gridPos)) {
            return ValidationResult.invalid("Position (" + gridPos.x + ", " + gridPos.y + ") is blocked or invalid");
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Validates material availability.
     * 
     * @param materialType Type of material required
     * @param quantity Quantity of material required
     * @return ValidationResult for material availability
     */
    private ValidationResult validateMaterialAvailability(FenceMaterialType materialType, int quantity) {
        if (materialProvider == null) {
            return ValidationResult.valid(); // Skip validation if no provider
        }
        
        if (!materialProvider.hasEnoughMaterials(materialType, quantity)) {
            int available = materialProvider.getMaterialCount(materialType);
            return ValidationResult.invalid("Insufficient " + materialType.getDisplayName() + 
                                          ". Required: " + quantity + ", Available: " + available);
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Validates ownership for placement operations in multiplayer mode.
     * 
     * @param gridPos Grid position being placed
     * @param playerId ID of the player attempting placement
     * @return ValidationResult for ownership
     */
    private ValidationResult validateOwnership(Point gridPos, String playerId) {
        if (playerId == null || playerId.trim().isEmpty()) {
            return ValidationResult.invalid("Player ID is required for multiplayer mode");
        }
        
        // Check if adjacent pieces belong to the same player or are unowned
        List<Point> adjacentPositions = grid.getAdjacentOccupiedPositions(gridPos);
        
        for (Point adjacentPos : adjacentPositions) {
            FencePiece adjacentPiece = structureManager.getFencePiece(adjacentPos);
            if (adjacentPiece != null) {
                // For now, we'll assume all pieces can be connected
                // In a full implementation, we'd check piece ownership
                // This is a placeholder for future ownership tracking
            }
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Validates ownership for removal operations in multiplayer mode.
     * 
     * @param gridPos Grid position being removed
     * @param playerId ID of the player attempting removal
     * @return ValidationResult for removal ownership
     */
    private ValidationResult validateRemovalOwnership(Point gridPos, String playerId) {
        if (playerId == null || playerId.trim().isEmpty()) {
            return ValidationResult.invalid("Player ID is required for multiplayer mode");
        }
        
        FencePiece piece = structureManager.getFencePiece(gridPos);
        if (piece == null) {
            return ValidationResult.invalid("No fence piece found at position");
        }
        
        // Check if the piece belongs to the player
        String pieceOwner = piece.getOwnerId();
        
        // If piece has no owner, allow removal (or could be restricted based on game rules)
        if (pieceOwner == null) {
            return ValidationResult.valid();
        }
        
        // Check if the requesting player is the owner
        if (!pieceOwner.equals(playerId)) {
            return ValidationResult.invalid("You cannot remove this fence because you do not own it");
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Validates whether multiple fence pieces can be placed for a complete enclosure.
     * 
     * @param positions List of grid positions for the enclosure
     * @param materialType Type of material to use
     * @param playerId ID of the player attempting placement
     * @return ValidationResult for the entire enclosure
     */
    public ValidationResult validateEnclosurePlacement(List<Point> positions, 
                                                      FenceMaterialType materialType, 
                                                      String playerId) {
        if (positions == null || positions.isEmpty()) {
            return ValidationResult.invalid("No positions provided for enclosure");
        }
        
        // Check each position individually
        for (Point pos : positions) {
            ValidationResult result = validatePlacement(pos, materialType, playerId);
            if (!result.isValid()) {
                return ValidationResult.invalid("Enclosure validation failed at position (" + 
                                              pos.x + ", " + pos.y + "): " + result.getErrorMessage());
            }
        }
        
        // Check total material requirement
        ValidationResult materialResult = validateMaterialAvailability(materialType, positions.size());
        if (!materialResult.isValid()) {
            return materialResult;
        }
        
        return ValidationResult.valid();
    }
    
    /**
     * Enables or disables ownership validation for multiplayer mode.
     * 
     * @param enabled true to enable ownership validation, false to disable
     */
    public void setOwnershipValidationEnabled(boolean enabled) {
        this.ownershipValidationEnabled = enabled;
    }
    
    /**
     * Checks if ownership validation is enabled.
     * 
     * @return true if ownership validation is enabled
     */
    public boolean isOwnershipValidationEnabled() {
        return ownershipValidationEnabled;
    }
    
    /**
     * Gets the fence grid used by this validator.
     * 
     * @return The FenceGrid instance
     */
    public FenceGrid getGrid() {
        return grid;
    }
    
    /**
     * Gets the material provider used by this validator.
     * 
     * @return The FenceMaterialProvider instance
     */
    public FenceMaterialProvider getMaterialProvider() {
        return materialProvider;
    }
    
    /**
     * Gets the structure manager used by this validator.
     * 
     * @return The FenceStructureManager instance
     */
    public FenceStructureManager getStructureManager() {
        return structureManager;
    }
    
    /**
     * Result of a validation operation.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        /**
         * Creates a valid validation result.
         * 
         * @return Valid ValidationResult
         */
        public static ValidationResult valid() {
            return new ValidationResult(true, null);
        }
        
        /**
         * Creates an invalid validation result with an error message.
         * 
         * @param errorMessage Error message describing why validation failed
         * @return Invalid ValidationResult
         */
        public static ValidationResult invalid(String errorMessage) {
            return new ValidationResult(false, errorMessage);
        }
        
        /**
         * Checks if the validation result is valid.
         * 
         * @return true if validation passed
         */
        public boolean isValid() {
            return valid;
        }
        
        /**
         * Gets the error message if validation failed.
         * 
         * @return Error message, or null if validation passed
         */
        public String getErrorMessage() {
            return errorMessage;
        }
        
        @Override
        public String toString() {
            return valid ? "Valid" : "Invalid: " + errorMessage;
        }
    }
}