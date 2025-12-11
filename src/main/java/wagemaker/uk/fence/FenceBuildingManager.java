package wagemaker.uk.fence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import java.awt.Point;
import java.util.List;

/**
 * Central coordinator for all fence building operations.
 * Manages building mode state, handles input, and coordinates between
 * structure management, validation, and rendering systems.
 */
public class FenceBuildingManager {
    
    /** Flag indicating if building mode is currently active */
    private boolean buildingModeActive;
    
    /** Flag to prevent double disposal */
    private boolean disposed = false;
    
    /** Structure manager for fence data operations */
    private final FenceStructureManager structureManager;
    
    /** Validator for placement and removal operations */
    private final FencePlacementValidator validator;
    
    /** Preview renderer for visual feedback */
    private FencePreviewRenderer previewRenderer;
    
    /** Optimized fence renderer for efficient rendering */
    private FenceRenderer fenceRenderer;
    
    /** Camera reference for input coordinate conversion */
    private final OrthographicCamera camera;
    
    /** Collision manager for fence collision boundaries */
    private FenceCollisionManager collisionManager;
    
    /** Current selected material type for building */
    private FenceMaterialType selectedMaterialType;
    
    /** Last processed input coordinates to prevent duplicate operations */
    private Point lastProcessedGridPos;
    
    /** Flag to track if left mouse button was pressed last frame */
    private boolean leftMousePressed;
    
    /** Flag to track if right mouse button was pressed last frame */
    private boolean rightMousePressed;
    
    /** Key code for toggling building mode (default: B key) */
    private int buildingModeToggleKey;
    
    /** Sound manager for fence operation audio feedback */
    private FenceSoundManager soundManager;
    
    /** Visual effects manager for fence operation visual feedback */
    private FenceVisualEffectsManager visualEffectsManager;
    
    /**
     * Creates a new FenceBuildingManager with the specified dependencies.
     * 
     * @param structureManager Manager for fence structure data
     * @param validator Validator for placement operations
     * @param camera Camera for input coordinate conversion
     */
    public FenceBuildingManager(FenceStructureManager structureManager, 
                               FencePlacementValidator validator,
                               OrthographicCamera camera) {
        this.structureManager = structureManager;
        this.validator = validator;
        this.camera = camera;
        this.buildingModeActive = false;
        this.selectedMaterialType = FenceMaterialType.WOOD; // Default material
        this.lastProcessedGridPos = null;
        this.leftMousePressed = false;
        this.rightMousePressed = false;
        this.buildingModeToggleKey = Input.Keys.B; // Default to B key
        
        // Initialize collision manager
        this.collisionManager = new FenceCollisionManager(structureManager.getGrid(), structureManager);
        
        // Initialize sound manager
        this.soundManager = new FenceSoundManager();
        
        // Initialize optimized fence renderer
        this.fenceRenderer = new FenceRenderer(camera);
        
        // Visual effects manager will be set externally since it requires ShapeRenderer
    }
    
    /**
     * Updates the fence building manager, processing input and managing state.
     * Should be called every frame when the game is active.
     * 
     * @param deltaTime Time elapsed since last frame
     */
    public void update(float deltaTime) {
        // Handle building mode toggle input
        if (Gdx.input.isKeyJustPressed(buildingModeToggleKey)) {
            toggleBuildingMode();
        }
        
        // Only process building input when in building mode
        if (buildingModeActive) {
            processBuildingInput();
        }
        
        // Update visual effects
        if (visualEffectsManager != null) {
            visualEffectsManager.update(deltaTime);
        }
    }
    
    /**
     * Renders all fence structures using the optimized renderer.
     * Should be called during the rendering phase.
     */
    public void renderFences() {
        if (fenceRenderer != null) {
            fenceRenderer.render(structureManager);
        }
    }
    
    /**
     * Renders visual effects for fence building operations.
     * Should be called during the rendering phase.
     */
    public void renderVisualEffects() {
        if (visualEffectsManager != null) {
            visualEffectsManager.render();
        }
    }
    
    /**
     * Toggles building mode on or off.
     * Handles state transitions and validation.
     */
    public void toggleBuildingMode() {
        if (buildingModeActive) {
            exitBuildingMode();
        } else {
            enterBuildingMode();
        }
    }
    
