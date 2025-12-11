package wagemaker.uk.fence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Manages visual effects for fence building operations.
 * Provides placement confirmation animations, removal effects, and material count animations.
 */
public class FenceVisualEffectsManager {
    
    /** List of active visual effects */
    private final List<VisualEffect> activeEffects;
    
    /** Shape renderer for drawing effects */
    private final ShapeRenderer shapeRenderer;
    
    /** Structure manager for grid coordinate conversion */
    private final FenceStructureManager structureManager;
    
    /** Size of fence pieces in pixels */
    private static final float FENCE_PIECE_SIZE = 64f;
    
    /**
     * Creates a new visual effects manager.
     * 
     * @param shapeRenderer Shape renderer for drawing effects
     * @param structureManager Structure manager for coordinate conversion
     */
    public FenceVisualEffectsManager(ShapeRenderer shapeRenderer, FenceStructureManager structureManager) {
        this.shapeRenderer = shapeRenderer;
        this.structureManager = structureManager;
        this.activeEffects = new ArrayList<>();
    }
    
    /**
     * Updates all active visual effects.
     * 
     * @param deltaTime Time elapsed since last update
     */
    public void update(float deltaTime) {
        Iterator<VisualEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            VisualEffect effect = iterator.next();
            effect.update(deltaTime);
            
            if (effect.isFinished()) {
                iterator.remove();
            }
        }
    }
    
    /**
     * Renders all active visual effects.
     */
    public void render() {
        if (activeEffects.isEmpty()) {
            return;
        }
        
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        
        for (VisualEffect effect : activeEffects) {
            effect.render(shapeRenderer);
        }
        
        shapeRenderer.end();
    }
    
    /**
     * Triggers a placement confirmation animation at the specified grid position.
     * 
     * @param gridPos Grid position where placement occurred
     * @param materialType Material type that was placed
     */
    public void triggerPlacementAnimation(Point gridPos, FenceMaterialType materialType) {
        Vector2 worldPos = structureManager.getGrid().gridToWorld(gridPos);
        
        // Create placement confirmation effect
        PlacementConfirmationEffect effect = new PlacementConfirmationEffect(
            worldPos.x + FENCE_PIECE_SIZE / 2,
            worldPos.y + FENCE_PIECE_SIZE / 2,
            materialType
        );
        
        activeEffects.add(effect);
    }
    
    /**
     * Triggers a removal confirmation animation at the specified grid position.
     * 
     * @param gridPos Grid position where removal occurred
     * @param materialType Material type that was removed
     */
    public void triggerRemovalAnimation(Point gridPos, FenceMaterialType materialType) {
        Vector2 worldPos = structureManager.getGrid().gridToWorld(gridPos);
        
        // Create removal confirmation effect
        RemovalConfirmationEffect effect = new RemovalConfirmationEffect(
            worldPos.x + FENCE_PIECE_SIZE / 2,
            worldPos.y + FENCE_PIECE_SIZE / 2,
            materialType
        );
        
        activeEffects.add(effect);
    }
    
    /**
     * Triggers a material count change animation.
     * 
     * @param screenX Screen X position for the animation
     * @param screenY Screen Y position for the animation
     * @param materialType Material type that changed
     * @param countChange Change in material count (positive for gain, negative for loss)
     */
    public void triggerMaterialCountAnimation(float screenX, float screenY, 
                                            FenceMaterialType materialType, 
                                            int countChange) {
        MaterialCountChangeEffect effect = new MaterialCountChangeEffect(
            screenX, screenY, materialType, countChange
        );
        
        activeEffects.add(effect);
    }
    
    /**
     * Triggers an error feedback animation at the specified grid position.
     * 
     * @param gridPos Grid position where error occurred
     * @param errorMessage Error message to display
     */
    public void triggerErrorAnimation(Point gridPos, String errorMessage) {
        Vector2 worldPos = structureManager.getGrid().gridToWorld(gridPos);
        
        ErrorFeedbackEffect effect = new ErrorFeedbackEffect(
            worldPos.x + FENCE_PIECE_SIZE / 2,
            worldPos.y + FENCE_PIECE_SIZE / 2,
            errorMessage
        );
        
        activeEffects.add(effect);
    }
    
    /**
     * Clears all active visual effects.
     */
    public void clearEffects() {
        activeEffects.clear();
    }
    
    /**
     * Gets the number of active visual effects.
     * 
     * @return Number of active effects
     */
    public int getActiveEffectCount() {
        return activeEffects.size();
    }
    
    /**
     * Base class for visual effects.
     */
    private abstract static class VisualEffect {
        protected float x, y;
        protected float duration;
        protected float elapsed;
        protected boolean finished;
        
        public VisualEffect(float x, float y, float duration) {
            this.x = x;
            this.y = y;
            this.duration = duration;
            this.elapsed = 0f;
            this.finished = false;
        }
        
        public void update(float deltaTime) {
            elapsed += deltaTime;
            if (elapsed >= duration) {
                finished = true;
            }
        }
        
        public boolean isFinished() {
            return finished;
        }
        
        protected float getProgress() {
            return Math.min(elapsed / duration, 1.0f);
        }
        
        public abstract void render(ShapeRenderer shapeRenderer);
    }
    
    /**
     * Visual effect for placement confirmation.
     */
    private static class PlacementConfirmationEffect extends VisualEffect {
        private final FenceMaterialType materialType;
        private final Color effectColor;
        
        public PlacementConfirmationEffect(float x, float y, FenceMaterialType materialType) {
            super(x, y, 0.5f); // 0.5 second duration
            this.materialType = materialType;
            
            // Choose color based on material type
            switch (materialType) {
                case WOOD:
                    effectColor = new Color(0.6f, 0.4f, 0.2f, 1.0f); // Brown
                    break;
                case BAMBOO:
                    effectColor = new Color(0.4f, 0.8f, 0.4f, 1.0f); // Green
                    break;
                default:
                    effectColor = new Color(0.5f, 0.5f, 0.5f, 1.0f); // Gray
                    break;
            }
        }
        
        @Override
        public void render(ShapeRenderer shapeRenderer) {
            float progress = getProgress();
            float alpha = 1.0f - progress; // Fade out over time
            float scale = 1.0f + progress * 0.5f; // Grow slightly over time
            
            Color renderColor = new Color(effectColor.r, effectColor.g, effectColor.b, alpha);
            shapeRenderer.setColor(renderColor);
            
            float size = FENCE_PIECE_SIZE * 0.3f * scale;
            shapeRenderer.circle(x, y, size);
        }
    }
    
    /**
     * Visual effect for removal confirmation.
     */
    private static class RemovalConfirmationEffect extends VisualEffect {
        private final FenceMaterialType materialType;
        private final Color effectColor;
        
        public RemovalConfirmationEffect(float x, float y, FenceMaterialType materialType) {
            super(x, y, 0.4f); // 0.4 second duration
            this.materialType = materialType;
            
            // Use red tint for removal effects
            effectColor = new Color(1.0f, 0.3f, 0.3f, 1.0f);
        }
        
        @Override
        public void render(ShapeRenderer shapeRenderer) {
            float progress = getProgress();
            float alpha = 1.0f - progress; // Fade out over time
            float scale = 1.0f + progress * 0.8f; // Grow more dramatically for removal
            
            Color renderColor = new Color(effectColor.r, effectColor.g, effectColor.b, alpha);
            shapeRenderer.setColor(renderColor);
            
            // Render expanding ring effect
            float outerRadius = FENCE_PIECE_SIZE * 0.4f * scale;
            float innerRadius = outerRadius * 0.7f;
            
            // Draw outer circle
            shapeRenderer.circle(x, y, outerRadius);
            
            // Draw inner circle with background color to create ring effect
            shapeRenderer.setColor(0, 0, 0, 0); // Transparent
            shapeRenderer.circle(x, y, innerRadius);
        }
    }
    
    /**
     * Visual effect for material count changes.
     */
    private static class MaterialCountChangeEffect extends VisualEffect {
        private final FenceMaterialType materialType;
        private final int countChange;
        private final Color effectColor;
        
        public MaterialCountChangeEffect(float x, float y, FenceMaterialType materialType, int countChange) {
            super(x, y, 1.0f); // 1 second duration
            this.materialType = materialType;
            this.countChange = countChange;
            
            // Choose color based on gain/loss
            if (countChange > 0) {
                effectColor = new Color(0.2f, 1.0f, 0.2f, 1.0f); // Green for gain
            } else {
                effectColor = new Color(1.0f, 0.2f, 0.2f, 1.0f); // Red for loss
            }
        }
        
        @Override
        public void render(ShapeRenderer shapeRenderer) {
            float progress = getProgress();
            float alpha = 1.0f - progress; // Fade out over time
            float yOffset = progress * 30f; // Move upward over time
            
            Color renderColor = new Color(effectColor.r, effectColor.g, effectColor.b, alpha);
            shapeRenderer.setColor(renderColor);
            
            // Render small indicator circle
            float size = 8f * (1.0f - progress * 0.5f); // Shrink slightly over time
            shapeRenderer.circle(x, y + yOffset, size);
            
            // Note: Text rendering would require SpriteBatch and BitmapFont
            // For now, we just show the colored circle
        }
    }
    
    /**
     * Visual effect for error feedback.
     */
    private static class ErrorFeedbackEffect extends VisualEffect {
        private final String errorMessage;
        private final Color effectColor;
        
        public ErrorFeedbackEffect(float x, float y, String errorMessage) {
            super(x, y, 0.8f); // 0.8 second duration
            this.errorMessage = errorMessage;
            this.effectColor = new Color(1.0f, 0.0f, 0.0f, 1.0f); // Red
        }
        
        @Override
        public void render(ShapeRenderer shapeRenderer) {
            float progress = getProgress();
            float alpha = 1.0f - progress; // Fade out over time
            float scale = 1.0f + progress * 0.3f; // Grow slightly over time
            
            Color renderColor = new Color(effectColor.r, effectColor.g, effectColor.b, alpha);
            shapeRenderer.setColor(renderColor);
            
            // Render pulsing X mark
            float size = FENCE_PIECE_SIZE * 0.2f * scale;
            float thickness = 3f;
            
            // Draw X shape with thick lines
            // Top-left to bottom-right
            shapeRenderer.rectLine(x - size, y + size, x + size, y - size, thickness);
            // Top-right to bottom-left  
            shapeRenderer.rectLine(x + size, y + size, x - size, y - size, thickness);
        }
    }
}