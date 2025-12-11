package wagemaker.uk.fence;

import com.badlogic.gdx.math.Vector2;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Manages the grid system for fence placement in the custom fence building system.
 * Provides coordinate conversion between world coordinates and grid coordinates,
 * position validation, and adjacent position calculations.
 */
public class FenceGrid {
    
    /** Size of each grid cell in world units (pixels) */
    public static final int GRID_SIZE = 64;
    
    /** Set of grid positions that are currently occupied by fence pieces */
    private final Set<Point> occupiedPositions;
    
    /** Set of grid positions that are invalid for placement (e.g., blocked by other objects) */
    private final Set<Point> blockedPositions;
    
    /**
     * Creates a new FenceGrid with no occupied positions.
     */
    public FenceGrid() {
        this.occupiedPositions = new HashSet<>();
        this.blockedPositions = new HashSet<>();
    }
    
    /**
     * Converts world coordinates to grid coordinates.
     * Grid coordinates represent the grid cell that contains the world position.
     * 
     * @param worldX World X coordinate
     * @param worldY World Y coordinate
     * @return Point representing the grid coordinates
     */
    public Point worldToGrid(float worldX, float worldY) {
        // Use floor division to handle negative coordinates correctly
        int gridX = (int) Math.floor(worldX / GRID_SIZE);
        int gridY = (int) Math.floor(worldY / GRID_SIZE);
        return new Point(gridX, gridY);
    }
    
    /**
     * Converts grid coordinates to world coordinates.
     * Returns the world position of the top-left corner of the grid cell.
     * 
     * @param gridPos Grid coordinates as a Point
     * @return Vector2 representing the world coordinates
     */
    public Vector2 gridToWorld(Point gridPos) {
        float worldX = gridPos.x * GRID_SIZE;
        float worldY = gridPos.y * GRID_SIZE;
        return new Vector2(worldX, worldY);
    }
    
    /**
     * Converts grid coordinates to world coordinates.
     * Returns the world position of the top-left corner of the grid cell.
     * 
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     * @return Vector2 representing the world coordinates
     */
    public Vector2 gridToWorld(int gridX, int gridY) {
        return gridToWorld(new Point(gridX, gridY));
    }
    
    /**
     * Gets the center world coordinates of a grid cell.
     * 
     * @param gridPos Grid coordinates as a Point
     * @return Vector2 representing the center world coordinates
     */
    public Vector2 gridToWorldCenter(Point gridPos) {
        Vector2 topLeft = gridToWorld(gridPos);
        return new Vector2(topLeft.x + GRID_SIZE / 2f, topLeft.y + GRID_SIZE / 2f);
    }
    
    /**
     * Checks if a grid position is valid for fence placement.
     * A position is valid if it's not occupied and not blocked.
     * 
     * @param gridPos Grid coordinates to check
     * @return true if the position is valid for placement
     */
    public boolean isValidPlacement(Point gridPos) {
        return !occupiedPositions.contains(gridPos) && !blockedPositions.contains(gridPos);
    }
    
    /**
     * Checks if a grid position is valid for fence placement.
     * A position is valid if it's not occupied and not blocked.
     * 
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     * @return true if the position is valid for placement
     */
    public boolean isValidPlacement(int gridX, int gridY) {
        return isValidPlacement(new Point(gridX, gridY));
    }
    
    /**
     * Checks if a grid position is currently occupied by a fence piece.
     * 
     * @param gridPos Grid coordinates to check
     * @return true if the position is occupied
     */
    public boolean isOccupied(Point gridPos) {
        return occupiedPositions.contains(gridPos);
    }
    
    /**
     * Checks if a grid position is currently occupied by a fence piece.
     * 
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     * @return true if the position is occupied
     */
    public boolean isOccupied(int gridX, int gridY) {
        return isOccupied(new Point(gridX, gridY));
    }
    
    /**
     * Marks a grid position as occupied by a fence piece.
     * 
     * @param gridPos Grid coordinates to mark as occupied
     */
    public void setOccupied(Point gridPos) {
        occupiedPositions.add(new Point(gridPos.x, gridPos.y));
    }
    
    /**
     * Marks a grid position as occupied by a fence piece.
     * 
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     */
    public void setOccupied(int gridX, int gridY) {
        setOccupied(new Point(gridX, gridY));
    }
    
    /**
     * Removes the occupied status from a grid position.
     * 
     * @param gridPos Grid coordinates to mark as unoccupied
     */
    public void setUnoccupied(Point gridPos) {
        occupiedPositions.remove(gridPos);
    }
    
    /**
     * Removes the occupied status from a grid position.
     * 
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     */
    public void setUnoccupied(int gridX, int gridY) {
        setUnoccupied(new Point(gridX, gridY));
    }
    
    /**
     * Marks a grid position as blocked (invalid for placement).
     * Blocked positions might be occupied by other game objects.
     * 
     * @param gridPos Grid coordinates to mark as blocked
     */
    public void setBlocked(Point gridPos) {
        blockedPositions.add(new Point(gridPos.x, gridPos.y));
    }
    
    /**
     * Marks a grid position as blocked (invalid for placement).
     * 
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     */
    public void setBlocked(int gridX, int gridY) {
        setBlocked(new Point(gridX, gridY));
    }
    
    /**
     * Removes the blocked status from a grid position.
     * 
     * @param gridPos Grid coordinates to unblock
     */
    public void setUnblocked(Point gridPos) {
        blockedPositions.remove(gridPos);
    }
    
