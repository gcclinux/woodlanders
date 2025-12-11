package wagemaker.uk.fence;

/**
 * Concrete implementation of a fence front edge piece.
 * Used for the bottom edge of rectangular enclosures.
 */
public class FenceFrontPiece extends FencePiece {
    
    /**
     * Creates a new fence front edge piece.
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public FenceFrontPiece(float x, float y) {
        super(x, y, FencePieceType.FENCE_FRONT);
    }
}