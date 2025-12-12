package wagemaker.uk.fence;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Optimized renderer for fence structures that uses texture atlasing and sprite batching
 * for efficient rendering of large fence structures. Implements viewport culling to
 * only render visible fence pieces.
 */
public class FenceRenderer implements Disposable {
    
    /** Texture atlas containing all fence piece textures */
    private final FenceTextureAtlas textureAtlas;
    
    /** Sprite batch for efficient rendering */
    private final SpriteBatch spriteBatch;
    
    /** Camera for viewport culling calculations */
    private OrthographicCamera camera;
    
    /** Flag indicating if the renderer owns the sprite batch */
    private final boolean ownsBatch;
    
    /** Flag to prevent double disposal */
    private boolean disposed = false;
    
    /** Viewport bounds for culling (updated each frame) */
    private final Rectangle viewportBounds;
    
    /** Culling margin to render slightly outside viewport */
    private static final float CULLING_MARGIN = 128f;
    
    /** Statistics for performance monitoring */
    private int renderedPiecesLastFrame = 0;
    private int culledPiecesLastFrame = 0;
    
    /**
     * Creates a new fence renderer with its own sprite batch.
     * 
     * @param camera Camera for viewport culling
     */
    public FenceRenderer(OrthographicCamera camera) {
        this(new SpriteBatch(), camera, true);
    }
    
    /**
     * Creates a new fence renderer with the specified sprite batch.
     * 
     * @param spriteBatch Sprite batch to use for rendering
     * @param camera Camera for viewport culling
     * @param ownsBatch Whether this renderer owns the sprite batch (for disposal)
     */
    public FenceRenderer(SpriteBatch spriteBatch, OrthographicCamera camera, boolean ownsBatch) {
        this.spriteBatch = spriteBatch;
        this.camera = camera;
        this.ownsBatch = ownsBatch;
        
        // Use shared texture atlas from resource manager for memory efficiency
        FenceResourceManager resourceManager = FenceResourceManager.getInstance();
        this.textureAtlas = resourceManager.getSharedTextureAtlas();
        
        this.viewportBounds = new Rectangle();
        
        // Register this renderer with the resource manager
        resourceManager.registerRenderer("renderer_" + System.identityHashCode(this), this);
        
        updateViewportBounds();
    }
    
    /**
     * Updates the viewport bounds for culling based on the current camera position.
     */
    private void updateViewportBounds() {
        if (camera == null) {
            viewportBounds.set(0, 0, Float.MAX_VALUE, Float.MAX_VALUE);
            return;
        }
        
        float halfWidth = camera.viewportWidth * camera.zoom / 2f + CULLING_MARGIN;
        float halfHeight = camera.viewportHeight * camera.zoom / 2f + CULLING_MARGIN;
        
        viewportBounds.set(
            camera.position.x - halfWidth,
            camera.position.y - halfHeight,
            halfWidth * 2f,
            halfHeight * 2f
        );
    }
    
    /**
     * Renders all fence pieces from the structure manager using optimized batching.
     * Only renders pieces that are visible in the current viewport.
     * 
     * @param structureManager The fence structure manager containing pieces to render
     */
    public void render(FenceStructureManager structureManager) {
        if (!textureAtlas.isInitialized()) {
            System.err.println("[FenceRenderer] Texture atlas not initialized - cannot render fences");
            return;
        }
        
        updateViewportBounds();
        
        Map<Point, FencePiece> allPieces = structureManager.getAllFencePieces();
        if (allPieces.isEmpty()) {
            renderedPiecesLastFrame = 0;
            culledPiecesLastFrame = 0;
            return;
        }
        
        System.out.println("[FenceRenderer] Rendering " + allPieces.size() + " fence pieces");
        
        // Begin batch rendering
        spriteBatch.begin();
        
        int rendered = 0;
        int culled = 0;
        
        // Render all visible fence pieces in a single batch
        for (FencePiece piece : allPieces.values()) {
            if (isInViewport(piece)) {
                renderPiece(piece);
                rendered++;
                System.out.println("[FenceRenderer] Rendered " + piece.getType() + " at (" + piece.getX() + ", " + piece.getY() + ")");
            } else {
                culled++;
                System.out.println("[FenceRenderer] Culled " + piece.getType() + " at (" + piece.getX() + ", " + piece.getY() + ") - outside viewport");
            }
        }
        
        spriteBatch.end();
        
        // Update statistics
        renderedPiecesLastFrame = rendered;
        culledPiecesLastFrame = culled;
        
        System.out.println("[FenceRenderer] Rendered: " + rendered + ", Culled: " + culled);
    }
    
    /**
     * Renders a list of fence pieces using optimized batching.
     * Useful for rendering specific subsets of fence pieces.
     * 
     * @param pieces List of fence pieces to render
     */
    public void renderPieces(List<FencePiece> pieces) {
        if (!textureAtlas.isInitialized() || pieces.isEmpty()) {
            return;
        }
        
        updateViewportBounds();
        
        spriteBatch.begin();
        
        int rendered = 0;
        int culled = 0;
        
        for (FencePiece piece : pieces) {
            if (isInViewport(piece)) {
                renderPiece(piece);
                rendered++;
            } else {
                culled++;
            }
        }
        
        spriteBatch.end();
        
        renderedPiecesLastFrame = rendered;
        culledPiecesLastFrame = culled;
    }
    
