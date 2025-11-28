package wagemaker.uk.ui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import wagemaker.uk.client.PlayerConfig;
import wagemaker.uk.freeworld.FreeWorldManager;
import wagemaker.uk.player.Player;
import wagemaker.uk.world.WorldSaveManager;
import wagemaker.uk.world.WorldSaveData;
import wagemaker.uk.localization.LocalizationManager;
import wagemaker.uk.localization.LanguageChangeListener;

public class GameMenu implements LanguageChangeListener, FontChangeListener {
    private boolean isOpen = false;
    private Texture woodenPlank;
    private BitmapFont font;
    private BitmapFont playerNameFont; // Custom font for player name
    private String[] singleplayerMenuItems;
    private String[] multiplayerMenuItems;
    private int selectedIndex = 0;
    private float menuX, menuY;
    private Player player;
    private wagemaker.uk.gdx.MyGdxGame gameInstance;
    private wagemaker.uk.inventory.InventoryManager inventoryManager;
    private static final float MENU_WIDTH = 300; // Reduced by 25% from 400
    private static final float MENU_HEIGHT = 325; // Reduced by 15% total to eliminate excess space after Exit
    private static final float BORDER_INSET = 5; // Inset for border positioning
    private static final float NAME_DIALOG_WIDTH = 384; // Increased by 20% (320 * 1.2 = 384)
    private static final float NAME_DIALOG_HEIGHT = 220;
    
    // Player name dialog
    private boolean nameDialogOpen = false;
    private boolean nameDialogFromProfile = false; // Track if opened from player profile
    private String playerName = "Player";
    private String inputBuffer = "";
    private Texture nameDialogPlank; // Separate texture for name dialog
    
    // Multiplayer components
    private MultiplayerMenu multiplayerMenu;
    private ServerHostDialog serverHostDialog;
    private ConnectDialog connectDialog;
    private ErrorDialog errorDialog;
    
    // Player profile menu
    private PlayerProfileMenu playerProfileMenu;
    
    // Track where dialogs were opened from
    private enum DialogSource {
        NONE,
        STARTUP,
        MAIN_MENU,
        PLAYER_PROFILE
    }
    
    private DialogSource controlsDialogSource = DialogSource.NONE;
    private DialogSource playerLocationDialogSource = DialogSource.NONE;
    
    // Track if dialogs were opened from player profile menu (legacy, kept for other dialogs)
    private boolean dialogOpenedFromProfile = false;
    
    // Track if we just closed a dialog this frame (to prevent ESC from also opening main menu)
    private boolean dialogJustClosed = false;
    
    // Track frames since dialog closed (to prevent immediate ESC processing)
    private int framesSinceDialogClosed = 0;
    
    // Pending character selection (set by CharacterSelectionDialog, saved by savePlayerPosition)
    private static String pendingCharacterSelection = null;
    
    // World save/load components
    private WorldSaveDialog worldSaveDialog;
    private WorldLoadDialog worldLoadDialog;
    private WorldManageDialog worldManageDialog;
    private WorldSaveManager worldSaveManager;
    
    // Language dialog
    private LanguageDialog languageDialog;
    
    // Font selection dialog
    private FontSelectionDialog fontSelectionDialog;
    
    // Player location dialog
    private PlayerLocationDialog playerLocationDialog;
    
    // Controls dialog
    private ControlsDialog controlsDialog;
    
    // Notification system for save confirmation
    private boolean showSaveNotification = false;
    private float saveNotificationTimer = 0f;
    private static final float SAVE_NOTIFICATION_DURATION = 2.0f; // 2 seconds
    
    // Compass reference for custom target
    private Compass compass;
    
    // First launch flag for showing controls on startup
    private boolean hasShownControlsOnStartup = false;
    private boolean shouldShowControlsOnStartup = false;
    private int startupFrameCounter = 0;


    public GameMenu() {
        woodenPlank = createWoodenPlank();
        nameDialogPlank = createNameDialogPlank();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        font.setColor(Color.WHITE);
        
        // Create custom font for player name
        createPlayerNameFont();
        
        // Initialize multiplayer components
        multiplayerMenu = new MultiplayerMenu();
        serverHostDialog = new ServerHostDialog();
        connectDialog = new ConnectDialog();
        errorDialog = new ErrorDialog();
        
        // Initialize player profile menu
        playerProfileMenu = new PlayerProfileMenu();
        
        // Initialize world save/load components
        worldSaveManager = new WorldSaveManager();
        worldSaveDialog = new WorldSaveDialog();
        worldLoadDialog = new WorldLoadDialog();
        worldManageDialog = new WorldManageDialog();
        
        // Initialize language dialog
        languageDialog = new LanguageDialog();
        
        // Initialize font selection dialog
        fontSelectionDialog = new FontSelectionDialog();
        
        // Initialize player location dialog
        playerLocationDialog = new PlayerLocationDialog();
        
        // Initialize controls dialog
        controlsDialog = new ControlsDialog();
        
        // Register as language change listener
        LocalizationManager.getInstance().addLanguageChangeListener(this);
        
        // Register as font change listener
        FontManager.getInstance().addFontChangeListener(this);
        
        // Initialize menu items with localized text
        updateMenuItems();
    }
    
    private void createPlayerNameFont() {
        // Use FontManager to get the current font
        playerNameFont = FontManager.getInstance().getCurrentFont();
    }
    
    public void setPlayer(Player player) {
        this.player = player;
    }
    
    public void setGameInstance(wagemaker.uk.gdx.MyGdxGame gameInstance) {
        this.gameInstance = gameInstance;
    }
    
    public void setInventoryManager(wagemaker.uk.inventory.InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
    }
    
    public void setCompass(Compass compass) {
        this.compass = compass;
    }
    
    /**
     * Updates menu items with localized text.
     * Called on initialization and when language changes.
     */
    private void updateMenuItems() {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        singleplayerMenuItems = new String[] {
            loc.getText("menu.player_profile"),      // Index 0
            loc.getText("menu.save_world"),          // Index 1
            loc.getText("menu.load_world"),          // Index 2
            loc.getText("menu.free_world"),          // Index 3
            loc.getText("menu.story_mode"),          // Index 4 - NEW
            loc.getText("menu.multiplayer"),         // Index 5 - SHIFTED
            loc.getText("menu.exit")                 // Index 6 - SHIFTED
        };
        
        multiplayerMenuItems = new String[] {
            loc.getText("menu.player_profile"),      // Index 0
            loc.getText("menu.save_world"),          // Index 1
            loc.getText("menu.load_world"),          // Index 2
            loc.getText("menu.free_world"),          // Index 3
            loc.getText("menu.story_mode"),          // Index 4 - NEW
            loc.getText("menu.disconnect"),          // Index 5 - SHIFTED
            loc.getText("menu.exit")                 // Index 6 - SHIFTED
        };
    }
    
    /**
     * Called when the application language changes.
     * Refreshes menu items to display in the new language.
     * 
     * @param newLanguage The new language code
     */
    @Override
    public void onLanguageChanged(String newLanguage) {
        System.out.println("GameMenu: Language changed to " + newLanguage);
        updateMenuItems();
    }
    
    @Override
    public void onFontChanged(FontType newFont) {
        System.out.println("GameMenu: Font changed to " + newFont.getDisplayName());
        // Reload the font
        createPlayerNameFont();
    }
    
    private void openNameDialog() {
        nameDialogOpen = true;
        inputBuffer = playerName; // Start with current name
    }
    
    private void handleNameDialogInput() {
        // Handle text input
        for (int i = 0; i < 256; i++) {
            if (Gdx.input.isKeyJustPressed(i)) {
                char character = getCharFromKeyCode(i);
                if (character != 0 && inputBuffer.length() < 15) { // Max 15 characters
                    inputBuffer += character;
                }
            }
        }
        
        // Handle backspace
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && inputBuffer.length() > 0) {
            inputBuffer = inputBuffer.substring(0, inputBuffer.length() - 1);
        }
        
