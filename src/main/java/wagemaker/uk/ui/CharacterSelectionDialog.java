package wagemaker.uk.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import wagemaker.uk.client.PlayerConfig;
import wagemaker.uk.localization.LanguageChangeListener;
import wagemaker.uk.localization.LocalizationManager;

/**
 * CharacterSelectionDialog allows players to select their preferred character sprite.
 * Displays a 2x2 grid of character options with keyboard navigation.
 */
public class CharacterSelectionDialog implements LanguageChangeListener {
    private boolean isOpen = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private CharacterOption[] characterOptions;
    private int selectedColumn = 0;  // 0-1 range
    private int selectedRow = 0;     // 0-3 range (4 rows)
    private String initialCharacter = null;  // Track initial character when dialog opens
    
    private static final float DIALOG_WIDTH = 550;
    private static final float DIALOG_HEIGHT = 950;
    
    /**
     * Inner class representing a character option.
     */
    private static class CharacterOption {
        String displayName;
        String spriteFilename;
        Texture previewTexture;
        TextureRegion idleFrame;
        
        CharacterOption(String displayName, String spriteFilename) {
            this.displayName = displayName;
            this.spriteFilename = spriteFilename;
        }
        
        void dispose() {
            if (previewTexture != null) {
                previewTexture.dispose();
            }
        }
    }
    
    /**
     * Creates a new CharacterSelectionDialog with wooden plank background and custom font.
     */
    public CharacterSelectionDialog() {
        woodenPlank = createWoodenPlank();
        createDialogFont();
        initializeCharacterOptions();
        
        // Register as language change listener
        LocalizationManager.getInstance().addLanguageChangeListener(this);
    }
    
    /**
     * Initializes the character options array and loads sprite sheets.
     */
    private void initializeCharacterOptions() {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        characterOptions = new CharacterOption[8];
        characterOptions[0] = new CharacterOption(
            loc.getText("character_selection_dialog.girl_red"),
            "girl_red_start.png"
        );
        characterOptions[1] = new CharacterOption(
            loc.getText("character_selection_dialog.girl_navy"),
            "girl_navy_start.png"
        );
        characterOptions[2] = new CharacterOption(
            loc.getText("character_selection_dialog.girl_green"),
            "girl_green_start.png"
        );
        characterOptions[3] = new CharacterOption(
            loc.getText("character_selection_dialog.girl_walnut"),
            "girl_walnut_start.png"
        );
        characterOptions[4] = new CharacterOption(
            loc.getText("character_selection_dialog.boy_red"),
            "boy_red_start.png"
        );
        characterOptions[5] = new CharacterOption(
            loc.getText("character_selection_dialog.boy_navy"),
            "boy_navy_start.png"
        );
        characterOptions[6] = new CharacterOption(
            loc.getText("character_selection_dialog.boy_green"),
            "boy_green_start.png"
        );
        characterOptions[7] = new CharacterOption(
            loc.getText("character_selection_dialog.boy_walnut"),
            "boy_walnut_start.png"
        );
        
        // Load sprite sheets and extract idle frames
        for (CharacterOption option : characterOptions) {
            loadCharacterPreview(option);
        }
    }
    
