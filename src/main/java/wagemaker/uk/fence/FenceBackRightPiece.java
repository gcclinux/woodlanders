package wagemaker.uk.fence;

/**
 * Concrete implementation of a fence back-right corner piece.
 * Used at the top-right corner of rectangular enclosures.
 */
public class FenceBackRightPiece extends FencePiece {
    
    /**
     * Creates a new fence back-right corner piece.
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public FenceBackRightPiece(float x, float y) {
        super(x, y, FencePieceType.FENCE_BACK_RIGHT);
    }
}