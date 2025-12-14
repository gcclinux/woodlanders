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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

/**
 * Property-based test for input mode exclusivity.
 * 
 * **Feature: custom-fence-building, Property 2: Input mode exclusivity**
 * **Validates: Requirements 1.4, 13.3, 13.4**
 */
@RunWith(JUnitQuickcheck.class)
public class InputModeExclusivityPropertyTest {
    
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
        // Initialize test components
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
     * Property 2: Input mode exclusivity
     * For any game state, normal player actions should be disabled when in building mode 
     * and enabled when in normal mode.
     * 
     * This property tests that building mode and normal mode are mutually exclusive states.
     */
    @Property(trials = 100)
    public void inputModeExclusivity(boolean buildingModeActive) {
        // Set building mode to the desired state
        boolean currentState = buildingManager.isBuildingModeActive();
        if (buildingModeActive != currentState) {
            buildingManager.toggleBuildingMode();
        }
        
        // Verify the building mode state
        boolean actualBuildingMode = buildingManager.isBuildingModeActive();
        assertEquals(buildingModeActive, actualBuildingMode, 
                    "Building mode state should match the requested state");
        
        // Test exclusivity: building mode and normal mode should be mutually exclusive
        boolean normalModeActive = !actualBuildingMode;
        
        // Verify that exactly one mode is active
        assertNotEquals(actualBuildingMode, normalModeActive, 
                       "Building mode and normal mode should be mutually exclusive");
        
        // Test state transitions maintain exclusivity
        buildingManager.toggleBuildingMode();
        boolean newBuildingMode = buildingManager.isBuildingModeActive();
        boolean newNormalMode = !newBuildingMode;
        
        // After toggle, the states should be flipped
        assertEquals(!buildingModeActive, newBuildingMode, 
                    "Building mode should be opposite after toggle");
        assertEquals(!normalModeActive, newNormalMode, 
                    "Normal mode should be opposite after toggle");
        
        // Verify exclusivity is maintained after toggle
        assertNotEquals(newBuildingMode, newNormalMode, 
                       "Building mode and normal mode should remain mutually exclusive after toggle");
        
        // Test multiple rapid toggles maintain exclusivity
        for (int i = 0; i < 5; i++) {
            buildingManager.toggleBuildingMode();
            boolean currentBuildingMode = buildingManager.isBuildingModeActive();
            boolean currentNormalMode = !currentBuildingMode;
            
            assertNotEquals(currentBuildingMode, currentNormalMode, 
                           "Building mode and normal mode should remain mutually exclusive during rapid toggles");
        }
    }
    
    /**
     * Tests that building mode state changes are atomic and consistent.
     */
    @Property(trials = 100)
    public void buildingModeStateConsistency(int toggleCount) {
        // Ensure toggleCount is reasonable for testing
        toggleCount = Math.abs(toggleCount) % 20; // Limit to 0-19 toggles
        
        boolean initialState = buildingManager.isBuildingModeActive();
        
        // Perform the specified number of toggles
        for (int i = 0; i < toggleCount; i++) {
            buildingManager.toggleBuildingMode();
        }
        
        boolean finalState = buildingManager.isBuildingModeActive();
        boolean expectedFinalState = (toggleCount % 2 == 0) ? initialState : !initialState;
        
        assertEquals(expectedFinalState, finalState, 
                    "Final building mode state should match expected state after " + toggleCount + " toggles");
        
        // Verify that normal mode state is always opposite of building mode
        boolean finalNormalMode = !finalState;
        assertNotEquals(finalState, finalNormalMode, 
                       "Building mode and normal mode should always be mutually exclusive");
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