    /**
     * Loads the sprite sheet for a character and extracts the idle frame.
     * The idle frame is at position (0, 2048) in image editor coordinates (top-left origin).
     * LibGDX uses bottom-left origin, so we need to convert the Y coordinate.
     * 
     * @param option The character option to load preview for
     */
    private void loadCharacterPreview(CharacterOption option) {
        try {
            String path = "sprites/player/" + option.spriteFilename;
            option.previewTexture = new Texture(Gdx.files.internal(path));
            
            // Extract idle frame at position (0, 2048) from top-left
            // Convert to LibGDX bottom-left coordinates
            int spriteSheetHeight = option.previewTexture.getHeight();
            int frameX = 0;
            int frameYFromTop = 1664;
            int frameWidth = 64;
            int frameHeight = 64;
            
            // Convert Y coordinate from top-left to bottom-left origin
            int frameYFromBottom = spriteSheetHeight - frameYFromTop - frameHeight;
            
            option.idleFrame = new TextureRegion(
                option.previewTexture,
                frameX,
                frameYFromBottom,
                frameWidth,
                frameHeight
            );
            
            System.out.println("Loaded character preview: " + option.spriteFilename + 
                             " (sheet height: " + spriteSheetHeight + 
                             ", frame Y from bottom: " + frameYFromBottom + ")");
            
        } catch (Exception e) {
            System.err.println("Error loading character preview for " + option.spriteFilename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates the custom font for dialog text using FontManager.
     */
    private void createDialogFont() {
        dialogFont = FontManager.getInstance().getCurrentFont();
    }
    
    /**
     * Creates a wooden plank texture for the dialog background.
     * 
     * @return The wooden plank texture
     */
    private Texture createWoodenPlank() {
        Pixmap pixmap = new Pixmap((int)DIALOG_WIDTH, (int)DIALOG_HEIGHT, Pixmap.Format.RGBA8888);
        
        // Base wood color
        pixmap.setColor(0.4f, 0.25f, 0.1f, 1.0f);
        pixmap.fill();
        
        // Wood grain lines
        pixmap.setColor(0.3f, 0.18f, 0.08f, 1.0f);
        for (int y = 10; y < DIALOG_HEIGHT; y += 15) {
            pixmap.drawLine(0, y, (int)DIALOG_WIDTH, y + 5);
        }
        
        // Border
        pixmap.setColor(0.2f, 0.12f, 0.05f, 1.0f);
        pixmap.drawRectangle(0, 0, (int)DIALOG_WIDTH, (int)DIALOG_HEIGHT);
        pixmap.drawRectangle(2, 2, (int)DIALOG_WIDTH - 4, (int)DIALOG_HEIGHT - 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
    
    /**
     * Opens the character selection dialog.
     */
    public void open() {
        this.isOpen = true;
        // Reset selection to top-left
        this.selectedColumn = 0;
        this.selectedRow = 0;
        
        // Store the current character selection to detect changes
        PlayerConfig config = PlayerConfig.load();
        this.initialCharacter = config.getSelectedCharacter();
        if (this.initialCharacter == null || this.initialCharacter.isEmpty()) {
            this.initialCharacter = "boy_navy_start.png";  // Default
        }
    }
    
    /**
     * Closes the character selection dialog.
     */
    public void close() {
        this.isOpen = false;
    }
    
    /**
     * Checks if the dialog is currently open.
     * 
     * @return true if the dialog is open, false otherwise
     */
    public boolean isOpen() {
        return isOpen;
    }
    
    /**
     * Updates the dialog state based on keyboard input.
     * Handles arrow keys for navigation, ENTER to confirm, ESC to cancel.
     */
    public void update() {
        if (!isOpen) {
            return;
        }
        
        // Handle arrow key navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            navigateUp();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            navigateDown();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            navigateLeft();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            navigateRight();
        }
        
        // Handle ENTER key - confirm selection
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            confirmSelection();
        }
        
        // Handle ESC key - cancel
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            close();
        }
    }
    
    /**
     * Confirms the current character selection.
     * Saves immediately to PlayerConfig and reloads the player sprite.
     */
    private void confirmSelection() {
        int selectedIndex = getSelectedCharacterIndex();
        if (selectedIndex >= 0 && selectedIndex < characterOptions.length) {
            CharacterOption selected = characterOptions[selectedIndex];
            
            // Apply the character selection immediately (saves to config and reloads sprite)
            wagemaker.uk.ui.GameMenu.applyCharacterSelectionImmediately(selected.spriteFilename);
            
            System.out.println("Character selected and applied immediately: " + selected.spriteFilename);
            
            // Close dialog
            close();
        }
    }
    
    /**
     * Navigates up in the grid (wraps to bottom if at top).
     */
    public void navigateUp() {
        selectedRow--;
        if (selectedRow < 0) {
            selectedRow = 3;  // Wrap to bottom (4 rows: 0-3)
        }
    }
    
    /**
     * Navigates down in the grid (wraps to top if at bottom).
     */
    public void navigateDown() {
        selectedRow++;
        if (selectedRow > 3) {
            selectedRow = 0;  // Wrap to top (4 rows: 0-3)
        }
    }
    
    /**
     * Navigates left in the grid (wraps to right if at left edge).
     */
    public void navigateLeft() {
        selectedColumn--;
        if (selectedColumn < 0) {
            selectedColumn = 1;  // Wrap to right
        }
    }
    
    /**
     * Navigates right in the grid (wraps to left if at right edge).
     */
    public void navigateRight() {
        selectedColumn++;
        if (selectedColumn > 1) {
            selectedColumn = 0;  // Wrap to left
        }
    }
    
    /**
     * Gets the currently selected character index based on grid position.
     * Grid layout: [0,0]=0, [1,0]=1, [0,1]=2, [1,1]=3, [0,2]=4, [1,2]=5, [0,3]=6, [1,3]=7
     * 
     * @return The index of the selected character (0-7)
     */
    public int getSelectedCharacterIndex() {
        return selectedRow * 2 + selectedColumn;
    }
    
    /**
     * Gets the currently selected column.
     * 
     * @return The selected column (0-1)
     */
    public int getSelectedColumn() {
        return selectedColumn;
    }
    
    /**
     * Gets the currently selected row.
     * 
     * @return The selected row (0-3)
     */
    public int getSelectedRow() {
        return selectedRow;
    }
    
    /**
     * Renders the character selection dialog.
     * 
     * @param batch The sprite batch for rendering
     * @param shapeRenderer The shape renderer for drawing borders
     * @param camX Camera X position (center)
     * @param camY Camera Y position (center)
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY) {
        if (!isOpen) {
            return;
        }
        
        // Calculate dialog position (centered on camera)
        float dialogX = camX - DIALOG_WIDTH / 2;
        float dialogY = camY - DIALOG_HEIGHT / 2;
        
        // Draw wooden plank background
        batch.begin();
        batch.draw(woodenPlank, dialogX, dialogY, DIALOG_WIDTH, DIALOG_HEIGHT);
        batch.end();
        
        // Get localized strings
        LocalizationManager loc = LocalizationManager.getInstance();
        String title = loc.getText("character_selection_dialog.title");
        String navInstruction = loc.getText("character_selection_dialog.navigation_instruction");
        String confirmInstruction = loc.getText("character_selection_dialog.confirm_instruction");
        String cancelInstruction = loc.getText("character_selection_dialog.cancel_instruction");
        
        // Draw title
        batch.begin();
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, title, dialogX + 20, dialogY + DIALOG_HEIGHT - 30);
        batch.end();
        
        // Draw 2x4 grid of character cells (2 columns, 4 rows)
        float cellSize = 128;
        float gap = 40;  // Uniform gap between all cells (horizontal and vertical)
        float gridWidth = 2 * cellSize + gap;  // 296 pixels
        float gridHeight = 4 * cellSize + 3 * gap; // Height with vertical gaps
        
        // Center grid horizontally and drop it 128px lower than center
        float gridStartX = dialogX + (DIALOG_WIDTH - gridWidth) / 2;
        float gridCenterY = dialogY + DIALOG_HEIGHT / 2 - 128;  // Center of dialog, dropped 128px
        float gridStartY = gridCenterY + gridHeight / 2;  // Top of grid (LibGDX Y grows upward)
        
        // Draw grid cells and character previews
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 2; col++) {
                float cellX = gridStartX + col * (cellSize + gap);
                float cellY = gridStartY - row * (cellSize + gap);
                
                // Draw cell border (white or yellow if selected)
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                if (col == selectedColumn && row == selectedRow) {
                    shapeRenderer.setColor(Color.YELLOW);
                } else {
                    shapeRenderer.setColor(Color.WHITE);
                }
                shapeRenderer.rect(cellX, cellY, cellSize, cellSize);
                shapeRenderer.rect(cellX + 1, cellY + 1, cellSize - 2, cellSize - 2);  // 2-pixel border
                shapeRenderer.end();
                
                // Draw character preview (96x96 centered in 128x128 cell)
                int characterIndex = row * 2 + col;
                if (characterIndex < characterOptions.length) {
                    CharacterOption option = characterOptions[characterIndex];
                    
                    // Draw character name above the cell
                    batch.begin();
                    dialogFont.setColor(Color.WHITE);
                    // Center the text above the cell
                    com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout(dialogFont, option.displayName);
                    float textX = cellX + (cellSize - layout.width) / 2;
                    float textY = cellY + cellSize + 20;  // 20 pixels above the cell
                    dialogFont.draw(batch, option.displayName, textX, textY);
                    batch.end();
                    
                    if (option.idleFrame != null) {
                        float previewSize = 96;
                        float previewX = cellX + (cellSize - previewSize) / 2;
                        float previewY = cellY + (cellSize - previewSize) / 2;
                        
                        batch.begin();
                        batch.draw(option.idleFrame, previewX, previewY, previewSize, previewSize);
                        batch.end();
                    }
                }
            }
        }
        
        // Draw instructions at bottom
        batch.begin();
        dialogFont.setColor(Color.LIGHT_GRAY);
        float instructionY = dialogY + 100;
        dialogFont.draw(batch, navInstruction, dialogX + 50, instructionY);
        dialogFont.draw(batch, confirmInstruction, dialogX + 50, instructionY - 25);
        dialogFont.draw(batch, cancelInstruction, dialogX + 50, instructionY - 50);
        batch.end();
    }
    
    /**
     * Called when the application language changes.
     * Refreshes the dialog text to display in the new language.
     * 
     * @param newLanguage The new language code
     */
    @Override
    public void onLanguageChanged(String newLanguage) {
        System.out.println("CharacterSelectionDialog: Language changed to " + newLanguage);
        // Refresh character display names
        if (characterOptions != null) {
            LocalizationManager loc = LocalizationManager.getInstance();
            characterOptions[0].displayName = loc.getText("character_selection_dialog.girl_red");
            characterOptions[1].displayName = loc.getText("character_selection_dialog.girl_navy");
            characterOptions[2].displayName = loc.getText("character_selection_dialog.girl_green");
            characterOptions[3].displayName = loc.getText("character_selection_dialog.girl_walnut");
            characterOptions[4].displayName = loc.getText("character_selection_dialog.boy_red");
            characterOptions[5].displayName = loc.getText("character_selection_dialog.boy_navy");
            characterOptions[6].displayName = loc.getText("character_selection_dialog.boy_green");
            characterOptions[7].displayName = loc.getText("character_selection_dialog.boy_walnut");
        }
    }
    
    /**
     * Disposes of resources used by the dialog.
     */
    public void dispose() {
        if (woodenPlank != null) {
            woodenPlank.dispose();
        }
        
        // Dispose character option textures
        if (characterOptions != null) {
            for (CharacterOption option : characterOptions) {
                if (option != null) {
                    option.dispose();
                }
            }
        }
        
        // Don't dispose dialogFont - it's managed by FontManager
        
        // Unregister from language change listener
        LocalizationManager.getInstance().removeLanguageChangeListener(this);
    }
}
