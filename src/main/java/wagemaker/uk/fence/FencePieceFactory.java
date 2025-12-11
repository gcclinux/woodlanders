package wagemaker.uk.fence;

import com.badlogic.gdx.math.Rectangle;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Factory class for creating fence pieces and calculating enclosure sequences.
 * Provides methods for creating individual fence pieces and determining the
 * correct sequence of pieces for rectangular enclosures.
 */
public class FencePieceFactory {
    
    /**
     * Creates a fence piece of the specified type at the given position.
     * @param type The type of fence piece to create
     * @param x World X coordinate
     * @param y World Y coordinate
     * @return A new FencePiece instance of the specified type
     * @throws IllegalArgumentException if the fence piece type is unknown
     */
    public static FencePiece createPiece(FencePieceType type, float x, float y) {
        switch (type) {
            case FENCE_BACK_LEFT:
                return new FenceBackLeftPiece(x, y);
            case FENCE_BACK:
                return new FenceBackPiece(x, y);
            case FENCE_BACK_RIGHT:
                return new FenceBackRightPiece(x, y);
            case FENCE_MIDDLE_RIGHT:
                return new FenceMiddleRightPiece(x, y);
            case FENCE_FRONT_RIGHT:
                return new FenceFrontRightPiece(x, y);
            case FENCE_FRONT:
                return new FenceFrontPiece(x, y);
            case FENCE_FRONT_LEFT:
                return new FenceFrontLeftPiece(x, y);
            case FENCE_MIDDLE_LEFT:
                return new FenceMiddleLeftPiece(x, y);
            default:
                throw new IllegalArgumentException("Unknown fence piece type: " + type);
        }
    }
    
    /**
     * Gets the sequence of fence piece types needed for a rectangular enclosure.
     * The sequence follows clockwise order starting from the top-left corner.
     * For larger rectangles, edge pieces are repeated as needed.
     * 
     * @param bounds Rectangle defining the enclosure area (in grid coordinates)
     * @return Array of FencePieceType in clockwise order
     * @throws IllegalArgumentException if bounds are invalid (width or height < 2)
     */
    public static FencePieceType[] getEnclosureSequence(Rectangle bounds) {
        if (bounds.width < 2 || bounds.height < 2) {
            throw new IllegalArgumentException("Enclosure must be at least 2x2 grid units");
        }
        
        int width = (int) bounds.width;
        int height = (int) bounds.height;
        
        // Calculate total pieces needed: perimeter of rectangle
        int totalPieces = 2 * (width + height - 2);
        List<FencePieceType> sequence = new ArrayList<>();
        
        // Top edge (left to right)
        sequence.add(FencePieceType.FENCE_BACK_LEFT); // Top-left corner
        for (int i = 1; i < width - 1; i++) {
            sequence.add(FencePieceType.FENCE_BACK); // Top edge pieces
        }
        sequence.add(FencePieceType.FENCE_BACK_RIGHT); // Top-right corner
        
        // Right edge (top to bottom)
        for (int i = 1; i < height - 1; i++) {
            sequence.add(FencePieceType.FENCE_MIDDLE_RIGHT); // Right edge pieces
        }
        
        // Bottom edge (right to left)
        sequence.add(FencePieceType.FENCE_FRONT_RIGHT); // Bottom-right corner
        for (int i = 1; i < width - 1; i++) {
            sequence.add(FencePieceType.FENCE_FRONT); // Bottom edge pieces
        }
        sequence.add(FencePieceType.FENCE_FRONT_LEFT); // Bottom-left corner
        
        // Left edge (bottom to top)
        for (int i = 1; i < height - 1; i++) {
            sequence.add(FencePieceType.FENCE_MIDDLE_LEFT); // Left edge pieces
        }
        
        return sequence.toArray(new FencePieceType[0]);
    }
    
    /**
     * Calculates the number of fence pieces needed for a rectangular enclosure.
     * This is the perimeter of the rectangle in grid units.
     * 
     * @param bounds Rectangle defining the enclosure area (in grid coordinates)
     * @return Total number of fence pieces required
     * @throws IllegalArgumentException if bounds are invalid (width or height < 2)
     */
    public static int calculateMaterialRequirement(Rectangle bounds) {
        if (bounds.width < 2 || bounds.height < 2) {
            throw new IllegalArgumentException("Enclosure must be at least 2x2 grid units");
        }
        
        int width = (int) bounds.width;
        int height = (int) bounds.height;
        
        // Perimeter calculation: 2 * (width + height) - 4 to avoid double-counting corners
        return 2 * (width + height) - 4;
    }
    
    /**
     * Calculates the material requirement for a rectangular enclosure of given dimensions.
     * Convenience method that creates a Rectangle and calculates material requirement.
     * 
     * @param width Width of the enclosure in grid units
     * @param height Height of the enclosure in grid units
     * @return Total number of fence pieces required
     * @throws IllegalArgumentException if width or height < 2
     */
    public static int calculateMaterialRequirement(int width, int height) {
        return calculateMaterialRequirement(new Rectangle(0, 0, width, height));
    }
    
