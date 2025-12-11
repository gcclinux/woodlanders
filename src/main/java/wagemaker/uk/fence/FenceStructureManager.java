package wagemaker.uk.fence;

import com.badlogic.gdx.math.Rectangle;
import java.awt.Point;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages fence structure data and operations for the custom fence building system.
 * Handles fence piece storage, retrieval, structure modification, and connection updates.
 * Thread-safe for multiplayer environments.
 */
public class FenceStructureManager {
    
    /** Map of grid positions to fence pieces */
    private final Map<Point, FencePiece> placedFences;
    
    /** List of complete fence enclosures */
    private final List<FenceEnclosure> enclosures;
    
    /** Grid system for coordinate management */
    private final FenceGrid grid;
    
    /** Set of positions that are part of incomplete structures */
    private final Set<Point> incompleteStructurePositions;
    
    /**
     * Creates a new FenceStructureManager with empty structures.
     */
    public FenceStructureManager() {
        this.placedFences = new ConcurrentHashMap<>();
        this.enclosures = new ArrayList<>();
        this.grid = new FenceGrid();
        this.incompleteStructurePositions = new HashSet<>();
        
        // Register with resource manager for cleanup tracking
        FenceResourceManager.getInstance().registerStructureManager(
            "manager_" + System.identityHashCode(this), this);
    }
    
    /**
     * Creates a new FenceStructureManager with the specified grid.
     * 
     * @param grid The FenceGrid to use for coordinate management
     */
    public FenceStructureManager(FenceGrid grid) {
        this.placedFences = new ConcurrentHashMap<>();
        this.enclosures = new ArrayList<>();
        this.grid = grid;
        this.incompleteStructurePositions = new HashSet<>();
        
        // Register with resource manager for cleanup tracking
        FenceResourceManager.getInstance().registerStructureManager(
            "manager_" + System.identityHashCode(this), this);
    }
    
    /**
     * Adds a fence piece at the specified grid position.
     * Automatically determines the correct piece type based on adjacent pieces.
     * Updates connections with adjacent pieces.
     * 
     * @param gridPos Grid position where to place the fence piece
     * @param materialType Type of material to use for the fence piece
     * @return The created FencePiece, or null if placement failed
     * @throws IllegalArgumentException if the position is already occupied
     */
    public FencePiece addFencePiece(Point gridPos, FenceMaterialType materialType) {
        if (placedFences.containsKey(gridPos)) {
            throw new IllegalArgumentException("Position " + gridPos + " is already occupied");
        }
        
        if (!grid.isValidPlacement(gridPos)) {
            return null; // Position is blocked or invalid
        }
        
        // Determine the appropriate piece type based on adjacent pieces
        FencePieceType pieceType = determinePieceType(gridPos);
        
        // Convert grid coordinates to world coordinates
        com.badlogic.gdx.math.Vector2 worldPos = grid.gridToWorld(gridPos);
        
        // Create the fence piece
        FencePiece piece = FencePieceFactory.createPiece(pieceType, worldPos.x, worldPos.y);
        
        // Add to our data structures
        placedFences.put(new Point(gridPos.x, gridPos.y), piece);
        grid.setOccupied(gridPos);
        incompleteStructurePositions.add(new Point(gridPos.x, gridPos.y));
        
        // Update connections with adjacent pieces
        updateConnections(gridPos);
        
        // Check if this placement completes any enclosures
        checkForCompletedEnclosures(gridPos);
        
        return piece;
    }
    
    /**
     * Removes a fence piece from the specified grid position.
     * Updates connections with adjacent pieces.
     * 
     * @param gridPos Grid position of the fence piece to remove
     * @return The removed FencePiece, or null if no piece was at that position
     */
    public FencePiece removeFencePiece(Point gridPos) {
        FencePiece removedPiece = placedFences.remove(gridPos);
        
        if (removedPiece != null) {
            grid.setUnoccupied(gridPos);
            incompleteStructurePositions.remove(gridPos);
            
            // Remove from any enclosures
            removeFromEnclosures(gridPos);
            
            // Update connections with adjacent pieces
            updateConnections(gridPos);
            
            // Dispose of the piece's resources
            removedPiece.dispose();
        }
        
        return removedPiece;
    }
    