    /**
     * Enters building mode if conditions are met.
     * Validates material availability before allowing entry.
     */
    private void enterBuildingMode() {
        // Check if player has any fence materials
        FenceMaterialProvider materialProvider = validator.getMaterialProvider();
        if (materialProvider != null) {
            boolean hasWood = materialProvider.hasEnoughMaterials(FenceMaterialType.WOOD, 1);
            boolean hasBamboo = materialProvider.hasEnoughMaterials(FenceMaterialType.BAMBOO, 1);
            
            if (!hasWood && !hasBamboo) {
                // No materials available, show message and prevent entry
                System.out.println("Cannot enter building mode: No fence materials available");
                return;
            }
            
            // Set selected material to the first available type
            if (hasWood) {
                selectedMaterialType = FenceMaterialType.WOOD;
            } else {
                selectedMaterialType = FenceMaterialType.BAMBOO;
            }
        }
        
        buildingModeActive = true;
        lastProcessedGridPos = null;
        leftMousePressed = false;
        rightMousePressed = false;
        
        System.out.println("Entered fence building mode - Press " + Input.Keys.toString(buildingModeToggleKey) + " to exit");
        System.out.println("Left click to place fence, Right click to remove fence");
        System.out.println("Current material: " + selectedMaterialType.getDisplayName());
    }
    
    /**
     * Exits building mode and returns to normal gameplay.
     */
    private void exitBuildingMode() {
        buildingModeActive = false;
        lastProcessedGridPos = null;
        leftMousePressed = false;
        rightMousePressed = false;
        
        System.out.println("Exited fence building mode");
    }
    
    /**
     * Processes building input when in building mode.
     * Handles mouse clicks for placement and removal operations.
     */
    private void processBuildingInput() {
        // Get current mouse input state
        boolean currentLeftPressed = Gdx.input.isButtonPressed(Input.Buttons.LEFT);
        boolean currentRightPressed = Gdx.input.isButtonPressed(Input.Buttons.RIGHT);
        
        // Detect button press events (not held)
        boolean leftClicked = currentLeftPressed && !leftMousePressed;
        boolean rightClicked = currentRightPressed && !rightMousePressed;
        
        // Update button state for next frame
        leftMousePressed = currentLeftPressed;
        rightMousePressed = currentRightPressed;
        
        // Get mouse position in world coordinates
        Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mousePos);
        
        // Convert to grid coordinates
        Point gridPos = structureManager.getGrid().worldToGrid(mousePos.x, mousePos.y);
        
        // Prevent duplicate operations on the same grid position
        if (gridPos.equals(lastProcessedGridPos)) {
            return;
        }
        
        // Handle placement (left click)
        if (leftClicked) {
            if (placeFenceSegment(gridPos.x, gridPos.y)) {
                lastProcessedGridPos = new Point(gridPos.x, gridPos.y);
            }
        }
        
        // Handle removal (right click)
        if (rightClicked) {
            if (removeFenceSegment(gridPos.x, gridPos.y)) {
                lastProcessedGridPos = new Point(gridPos.x, gridPos.y);
            }
        }
        
