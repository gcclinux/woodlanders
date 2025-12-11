package wagemaker.uk.fence;

import com.badlogic.gdx.math.Rectangle;
import java.awt.Point;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages collision boundaries for fence pieces in the custom fence building system.
 * Handles collision rectangle generation, collision map updates, and continuous
 * collision boundaries for complete enclosures.
 */
public class FenceCollisionManager {
    
    /** Map of grid positions to their collision rectangles */
    private final Map<Point, Rectangle> collisionBoundaries;
    
    /** Set of all collision rectangles for efficient collision checking */
    private final Set<Rectangle> allCollisionRects;
    
    /** Reference to the fence grid for coordinate conversion */
    private final FenceGrid grid;
    
    /** Reference to the structure manager for fence piece information */
    private final FenceStructureManager structureManager;
    
    /** Cache of enclosure collision boundaries for performance */
    private final Map<FenceEnclosure, List<Rectangle>> enclosureCollisionCache;
    
    /**
     * Creates a new FenceCollisionManager.
     * 
     * @param grid The fence grid system
     * @param structureManager The fence structure manager
     */
    public FenceCollisionManager(FenceGrid grid, FenceStructureManager structureManager) {
        this.grid = grid;
        this.structureManager = structureManager;
        this.collisionBoundaries = new ConcurrentHashMap<>();
        this.allCollisionRects = Collections.synchronizedSet(new HashSet<>());
        this.enclosureCollisionCache = new ConcurrentHashMap<>();
    }
    
    /**
     * Updates collision boundaries when a fence piece is placed.
     * Generates collision rectangle and adds it to the collision map.
     * 
     * @param gridPos Grid position where the fence piece was placed
     * @param piece The fence piece that was placed
     */
    public void addCollisionBoundary(Point gridPos, FencePiece piece) {
        if (piece == null) {
            return;
        }
        
        // Generate collision rectangle for the fence piece
        Rectangle collisionRect = generateCollisionRectangle(gridPos, piece);
        
        // Add to collision maps
        collisionBoundaries.put(new Point(gridPos.x, gridPos.y), collisionRect);
        allCollisionRects.add(collisionRect);
        
        // Update enclosure collision boundaries if this piece completes an enclosure
        updateEnclosureCollisions();
        
        System.out.println("Added collision boundary at (" + gridPos.x + ", " + gridPos.y + 
                          ") - " + collisionRect);
    }
    
    /**
     * Updates collision boundaries when a fence piece is removed.
     * Removes collision rectangle from the collision map.
     * 
     * @param gridPos Grid position where the fence piece was removed
     */
    public void removeCollisionBoundary(Point gridPos) {
        Rectangle removedRect = collisionBoundaries.remove(gridPos);
        
        if (removedRect != null) {
            allCollisionRects.remove(removedRect);
            
            // Update enclosure collision boundaries
            updateEnclosureCollisions();
            
            System.out.println("Removed collision boundary at (" + gridPos.x + ", " + gridPos.y + ")");
        }
    }
    
    /**
     * Generates a collision rectangle for a fence piece at the specified position.
     * The collision rectangle represents the area that blocks movement.
     * 
     * @param gridPos Grid position of the fence piece
     * @param piece The fence piece
     * @return Rectangle representing the collision boundary
     */
    private Rectangle generateCollisionRectangle(Point gridPos, FencePiece piece) {
        // Convert grid position to world coordinates
        com.badlogic.gdx.math.Vector2 worldPos = grid.gridToWorld(gridPos);
        
        // Create collision rectangle based on piece type
        Rectangle collisionRect;
        
        if (piece.getType().isCornerPiece()) {
            // Corner pieces have full collision boundaries
            collisionRect = new Rectangle(worldPos.x, worldPos.y, FenceGrid.GRID_SIZE, FenceGrid.GRID_SIZE);
        } else {
            // Edge pieces have directional collision boundaries
            collisionRect = generateDirectionalCollisionRect(worldPos, piece.getType());
        }
        
        return collisionRect;
    }
    
