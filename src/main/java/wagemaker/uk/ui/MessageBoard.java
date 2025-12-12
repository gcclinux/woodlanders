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
import wagemaker.uk.localization.LanguageChangeListener;
import wagemaker.uk.localization.LocalizationManager;

/**
 * MessageBoard displays important announcements and information to players.
 * Shows welcome messages, feature updates, and links to community discussions.
 * Supports multi-language localization and follows the game's wooden plank dialog style.
 * Uses FontManager to respect user's font preference.
 */
public class MessageBoard implements LanguageChangeListener, FontChangeListener {
    private boolean isVisible = false;
    private Texture woodenPlank;
    private BitmapFont dialogFont;
    private static final float DIALOG_WIDTH = 840;
    private static final float DIALOG_HEIGHT = 480;
    
    /**
     * Creates a new MessageBoard with wooden plank background and font from FontManager.
     */
    public MessageBoard() {
        woodenPlank = createWoodenPlank();
        updateDialogFont();
        
        // Register as language change listener
        LocalizationManager.getInstance().addLanguageChangeListener(this);
        
        // Register as font change listener
        FontManager.getInstance().addFontChangeListener(this);
    }
    
    /**
     * Updates the dialog font to use the current font from FontManager.
     */
    private void updateDialogFont() {
        // Dispose of old font if it exists
        if (dialogFont != null) {
            dialogFont.dispose();
        }
        
        // Get the current font from FontManager
        dialogFont = FontManager.getInstance().getCurrentFont();
        System.out.println("MessageBoard: Updated to use font: " + FontManager.getInstance().getCurrentFontType().getDisplayName());
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
     * Shows the message board dialog.
     */
    public void show() {
        this.isVisible = true;
    }
    
    /**
     * Hides the message board dialog.
     */
    public void hide() {
        this.isVisible = false;
    }
    
    /**
     * Checks if the dialog is currently visible.
     * 
     * @return true if the dialog is visible, false otherwise
     */
    public boolean isVisible() {
        return isVisible;
    }
    
    /**
     * Handles keyboard input for the dialog.
     * ESC key closes the dialog.
     * D key opens the discussions link in browser.
     */
    public void handleInput() {
        if (!isVisible) {
            return;
        }
        
        // Handle escape (close dialog)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            hide();
        }
        
        // Handle D key to open discussions in browser
        if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            try {
                Gdx.net.openURI("https://github.com/gcclinux/woodlanders/discussions");
                System.out.println("Opening discussions in browser...");
            } catch (Exception e) {
                System.err.println("Failed to open browser: " + e.getMessage());
            }
        }
    }
    
    /**
     * Renders the message board with announcements and information.
     * 
     * @param batch The sprite batch for rendering
     * @param shapeRenderer The shape renderer (unused but kept for consistency)
     * @param camX Camera X position
     * @param camY Camera Y position
     */
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY) {
        if (!isVisible) {
            return;
        }
        
        batch.begin();
        
        // Center the dialog on screen
        float dialogX = camX - DIALOG_WIDTH / 2;
        float dialogY = camY - DIALOG_HEIGHT / 2;
        
        // Draw wooden plank background
        batch.draw(woodenPlank, dialogX, dialogY, DIALOG_WIDTH, DIALOG_HEIGHT);
        
        // Get localized strings
        LocalizationManager loc = LocalizationManager.getInstance();
        
        // Draw title with emoji
        dialogFont.setColor(Color.YELLOW);
        String title = loc.getText("message_board.title");
        dialogFont.draw(batch, title, dialogX + 30, dialogY + DIALOG_HEIGHT - 30);
        
        float currentY = dialogY + DIALOG_HEIGHT - 80;
        float leftMargin = dialogX + 40;
        
        // Welcome section
        dialogFont.setColor(Color.CYAN);
        dialogFont.draw(batch, loc.getText("message_board.welcome_header"), leftMargin, currentY);
        currentY -= 30;
        
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("message_board.welcome_line1"), leftMargin, currentY);
        currentY -= 22;
        dialogFont.draw(batch, loc.getText("message_board.welcome_line2"), leftMargin, currentY);
        currentY -= 22;
        dialogFont.draw(batch, loc.getText("message_board.welcome_line3"), leftMargin, currentY);
        currentY -= 40;
        
        // Story Mode section
        dialogFont.setColor(Color.ORANGE);
        dialogFont.draw(batch, loc.getText("message_board.story_mode_header"), leftMargin, currentY);
        currentY -= 30;
        
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("message_board.story_mode_line1"), leftMargin, currentY);
        currentY -= 22;
        dialogFont.draw(batch, loc.getText("message_board.story_mode_line2"), leftMargin, currentY);
        currentY -= 22;
        dialogFont.draw(batch, loc.getText("message_board.story_mode_line3"), leftMargin, currentY);
        currentY -= 40;
        
        // Feedback section
        dialogFont.setColor(Color.LIME);
        dialogFont.draw(batch, loc.getText("message_board.feedback_header"), leftMargin, currentY);
        currentY -= 30;
        
        dialogFont.setColor(Color.WHITE);
        dialogFont.draw(batch, loc.getText("message_board.feedback_line1"), leftMargin, currentY);
        currentY -= 22;
        dialogFont.draw(batch, loc.getText("message_board.feedback_line2"), leftMargin, currentY);
        
        // Draw close instruction at bottom
        dialogFont.setColor(Color.LIGHT_GRAY);
        String closeInstruction = loc.getText("message_board.close_instruction");
        dialogFont.draw(batch, closeInstruction, dialogX + 30, dialogY + 30);
        
        batch.end();
    }
    
    /**
     * Called when the application language changes.
     * The dialog will automatically use the new language on next render.
     * 
     * @param newLanguage The new language code
     */
    @Override
    public void onLanguageChanged(String newLanguage) {
        System.out.println("MessageBoard: Language changed to " + newLanguage);
        // The dialog will automatically use the new language on next render
        // because it calls LocalizationManager.getText() each time
    }
    
    /**
     * Called when the font changes.
     * Updates the dialog font to use the new font.
     * 
     * @param newFontType The new font type
     */
    @Override
    public void onFontChanged(FontType newFontType) {
        System.out.println("MessageBoard: Font changed to " + newFontType.getDisplayName());
        updateDialogFont();
    }
    
    /**
     * Disposes of resources used by the dialog.
     */
    public void dispose() {
        if (woodenPlank != null) {
            woodenPlank.dispose();
        }
        // Note: Don't dispose dialogFont as it's managed by FontManager
        
        // Unregister from listeners
        LocalizationManager.getInstance().removeLanguageChangeListener(this);
        FontManager.getInstance().removeFontChangeListener(this);
    }
}
