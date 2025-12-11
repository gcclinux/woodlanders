package wagemaker.uk.fence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.awt.Point;

/**
 * Renders visual feedback for fence building operations.
 * Provides preview rendering, hover effects, and invalid placement indicators.
 */
public class FencePreviewRenderer {
    
    /** Shape renderer for drawing preview outlines */
    private final ShapeRenderer shapeRenderer;
    
    /** Structure manager for checking existing pieces */
    private final FenceStructureManager structureManager;
    
    /** Validator for checking placement validity */
    private final FencePlacementValidator validator;
    
    /** Colors for different preview states */
    private static final Color VALID_PREVIEW_COLOR = new Color(0, 1, 0, 0.5f); // Semi-transparent green
    private static final Color INVALID_PREVIEW_COLOR = new Color(1, 0, 0, 0.5f); // Semi-transparent red
    private static final Color HOVER_COLOR = new Color(1, 1, 1, 0.3f); // Semi-transparent white
    
    /** Size of fence pieces in pixels */
    private static final float FENCE_PIECE_SIZE = 64f;
    
    /**
     * Creates a new fence preview renderer.
     * 
     * @param shapeRenderer Shape renderer for drawing
     * @param structureManager Structure manager for data access
     * @param validator Validator for placement checking
     */
    public FencePreviewRenderer(ShapeRenderer shapeRenderer, 
                               FenceStructureManager structureManager,
                               FencePlacementValidator validator) {
        this.shapeRenderer = shapeRenderer;
        this.structureManager = structureManager;
        this.validator = validator;
    }
    
    /**
     * Renders a preview of a fence piece at the specified world coordinates.
     * 
     * @param worldX World X coordinate
     * @param worldY World Y coordinate
     * @param materialType Material type for the preview
     * @param playerId Player ID for validation
     */
    public void renderPreview(float worldX, float worldY, 
                             FenceMaterialType materialType, 
                             String playerId) {
        // Convert world coordinates to grid coordinates
        Point gridPos = structureManager.getGrid().worldToGrid(worldX, worldY);
        
        // Validate placement to determine preview color
        FencePlacementValidator.ValidationResult result = 
            validator.validatePlacement(gridPos, materialType, playerId);
        
        Color previewColor = result.isValid() ? VALID_PREVIEW_COLOR : INVALID_PREVIEW_COLOR;
        
        // Convert grid coordinates back to world coordinates for rendering
        Vector2 renderPos = structureManager.getGrid().gridToWorld(gridPos);
        
        // Render preview rectangle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(previewColor);
        shapeRenderer.rect(renderPos.x, renderPos.y, FENCE_PIECE_SIZE, FENCE_PIECE_SIZE);
        shapeRenderer.end();
        
        // Render border for better visibility
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(previewColor.r, previewColor.g, previewColor.b, 1.0f); // Full opacity for border
        shapeRenderer.rect(renderPos.x, renderPos.y, FENCE_PIECE_SIZE, FENCE_PIECE_SIZE);
        shapeRenderer.end();
    }
    
    /**
     * Renders a hover effect at the specified world coordinates.
     * 
     * @param worldX World X coordinate
     * @param worldY World Y coordinate
     */
    public void renderHoverEffect(float worldX, float worldY) {
        // Convert world coordinates to grid coordinates
        Point gridPos = structureManager.getGrid().worldToGrid(worldX, worldY);
        
        // Convert grid coordinates back to world coordinates for rendering
        Vector2 renderPos = structureManager.getGrid().gridToWorld(gridPos);
        
        // Render hover rectangle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(HOVER_COLOR);
        shapeRenderer.rect(renderPos.x, renderPos.y, FENCE_PIECE_SIZE, FENCE_PIECE_SIZE);
        shapeRenderer.end();
    }
    
    /**
     * Renders an invalid placement indicator with error message.
     * 
     * @param batch Sprite batch for text rendering
     * @param worldX World X coordinate
     * @param worldY World Y coordinate
     * @param errorMessage Error message to display
     */
    public void renderInvalidIndicator(SpriteBatch batch, float worldX, float worldY, String errorMessage) {
        // Convert world coordinates to grid coordinates
        Point gridPos = structureManager.getGrid().worldToGrid(worldX, worldY);
        
        // Convert grid coordinates back to world coordinates for rendering
        Vector2 renderPos = structureManager.getGrid().gridToWorld(gridPos);
        
        // Render invalid placement rectangle
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(INVALID_PREVIEW_COLOR);
        shapeRenderer.rect(renderPos.x, renderPos.y, FENCE_PIECE_SIZE, FENCE_PIECE_SIZE);
        shapeRenderer.end();
        
        // Render X mark to indicate invalid placement
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        float centerX = renderPos.x + FENCE_PIECE_SIZE / 2;
        float centerY = renderPos.y + FENCE_PIECE_SIZE / 2;
        float halfSize = FENCE_PIECE_SIZE / 4;
        
        // Draw X
        shapeRenderer.line(centerX - halfSize, centerY - halfSize, centerX + halfSize, centerY + halfSize);
        shapeRenderer.line(centerX - halfSize, centerY + halfSize, centerX + halfSize, centerY - halfSize);
        shapeRenderer.end();
        
        // Note: Error message rendering would require a font, which we'll handle in the UI components
    }
    
