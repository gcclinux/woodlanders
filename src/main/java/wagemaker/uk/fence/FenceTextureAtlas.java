package wagemaker.uk.fence;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;
import java.util.Map;

/**
 * Texture atlas for fence pieces that combines all fence piece textures into a single texture
 * for efficient rendering. Provides texture regions for each fence piece type.
 */
public class FenceTextureAtlas implements Disposable {
    
    /** The combined texture atlas */
    private Texture atlasTexture;
    
    /** Map of fence piece types to their texture regions in the atlas */
    private final Map<FencePieceType, TextureRegion> textureRegions;
    
    /** Size of each fence piece in pixels */
    private static final int PIECE_SIZE = 64;
    
    /** Path to the fence sprite sheet */
    private static final String TEXTURE_PATH = "textures/fense.png";
    
    /** Flag indicating if the atlas has been initialized */
    private boolean initialized = false;
    
    /**
     * Creates a new fence texture atlas.
     */
    public FenceTextureAtlas() {
        this.textureRegions = new HashMap<>();
        initialize();
    }
    
    /**
     * Initializes the texture atlas by loading the fence sprite sheet
     * and creating texture regions for each fence piece type.
     */
    private void initialize() {
        try {
            // Load the main fence texture
            atlasTexture = new Texture(TEXTURE_PATH);
            
            // Create texture regions for each fence piece type
            for (FencePieceType type : FencePieceType.values()) {
                TextureRegion region = new TextureRegion(
                    atlasTexture,
                    type.getTextureX(),
                    type.getTextureY(),
                    PIECE_SIZE,
                    PIECE_SIZE
                );
                textureRegions.put(type, region);
            }
            
            initialized = true;
            System.out.println("Fence texture atlas initialized successfully with " + 
                             textureRegions.size() + " regions");
            
        } catch (Exception e) {
            System.err.println("Failed to load fence texture atlas: " + e.getMessage());
            createPlaceholderAtlas();
        }
    }
    
    /**
     * Creates a placeholder atlas with colored rectangles when the main texture fails to load.
     */
    private void createPlaceholderAtlas() {
        // Create a 256x128 atlas (4x2 grid of 64x64 pieces)
        Pixmap atlasPixmap = new Pixmap(256, 128, Pixmap.Format.RGBA8888);
        
        int index = 0;
        for (FencePieceType type : FencePieceType.values()) {
            int atlasX = (index % 4) * PIECE_SIZE;
            int atlasY = (index / 4) * PIECE_SIZE;
            
            // Set color based on piece type
            if (type.isCornerPiece()) {
                atlasPixmap.setColor(0.8f, 0.4f, 0.2f, 1.0f); // Brown for corners
            } else {
                atlasPixmap.setColor(0.6f, 0.3f, 0.1f, 1.0f); // Darker brown for edges
            }
            
            // Fill the region for this piece type
            atlasPixmap.fillRectangle(atlasX, atlasY, PIECE_SIZE, PIECE_SIZE);
            
            // Create texture region
            TextureRegion region = new TextureRegion();
            textureRegions.put(type, region);
            
            index++;
        }
        
        atlasTexture = new Texture(atlasPixmap);
        atlasPixmap.dispose();
        
        // Update texture regions with the actual atlas texture
        index = 0;
        for (FencePieceType type : FencePieceType.values()) {
            int atlasX = (index % 4) * PIECE_SIZE;
            int atlasY = (index / 4) * PIECE_SIZE;
            
            TextureRegion region = textureRegions.get(type);
            region.setTexture(atlasTexture);
            region.setRegion(atlasX, atlasY, PIECE_SIZE, PIECE_SIZE);
            
            index++;
        }
        
        initialized = true;
        System.out.println("Created placeholder fence texture atlas");
    }
    
    /**
     * Gets the texture region for the specified fence piece type.
     * 
     * @param type The fence piece type
     * @return TextureRegion for the specified type, or null if not found
     */
    public TextureRegion getTextureRegion(FencePieceType type) {
        return textureRegions.get(type);
    }
    
    /**
     * Gets the atlas texture.
     * 
     * @return The combined atlas texture
     */
    public Texture getAtlasTexture() {
        return atlasTexture;
    }
    
    /**
     * Checks if the atlas has been successfully initialized.
     * 
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Gets the size of each fence piece in pixels.
     * 
     * @return Piece size in pixels
     */
    public int getPieceSize() {
        return PIECE_SIZE;
    }
    
    /**
     * Gets the number of texture regions in the atlas.
     * 
     * @return Number of regions
     */
    public int getRegionCount() {
        return textureRegions.size();
    }
    
    @Override
    public void dispose() {
        if (atlasTexture != null) {
            atlasTexture.dispose();
            atlasTexture = null;
        }
        textureRegions.clear();
        initialized = false;
    }
    
    @Override
    public String toString() {
        return String.format("FenceTextureAtlas[initialized=%s, regions=%d]", 
                           initialized, textureRegions.size());
    }
}