    /**
     * Gets the fence piece at the specified grid position.
     * 
     * @param gridPos Grid position to check
     * @return The FencePiece at that position, or null if none exists
     */
    public FencePiece getFencePiece(Point gridPos) {
        return placedFences.get(gridPos);
    }
    
    /**
     * Gets the fence piece at the specified grid coordinates.
     * 
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     * @return The FencePiece at that position, or null if none exists
     */
    public FencePiece getFencePiece(int gridX, int gridY) {
        return getFencePiece(new Point(gridX, gridY));
    }
    
    /**
     * Determines the appropriate fence piece type for a position based on adjacent pieces.
     * Uses pattern recognition to identify if the position is part of a rectangular structure.
     * 
     * @param gridPos Grid position to analyze
     * @return The appropriate FencePieceType for this position
     */
    public FencePieceType determinePieceType(Point gridPos) {
        List<Point> adjacentOccupied = grid.getAdjacentOccupiedPositions(gridPos);
        
        // If no adjacent pieces, default to a corner piece (can be refined later)
        if (adjacentOccupied.isEmpty()) {
            return FencePieceType.FENCE_BACK_LEFT;
        }
        
        // Analyze the pattern of adjacent pieces to determine type
        boolean hasNorth = adjacentOccupied.contains(new Point(gridPos.x, gridPos.y - 1));
        boolean hasSouth = adjacentOccupied.contains(new Point(gridPos.x, gridPos.y + 1));
        boolean hasEast = adjacentOccupied.contains(new Point(gridPos.x + 1, gridPos.y));
        boolean hasWest = adjacentOccupied.contains(new Point(gridPos.x - 1, gridPos.y));
        
        // Determine piece type based on adjacent connections
        if (hasNorth && hasEast && !hasSouth && !hasWest) {
            return FencePieceType.FENCE_BACK_LEFT; // Top-left corner
        } else if (hasNorth && hasWest && !hasSouth && !hasEast) {
            return FencePieceType.FENCE_BACK_RIGHT; // Top-right corner
        } else if (hasSouth && hasWest && !hasNorth && !hasEast) {
            return FencePieceType.FENCE_FRONT_RIGHT; // Bottom-right corner
        } else if (hasSouth && hasEast && !hasNorth && !hasWest) {
            return FencePieceType.FENCE_FRONT_LEFT; // Bottom-left corner
        } else if (hasEast && hasWest && !hasNorth && !hasSouth) {
            return FencePieceType.FENCE_BACK; // Horizontal edge (top or bottom)
        } else if (hasNorth && hasSouth && !hasEast && !hasWest) {
            return FencePieceType.FENCE_MIDDLE_RIGHT; // Vertical edge (left or right)
        } else if (hasEast && !hasWest && !hasNorth && !hasSouth) {
            return FencePieceType.FENCE_MIDDLE_LEFT; // Left edge piece
        } else if (hasWest && !hasEast && !hasNorth && !hasSouth) {
            return FencePieceType.FENCE_MIDDLE_RIGHT; // Right edge piece
        } else if (hasNorth && !hasSouth && !hasEast && !hasWest) {
            return FencePieceType.FENCE_FRONT; // Bottom edge piece
        } else if (hasSouth && !hasNorth && !hasEast && !hasWest) {
            return FencePieceType.FENCE_BACK; // Top edge piece
        }
        
        // Default to corner piece if pattern is unclear
        return FencePieceType.FENCE_BACK_LEFT;
    }
    
    /**
     * Updates connections with adjacent fence pieces after a placement or removal.
     * Recalculates piece types for adjacent pieces to maintain proper connections.
     * 
     * @param gridPos Grid position that was modified
     */
    public void updateConnections(Point gridPos) {
        List<Point> adjacentPositions = grid.getAdjacentPositions(gridPos);
        
        for (Point adjacentPos : adjacentPositions) {
            FencePiece adjacentPiece = placedFences.get(adjacentPos);
            if (adjacentPiece != null) {
                // Recalculate the piece type for the adjacent piece
                FencePieceType newType = determinePieceType(adjacentPos);
                
                // If the type should change, replace the piece
                if (adjacentPiece.getType() != newType) {
                    replacePieceType(adjacentPos, newType);
                }
            }
        }
    }
    