    /**
     * Generates directional collision rectangles for edge pieces.
     * Edge pieces only block movement in certain directions.
     * 
     * @param worldPos World position of the fence piece
     * @param pieceType Type of the fence piece
     * @return Rectangle representing the directional collision boundary
     */
    private Rectangle generateDirectionalCollisionRect(com.badlogic.gdx.math.Vector2 worldPos, 
                                                      FencePieceType pieceType) {
        float x = worldPos.x;
        float y = worldPos.y;
        float gridSize = FenceGrid.GRID_SIZE;
        
        switch (pieceType) {
            case FENCE_BACK:
                // Top edge - blocks vertical movement
                return new Rectangle(x, y + gridSize * 0.8f, gridSize, gridSize * 0.2f);
                
            case FENCE_FRONT:
                // Bottom edge - blocks vertical movement
                return new Rectangle(x, y, gridSize, gridSize * 0.2f);
                
            case FENCE_MIDDLE_LEFT:
                // Left edge - blocks horizontal movement
                return new Rectangle(x, y, gridSize * 0.2f, gridSize);
                
            case FENCE_MIDDLE_RIGHT:
                // Right edge - blocks horizontal movement
                return new Rectangle(x + gridSize * 0.8f, y, gridSize * 0.2f, gridSize);
                
            default:
                // Default to full collision for unknown types
                return new Rectangle(x, y, gridSize, gridSize);
        }
    }
    
    /**
     * Updates collision boundaries for complete enclosures.
     * Creates continuous collision boundaries around enclosure perimeters.
     */
    private void updateEnclosureCollisions() {
        // Clear existing enclosure collision cache
        enclosureCollisionCache.clear();
        
        // Process each complete enclosure
        List<FenceEnclosure> enclosures = structureManager.getEnclosures();
        for (FenceEnclosure enclosure : enclosures) {
            List<Rectangle> enclosureCollisions = generateEnclosureCollisionBoundaries(enclosure);
            enclosureCollisionCache.put(enclosure, enclosureCollisions);
        }
    }
    
    /**
     * Generates continuous collision boundaries for a complete enclosure.
     * Creates a continuous barrier around the enclosure perimeter.
     * 
     * @param enclosure The fence enclosure
     * @return List of collision rectangles forming the enclosure boundary
     */
    private List<Rectangle> generateEnclosureCollisionBoundaries(FenceEnclosure enclosure) {
        List<Rectangle> boundaries = new ArrayList<>();
        
        if (!enclosure.isComplete()) {
            return boundaries; // Only process complete enclosures
        }
        
        Rectangle bounds = enclosure.getBounds();
        float gridSize = FenceGrid.GRID_SIZE;
        
        // Create continuous collision boundaries around the perimeter
        
        // Top boundary
        boundaries.add(new Rectangle(
            bounds.x * gridSize, 
            (bounds.y + bounds.height - 1) * gridSize + gridSize * 0.8f,
            bounds.width * gridSize, 
            gridSize * 0.2f
        ));
        
        // Bottom boundary
        boundaries.add(new Rectangle(
            bounds.x * gridSize, 
            bounds.y * gridSize,
            bounds.width * gridSize, 
            gridSize * 0.2f
        ));
        
        // Left boundary
        boundaries.add(new Rectangle(
            bounds.x * gridSize, 
            bounds.y * gridSize,
            gridSize * 0.2f, 
            bounds.height * gridSize
        ));
        
        // Right boundary
        boundaries.add(new Rectangle(
            (bounds.x + bounds.width - 1) * gridSize + gridSize * 0.8f, 
            bounds.y * gridSize,
            gridSize * 0.2f, 
            bounds.height * gridSize
        ));
        
        return boundaries;
    }
    
