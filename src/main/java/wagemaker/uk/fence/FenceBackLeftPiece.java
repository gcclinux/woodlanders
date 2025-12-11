package wagemaker.uk.fence;

/**
 * Concrete implementation of a fence back-left corner piece.
 * Used at the top-left corner of rectangular enclosures.
 */
public class FenceBackLeftPiece extends FencePiece {
    
    /**
     * Creates a new fence back-left corner piece.
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public FenceBackLeftPiece(float x, float y) {
        super(x, y, FencePieceType.FENCE_BACK_LEFT);
    }
}