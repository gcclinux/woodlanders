package wagemaker.uk.fence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for the fence system to ensure efficient memory usage
 * with large fence structures and frequent updates.
 * Note: Rendering tests are excluded due to LibGDX native library requirements in headless environment.
 */
public class FencePerformanceTest {
    
    private FenceStructureManager structureManager;
    private FenceResourceManager resourceManager;
    
    @BeforeEach
    void setUp() {
        // Initialize test environment
        structureManager = new FenceStructureManager();
        resourceManager = FenceResourceManager.getInstance();
    }
    
    @AfterEach
    void tearDown() {
        if (structureManager != null) {
            structureManager.dispose();
        }
        
        // Clean up resource manager
        resourceManager.cleanupOnWorldUnload();
    }
    
    /**
     * Tests grid coordinate conversion performance.
     * Tests the performance of world-to-grid and grid-to-world conversions.
     */
    @Test
    void testGridCoordinateConversionPerformance(TestInfo testInfo) {
        System.out.println("Running: " + testInfo.getDisplayName());
        
        FenceGrid grid = structureManager.getGrid();
        int conversionCount = 100000;
        
        // Test world-to-grid conversion performance
        long startTime = System.nanoTime();
        
        for (int i = 0; i < conversionCount; i++) {
            float worldX = i * 64.0f;
            float worldY = i * 64.0f;
            Point gridPos = grid.worldToGrid(worldX, worldY);
            assertNotNull(gridPos, "Grid position should not be null");
        }
        
        long worldToGridTime = System.nanoTime() - startTime;
        double avgWorldToGridTime = (worldToGridTime / 1_000_000.0) / conversionCount;
        
        System.out.println("World-to-grid conversions: " + conversionCount + " in " + 
                          (worldToGridTime / 1_000_000) + "ms");
        System.out.println("Average world-to-grid time: " + 
                          String.format("%.6f", avgWorldToGridTime) + "ms");
        
        // Test grid-to-world conversion performance
        startTime = System.nanoTime();
        
        for (int i = 0; i < conversionCount; i++) {
            Point gridPos = new Point(i % 1000, i / 1000);
            com.badlogic.gdx.math.Vector2 worldPos = grid.gridToWorld(gridPos);
            assertNotNull(worldPos, "World position should not be null");
        }
        
        long gridToWorldTime = System.nanoTime() - startTime;
        double avgGridToWorldTime = (gridToWorldTime / 1_000_000.0) / conversionCount;
        
        System.out.println("Grid-to-world conversions: " + conversionCount + " in " + 
                          (gridToWorldTime / 1_000_000) + "ms");
        System.out.println("Average grid-to-world time: " + 
                          String.format("%.6f", avgGridToWorldTime) + "ms");
        
        // Performance assertions
        assertTrue(avgWorldToGridTime < 0.01, 
                  "Average world-to-grid conversion should be less than 0.01ms");
        assertTrue(avgGridToWorldTime < 0.01, 
                  "Average grid-to-world conversion should be less than 0.01ms");
    }
    
