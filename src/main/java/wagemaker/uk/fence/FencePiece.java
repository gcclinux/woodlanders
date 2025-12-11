package wagemaker.uk.fence;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

/**
 * Abstract base class for all fence pieces in the custom fence building system.
 * Each fence piece has a position, texture, and collision boundaries.
 * Now optimized to work with texture atlasing for better performance.
 */
public abstract class FencePiece {
    protected float x, y;
    protected Texture texture; // Kept for backward compatibility
    protected FencePieceType type;
    protected static final int PIECE_SIZE = 64; // 64x64 pixel pieces
    protected static final String TEXTURE_PATH = "textures/fense.png";

    /**
     * Creates a new fence piece at the specified position.
     * @param x World X coordinate
     * @param y World Y coordinate
     * @param type The type of fence piece
     */
    public FencePiece(float x, float y, FencePieceType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        loadTexture();
    }

    /**
     * Loads the texture for this fence piece from the sprite sheet.
     * Uses the texture coordinates defined in the FencePieceType enum.
     * Note: This method is kept for backward compatibility but is less efficient
     * than using the FenceRenderer with texture atlas.
     */
    protected void loadTexture() {
        try {
            Texture spriteSheet = new Texture(TEXTURE_PATH);
            Pixmap pixmap = new Pixmap(PIECE_SIZE, PIECE_SIZE, Pixmap.Format.RGBA8888);
            spriteSheet.getTextureData().prepare();
            Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
            
            // Extract the specific piece from the sprite sheet
            pixmap.drawPixmap(sheetPixmap, 0, 0, type.getTextureX(), type.getTextureY(), PIECE_SIZE, PIECE_SIZE);
            
            texture = new Texture(pixmap);
            pixmap.dispose();
            sheetPixmap.dispose();
            spriteSheet.dispose();
        } catch (Exception e) {
            // Create a placeholder texture if loading fails
            createPlaceholderTexture();
            System.err.println("Warning: Failed to load fence texture for " + type + ", using placeholder");
        }
    }

    /**
     * Creates a placeholder texture when the main texture fails to load.
     */
    private void createPlaceholderTexture() {
        Pixmap pixmap = new Pixmap(PIECE_SIZE, PIECE_SIZE, Pixmap.Format.RGBA8888);
        // Use different colors for different piece types
        if (type.isCornerPiece()) {
            pixmap.setColor(0.8f, 0.4f, 0.2f, 1.0f); // Brown for corners
        } else {
            pixmap.setColor(0.6f, 0.3f, 0.1f, 1.0f); // Darker brown for edges
        }
        pixmap.fill();
        texture = new Texture(pixmap);
        pixmap.dispose();
    }

    /**
     * Renders this fence piece using the provided SpriteBatch.
     * @param batch The SpriteBatch to use for rendering
     * @deprecated Use FenceRenderer for optimized rendering with texture atlas
     */
    @Deprecated
    public void render(SpriteBatch batch) {
        if (texture != null) {
            batch.draw(texture, x, y, PIECE_SIZE, PIECE_SIZE);
        }
    }

    /**
     * Renders this fence piece using a texture region from an atlas.
     * This is the preferred rendering method for performance.
     * 
     * @param batch The SpriteBatch to use for rendering
     * @param textureRegion The texture region from the atlas
     */
    public void render(SpriteBatch batch, TextureRegion textureRegion) {
        if (textureRegion != null) {
            batch.draw(textureRegion, x, y, PIECE_SIZE, PIECE_SIZE);
        } else {
            // Fallback to individual texture rendering
            render(batch);
        }
    }

    /**
     * Gets the collision bounds for this fence piece.
     * @return Rectangle representing the collision area
     */
    public Rectangle getCollisionBounds() {
        return new Rectangle(x, y, PIECE_SIZE, PIECE_SIZE);
    }

    /**
     * Gets the world X coordinate of this fence piece.
     * @return X coordinate
     */
    public float getX() {
        return x;
    }

    /**
     * Gets the world Y coordinate of this fence piece.
     * @return Y coordinate
     */
    public float getY() {
        return y;
    }

    /**
     * Gets the type of this fence piece.
     * @return FencePieceType
     */
    public FencePieceType getType() {
        return type;
    }

    /**
     * Gets the texture for this fence piece.
     * @return Texture object, or null if not loaded
     */
    public Texture getTexture() {
        return texture;
    }

    /**
     * Disposes of the texture resources used by this fence piece.
     * Should be called when the fence piece is no longer needed.
     */
    public void dispose() {
        if (texture != null) {
            texture.dispose();
            texture = null;
        }
    }

    /**
     * Checks if this fence piece is at the specified world coordinates.
     * @param worldX World X coordinate
     * @param worldY World Y coordinate
     * @return true if the fence piece is at the specified coordinates
     */
    public boolean isAt(float worldX, float worldY) {
        return Math.abs(x - worldX) < 0.1f && Math.abs(y - worldY) < 0.1f;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FencePiece that = (FencePiece) obj;
        return Float.compare(that.x, x) == 0 && 
               Float.compare(that.y, y) == 0 && 
               type == that.type;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y, type);
    }

    @Override
    public String toString() {
        return String.format("%s at (%.1f, %.1f)", type, x, y);
    }
}