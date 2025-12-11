package wagemaker.uk.fence;

import org.junit.Before;
import org.junit.Test;
import org.junit.After;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Unit tests for FenceWorldIntegration.
 * Tests world lifecycle integration, resource cleanup,
 * and building manager integration.
 */
public class FenceWorldIntegrationTest {
    
    private FenceBuildingManager buildingManager;
    private FenceWorldIntegration integration;
    private FenceGrid grid;
    
    @Before
    public void setUp() {
        grid = new FenceGrid();
        buildingManager = mock(FenceBuildingManager.class);
        integration = new FenceWorldIntegration();
        integration.setBuildingManager(buildingManager);
    }
    
    @After
    public void tearDown() {
        if (integration != null) {
            integration.dispose();
        }
    }
    
    @Test
    public void testInitialState() {
        // Assert
        assertTrue("Should be active initially", integration.isActive());
        assertSame("Should return the same building manager", buildingManager, integration.getBuildingManager());
        assertNotNull("Memory stats should not be null", integration.getMemoryStats());
    }
    
    @Test
    public void testWorldLoadHandling() {
        // Arrange
        when(buildingManager.isBuildingModeActive()).thenReturn(true);
        
        // Act
        integration.onWorldLoad("test-world");
        
        // Assert
        verify(buildingManager).isBuildingModeActive();
        verify(buildingManager).toggleBuildingMode(); // Should disable building mode during load
    }
    
    @Test
    public void testWorldUnloadHandling() {
        // Arrange
        when(buildingManager.isBuildingModeActive()).thenReturn(true);
        
        // Act
        integration.onWorldUnload("test-world");
        
        // Assert
        verify(buildingManager).isBuildingModeActive();
        verify(buildingManager).toggleBuildingMode(); // Should disable building mode during unload
    }
    
    @Test
    public void testGameShutdownHandling() {
        // Arrange
        when(buildingManager.isBuildingModeActive()).thenReturn(true);
        
        // Act
        integration.onGameShutdown();
        
        // Assert
        verify(buildingManager).isBuildingModeActive();
        verify(buildingManager).toggleBuildingMode(); // Should disable building mode during shutdown
        verify(buildingManager).dispose(); // Should dispose building manager
        assertFalse("Should be inactive after shutdown", integration.isActive());
        assertNull("Building manager should be null after shutdown", integration.getBuildingManager());
    }
    
    @Test
    public void testForceCleanup() {
        // Act
        integration.forceCleanup();
        
        // Assert - should not throw any exceptions
        assertTrue("Should remain active after force cleanup", integration.isActive());
    }
    
    @Test
    public void testMemoryStats() {
        // Act
        FenceResourceManager.MemoryStats stats = integration.getMemoryStats();
        
        // Assert
        assertNotNull("Memory stats should not be null", stats);
    }
    
    @Test
    public void testToString() {
        // Act
        String toString = integration.toString();
        
        // Assert
        assertNotNull("toString should not be null", toString);
        assertTrue("toString should contain active status", toString.contains("active=true"));
        assertTrue("toString should contain memory stats", toString.contains("MemoryStats"));
    }
    
    @Test
    public void testDisposeCallsShutdown() {
        // Arrange
        when(buildingManager.isBuildingModeActive()).thenReturn(false);
        
        // Act
        integration.dispose();
        
        // Assert
        assertFalse("Should be inactive after dispose", integration.isActive());
        verify(buildingManager).dispose();
    }
    
    @Test
    public void testWorldLoadWithInactiveBuildingMode() {
        // Arrange
        when(buildingManager.isBuildingModeActive()).thenReturn(false);
        
        // Act
        integration.onWorldLoad("test-world");
        
        // Assert
        verify(buildingManager).isBuildingModeActive();
        verify(buildingManager, never()).toggleBuildingMode(); // Should not toggle if already inactive
    }
    
    @Test
    public void testWorldUnloadWithInactiveBuildingMode() {
        // Arrange
        when(buildingManager.isBuildingModeActive()).thenReturn(false);
        
        // Act
        integration.onWorldUnload("test-world");
        
        // Assert
        verify(buildingManager).isBuildingModeActive();
        verify(buildingManager, never()).toggleBuildingMode(); // Should not toggle if already inactive
    }
    
    @Test
    public void testInactiveIntegrationIgnoresOperations() {
        // Arrange - shutdown the integration first
        integration.onGameShutdown();
        assertFalse("Should be inactive after shutdown", integration.isActive());
        
        // Reset the mock to clear previous interactions
        reset(buildingManager);
        
        // Act - try operations on inactive integration
        integration.onWorldLoad("test-world");
        integration.onWorldUnload("test-world");
        integration.forceCleanup();
        
        // Assert - should not interact with building manager when inactive
        verifyNoInteractions(buildingManager);
    }
}