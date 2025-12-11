package wagemaker.uk.fence;

/**
 * Concrete implementation of a fence front-right corner piece.
 * Used at the bottom-right corner of rectangular enclosures.
 */
public class FenceFrontRightPiece extends FencePiece {
    
    /**
     * Creates a new fence front-right corner piece.
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public FenceFrontRightPiece(float x, float y) {
        super(x, y, FencePieceType.FENCE_FRONT_RIGHT);
    }
}