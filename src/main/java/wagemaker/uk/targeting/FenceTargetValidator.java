package wagemaker.uk.targeting;

import wagemaker.uk.fence.FenceBuildingManager;
import wagemaker.uk.fence.FencePlacementValidator;
import wagemaker.uk.fence.FenceMaterialType;
import java.awt.Point;

/**
 * Validator for fence placement targets.
 * Integrates with the fence building system to validate fence placement positions.
 * Allows placement on any terrain (grass, sand, etc.) as long as the position is not occupied.
 */
public class FenceTargetValidator implements TargetValidator {
    
    private final FenceBuildingManager fenceBuildingManager;
    
    /**
     * Creates a new FenceTargetValidator.
     * 
     * @param fenceBuildingManager The fence building manager for validation
     */
    public FenceTargetValidator(FenceBuildingManager fenceBuildingManager) {
        this.fenceBuildingManager = fenceBuildingManager;
    }
    
    @Override
    public boolean isValidTarget(float targetX, float targetY) {
        if (fenceBuildingManager == null) {
            return false;
        }
        
        // Check if fence building mode is active
        if (!fenceBuildingManager.isBuildingModeActive()) {
            return false;
        }
        
        // Convert world coordinates to grid coordinates
        wagemaker.uk.fence.FenceGrid fenceGrid = fenceBuildingManager.getStructureManager().getGrid();
        Point gridPos = fenceGrid.worldToGrid(targetX, targetY);
        
        // Get the fence placement validator
        FencePlacementValidator validator = fenceBuildingManager.getValidator();
        if (validator == null) {
            return false;
        }
        
        // Get the selected material type
        FenceMaterialType materialType = fenceBuildingManager.getSelectedMaterialType();
        
        // Validate the placement (use empty player ID for single player)
        FencePlacementValidator.ValidationResult result = 
            validator.validatePlacement(gridPos, materialType, "");
        
        return result.isValid();
    }
}