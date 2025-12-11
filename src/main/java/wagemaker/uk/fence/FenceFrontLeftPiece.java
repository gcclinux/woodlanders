package wagemaker.uk.fence;

/**
 * Concrete implementation of a fence front-left corner piece.
 * Used at the bottom-left corner of rectangular enclosures.
 */
public class FenceFrontLeftPiece extends FencePiece {
    
    /**
     * Creates a new fence front-left corner piece.
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public FenceFrontLeftPiece(float x, float y) {
        super(x, y, FencePieceType.FENCE_FRONT_LEFT);
    }
}