    /**
     * Renders a single fence piece using the texture atlas.
     * 
     * @param piece The fence piece to render
     */
    private void renderPiece(FencePiece piece) {
        TextureRegion region = textureAtlas.getTextureRegion(piece.getType());
        if (region != null) {
            spriteBatch.draw(region, piece.getX(), piece.getY(), 
                           textureAtlas.getPieceSize(), textureAtlas.getPieceSize());
        }
    }
    
    /**
     * Checks if a fence piece is within the current viewport bounds.
     * 
     * @param piece The fence piece to check
     * @return true if the piece is visible, false otherwise
     */
    private boolean isInViewport(FencePiece piece) {
        if (camera == null) {
            System.out.println("[FenceRenderer] No camera - rendering all pieces");
            return true; // No culling if no camera
        }
        
        float pieceSize = textureAtlas.getPieceSize();
        Rectangle pieceRect = new Rectangle(piece.getX(), piece.getY(), pieceSize, pieceSize);
        boolean inViewport = viewportBounds.overlaps(pieceRect);
        
        if (!inViewport) {
            System.out.println("[FenceRenderer] Piece at (" + piece.getX() + ", " + piece.getY() + ") outside viewport bounds: " + 
                             "viewport=(" + viewportBounds.x + ", " + viewportBounds.y + ", " + viewportBounds.width + ", " + viewportBounds.height + "), " +
                             "camera=(" + camera.position.x + ", " + camera.position.y + ")");
        }
        
        return inViewport;
    }
    
    /**
     * Renders fence pieces within a specific area for efficient partial updates.
     * 
     * @param structureManager The fence structure manager
     * @param area The area to render (in world coordinates)
     */
    public void renderArea(FenceStructureManager structureManager, Rectangle area) {
        if (!textureAtlas.isInitialized()) {
            return;
        }
        
        Map<Point, FencePiece> allPieces = structureManager.getAllFencePieces();
        List<FencePiece> piecesInArea = new ArrayList<>();
        
        // Filter pieces that are in the specified area
        for (FencePiece piece : allPieces.values()) {
            float pieceSize = textureAtlas.getPieceSize();
            Rectangle pieceRect = new Rectangle(piece.getX(), piece.getY(), pieceSize, pieceSize);
            if (area.overlaps(pieceRect)) {
                piecesInArea.add(piece);
            }
        }
        
        renderPieces(piecesInArea);
    }
    
    /**
     * Sets the camera used for viewport culling.
     * 
     * @param camera The camera to use, or null to disable culling
     */
    public void setCamera(OrthographicCamera camera) {
        this.camera = camera;
        updateViewportBounds();
    }
    
    /**
     * Gets the camera used for viewport culling.
     * 
     * @return The current camera, or null if none set
     */
    public OrthographicCamera getCamera() {
        return camera;
    }
    
    /**
     * Gets the texture atlas used by this renderer.
     * 
     * @return The fence texture atlas
     */
    public FenceTextureAtlas getTextureAtlas() {
        return textureAtlas;
    }
    
    /**
     * Gets the sprite batch used by this renderer.
     * 
     * @return The sprite batch
     */
    public SpriteBatch getSpriteBatch() {
        return spriteBatch;
    }
    
    /**
     * Gets the number of fence pieces rendered in the last frame.
     * 
     * @return Number of rendered pieces
     */
    public int getRenderedPiecesLastFrame() {
        return renderedPiecesLastFrame;
    }
    
    /**
     * Gets the number of fence pieces culled in the last frame.
     * 
     * @return Number of culled pieces
     */
    public int getCulledPiecesLastFrame() {
        return culledPiecesLastFrame;
    }
    
    /**
     * Gets the total number of pieces processed in the last frame.
     * 
     * @return Total pieces (rendered + culled)
     */
    public int getTotalPiecesLastFrame() {
        return renderedPiecesLastFrame + culledPiecesLastFrame;
    }
    
    /**
     * Gets the culling efficiency as a percentage.
     * 
     * @return Percentage of pieces culled (0-100)
     */
    public float getCullingEfficiency() {
        int total = getTotalPiecesLastFrame();
        if (total == 0) {
            return 0f;
        }
        return (culledPiecesLastFrame / (float) total) * 100f;
    }
    
    /**
     * Checks if the renderer is ready for rendering.
     * 
     * @return true if the texture atlas is initialized
     */
    public boolean isReady() {
        return textureAtlas.isInitialized();
    }
    
    @Override
    public void dispose() {
        if (disposed) {
            return; // Already disposed, prevent double disposal
        }
        
        disposed = true;
        
        // Unregister from resource manager
        FenceResourceManager resourceManager = FenceResourceManager.getInstance();
        resourceManager.unregisterRenderer("renderer_" + System.identityHashCode(this));
        
        // Release shared texture atlas reference
        if (textureAtlas != null) {
            resourceManager.releaseSharedTextureAtlas();
        }
        
        if (ownsBatch && spriteBatch != null) {
            spriteBatch.dispose();
            // Note: spriteBatch is final, so we can't null it, but it's now disposed
        }
    }
    
    @Override
    public String toString() {
        return String.format("FenceRenderer[ready=%s, lastFrame: rendered=%d, culled=%d, efficiency=%.1f%%]",
                           isReady(), renderedPiecesLastFrame, culledPiecesLastFrame, getCullingEfficiency());
    }
}