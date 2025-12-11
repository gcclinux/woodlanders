package wagemaker.uk.fence;

/**
 * Concrete implementation of a fence middle-left edge piece.
 * Used for the left edge of rectangular enclosures.
 */
public class FenceMiddleLeftPiece extends FencePiece {
    
    /**
     * Creates a new fence middle-left edge piece.
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public FenceMiddleLeftPiece(float x, float y) {
        super(x, y, FencePieceType.FENCE_MIDDLE_LEFT);
    }
}