    /**
     * Tests structure manager data operations performance.
     * Tests adding, retrieving, and removing positions without creating actual fence pieces.
     */
    @Test
    void testStructureManagerDataOperationsPerformance(TestInfo testInfo) {
        System.out.println("Running: " + testInfo.getDisplayName());
        
        // Get initial memory stats
        FenceResourceManager.MemoryStats initialStats = resourceManager.getMemoryStats();
        System.out.println("Initial memory stats: " + initialStats);
        
        // Test grid position operations (without creating fence pieces)
        FenceGrid grid = structureManager.getGrid();
        int operationCount = 10000;
        List<Point> positions = new ArrayList<>();
        
        // Generate test positions
        for (int i = 0; i < operationCount; i++) {
            positions.add(new Point(i % 100, i / 100));
        }
        
        // Test position validation performance
        long startTime = System.nanoTime();
        
        for (Point pos : positions) {
            boolean valid = grid.isValidPlacement(pos);
            assertTrue(valid, "Position should be valid for placement");
        }
        
        long validationTime = System.nanoTime() - startTime;
        double avgValidationTime = (validationTime / 1_000_000.0) / operationCount;
        
        System.out.println("Position validations: " + operationCount + " in " + 
                          (validationTime / 1_000_000) + "ms");
        System.out.println("Average validation time: " + 
                          String.format("%.6f", avgValidationTime) + "ms");
        
        // Test position occupation tracking
        startTime = System.nanoTime();
        
        for (Point pos : positions) {
            grid.setOccupied(pos);
        }
        
        long occupationTime = System.nanoTime() - startTime;
        double avgOccupationTime = (occupationTime / 1_000_000.0) / operationCount;
        
        System.out.println("Position occupations: " + operationCount + " in " + 
                          (occupationTime / 1_000_000) + "ms");
        System.out.println("Average occupation time: " + 
                          String.format("%.6f", avgOccupationTime) + "ms");
        
        // Test position lookup performance
        startTime = System.nanoTime();
        
        for (Point pos : positions) {
            boolean occupied = !grid.isValidPlacement(pos); // Occupied positions are not valid for placement
            assertTrue(occupied, "Position should be marked as occupied");
        }
        
        long lookupTime = System.nanoTime() - startTime;
        double avgLookupTime = (lookupTime / 1_000_000.0) / operationCount;
        
        System.out.println("Position lookups: " + operationCount + " in " + 
                          (lookupTime / 1_000_000) + "ms");
        System.out.println("Average lookup time: " + 
                          String.format("%.6f", avgLookupTime) + "ms");
        
        // Test cleanup performance
        startTime = System.nanoTime();
        grid.clearAllPositions();
        long cleanupTime = System.nanoTime() - startTime;
        
        System.out.println("Grid cleanup completed in " + (cleanupTime / 1_000_000) + "ms");
        
        // Performance assertions
        assertTrue(avgValidationTime < 0.01, 
                  "Average validation time should be less than 0.01ms");
        assertTrue(avgOccupationTime < 0.01, 
                  "Average occupation time should be less than 0.01ms");
        assertTrue(avgLookupTime < 0.01, 
                  "Average lookup time should be less than 0.01ms");
        assertTrue(cleanupTime < 50_000_000, // 50ms in nanoseconds
                  "Cleanup should complete in less than 50ms");
    }
    
