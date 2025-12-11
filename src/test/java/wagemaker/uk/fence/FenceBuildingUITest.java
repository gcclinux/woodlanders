package wagemaker.uk.fence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import com.badlogic.gdx.graphics.OrthographicCamera;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for fence building components.
 * Tests building mode state management and core functionality.
 */
public class FenceBuildingUITest {
    
    private FenceBuildingManager buildingManager;
    private FenceStructureManager structureManager;
    private FencePlacementValidator validator;
    private OrthographicCamera camera;
    
    @BeforeEach
    public void setUp() {
        // Initialize test components without OpenGL dependencies
        camera = new OrthographicCamera();
        structureManager = new FenceStructureManager();
        
        // Create mock material provider
        FenceMaterialProvider materialProvider = new MockFenceMaterialProvider();
        validator = new FencePlacementValidator(structureManager.getGrid(), materialProvider, structureManager);
        
        buildingManager = new FenceBuildingManager(structureManager, validator, camera);
        
        // Create UI without OpenGL-dependent components for testing
        // We'll test the logic without actual rendering
    }
    
    @AfterEach
    public void tearDown() {
        // Clean up test resources
    }
    
    /**
     * Test building mode state management.
     * Verifies that building mode can be toggled correctly.
     */
    @Test
    public void testBuildingModeState() {
        // Initially, building mode should be inactive
        assertFalse(buildingManager.isBuildingModeActive(), 
                   "Building mode should be inactive initially");
        
        // Activate building mode
        buildingManager.toggleBuildingMode();
        assertTrue(buildingManager.isBuildingModeActive(), 
                  "Building mode should be active after toggle");
        
        // Deactivate building mode
        buildingManager.toggleBuildingMode();
        assertFalse(buildingManager.isBuildingModeActive(), 
                   "Building mode should be inactive after second toggle");
    }
    
    /**
     * Test material count display accuracy.
     * Verifies that the building manager correctly accesses material counts
     * from the material provider.
     */
    @Test
    public void testMaterialCountDisplay() {
        // Verify that the building manager has access to material provider
        FenceMaterialProvider materialProvider = buildingManager.getValidator().getMaterialProvider();
        assertNotNull(materialProvider, "Material provider should be available");
        
        // Test material counts
        int woodCount = materialProvider.getMaterialCount(FenceMaterialType.WOOD);
        int bambooCount = materialProvider.getMaterialCount(FenceMaterialType.BAMBOO);
        
        assertEquals(100, woodCount, "Wood count should match mock provider value");
        assertEquals(100, bambooCount, "Bamboo count should match mock provider value");
        
        // Test material availability
        assertTrue(materialProvider.hasEnoughMaterials(FenceMaterialType.WOOD, 1), 
                  "Should have enough wood materials");
        assertTrue(materialProvider.hasEnoughMaterials(FenceMaterialType.BAMBOO, 1), 
                  "Should have enough bamboo materials");
    }
    
    /**
     * Test building mode state consistency.
     * Verifies that building mode state remains consistent when toggling multiple times.
     */
    @Test
    public void testBuildingModeStateConsistency() {
        // Test multiple building mode toggles
        for (int i = 0; i < 5; i++) {
            boolean expectedActive = (i % 2 == 1); // Odd iterations should be active
            
            if (i > 0) {
                buildingManager.toggleBuildingMode();
            }
            
            assertEquals(expectedActive, buildingManager.isBuildingModeActive(), 
                        "Building mode state should be consistent at iteration " + i);
        }
    }
    
    /**
     * Test selected material type consistency.
     * Verifies that the building manager correctly manages the selected material type.
     */
    @Test
    public void testSelectedMaterialType() {
        // Test initial material type
        FenceMaterialType initialType = buildingManager.getSelectedMaterialType();
        assertNotNull(initialType, "Selected material type should not be null");
        
        // Test changing material type
        FenceMaterialType newType = (initialType == FenceMaterialType.WOOD) ? 
            FenceMaterialType.BAMBOO : FenceMaterialType.WOOD;
        
        buildingManager.setSelectedMaterialType(newType);
        assertEquals(newType, buildingManager.getSelectedMaterialType(), 
                    "Selected material type should change when set");
        
        // Verify consistency after state changes
        assertEquals(newType, buildingManager.getSelectedMaterialType(), 
                    "Selected material type should remain consistent");
    }
    
    /**
     * Mock material provider for testing.
     */
    private static class MockFenceMaterialProvider implements FenceMaterialProvider {
        
        @Override
        public boolean hasEnoughMaterials(FenceMaterialType type, int count) {
            return true; // Always have materials for testing
        }
        
        @Override
        public void consumeMaterials(FenceMaterialType type, int count) {
            // Mock implementation - do nothing
        }
        
        @Override
        public void returnMaterials(FenceMaterialType type, int count) {
            // Mock implementation - do nothing
        }
        
        @Override
        public int getMaterialCount(FenceMaterialType type) {
            return 100; // Always return a high count for testing
        }
    }
}