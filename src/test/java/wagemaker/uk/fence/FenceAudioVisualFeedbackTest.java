package wagemaker.uk.fence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import java.awt.Point;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for fence audio-visual feedback components.
 * Tests sound effect triggering, visual feedback timing, and animation completion.
 */
public class FenceAudioVisualFeedbackTest {
    
    private FenceSoundManager soundManager;
    private FenceVisualEffectsManager visualEffectsManager;
    private FenceStructureManager structureManager;
    
    @Mock
    private ShapeRenderer mockShapeRenderer;
    
    private HeadlessApplication application;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Initialize headless LibGDX application for testing
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        application = new HeadlessApplication(new ApplicationAdapter() {}, config);
        
        // Initialize test components
        structureManager = new FenceStructureManager();
        
        // Initialize sound manager with low volume for testing
        soundManager = new FenceSoundManager(0.1f);
        
        // Initialize visual effects manager with mock shape renderer
        visualEffectsManager = new FenceVisualEffectsManager(mockShapeRenderer, structureManager);
    }
    
    @AfterEach
    public void tearDown() {
        if (soundManager != null) {
            soundManager.dispose();
        }
        if (application != null) {
            application.exit();
        }
    }
    
    /**
     * Test sound effect triggering for placement operations.
     * Verifies that placement sounds can be triggered without errors.
     */
    @Test
    public void testPlacementSoundTriggering() {
        // Test that sound manager is initialized
        assertNotNull(soundManager, "Sound manager should be initialized");
        
        // Test placement sound triggering (should not throw exceptions)
        assertDoesNotThrow(() -> {
            soundManager.playPlacementSound();
        }, "Placement sound should trigger without exceptions");
        
        // Test multiple rapid triggers (stress test)
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                soundManager.playPlacementSound();
            }
        }, "Multiple placement sounds should trigger without exceptions");
    }
    
    /**
     * Test sound effect triggering for removal operations.
     * Verifies that removal sounds can be triggered without errors.
     */
    @Test
    public void testRemovalSoundTriggering() {
        // Test removal sound triggering
        assertDoesNotThrow(() -> {
            soundManager.playRemovalSound();
        }, "Removal sound should trigger without exceptions");
        
        // Test multiple rapid triggers
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 5; i++) {
                soundManager.playRemovalSound();
            }
        }, "Multiple removal sounds should trigger without exceptions");
    }
    
    /**
     * Test sound effect triggering for error feedback.
     * Verifies that error sounds can be triggered without errors.
     */
    @Test
    public void testErrorSoundTriggering() {
        // Test error sound triggering
        assertDoesNotThrow(() -> {
            soundManager.playErrorSound();
        }, "Error sound should trigger without exceptions");
        
        // Test multiple rapid triggers
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 3; i++) {
                soundManager.playErrorSound();
            }
        }, "Multiple error sounds should trigger without exceptions");
    }
    
    /**
     * Test sound manager volume control.
     * Verifies that volume can be set and retrieved correctly.
     */
    @Test
    public void testSoundVolumeControl() {
        // Test initial volume
        assertEquals(0.1f, soundManager.getVolume(), 0.001f, 
                    "Initial volume should match constructor parameter");
        
        // Test volume setting
        soundManager.setVolume(0.5f);
        assertEquals(0.5f, soundManager.getVolume(), 0.001f, 
                    "Volume should be updated correctly");
        
        // Test volume bounds (should clamp to 0.0-1.0)
        soundManager.setVolume(-0.5f);
        assertEquals(0.0f, soundManager.getVolume(), 0.001f, 
                    "Volume should be clamped to minimum 0.0");
        
        soundManager.setVolume(1.5f);
        assertEquals(1.0f, soundManager.getVolume(), 0.001f, 
                    "Volume should be clamped to maximum 1.0");
    }
    
    /**
     * Test visual feedback timing for placement animations.
     * Verifies that placement animations are created and managed correctly.
     */
    @Test
    public void testPlacementAnimationTiming() {
        // Test initial state - no active effects
        assertEquals(0, visualEffectsManager.getActiveEffectCount(), 
                    "Should have no active effects initially");
        
        // Trigger placement animation
        Point testPos = new Point(5, 5);
        visualEffectsManager.triggerPlacementAnimation(testPos, FenceMaterialType.WOOD);
        
        // Verify effect was created
        assertEquals(1, visualEffectsManager.getActiveEffectCount(), 
                    "Should have one active effect after triggering placement animation");
        
        // Update effects over time to test duration
        float deltaTime = 0.1f;
        for (int i = 0; i < 6; i++) { // 0.6 seconds total (should exceed 0.5s duration)
            visualEffectsManager.update(deltaTime);
        }
        
        // Effect should be finished and removed
        assertEquals(0, visualEffectsManager.getActiveEffectCount(), 
                    "Effect should be finished and removed after duration");
    }
    
    /**
     * Test visual feedback timing for removal animations.
     * Verifies that removal animations are created and managed correctly.
     */
    @Test
    public void testRemovalAnimationTiming() {
        // Trigger removal animation
        Point testPos = new Point(3, 7);
        visualEffectsManager.triggerRemovalAnimation(testPos, FenceMaterialType.BAMBOO);
        
        // Verify effect was created
        assertEquals(1, visualEffectsManager.getActiveEffectCount(), 
                    "Should have one active effect after triggering removal animation");
        
        // Update effects over time (removal duration is 0.4s)
        float deltaTime = 0.1f;
        for (int i = 0; i < 5; i++) { // 0.5 seconds total (should exceed 0.4s duration)
            visualEffectsManager.update(deltaTime);
        }
        
        // Effect should be finished and removed
        assertEquals(0, visualEffectsManager.getActiveEffectCount(), 
                    "Removal effect should be finished and removed after duration");
    }
    
    /**
     * Test visual feedback timing for error animations.
     * Verifies that error animations are created and managed correctly.
     */
    @Test
    public void testErrorAnimationTiming() {
        // Trigger error animation
        Point testPos = new Point(1, 1);
        visualEffectsManager.triggerErrorAnimation(testPos, "Test error message");
        
        // Verify effect was created
        assertEquals(1, visualEffectsManager.getActiveEffectCount(), 
                    "Should have one active effect after triggering error animation");
        
        // Update effects over time (error duration is 0.8s)
        float deltaTime = 0.1f;
        for (int i = 0; i < 9; i++) { // 0.9 seconds total (should exceed 0.8s duration)
            visualEffectsManager.update(deltaTime);
        }
        
        // Effect should be finished and removed
        assertEquals(0, visualEffectsManager.getActiveEffectCount(), 
                    "Error effect should be finished and removed after duration");
    }
    
    /**
     * Test material count change animation timing.
     * Verifies that material count animations are created and managed correctly.
     */
    @Test
    public void testMaterialCountAnimationTiming() {
        // Trigger material count change animation
        visualEffectsManager.triggerMaterialCountAnimation(100f, 200f, FenceMaterialType.WOOD, -1);
        
        // Verify effect was created
        assertEquals(1, visualEffectsManager.getActiveEffectCount(), 
                    "Should have one active effect after triggering material count animation");
        
        // Update effects over time (material count duration is 1.0s)
        float deltaTime = 0.2f;
        for (int i = 0; i < 6; i++) { // 1.2 seconds total (should exceed 1.0s duration)
            visualEffectsManager.update(deltaTime);
        }
        
        // Effect should be finished and removed
        assertEquals(0, visualEffectsManager.getActiveEffectCount(), 
                    "Material count effect should be finished and removed after duration");
    }
    
    /**
     * Test multiple simultaneous animations.
     * Verifies that multiple effects can be active simultaneously and complete independently.
     */
    @Test
    public void testMultipleSimultaneousAnimations() {
        // Trigger multiple different animations
        Point pos1 = new Point(1, 1);
        Point pos2 = new Point(2, 2);
        Point pos3 = new Point(3, 3);
        
        visualEffectsManager.triggerPlacementAnimation(pos1, FenceMaterialType.WOOD);
        visualEffectsManager.triggerRemovalAnimation(pos2, FenceMaterialType.BAMBOO);
        visualEffectsManager.triggerErrorAnimation(pos3, "Test error");
        
        // Verify all effects were created
        assertEquals(3, visualEffectsManager.getActiveEffectCount(), 
                    "Should have three active effects");
        
        // Update for a short time (0.3s) - only removal should finish (0.4s duration)
        float deltaTime = 0.1f;
        for (int i = 0; i < 3; i++) {
            visualEffectsManager.update(deltaTime);
        }
        
        // Should still have effects running
        assertTrue(visualEffectsManager.getActiveEffectCount() > 0, 
                  "Should still have active effects after short duration");
        
        // Update for longer time (1.0s total) - all should finish
        for (int i = 0; i < 7; i++) {
            visualEffectsManager.update(deltaTime);
        }
        
        // All effects should be finished
        assertEquals(0, visualEffectsManager.getActiveEffectCount(), 
                    "All effects should be finished after sufficient time");
    }
    
    /**
     * Test animation completion consistency.
     * Verifies that animations complete reliably and don't leave orphaned effects.
     */
    @Test
    public void testAnimationCompletionConsistency() {
        // Test multiple cycles of animation creation and completion
        for (int cycle = 0; cycle < 3; cycle++) {
            // Create several animations
            for (int i = 0; i < 4; i++) {
                Point pos = new Point(i, cycle);
                visualEffectsManager.triggerPlacementAnimation(pos, FenceMaterialType.WOOD);
            }
            
            // Verify effects were created
            assertEquals(4, visualEffectsManager.getActiveEffectCount(), 
                        "Should have 4 active effects in cycle " + cycle);
            
            // Run effects to completion
            float deltaTime = 0.1f;
            for (int i = 0; i < 6; i++) { // 0.6s total
                visualEffectsManager.update(deltaTime);
            }
            
            // Verify all effects completed
            assertEquals(0, visualEffectsManager.getActiveEffectCount(), 
                        "All effects should be completed in cycle " + cycle);
        }
    }
    
    /**
     * Test visual effects manager clear functionality.
     * Verifies that effects can be cleared manually.
     */
    @Test
    public void testVisualEffectsClear() {
        // Create several effects
        Point pos1 = new Point(1, 1);
        Point pos2 = new Point(2, 2);
        
        visualEffectsManager.triggerPlacementAnimation(pos1, FenceMaterialType.WOOD);
        visualEffectsManager.triggerRemovalAnimation(pos2, FenceMaterialType.BAMBOO);
        
        // Verify effects were created
        assertEquals(2, visualEffectsManager.getActiveEffectCount(), 
                    "Should have 2 active effects");
        
        // Clear all effects
        visualEffectsManager.clearEffects();
        
        // Verify effects were cleared
        assertEquals(0, visualEffectsManager.getActiveEffectCount(), 
                    "All effects should be cleared");
    }
    
    /**
     * Test render method doesn't throw exceptions.
     * Verifies that the render method can be called safely.
     */
    @Test
    public void testRenderSafety() {
        // Test rendering with no effects
        assertDoesNotThrow(() -> {
            visualEffectsManager.render();
        }, "Render should not throw exceptions with no effects");
        
        // Test rendering with active effects
        Point pos = new Point(5, 5);
        visualEffectsManager.triggerPlacementAnimation(pos, FenceMaterialType.WOOD);
        
        assertDoesNotThrow(() -> {
            visualEffectsManager.render();
        }, "Render should not throw exceptions with active effects");
    }
}