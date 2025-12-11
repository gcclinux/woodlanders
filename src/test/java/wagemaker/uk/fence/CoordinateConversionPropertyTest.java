package wagemaker.uk.fence;

import com.badlogic.gdx.math.Vector2;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;

import java.awt.Point;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for coordinate conversion consistency in the fence grid system.
 * 
 * Tests round-trip coordinate conversion consistency and grid boundary validation.
 */
@RunWith(JUnitQuickcheck.class)
public class CoordinateConversionPropertyTest {
    
    /**
     * Property test for round-trip coordinate conversion consistency.
     * For any world coordinates, converting to grid and back to world should
     * place the result within the same grid cell.
     */
    @Property(trials = 100)
    public void roundTripCoordinateConversionProperty(
            @InRange(minFloat = -10000.0f, maxFloat = 10000.0f) float worldX,
            @InRange(minFloat = -10000.0f, maxFloat = 10000.0f) float worldY) {
        
        FenceGrid grid = new FenceGrid();
        
        // Convert world to grid
        Point gridPos = grid.worldToGrid(worldX, worldY);
        
        // Convert grid back to world
        Vector2 convertedWorld = grid.gridToWorld(gridPos);
        
        // The converted world coordinates should be the top-left corner of the grid cell
        // that contains the original world coordinates
        assertTrue(convertedWorld.x <= worldX, 
            "Converted world X should be <= original world X");
        assertTrue(convertedWorld.y <= worldY, 
            "Converted world Y should be <= original world Y");
        assertTrue(convertedWorld.x + FenceGrid.GRID_SIZE > worldX,
            "Converted world X + grid size should be > original world X");
        assertTrue(convertedWorld.y + FenceGrid.GRID_SIZE > worldY,
            "Converted world Y + grid size should be > original world Y");
        
        // Test that the original coordinates fall within the grid cell
        float cellLeft = convertedWorld.x;
        float cellRight = convertedWorld.x + FenceGrid.GRID_SIZE;
        float cellTop = convertedWorld.y;
        float cellBottom = convertedWorld.y + FenceGrid.GRID_SIZE;
        
        assertTrue(worldX >= cellLeft && worldX < cellRight,
            "Original world X should be within the grid cell bounds");
        assertTrue(worldY >= cellTop && worldY < cellBottom,
            "Original world Y should be within the grid cell bounds");
        
        // Test center coordinate calculation
        Vector2 centerWorld = grid.gridToWorldCenter(gridPos);
        assertEquals(convertedWorld.x + FenceGrid.GRID_SIZE / 2f, centerWorld.x, 0.001f,
            "Center X should be at the middle of the grid cell");
        assertEquals(convertedWorld.y + FenceGrid.GRID_SIZE / 2f, centerWorld.y, 0.001f,
            "Center Y should be at the middle of the grid cell");
    }
    
    /**
     * Property test for grid coordinate consistency.
     * Tests that grid coordinates behave correctly for different world positions.
     */
    @Property(trials = 100)
    public void gridCoordinateConsistencyProperty(
            @InRange(minInt = -100, maxInt = 100) int gridX,
            @InRange(minInt = -100, maxInt = 100) int gridY) {
        
        FenceGrid grid = new FenceGrid();
        Point gridPos = new Point(gridX, gridY);
        
        // Convert grid to world and back
        Vector2 worldPos = grid.gridToWorld(gridPos);
        Point convertedGrid = grid.worldToGrid(worldPos.x, worldPos.y);
        
        // Should get back the same grid coordinates
        assertEquals(gridPos, convertedGrid,
            "Grid to world to grid conversion should be consistent");
        
        // Test alternative method signatures
        Vector2 worldPos2 = grid.gridToWorld(gridX, gridY);
        assertEquals(worldPos, worldPos2,
            "Both gridToWorld methods should return the same result");
        
        // Test that world coordinates are multiples of grid size
        assertEquals(0, worldPos.x % FenceGrid.GRID_SIZE, 0.001f,
            "World X coordinate should be a multiple of grid size");
        assertEquals(0, worldPos.y % FenceGrid.GRID_SIZE, 0.001f,
            "World Y coordinate should be a multiple of grid size");
        
        // Test that grid coordinates are calculated correctly
        assertEquals(gridX, (int) (worldPos.x / FenceGrid.GRID_SIZE),
            "Grid X should match calculated value from world X");
        assertEquals(gridY, (int) (worldPos.y / FenceGrid.GRID_SIZE),
            "Grid Y should match calculated value from world Y");
    }
    
