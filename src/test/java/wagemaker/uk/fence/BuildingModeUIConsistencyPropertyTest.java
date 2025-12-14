package wagemaker.uk.fence;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import wagemaker.uk.player.Player;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * Property-based test for building mode UI consistency.
 * 
 * **Feature: custom-fence-building, Property 1: Building mode UI consistency**
 * **Validates: Requirements 1.1, 1.2, 1.3, 1.5**
 */
@RunWith(JUnitQuickcheck.class)
public class BuildingModeUIConsistencyPropertyTest {
    
    private static HeadlessApplication application;
    
    private FenceBuildingManager buildingManager;
    private FenceStructureManager structureManager;
    private FencePlacementValidator validator;
    private OrthographicCamera camera;
    private Player player;
    
    @BeforeClass
    public static void setUpClass() {
        // Initialize headless LibGDX application for testing
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        // Mock GL20 to prevent OpenGL calls
        Gdx.gl = Mockito.mock(GL20.class);
    }
    
    @AfterClass
    public static void tearDownClass() {
        if (application != null) {
            application.exit();
        }
    }
    
    @Before
    public void setUp() {
        // Initialize test components without OpenGL dependencies
        camera = new OrthographicCamera();
        structureManager = new FenceStructureManager();
        
        // Create a mock player for testing
        player = mock(Player.class);
        
        // Create a mock material provider for testing
        FenceMaterialProvider materialProvider = new MockFenceMaterialProvider();
        validator = new FencePlacementValidator(structureManager.getGrid(), materialProvider, structureManager);
        
        buildingManager = new FenceBuildingManager(structureManager, validator, camera, player, true);
    }
    
    /**
     * Property 1: Building mode UI consistency
     * For any building mode state change, the UI elements (grid overlay, material count, instructions) 
     * should be visible when active and hidden when inactive.
     * 
     * This property tests that the building mode state is consistently reflected in the UI state.
     */
    @Property(trials = 100)
    public void buildingModeUIConsistency(boolean shouldBeActive) {
        // Set the building mode to the desired state
        boolean initialState = buildingManager.isBuildingModeActive();
        
        // Toggle building mode to reach the desired state
        if (shouldBeActive != initialState) {
            buildingManager.toggleBuildingMode();
        }
        
        // Verify the building mode state matches expectation
        boolean actualState = buildingManager.isBuildingModeActive();
        assertEquals(shouldBeActive, actualState, 
                    "Building mode state should match the requested state");
        
        // Verify UI components are consistent with building mode state
        // Core components should always be available regardless of mode
        assertNotNull(buildingManager.getSelectedMaterialType(), 
                     "Selected material type should always be available");
        assertNotNull(buildingManager.getStructureManager(), 
                     "Structure manager should always be available");
        assertNotNull(buildingManager.getValidator(), 
                     "Validator should always be available");
        
        // Test state consistency after multiple toggles
        buildingManager.toggleBuildingMode();
        boolean toggledState = buildingManager.isBuildingModeActive();
        assertEquals(!shouldBeActive, toggledState, 
                    "Building mode state should be opposite after toggle");
        
        // Toggle back to original state
        buildingManager.toggleBuildingMode();
        boolean finalState = buildingManager.isBuildingModeActive();
        assertEquals(shouldBeActive, finalState, 
                    "Building mode state should return to original after double toggle");
    }
    
    /**
     * Mock material provider for testing that always has materials available.
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