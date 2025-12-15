package wagemaker.uk.fence;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * UI components for fence building mode.
 * Handles grid overlay rendering, material count display, and building
 * instructions.
 */
public class FenceBuildingUI {

    /** Shape renderer for drawing grid overlay */
    private final ShapeRenderer shapeRenderer;

    /** Font for text rendering */
    private final BitmapFont font;

    /** Building manager reference for state access */
    private final FenceBuildingManager buildingManager;

    /** Grid overlay settings */
    private static final Color GRID_LINE_COLOR = new Color(1, 1, 1, 0.3f); // Semi-transparent white
    @SuppressWarnings("unused")
    private static final float GRID_LINE_WIDTH = 1.0f;
    private static final int GRID_SIZE = 64; // Size of each grid cell in pixels

    /** UI text colors */
    private static final Color INSTRUCTION_TEXT_COLOR = Color.WHITE;
    private static final Color MATERIAL_COUNT_COLOR = Color.YELLOW;
    private static final Color ERROR_TEXT_COLOR = Color.RED;

    /** UI positioning constants */
    private static final float INSTRUCTION_MARGIN = 20f;
    private static final float MATERIAL_COUNT_MARGIN = 20f;
    private static final float LINE_HEIGHT = 25f;

    /** Flag to control grid overlay visibility */
    private boolean gridOverlayVisible;

    /** Current error message to display */
    private String currentErrorMessage;

    /** Timer for error message display */
    private float errorMessageTimer;

    /** Duration to show error messages */
    private static final float ERROR_MESSAGE_DURATION = 3.0f;

    /**
     * Creates a new fence building UI.
     * 
     * @param shapeRenderer   Shape renderer for drawing
     * @param font            Font for text rendering
     * @param buildingManager Building manager for state access
     */
    public FenceBuildingUI(ShapeRenderer shapeRenderer, BitmapFont font, FenceBuildingManager buildingManager) {
        this.shapeRenderer = shapeRenderer;
        this.font = font;
        this.buildingManager = buildingManager;
        this.gridOverlayVisible = false;
        this.currentErrorMessage = null;
        this.errorMessageTimer = 0f;
    }

    /**
     * Updates the UI state and timers.
     * 
     * @param deltaTime Time elapsed since last frame
     */
    public void update(float deltaTime) {
        // Update grid overlay visibility based on building mode
        gridOverlayVisible = buildingManager.isBuildingModeActive();

        // Update error message timer
        if (currentErrorMessage != null) {
            errorMessageTimer -= deltaTime;
            if (errorMessageTimer <= 0) {
                currentErrorMessage = null;
            }
        }
    }

    /**
     * Renders the fence building UI components.
     * 
     * @param batch    Sprite batch for text rendering
     * @param camera   Camera for coordinate conversion
     * @param viewport Viewport for screen dimensions
     */
    public void render(SpriteBatch batch, OrthographicCamera camera, Viewport viewport) {
        if (!buildingManager.isBuildingModeActive()) {
            return; // Don't render UI when not in building mode
        }

        // Render grid overlay
        if (gridOverlayVisible) {
            renderGridOverlay(camera, viewport);
        }

        // Render text elements
        batch.begin();
        renderInstructions(batch, camera, viewport);
        renderMaterialCount(batch, camera, viewport);
        renderErrorMessage(batch, camera, viewport);
        batch.end();
    }

    /**
     * Renders the grid overlay showing valid placement positions.
     * 
     * @param camera   Camera for coordinate conversion
     * @param viewport Viewport for screen dimensions
     */
    private void renderGridOverlay(OrthographicCamera camera, Viewport viewport) {
        // Calculate visible grid area
        float camX = camera.position.x;
        float camY = camera.position.y;
        float viewWidth = viewport.getWorldWidth();
        float viewHeight = viewport.getWorldHeight();

        // Calculate grid bounds
        int startX = (int) ((camX - viewWidth / 2) / GRID_SIZE) * GRID_SIZE;
        int startY = (int) ((camY - viewHeight / 2) / GRID_SIZE) * GRID_SIZE;
        int endX = (int) ((camX + viewWidth / 2) / GRID_SIZE) * GRID_SIZE + GRID_SIZE;
        int endY = (int) ((camY + viewHeight / 2) / GRID_SIZE) * GRID_SIZE + GRID_SIZE;

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(GRID_LINE_COLOR);

        // Draw vertical grid lines
        for (int x = startX; x <= endX; x += GRID_SIZE) {
            shapeRenderer.line(x, startY, x, endY);
        }

        // Draw horizontal grid lines
        for (int y = startY; y <= endY; y += GRID_SIZE) {
            shapeRenderer.line(startX, y, endX, y);
        }

        shapeRenderer.end();
    }

