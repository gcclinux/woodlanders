package wagemaker.uk.fence;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.generator.InRange;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import java.awt.Point;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Property-based tests for connection consistency in fence structures.
 * **Feature: custom-fence-building, Property 6: Connection consistency**
 * **Validates: Requirements 2.5, 3.2, 4.2, 4.3**
 */
@RunWith(JUnitQuickcheck.class)
public class ConnectionConsistencyPropertyTest {

    /**
     * Property: For any rectangular fence pattern, piece types should be consistent 
     * with their position in the structure.
     */
    @Property(trials = 100)
    public void pieceTypesConsistentWithPosition(
            @InRange(min = "2", max = "8") int width,
            @InRange(min = "2", max = "8") int height) {
        
        // Test all perimeter positions
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // Skip interior positions
                if (x != 0 && x != width - 1 && y != 0 && y != height - 1) continue;
                
                FencePieceType pieceType = FencePieceFactory.determinePieceTypeForPosition(x, y, width, height);
                
                // Verify corner positions have corner pieces
                if ((x == 0 && y == 0) || (x == width - 1 && y == 0) || 
                    (x == width - 1 && y == height - 1) || (x == 0 && y == height - 1)) {
                    assertTrue("Corner position (" + x + ", " + y + ") should have corner piece", 
                              pieceType.isCornerPiece());
                } else {
                    // Non-corner perimeter positions should have edge pieces
                    assertTrue("Edge position (" + x + ", " + y + ") should have edge piece", 
                              pieceType.isEdgePiece());
                }
            }
        }
    }
    
    /**
     * Property: Corner pieces should be placed at exactly the four corners of any rectangle.
     */
    @Property(trials = 100)
    public void cornerPiecesAtCorrectPositions(
            @InRange(min = "2", max = "10") int width,
            @InRange(min = "2", max = "10") int height) {
        
        // Check the four corner positions
        FencePieceType topLeft = FencePieceFactory.determinePieceTypeForPosition(0, 0, width, height);
        FencePieceType topRight = FencePieceFactory.determinePieceTypeForPosition(width - 1, 0, width, height);
        FencePieceType bottomRight = FencePieceFactory.determinePieceTypeForPosition(width - 1, height - 1, width, height);
        FencePieceType bottomLeft = FencePieceFactory.determinePieceTypeForPosition(0, height - 1, width, height);
        
        // Verify specific corner types
        assertEquals("Top-left should be FENCE_BACK_LEFT", FencePieceType.FENCE_BACK_LEFT, topLeft);
        assertEquals("Top-right should be FENCE_BACK_RIGHT", FencePieceType.FENCE_BACK_RIGHT, topRight);
        assertEquals("Bottom-right should be FENCE_FRONT_RIGHT", FencePieceType.FENCE_FRONT_RIGHT, bottomRight);
        assertEquals("Bottom-left should be FENCE_FRONT_LEFT", FencePieceType.FENCE_FRONT_LEFT, bottomLeft);
    }
    
    /**
     * Property: Edge pieces should be placed correctly on rectangle edges.
     */
    @Property(trials = 100)
    public void edgePiecesAtCorrectPositions(
            @InRange(min = "3", max = "10") int width,
            @InRange(min = "3", max = "10") int height) {
        
        // Test edge pieces (only for rectangles larger than 2x2)
        if (width > 2) {
            // Top edge (middle pieces)
            for (int x = 1; x < width - 1; x++) {
                FencePieceType topEdge = FencePieceFactory.determinePieceTypeForPosition(x, 0, width, height);
                assertEquals("Top edge at (" + x + ", 0) should be FENCE_BACK", 
                           FencePieceType.FENCE_BACK, topEdge);
            }
            
            // Bottom edge (middle pieces)
            for (int x = 1; x < width - 1; x++) {
                FencePieceType bottomEdge = FencePieceFactory.determinePieceTypeForPosition(x, height - 1, width, height);
                assertEquals("Bottom edge at (" + x + ", " + (height - 1) + ") should be FENCE_FRONT", 
                           FencePieceType.FENCE_FRONT, bottomEdge);
            }
        }
        
        if (height > 2) {
            // Right edge (middle pieces)
            for (int y = 1; y < height - 1; y++) {
                FencePieceType rightEdge = FencePieceFactory.determinePieceTypeForPosition(width - 1, y, width, height);
                assertEquals("Right edge at (" + (width - 1) + ", " + y + ") should be FENCE_MIDDLE_RIGHT", 
                           FencePieceType.FENCE_MIDDLE_RIGHT, rightEdge);
            }
            
            // Left edge (middle pieces)
            for (int y = 1; y < height - 1; y++) {
                FencePieceType leftEdge = FencePieceFactory.determinePieceTypeForPosition(0, y, width, height);
                assertEquals("Left edge at (0, " + y + ") should be FENCE_MIDDLE_LEFT", 
                           FencePieceType.FENCE_MIDDLE_LEFT, leftEdge);
            }
        }
    }
    
    /**
     * Property: Grid position validation should be consistent.
     */
    @Property(trials = 100)
    public void gridPositionValidationConsistent(
            @InRange(min = "0", max = "15") int gridX,
            @InRange(min = "0", max = "15") int gridY) {
        
        FenceGrid grid = new FenceGrid();
        Point pos = new Point(gridX, gridY);
        
        // Initially, all positions should be valid for placement
        assertTrue("Fresh grid position should be valid", grid.isValidPlacement(pos));
        assertFalse("Fresh grid position should not be occupied", grid.isOccupied(pos));
        
        // After marking as occupied, should not be valid for placement
        grid.setOccupied(pos);
        assertFalse("Occupied position should not be valid for placement", grid.isValidPlacement(pos));
        assertTrue("Position should be marked as occupied", grid.isOccupied(pos));
        
        // After marking as unoccupied, should be valid again
        grid.setUnoccupied(pos);
        assertTrue("Unoccupied position should be valid for placement", grid.isValidPlacement(pos));
        assertFalse("Position should not be marked as occupied", grid.isOccupied(pos));
    }
    
    /**
     * Property: Adjacent position calculation should be symmetric.
     */
    @Property(trials = 100)
    public void adjacentPositionCalculationSymmetric(
            @InRange(min = "1", max = "10") int centerX,
            @InRange(min = "1", max = "10") int centerY) {
        
        FenceGrid grid = new FenceGrid();
        Point center = new Point(centerX, centerY);
        
        List<Point> adjacentPositions = grid.getAdjacentPositions(center);
        
        // Should have exactly 4 adjacent positions (north, south, east, west)
        assertEquals("Should have exactly 4 adjacent positions", 4, adjacentPositions.size());
        
        // Check that each adjacent position considers the center as adjacent
        for (Point adjacent : adjacentPositions) {
            List<Point> adjacentToAdjacent = grid.getAdjacentPositions(adjacent);
            assertTrue("Adjacent position should consider center as adjacent", 
                      adjacentToAdjacent.contains(center));
        }
        
        // Verify the specific adjacent positions
        assertTrue("Should include north position", 
                  adjacentPositions.contains(new Point(centerX, centerY - 1)));
        assertTrue("Should include south position", 
                  adjacentPositions.contains(new Point(centerX, centerY + 1)));
        assertTrue("Should include east position", 
                  adjacentPositions.contains(new Point(centerX + 1, centerY)));
        assertTrue("Should include west position", 
                  adjacentPositions.contains(new Point(centerX - 1, centerY)));
    }
    
    /**
     * Property: Coordinate conversion should be consistent (round-trip).
     */
    @Property(trials = 100)
    public void coordinateConversionRoundTrip(
            @InRange(min = "0", max = "1000") int worldX,
            @InRange(min = "0", max = "1000") int worldY) {
        
        FenceGrid grid = new FenceGrid();
        
        // Convert world to grid and back
        Point gridPos = grid.worldToGrid(worldX, worldY);
        com.badlogic.gdx.math.Vector2 backToWorld = grid.gridToWorld(gridPos);
        
        // The world coordinates should be within the same grid cell
        int expectedGridX = worldX / FenceGrid.GRID_SIZE;
        int expectedGridY = worldY / FenceGrid.GRID_SIZE;
        
        assertEquals("Grid X should match expected", expectedGridX, gridPos.x);
        assertEquals("Grid Y should match expected", expectedGridY, gridPos.y);
        
        // Back to world should give the top-left corner of the grid cell
        float expectedWorldX = expectedGridX * FenceGrid.GRID_SIZE;
        float expectedWorldY = expectedGridY * FenceGrid.GRID_SIZE;
        
        assertEquals("World X should match expected", expectedWorldX, backToWorld.x, 0.001f);
        assertEquals("World Y should match expected", expectedWorldY, backToWorld.y, 0.001f);
    }
    
    /**
     * Property: Fence structure manager should maintain consistent state.
     */
    @Property(trials = 100)
    public void structureManagerMaintainsConsistentState(
            @InRange(min = "0", max = "8") int gridX,
            @InRange(min = "0", max = "8") int gridY) {
        
        FenceGrid grid = new FenceGrid();
        FenceStructureManager manager = new FenceStructureManager(grid);
        Point pos = new Point(gridX, gridY);
        
        // Initially should be empty
        assertTrue("Manager should start empty", manager.isEmpty());
        assertEquals("Should have 0 pieces initially", 0, manager.getFencePieceCount());
        assertNull("Should not have piece at position", manager.getFencePiece(pos));
        
        // Grid should be consistent with manager state
        assertFalse("Grid should not show position as occupied", grid.isOccupied(pos));
        assertTrue("Grid should show position as valid for placement", grid.isValidPlacement(pos));
        
        // Clean up
        manager.dispose();
    }
}