package wagemaker.uk.fence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.awt.Point;

/**
 * Renders a grid overlay for fence building mode.
 * Shows valid placement positions and highlights occupied cells.
 */
public class FenceGridOverlay {
    
    /** Shape renderer for drawing grid lines */
    private final ShapeRenderer shapeRenderer;
    
    /** Structure manager for checking occupied positions */
    private final FenceStructureManager structureManager;
    
    /** Grid system for coordinate conversion */
    private final FenceGrid grid;
    
    /** Grid visual settings */
    private static final Color GRID_LINE_COLOR = new Color(1, 1, 1, 0.2f); // Semi-transparent white
    private static final Color OCCUPIED_CELL_COLOR = new Color(0, 1, 0, 0.3f); // Semi-transparent green
    private static final Color INVALID_CELL_COLOR = new Color(1, 0, 0, 0.3f); // Semi-transparent red
    private static final float GRID_LINE_WIDTH = 1.0f;
    
    /** Grid size in pixels */
    private static final int GRID_SIZE = 64;
    
    /** Visibility flag */
    private boolean visible;
    
    /**
     * Creates a new fence grid overlay.
     * 
     * @param shapeRenderer Shape renderer for drawing
     * @param structureManager Structure manager for data access
     */
    public FenceGridOverlay(ShapeRenderer shapeRenderer, FenceStructureManager structureManager) {
        this.shapeRenderer = shapeRenderer;
        this.structureManager = structureManager;
        this.grid = structureManager.getGrid();
        this.visible = false;
    }
    
    /**
     * Renders the grid overlay.
     * 
     * @param camera Camera for coordinate conversion
     * @param viewport Viewport for screen dimensions
     */
    public void render(OrthographicCamera camera, Viewport viewport) {
        if (!visible) {
            return;
        }
        
        // Set up rendering
        shapeRenderer.setProjectionMatrix(camera.combined);
        
        // Calculate visible area
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();
        
        // Calculate grid bounds (expand slightly to ensure coverage)
        int startGridX = (int)((camX - viewWidth / 2 - GRID_SIZE) / GRID_SIZE);
        int startGridY = (int)((camY - viewHeight / 2 - GRID_SIZE) / GRID_SIZE);
        int endGridX = (int)((camX + viewWidth / 2 + GRID_SIZE) / GRID_SIZE);
        int endGridY = (int)((camY + viewHeight / 2 + GRID_SIZE) / GRID_SIZE);
        
        // Render occupied cells first (filled rectangles)
        renderOccupiedCells(startGridX, startGridY, endGridX, endGridY);
        
        // Render grid lines on top
        renderGridLines(startGridX, startGridY, endGridX, endGridY);
    }
    
    /**
     * Renders filled rectangles for occupied grid cells.
     * 
     * @param startGridX Starting grid X coordinate
     * @param startGridY Starting grid Y coordinate
     * @param endGridX Ending grid X coordinate
     * @param endGridY Ending grid Y coordinate
     */
    private void renderOccupiedCells(int startGridX, int startGridY, int endGridX, int endGridY) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (int gridX = startGridX; gridX <= endGridX; gridX++) {
            for (int gridY = startGridY; gridY <= endGridY; gridY++) {
                Point gridPos = new Point(gridX, gridY);
                
                if (grid.isOccupied(gridPos)) {
                    // Cell is occupied - render in green
                    shapeRenderer.setColor(OCCUPIED_CELL_COLOR);
                    Vector2 worldPos = grid.gridToWorld(gridPos);
                    shapeRenderer.rect(worldPos.x, worldPos.y, GRID_SIZE, GRID_SIZE);
                } else if (!grid.isValidPlacement(gridPos)) {
                    // Cell is invalid for placement - render in red
                    shapeRenderer.setColor(INVALID_CELL_COLOR);
                    Vector2 worldPos = grid.gridToWorld(gridPos);
                    shapeRenderer.rect(worldPos.x, worldPos.y, GRID_SIZE, GRID_SIZE);
                }
            }
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Renders grid lines.
     * 
     * @param startGridX Starting grid X coordinate
     * @param startGridY Starting grid Y coordinate
     * @param endGridX Ending grid X coordinate
     * @param endGridY Ending grid Y coordinate
     */
    private void renderGridLines(int startGridX, int startGridY, int endGridX, int endGridY) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(GRID_LINE_COLOR);
        
        // Convert grid coordinates to world coordinates for rendering
        int startWorldX = startGridX * GRID_SIZE;
        int startWorldY = startGridY * GRID_SIZE;
        int endWorldX = (endGridX + 1) * GRID_SIZE;
        int endWorldY = (endGridY + 1) * GRID_SIZE;
        
        // Draw vertical lines
        for (int gridX = startGridX; gridX <= endGridX + 1; gridX++) {
            int worldX = gridX * GRID_SIZE;
            shapeRenderer.line(worldX, startWorldY, worldX, endWorldY);
        }
        
        // Draw horizontal lines
        for (int gridY = startGridY; gridY <= endGridY + 1; gridY++) {
            int worldY = gridY * GRID_SIZE;
            shapeRenderer.line(startWorldX, worldY, endWorldX, worldY);
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Renders a highlight for a specific grid cell.
     * Useful for showing hover effects or selected cells.
     * 
     * @param gridPos Grid position to highlight
     * @param color Color for the highlight
     */
    public void renderCellHighlight(Point gridPos, Color color) {
        if (!visible) {
            return;
        }
        
        Vector2 worldPos = grid.gridToWorld(gridPos);
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(worldPos.x, worldPos.y, GRID_SIZE, GRID_SIZE);
        shapeRenderer.end();
    }
    
    /**
     * Sets the visibility of the grid overlay.
     * 
     * @param visible true to show the overlay, false to hide
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    /**
     * Checks if the grid overlay is currently visible.
     * 
     * @return true if visible
     */
    public boolean isVisible() {
        return visible;
    }
    
    /**
     * Gets the grid size in pixels.
     * 
     * @return Grid size in pixels
     */
    public static int getGridSize() {
        return GRID_SIZE;
    }
    
    /**
     * Disposes of resources used by this overlay.
     */
    public void dispose() {
        // Shape renderer is typically shared, so we don't dispose it here
    }
}