    /**
     * Removes the blocked status from a grid position.
     * 
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     */
    public void setUnblocked(int gridX, int gridY) {
        setUnblocked(new Point(gridX, gridY));
    }
    
    /**
     * Gets all adjacent grid positions (4-directional: north, south, east, west).
     * 
     * @param gridPos Center grid position
     * @return List of adjacent grid positions
     */
    public List<Point> getAdjacentPositions(Point gridPos) {
        List<Point> adjacent = new ArrayList<>();
        
        // North (up)
        adjacent.add(new Point(gridPos.x, gridPos.y - 1));
        // South (down)
        adjacent.add(new Point(gridPos.x, gridPos.y + 1));
        // East (right)
        adjacent.add(new Point(gridPos.x + 1, gridPos.y));
        // West (left)
        adjacent.add(new Point(gridPos.x - 1, gridPos.y));
        
        return adjacent;
    }
    
    /**
     * Gets all adjacent grid positions (4-directional: north, south, east, west).
     * 
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     * @return List of adjacent grid positions
     */
    public List<Point> getAdjacentPositions(int gridX, int gridY) {
        return getAdjacentPositions(new Point(gridX, gridY));
    }
    
    /**
     * Gets all adjacent grid positions that are currently occupied.
     * 
     * @param gridPos Center grid position
     * @return List of adjacent occupied positions
     */
    public List<Point> getAdjacentOccupiedPositions(Point gridPos) {
        List<Point> adjacent = getAdjacentPositions(gridPos);
        List<Point> occupiedAdjacent = new ArrayList<>();
        
        for (Point pos : adjacent) {
            if (isOccupied(pos)) {
                occupiedAdjacent.add(pos);
            }
        }
        
        return occupiedAdjacent;
    }
    
    /**
     * Gets all diagonal grid positions (4-directional: northeast, northwest, southeast, southwest).
     * 
     * @param gridPos Center grid position
     * @return List of diagonal grid positions
     */
    public List<Point> getDiagonalPositions(Point gridPos) {
        List<Point> diagonal = new ArrayList<>();
        
        // Northeast
        diagonal.add(new Point(gridPos.x + 1, gridPos.y - 1));
        // Northwest
        diagonal.add(new Point(gridPos.x - 1, gridPos.y - 1));
        // Southeast
        diagonal.add(new Point(gridPos.x + 1, gridPos.y + 1));
        // Southwest
        diagonal.add(new Point(gridPos.x - 1, gridPos.y + 1));
        
        return diagonal;
    }
    
    /**
     * Gets all 8 surrounding grid positions (adjacent + diagonal).
     * 
     * @param gridPos Center grid position
     * @return List of all surrounding grid positions
     */
    public List<Point> getSurroundingPositions(Point gridPos) {
        List<Point> surrounding = new ArrayList<>();
        surrounding.addAll(getAdjacentPositions(gridPos));
        surrounding.addAll(getDiagonalPositions(gridPos));
        return surrounding;
    }
    
    /**
     * Calculates the Manhattan distance between two grid positions.
     * 
     * @param pos1 First grid position
     * @param pos2 Second grid position
     * @return Manhattan distance (sum of absolute differences in x and y)
     */
    public int getManhattanDistance(Point pos1, Point pos2) {
        return Math.abs(pos1.x - pos2.x) + Math.abs(pos1.y - pos2.y);
    }
    
    /**
     * Calculates the Euclidean distance between two grid positions.
     * 
     * @param pos1 First grid position
     * @param pos2 Second grid position
     * @return Euclidean distance
     */
    public double getEuclideanDistance(Point pos1, Point pos2) {
        int dx = pos1.x - pos2.x;
        int dy = pos1.y - pos2.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    /**
     * Checks if two grid positions are adjacent (4-directional).
     * 
     * @param pos1 First grid position
     * @param pos2 Second grid position
     * @return true if the positions are adjacent
     */
    public boolean areAdjacent(Point pos1, Point pos2) {
        return getManhattanDistance(pos1, pos2) == 1;
    }
    
    /**
     * Gets all currently occupied grid positions.
     * 
     * @return Set of occupied grid positions (defensive copy)
     */
    public Set<Point> getOccupiedPositions() {
        return new HashSet<>(occupiedPositions);
    }
    
    /**
     * Gets all currently blocked grid positions.
     * 
     * @return Set of blocked grid positions (defensive copy)
     */
    public Set<Point> getBlockedPositions() {
        return new HashSet<>(blockedPositions);
    }
    
    /**
     * Clears all occupied positions from the grid.
     */
    public void clearOccupiedPositions() {
        occupiedPositions.clear();
    }
    
    /**
     * Clears all blocked positions from the grid.
     */
    public void clearBlockedPositions() {
        blockedPositions.clear();
    }
    
    /**
     * Clears all positions (both occupied and blocked) from the grid.
     */
    public void clearAllPositions() {
        clearOccupiedPositions();
        clearBlockedPositions();
    }
    
    /**
     * Gets the number of currently occupied positions.
     * 
     * @return Number of occupied positions
     */
    public int getOccupiedCount() {
        return occupiedPositions.size();
    }
    
    /**
     * Gets the number of currently blocked positions.
     * 
     * @return Number of blocked positions
     */
    public int getBlockedCount() {
        return blockedPositions.size();
    }
    
    /**
     * Checks if the grid is empty (no occupied positions).
     * 
     * @return true if no positions are occupied
     */
    public boolean isEmpty() {
        return occupiedPositions.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("FenceGrid[occupied=%d, blocked=%d]", 
                           occupiedPositions.size(), blockedPositions.size());
    }
}