    /**
     * Replaces a fence piece with a different type at the same position.
     * Used when connections change and a different piece type is needed.
     * 
     * @param gridPos Grid position of the piece to replace
     * @param newType New fence piece type
     */
    private void replacePieceType(Point gridPos, FencePieceType newType) {
        FencePiece oldPiece = placedFences.get(gridPos);
        if (oldPiece == null || oldPiece.getType() == newType) {
            return; // Nothing to replace
        }
        
        // Create new piece with the same position
        com.badlogic.gdx.math.Vector2 worldPos = grid.gridToWorld(gridPos);
        FencePiece newPiece = FencePieceFactory.createPiece(newType, worldPos.x, worldPos.y);
        
        // Replace in the map
        placedFences.put(gridPos, newPiece);
        
        // Dispose of the old piece
        oldPiece.dispose();
    }
    
    /**
     * Checks if placing a fence piece at the given position would complete any enclosures.
     * Creates FenceEnclosure objects for completed rectangular structures.
     * 
     * @param gridPos Grid position that was just placed
     */
    private void checkForCompletedEnclosures(Point gridPos) {
        // This is a simplified implementation - a full implementation would
        // use flood-fill or similar algorithms to detect completed rectangles
        
        // For now, we'll check if this position is part of a simple rectangular pattern
        Set<Point> connectedPieces = findConnectedPieces(gridPos);
        
        if (connectedPieces.size() >= 8) { // Minimum for a 2x2 enclosure
            Rectangle bounds = calculateBounds(connectedPieces);
            if (isCompleteRectangle(connectedPieces, bounds)) {
                createEnclosure(connectedPieces, bounds);
            }
        }
    }
    
    /**
     * Finds all fence pieces connected to the given position.
     * Uses breadth-first search to find connected components.
     * 
     * @param startPos Starting grid position
     * @return Set of all connected grid positions
     */
    private Set<Point> findConnectedPieces(Point startPos) {
        Set<Point> visited = new HashSet<>();
        Queue<Point> queue = new LinkedList<>();
        
        queue.offer(startPos);
        visited.add(startPos);
        
        while (!queue.isEmpty()) {
            Point current = queue.poll();
            List<Point> adjacent = grid.getAdjacentOccupiedPositions(current);
            
            for (Point adj : adjacent) {
                if (!visited.contains(adj)) {
                    visited.add(adj);
                    queue.offer(adj);
                }
            }
        }
        
        return visited;
    }
    