    /**
     * Checks if a point collides with any fence collision boundaries.
     * 
     * @param x World X coordinate
     * @param y World Y coordinate
     * @return true if the point collides with a fence boundary
     */
    public boolean checkCollision(float x, float y) {
        // Check individual fence piece collisions
        for (Rectangle rect : allCollisionRects) {
            if (rect.contains(x, y)) {
                return true;
            }
        }
        
        // Check enclosure collision boundaries
        for (List<Rectangle> enclosureRects : enclosureCollisionCache.values()) {
            for (Rectangle rect : enclosureRects) {
                if (rect.contains(x, y)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Checks if a rectangle collides with any fence collision boundaries.
     * 
     * @param testRect Rectangle to test for collision
     * @return true if the rectangle collides with a fence boundary
     */
    public boolean checkCollision(Rectangle testRect) {
        // Check individual fence piece collisions
        for (Rectangle rect : allCollisionRects) {
            if (rect.overlaps(testRect)) {
                return true;
            }
        }
        
        // Check enclosure collision boundaries
        for (List<Rectangle> enclosureRects : enclosureCollisionCache.values()) {
            for (Rectangle rect : enclosureRects) {
                if (rect.overlaps(testRect)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Gets all collision rectangles for a specific grid position.
     * 
     * @param gridPos Grid position to check
     * @return Rectangle representing the collision boundary, or null if none exists
     */
    public Rectangle getCollisionBoundary(Point gridPos) {
        return collisionBoundaries.get(gridPos);
    }
    
    /**
     * Gets all collision rectangles currently active.
     * 
     * @return Set of all collision rectangles (defensive copy)
     */
    public Set<Rectangle> getAllCollisionBoundaries() {
        Set<Rectangle> allBoundaries = new HashSet<>(allCollisionRects);
        
        // Add enclosure collision boundaries
        for (List<Rectangle> enclosureRects : enclosureCollisionCache.values()) {
            allBoundaries.addAll(enclosureRects);
        }
        
        return allBoundaries;
    }
    
    /**
     * Gets collision boundaries for a specific enclosure.
     * 
     * @param enclosure The fence enclosure
     * @return List of collision rectangles for the enclosure
     */
    public List<Rectangle> getEnclosureCollisionBoundaries(FenceEnclosure enclosure) {
        List<Rectangle> boundaries = enclosureCollisionCache.get(enclosure);
        return boundaries != null ? new ArrayList<>(boundaries) : new ArrayList<>();
    }
    
    /**
     * Rebuilds all collision boundaries from the current fence structure.
     * Should be called after loading a saved world or major structure changes.
     */
    public void rebuildAllCollisionBoundaries() {
        // Clear existing boundaries
        collisionBoundaries.clear();
        allCollisionRects.clear();
        enclosureCollisionCache.clear();
        
        // Rebuild from current fence pieces
        Map<Point, FencePiece> allPieces = structureManager.getAllFencePieces();
        for (Map.Entry<Point, FencePiece> entry : allPieces.entrySet()) {
            addCollisionBoundary(entry.getKey(), entry.getValue());
        }
        
        System.out.println("Rebuilt " + collisionBoundaries.size() + " collision boundaries");
    }
    
    /**
     * Gets the total number of collision boundaries.
     * 
     * @return Number of collision boundaries
     */
    public int getCollisionBoundaryCount() {
        return collisionBoundaries.size();
    }
    
    /**
     * Checks if there are any collision boundaries.
     * 
     * @return true if no collision boundaries exist
     */
    public boolean isEmpty() {
        return collisionBoundaries.isEmpty();
    }
    
    /**
     * Clears all collision boundaries.
     */
    public void clear() {
        collisionBoundaries.clear();
        allCollisionRects.clear();
        enclosureCollisionCache.clear();
    }
    
    @Override
    public String toString() {
        return String.format("FenceCollisionManager[boundaries=%d, enclosures=%d]",
                           collisionBoundaries.size(), enclosureCollisionCache.size());
    }
}