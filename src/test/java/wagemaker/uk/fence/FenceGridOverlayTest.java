package wagemaker.uk.fence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.Point;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for fence grid functionality.
 * Tests grid positioning and state management without OpenGL dependencies.
 */
public class FenceGridOverlayTest {
    
    private FenceStructureManager structureManager;
    private FenceGrid grid;
    
    @BeforeEach
    public void setUp() {
        // Initialize test components without OpenGL dependencies
        structureManager = new FenceStructureManager();
        grid = structureManager.getGrid();
    }
    
    /**
     * Test grid size constant.
     * Verifies that the grid size is correctly defined.
     */
    @Test
    public void testGridSize() {
        int gridSize = FenceGridOverlay.getGridSize();
        assertEquals(64, gridSize, 
                    "Grid size should be 64 pixels");
    }
    
    /**
     * Test grid occupied positions.
     * Verifies that the grid correctly handles occupied positions.
     */
    @Test
    public void testGridOccupiedPositions() {
        Point pos1 = new Point(0, 0);
        Point pos2 = new Point(1, 0);
        Point pos3 = new Point(0, 1);
        
        // Initially, positions should not be occupied
        assertFalse(grid.isOccupied(pos1), "Position (0,0) should not be occupied initially");
        assertFalse(grid.isOccupied(pos2), "Position (1,0) should not be occupied initially");
        assertFalse(grid.isOccupied(pos3), "Position (0,1) should not be occupied initially");
        
        // Set positions as occupied (simulating fence placement)
        grid.setOccupied(pos1);
        grid.setOccupied(pos2);
        
        assertTrue(grid.isOccupied(pos1), "Position (0,0) should be occupied after setting");
        assertTrue(grid.isOccupied(pos2), "Position (1,0) should be occupied after setting");
        assertFalse(grid.isOccupied(pos3), "Position (0,1) should still not be occupied");
    }
    
    /**
     * Test grid coordinate conversion.
     * Verifies that world-to-grid and grid-to-world conversions work correctly.
     */
    @Test
    public void testGridCoordinateConversion() {
        // Test world-to-grid conversion
        Point gridPos1 = grid.worldToGrid(0, 0);
        assertEquals(new Point(0, 0), gridPos1, "World (0,0) should convert to grid (0,0)");
        
        Point gridPos2 = grid.worldToGrid(64, 64);
        assertEquals(new Point(1, 1), gridPos2, "World (64,64) should convert to grid (1,1)");
        
        Point gridPos3 = grid.worldToGrid(128, 192);
        assertEquals(new Point(2, 3), gridPos3, "World (128,192) should convert to grid (2,3)");
        
        // Test grid-to-world conversion
        com.badlogic.gdx.math.Vector2 worldPos1 = grid.gridToWorld(new Point(0, 0));
        assertEquals(0, worldPos1.x, 0.001f, "Grid (0,0) should convert to world x=0");
        assertEquals(0, worldPos1.y, 0.001f, "Grid (0,0) should convert to world y=0");
        
        com.badlogic.gdx.math.Vector2 worldPos2 = grid.gridToWorld(new Point(2, 3));
        assertEquals(128, worldPos2.x, 0.001f, "Grid (2,3) should convert to world x=128");
        assertEquals(192, worldPos2.y, 0.001f, "Grid (2,3) should convert to world y=192");
    }
    
    /**
     * Test grid valid placement checking.
     * Verifies that the grid correctly identifies valid placement positions.
     */
    @Test
    public void testGridValidPlacement() {
        Point pos1 = new Point(0, 0);
        Point pos2 = new Point(1, 1);
        
        // Initially, positions should be valid for placement
        assertTrue(grid.isValidPlacement(pos1), "Position (0,0) should be valid for placement initially");
        assertTrue(grid.isValidPlacement(pos2), "Position (1,1) should be valid for placement initially");
        
        // Occupy a position
        grid.setOccupied(pos1);
        
        // Occupied position should no longer be valid for placement
        assertFalse(grid.isValidPlacement(pos1), "Occupied position (0,0) should not be valid for placement");
        assertTrue(grid.isValidPlacement(pos2), "Unoccupied position (1,1) should still be valid for placement");
    }
}