    /**
     * Calculates the bounding rectangle for a set of grid positions.
     * 
     * @param positions Set of grid positions
     * @return Rectangle representing the bounds
     */
    private Rectangle calculateBounds(Set<Point> positions) {
        if (positions.isEmpty()) {
            return new Rectangle(0, 0, 0, 0);
        }
        
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        
        for (Point pos : positions) {
            minX = Math.min(minX, pos.x);
            maxX = Math.max(maxX, pos.x);
            minY = Math.min(minY, pos.y);
            maxY = Math.max(maxY, pos.y);
        }
        
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
    
    /**
     * Checks if a set of positions forms a complete rectangle.
     * 
     * @param positions Set of grid positions
     * @param bounds Bounding rectangle
     * @return true if the positions form a complete rectangle perimeter
     */
    private boolean isCompleteRectangle(Set<Point> positions, Rectangle bounds) {
        int expectedPieces = 2 * ((int)bounds.width + (int)bounds.height - 2);
        if (positions.size() != expectedPieces) {
            return false;
        }
        
        // Check that all perimeter positions are present
        for (int x = (int)bounds.x; x < bounds.x + bounds.width; x++) {
            for (int y = (int)bounds.y; y < bounds.y + bounds.height; y++) {
                Point pos = new Point(x, y);
                boolean isPerimeter = (x == bounds.x || x == bounds.x + bounds.width - 1 ||
                                     y == bounds.y || y == bounds.y + bounds.height - 1);
                
                if (isPerimeter && !positions.contains(pos)) {
                    return false; // Missing perimeter piece
                }
                if (!isPerimeter && positions.contains(pos)) {
                    return false; // Interior piece present
                }
            }
        }
        
        return true;
    }
    
    /**
     * Creates a FenceEnclosure for a completed rectangular structure.
     * 
     * @param positions Set of grid positions forming the enclosure
     * @param bounds Bounding rectangle
     */
    private void createEnclosure(Set<Point> positions, Rectangle bounds) {
        List<FencePiece> pieces = new ArrayList<>();
        
        for (Point pos : positions) {
            FencePiece piece = placedFences.get(pos);
            if (piece != null) {
                pieces.add(piece);
                incompleteStructurePositions.remove(pos);
            }
        }
        
        FenceEnclosure enclosure = new FenceEnclosure(bounds, pieces, FenceMaterialType.WOOD);
        enclosures.add(enclosure);
    }
    
    /**
     * Removes a position from any enclosures it belongs to.
     * 
     * @param gridPos Grid position to remove
     */
    private void removeFromEnclosures(Point gridPos) {
        Iterator<FenceEnclosure> iterator = enclosures.iterator();
        while (iterator.hasNext()) {
            FenceEnclosure enclosure = iterator.next();
            if (enclosure.containsPosition(gridPos)) {
                iterator.remove();
                // Add all positions back to incomplete structures
                for (FencePiece piece : enclosure.getPieces()) {
                    Point pos = grid.worldToGrid(piece.getX(), piece.getY());
                    if (!pos.equals(gridPos)) { // Don't add the removed position
                        incompleteStructurePositions.add(pos);
                    }
                }
                break;
            }
        }
    }
    
    /**
     * Gets all fence pieces currently placed.
     * 
     * @return Map of grid positions to fence pieces (defensive copy)
     */
    public Map<Point, FencePiece> getAllFencePieces() {
        return new HashMap<>(placedFences);
    }
    
    /**
     * Gets all complete fence enclosures.
     * 
     * @return List of fence enclosures (defensive copy)
     */
    public List<FenceEnclosure> getEnclosures() {
        return new ArrayList<>(enclosures);
    }
    
    /**
     * Gets the fence grid used by this manager.
     * 
     * @return The FenceGrid instance
     */
    public FenceGrid getGrid() {
        return grid;
    }
    
    /**
     * Gets all positions that are part of incomplete structures.
     * 
     * @return Set of grid positions (defensive copy)
     */
    public Set<Point> getIncompleteStructurePositions() {
        return new HashSet<>(incompleteStructurePositions);
    }
    
    /**
     * Checks if there are any fence pieces placed.
     * 
     * @return true if no fence pieces are placed
     */
    public boolean isEmpty() {
        return placedFences.isEmpty();
    }
    
    /**
     * Gets the total number of fence pieces placed.
     * 
     * @return Number of fence pieces
     */
    public int getFencePieceCount() {
        return placedFences.size();
    }
    
    /**
     * Gets the number of complete enclosures.
     * 
     * @return Number of enclosures
     */
    public int getEnclosureCount() {
        return enclosures.size();
    }
    
    /**
     * Clears all fence pieces and enclosures.
     * Disposes of all fence piece resources.
     */
    public void clear() {
        // Dispose of all fence pieces
        for (FencePiece piece : placedFences.values()) {
            piece.dispose();
        }
        
        placedFences.clear();
        enclosures.clear();
        incompleteStructurePositions.clear();
        grid.clearAllPositions();
    }
    
    /**
     * Adds a fence piece for restoration purposes, bypassing normal placement validation.
     * Used when loading fence structures from save data.
     * 
     * @param gridPos Grid position where to place the fence piece
     * @param piece The fence piece to add
     * @return true if the piece was added successfully, false otherwise
     */
    public boolean addFencePieceForRestore(Point gridPos, FencePiece piece) {
        if (piece == null) {
            return false;
        }
        
        // Force placement even if position validation would normally fail
        placedFences.put(new Point(gridPos.x, gridPos.y), piece);
        grid.setOccupied(gridPos);
        incompleteStructurePositions.add(new Point(gridPos.x, gridPos.y));
        
        return true;
    }
    
    /**
     * Disposes of all resources used by this manager.
     * Should be called when the manager is no longer needed.
     */
    public void dispose() {
        // Unregister from resource manager
        FenceResourceManager.getInstance().unregisterStructureManager(
            "manager_" + System.identityHashCode(this));
        
        clear();
    }
    
    @Override
    public String toString() {
        return String.format("FenceStructureManager[pieces=%d, enclosures=%d, incomplete=%d]",
                           placedFences.size(), enclosures.size(), incompleteStructurePositions.size());
    }
}