package wagemaker.uk.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import wagemaker.uk.fence.FencePieceType;
import wagemaker.uk.fence.FenceMaterialType;
import wagemaker.uk.fence.FenceBuildingManager;
import wagemaker.uk.localization.LocalizationManager;

/**
 * Renders the fence item selection UI panel above the inventory.
 * Displays all 8 fence piece types and allows selection using B key and arrow keys.
 * Only visible when fence building mode is active.
 */
public class FenceItemRenderer {
    
    // Fence piece icons (extracted from fence texture atlas)
    private Texture[] fencePieceIcons;
    
    // Background and UI elements
    private Texture woodenBackground;
    private Texture slotBorder;
    
    // Font for rendering labels
    private BitmapFont labelFont;
    
    // ShapeRenderer for selection highlight
    private ShapeRenderer shapeRenderer;
    
    // Layout constants
    private static final int SLOT_SIZE = 40;
    private static final int SLOT_SPACING = 8;
    private static final int PANEL_PADDING = 8;
    private static final int FENCE_PIECES_COUNT = 8;
    private static final int PANEL_WIDTH = (SLOT_SIZE * FENCE_PIECES_COUNT) + (SLOT_SPACING * (FENCE_PIECES_COUNT - 1)) + (PANEL_PADDING * 2);
    private static final int PANEL_HEIGHT = SLOT_SIZE + (PANEL_PADDING * 2) + 20; // Extra height for label
    private static final int ICON_SIZE = 32;
    
    // Selection highlight constants
    private static final float HIGHLIGHT_R = 0.0f;
    private static final float HIGHLIGHT_G = 0.8f;
    private static final float HIGHLIGHT_B = 1.0f;
    private static final float HIGHLIGHT_ALPHA = 0.8f;
    private static final int HIGHLIGHT_BORDER_WIDTH = 3;
    
    // Selection state
    private int selectedFencePieceIndex = 0; // Currently selected fence piece (0-7)
    private boolean fenceSelectionActive = false; // Whether fence selection mode is active
    
    // Input handling
    private boolean leftArrowPressed = false;
    private boolean rightArrowPressed = false;
    private boolean bKeyPressed = false;
    
    // Reference to fence building manager
    private FenceBuildingManager fenceBuildingManager;
    
    /**
     * Create a new FenceItemRenderer and load all required assets.
     */
    public FenceItemRenderer() {
        loadFencePieceIcons();
        createWoodenBackground();
        createSlotBorder();
        initializeFont();
        shapeRenderer = new ShapeRenderer();
    }
    
    /**
     * Set the fence building manager reference for state queries.
     * @param manager The fence building manager
     */
    public void setFenceBuildingManager(FenceBuildingManager manager) {
        this.fenceBuildingManager = manager;
    }
    
    /**
     * Load fence piece icon textures from the fence texture atlas.
     */
    private void loadFencePieceIcons() {
        fencePieceIcons = new Texture[FENCE_PIECES_COUNT];
        FencePieceType[] pieceTypes = FencePieceType.values();
        
        System.out.println("Loading " + FENCE_PIECES_COUNT + " fence piece icons...");
        
        for (int i = 0; i < FENCE_PIECES_COUNT; i++) {
            FencePieceType pieceType = pieceTypes[i];
            System.out.println("Loading fence piece " + i + ": " + pieceType.getDescription() + 
                             " at (" + pieceType.getTextureX() + ", " + pieceType.getTextureY() + ")");
            
            fencePieceIcons[i] = extractIconFromFenceSheet(
                pieceType.getTextureX(), 
                pieceType.getTextureY(), 
                64, 64
            );
            
            if (fencePieceIcons[i] == null) {
                System.err.println("Failed to load fence piece " + i + ", creating fallback");
                fencePieceIcons[i] = createFallbackTexture(64, 64, i);
            }
        }
        
        System.out.println("Fence piece icon loading complete");
    }
    