        // Handle enter (confirm)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            if (inputBuffer.length() >= 3) {
                playerName = inputBuffer;
                nameDialogOpen = false;
                System.out.println("Player name set to: " + playerName);
                // Return to player profile menu if opened from there
                if (nameDialogFromProfile) {
                    playerProfileMenu.open();
                    isOpen = false;
                    nameDialogFromProfile = false;
                }
            } else {
                // Validation message is displayed in the dialog, just log it
                System.out.println("Name must be at least 3 characters long");
            }
        }
        
        // Handle escape (cancel)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            nameDialogOpen = false;
            inputBuffer = playerName; // Reset to original name
            // Return to player profile menu if opened from there
            if (nameDialogFromProfile) {
                playerProfileMenu.open();
                isOpen = false;
                nameDialogFromProfile = false;
            }
        }
    }
    
    private char getCharFromKeyCode(int keyCode) {
        // Handle letters
        if (keyCode >= Input.Keys.A && keyCode <= Input.Keys.Z) {
            char letter = (char)('a' + (keyCode - Input.Keys.A));
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                return Character.toUpperCase(letter);
            }
            return letter;
        }
        
        // Handle numbers
        if (keyCode >= Input.Keys.NUM_0 && keyCode <= Input.Keys.NUM_9) {
            return (char)('0' + (keyCode - Input.Keys.NUM_0));
        }
        
        // Handle space
        if (keyCode == Input.Keys.SPACE) {
            return ' ';
        }
        
        return 0; // Invalid character
    }
    
    public boolean loadPlayerPosition() {
        if (player == null) {
            System.out.println("Cannot load: Player reference not set");
            return false;
        }
        
        try {
            File configDir = getConfigDirectory();
            File saveFile = new File(configDir, "woodlanders.json");
            
            if (!saveFile.exists()) {
                System.out.println("No save file found, starting at default position (0, 0)");
                return false;
            }
            
            // Read the JSON file
            String jsonContent = new String(Files.readAllBytes(Paths.get(saveFile.getAbsolutePath())));
            
            // Determine which position to load based on current game mode
            boolean isMultiplayer = (gameInstance != null && 
                                    gameInstance.getGameMode() != wagemaker.uk.gdx.MyGdxGame.GameMode.SINGLEPLAYER);
            
            float x, y, health, hunger;
            
            if (isMultiplayer) {
                // Load multiplayer position from object format
                try {
                    x = parseJsonObjectFloat(jsonContent, "\"multiplayerPosition\"", "x");
                    y = parseJsonObjectFloat(jsonContent, "\"multiplayerPosition\"", "y");
                    health = parseJsonFloat(jsonContent, "\"multiplayerHealth\":");
                    try {
                        hunger = parseJsonFloat(jsonContent, "\"multiplayerHunger\":");
                    } catch (Exception e) {
                        hunger = 0.0f; // Default hunger if not found
                    }
                    System.out.println("Loading multiplayer position from object format");
                } catch (Exception e) {

                    // Fallback to flat format for backwards compatibility
                    try {
                        x = parseJsonFloat(jsonContent, "\"multiplayerX\":");
                        y = parseJsonFloat(jsonContent, "\"multiplayerY\":");
                        health = parseJsonFloat(jsonContent, "\"multiplayerHealth\":");
                        try {
                            hunger = parseJsonFloat(jsonContent, "\"multiplayerHunger\":");
                        } catch (Exception e3) {
                            hunger = 0.0f;
                        }
                        System.out.println("Loading multiplayer position from legacy flat format");
                    } catch (Exception e2) {
                        // Fallback to spawn if multiplayer position doesn't exist
                        System.out.println("No multiplayer position found, using spawn (0, 0)");
                        x = 0;
                        y = 0;
                        health = 100.0f;
                        hunger = 0.0f;
                    }
                }
            } else {
                // Load singleplayer position from object format
                try {
                    x = parseJsonObjectFloat(jsonContent, "\"singleplayerPosition\"", "x");
                    y = parseJsonObjectFloat(jsonContent, "\"singleplayerPosition\"", "y");
                    health = parseJsonFloat(jsonContent, "\"singleplayerHealth\":");
                    try {
                        hunger = parseJsonFloat(jsonContent, "\"singleplayerHunger\":");
                    } catch (Exception e) {
                        hunger = 0.0f; // Default hunger if not found
                    }
                    System.out.println("Loading singleplayer position from object format");
                } catch (Exception e) {
                    // Fallback to flat format for backwards compatibility
                    try {
                        x = parseJsonFloat(jsonContent, "\"singleplayerX\":");
                        y = parseJsonFloat(jsonContent, "\"singleplayerY\":");
                        health = parseJsonFloat(jsonContent, "\"singleplayerHealth\":");
                        try {
                            hunger = parseJsonFloat(jsonContent, "\"singleplayerHunger\":");
                        } catch (Exception e3) {
                            hunger = 0.0f;
                        }
                        System.out.println("Loading singleplayer position from legacy flat format");
                    } catch (Exception e2) {
                        // Fallback to old format for backwards compatibility
                        try {
                            x = parseJsonFloat(jsonContent, "\"x\":");
                            y = parseJsonFloat(jsonContent, "\"y\":");
                            health = parseJsonFloat(jsonContent, "\"playerHealth\":");
                            hunger = 0.0f; // Old format doesn't have hunger
                            System.out.println("Loading position from legacy format");
                        } catch (Exception e3) {
                            System.out.println("No singleplayer position found, using default (0, 0)");
                            x = 0;
                            y = 0;
                            health = 100.0f;
                            hunger = 0.0f;
                        }
                    }
                }
            }
            
            // Load player name (shared between modes)
            String loadedName = parseJsonString(jsonContent, "\"playerName\":");
            
            // Load font preference
            try {
                String savedFont = parseJsonString(jsonContent, "\"fontName\":");
                if (savedFont != null && !savedFont.isEmpty()) {
                    FontType fontType = FontType.fromDisplayName(savedFont);
                    FontManager.getInstance().setFont(fontType);
                    System.out.println("Font loaded: " + savedFont);
                }
            } catch (Exception e) {
                // No font saved or error parsing, use default
                System.out.println("No saved font found, using default");
            }
            
            // Load selected character
            try {
                String savedCharacter = parseJsonString(jsonContent, "\"selectedCharacter\":");
                if (savedCharacter != null && !savedCharacter.isEmpty()) {
                    PlayerConfig playerConfig = PlayerConfig.load();
                    playerConfig.setSelectedCharacter(savedCharacter);
                    System.out.println("Selected character loaded: " + savedCharacter);
                }
            } catch (Exception e) {
                // No character saved or error parsing, use default
                System.out.println("No saved character found, using default");
            }
            
            // Set player position, health, and hunger
            player.setPosition(x, y);
            player.setHealth(health);
            player.setHunger(hunger);
            
            // Set player name if found
            if (loadedName != null && !loadedName.isEmpty()) {
                playerName = loadedName;
            }
            
            // Load inventory data if available
            if (inventoryManager != null) {
                try {
                    // Load singleplayer inventory
                    int spApple = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "apple");
                    int spBanana = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "banana");
                    int spBambooSapling = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "babyBamboo");
                    int spBambooStack = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "bambooStack");
                    int spBabyTree = 0;
                    try {
                        spBabyTree = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "babyTree");
                    } catch (Exception e) {
                        // BabyTree field doesn't exist in old saves
                    }
                    int spWoodStack = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "woodStack");
                    int spPebble = 0;
                    try {
                        spPebble = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "pebble");
                    } catch (Exception e) {
                        // Pebble field doesn't exist in old saves
                    }
                    int spPalmFiber = 0;
                    try {
                        spPalmFiber = parseJsonObjectInt(jsonContent, "\"singleplayerInventory\"", "palmFiber");
                    } catch (Exception e) {
                        // PalmFiber field doesn't exist in old saves
                    }
                    
                    wagemaker.uk.inventory.Inventory spInv = inventoryManager.getSingleplayerInventory();
                    spInv.setAppleCount(spApple);
                    spInv.setBananaCount(spBanana);
                    spInv.setBambooSaplingCount(spBambooSapling);
                    spInv.setBambooStackCount(spBambooStack);
                    spInv.setBabyTreeCount(spBabyTree);
                    spInv.setWoodStackCount(spWoodStack);
                    spInv.setPebbleCount(spPebble);
                    spInv.setPalmFiberCount(spPalmFiber);
                    
                    System.out.println("Singleplayer inventory loaded: Apple=" + spApple + 
                                      ", Banana=" + spBanana + ", BambooSapling=" + spBambooSapling + 
                                      ", BambooStack=" + spBambooStack + ", BabyTree=" + spBabyTree +
                                      ", WoodStack=" + spWoodStack + ", Pebble=" + spPebble);
                } catch (Exception e) {
                    System.out.println("No singleplayer inventory data found, starting with empty inventory");
                }
                
                try {
                    // Load multiplayer inventory
                    int mpApple = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "apple");
                    int mpBanana = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "banana");
                    int mpBambooSapling = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "babyBamboo");
                    int mpBambooStack = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "bambooStack");
                    int mpBabyTree = 0;
                    try {
                        mpBabyTree = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "babyTree");
                    } catch (Exception e) {
                        // BabyTree field doesn't exist in old saves
                    }
                    int mpWoodStack = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "woodStack");
                    int mpPebble = 0;
                    try {
                        mpPebble = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "pebble");
                    } catch (Exception e) {
                        // Pebble field doesn't exist in old saves
                    }
                    int mpPalmFiber = 0;
                    try {
                        mpPalmFiber = parseJsonObjectInt(jsonContent, "\"multiplayerInventory\"", "palmFiber");
                    } catch (Exception e) {
                        // PalmFiber field doesn't exist in old saves
                    }
                    
                    wagemaker.uk.inventory.Inventory mpInv = inventoryManager.getMultiplayerInventory();
                    mpInv.setAppleCount(mpApple);
                    mpInv.setBananaCount(mpBanana);
                    mpInv.setBambooSaplingCount(mpBambooSapling);
                    mpInv.setBambooStackCount(mpBambooStack);
                    mpInv.setBabyTreeCount(mpBabyTree);
                    mpInv.setWoodStackCount(mpWoodStack);
                    mpInv.setPebbleCount(mpPebble);
                    mpInv.setPalmFiberCount(mpPalmFiber);
                    
                    System.out.println("Multiplayer inventory loaded: Apple=" + mpApple + 
                                      ", Banana=" + mpBanana + ", BambooSapling=" + mpBambooSapling + 
                                      ", BambooStack=" + mpBambooStack + ", BabyTree=" + mpBabyTree +
                                      ", WoodStack=" + mpWoodStack + ", Pebble=" + mpPebble);
                } catch (Exception e) {
                    System.out.println("No multiplayer inventory data found, starting with empty inventory");
                }
            }
            
            System.out.println("Game loaded from: " + saveFile.getAbsolutePath());
            System.out.println("Player position loaded: (" + x + ", " + y + ")");
            System.out.println("Player health loaded: " + health);
            System.out.println("Player hunger loaded: " + hunger);
            System.out.println("Player name loaded: " + playerName);
            
            return true;
            
        } catch (IOException e) {
            System.out.println("Error loading game: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.out.println("Error parsing save file: " + e.getMessage());
            return false;
        }
    }
    
    private float parseJsonFloat(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) {
            throw new RuntimeException("Key not found: " + key);
        }
        
        // Find the start of the value (after the colon and any whitespace)
        int valueStart = keyIndex + key.length();
        while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\t')) {
            valueStart++;
        }
        
        // Find the end of the value (before comma, newline, or closing brace)
        int valueEnd = valueStart;
        while (valueEnd < json.length()) {
            char c = json.charAt(valueEnd);
            if (c == ',' || c == '\n' || c == '\r' || c == '}' || c == ' ' || c == '\t') {
                break;
            }
            valueEnd++;
        }
        
        String valueStr = json.substring(valueStart, valueEnd).trim();
        
        // Remove any trailing non-numeric characters
        StringBuilder cleanValue = new StringBuilder();
        for (char c : valueStr.toCharArray()) {
            if (Character.isDigit(c) || c == '.' || c == '-') {
                cleanValue.append(c);
            } else {
                break; // Stop at first non-numeric character
            }
        }
        
        return Float.parseFloat(cleanValue.toString());
    }
    
    private String parseJsonString(String json, String key) {
        int keyIndex = json.indexOf(key);
        if (keyIndex == -1) {
            return null; // Key not found
        }
        
        // Find the start of the value (after the colon and any whitespace)
        int valueStart = keyIndex + key.length();
        while (valueStart < json.length() && (json.charAt(valueStart) == ' ' || json.charAt(valueStart) == '\t')) {
            valueStart++;
        }
        
        // Skip opening quote
        if (valueStart < json.length() && json.charAt(valueStart) == '"') {
            valueStart++;
        }
        
        // Find the end of the value (closing quote)
        int valueEnd = valueStart;
        while (valueEnd < json.length() && json.charAt(valueEnd) != '"') {
            valueEnd++;
        }
        
        if (valueEnd > valueStart) {
            return json.substring(valueStart, valueEnd);
        }
        
        return null;
    }
    
    private float parseJsonObjectFloat(String json, String objectKey, String propertyKey) {
        // Find the object
        int objectIndex = json.indexOf(objectKey);
        if (objectIndex == -1) {
            throw new RuntimeException("Object not found: " + objectKey);
        }
        
        // Find the opening brace of the object
        int braceStart = json.indexOf("{", objectIndex);
        if (braceStart == -1) {
            throw new RuntimeException("Object opening brace not found for: " + objectKey);
        }
        
        // Find the closing brace of the object
        int braceEnd = json.indexOf("}", braceStart);
        if (braceEnd == -1) {
            throw new RuntimeException("Object closing brace not found for: " + objectKey);
        }
        
        // Extract the object content
        String objectContent = json.substring(braceStart + 1, braceEnd);
        
        // Parse the property within the object
        String propertyPattern = "\"" + propertyKey + "\":";
        return parseJsonFloat(objectContent, propertyPattern);
    }
    
    private int parseJsonObjectInt(String json, String objectKey, String propertyKey) {
        // Find the object
        int objectIndex = json.indexOf(objectKey);
        if (objectIndex == -1) {
            throw new RuntimeException("Object not found: " + objectKey);
        }
        
        // Find the opening brace of the object
        int braceStart = json.indexOf("{", objectIndex);
        if (braceStart == -1) {
            throw new RuntimeException("Object opening brace not found for: " + objectKey);
        }
        
        // Find the closing brace of the object
        int braceEnd = json.indexOf("}", braceStart);
        if (braceEnd == -1) {
            throw new RuntimeException("Object closing brace not found for: " + objectKey);
        }
        
        // Extract the object content
        String objectContent = json.substring(braceStart + 1, braceEnd);
        
        // Parse the property within the object
        String propertyPattern = "\"" + propertyKey + "\":";
        int keyIndex = objectContent.indexOf(propertyPattern);
        if (keyIndex == -1) {
            throw new RuntimeException("Property not found: " + propertyKey);
        }
        
        // Find the start of the value (after the colon and any whitespace)
        int valueStart = keyIndex + propertyPattern.length();
        while (valueStart < objectContent.length() && 
               (objectContent.charAt(valueStart) == ' ' || objectContent.charAt(valueStart) == '\t')) {
            valueStart++;
        }
        
        // Find the end of the value (before comma, newline, or closing brace)
        int valueEnd = valueStart;
        while (valueEnd < objectContent.length()) {
            char c = objectContent.charAt(valueEnd);
            if (c == ',' || c == '\n' || c == '\r' || c == '}' || c == ' ' || c == '\t') {
                break;
            }
            valueEnd++;
        }
        
        String valueStr = objectContent.substring(valueStart, valueEnd).trim();
        
        // Remove any trailing non-numeric characters
        StringBuilder cleanValue = new StringBuilder();
        for (char c : valueStr.toCharArray()) {
            if (Character.isDigit(c) || c == '-') {
                cleanValue.append(c);
            } else {
                break; // Stop at first non-numeric character
            }
        }
        
        return Integer.parseInt(cleanValue.toString());
    }

    public void update() {
        // Reset the dialog just closed flag at the start of each frame
        dialogJustClosed = false;
        
        // Increment frames counter if we're waiting
        if (framesSinceDialogClosed > 0) {
            framesSinceDialogClosed++;
            if (framesSinceDialogClosed > 5) {
                framesSinceDialogClosed = 0; // Reset after 5 frames (more time for key release)
            }
        }
        
        // Check if we should show controls on startup (after a few frames)
        checkStartupControlsDisplay();
        
        // Update save notification timer
        if (showSaveNotification) {
            saveNotificationTimer -= Gdx.graphics.getDeltaTime();
            if (saveNotificationTimer <= 0) {
                showSaveNotification = false;
            }
        }
        
        // Handle dialogs first (highest priority)
        if (errorDialog.isVisible()) {
            errorDialog.handleInput();
            return;
        }
        
        if (worldSaveDialog.isVisible()) {
            worldSaveDialog.handleInput();
            handleWorldSaveDialogResult();
            return;
        }
        
        if (worldLoadDialog.isVisible()) {
            worldLoadDialog.handleInput();
            handleWorldLoadDialogResult();
            return;
        }
        
        if (worldManageDialog.isVisible()) {
            worldManageDialog.handleInput();
            return;
        }
        
        if (connectDialog.isVisible()) {
            connectDialog.handleInput();
            return;
        }
        
        if (serverHostDialog.isVisible()) {
            serverHostDialog.handleInput();
            return;
        }
        
        if (languageDialog.isVisible()) {
            languageDialog.handleInput();
            handleLanguageDialogResult();
            return;
        }
        
        if (fontSelectionDialog.isOpen()) {
            fontSelectionDialog.update();
            if (!fontSelectionDialog.isOpen()) {
                playerProfileMenu.open(); // Return to Player Profile menu after dialog closes
                isOpen = false; // Ensure main menu stays closed
            }
            return;
        }
        
        if (playerLocationDialog.isVisible()) {
            boolean wasVisible = true;
            playerLocationDialog.handleInput();
            // Check if dialog was closed by ESC this frame
            if (wasVisible && !playerLocationDialog.isVisible()) {
                dialogJustClosed = true;
            }
            handlePlayerLocationDialogResult();
            return;
        }
        
        if (controlsDialog.isVisible()) {
            boolean wasVisible = true;
            controlsDialog.handleInput();
            // Check if dialog was closed by ESC this frame
            if (wasVisible && !controlsDialog.isVisible()) {
                dialogJustClosed = true;
                framesSinceDialogClosed = 1; // Start counting frames
            }
            handleControlsDialogResult();
            return;
        }
        
        if (nameDialogOpen) {
            handleNameDialogInput();
            return;
        }
        
        // Handle player profile menu
        if (playerProfileMenu.isOpen()) {
            // Don't process input if we just closed a dialog this frame or within last 5 frames
            if (!dialogJustClosed && framesSinceDialogClosed == 0) {
                boolean wasOpen = playerProfileMenu.isOpen();
                playerProfileMenu.update();
                // Check if user selected an option
                boolean selectionHandled = false;
                if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                    handlePlayerProfileMenuSelection();
                    selectionHandled = true;
                }
                // Check if menu was closed (e.g., via ESC key)
                // But don't open main menu if we just handled a selection (which may have opened a dialog)
                if (!playerProfileMenu.isOpen() && !selectionHandled) {
                    isOpen = true; // Return to main menu
                }
            }
            return;
        }
        
        // Handle multiplayer menu
        if (multiplayerMenu.isOpen()) {
            multiplayerMenu.update();
            // Check if user selected an option
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                handleMultiplayerMenuSelection();
            }
            return;
        }
        
        // Handle main menu toggle with ESC key
        // ESC opens/closes the main menu, but only when no dialogs or submenus are active
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && 
            !playerProfileMenu.isOpen() && 
            !multiplayerMenu.isOpen() && 
            !dialogJustClosed &&
            framesSinceDialogClosed == 0) {
            isOpen = !isOpen;
            if (isOpen) {
                ensureValidMenuSelection();
            }
        }

        if (isOpen) {
            String[] currentMenuItems = getCurrentMenuItems();
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                do {
                    selectedIndex = (selectedIndex - 1 + currentMenuItems.length) % currentMenuItems.length;
                } while (isMenuItemDisabled(currentMenuItems[selectedIndex]));
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
                do {
                    selectedIndex = (selectedIndex + 1) % currentMenuItems.length;
                } while (isMenuItemDisabled(currentMenuItems[selectedIndex]));
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
                // Only execute if the item is not disabled
                if (!isMenuItemDisabled(currentMenuItems[selectedIndex])) {
                    executeMenuItem(selectedIndex);
                }
            }
        }
    }

    /**
     * Renders the menu title at the top of the menu background.
     * The title is retrieved from localization and centered horizontally.
     * 
     * @param batch The SpriteBatch to use for rendering
     */
    private void renderMenuTitle(SpriteBatch batch) {
        // Retrieve localized "menu.title" text from LocalizationManager
        LocalizationManager loc = LocalizationManager.getInstance();
        String titleText = loc.getText("menu.title");
        
        // Calculate centered X position using GlyphLayout
        com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        titleLayout.setText(playerNameFont, titleText);
        float titleX = menuX + (MENU_WIDTH - titleLayout.width) / 2;
        
        // Position title 30 pixels from top of menu
        float titleY = menuY + MENU_HEIGHT - 30;
        
        // Render using playerNameFont with white color
        playerNameFont.setColor(Color.WHITE);
        playerNameFont.draw(batch, titleText, titleX, titleY);
    }
    
    /**
     * Renders white borders around the menu background.
     * The borders are positioned 5 pixels inset from the menu edges.
     * 
     * @param shapeRenderer The ShapeRenderer to use for drawing the border
     */
    private void renderMenuBorders(ShapeRenderer shapeRenderer) {
        // Calculate border dimensions based on MENU_WIDTH and MENU_HEIGHT
        // Position border 5 pixels inset from menu edges
        float borderX = menuX + BORDER_INSET;
        float borderY = menuY + BORDER_INSET;
        float borderWidth = MENU_WIDTH - (BORDER_INSET * 2);
        float borderHeight = MENU_HEIGHT - (BORDER_INSET * 2);
        
        // Use ShapeRenderer to draw rectangular border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        
        // Use white color (1.0f, 1.0f, 1.0f, 1.0f)
        shapeRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        
        // Draw the border rectangle
        shapeRenderer.rect(borderX, borderY, borderWidth, borderHeight);
        
        shapeRenderer.end();
    }
    
    /**
     * Renders horizontal dividers between menu sections.
     * Dividers appear after Player Profile, Load World, and Story Mode.
     * 
     * @param shapeRenderer The ShapeRenderer to use for drawing lines
     */
    private void renderMenuDividers(ShapeRenderer shapeRenderer) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1.0f, 1.0f, 1.0f, 1.0f); // White color
        
        float dividerX = menuX + 20; // Start 20 pixels from left edge
        float dividerWidth = MENU_WIDTH - 40; // 20 pixels margin on each side
        float dividerHeight = 2; // 2 pixels thick
        
        // Divider after Player Profile (index 0)
        // Menu items start at menuY + MENU_HEIGHT - 70, each item is 30 pixels apart
        // Positioned 21 pixels below item (17 base + 4 extra spacing above divider)
        float divider1Y = menuY + MENU_HEIGHT - 70 - (0 * 30) - 21 - 4;
        shapeRenderer.rect(dividerX, divider1Y, dividerWidth, dividerHeight);
        
        // Divider after Load World (index 2)
        // Account for 4px extra spacing after first divider
        float divider2Y = menuY + MENU_HEIGHT - 70 - (2 * 30) - 4 - 21 - 4;
        shapeRenderer.rect(dividerX, divider2Y, dividerWidth, dividerHeight);
        
        // Divider after Story Mode (index 4)
        // Account for 8px extra spacing (4px after first divider + 4px after second divider)
        float divider3Y = menuY + MENU_HEIGHT - 70 - (4 * 30) - 8 - 21 - 4;
        shapeRenderer.rect(dividerX, divider3Y, dividerWidth, dividerHeight);
        
        shapeRenderer.end();
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, float camX, float camY, float viewWidth, float viewHeight) {
        // Render dialogs (highest priority)
        if (errorDialog.isVisible()) {
            errorDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (worldSaveDialog.isVisible()) {
            worldSaveDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (worldLoadDialog.isVisible()) {
            worldLoadDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (worldManageDialog.isVisible()) {
            worldManageDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (connectDialog.isVisible()) {
            connectDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (serverHostDialog.isVisible()) {
            serverHostDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (languageDialog.isVisible()) {
            languageDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (fontSelectionDialog.isOpen()) {
            fontSelectionDialog.render(batch, playerNameFont, woodenPlank, camX, camY);
            return;
        }
        
        if (playerLocationDialog.isVisible()) {
            playerLocationDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (controlsDialog.isVisible()) {
            controlsDialog.render(batch, shapeRenderer, camX, camY);
            return;
        }
        
        if (!isOpen && !nameDialogOpen && !multiplayerMenu.isOpen() && !playerProfileMenu.isOpen()) return;

        batch.begin();
        
        if (nameDialogOpen) {
            // Render name editor on wooden plank - centered on screen
            float centerX = camX - NAME_DIALOG_WIDTH / 2;
            float centerY = camY - NAME_DIALOG_HEIGHT / 2;

            batch.draw(nameDialogPlank, centerX, centerY, NAME_DIALOG_WIDTH, NAME_DIALOG_HEIGHT);
            
            // Get localized strings
            LocalizationManager loc = LocalizationManager.getInstance();
            String title = loc.getText("player_name_dialog.title");
            String minCharsText = loc.getText("player_name_dialog.min_characters");
            String instructionsText = loc.getText("player_name_dialog.instructions");
            
            // Title - centered (removed colon)
            playerNameFont.setColor(Color.WHITE);
            com.badlogic.gdx.graphics.g2d.GlyphLayout titleLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
            titleLayout.setText(playerNameFont, title);
            float titleX = centerX + (NAME_DIALOG_WIDTH - titleLayout.width) / 2;
            playerNameFont.draw(batch, title, titleX, centerY + NAME_DIALOG_HEIGHT - 30);
            
            // Input text - centered with cursor
            playerNameFont.setColor(Color.YELLOW);
            String inputText = inputBuffer + "_";
            com.badlogic.gdx.graphics.g2d.GlyphLayout inputLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
            inputLayout.setText(playerNameFont, inputText);
            float inputX = centerX + (NAME_DIALOG_WIDTH - inputLayout.width) / 2;
            playerNameFont.draw(batch, inputText, inputX, centerY + NAME_DIALOG_HEIGHT - 80);
            
            // Min characters warning - centered
            playerNameFont.setColor(Color.LIGHT_GRAY);
            com.badlogic.gdx.graphics.g2d.GlyphLayout minCharsLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
            minCharsLayout.setText(playerNameFont, minCharsText);
            float minCharsX = centerX + (NAME_DIALOG_WIDTH - minCharsLayout.width) / 2;
            playerNameFont.draw(batch, minCharsText, minCharsX, centerY + 70);
            
            // Instructions - centered
            com.badlogic.gdx.graphics.g2d.GlyphLayout instructionsLayout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
            instructionsLayout.setText(playerNameFont, instructionsText);
            float instructionsX = centerX + (NAME_DIALOG_WIDTH - instructionsLayout.width) / 2;
            playerNameFont.draw(batch, instructionsText, instructionsX, centerY + 40);
            
        } else if (isOpen) {
            // Render main menu
            menuX = camX - viewWidth / 2 + 25;
            menuY = camY + viewHeight / 2 - 25 - MENU_HEIGHT;

            batch.draw(woodenPlank, menuX, menuY, MENU_WIDTH, MENU_HEIGHT);
            
            // End batch to render borders with ShapeRenderer
            batch.end();
            
            // Render menu borders
            renderMenuBorders(shapeRenderer);
            
            // Restart batch for title and menu items
            batch.begin();
            
            // Render menu title
            renderMenuTitle(batch);
            
            // End batch to render dividers with ShapeRenderer
            batch.end();
            
            // Render menu dividers
            renderMenuDividers(shapeRenderer);
            
            // Restart batch for menu items
            batch.begin();
            
            String[] currentMenuItems = getCurrentMenuItems();
            for (int i = 0; i < currentMenuItems.length; i++) {
                String menuItem = currentMenuItems[i];
                
                // Check if this menu item should be disabled
                boolean isDisabled = isMenuItemDisabled(menuItem);
                
                if (isDisabled) {
                    playerNameFont.setColor(Color.GRAY);
                } else if (i == selectedIndex) {
                    playerNameFont.setColor(Color.YELLOW);
                } else {
                    playerNameFont.setColor(Color.WHITE);
                }
                
                float textX = menuX + 40;
                // Start menu items lower to avoid overlapping with title (70 pixels from top instead of 40)
                // Add extra spacing after dividers (items 0, 2, 4) to create larger gaps
                float extraSpacing = 0;
                if (i > 0) extraSpacing += 4; // Add 4px after item 0 (Player Profile divider)
                if (i > 2) extraSpacing += 4; // Add 4px after item 2 (Load World divider)
                if (i > 4) extraSpacing += 4; // Add 4px after item 4 (Story Mode divider)
                
                float textY = menuY + MENU_HEIGHT - 70 - (i * 30) - extraSpacing;
                
                // Display the menu item (disabled items are shown in gray color)
                playerNameFont.draw(batch, menuItem, textX, textY);
            }
        }
        
        batch.end();
        
        // Render multiplayer menu if open
        if (multiplayerMenu.isOpen()) {
            multiplayerMenu.render(batch, shapeRenderer, camX, camY, viewWidth, viewHeight);
        }
        
        // Render player profile menu if open
        if (playerProfileMenu.isOpen()) {
            playerProfileMenu.render(batch, shapeRenderer, camX, camY, viewWidth, viewHeight);
            
            // Render save notification overlay if active
            if (showSaveNotification) {
                // Create a semi-transparent background for the notification
                float notifWidth = 200;
                float notifHeight = 60;
                float notifX = camX - notifWidth / 2;
                float notifY = camY + 100; // Position above center
                
                // Draw background
                shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                shapeRenderer.setColor(0.2f, 0.5f, 0.2f, 0.9f); // Green with transparency
                shapeRenderer.rect(notifX, notifY, notifWidth, notifHeight);
                shapeRenderer.end();
                
                // Draw border
                shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
                shapeRenderer.setColor(0.1f, 0.3f, 0.1f, 1.0f); // Darker green border
                shapeRenderer.rect(notifX, notifY, notifWidth, notifHeight);
                shapeRenderer.end();
                
                // Draw "Saved!" text
                batch.begin();
                playerNameFont.setColor(Color.WHITE);
                String savedText = "Saved!";
                com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
                layout.setText(playerNameFont, savedText);
                float textX = notifX + (notifWidth - layout.width) / 2;
                float textY = notifY + notifHeight / 2 + layout.height / 2;
                playerNameFont.draw(batch, savedText, textX, textY);
                batch.end();
            }
        }
    }

    private void executeMenuItem(int index) {
        String[] currentMenuItems = getCurrentMenuItems();
        String selectedItem = currentMenuItems[index];
        
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (selectedItem.equals(loc.getText("menu.player_profile"))) {
            openPlayerProfileMenu();
        } else if (selectedItem.equals(loc.getText("menu.player_name"))) {
            openNameDialog();
        } else if (selectedItem.equals(loc.getText("menu.save_world"))) {
            openWorldSaveDialog();
        } else if (selectedItem.equals(loc.getText("menu.load_world"))) {
            openWorldLoadDialog();
        } else if (selectedItem.equals(loc.getText("menu.free_world"))) {
            activateFreeWorld();
        } else if (selectedItem.equals(loc.getText("menu.story_mode"))) {
            // Story Mode placeholder - no action taken
            // This menu entry is reserved for future story mode functionality
            System.out.println("Story Mode selected (placeholder - no action)");
        } else if (selectedItem.equals(loc.getText("menu.multiplayer"))) {
            openMultiplayerMenu();
        } else if (selectedItem.equals(loc.getText("menu.disconnect"))) {
            disconnectFromMultiplayer();
        } else if (selectedItem.equals(loc.getText("menu.language"))) {
            openLanguageDialog();
        } else if (selectedItem.equals(loc.getText("menu.save_player"))) {
            savePlayerPosition();
        } else if (selectedItem.equals(loc.getText("menu.exit"))) {
            if (!FreeWorldManager.isFreeWorldActive()) {
                savePlayerPosition(); // Auto-save before exit (unless Free World is active)
            }
            Gdx.app.exit();
        }
    }
    
    /**
     * Activates Free World mode and grants 250 of each item.
     */
    private void activateFreeWorld() {
        if (inventoryManager == null) {
            System.err.println("Cannot activate Free World: InventoryManager not set");
            return;
        }
        
        // Activate Free World mode
        FreeWorldManager.activateFreeWorld();
        
        // Grant 250 items to current inventory
        FreeWorldManager.grantFreeWorldItems(inventoryManager.getCurrentInventory());
        
        // Send inventory update to server in multiplayer
        if (inventoryManager != null) {
            inventoryManager.sendInventoryUpdateToServer();
        }
        
        // Broadcast to all clients in multiplayer
        if (gameInstance != null && gameInstance.getGameMode() == wagemaker.uk.gdx.MyGdxGame.GameMode.MULTIPLAYER_HOST) {
            wagemaker.uk.network.FreeWorldActivationMessage freeWorldMsg = 
                new wagemaker.uk.network.FreeWorldActivationMessage(true);
            gameInstance.getGameServer().broadcastToAll(freeWorldMsg);
            System.out.println("Free World activation broadcasted to all clients");
        }
        
        // Show confirmation message
        LocalizationManager loc = LocalizationManager.getInstance();
        String title = loc.getText("messages.free_world_activated");
        String message = loc.getText("messages.free_world_message");
        showSuccess(message, title);
        
        isOpen = false; // Close menu
    }
    
    /**
     * Opens the language selection dialog.
     */
    private void openLanguageDialog() {
        isOpen = false; // Close main menu
        languageDialog.show();
    }
    
    /**
     * Opens the font selection dialog.
     */
    private void openFontSelectionDialog() {
        isOpen = false; // Close main menu
        fontSelectionDialog.open();
    }
    
    /**
     * Opens the player location dialog from main menu.
     */
    private void openPlayerLocationDialog() {
        playerLocationDialogSource = DialogSource.MAIN_MENU;
        isOpen = false; // Close main menu
        PlayerConfig config = PlayerConfig.load();
        playerLocationDialog.show(player, compass, config);
    }
    
    /**
     * Opens the player location dialog from player profile menu.
     */
    private void openPlayerLocationDialogFromProfile() {
        playerLocationDialogSource = DialogSource.PLAYER_PROFILE;
        dialogOpenedFromProfile = true;
        isOpen = false; // Ensure main menu is closed
        PlayerConfig config = PlayerConfig.load();
        playerLocationDialog.show(player, compass, config);
    }
    
    /**
     * Opens the controls dialog from main menu.
     */
    private void openControlsDialog() {
        controlsDialogSource = DialogSource.MAIN_MENU;
        isOpen = false; // Close main menu
        controlsDialog.show();
    }
    
    /**
     * Opens the controls dialog from player profile menu.
     */
    private void openControlsDialogFromProfile() {
        controlsDialogSource = DialogSource.PLAYER_PROFILE;
        dialogOpenedFromProfile = true;
        isOpen = false; // Ensure main menu is closed
        controlsDialog.show();
    }
    
    /**
     * Marks that the controls dialog should be shown on startup.
     * This is called during game initialization.
     * The actual display is delayed by a few frames to allow the world to load properly.
     */
    public void showControlsOnStartup() {
        if (!hasShownControlsOnStartup) {
            shouldShowControlsOnStartup = true;
            startupFrameCounter = 0;
        }
    }
    
    /**
     * Checks if controls should be shown after a few frames have passed.
     * This ensures the world has loaded and camera is positioned correctly.
     */
    private void checkStartupControlsDisplay() {
        if (shouldShowControlsOnStartup) {
            startupFrameCounter++;
            // Wait 3 frames before showing controls to allow world to load
            if (startupFrameCounter >= 3) {
                controlsDialogSource = DialogSource.STARTUP;
                controlsDialog.show();
                hasShownControlsOnStartup = true;
                shouldShowControlsOnStartup = false;
            }
        }
    }
    
    /**
     * Handles the result of the player location dialog.
     */
    private void handlePlayerLocationDialogResult() {
        if (!playerLocationDialog.isVisible()) {
            // Dialog was closed - return to appropriate location
            switch (playerLocationDialogSource) {
                case PLAYER_PROFILE:
                    playerProfileMenu.open(); // Return to player profile menu
                    break;
                case MAIN_MENU:
                    isOpen = true; // Return to main menu
                    break;
                case STARTUP:
                case NONE:
                default:
                    // Return to game, don't open any menu
                    break;
            }
            playerLocationDialogSource = DialogSource.NONE;
            dialogOpenedFromProfile = false;
        }
    }
    
    /**
     * Handles the result of the controls dialog.
     */
    private void handleControlsDialogResult() {
        if (!controlsDialog.isVisible()) {
            // Dialog was closed - return to appropriate location
            switch (controlsDialogSource) {
                case PLAYER_PROFILE:
                    playerProfileMenu.open(); // Return to player profile menu
                    break;
                case MAIN_MENU:
                    isOpen = true; // Return to main menu
                    break;
                case STARTUP:
                case NONE:
                default:
                    // Return to game, don't open any menu
                    break;
            }
            controlsDialogSource = DialogSource.NONE;
            dialogOpenedFromProfile = false;
        }
    }
    
    /**
     * Gets the current menu items based on game mode.
     * @return The appropriate menu items array
     */
    private String[] getCurrentMenuItems() {
        if (gameInstance != null && gameInstance.getGameMode() != wagemaker.uk.gdx.MyGdxGame.GameMode.SINGLEPLAYER) {
            return multiplayerMenuItems;
        }
        return singleplayerMenuItems;
    }
    
    /**
     * Disconnects from multiplayer and returns to singleplayer mode.
     */
    private void disconnectFromMultiplayer() {
        if (gameInstance != null) {
            gameInstance.disconnectFromMultiplayer();
            isOpen = false; // Close menu after disconnect
        }
    }
    
    /**
     * Opens the multiplayer menu.
     */
    private void openMultiplayerMenu() {
        isOpen = false; // Close main menu
        multiplayerMenu.open();
    }
    
    /**
     * Opens the player profile menu.
     */
    private void openPlayerProfileMenu() {
        isOpen = false; // Close main menu
        playerProfileMenu.open();
    }
    
    /**
     * Opens the world save dialog if save functionality is available.
     */
    private void openWorldSaveDialog() {
        if (!isWorldSaveAllowed()) {
            LocalizationManager loc = LocalizationManager.getInstance();
            String errorMessage = getWorldSaveRestrictionMessage();
            String errorTitle = loc.getText("messages.save_restricted");
            showError(errorMessage, errorTitle);
            return;
        }
        
        isOpen = false; // Close main menu
        boolean isMultiplayer = isCurrentlyMultiplayer();
        worldSaveDialog.show(isMultiplayer);
    }
    
    /**
     * Gets the appropriate error message for world save restrictions.
     * @return The error message explaining why world save is not available
     */
    private String getWorldSaveRestrictionMessage() {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (gameInstance == null) {
            return loc.getText("messages.world_save_not_available");
        }
        
        wagemaker.uk.gdx.MyGdxGame.GameMode currentMode = gameInstance.getGameMode();
        
        if (currentMode == wagemaker.uk.gdx.MyGdxGame.GameMode.MULTIPLAYER_CLIENT) {
            return loc.getText("messages.world_save_client_restricted");
        }
        
        return loc.getText("messages.world_save_mode_restricted");
    }
    
    /**
     * Checks if a menu item should be disabled based on current game state.
     * @param menuItem The menu item to check
     * @return true if the menu item should be disabled, false otherwise
     */
    private boolean isMenuItemDisabled(String menuItem) {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (menuItem.equals(loc.getText("menu.save_world"))) {
            return !isWorldSaveAllowed();
        }
        
        if (menuItem.equals(loc.getText("menu.free_world"))) {
            // Hide Free World for multiplayer clients
            if (gameInstance != null && 
                gameInstance.getGameMode() == wagemaker.uk.gdx.MyGdxGame.GameMode.MULTIPLAYER_CLIENT) {
                return true;
            }
        }
        
        // Other menu items are always enabled
        return false;
    }
    
    /**
     * Ensures that the currently selected menu item is not disabled.
     * If it is disabled, moves to the next available item.
     */
    private void ensureValidMenuSelection() {
        String[] currentMenuItems = getCurrentMenuItems();
        if (currentMenuItems.length == 0) {
            return;
        }
        
        // If current selection is disabled, find the next enabled item
        if (isMenuItemDisabled(currentMenuItems[selectedIndex])) {
            int originalIndex = selectedIndex;
            do {
                selectedIndex = (selectedIndex + 1) % currentMenuItems.length;
            } while (isMenuItemDisabled(currentMenuItems[selectedIndex]) && selectedIndex != originalIndex);
        }
    }
    
    /**
     * Opens the world load dialog.
     */
    private void openWorldLoadDialog() {
        isOpen = false; // Close main menu
        boolean isMultiplayer = isCurrentlyMultiplayer();
        worldLoadDialog.show(isMultiplayer);
    }
    
    /**
     * Checks if the current game mode is multiplayer.
     * @return true if in multiplayer mode, false otherwise
     */
    private boolean isCurrentlyMultiplayer() {
        if (gameInstance == null) {
            return false;
        }
        
        return gameInstance.getGameMode() != wagemaker.uk.gdx.MyGdxGame.GameMode.SINGLEPLAYER;
    }
    
    /**
     * Handles the result of the language dialog.
     */
    private void handleLanguageDialogResult() {
        if (languageDialog.isConfirmed()) {
            languageDialog.hide();
            playerProfileMenu.open(); // Return to Player Profile menu
            isOpen = false; // Ensure main menu stays closed
        } else if (!languageDialog.isVisible()) {
            // Dialog was cancelled
            playerProfileMenu.open(); // Return to Player Profile menu
            isOpen = false; // Ensure main menu stays closed
        }
    }
    
    /**
     * Handles the result of the world save dialog.
     */
    private void handleWorldSaveDialogResult() {
        if (worldSaveDialog.isConfirmed()) {
            String saveName = worldSaveDialog.getSaveName();
            if (saveName != null && !saveName.trim().isEmpty()) {
                performWorldSave(saveName.trim());
            }
            worldSaveDialog.hide();
        } else if (worldSaveDialog.isCancelled()) {
            worldSaveDialog.hide();
            isOpen = true; // Return to main menu
        }
    }
    
    /**
     * Handles the result of the world load dialog.
     */
    private void handleWorldLoadDialogResult() {
        if (worldLoadDialog.isConfirmed()) {
            String saveName = worldLoadDialog.getSelectedSaveName();
            if (saveName != null && !saveName.trim().isEmpty()) {
                performWorldLoad(saveName.trim());
            }
            worldLoadDialog.hide();
        } else if (worldLoadDialog.isCancelled()) {
            worldLoadDialog.hide();
            isOpen = true; // Return to main menu
        }
    }
    
    /**
     * Performs the actual world save operation.
     * @param saveName The name of the save
     */
    private void performWorldSave(String saveName) {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (gameInstance == null || player == null) {
            showError(loc.getText("messages.cannot_save_not_initialized"), 
                     loc.getText("error_dialog.save_error"));
            return;
        }
        
        try {
            System.out.println("World save requested for: " + saveName);
            
            // Extract current world state from the game
            wagemaker.uk.network.WorldState currentWorldState = gameInstance.extractCurrentWorldState();
            
            if (currentWorldState == null) {
                showError(loc.getText("messages.failed_extract_state"), 
                         loc.getText("error_dialog.save_error"));
                return;
            }
            
            boolean isMultiplayer = isCurrentlyMultiplayer();
            
            // If Free World is active, don't save player data (position, health, inventory)
            float saveX, saveY, saveHealth;
            wagemaker.uk.inventory.Inventory currentInventory = null;
            
            if (FreeWorldManager.isFreeWorldActive()) {
                // Save with spawn position and default values
                saveX = 0;
                saveY = 0;
                saveHealth = 100;
                currentInventory = null;
            } else {
                // Save actual player data
                saveX = player.getX();
                saveY = player.getY();
                saveHealth = player.getHealth();
                if (inventoryManager != null) {
                    currentInventory = inventoryManager.getCurrentInventory();
                }
            }
            
            boolean success = WorldSaveManager.saveWorld(
                saveName, 
                currentWorldState,
                saveX, 
                saveY, 
                saveHealth,
                currentInventory,
                isMultiplayer
            );
            
            if (success) {
                // World saved successfully - no confirmation dialog needed, just continue
                System.out.println("World '" + saveName + "' saved successfully");
            } else {
                showError(loc.getText("messages.save_failed"), 
                         loc.getText("error_dialog.save_error"));
                System.err.println("World save failed for: " + saveName);
            }
        } catch (Exception e) {
            System.err.println("Error saving world: " + e.getMessage());
            showError(loc.getText("messages.save_error", e.getMessage()), 
                     loc.getText("error_dialog.save_error"));
        }
    }
    
    /**
     * Performs the actual world load operation.
     * @param saveName The name of the save to load
     */
    private void performWorldLoad(String saveName) {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (gameInstance == null) {
            showError(loc.getText("messages.cannot_load_not_initialized"), 
                     loc.getText("error_dialog.load_error"));
            return;
        }
        
        try {
            boolean isMultiplayer = isCurrentlyMultiplayer();
            
            // Load the world save data
            WorldSaveData saveData = WorldSaveManager.loadWorld(saveName, isMultiplayer);
            
            if (saveData != null) {
                System.out.println("World save data loaded: " + saveName);
                System.out.println("World seed: " + saveData.getWorldSeed());
                System.out.println("Trees: " + saveData.getTrees().size());
                System.out.println("Items: " + saveData.getItems().size());
                
                // Restore the world state in the game
                boolean success = gameInstance.restoreWorldState(saveData);
                
                if (success) {
                    // Update player position and health from save data
                    if (player != null) {
                        player.setPosition(saveData.getPlayerX(), saveData.getPlayerY());
                        player.setHealth(saveData.getPlayerHealth());
                    }
                    
                    // World loaded successfully - no confirmation dialog needed, just continue
                    System.out.println("World '" + saveName + "' loaded successfully");
                } else {
                    showError(loc.getText("messages.failed_restore_state"), 
                             loc.getText("error_dialog.load_error"));
                    System.err.println("World state restoration failed for: " + saveName);
                }
            } else {
                showError(loc.getText("messages.load_failed"), 
                         loc.getText("error_dialog.load_error"));
            }
        } catch (Exception e) {
            System.err.println("Error loading world: " + e.getMessage());
            showError(loc.getText("messages.load_error", e.getMessage()), 
                     loc.getText("error_dialog.load_error"));
        }
    }
    
    /**
     * Checks if world save functionality is allowed based on current game mode.
     * @return true if world save is allowed, false otherwise
     */
    private boolean isWorldSaveAllowed() {
        if (gameInstance == null) {
            return true; // Default to allowing save if game instance not set
        }
        
        wagemaker.uk.gdx.MyGdxGame.GameMode currentMode = gameInstance.getGameMode();
        
        // Allow save in singleplayer mode
        if (currentMode == wagemaker.uk.gdx.MyGdxGame.GameMode.SINGLEPLAYER) {
            return true;
        }
        
        // Allow save when hosting multiplayer (server mode)
        if (currentMode == wagemaker.uk.gdx.MyGdxGame.GameMode.MULTIPLAYER_HOST) {
            return true;
        }
        
        // Disable save for multiplayer clients
        if (currentMode == wagemaker.uk.gdx.MyGdxGame.GameMode.MULTIPLAYER_CLIENT) {
            return false;
        }
        
        // Default to false for any unknown modes
        return false;
    }
    
    /**
     * Handles selection in the multiplayer menu.
     */
    private void handleMultiplayerMenuSelection() {
        int selectedIndex = multiplayerMenu.getSelectedIndex();
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (selectedIndex == 0) { // Host Server
            multiplayerMenu.close();
            if (gameInstance != null) {
                gameInstance.attemptHostServer();
            } else {
                System.err.println("Cannot host server: game instance not set");
            }
        } else if (selectedIndex == 1) { // Connect to Server
            multiplayerMenu.close();
            
            // Load PlayerConfig and pre-fill the saved server address
            PlayerConfig config = PlayerConfig.load();
            String lastServer = config.getLastServer();
            
            // Show dialog with pre-filled address (or empty if no last server)
            connectDialog.show(lastServer);
        } else if (selectedIndex == 2) { // Back
            multiplayerMenu.close();
            isOpen = true; // Return to main menu
        }
    }
    
    /**
     * Handles selection in the player profile menu.
     */
    private void handlePlayerProfileMenuSelection() {
        int selectedIndex = playerProfileMenu.getSelectedIndex();
        LocalizationManager loc = LocalizationManager.getInstance();
        
        if (selectedIndex == 0) { // Player Name
            playerProfileMenu.close();
            nameDialogFromProfile = true; // Track that we came from player profile
            openNameDialog();
        } else if (selectedIndex == 1) { // Player Controls
            playerProfileMenu.close();
            openControlsDialogFromProfile();
        } else if (selectedIndex == 2) { // Player Location
            playerProfileMenu.close();
            openPlayerLocationDialogFromProfile();
        } else if (selectedIndex == 3) { // Choose Character
            // Character selection is handled by PlayerProfileMenu itself
            // Do nothing here - the dialog opens from PlayerProfileMenu.handleMenuSelection()
        } else if (selectedIndex == 4) { // Save Player
            if (!FreeWorldManager.isFreeWorldActive()) {
                savePlayerPosition();
                showSaveNotification = true;
                saveNotificationTimer = SAVE_NOTIFICATION_DURATION;
            }
            // Stay in Player Profile menu
        } else if (selectedIndex == 5) { // Menu Font
            playerProfileMenu.close();
            openFontSelectionDialog();
        } else if (selectedIndex == 6) { // Language
            playerProfileMenu.close();
            openLanguageDialog();
        } else if (selectedIndex == 7) { // Back
            playerProfileMenu.close();
            isOpen = true; // Return to main menu
        }
    }
    
    /**
     * Shows an error dialog with the specified message.
     * @param message The error message to display
     */
    public void showError(String message) {
        LocalizationManager loc = LocalizationManager.getInstance();
        
        // Determine appropriate title based on message content
        String title = loc.getText("error_dialog.title");
        if (message.toLowerCase().contains("connect") || message.toLowerCase().contains("server")) {
            title = loc.getText("error_dialog.connection_error");
        } else if (message.toLowerCase().contains("save") || message.toLowerCase().contains("load")) {
            title = loc.getText("error_dialog.title");
        }
        
        errorDialog.show(message, title);
    }
    
    /**
     * Shows an error dialog with the specified message and custom title.
     * @param message The error message to display
     * @param title The title for the dialog
     */
    public void showError(String message, String title) {
        errorDialog.show(message, title);
    }
    
    /**
     * Shows a success dialog with the specified message and title.
     * @param message The success message to display
     * @param title The title for the dialog
     */
    public void showSuccess(String message, String title) {
        errorDialog.showSuccess(message, title);
    }
    
    /**
     * Returns to the multiplayer menu.
     */
    public void returnToMultiplayerMenu() {
        isOpen = false;
        multiplayerMenu.open();
    }
    
    public void savePlayerPosition() {
        if (FreeWorldManager.isFreeWorldActive()) {
            System.out.println("Save blocked: Free World mode is active");
            return;
        }
        if (player == null) {
            System.out.println("Cannot save: Player reference not set");
            return;
        }
        
        try {
            // Get the appropriate config directory based on OS
            File configDir = getConfigDirectory();
            if (!configDir.exists()) {
                configDir.mkdirs();
            }
            
            File saveFile = new File(configDir, "woodlanders.json");
            
            // Read existing values if file exists
            String lastServer = null;
            Float singleplayerX = null, singleplayerY = null, singleplayerHealth = null, singleplayerHunger = null;
            Float multiplayerX = null, multiplayerY = null, multiplayerHealth = null, multiplayerHunger = null;
            
            if (saveFile.exists()) {
                try {
                    String existingContent = new String(Files.readAllBytes(Paths.get(saveFile.getAbsolutePath())));
                    lastServer = parseJsonString(existingContent, "\"lastServer\":");
                    
                    // Try to read existing positions from object format first
                    try {
                        singleplayerX = parseJsonObjectFloat(existingContent, "\"singleplayerPosition\"", "x");
                        singleplayerY = parseJsonObjectFloat(existingContent, "\"singleplayerPosition\"", "y");
                        singleplayerHealth = parseJsonFloat(existingContent, "\"singleplayerHealth\":");
                        try {
                            singleplayerHunger = parseJsonFloat(existingContent, "\"singleplayerHunger\":");
                        } catch (Exception e) {
                            singleplayerHunger = 0.0f; // Default if not found
                        }
                    } catch (Exception e) {
                        // Fallback to flat format for backwards compatibility
                        try {
                            singleplayerX = parseJsonFloat(existingContent, "\"singleplayerX\":");
                            singleplayerY = parseJsonFloat(existingContent, "\"singleplayerY\":");
                            singleplayerHealth = parseJsonFloat(existingContent, "\"singleplayerHealth\":");
                            try {
                                singleplayerHunger = parseJsonFloat(existingContent, "\"singleplayerHunger\":");
                            } catch (Exception e3) {
                                singleplayerHunger = 0.0f;
                            }
                        } catch (Exception e2) {
                            // Singleplayer position doesn't exist yet
                        }
                    }
                    
                    try {
                        multiplayerX = parseJsonObjectFloat(existingContent, "\"multiplayerPosition\"", "x");
                        multiplayerY = parseJsonObjectFloat(existingContent, "\"multiplayerPosition\"", "y");
                        multiplayerHealth = parseJsonFloat(existingContent, "\"multiplayerHealth\":");
                        try {
                            multiplayerHunger = parseJsonFloat(existingContent, "\"multiplayerHunger\":");
                        } catch (Exception e) {
                            multiplayerHunger = 0.0f; // Default if not found
                        }
                    } catch (Exception e) {
                        // Fallback to flat format for backwards compatibility
                        try {
                            multiplayerX = parseJsonFloat(existingContent, "\"multiplayerX\":");
                            multiplayerY = parseJsonFloat(existingContent, "\"multiplayerY\":");
                            multiplayerHealth = parseJsonFloat(existingContent, "\"multiplayerHealth\":");
                            try {
                                multiplayerHunger = parseJsonFloat(existingContent, "\"multiplayerHunger\":");
                            } catch (Exception e3) {
                                multiplayerHunger = 0.0f;
                            }
                        } catch (Exception e2) {
                            // Multiplayer position doesn't exist yet
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Could not read existing save data: " + e.getMessage());
                }
            }
            
            // Determine which position to update based on current game mode
            boolean isMultiplayer = (gameInstance != null && 
                                    gameInstance.getGameMode() != wagemaker.uk.gdx.MyGdxGame.GameMode.SINGLEPLAYER);
            
            if (isMultiplayer) {
                // Update multiplayer position
                multiplayerX = player.getX();
                multiplayerY = player.getY();
                multiplayerHealth = player.getHealth();
                multiplayerHunger = player.getHunger();
                System.out.println("Saving multiplayer position");
            } else {
                // Update singleplayer position
                singleplayerX = player.getX();
                singleplayerY = player.getY();
                singleplayerHealth = player.getHealth();
                singleplayerHunger = player.getHunger();
                System.out.println("Saving singleplayer position");
            }
            
            // Create JSON content with separate positions for singleplayer and multiplayer
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("{\n");
            jsonBuilder.append(String.format("  \"playerName\": \"%s\",\n", playerName));
            
            // Singleplayer position
            if (singleplayerX != null && singleplayerY != null && singleplayerHealth != null) {
                jsonBuilder.append("  \"singleplayerPosition\": {\n");
                jsonBuilder.append(String.format("    \"x\": %.2f,\n", singleplayerX));
                jsonBuilder.append(String.format("    \"y\": %.2f\n", singleplayerY));
                jsonBuilder.append("  },\n");
                jsonBuilder.append(String.format("  \"singleplayerHealth\": %.1f,\n", singleplayerHealth));
                if (singleplayerHunger != null) {
                    jsonBuilder.append(String.format("  \"singleplayerHunger\": %.1f,\n", singleplayerHunger));
                }
            }
            
            // Singleplayer inventory
            if (inventoryManager != null) {
                wagemaker.uk.inventory.Inventory spInv = inventoryManager.getSingleplayerInventory();
                jsonBuilder.append("  \"singleplayerInventory\": {\n");
                jsonBuilder.append(String.format("    \"apple\": %d,\n", spInv.getAppleCount()));
                jsonBuilder.append(String.format("    \"banana\": %d,\n", spInv.getBananaCount()));
                jsonBuilder.append(String.format("    \"babyBamboo\": %d,\n", spInv.getBambooSaplingCount()));
                jsonBuilder.append(String.format("    \"bambooStack\": %d,\n", spInv.getBambooStackCount()));
                jsonBuilder.append(String.format("    \"babyTree\": %d,\n", spInv.getBabyTreeCount()));
                jsonBuilder.append(String.format("    \"woodStack\": %d,\n", spInv.getWoodStackCount()));
                jsonBuilder.append(String.format("    \"pebble\": %d,\n", spInv.getPebbleCount()));
                jsonBuilder.append(String.format("    \"palmFiber\": %d\n", spInv.getPalmFiberCount()));
                jsonBuilder.append("  },\n");
            }
            
            // Multiplayer position
            if (multiplayerX != null && multiplayerY != null && multiplayerHealth != null) {
                jsonBuilder.append("  \"multiplayerPosition\": {\n");
                jsonBuilder.append(String.format("    \"x\": %.2f,\n", multiplayerX));
                jsonBuilder.append(String.format("    \"y\": %.2f\n", multiplayerY));
                jsonBuilder.append("  },\n");
                jsonBuilder.append(String.format("  \"multiplayerHealth\": %.1f,\n", multiplayerHealth));
                if (multiplayerHunger != null) {
                    jsonBuilder.append(String.format("  \"multiplayerHunger\": %.1f,\n", multiplayerHunger));
                }
            }
            
            // Multiplayer inventory
            if (inventoryManager != null) {
                wagemaker.uk.inventory.Inventory mpInv = inventoryManager.getMultiplayerInventory();
                jsonBuilder.append("  \"multiplayerInventory\": {\n");
                jsonBuilder.append(String.format("    \"apple\": %d,\n", mpInv.getAppleCount()));
                jsonBuilder.append(String.format("    \"banana\": %d,\n", mpInv.getBananaCount()));
                jsonBuilder.append(String.format("    \"babyBamboo\": %d,\n", mpInv.getBambooSaplingCount()));
                jsonBuilder.append(String.format("    \"bambooStack\": %d,\n", mpInv.getBambooStackCount()));
                jsonBuilder.append(String.format("    \"babyTree\": %d,\n", mpInv.getBabyTreeCount()));
                jsonBuilder.append(String.format("    \"woodStack\": %d,\n", mpInv.getWoodStackCount()));
                jsonBuilder.append(String.format("    \"pebble\": %d,\n", mpInv.getPebbleCount()));
                jsonBuilder.append(String.format("    \"palmFiber\": %d\n", mpInv.getPalmFiberCount()));
                jsonBuilder.append("  },\n");
            }
            
            // Include lastServer if it exists
            if (lastServer != null && !lastServer.isEmpty()) {
                jsonBuilder.append(String.format("  \"lastServer\": \"%s\",\n", lastServer));
            }
            
            // Include current language
            String currentLanguage = LocalizationManager.getInstance().getCurrentLanguage();
            jsonBuilder.append(String.format("  \"language\": \"%s\",\n", currentLanguage));
            
            // Include current font
            String currentFont = FontManager.getInstance().getCurrentFontType().getDisplayName();
            jsonBuilder.append(String.format("  \"fontName\": \"%s\",\n", currentFont));
            
            // Include selected character (use pending selection if available, otherwise load from config)
            String selectedCharacter;
            boolean characterWasChanged = false;
            if (pendingCharacterSelection != null) {
                selectedCharacter = pendingCharacterSelection;
                characterWasChanged = true;
                System.out.println("Saving pending character selection: " + selectedCharacter);
                
                // Save to PlayerConfig so it persists across games
                PlayerConfig playerConfig = PlayerConfig.load();
                playerConfig.saveSelectedCharacter(selectedCharacter);
                System.out.println("Saved character to PlayerConfig: " + selectedCharacter);
            } else {
                PlayerConfig playerConfig = PlayerConfig.load();
                selectedCharacter = playerConfig.getSelectedCharacter();
            }
            jsonBuilder.append(String.format("  \"selectedCharacter\": \"%s\",\n", selectedCharacter));
            
            jsonBuilder.append(String.format("  \"savedAt\": \"%s\"\n", new java.util.Date().toString()));
            jsonBuilder.append("}");
            
            // Write to file
            try (FileWriter writer = new FileWriter(saveFile)) {
                writer.write(jsonBuilder.toString());
            }
            
            System.out.println("Game saved to: " + saveFile.getAbsolutePath());
            System.out.println("Player position saved: (" + player.getX() + ", " + player.getY() + ")");
            System.out.println("Player health saved: " + player.getHealth());
            System.out.println("Player hunger saved: " + player.getHunger());
            System.out.println("Player name saved: " + playerName);
            System.out.println("Font saved: " + FontManager.getInstance().getCurrentFontType().getDisplayName());
            System.out.println("Character saved: " + selectedCharacter);
            System.out.println("Mode: " + (isMultiplayer ? "Multiplayer" : "Singleplayer"));
            if (lastServer != null) {
                System.out.println("Last server preserved: " + lastServer);
            }
            
            // If a character was just selected, reload the player sprite immediately
            if (characterWasChanged) {
                System.out.println("Reloading player character sprite to apply changes...");
                player.reloadCharacter();
                
                // If in multiplayer, broadcast character change to other players
                if (gameInstance != null && gameInstance.getGameClient() != null && 
                    gameInstance.getGameClient().isConnected()) {
                    String clientId = gameInstance.getGameClient().getClientId();
                    if (clientId != null) {
                        String playerName = "Player_" + clientId.substring(0, Math.min(8, clientId.length()));
                        wagemaker.uk.network.PlayerInfoMessage playerInfo = 
                            new wagemaker.uk.network.PlayerInfoMessage(
                                clientId,
                                playerName,
                                selectedCharacter
                            );
                        gameInstance.getGameClient().sendMessage(playerInfo);
                        System.out.println("[CLIENT] Broadcasting character change to server: " + selectedCharacter);
                    }
                }
                
                clearPendingCharacterSelection(); // Clear after reloading
            }
            
        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
        }
    }
    
    private File getConfigDirectory() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        
        if (os.contains("win")) {
            // Windows: %APPDATA%/Woodlanders
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                return new File(appData, "Woodlanders");
            } else {
                return new File(userHome, "AppData/Roaming/Woodlanders");
            }
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/Woodlanders
            return new File(userHome, "Library/Application Support/Woodlanders");
        } else {
            // Linux/Unix: ~/.config/woodlanders
            return new File(userHome, ".config/woodlanders");
        }
    }
    


    private Texture createWoodenPlank() {
        Pixmap pixmap = new Pixmap((int)MENU_WIDTH, (int)MENU_HEIGHT, Pixmap.Format.RGBA8888);
        
        pixmap.setColor(0.4f, 0.25f, 0.1f, 1.0f);
        pixmap.fill();
        
        pixmap.setColor(0.3f, 0.18f, 0.08f, 1.0f);
        for (int y = 10; y < MENU_HEIGHT; y += 15) {
            pixmap.drawLine(0, y, (int)MENU_WIDTH, y + 5);
        }
        
        pixmap.setColor(0.2f, 0.12f, 0.05f, 1.0f);
        pixmap.drawRectangle(0, 0, (int)MENU_WIDTH, (int)MENU_HEIGHT);
        pixmap.drawRectangle(2, 2, (int)MENU_WIDTH - 4, (int)MENU_HEIGHT - 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createNameDialogPlank() {
        Pixmap pixmap = new Pixmap((int)NAME_DIALOG_WIDTH, (int)NAME_DIALOG_HEIGHT, Pixmap.Format.RGBA8888);
        
        pixmap.setColor(0.4f, 0.25f, 0.1f, 1.0f);
        pixmap.fill();
        
        pixmap.setColor(0.3f, 0.18f, 0.08f, 1.0f);
        for (int y = 10; y < NAME_DIALOG_HEIGHT; y += 15) {
            pixmap.drawLine(0, y, (int)NAME_DIALOG_WIDTH, y + 5);
        }
        
        pixmap.setColor(0.2f, 0.12f, 0.05f, 1.0f);
        pixmap.drawRectangle(0, 0, (int)NAME_DIALOG_WIDTH, (int)NAME_DIALOG_HEIGHT);
        pixmap.drawRectangle(2, 2, (int)NAME_DIALOG_WIDTH - 4, (int)NAME_DIALOG_HEIGHT - 4);
        
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void renderPlayerNameTag(SpriteBatch batch) {
        if (player == null) return;
        
        batch.begin();
        
        // Calculate position above player (centered)
        // Player sprite is 100x100 pixels as seen in MyGdxGame.java
        float playerCenterX = player.getX() + 50; // 100/2 = 50
        float playerTopY = player.getY() + 100 + 2; // 100 height + 2 pixels above player
        
        // Calculate text width for centering using custom font
        com.badlogic.gdx.graphics.g2d.GlyphLayout layout = new com.badlogic.gdx.graphics.g2d.GlyphLayout();
        layout.setText(playerNameFont, playerName);
        float textWidth = layout.width;
        
        // Center the text above the player
        float textX = playerCenterX - textWidth / 2;
        
        // Render player name using custom font (already has shadow/border built-in)
        playerNameFont.draw(batch, playerName, textX, playerTopY);
        
        batch.end();
    }

    public boolean isOpen() {
        return isOpen;
    }
    
    /**
     * Checks if any menu or dialog is currently open.
     * This includes the main menu, multiplayer menu, name dialog, and all other dialogs.
     * 
     * @return true if any menu or dialog is open, false otherwise
     */
    public boolean isAnyMenuOpen() {
        return isOpen || 
               nameDialogOpen || 
               multiplayerMenu.isOpen() ||
               playerProfileMenu.isOpen() ||
               errorDialog.isVisible() || 
               connectDialog.isVisible() || 
               serverHostDialog.isVisible() ||
               worldSaveDialog.isVisible() ||
               worldLoadDialog.isVisible() ||
               worldManageDialog.isVisible() ||
               languageDialog.isVisible() ||
               fontSelectionDialog.isOpen() ||
               playerLocationDialog.isVisible() ||
               controlsDialog.isVisible();
    }
    
    /**
     * Gets the player name font for rendering text.
     * @return The player name font
     */
    public BitmapFont getFont() {
        return playerNameFont;
    }

    /**
     * Gets the multiplayer menu instance.
     * @return The multiplayer menu
     */
    public MultiplayerMenu getMultiplayerMenu() {
        return multiplayerMenu;
    }
    
    /**
     * Gets the server host dialog instance.
     * @return The server host dialog
     */
    public ServerHostDialog getServerHostDialog() {
        return serverHostDialog;
    }
    
    /**
     * Gets the connect dialog instance.
     * @return The connect dialog
     */
    public ConnectDialog getConnectDialog() {
        return connectDialog;
    }
    
    /**
     * Gets the error dialog instance.
     * @return The error dialog
     */
    public ErrorDialog getErrorDialog() {
        return errorDialog;
    }
    
    /**
     * Gets the world save dialog instance.
     * @return The world save dialog
     */
    public WorldSaveDialog getWorldSaveDialog() {
        return worldSaveDialog;
    }
    
    /**
     * Gets the world load dialog instance.
     * @return The world load dialog
     */
    public WorldLoadDialog getWorldLoadDialog() {
        return worldLoadDialog;
    }
    
    /**
     * Gets the world manage dialog instance.
     * @return The world manage dialog
     */
    public WorldManageDialog getWorldManageDialog() {
        return worldManageDialog;
    }
    
    /**
     * Gets the world save manager instance.
     * @return The world save manager
     */
    public WorldSaveManager getWorldSaveManager() {
        return worldSaveManager;
    }
    
    /**
     * Gets the language dialog instance.
     * @return The language dialog
     */
    public LanguageDialog getLanguageDialog() {
        return languageDialog;
    }

    public void dispose() {
        woodenPlank.dispose();
        if (nameDialogPlank != null) {
            nameDialogPlank.dispose();
        }
        font.dispose();
        // Don't dispose playerNameFont - it's managed by FontManager
        if (multiplayerMenu != null) {
            multiplayerMenu.dispose();
        }
        if (playerProfileMenu != null) {
            playerProfileMenu.dispose();
        }
        if (serverHostDialog != null) {
            serverHostDialog.dispose();
        }
        if (connectDialog != null) {
            connectDialog.dispose();
        }
        if (errorDialog != null) {
            errorDialog.dispose();
        }
        if (worldSaveDialog != null) {
            worldSaveDialog.dispose();
        }
        if (worldLoadDialog != null) {
            worldLoadDialog.dispose();
        }
        if (worldManageDialog != null) {
            worldManageDialog.dispose();
        }
        if (languageDialog != null) {
            languageDialog.dispose();
        }
        if (playerLocationDialog != null) {
            playerLocationDialog.dispose();
        }
        if (controlsDialog != null) {
            controlsDialog.dispose();
        }
        
        // Unregister from language change listener
        LocalizationManager.getInstance().removeLanguageChangeListener(this);
    }
    
    /**
     * Sets the pending character selection.
     * Called by CharacterSelectionDialog when a character is selected.
     * The selection will be saved when savePlayerPosition() is called.
     * 
     * @param characterFilename The character sprite filename
     */
    public static void setPendingCharacterSelection(String characterFilename) {
        pendingCharacterSelection = characterFilename;
        System.out.println("Pending character selection set to: " + characterFilename);
    }
    
    /**
     * Gets the pending character selection, or null if none is pending.
     * 
     * @return The pending character filename, or null
     */
    public static String getPendingCharacterSelection() {
        return pendingCharacterSelection;
    }
    
    /**
     * Clears the pending character selection after it has been saved.
     */
    private static void clearPendingCharacterSelection() {
        pendingCharacterSelection = null;
    }
}
