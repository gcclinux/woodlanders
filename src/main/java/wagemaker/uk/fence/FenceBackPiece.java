package wagemaker.uk.fence;

/**
 * Concrete implementation of a fence back edge piece.
 * Used for the top edge of rectangular enclosures.
 */
public class FenceBackPiece extends FencePiece {
    
    /**
     * Creates a new fence back edge piece.
     * @param x World X coordinate
     * @param y World Y coordinate
     */
    public FenceBackPiece(float x, float y) {
        super(x, y, FencePieceType.FENCE_BACK);
    }
}