    /**
     * Property test for grid position validation and state management.
     */
    @Property(trials = 100)
    public void gridPositionValidationProperty(
            @InRange(minInt = -50, maxInt = 50) int gridX,
            @InRange(minInt = -50, maxInt = 50) int gridY) {
        
        FenceGrid grid = new FenceGrid();
        Point gridPos = new Point(gridX, gridY);
        
        // Initially, position should be valid and unoccupied
        assertTrue(grid.isValidPlacement(gridPos),
            "Position should initially be valid for placement");
        assertTrue(grid.isValidPlacement(gridX, gridY),
            "Position should be valid using coordinate parameters");
        assertFalse(grid.isOccupied(gridPos),
            "Position should initially be unoccupied");
        assertFalse(grid.isOccupied(gridX, gridY),
            "Position should be unoccupied using coordinate parameters");
        
        // Mark as occupied
        grid.setOccupied(gridPos);
        assertTrue(grid.isOccupied(gridPos),
            "Position should be occupied after setting");
        assertFalse(grid.isValidPlacement(gridPos),
            "Occupied position should not be valid for placement");
        
        // Mark as unoccupied
        grid.setUnoccupied(gridPos);
        assertFalse(grid.isOccupied(gridPos),
            "Position should be unoccupied after clearing");
        assertTrue(grid.isValidPlacement(gridPos),
            "Unoccupied position should be valid for placement");
        
        // Mark as blocked
        grid.setBlocked(gridX, gridY);
        assertFalse(grid.isValidPlacement(gridPos),
            "Blocked position should not be valid for placement");
        
        // Unblock
        grid.setUnblocked(gridX, gridY);
        assertTrue(grid.isValidPlacement(gridPos),
            "Unblocked position should be valid for placement");
        
        // Test that occupied and blocked are independent
        grid.setOccupied(gridPos);
        grid.setBlocked(gridPos);
        assertTrue(grid.isOccupied(gridPos),
            "Position should remain occupied when blocked");
        assertFalse(grid.isValidPlacement(gridPos),
            "Position should not be valid when both occupied and blocked");
        
        grid.setUnoccupied(gridPos);
        assertFalse(grid.isValidPlacement(gridPos),
            "Position should still be invalid when blocked but not occupied");
        
        grid.setUnblocked(gridPos);
        assertTrue(grid.isValidPlacement(gridPos),
            "Position should be valid when neither occupied nor blocked");
    }
    
    /**
     * Property test for adjacent position calculations.
     */
    @Property(trials = 100)
    public void adjacentPositionCalculationProperty(
            @InRange(minInt = -20, maxInt = 20) int centerX,
            @InRange(minInt = -20, maxInt = 20) int centerY) {
        
        FenceGrid grid = new FenceGrid();
        Point center = new Point(centerX, centerY);
        
        // Get adjacent positions
        List<Point> adjacent = grid.getAdjacentPositions(center);
        
        // Should have exactly 4 adjacent positions
        assertEquals(4, adjacent.size(),
            "Should have exactly 4 adjacent positions");
        
        // Verify each adjacent position is exactly 1 Manhattan distance away
        for (Point adj : adjacent) {
            assertEquals(1, grid.getManhattanDistance(center, adj),
                "Adjacent position should be exactly 1 Manhattan distance away");
            assertTrue(grid.areAdjacent(center, adj),
                "Position should be identified as adjacent");
        }
        
        // Verify the specific adjacent positions
        assertTrue(adjacent.contains(new Point(centerX, centerY - 1)),
            "Should include north position");
        assertTrue(adjacent.contains(new Point(centerX, centerY + 1)),
            "Should include south position");
        assertTrue(adjacent.contains(new Point(centerX + 1, centerY)),
            "Should include east position");
        assertTrue(adjacent.contains(new Point(centerX - 1, centerY)),
            "Should include west position");
        
        // Test diagonal positions
        List<Point> diagonal = grid.getDiagonalPositions(center);
        assertEquals(4, diagonal.size(),
            "Should have exactly 4 diagonal positions");
        
        for (Point diag : diagonal) {
            assertEquals(2, grid.getManhattanDistance(center, diag),
                "Diagonal position should be exactly 2 Manhattan distance away");
            assertFalse(grid.areAdjacent(center, diag),
                "Diagonal position should not be identified as adjacent");
        }
        
        // Test surrounding positions (adjacent + diagonal)
        List<Point> surrounding = grid.getSurroundingPositions(center);
        assertEquals(8, surrounding.size(),
            "Should have exactly 8 surrounding positions");
        
        // All adjacent positions should be in surrounding
        for (Point adj : adjacent) {
            assertTrue(surrounding.contains(adj),
                "Surrounding positions should include all adjacent positions");
        }
        
        // All diagonal positions should be in surrounding
        for (Point diag : diagonal) {
            assertTrue(surrounding.contains(diag),
                "Surrounding positions should include all diagonal positions");
        }
    }
    