    /**
     * Tests object pooling performance with frequent allocations.
     * Simulates rapid allocation and deallocation of pooled objects.
     */
    @Test
    void testObjectPoolingPerformance(TestInfo testInfo) {
        System.out.println("Running: " + testInfo.getDisplayName());
        
        int allocationCount = 10000;
        
        // Test Point object pooling
        long startTime = System.nanoTime();
        List<Point> points = new ArrayList<>();
        
        for (int i = 0; i < allocationCount; i++) {
            Point point = resourceManager.obtain(Point.class);
            point.setLocation(i % 100, i / 100);
            points.add(point);
        }
        
        long allocationTime = System.nanoTime() - startTime;
        double avgAllocationTime = (allocationTime / 1_000_000.0) / allocationCount;
        
        System.out.println("Point allocations: " + allocationCount + " in " + 
                          (allocationTime / 1_000_000) + "ms");
        System.out.println("Average allocation time: " + 
                          String.format("%.6f", avgAllocationTime) + "ms");
        
        // Test deallocation performance
        startTime = System.nanoTime();
        
        for (Point point : points) {
            resourceManager.free(point);
        }
        
        long deallocationTime = System.nanoTime() - startTime;
        double avgDeallocationTime = (deallocationTime / 1_000_000.0) / allocationCount;
        
        System.out.println("Point deallocations: " + allocationCount + " in " + 
                          (deallocationTime / 1_000_000) + "ms");
        System.out.println("Average deallocation time: " + 
                          String.format("%.6f", avgDeallocationTime) + "ms");
        
        // Test Rectangle object pooling
        startTime = System.nanoTime();
        List<com.badlogic.gdx.math.Rectangle> rectangles = new ArrayList<>();
        
        for (int i = 0; i < allocationCount; i++) {
            com.badlogic.gdx.math.Rectangle rect = resourceManager.obtain(com.badlogic.gdx.math.Rectangle.class);
            rect.set(i % 100, i / 100, 64, 64);
            rectangles.add(rect);
        }
        
        long rectAllocationTime = System.nanoTime() - startTime;
        double avgRectAllocationTime = (rectAllocationTime / 1_000_000.0) / allocationCount;
        
        System.out.println("Rectangle allocations: " + allocationCount + " in " + 
                          (rectAllocationTime / 1_000_000) + "ms");
        System.out.println("Average rectangle allocation time: " + 
                          String.format("%.6f", avgRectAllocationTime) + "ms");
        
        // Free rectangles
        for (com.badlogic.gdx.math.Rectangle rect : rectangles) {
            resourceManager.free(rect);
        }
        
        // Performance assertions
        assertTrue(avgAllocationTime < 0.01, 
                  "Average Point allocation time should be less than 0.01ms");
        assertTrue(avgDeallocationTime < 0.01, 
                  "Average Point deallocation time should be less than 0.01ms");
        assertTrue(avgRectAllocationTime < 0.01, 
                  "Average Rectangle allocation time should be less than 0.01ms");
    }
    
    /**
     * Tests resource manager memory tracking efficiency.
     */
    @Test
    void testResourceManagerMemoryTracking(TestInfo testInfo) {
        System.out.println("Running: " + testInfo.getDisplayName());
        
        // Get initial memory stats
        FenceResourceManager.MemoryStats initialStats = resourceManager.getMemoryStats();
        System.out.println("Initial memory stats: " + initialStats);
        
        // Create multiple structure managers
        List<FenceStructureManager> managers = new ArrayList<>();
        int managerCount = 5;
        
        for (int i = 0; i < managerCount; i++) {
            FenceStructureManager manager = new FenceStructureManager();
            managers.add(manager);
        }
        
        FenceResourceManager.MemoryStats afterCreationStats = resourceManager.getMemoryStats();
        System.out.println("After creating " + managerCount + " managers: " + afterCreationStats);
        
        // Should have increased manager count
        assertTrue(afterCreationStats.getActiveStructureManagers() > initialStats.getActiveStructureManagers(),
                  "Active structure manager count should increase");
        
        // Test memory tracking without creating fence pieces (to avoid LibGDX dependencies)
        // Just verify that the managers are being tracked correctly
        
        // Dispose half the managers
        int halfCount = managerCount / 2;
        for (int i = 0; i < halfCount; i++) {
            managers.get(i).dispose();
        }
        
        FenceResourceManager.MemoryStats afterPartialDisposeStats = resourceManager.getMemoryStats();
        System.out.println("After disposing " + halfCount + " managers: " + afterPartialDisposeStats);
        
        // Should have fewer active managers
        assertTrue(afterPartialDisposeStats.getActiveStructureManagers() < afterCreationStats.getActiveStructureManagers(),
                  "Active structure manager count should decrease when managers are disposed");
        
        // Dispose remaining managers
        for (int i = halfCount; i < managerCount; i++) {
            managers.get(i).dispose();
        }
        
        FenceResourceManager.MemoryStats finalStats = resourceManager.getMemoryStats();
        System.out.println("Final memory stats: " + finalStats);
        
        // Should be back to initial level (or close to it)
        assertTrue(finalStats.getActiveStructureManagers() <= initialStats.getActiveStructureManagers() + 1,
                  "Active structure manager count should return to initial level");
    }
}