    /**
     * Extract an icon from the fence texture sheet at the specified coordinates.
     */
    private Texture extractIconFromFenceSheet(int srcX, int srcY, int width, int height) {
        try {
            Texture fenceSheet = new Texture("textures/fense.png");
            
            // Validate texture coordinates
            if (srcX < 0 || srcY < 0 || srcX + width > fenceSheet.getWidth() || srcY + height > fenceSheet.getHeight()) {
                System.err.println("Invalid texture coordinates: (" + srcX + ", " + srcY + ") size: " + width + "x" + height + 
                                 " for texture size: " + fenceSheet.getWidth() + "x" + fenceSheet.getHeight());
                fenceSheet.dispose();
                return createFallbackTexture(width, height);
            }
            
            fenceSheet.getTextureData().prepare();
            Pixmap sheetPixmap = fenceSheet.getTextureData().consumePixmap();
            
            // Create pixmap for the icon
            Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
            pixmap.drawPixmap(sheetPixmap, 0, 0, srcX, srcY, width, height);
            
            Texture icon = new Texture(pixmap);
            pixmap.dispose();
            sheetPixmap.dispose();
            fenceSheet.dispose();
            
            System.out.println("Successfully loaded fence icon at (" + srcX + ", " + srcY + ")");
            return icon;
        } catch (Exception e) {
            // Fallback: create a colored rectangle if texture loading fails
            System.err.println("Failed to load fence texture at (" + srcX + ", " + srcY + "), using fallback: " + e.getMessage());
            return createFallbackTexture(width, height);
        }
    }
    
    /**
     * Create a fallback texture when fence textures can't be loaded.
     */
    private Texture createFallbackTexture(int width, int height) {
        return createFallbackTexture(width, height, 0);
    }
    
    /**
     * Create a fallback texture with a specific pattern for each fence piece type.
     */
    private Texture createFallbackTexture(int width, int height, int pieceIndex) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        
        // Different colors for different piece types to make them distinguishable
        Color[] colors = {
            new Color(0.8f, 0.4f, 0.2f, 1.0f), // Orange-brown for piece 0
            new Color(0.6f, 0.4f, 0.2f, 1.0f), // Brown for piece 1
            new Color(0.7f, 0.5f, 0.3f, 1.0f), // Light brown for piece 2
            new Color(0.5f, 0.3f, 0.1f, 1.0f), // Dark brown for piece 3
            new Color(0.9f, 0.6f, 0.4f, 1.0f), // Light orange for piece 4
            new Color(0.4f, 0.2f, 0.1f, 1.0f), // Very dark brown for piece 5
            new Color(0.8f, 0.5f, 0.2f, 1.0f), // Golden brown for piece 6
            new Color(0.6f, 0.3f, 0.2f, 1.0f)  // Reddish brown for piece 7
        };
        
        Color baseColor = colors[pieceIndex % colors.length];
        pixmap.setColor(baseColor);
        pixmap.fill();
        
        // Draw border
        pixmap.setColor(Color.BLACK);
        pixmap.drawRectangle(0, 0, width, height);
        
        // Draw piece number in the center
        pixmap.setColor(Color.WHITE);
        int centerX = width / 2;
        int centerY = height / 2;
        // Draw a simple pattern to represent the piece number
        for (int i = 0; i <= pieceIndex; i++) {
            int dotX = centerX - 10 + (i * 4);
            int dotY = centerY;
            if (dotX >= 2 && dotX < width - 2 && dotY >= 2 && dotY < height - 2) {
                pixmap.fillCircle(dotX, dotY, 2);
            }
        }
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
    