    /**
     * Property test for distance calculations.
     */
    @Property(trials = 100)
    public void distanceCalculationProperty(
            @InRange(minInt = -10, maxInt = 10) int x1,
            @InRange(minInt = -10, maxInt = 10) int y1,
            @InRange(minInt = -10, maxInt = 10) int x2,
            @InRange(minInt = -10, maxInt = 10) int y2) {
        
        FenceGrid grid = new FenceGrid();
        Point pos1 = new Point(x1, y1);
        Point pos2 = new Point(x2, y2);
        
        // Test Manhattan distance
        int manhattanDistance = grid.getManhattanDistance(pos1, pos2);
        int expectedManhattan = Math.abs(x1 - x2) + Math.abs(y1 - y2);
        assertEquals(expectedManhattan, manhattanDistance,
            "Manhattan distance should match expected calculation");
        
        // Test Euclidean distance
        double euclideanDistance = grid.getEuclideanDistance(pos1, pos2);
        double expectedEuclidean = Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
        assertEquals(expectedEuclidean, euclideanDistance, 0.001,
            "Euclidean distance should match expected calculation");
        
        // Test distance properties
        assertTrue(manhattanDistance >= 0,
            "Manhattan distance should be non-negative");
        assertTrue(euclideanDistance >= 0,
            "Euclidean distance should be non-negative");
        
        // Distance to self should be 0
        assertEquals(0, grid.getManhattanDistance(pos1, pos1),
            "Manhattan distance to self should be 0");
        assertEquals(0.0, grid.getEuclideanDistance(pos1, pos1), 0.001,
            "Euclidean distance to self should be 0");
        
        // Distance should be symmetric
        assertEquals(manhattanDistance, grid.getManhattanDistance(pos2, pos1),
            "Manhattan distance should be symmetric");
        assertEquals(euclideanDistance, grid.getEuclideanDistance(pos2, pos1), 0.001,
            "Euclidean distance should be symmetric");
        
        // Manhattan distance should be >= Euclidean distance
        assertTrue(manhattanDistance >= euclideanDistance - 0.001,
            "Manhattan distance should be >= Euclidean distance");
    }
    
    /**
     * Property test for grid state management.
     */
    @Property(trials = 50)
    public void gridStateManagementProperty(
            @InRange(minInt = 1, maxInt = 10) int numPositions) {
        
        FenceGrid grid = new FenceGrid();
        
        // Initially empty
        assertTrue(grid.isEmpty(),
            "Grid should initially be empty");
        assertEquals(0, grid.getOccupiedCount(),
            "Grid should initially have 0 occupied positions");
        assertEquals(0, grid.getBlockedCount(),
            "Grid should initially have 0 blocked positions");
        
        // Add some occupied positions
        for (int i = 0; i < numPositions; i++) {
            grid.setOccupied(i, i);
        }
        
        assertFalse(grid.isEmpty(),
            "Grid should not be empty after adding positions");
        assertEquals(numPositions, grid.getOccupiedCount(),
            "Grid should have correct number of occupied positions");
        
        // Add some blocked positions
        for (int i = 0; i < numPositions; i++) {
            grid.setBlocked(i + 100, i + 100);
        }
        
        assertEquals(numPositions, grid.getBlockedCount(),
            "Grid should have correct number of blocked positions");
        
        // Test defensive copying of position sets
        var occupiedCopy1 = grid.getOccupiedPositions();
        var occupiedCopy2 = grid.getOccupiedPositions();
        
        assertNotSame(occupiedCopy1, occupiedCopy2,
            "Should return different instances for defensive copying");
        assertEquals(occupiedCopy1, occupiedCopy2,
            "Defensive copies should have same content");
        
        // Modifying the copy should not affect the grid
        occupiedCopy1.clear();
        assertEquals(numPositions, grid.getOccupiedCount(),
            "Modifying defensive copy should not affect grid");
        
        // Clear positions
        grid.clearOccupiedPositions();
        assertTrue(grid.isEmpty(),
            "Grid should be empty after clearing occupied positions");
        assertEquals(numPositions, grid.getBlockedCount(),
            "Blocked positions should remain after clearing occupied");
        
        grid.clearBlockedPositions();
        assertEquals(0, grid.getBlockedCount(),
            "Grid should have 0 blocked positions after clearing");
        
        // Test clearAllPositions
        grid.setOccupied(0, 0);
        grid.setBlocked(1, 1);
        grid.clearAllPositions();
        assertTrue(grid.isEmpty(),
            "Grid should be empty after clearing all positions");
        assertEquals(0, grid.getBlockedCount(),
            "Grid should have 0 blocked positions after clearing all");
    }
}