        // Reset last processed position if mouse moved to different grid cell
        if (!gridPos.equals(lastProcessedGridPos)) {
            lastProcessedGridPos = null;
        }
    }
    
    /**
     * Attempts to place a fence segment at the specified grid coordinates.
     * Implements click-to-place functionality with validation and error handling.
     * Automatically selects the correct piece type based on position.
     * 
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     * @return true if placement was successful, false otherwise
     */
    public boolean placeFenceSegment(int gridX, int gridY) {
        Point gridPos = new Point(gridX, gridY);
        
        // Validate placement with comprehensive error handling
        FencePlacementValidator.ValidationResult result = 
            validator.validatePlacement(gridPos, selectedMaterialType, "local_player");
        
        if (!result.isValid()) {
            // Provide detailed error feedback to the user
            displayPlacementError(result.getErrorMessage(), gridPos);
            // Play error sound
            if (soundManager != null) {
                soundManager.playErrorSound();
            }
            // Trigger error animation
            if (visualEffectsManager != null) {
                visualEffectsManager.triggerErrorAnimation(gridPos, result.getErrorMessage());
            }
            return false;
        }
        
        try {
            // Attempt to place the fence piece with automatic piece type selection
            FencePiece placedPiece = structureManager.addFencePiece(gridPos, selectedMaterialType);
            
            if (placedPiece != null) {
                // Consume materials from inventory
                FenceMaterialProvider materialProvider = validator.getMaterialProvider();
                if (materialProvider != null) {
                    materialProvider.consumeMaterials(selectedMaterialType, 1);
                    
                    // Update collision boundaries immediately
                    updateCollisionBoundaries(gridPos, true);
                    
                    // Provide success feedback
                    displayPlacementSuccess(placedPiece, gridPos);
                    // Play placement sound
                    if (soundManager != null) {
                        soundManager.playPlacementSound();
                    }
                    // Trigger placement animation
                    if (visualEffectsManager != null) {
                        visualEffectsManager.triggerPlacementAnimation(gridPos, selectedMaterialType);
                    }
                    return true;
                } else {
                    // Rollback placement if material consumption fails
                    structureManager.removeFencePiece(gridPos);
                    displayPlacementError("Failed to consume materials", gridPos);
                    // Play error sound
                    if (soundManager != null) {
                        soundManager.playErrorSound();
                    }
                    return false;
                }
            } else {
                displayPlacementError("Failed to create fence piece", gridPos);
                // Play error sound
                if (soundManager != null) {
                    soundManager.playErrorSound();
                }
                return false;
            }
        } catch (Exception e) {
            // Handle any unexpected errors during placement
            displayPlacementError("Unexpected error during placement: " + e.getMessage(), gridPos);
            // Play error sound
            if (soundManager != null) {
                soundManager.playErrorSound();
            }
            return false;
        }
    }
    
    /**
     * Displays placement error feedback to the user.
     * 
     * @param errorMessage The error message to display
     * @param gridPos The grid position where placement failed
     */
    private void displayPlacementError(String errorMessage, Point gridPos) {
        System.out.println("Placement failed at (" + gridPos.x + ", " + gridPos.y + "): " + errorMessage);
        // In a full implementation, this would show UI feedback or play error sounds
    }
    
    /**
     * Displays placement success feedback to the user.
     * 
     * @param placedPiece The fence piece that was placed
     * @param gridPos The grid position where placement succeeded
     */
    private void displayPlacementSuccess(FencePiece placedPiece, Point gridPos) {
        System.out.println("Successfully placed " + placedPiece.getType() + " at (" + 
                          gridPos.x + ", " + gridPos.y + ")");
        
        // Display remaining materials
        FenceMaterialProvider materialProvider = validator.getMaterialProvider();
        if (materialProvider != null) {
            int remaining = materialProvider.getMaterialCount(selectedMaterialType);
            System.out.println("Remaining " + selectedMaterialType.getDisplayName() + ": " + remaining);
        }
        
        // In a full implementation, this would play placement sounds and show visual feedback
    }
    
    /**
     * Attempts to remove a fence segment at the specified grid coordinates.
     * Implements right-click removal functionality with material return.
     * Updates adjacent piece connections after removal.
     * 
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     * @return true if removal was successful, false otherwise
     */
    public boolean removeFenceSegment(int gridX, int gridY) {
        Point gridPos = new Point(gridX, gridY);
        
        // Validate removal with comprehensive error handling
        FencePlacementValidator.ValidationResult result = 
            validator.validateRemoval(gridPos, "local_player");
        
        if (!result.isValid()) {
            displayRemovalError(result.getErrorMessage(), gridPos);
            // Play error sound
            if (soundManager != null) {
                soundManager.playErrorSound();
            }
            // Trigger error animation
            if (visualEffectsManager != null) {
                visualEffectsManager.triggerErrorAnimation(gridPos, result.getErrorMessage());
            }
            return false;
        }
        
        // Get the piece before removal to determine material type
        FencePiece pieceToRemove = structureManager.getFencePiece(gridPos);
        if (pieceToRemove == null) {
            displayRemovalError("No fence piece found at position", gridPos);
            // Play error sound
            if (soundManager != null) {
                soundManager.playErrorSound();
            }
            return false;
        }
        
        // Store material type for return (in a full implementation, this would be stored per piece)
        FenceMaterialType materialToReturn = selectedMaterialType;
        
        try {
            // Attempt to remove the fence piece
            FencePiece removedPiece = structureManager.removeFencePiece(gridPos);
            
            if (removedPiece != null) {
                // Return materials to inventory
                FenceMaterialProvider materialProvider = validator.getMaterialProvider();
                if (materialProvider != null) {
                    materialProvider.returnMaterials(materialToReturn, 1);
                }
                
                // Update collision boundaries immediately
                updateCollisionBoundaries(gridPos, false);
                
                // Update adjacent piece connections (handled by structureManager.removeFencePiece)
                // The structure manager automatically updates connections when pieces are removed
                
                // Provide success feedback
                displayRemovalSuccess(removedPiece, gridPos, materialToReturn);
                // Play removal sound
                if (soundManager != null) {
                    soundManager.playRemovalSound();
                }
                // Trigger removal animation
                if (visualEffectsManager != null) {
                    visualEffectsManager.triggerRemovalAnimation(gridPos, materialToReturn);
                }
                return true;
            } else {
                displayRemovalError("Failed to remove fence piece", gridPos);
                // Play error sound
                if (soundManager != null) {
                    soundManager.playErrorSound();
                }
                return false;
            }
        } catch (Exception e) {
            // Handle any unexpected errors during removal
            displayRemovalError("Unexpected error during removal: " + e.getMessage(), gridPos);
            // Play error sound
            if (soundManager != null) {
                soundManager.playErrorSound();
            }
            return false;
        }
    }
    
    /**
     * Displays removal error feedback to the user.
     * 
     * @param errorMessage The error message to display
     * @param gridPos The grid position where removal failed
     */
    private void displayRemovalError(String errorMessage, Point gridPos) {
        System.out.println("Removal failed at (" + gridPos.x + ", " + gridPos.y + "): " + errorMessage);
        // In a full implementation, this would show UI feedback or play error sounds
    }
    
    /**
     * Displays removal success feedback to the user.
     * 
     * @param removedPiece The fence piece that was removed
     * @param gridPos The grid position where removal succeeded
     * @param materialReturned The type of material returned to inventory
     */
    private void displayRemovalSuccess(FencePiece removedPiece, Point gridPos, FenceMaterialType materialReturned) {
        System.out.println("Successfully removed " + removedPiece.getType() + " at (" + 
                          gridPos.x + ", " + gridPos.y + ")");
        
        // Display updated materials
        FenceMaterialProvider materialProvider = validator.getMaterialProvider();
        if (materialProvider != null) {
            int current = materialProvider.getMaterialCount(materialReturned);
            System.out.println("Returned 1 " + materialReturned.getDisplayName() + 
                             " (now have: " + current + ")");
        }
        
        // In a full implementation, this would play removal sounds and show visual feedback
    }
    
    /**
     * Calculates material requirements for a rectangular enclosure.
     * 
     * @param area Rectangle defining the enclosure area
     * @return Material requirement information
     */
    public EnclosureRequirement calculateEnclosureRequirements(Rectangle area) {
        if (area.width < 2 || area.height < 2) {
            return new EnclosureRequirement(0, "Area too small for enclosure (minimum 2x2)");
        }
        
        // Calculate perimeter pieces needed
        int piecesNeeded = FencePieceFactory.calculateMaterialRequirement(area);
        
        // Check material availability
        FenceMaterialProvider materialProvider = validator.getMaterialProvider();
        boolean canAfford = false;
        if (materialProvider != null) {
            canAfford = materialProvider.hasEnoughMaterials(selectedMaterialType, piecesNeeded);
        }
        
        return new EnclosureRequirement(piecesNeeded, canAfford ? null : "Insufficient materials");
    }
    
    /**
     * Checks if building mode is currently active.
     * 
     * @return true if in building mode
     */
    public boolean isBuildingModeActive() {
        return buildingModeActive;
    }
    
    /**
     * Gets the currently selected material type.
     * 
     * @return Current material type
     */
    public FenceMaterialType getSelectedMaterialType() {
        return selectedMaterialType;
    }
    
    /**
     * Sets the selected material type for building.
     * 
     * @param materialType Material type to select
     */
    public void setSelectedMaterialType(FenceMaterialType materialType) {
        this.selectedMaterialType = materialType;
    }
    
    /**
     * Gets the key code used for toggling building mode.
     * 
     * @return Key code for building mode toggle
     */
    public int getBuildingModeToggleKey() {
        return buildingModeToggleKey;
    }
    
    /**
     * Sets the key code for toggling building mode.
     * 
     * @param keyCode Key code to use for toggle
     */
    public void setBuildingModeToggleKey(int keyCode) {
        this.buildingModeToggleKey = keyCode;
    }
    
    /**
     * Sets the preview renderer for visual feedback.
     * 
     * @param previewRenderer Preview renderer instance
     */
    public void setPreviewRenderer(FencePreviewRenderer previewRenderer) {
        this.previewRenderer = previewRenderer;
    }
    
    /**
     * Gets the preview renderer.
     * 
     * @return Preview renderer instance
     */
    public FencePreviewRenderer getPreviewRenderer() {
        return previewRenderer;
    }
    
    /**
     * Gets the structure manager.
     * 
     * @return Structure manager instance
     */
    public FenceStructureManager getStructureManager() {
        return structureManager;
    }
    
    /**
     * Gets the placement validator.
     * 
     * @return Placement validator instance
     */
    public FencePlacementValidator getValidator() {
        return validator;
    }
    
    /**
     * Gets the collision manager.
     * 
     * @return Collision manager instance
     */
    public FenceCollisionManager getCollisionManager() {
        return collisionManager;
    }
    
    /**
     * Gets the sound manager.
     * 
     * @return Sound manager instance
     */
    public FenceSoundManager getSoundManager() {
        return soundManager;
    }
    
    /**
     * Sets the sound manager.
     * 
     * @param soundManager Sound manager instance
     */
    public void setSoundManager(FenceSoundManager soundManager) {
        this.soundManager = soundManager;
    }
    
    /**
     * Gets the optimized fence renderer.
     * 
     * @return Fence renderer instance
     */
    public FenceRenderer getFenceRenderer() {
        return fenceRenderer;
    }
    
    /**
     * Sets the fence renderer.
     * 
     * @param fenceRenderer Fence renderer instance
     */
    public void setFenceRenderer(FenceRenderer fenceRenderer) {
        this.fenceRenderer = fenceRenderer;
    }
    
    /**
     * Gets the visual effects manager.
     * 
     * @return Visual effects manager instance
     */
    public FenceVisualEffectsManager getVisualEffectsManager() {
        return visualEffectsManager;
    }
    
    /**
     * Sets the visual effects manager.
     * 
     * @param visualEffectsManager Visual effects manager instance
     */
    public void setVisualEffectsManager(FenceVisualEffectsManager visualEffectsManager) {
        this.visualEffectsManager = visualEffectsManager;
    }
    
    /**
     * Checks if a point collides with any fence boundaries.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @return true if the point collides with a fence boundary
     */
    public boolean checkFenceCollision(float x, float y) {
        return collisionManager != null && collisionManager.checkCollision(x, y);
    }
    
    /**
     * Checks if a rectangle collides with any fence boundaries.
     * 
     * @param testRect Rectangle to test for collision
     * @return true if the rectangle collides with a fence boundary
     */
    public boolean checkFenceCollision(Rectangle testRect) {
        return collisionManager != null && collisionManager.checkCollision(testRect);
    }
    
    /**
     * Updates collision boundaries when fence pieces are placed or removed.
     * Manages collision rectangle generation and collision map updates.
     * 
     * @param gridPos Grid position that was modified
     * @param placed true if a piece was placed, false if removed
     */
    private void updateCollisionBoundaries(Point gridPos, boolean placed) {
        if (collisionManager == null) {
            return; // Safety check
        }
        
        if (placed) {
            // Add collision boundary for the placed piece
            FencePiece piece = structureManager.getFencePiece(gridPos);
            if (piece != null) {
                collisionManager.addCollisionBoundary(gridPos, piece);
            }
        } else {
            // Remove collision boundary for the removed piece
            collisionManager.removeCollisionBoundary(gridPos);
        }
    }
    
    /**
     * Disposes of resources used by this manager.
     */
    public void dispose() {
        if (disposed) {
            return; // Already disposed, prevent double disposal
        }
        
        disposed = true;
        
        if (previewRenderer != null) {
            previewRenderer.dispose();
            previewRenderer = null;
        }
        if (fenceRenderer != null) {
            fenceRenderer.dispose();
            fenceRenderer = null;
        }
        if (collisionManager != null) {
            collisionManager.clear();
            collisionManager = null;
        }
        if (soundManager != null) {
            soundManager.dispose();
            soundManager = null;
        }
    }
    
    /**
     * Information about enclosure material requirements.
     */
    public static class EnclosureRequirement {
        private final int piecesNeeded;
        private final String errorMessage;
        
        public EnclosureRequirement(int piecesNeeded, String errorMessage) {
            this.piecesNeeded = piecesNeeded;
            this.errorMessage = errorMessage;
        }
        
        public int getPiecesNeeded() {
            return piecesNeeded;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
        
        public boolean isValid() {
            return errorMessage == null;
        }
        
        @Override
        public String toString() {
            return isValid() ? 
                "Pieces needed: " + piecesNeeded : 
                "Error: " + errorMessage;
        }
    }
}