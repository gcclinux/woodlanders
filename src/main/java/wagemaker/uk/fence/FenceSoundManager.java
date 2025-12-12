package wagemaker.uk.fence;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

/**
 * Manages sound effects for fence building operations.
 * Provides placement, removal, and error sound feedback.
 */
public class FenceSoundManager {
    
    /** Sound for successful fence placement */
    private Sound placementSound;
    
    /** Sound for successful fence removal */
    private Sound removalSound;
    
    /** Sound for error feedback (invalid placement/removal) */
    private Sound errorSound;
    
    /** Flag to track if sounds are loaded successfully */
    private boolean soundsLoaded;
    
    /** Volume level for fence sounds (0.0 to 1.0) */
    private float volume;
    
    /**
     * Creates a new fence sound manager and loads sound assets.
     */
    public FenceSoundManager() {
        this.volume = 0.5f; // Default volume
        loadSounds();
    }
    
    /**
     * Creates a new fence sound manager with specified volume.
     * 
     * @param volume Volume level (0.0 to 1.0)
     */
    public FenceSoundManager(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
        loadSounds();
    }
    
    /**
     * Loads all fence sound assets.
     * Handles loading errors gracefully by setting soundsLoaded to false.
     */
    private void loadSounds() {
        try {
            // Load placement sound
            placementSound = Gdx.audio.newSound(Gdx.files.internal("sound/fence_place.mp3"));
            System.out.println("[FENCE] Placement sound loaded successfully");
            
            // Load removal sound
            removalSound = Gdx.audio.newSound(Gdx.files.internal("sound/fence_remove.mp3"));
            System.out.println("[FENCE] Removal sound loaded successfully");
            
            // Load error sound
            errorSound = Gdx.audio.newSound(Gdx.files.internal("sound/fence_error.mp3"));
            System.out.println("[FENCE] Error sound loaded successfully");
            
            soundsLoaded = true;
            System.out.println("[FENCE] All fence sounds loaded successfully");
            
        } catch (Exception e) {
            System.err.println("[FENCE] Failed to load fence sounds: " + e.getMessage());
            soundsLoaded = false;
            
            // Set all sounds to null for safety
            placementSound = null;
            removalSound = null;
            errorSound = null;
        }
    }
    
    /**
     * Plays the fence placement sound effect.
     * Only plays if sounds are loaded successfully.
     */
    public void playPlacementSound() {
        if (soundsLoaded && placementSound != null) {
            placementSound.play(volume);
        }
    }
    
    /**
     * Plays the fence removal sound effect.
     * Only plays if sounds are loaded successfully.
     */
    public void playRemovalSound() {
        if (soundsLoaded && removalSound != null) {
            removalSound.play(volume);
        }
    }
    
    /**
     * Plays the error sound effect for invalid operations.
     * Only plays if sounds are loaded successfully.
     */
    public void playErrorSound() {
        if (soundsLoaded && errorSound != null) {
            errorSound.play(volume);
        }
    }
    
    /**
     * Sets the volume level for all fence sounds.
     * 
     * @param volume Volume level (0.0 to 1.0)
     */
    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }
    
    /**
     * Gets the current volume level.
     * 
     * @return Current volume level (0.0 to 1.0)
     */
    public float getVolume() {
        return volume;
    }
    
    /**
     * Checks if all sounds are loaded successfully.
     * 
     * @return true if sounds are loaded, false otherwise
     */
    public boolean areSoundsLoaded() {
        return soundsLoaded;
    }
    
    /**
     * Disposes of all sound resources.
     * Should be called when the sound manager is no longer needed.
     */
    public void dispose() {
        if (placementSound != null) {
            placementSound.dispose();
            placementSound = null;
        }
        
        if (removalSound != null) {
            removalSound.dispose();
            removalSound = null;
        }
        
        if (errorSound != null) {
            errorSound.dispose();
            errorSound = null;
        }
        
        soundsLoaded = false;
        System.out.println("[FENCE] Fence sound manager disposed");
    }
}