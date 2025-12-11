package wagemaker.uk.fence;

/**
 * Concrete implementation of a fence middle-right edge piece.
 * Used for the right edge of rectangular enclosures.
 */
public class FenceMiddleRightPiece extends FencePiece {
    
    /**
     * Creates a new fence middle-right edge piece.
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public FenceMiddleRightPiece(float x, float y) {
        super(x, y, FencePieceType.FENCE_MIDDLE_RIGHT);
    }
}