    /**
     * Create a wooden plank background texture procedurally.
     */
    private void createWoodenBackground() {
        Pixmap pixmap = new Pixmap(PANEL_WIDTH, PANEL_HEIGHT, Pixmap.Format.RGBA8888);
        
        // Fill with brown wood color (slightly different from inventory)
        pixmap.setColor(new Color(0.5f, 0.3f, 0.15f, 0.9f));
        pixmap.fill();
        
        // Add darker border for depth
        pixmap.setColor(new Color(0.3f, 0.15f, 0.05f, 0.9f));
        pixmap.drawRectangle(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        pixmap.drawRectangle(1, 1, PANEL_WIDTH - 2, PANEL_HEIGHT - 2);
        
        woodenBackground = new Texture(pixmap);
        pixmap.dispose();
    }
    
    /**
     * Create a slot border texture for visual separation.
     */
    private void createSlotBorder() {
        Pixmap pixmap = new Pixmap(SLOT_SIZE, SLOT_SIZE, Pixmap.Format.RGBA8888);
        
        // Draw dark brown border
        pixmap.setColor(new Color(0.25f, 0.1f, 0.0f, 0.8f));
        pixmap.drawRectangle(0, 0, SLOT_SIZE, SLOT_SIZE);
        
        slotBorder = new Texture(pixmap);
        pixmap.dispose();
    }
    
    /**
     * Initialize the font for rendering labels using StackSansText for better readability.
     * Creates a separate font instance to avoid affecting other UI components.
     */
    private void initializeFont() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/StackSansText-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
            
            parameter.size = 14; // Slightly smaller for better fit
            parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + 
                                  "ąćęłńóśźżĄĆĘŁŃÓŚŹŻ" +  // Polish
                                  "ãõâêôáéíóúàçÃÕÂÊÔÁÉÍÓÚÀÇ" +  // Portuguese
                                  "äöüßÄÖÜ";  // German
            parameter.color = Color.WHITE;
            parameter.borderWidth = 1; // Same border as main menu for smooth rendering
            parameter.borderColor = Color.BLACK;
            parameter.shadowOffsetX = 1; // Same shadow as main menu
            parameter.shadowOffsetY = 1;
            parameter.shadowColor = Color.BLACK;
            
            labelFont = generator.generateFont(parameter);
            generator.dispose();
        } catch (Exception e) {
            System.err.println("Failed to load StackSansText font, using default: " + e.getMessage());
            // Fallback to default font
            labelFont = new BitmapFont();
            labelFont.getData().setScale(0.8f);
        }
    }
    
    /**
     * Update fence selection state based on fence building mode.
     * Input handling is now done by the Player class to prevent conflicts.
     */
    public void update() {
        // Automatically activate fence selection when fence building mode is active
        if (fenceBuildingManager != null && fenceBuildingManager.isBuildingModeActive()) {
            if (!fenceSelectionActive) {
                // Immediately activate fence selection when building mode becomes active
                fenceSelectionActive = true;
                // Set first fence piece as selected by default
                selectedFencePieceIndex = 0;
                System.out.println("[FenceItemRenderer] Fence selection activated - first piece selected");
            }
            
            // Validate selection state to ensure consistency
            validateSelectionState();
        } else {
            // Deactivate fence selection when building mode is not active
            if (fenceSelectionActive) {
                fenceSelectionActive = false;
                System.out.println("[FenceItemRenderer] Fence selection deactivated");
            }
        }
    }
    
    /**
     * Render the fence item selection UI panel.
     * Only renders when fence building mode is active.
     * 
     * @param batch The SpriteBatch to use for rendering
     * @param inventory The inventory to get fence material counts from
     * @param camX Camera X position
     * @param camY Camera Y position
     * @param viewWidth Viewport width
     * @param viewHeight Viewport height
     * @param inventoryPanelWidth Width of the inventory panel (to align fence panel with inventory)
     */
    public void render(SpriteBatch batch, wagemaker.uk.inventory.Inventory inventory, float camX, float camY, 
                      float viewWidth, float viewHeight, float inventoryPanelWidth) {
        
        // Only render when fence building mode is active
        if (fenceBuildingManager == null || !fenceBuildingManager.isBuildingModeActive() || inventory == null) {
            return;
        }
        
        // Calculate position above the inventory panel (same X position as inventory)
        float panelX = camX + viewWidth / 2 - inventoryPanelWidth - 20; // Same X as inventory
        float inventoryHeight = wagemaker.uk.ui.InventoryRenderer.getPanelHeight();
        float panelY = camY - viewHeight / 2 + 20 + inventoryHeight + 10; // 10px gap above inventory
        
        batch.begin();
        
        // Draw wooden background
        batch.draw(woodenBackground, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);
        
        // Draw title label with actual fence material count
        String titleText = LocalizationManager.getInstance().getText("fence_inventory.title");
        int totalMaterialCount = inventory.getWoodFenceMaterialCount() + inventory.getBambooFenceMaterialCount();
        String titleWithCount = titleText + " (" + totalMaterialCount + ")";
        
        // Calculate title position with more space from top edge
        float titleX = panelX + PANEL_PADDING;
        float titleY = panelY + PANEL_HEIGHT - 8; // Moved higher by 2 pixels
        
        // Draw title text (font already has built-in border and shadow for smooth rendering)
        labelFont.setColor(1.0f, 1.0f, 1.0f, 1.0f); // Bright white
        labelFont.draw(batch, titleWithCount, titleX, titleY);
        

        
        // Draw slot borders and fence piece icons
        float slotX = panelX + PANEL_PADDING;
        float slotY = panelY + PANEL_PADDING;
        
        for (int i = 0; i < FENCE_PIECES_COUNT; i++) {
            float x = slotX + i * (SLOT_SIZE + SLOT_SPACING);
            
            // Draw slot border
            batch.draw(slotBorder, x, slotY, SLOT_SIZE, SLOT_SIZE);
            
            // Draw fence piece icon
            if (fencePieceIcons[i] != null) {
                float iconX = x + (SLOT_SIZE - ICON_SIZE) / 2;
                float iconY = slotY + (SLOT_SIZE - ICON_SIZE) / 2;
                
                // Slightly brighten the selected piece icon
                if (fenceSelectionActive && i == selectedFencePieceIndex) {
                    batch.setColor(1.2f, 1.2f, 1.2f, 1.0f); // Brighter
                } else {
                    batch.setColor(1.0f, 1.0f, 1.0f, 1.0f); // Normal
                }
                
                batch.draw(fencePieceIcons[i], iconX, iconY, ICON_SIZE, ICON_SIZE);
                batch.setColor(1.0f, 1.0f, 1.0f, 1.0f); // Reset color
            } else {
                // Draw a red X if the icon failed to load
                batch.setColor(Color.RED);
                float iconX = x + (SLOT_SIZE - ICON_SIZE) / 2;
                float iconY = slotY + (SLOT_SIZE - ICON_SIZE) / 2;
                // Draw simple red rectangle as error indicator
                batch.draw(slotBorder, iconX, iconY, ICON_SIZE, ICON_SIZE);
                batch.setColor(1.0f, 1.0f, 1.0f, 1.0f); // Reset color
                System.err.println("Missing fence piece icon for index " + i);
            }
            
            // Draw selection number indicator
            if (fenceSelectionActive) {
                String numberText = String.valueOf(i + 1);
                labelFont.setColor(i == selectedFencePieceIndex ? Color.YELLOW : Color.GRAY);
                labelFont.getData().setScale(0.5f); // Small text
                float numberX = x + SLOT_SIZE - 8;
                float numberY = slotY + SLOT_SIZE - 2;
                labelFont.draw(batch, numberText, numberX, numberY);
                labelFont.getData().setScale(1.0f); // Properly reset scale to 1.0f
            }
        }
        
        batch.end();
        
        // Draw selection highlight if fence selection is active (border only, like inventory)
        if (fenceSelectionActive) {
            float selectedX = slotX + selectedFencePieceIndex * (SLOT_SIZE + SLOT_SPACING);
            
            // Get the current projection matrix from the batch
            com.badlogic.gdx.math.Matrix4 projectionMatrix = batch.getProjectionMatrix().cpy();
            
            // Set ShapeRenderer to use the same projection matrix as the batch
            shapeRenderer.setProjectionMatrix(projectionMatrix);
            
            // Draw border highlight only (no filled background) to match inventory style
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(HIGHLIGHT_R, HIGHLIGHT_G, HIGHLIGHT_B, HIGHLIGHT_ALPHA);
            Gdx.gl.glLineWidth(HIGHLIGHT_BORDER_WIDTH);
            
            // Draw highlight border around selected fence piece
            shapeRenderer.rect(selectedX - 2, slotY - 2, SLOT_SIZE + 4, SLOT_SIZE + 4);
            
            shapeRenderer.end();
            Gdx.gl.glLineWidth(1); // Reset line width
        }
    }
    
    /**
     * Get the currently selected fence piece type.
     * @return The selected fence piece type
     */
    public FencePieceType getSelectedFencePieceType() {
        return FencePieceType.values()[selectedFencePieceIndex];
    }
    
    /**
     * Check if fence selection mode is currently active.
     * @return true if fence selection is active, false otherwise
     */
    public boolean isFenceSelectionActive() {
        return fenceSelectionActive;
    }
    
    /**
     * Set the fence selection mode state.
     * @param active Whether fence selection should be active
     */
    public void setFenceSelectionActive(boolean active) {
        this.fenceSelectionActive = active;
    }
    
    /**
     * Get the currently selected fence piece index.
     * @return The selected index (0-7)
     */
    public int getSelectedFencePieceIndex() {
        return selectedFencePieceIndex;
    }
    
    /**
     * Set the selected fence piece index.
     * @param index The index to select (0-7)
     */
    public void setSelectedFencePieceIndex(int index) {
        if (index >= 0 && index < FENCE_PIECES_COUNT) {
            int previousIndex = this.selectedFencePieceIndex;
            this.selectedFencePieceIndex = index;
            
            // Provide visual feedback for selection change
            if (previousIndex != index) {
                System.out.println("[FenceItemRenderer] Selection changed from " + 
                                 FencePieceType.values()[previousIndex].getDescription() + 
                                 " to " + FencePieceType.values()[index].getDescription());
            }
        }
    }
    
    /**
     * Get the panel height for positioning calculations.
     * @return The height of the fence selection panel
     */
    public static int getPanelHeight() {
        return PANEL_HEIGHT;
    }
    
    /**
     * Get the panel width for positioning calculations.
     * @return The width of the fence selection panel
     */
    public static int getPanelWidth() {
        return PANEL_WIDTH;
    }
    
    /**
     * Check if the fence selection panel should be visible.
     * @return true if the panel should be rendered
     */
    public boolean shouldRender() {
        return fenceBuildingManager != null && fenceBuildingManager.isBuildingModeActive();
    }
    
    /**
     * Ensure selection state is properly maintained.
     * Called to validate and correct selection state if needed.
     */
    public void validateSelectionState() {
        // Ensure selection index is within valid range
        if (selectedFencePieceIndex < 0 || selectedFencePieceIndex >= FENCE_PIECES_COUNT) {
            selectedFencePieceIndex = 0; // Reset to first piece if invalid
            System.out.println("[FenceItemRenderer] Selection index corrected to 0");
        }
        
        // Ensure fence selection is active when building mode is active
        if (fenceBuildingManager != null && fenceBuildingManager.isBuildingModeActive()) {
            if (!fenceSelectionActive) {
                fenceSelectionActive = true;
                System.out.println("[FenceItemRenderer] Fence selection activated during validation");
            }
        }
    }
    
    /**
     * Dispose of all textures and resources.
     */
    public void dispose() {
        if (fencePieceIcons != null) {
            for (Texture icon : fencePieceIcons) {
                if (icon != null) {
                    icon.dispose();
                }
            }
        }
        if (woodenBackground != null) woodenBackground.dispose();
        if (slotBorder != null) slotBorder.dispose();
        if (labelFont != null) labelFont.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
    }
}