    /**
     * Creates a complete rectangular enclosure at the specified position.
     * Returns a list of fence pieces positioned correctly for the enclosure.
     * 
     * @param startX Starting X coordinate (top-left corner)
     * @param startY Starting Y coordinate (top-left corner)
     * @param width Width of the enclosure in grid units
     * @param height Height of the enclosure in grid units
     * @param gridSize Size of each grid cell in world units (typically 64)
     * @return List of positioned fence pieces forming the complete enclosure
     * @throws IllegalArgumentException if width or height < 2
     */
    public static List<FencePiece> createCompleteEnclosure(float startX, float startY, 
                                                          int width, int height, int gridSize) {
        Rectangle bounds = new Rectangle(0, 0, width, height);
        FencePieceType[] sequence = getEnclosureSequence(bounds);
        List<FencePiece> pieces = new ArrayList<>();
        
        int sequenceIndex = 0;
        
        // Top edge (left to right)
        for (int x = 0; x < width; x++) {
            float worldX = startX + x * gridSize;
            float worldY = startY;
            pieces.add(createPiece(sequence[sequenceIndex++], worldX, worldY));
        }
        
        // Right edge (top to bottom, excluding corners)
        for (int y = 1; y < height - 1; y++) {
            float worldX = startX + (width - 1) * gridSize;
            float worldY = startY - y * gridSize;
            pieces.add(createPiece(sequence[sequenceIndex++], worldX, worldY));
        }
        
        // Bottom edge (right to left)
        for (int x = width - 1; x >= 0; x--) {
            float worldX = startX + x * gridSize;
            float worldY = startY - (height - 1) * gridSize;
            pieces.add(createPiece(sequence[sequenceIndex++], worldX, worldY));
        }
        
        // Left edge (bottom to top, excluding corners)
        for (int y = height - 2; y > 0; y--) {
            float worldX = startX;
            float worldY = startY - y * gridSize;
            pieces.add(createPiece(sequence[sequenceIndex++], worldX, worldY));
        }
        
        return pieces;
    }
    
    /**
     * Determines the appropriate fence piece type for a position within a rectangular pattern.
     * This method analyzes the position relative to the enclosure bounds to select
     * the correct piece type (corner or edge).
     * 
     * @param gridX X coordinate in grid units (relative to enclosure)
     * @param gridY Y coordinate in grid units (relative to enclosure)
     * @param width Width of the enclosure in grid units
     * @param height Height of the enclosure in grid units
     * @return The appropriate FencePieceType for this position
     * @throws IllegalArgumentException if position is not on the perimeter
     */
    public static FencePieceType determinePieceTypeForPosition(int gridX, int gridY, 
                                                              int width, int height) {
        // Check if position is on the perimeter
        boolean onLeftEdge = (gridX == 0);
        boolean onRightEdge = (gridX == width - 1);
        boolean onTopEdge = (gridY == 0);
        boolean onBottomEdge = (gridY == height - 1);
        
        if (!onLeftEdge && !onRightEdge && !onTopEdge && !onBottomEdge) {
            throw new IllegalArgumentException("Position (" + gridX + ", " + gridY + 
                                             ") is not on the perimeter of a " + width + "x" + height + " enclosure");
        }
        
        // Determine piece type based on position
        if (onTopEdge && onLeftEdge) {
            return FencePieceType.FENCE_BACK_LEFT;
        } else if (onTopEdge && onRightEdge) {
            return FencePieceType.FENCE_BACK_RIGHT;
        } else if (onBottomEdge && onRightEdge) {
            return FencePieceType.FENCE_FRONT_RIGHT;
        } else if (onBottomEdge && onLeftEdge) {
            return FencePieceType.FENCE_FRONT_LEFT;
        } else if (onTopEdge) {
            return FencePieceType.FENCE_BACK;
        } else if (onRightEdge) {
            return FencePieceType.FENCE_MIDDLE_RIGHT;
        } else if (onBottomEdge) {
            return FencePieceType.FENCE_FRONT;
        } else if (onLeftEdge) {
            return FencePieceType.FENCE_MIDDLE_LEFT;
        }
        
        // This should never be reached due to the perimeter check above
        throw new IllegalStateException("Unable to determine fence piece type for position (" + 
                                      gridX + ", " + gridY + ")");
    }
    
    /**
     * Validates that a fence piece sequence is correct for a rectangular enclosure.
     * Checks that the sequence contains the right number and types of pieces.
     * 
     * @param sequence Array of fence piece types to validate
     * @param width Width of the intended enclosure
     * @param height Height of the intended enclosure
     * @return true if the sequence is valid, false otherwise
     */
    public static boolean validateEnclosureSequence(FencePieceType[] sequence, int width, int height) {
        if (width < 2 || height < 2) {
            return false;
        }
        
        int expectedLength = 2 * (width + height - 2);
        if (sequence.length != expectedLength) {
            return false;
        }
        
        // Count expected pieces
        int expectedCorners = 4;
        int expectedEdges = expectedLength - 4;
        
        int actualCorners = 0;
        int actualEdges = 0;
        
        for (FencePieceType type : sequence) {
            if (type.isCornerPiece()) {
                actualCorners++;
            } else if (type.isEdgePiece()) {
                actualEdges++;
            }
        }
        
        return actualCorners == expectedCorners && actualEdges == expectedEdges;
    }
}