    /**
     * Renders visual feedback for a complete enclosure preview.
     * 
     * @param enclosurePositions Array of grid positions forming the enclosure
     * @param materialType Material type for validation
     * @param playerId Player ID for validation
     */
    public void renderEnclosurePreview(Point[] enclosurePositions, 
                                      FenceMaterialType materialType, 
                                      String playerId) {
        if (enclosurePositions == null || enclosurePositions.length == 0) {
            return;
        }
        
        // Validate the entire enclosure
        boolean allValid = true;
        for (Point pos : enclosurePositions) {
            FencePlacementValidator.ValidationResult result = 
                validator.validatePlacement(pos, materialType, playerId);
            if (!result.isValid()) {
                allValid = false;
                break;
            }
        }
        
        Color previewColor = allValid ? VALID_PREVIEW_COLOR : INVALID_PREVIEW_COLOR;
        
        // Render all positions in the enclosure
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(previewColor);
        
        for (Point pos : enclosurePositions) {
            Vector2 renderPos = structureManager.getGrid().gridToWorld(pos);
            shapeRenderer.rect(renderPos.x, renderPos.y, FENCE_PIECE_SIZE, FENCE_PIECE_SIZE);
        }
        
        shapeRenderer.end();
        
        // Render borders
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(previewColor.r, previewColor.g, previewColor.b, 1.0f);
        
        for (Point pos : enclosurePositions) {
            Vector2 renderPos = structureManager.getGrid().gridToWorld(pos);
            shapeRenderer.rect(renderPos.x, renderPos.y, FENCE_PIECE_SIZE, FENCE_PIECE_SIZE);
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Renders hover effects for the current mouse position.
     * 
     * @param mouseWorldX Mouse X coordinate in world space
     * @param mouseWorldY Mouse Y coordinate in world space
     */
    public void renderMouseHover(float mouseWorldX, float mouseWorldY) {
        // Convert mouse position to grid coordinates
        Point gridPos = structureManager.getGrid().worldToGrid(mouseWorldX, mouseWorldY);
        
        // Convert back to world coordinates for consistent rendering
        Vector2 renderPos = structureManager.getGrid().gridToWorld(gridPos);
        
        // Render hover outline
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(HOVER_COLOR);
        shapeRenderer.rect(renderPos.x, renderPos.y, FENCE_PIECE_SIZE, FENCE_PIECE_SIZE);
        shapeRenderer.end();
    }
    
    /**
     * Renders a placement preview with validation feedback.
     * 
     * @param mouseWorldX Mouse X coordinate in world space
     * @param mouseWorldY Mouse Y coordinate in world space
     * @param materialType Material type for validation
     * @param playerId Player ID for validation
     * @return ValidationResult for the preview position
     */
    public FencePlacementValidator.ValidationResult renderPlacementPreview(float mouseWorldX, float mouseWorldY, 
                                                                           FenceMaterialType materialType, 
                                                                           String playerId) {
        // Convert mouse position to grid coordinates
        Point gridPos = structureManager.getGrid().worldToGrid(mouseWorldX, mouseWorldY);
        
        // Validate placement
        FencePlacementValidator.ValidationResult result = 
            validator.validatePlacement(gridPos, materialType, playerId);
        
        // Convert back to world coordinates for rendering
        Vector2 renderPos = structureManager.getGrid().gridToWorld(gridPos);
        
        // Choose color based on validation result
        Color previewColor = result.isValid() ? VALID_PREVIEW_COLOR : INVALID_PREVIEW_COLOR;
        
        // Render filled preview
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(previewColor);
        shapeRenderer.rect(renderPos.x, renderPos.y, FENCE_PIECE_SIZE, FENCE_PIECE_SIZE);
        shapeRenderer.end();
        
        // Render border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(previewColor.r, previewColor.g, previewColor.b, 1.0f);
        shapeRenderer.rect(renderPos.x, renderPos.y, FENCE_PIECE_SIZE, FENCE_PIECE_SIZE);
        shapeRenderer.end();
        
        // If invalid, render additional visual indicators
        if (!result.isValid()) {
            renderInvalidIndicatorSymbol(renderPos.x, renderPos.y);
        }
        
        return result;
    }
    
    /**
     * Renders removal preview for existing fence pieces.
     * 
     * @param mouseWorldX Mouse X coordinate in world space
     * @param mouseWorldY Mouse Y coordinate in world space
     * @param playerId Player ID for validation
     * @return ValidationResult for the removal operation
     */
    public FencePlacementValidator.ValidationResult renderRemovalPreview(float mouseWorldX, float mouseWorldY, 
                                                                         String playerId) {
        // Convert mouse position to grid coordinates
        Point gridPos = structureManager.getGrid().worldToGrid(mouseWorldX, mouseWorldY);
        
        // Check if there's a fence piece at this position
        FencePiece existingPiece = structureManager.getFencePiece(gridPos);
        if (existingPiece == null) {
            return FencePlacementValidator.ValidationResult.invalid("No fence piece at this position");
        }
        
        // Validate removal
        FencePlacementValidator.ValidationResult result = 
            validator.validateRemoval(gridPos, playerId);
        
        // Convert back to world coordinates for rendering
        Vector2 renderPos = structureManager.getGrid().gridToWorld(gridPos);
        
        // Render removal preview (red overlay)
        Color removalColor = result.isValid() ? 
            new Color(1, 0, 0, 0.5f) : // Semi-transparent red for valid removal
            new Color(0.5f, 0, 0, 0.5f); // Darker red for invalid removal
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(removalColor);
        shapeRenderer.rect(renderPos.x, renderPos.y, FENCE_PIECE_SIZE, FENCE_PIECE_SIZE);
        shapeRenderer.end();
        
        // Render removal indicator (X mark)
        if (result.isValid()) {
            renderRemovalIndicatorSymbol(renderPos.x, renderPos.y);
        }
        
        return result;
    }
    
    /**
     * Renders an X symbol to indicate invalid placement.
     * 
     * @param worldX World X coordinate of the cell
     * @param worldY World Y coordinate of the cell
     */
    private void renderInvalidIndicatorSymbol(float worldX, float worldY) {
        float centerX = worldX + FENCE_PIECE_SIZE / 2;
        float centerY = worldY + FENCE_PIECE_SIZE / 2;
        float halfSize = FENCE_PIECE_SIZE / 4;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        
        // Draw X
        shapeRenderer.line(centerX - halfSize, centerY - halfSize, centerX + halfSize, centerY + halfSize);
        shapeRenderer.line(centerX - halfSize, centerY + halfSize, centerX + halfSize, centerY - halfSize);
        
        shapeRenderer.end();
    }
    
    /**
     * Renders an X symbol to indicate removal operation.
     * 
     * @param worldX World X coordinate of the cell
     * @param worldY World Y coordinate of the cell
     */
    private void renderRemovalIndicatorSymbol(float worldX, float worldY) {
        float centerX = worldX + FENCE_PIECE_SIZE / 2;
        float centerY = worldY + FENCE_PIECE_SIZE / 2;
        float halfSize = FENCE_PIECE_SIZE / 3;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.WHITE);
        
        // Draw thicker X for removal
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                shapeRenderer.line(centerX - halfSize + i, centerY - halfSize + j, 
                                 centerX + halfSize + i, centerY + halfSize + j);
                shapeRenderer.line(centerX - halfSize + i, centerY + halfSize + j, 
                                 centerX + halfSize + i, centerY - halfSize + j);
            }
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Renders connection indicators showing how a new piece would connect to existing pieces.
     * 
     * @param gridPos Grid position of the potential new piece
     */
    public void renderConnectionIndicators(Point gridPos) {
        // Get adjacent positions
        java.util.List<Point> adjacentPositions = structureManager.getGrid().getAdjacentPositions(gridPos);
        
        Vector2 centerPos = structureManager.getGrid().gridToWorld(gridPos);
        float centerX = centerPos.x + FENCE_PIECE_SIZE / 2;
        float centerY = centerPos.y + FENCE_PIECE_SIZE / 2;
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN);
        
        for (Point adjacentPos : adjacentPositions) {
            if (structureManager.getGrid().isOccupied(adjacentPos)) {
                // Draw connection line to adjacent occupied cell
                Vector2 adjacentWorldPos = structureManager.getGrid().gridToWorld(adjacentPos);
                float adjacentCenterX = adjacentWorldPos.x + FENCE_PIECE_SIZE / 2;
                float adjacentCenterY = adjacentWorldPos.y + FENCE_PIECE_SIZE / 2;
                
                shapeRenderer.line(centerX, centerY, adjacentCenterX, adjacentCenterY);
            }
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Disposes of resources used by this renderer.
     */
    public void dispose() {
        // Shape renderer is typically shared, so we don't dispose it here
        // Individual implementations may override this if they own resources
    }
}