    /**
     * Renders building instructions and controls.
     * 
     * @param batch    Sprite batch for text rendering
     * @param camera   Camera for positioning
     * @param viewport Viewport for screen dimensions
     */
    private void renderInstructions(SpriteBatch batch, OrthographicCamera camera, Viewport viewport) {
        float screenX = camera.position.x - viewport.getWorldWidth() / 2 + INSTRUCTION_MARGIN;
        float screenY = camera.position.y + viewport.getWorldHeight() / 2 - INSTRUCTION_MARGIN;

        font.setColor(INSTRUCTION_TEXT_COLOR);

        // Building mode instructions
        font.draw(batch, "FENCE BUILDING MODE", screenX, screenY);
        screenY -= LINE_HEIGHT;

        font.draw(batch, "Left Click: Place fence", screenX, screenY);
        screenY -= LINE_HEIGHT;

        font.draw(batch, "Right Click: Remove fence", screenX, screenY);
        screenY -= LINE_HEIGHT;

        String toggleKey = com.badlogic.gdx.Input.Keys.toString(buildingManager.getBuildingModeToggleKey());
        font.draw(batch, "Press " + toggleKey + " to exit", screenX, screenY);
    }

    /**
     * Renders material count display.
     * 
     * @param batch    Sprite batch for text rendering
     * @param camera   Camera for positioning
     * @param viewport Viewport for screen dimensions
     */
    private void renderMaterialCount(SpriteBatch batch, OrthographicCamera camera, Viewport viewport) {
        float screenX = camera.position.x + viewport.getWorldWidth() / 2 - 200; // Right side
        float screenY = camera.position.y + viewport.getWorldHeight() / 2 - MATERIAL_COUNT_MARGIN;

        font.setColor(MATERIAL_COUNT_COLOR);

        // Current material type
        FenceMaterialType currentMaterial = buildingManager.getSelectedMaterialType();
        font.draw(batch, "Material: " + currentMaterial.getDisplayName(), screenX, screenY);
        screenY -= LINE_HEIGHT;

        // Material counts
        FenceMaterialProvider materialProvider = buildingManager.getValidator().getMaterialProvider();
        if (materialProvider != null) {
            int woodCount = materialProvider.getMaterialCount(FenceMaterialType.WOOD);
            int bambooCount = materialProvider.getMaterialCount(FenceMaterialType.BAMBOO);

            font.draw(batch, "Wood: " + woodCount, screenX, screenY);
            screenY -= LINE_HEIGHT;

            font.draw(batch, "Bamboo: " + bambooCount, screenX, screenY);
        }
    }

    /**
     * Renders error messages if any are active.
     * 
     * @param batch    Sprite batch for text rendering
     * @param camera   Camera for positioning
     * @param viewport Viewport for screen dimensions
     */
    private void renderErrorMessage(SpriteBatch batch, OrthographicCamera camera, Viewport viewport) {
        if (currentErrorMessage == null) {
            return;
        }

        float screenX = camera.position.x - viewport.getWorldWidth() / 2 + INSTRUCTION_MARGIN;
        float screenY = camera.position.y - viewport.getWorldHeight() / 2 + 100; // Bottom area

        font.setColor(ERROR_TEXT_COLOR);
        font.draw(batch, "Error: " + currentErrorMessage, screenX, screenY);
    }

    /**
     * Shows an error message for a limited time.
     * 
     * @param message Error message to display
     */
    public void showErrorMessage(String message) {
        this.currentErrorMessage = message;
        this.errorMessageTimer = ERROR_MESSAGE_DURATION;
    }

    /**
     * Clears any currently displayed error message.
     */
    public void clearErrorMessage() {
        this.currentErrorMessage = null;
        this.errorMessageTimer = 0f;
    }

    /**
     * Checks if the grid overlay is currently visible.
     * 
     * @return true if grid overlay is visible
     */
    public boolean isGridOverlayVisible() {
        return gridOverlayVisible;
    }

    /**
     * Sets the grid overlay visibility.
     * 
     * @param visible true to show grid overlay, false to hide
     */
    public void setGridOverlayVisible(boolean visible) {
        this.gridOverlayVisible = visible;
    }

    /**
     * Gets the current error message.
     * 
     * @return Current error message, or null if none
     */
    public String getCurrentErrorMessage() {
        return currentErrorMessage;
    }

    /**
     * Checks if an error message is currently being displayed.
     * 
     * @return true if error message is active
     */
    public boolean hasErrorMessage() {
        return currentErrorMessage != null;
    }

    /**
     * Disposes of resources used by this UI.
     */
    public void dispose() {
        // Font is typically shared, so we don't dispose it here
        // Shape renderer is typically shared, so we don't dispose it here
    }
}