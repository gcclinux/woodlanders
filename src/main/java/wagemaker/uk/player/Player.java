package wagemaker.uk.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import wagemaker.uk.items.Apple;
import wagemaker.uk.items.AppleSapling;
import wagemaker.uk.items.BambooSapling;
import wagemaker.uk.items.TreeSapling;
import wagemaker.uk.items.Banana;
import wagemaker.uk.items.BananaSapling;
import wagemaker.uk.items.BambooStack;
import wagemaker.uk.items.PalmFiber;
import wagemaker.uk.items.Pebble;
import wagemaker.uk.items.WoodStack;
import wagemaker.uk.objects.Stone;
import wagemaker.uk.trees.SmallTree;
import wagemaker.uk.trees.AppleTree;
import wagemaker.uk.trees.BambooTree;
import wagemaker.uk.trees.BananaTree;
import wagemaker.uk.trees.CoconutTree;
import wagemaker.uk.trees.Cactus;
import wagemaker.uk.ui.GameMenu;
import wagemaker.uk.network.GameClient;
import wagemaker.uk.inventory.InventoryManager;
import wagemaker.uk.planting.PlantingSystem;
import wagemaker.uk.planting.PlantedBamboo;
import wagemaker.uk.planting.PlantedTree;
import wagemaker.uk.biome.BiomeManager;
import wagemaker.uk.biome.BiomeType;
import wagemaker.uk.targeting.TargetingSystem;
import wagemaker.uk.targeting.TargetIndicatorRenderer;
import wagemaker.uk.targeting.TargetingMode;
import wagemaker.uk.targeting.TargetingCallback;
import wagemaker.uk.targeting.PlantingTargetValidator;
import wagemaker.uk.client.PlayerConfig;
import wagemaker.uk.weather.PuddleCollisionSystem;
import wagemaker.uk.weather.FallAnimationSystem;
import wagemaker.uk.weather.PuddleManager;
import java.util.Map;
import java.util.Random;

public class Player {
    private float x, y;
    private float speed = 200;
    private float animTime = 0;
    private float health = 100; // Player health
    private float lastCactusDamageTime = 0; // To prevent spam damage
    private float previousHealth = 100; // Track previous health for change detection
    
    // Puddle fall damage system fields
    private PuddleCollisionSystem puddleCollisionSystem;
    private FallAnimationSystem fallAnimationSystem;
    private boolean isFalling = false;
    private PuddleManager puddleManager;
    
    // Hunger system fields
    private float hunger = 0; // Hunger level (0-100%)
    private float hungerTimer = 0; // Timer accumulator for hunger increase
    private float previousHunger = 0; // Track previous hunger for change detection
    private static final float HUNGER_INTERVAL = 60.0f; // 60 seconds per 1% hunger increase
    private static final float TREE_ATTACK_HOLD_INTERVAL = 0.25f; // Seconds between hold attacks
    private float treeAttackHoldTimer = 0f;
    
    // Multiplayer fields
    private String playerId; // Unique identifier for multiplayer
    private GameClient gameClient; // Reference for sending network updates
    private boolean isLocalPlayer = true; // Distinguish local vs remote players
    private Map<String, RemotePlayer> remotePlayers; // Reference to remote players for PvP
    private float lastPlayerAttackTime = 0; // Client-side attack cooldown tracking
    private static final float PLAYER_ATTACK_COOLDOWN = 0.5f; // 0.5 seconds between player attacks
    private Texture spriteSheet;
    private Animation<TextureRegion> walkUpAnimation;
    private Animation<TextureRegion> walkLeftAnimation;
    private Animation<TextureRegion> walkDownAnimation;
    private Animation<TextureRegion> walkRightAnimation;
    private Animation<TextureRegion> currentAnimation;
    private TextureRegion idleUpFrame;
    private TextureRegion idleDownFrame;
    private TextureRegion idleLeftFrame;
    private TextureRegion idleRightFrame;
    private OrthographicCamera camera;
    private Map<String, SmallTree> trees;
    private Map<String, AppleTree> appleTrees;
    private Map<String, CoconutTree> coconutTrees;
    private Map<String, BambooTree> bambooTrees;
    private Map<String, BananaTree> bananaTrees;
    private Map<String, Apple> apples;
    private Map<String, AppleSapling> appleSaplings;
    private Map<String, Banana> bananas;
    private Map<String, BananaSapling> bananaSaplings;
    private Map<String, wagemaker.uk.items.BambooStack> bambooStacks;
    private Map<String, wagemaker.uk.items.BambooSapling> bambooSaplings;
    private Map<String, TreeSapling> treeSaplings;
    private Map<String, WoodStack> woodStacks;
    private Map<String, Pebble> pebbles;
    private Map<String, PalmFiber> palmFibers;
    private Random random = new Random();
    private Map<String, Stone> stones;
    private Cactus cactus; // Single cactus reference
    private Object gameInstance; // Reference to MyGdxGame for cactus respawning
    private Map<String, Boolean> clearedPositions;
    private GameMenu gameMenu;
    private InventoryManager inventoryManager;
    
    // Planting system fields
    private PlantingSystem plantingSystem;
    private BiomeManager biomeManager;
    private Map<String, PlantedBamboo> plantedBamboos;
    private Map<String, PlantedTree> plantedTrees;
    private Map<String, wagemaker.uk.planting.PlantedBananaTree> plantedBananaTrees;
    private Map<String, wagemaker.uk.planting.PlantedAppleTree> plantedAppleTrees;
    
    // Targeting system fields
    private TargetingSystem targetingSystem;
    private TargetIndicatorRenderer targetIndicatorRenderer;
    
    // Direction tracking
    private enum Direction { UP, DOWN, LEFT, RIGHT }
    private Direction currentDirection = Direction.DOWN;
    private boolean isMoving = false;
    
    // Navigation mode state management
    private NavigationMode currentNavigationMode = NavigationMode.NORMAL;
    private NavigationMode requestedNavigationMode = NavigationMode.NORMAL;
    
    // Legacy compatibility fields (deprecated - use currentNavigationMode instead)
    private boolean inventoryNavigationMode = false;
    private boolean fenceNavigationMode = false;
    
    // Flag to prevent double B key processing - tracks when building mode was just entered
    private boolean buildingModeJustEntered = false;
    
    // Frame-based stability for fence navigation mode
    private boolean fenceNavigationStable = false;
    private int framesSinceFenceActivation = 0;

    public Player(float startX, float startY, OrthographicCamera camera) {
        this.x = startX;
        this.y = startY;
        this.camera = camera;
        loadAnimations();
        
        // Initialize targeting system
        this.targetingSystem = new TargetingSystem();
        this.targetingSystem.setCamera(camera); // Set camera for mouse input support
        this.targetIndicatorRenderer = new TargetIndicatorRenderer();
        this.targetIndicatorRenderer.initialize();
        
        // Initialize puddle fall damage systems
        this.puddleCollisionSystem = new PuddleCollisionSystem();
        this.fallAnimationSystem = new FallAnimationSystem();
    }
    
    public void setTrees(Map<String, SmallTree> trees) {
        this.trees = trees;
        updateTargetingValidator();
    }
    
    public void setAppleTrees(Map<String, AppleTree> appleTrees) {
        this.appleTrees = appleTrees;
        updateTargetingValidator();
    }
    
    public void setCoconutTrees(Map<String, CoconutTree> coconutTrees) {
        this.coconutTrees = coconutTrees;
        updateTargetingValidator();
    }
    
    public void setBambooTrees(Map<String, BambooTree> bambooTrees) {
        this.bambooTrees = bambooTrees;
        updateTargetingValidator();
    }
    
    public void setBananaTrees(Map<String, BananaTree> bananaTrees) {
        this.bananaTrees = bananaTrees;
        updateTargetingValidator();
    }
    
    public void setApples(Map<String, Apple> apples) {
        this.apples = apples;
    }
    
    public void setAppleSaplings(Map<String, AppleSapling> appleSaplings) {
        this.appleSaplings = appleSaplings;
    }
    
    public void setBananas(Map<String, Banana> bananas) {
        this.bananas = bananas;
    }
    
    public void setBananaSaplings(Map<String, BananaSapling> bananaSaplings) {
        this.bananaSaplings = bananaSaplings;
    }
    
    public void setBambooStacks(Map<String, wagemaker.uk.items.BambooStack> bambooStacks) {
        this.bambooStacks = bambooStacks;
    }
    
    public void setBambooSaplings(Map<String, wagemaker.uk.items.BambooSapling> bambooSaplings) {
        this.bambooSaplings = bambooSaplings;
    }
    
    public void setTreeSaplings(Map<String, TreeSapling> treeSaplings) {
        this.treeSaplings = treeSaplings;
    }
    
    public void setWoodStacks(Map<String, WoodStack> woodStacks) {
        this.woodStacks = woodStacks;
    }
    
    public void setPebbles(Map<String, Pebble> pebbles) {
        this.pebbles = pebbles;
    }
    
    public void setPalmFibers(Map<String, PalmFiber> palmFibers) {
        this.palmFibers = palmFibers;
    }
    
    public void setStones(Map<String, Stone> stones) {
        this.stones = stones;
    }
    
    public void setCactus(Cactus cactus) {
        this.cactus = cactus;
    }
    
    public void setGameInstance(Object gameInstance) {
        this.gameInstance = gameInstance;
    }
    
    public void setClearedPositions(Map<String, Boolean> clearedPositions) {
        this.clearedPositions = clearedPositions;
    }
    
    public void setGameMenu(GameMenu gameMenu) {
        this.gameMenu = gameMenu;
    }
    
    public void setInventoryManager(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        updateTargetingValidator();
    }
    
    public void setPlantingSystem(PlantingSystem plantingSystem) {
        this.plantingSystem = plantingSystem;
    }
    
    public void setBiomeManager(BiomeManager biomeManager) {
        this.biomeManager = biomeManager;
        updateTargetingValidator();
    }
    
    public void setPlantedBamboos(Map<String, PlantedBamboo> plantedBamboos) {
        this.plantedBamboos = plantedBamboos;
        updateTargetingValidator();
    }
    
    public void setPlantedTrees(Map<String, PlantedTree> plantedTrees) {
        this.plantedTrees = plantedTrees;
        updateTargetingValidator();
    }
    
    public void setPlantedBananaTrees(Map<String, wagemaker.uk.planting.PlantedBananaTree> plantedBananaTrees) {
        this.plantedBananaTrees = plantedBananaTrees;
        updateTargetingValidator();
    }
    
    public void setPlantedAppleTrees(Map<String, wagemaker.uk.planting.PlantedAppleTree> plantedAppleTrees) {
        this.plantedAppleTrees = plantedAppleTrees;
        updateTargetingValidator();
    }
    
    public void setPuddleManager(PuddleManager puddleManager) {
        this.puddleManager = puddleManager;
        System.out.println("DEBUG: PuddleManager set on Player - puddle fall damage enabled");
    }
    
    /**
     * Update the targeting system validator with current dependencies.
     * Called whenever planting-related dependencies are updated.
     */
    private void updateTargetingValidator() {
        if (inventoryManager != null && biomeManager != null && 
            plantedBamboos != null && bambooTrees != null) {
            PlantingTargetValidator validator = new PlantingTargetValidator(
                inventoryManager, biomeManager, plantedBamboos, bambooTrees
            );
            
            // Set all tree maps if available
            if (plantedTrees != null && trees != null && appleTrees != null && 
                coconutTrees != null && bananaTrees != null) {
                validator.setTreeMaps(plantedTrees, trees, appleTrees, coconutTrees, bananaTrees);
            }
            
            // Set planted fruit tree maps if available
            if (plantedBananaTrees != null && plantedAppleTrees != null) {
                validator.setPlantedFruitTreeMaps(plantedBananaTrees, plantedAppleTrees);
            }
            
            targetingSystem.setValidator(validator);
        }
    }
    
    // Multiplayer getters and setters
    public String getPlayerId() {
        return playerId;
    }
    
    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
    
    public GameClient getGameClient() {
        return gameClient;
    }
    
    public void setGameClient(GameClient gameClient) {
        this.gameClient = gameClient;
    }
    
    public boolean isLocalPlayer() {
        return isLocalPlayer;
    }
    
    public void setLocalPlayer(boolean isLocalPlayer) {
        this.isLocalPlayer = isLocalPlayer;
    }
    
    public void setRemotePlayers(Map<String, RemotePlayer> remotePlayers) {
        this.remotePlayers = remotePlayers;
    }
    
    /**
     * Find the nearest remote player within attack range (100 pixels).
     * @return The nearest remote player in range, or null if none found
     */
    private RemotePlayer findNearestRemotePlayerInRange() {
        if (remotePlayers == null || remotePlayers.isEmpty()) {
            return null;
        }
        
        RemotePlayer nearestPlayer = null;
        float nearestDistance = Float.MAX_VALUE;
        
        for (RemotePlayer remotePlayer : remotePlayers.values()) {
            if (isPlayerInAttackRange(remotePlayer)) {
                float distance = calculateDistance(remotePlayer);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestPlayer = remotePlayer;
                }
            }
        }
        
        return nearestPlayer;
    }
    
    /**
     * Check if a specific remote player is within attack range (100 pixels).
     * Uses Euclidean distance calculation: sqrt(dx² + dy²)
     * @param remotePlayer The remote player to check
     * @return true if player is within 100 pixels, false otherwise
     */
    private boolean isPlayerInAttackRange(RemotePlayer remotePlayer) {
        if (remotePlayer == null) {
            return false;
        }
        
        float distance = calculateDistance(remotePlayer);
        return distance <= 100; // 100 pixel attack range
    }
    
    /**
     * Calculate Euclidean distance between this player and a remote player.
     * @param remotePlayer The remote player to calculate distance to
     * @return The distance in pixels
     */
    private float calculateDistance(RemotePlayer remotePlayer) {
        // Calculate center positions
        float playerCenterX = x + 32; // Player is 64x64, center is +32
        float playerCenterY = y + 32;
        float remoteCenterX = remotePlayer.getX() + 32;
        float remoteCenterY = remotePlayer.getY() + 32;
        
        // Calculate distance using Euclidean formula: sqrt(dx² + dy²)
        float dx = remoteCenterX - playerCenterX;
        float dy = remoteCenterY - playerCenterY;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public void update(float deltaTime) {
        
        // Update fall animation if active
        if (isFalling) {
            fallAnimationSystem.update(deltaTime);
            
            // Check if sequence complete
            if (fallAnimationSystem.isFallSequenceComplete()) {
                completeFall();
            }
            
            // Skip normal movement processing while falling
            return;
        }
        
        isMoving = false;
        
        // Handle inventory navigation mode toggle (only when menu is not open)
        if (gameMenu != null && !gameMenu.isAnyMenuOpen()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
                toggleInventoryNavigationMode();
            }
            
            // ESC exits inventory mode (if active and menu not open)
            if (isNavigationModeActive(NavigationMode.INVENTORY) && Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                requestNavigationMode(NavigationMode.NORMAL);
                System.out.println("Inventory navigation mode: OFF (ESC pressed, deselected)");
            }
        }
        
        // Exit inventory mode when menu opens
        if (gameMenu != null && gameMenu.isAnyMenuOpen() && isNavigationModeActive(NavigationMode.INVENTORY)) {
            requestNavigationMode(NavigationMode.NORMAL);
            System.out.println("Inventory navigation mode: OFF (menu opened, deselected)");
        }
        
        // Handle fence building mode - fence navigation is always active when building mode is active
        wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
        if (fenceBuildingManager != null && fenceBuildingManager.isBuildingModeActive()) {
            // Only activate fence navigation mode if NOT activated via inventory selection
            if (!fenceBuildingManager.isActivatedViaInventorySelection()) {
                // Ensure fence navigation mode is active when building mode is active
                if (!isNavigationModeActive(NavigationMode.FENCE_BUILDING)) {
                    // If we're in inventory navigation mode, force exit it first to allow fence building
                    if (isNavigationModeActive(NavigationMode.INVENTORY)) {
                        System.out.println("[FenceNav] Forcing exit from inventory navigation to allow fence building");
                        forceNavigationMode(NavigationMode.NORMAL);
                    }
                    
                    boolean accepted = requestNavigationMode(NavigationMode.FENCE_BUILDING);
                    if (accepted) {
                        fenceNavigationStable = false; // Reset stability flag
                        framesSinceFenceActivation = 0; // Reset frame counter
                        buildingModeJustEntered = true; // Mark that we just entered building mode
                        System.out.println("[FenceNav] Fence navigation mode: ON (building mode active)");
                        System.out.println("[FenceNav] State: fenceNavigationMode=" + fenceNavigationMode + 
                                         ", fenceNavigationStable=" + fenceNavigationStable + 
                                         ", framesSinceFenceActivation=" + framesSinceFenceActivation +
                                         ", buildingModeJustEntered=" + buildingModeJustEntered);
                    } else {
                        System.out.println("[FenceNav] Cannot activate fence navigation - higher priority mode is active");
                    }
                }
                
                // Update frame counter and stability
                if (isNavigationModeActive(NavigationMode.FENCE_BUILDING)) {
                    framesSinceFenceActivation++;
                    if (framesSinceFenceActivation >= 2) { // Stable after 2 frames
                        fenceNavigationStable = true;
                        // Clear the buildingModeJustEntered flag once we're stable
                        if (buildingModeJustEntered) {
                            buildingModeJustEntered = false;
                            System.out.println("[FenceNav] Building mode stabilized, cleared buildingModeJustEntered flag");
                        }
                    }
                }
                
                // Handle B key when in building mode - exit both modes
                // Only process B key when fence navigation is stable AND we didn't just enter building mode
                // AND the FenceBuildingManager didn't just activate building mode this frame
                // This prevents double B key processing between FenceBuildingManager and Player
                boolean fenceBuildingJustActivated = fenceBuildingManager.wasBuildingModeJustActivated();
                if (Gdx.input.isKeyJustPressed(Input.Keys.B) && fenceNavigationStable && 
                    !buildingModeJustEntered && !fenceBuildingJustActivated) {
                    System.out.println("[FenceNav] B key pressed - exiting fence navigation and building mode");
                    // Exit fence navigation mode and building mode
                    requestNavigationMode(NavigationMode.NORMAL);
                    // Exit building mode
                    fenceBuildingManager.exitBuildingMode();
                    System.out.println("[FenceNav] Exited fence navigation mode and building mode");
                }
                
                // ESC exits fence navigation mode only (stay in building mode)
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    System.out.println("[FenceNav] ESC key pressed - exiting fence navigation mode only");
                    requestNavigationMode(NavigationMode.NORMAL);
                    System.out.println("[FenceNav] Fence navigation mode: OFF (ESC pressed)");
                }
            } else {
                // When activated via inventory selection, fence building works in background
                // No navigation mode changes, inventory selection remains active
                
                // Handle ESC key to exit fence building when activated via inventory
                if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
                    System.out.println("[FenceNav] ESC key pressed - exiting fence building mode (inventory activated)");
                    fenceBuildingManager.exitBuildingMode();
                    // Also clear inventory selection to exit fence item
                    if (inventoryManager != null) {
                        inventoryManager.clearSelection();
                    }
                    System.out.println("[FenceNav] Fence building mode: OFF (ESC pressed, inventory cleared)");
                }
            }
        } else {
            // Exit fence navigation mode if fence building is no longer active
            if (isNavigationModeActive(NavigationMode.FENCE_BUILDING)) {
                System.out.println("[FenceNav] Building mode deactivated - exiting fence navigation mode");
                requestNavigationMode(NavigationMode.NORMAL);
                System.out.println("[FenceNav] Fence navigation mode: OFF (fence building deactivated)");
            }
        }
        
        // Handle special navigation modes first (higher priority than movement)
        if (gameMenu != null && !gameMenu.isAnyMenuOpen()) {
            switch (currentNavigationMode) {
                case INVENTORY:
                    handleInventoryNavigation();
                    break;
                case FENCE_BUILDING:
                    handleFenceNavigation();
                    break;
                case TARGETING:
                    // Targeting input is handled separately in handleTargetingInput()
                    break;
                case NORMAL:
                    // Normal mode - no special navigation handling needed
                    break;
            }
        }
        
        // Handle targeting input (A/W/D/S when targeting active)
        if (gameMenu != null && !gameMenu.isAnyMenuOpen()) {
            handleTargetingInput();
            handlePlantingAction();
        }
        
        // Track movement in both directions
        boolean movingLeft = false;
        boolean movingRight = false;
        boolean movingUp = false;
        boolean movingDown = false;
        
        // handle input with collision detection
        float newX = x;
        float newY = y;
        
        // Only process movement if no special navigation modes are active
        if (!shouldBlockPlayerMovement()) {
            // Check horizontal movement
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) { 
                float testX = x - speed * deltaTime;
                if (!wouldCollide(testX, y)) {
                    newX = testX;
                    movingLeft = true;
                    isMoving = true;
                }
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) { 
                float testX = x + speed * deltaTime;
                if (!wouldCollide(testX, y)) {
                    newX = testX;
                    movingRight = true;
                    isMoving = true;
                }
            }
            
            // Check vertical movement
            if (Gdx.input.isKeyPressed(Input.Keys.UP)) { 
                float testY = y + speed * deltaTime;
                if (!wouldCollide(newX, testY)) { // Use newX in case of diagonal movement
                    newY = testY;
                    movingUp = true;
                    isMoving = true;
                }
            }
            if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) { 
                float testY = y - speed * deltaTime;
                if (!wouldCollide(newX, testY)) { // Use newX in case of diagonal movement
                    newY = testY;
                    movingDown = true;
                    isMoving = true;
                }
            }
        }
        
        // Apply movement
        x = newX;
        y = newY;
        
        // Determine animation based on movement priority:
        // For diagonal movement, prioritize horizontal direction (LEFT/RIGHT)
        // Only use vertical animations (UP/DOWN) when moving purely vertically
        if (movingLeft) {
            currentDirection = Direction.LEFT;
            currentAnimation = walkLeftAnimation;
        } else if (movingRight) {
            currentDirection = Direction.RIGHT;
            currentAnimation = walkRightAnimation;
        } else if (movingUp) {
            currentDirection = Direction.UP;
            currentAnimation = walkUpAnimation;
        } else if (movingDown) {
            currentDirection = Direction.DOWN;
            currentAnimation = walkDownAnimation;
        }
        
        // Send position updates to server in multiplayer mode (client-side prediction)
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            wagemaker.uk.network.Direction networkDirection = convertToNetworkDirection(currentDirection);
            gameClient.sendPlayerMovement(x, y, networkDirection, isMoving);
        }



        // Handle spacebar - context-sensitive action
        // Priority 1: If targeting is active: plant item at target
        // Priority 2: If item is selected (consumable): consume the item
        // Priority 3: Otherwise: attack nearby targets
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (targetingSystem.isActive()) {
                // Targeting mode: use spacebar to plant
                handleSpacebarPlanting();
            } else if (inventoryManager != null && inventoryManager.getSelectedSlot() != -1) {
                // Item selected: try to consume it
                consumeSelectedItem();
            } else {
                // Normal mode: use spacebar to attack
                attackNearbyTargets(false);
            }
        }

        // Allow holding spacebar to continuously attack trees (but never for planting or consuming)
        boolean menuInactive = gameMenu == null || !gameMenu.isAnyMenuOpen();
        boolean inventoryIdle = inventoryManager == null || inventoryManager.getSelectedSlot() == -1;
        boolean canAutoAttackTrees = menuInactive && inventoryIdle && !targetingSystem.isActive();
        if (canAutoAttackTrees && Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            treeAttackHoldTimer += deltaTime;
            if (treeAttackHoldTimer >= TREE_ATTACK_HOLD_INTERVAL) {
                treeAttackHoldTimer = 0f;
                attackNearbyTargets(true);
            }
        } else {
            treeAttackHoldTimer = 0f;
        }

        // update animation time
        if (isMoving) {
            animTime += deltaTime;
        } else {
            // Reset to first frame when not moving (idle pose)
            animTime = 0;
        }
        
        // Check for puddle collision (only when not falling)
        if (!isFalling && puddleManager != null) {
            java.util.List<wagemaker.uk.weather.WaterPuddle> activePuddles = puddleManager.getActivePuddles();
            
            // Calculate player center (player sprite is 64x64, so center is +32)
            float playerCenterX = x + 32;
            float playerCenterY = y + 32;
            
            wagemaker.uk.weather.PuddleCollisionResult collision = 
                puddleCollisionSystem.checkCollision(playerCenterX, playerCenterY, activePuddles);
            
            if (collision.hasCollision()) {
                triggerFall(collision.getPuddle());
            }
            
            // Update triggered states after normal movement
            puddleCollisionSystem.updateTriggeredStates(playerCenterX, playerCenterY, activePuddles);
        }
        
        // Check for cactus damage
        checkCactusDamage(deltaTime);
        
        // Update hunger system
        updateHunger(deltaTime);
        
        // Check for apple pickups
        checkApplePickups();
        
        // Check for apple sapling pickups
        checkAppleSaplingPickups();
        
        // Check for banana pickups
        checkBananaPickups();
        
        // Check for banana sapling pickups
        checkBananaSaplingPickups();
        
        // Check for bamboo stack pickups
        checkBambooStackPickups();
        
        // Check for baby bamboo pickups
        checkBambooSaplingPickups();
        
        // Check for baby tree pickups
        checkTreeSaplingPickups();
        
        // Check for wood stack pickups
        checkWoodStackPickups();
        
        // Check for pebble pickups
        checkPebblePickups();
        
        // Check for palm fiber pickups
        checkPalmFiberPickups();
        
        // Track previous health for change detection
        previousHealth = health;
        
        // Send health updates to server in multiplayer mode
        checkAndSendHealthUpdate();
        
        // update camera to follow player
        updateCamera();
    }

    /**
     * Reloads the player character sprite based on the current PlayerConfig selection.
     * This can be called at runtime to change the player's appearance.
     * Disposes the old sprite sheet before loading the new one.
     */
    public void reloadCharacter() {
        // Dispose old sprite sheet if it exists
        if (spriteSheet != null) {
            spriteSheet.dispose();
            System.out.println("Disposed old character sprite");
        }
        
        // Reload animations with new character
        loadAnimations();
        System.out.println("Character sprite reloaded successfully");
    }
    
    private void loadAnimations() {
        // Load PlayerConfig to get selected character
        PlayerConfig config = PlayerConfig.load();
        String characterFilename = config.getSelectedCharacter();
        
        // Use default if config is empty or invalid
        if (characterFilename == null || characterFilename.isEmpty()) {
            characterFilename = "boy_navy_start.png";
            System.out.println("No character selected in config, using default: " + characterFilename);
        }
        
        // Load the sprite sheet with error handling
        try {
            spriteSheet = new Texture("sprites/player/" + characterFilename);
            System.out.println("Loaded character sprite: " + characterFilename);
        } catch (Exception e) {
            System.err.println("Error loading character sprite '" + characterFilename + "': " + e.getMessage());
            System.err.println("Falling back to default character: boy_navy_start.png");
            characterFilename = "boy_navy_start.png";
            try {
                spriteSheet = new Texture("sprites/player/" + characterFilename);
            } catch (Exception fallbackError) {
                System.err.println("CRITICAL: Could not load default character sprite: " + fallbackError.getMessage());
                throw new RuntimeException("Failed to load player sprite", fallbackError);
            }
        }
        
        // Get sprite sheet dimensions
        // int spriteSheetHeight = spriteSheet.getHeight();
        
        // Create animation frames for each direction
        TextureRegion[] walkUpFrames = new TextureRegion[9];
        TextureRegion[] walkLeftFrames = new TextureRegion[9];
        TextureRegion[] walkDownFrames = new TextureRegion[9];
        TextureRegion[] walkRightFrames = new TextureRegion[9];
        
        // UP frames: 1st row (Y=512 in LibGDX coordinates)
        // Using coordinates: 0,512 | 64,512 | 128,512 | 192,512 | 256,512 | 320,512 | 384,512 | 448,512 | 512,512
        int upTopY = 512;
        int[] upXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkUpFrames[i] = new TextureRegion(spriteSheet, upXCoords[i], upTopY, 64, 64);
        }
        
        // LEFT frames: 2nd row (Y=576 in LibGDX coordinates)
        // Using coordinates: 0,576 | 64,576 | 128,576 | 192,576 | 256,576 | 320,576 | 384,576 | 448,576 | 512,576
        int leftTopY = 576;
        int[] leftXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkLeftFrames[i] = new TextureRegion(spriteSheet, leftXCoords[i], leftTopY, 64, 64);
        }
        
        // DOWN frames: 3rd row (Y=640 in LibGDX coordinates)
        // Using coordinates: 0,640 | 64,640 | 128,640 | 192,640 | 256,640 | 320,640 | 384,640 | 448,640 | 512,640
        int downTopY = 640;
        int[] downXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkDownFrames[i] = new TextureRegion(spriteSheet, downXCoords[i], downTopY, 64, 64);
        }
        
        // RIGHT frames: 4th row (Y=704 in LibGDX coordinates)
        // Using coordinates: 0,704 | 64,704 | 128,704 | 192,704 | 256,704 | 320,704 | 384,704 | 448,704 | 512,704
        int rightTopY = 704;
        int[] rightXCoords = {0, 64, 128, 192, 256, 320, 384, 448, 512};
        for (int i = 0; i < 9; i++) {
            walkRightFrames[i] = new TextureRegion(spriteSheet, rightXCoords[i], rightTopY, 64, 64);
        }
        
        // Create animations (0.1f = 100ms per frame, gives smooth 10 FPS animation)
        walkUpAnimation = new Animation<>(0.1f, walkUpFrames);
        walkLeftAnimation = new Animation<>(0.1f, walkLeftFrames);
        walkDownAnimation = new Animation<>(0.1f, walkDownFrames);
        walkRightAnimation = new Animation<>(0.1f, walkRightFrames);
        
        // Set all animations to loop
        walkUpAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkLeftAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkDownAnimation.setPlayMode(Animation.PlayMode.LOOP);
        walkRightAnimation.setPlayMode(Animation.PlayMode.LOOP);
        
        // Create directional idle frames (first frame of each animation)
        idleUpFrame = new TextureRegion(spriteSheet, 0, 512, 64, 64);    // First UP frame
        idleLeftFrame = new TextureRegion(spriteSheet, 0, 576, 64, 64);  // First LEFT frame
        idleDownFrame = new TextureRegion(spriteSheet, 0, 640, 64, 64);  // First DOWN frame
        idleRightFrame = new TextureRegion(spriteSheet, 0, 704, 64, 64); // First RIGHT frame
        
        // Set default animation to LEFT (Row 3 - character facing camera/standing still)
        currentAnimation = walkLeftAnimation;
    }

    public TextureRegion getCurrentFrame() {
        // Check if falling and return fall animation frame
        if (isFalling) {
            return fallAnimationSystem.getCurrentFallFrame();
        }
        
        // Otherwise return normal animation frame
        if (isMoving) {
            return currentAnimation.getKeyFrame(animTime);
        } else {
            // Return directional idle frame based on last movement direction
            switch (currentDirection) {
                case UP:
                    return idleUpFrame;
                case DOWN:
                    return idleDownFrame;
                case LEFT:
                    return idleLeftFrame;
                case RIGHT:
                    return idleRightFrame;
                default:
                    return idleDownFrame; // Fallback
            }
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    private void updateCamera() {
        // Center camera on player (infinite world)
        float cameraX = x + 32; // player center
        float cameraY = y + 32; // player center
        
        camera.position.set(cameraX, cameraY, 0);
    }
    
    /**
     * Checks if a location is valid for item spawning.
     * Items should not spawn in water biomes.
     * 
     * @param x The x-coordinate to check
     * @param y The y-coordinate to check
     * @return true if location is valid for item spawning (not water), false otherwise
     * 
     * Requirements: 3.3 (item spawn validation)
     */
    private boolean isValidItemSpawnLocation(float x, float y) {
        if (biomeManager != null && biomeManager.isInitialized()) {
            BiomeType biomeType = biomeManager.getBiomeAtPosition(x, y);
            // Items should not spawn in water
            return biomeType != BiomeType.WATER;
        }
        // If biome manager not available, allow spawn (backward compatibility)
        return true;
    }
    
    /**
     * Finds a valid spawn location for an item, with retry logic if the original location is in water.
     * Attempts to find an alternative location within a 100px radius if the original location is invalid.
     * 
     * @param originalX The original x-coordinate
     * @param originalY The original y-coordinate
     * @param itemId Unique identifier for the item (used for deterministic retry)
     * @return A float array [x, y] with valid spawn coordinates, or null if no valid location found
     * 
     * Requirements: 3.3 (item spawn validation), 3.5 (alternative location selection)
     */
    private float[] findValidItemSpawnLocation(float originalX, float originalY, String itemId) {
        // Check if original location is valid
        if (isValidItemSpawnLocation(originalX, originalY)) {
            return new float[]{originalX, originalY};
        }
        
        // Original location is in water, try to find alternative location
        Random retryRandom = new Random(itemId.hashCode()); // Deterministic based on item ID
        int maxRetries = 100;
        
        for (int attempt = 0; attempt < maxRetries; attempt++) {
            // Try random offset within 100px radius (smaller than trees since items are smaller)
            float angle = retryRandom.nextFloat() * 2 * (float) Math.PI;
            float distance = retryRandom.nextFloat() * 100f;
            float offsetX = (float) Math.cos(angle) * distance;
            float offsetY = (float) Math.sin(angle) * distance;
            
            float candidateX = originalX + offsetX;
            float candidateY = originalY + offsetY;
            
            if (isValidItemSpawnLocation(candidateX, candidateY)) {
                System.out.println("[ItemSpawn] Found alternative location for item " + itemId + 
                                 " at (" + candidateX + ", " + candidateY + ") after " + (attempt + 1) + " attempts");
                return new float[]{candidateX, candidateY};
            }
        }
        
        System.out.println("[ItemSpawn] WARNING: Could not find valid location for item " + itemId + 
                         " - original location in water and no alternative found after " + maxRetries + " attempts");
        return null; // No valid location found
    }

    private boolean wouldCollide(float newX, float newY) {
        // Check collision with water biomes
        if (biomeManager != null && biomeManager.isInitialized()) {
            // Check player center position (player is 64x64, center is +32)
            float playerCenterX = newX + 32;
            float playerCenterY = newY + 32;
            
            BiomeType biomeAtPosition = biomeManager.getBiomeAtPosition(
                playerCenterX, 
                playerCenterY
            );
            
            if (biomeAtPosition == BiomeType.WATER) {
                return true; // Block movement into water
            }
        }
        
        // Check collision with regular trees
        if (trees != null) {
            for (SmallTree tree : trees.values()) {
                if (tree.collidesWith(newX, newY, 64, 64)) {
                    return true;
                }
            }
        }
        
        // Check collision with apple trees
        if (appleTrees != null) {
            for (AppleTree appleTree : appleTrees.values()) {
                if (appleTree.collidesWith(newX, newY, 64, 64)) {
                    return true;
                }
            }
        }
        
        // Check collision with coconut trees
        if (coconutTrees != null) {
            for (CoconutTree coconutTree : coconutTrees.values()) {
                if (coconutTree.collidesWith(newX, newY, 64, 64)) {
                    return true;
                }
            }
        }
        
        // Check collision with bamboo trees
        if (bambooTrees != null) {
            for (BambooTree bambooTree : bambooTrees.values()) {
                if (bambooTree.collidesWith(newX, newY, 64, 64)) {
                    return true;
                }
            }
        }
        
        // Check collision with banana trees
        if (bananaTrees != null) {
            for (BananaTree bananaTree : bananaTrees.values()) {
                if (bananaTree.collidesWith(newX, newY, 64, 64)) {
                    return true;
                }
            }
        }
        
        // Check collision with cactus
        if (cactus != null) {
            if (cactus.collidesWith(newX, newY, 64, 64)) {
                return true;
            }
        }
        
        // Check collision with stones
        if (stones != null) {
            for (Stone stone : stones.values()) {
                if (stone.collidesWith(newX, newY, 64, 64)) {
                    return true;
                }
            }
        }
        
        // Check collision with fences using standard collision detection
        wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
        if (fenceBuildingManager != null) {
            // Use standard player collision rectangle (collision boundaries are now shifted left)
            com.badlogic.gdx.math.Rectangle playerRect = new com.badlogic.gdx.math.Rectangle(newX, newY, 64, 64);
            if (fenceBuildingManager.checkFenceCollision(playerRect)) {
                return true;
            }
        }
        
        return false;
    }
    

    
    /**
     * Check if any special navigation mode is currently active.
     * Special navigation modes include inventory navigation and fence navigation.
     * @return true if any special navigation mode is active, false otherwise
     */
    private boolean isSpecialNavigationActive() {
        return inventoryNavigationMode || fenceNavigationMode;
    }
    
    /**
     * Determine if player movement should be blocked due to active navigation modes.
     * Movement is blocked when special navigation modes are active or targeting is active.
     * @return true if player movement should be blocked, false otherwise
     */
    private boolean shouldBlockPlayerMovement() {
        return currentModeBlocksMovement() || (targetingSystem != null && targetingSystem.isActive());
    }
    
    /**
     * Get the current navigation mode.
     * @return The currently active navigation mode
     */
    public NavigationMode getCurrentNavigationMode() {
        return currentNavigationMode;
    }
    
    /**
     * Request a navigation mode change with priority validation and exclusivity logic.
     * Higher priority modes can override lower priority modes.
     * Lower priority modes cannot override higher priority modes.
     * Only one special navigation mode can be active at a time.
     * 
     * @param requestedMode The navigation mode to activate
     * @return true if the mode change was accepted, false if rejected due to priority or exclusivity
     */
    public boolean requestNavigationMode(NavigationMode requestedMode) {
        if (requestedMode == null) {
            requestedMode = NavigationMode.NORMAL;
        }
        
        // If requesting the same mode, no change needed
        if (requestedMode == currentNavigationMode) {
            return true;
        }
        
        // Check mode exclusivity rules
        if (!isNavigationModeChangeAllowed(requestedMode)) {
            System.out.println("[NavMode] Mode change blocked by exclusivity rules: " + 
                             requestedMode.getDescription() + " cannot be activated while " + 
                             currentNavigationMode.getDescription() + " is active");
            return false;
        }
        
        // Check if the requested mode has higher or equal priority
        if (requestedMode.hasHigherPriorityThan(currentNavigationMode) || requestedMode == NavigationMode.NORMAL) {
            setNavigationMode(requestedMode);
            System.out.println("[NavMode] Mode change accepted: " + currentNavigationMode.getDescription() + 
                             " (priority " + currentNavigationMode.getPriority() + ")");
            return true;
        } else {
            System.out.println("[NavMode] Mode change rejected: " + requestedMode.getDescription() + 
                             " (priority " + requestedMode.getPriority() + ") cannot override " + 
                             currentNavigationMode.getDescription() + " (priority " + currentNavigationMode.getPriority() + ")");
            return false;
        }
    }
    
    /**
     * Check if a navigation mode change is allowed based on exclusivity rules.
     * Implements the core exclusivity logic for navigation modes.
     * 
     * @param requestedMode The mode being requested
     * @return true if the mode change is allowed, false if blocked by exclusivity rules
     */
    private boolean isNavigationModeChangeAllowed(NavigationMode requestedMode) {
        // NORMAL mode can always be activated (it's the default/exit mode)
        if (requestedMode == NavigationMode.NORMAL) {
            return true;
        }
        
        // TARGETING mode can override any other mode (highest priority)
        if (requestedMode == NavigationMode.TARGETING) {
            return true;
        }
        
        // Special navigation modes (INVENTORY, FENCE_BUILDING) are mutually exclusive
        // They cannot be activated when another special mode is active
        if (requestedMode == NavigationMode.INVENTORY || requestedMode == NavigationMode.FENCE_BUILDING) {
            // Can only activate if currently in NORMAL mode or the same mode
            return currentNavigationMode == NavigationMode.NORMAL || currentNavigationMode == requestedMode;
        }
        
        return true;
    }
    
    /**
     * Force a navigation mode change without priority validation.
     * Use with caution - this bypasses the priority system.
     * 
     * @param mode The navigation mode to activate
     */
    public void forceNavigationMode(NavigationMode mode) {
        if (mode == null) {
            mode = NavigationMode.NORMAL;
        }
        setNavigationMode(mode);
        System.out.println("[NavMode] Mode forced: " + currentNavigationMode.getDescription());
    }
    
    /**
     * Internal method to set the navigation mode and update legacy compatibility fields.
     * Also handles cleanup when exiting modes.
     * 
     * @param mode The navigation mode to set
     */
    private void setNavigationMode(NavigationMode mode) {
        NavigationMode previousMode = currentNavigationMode;
        currentNavigationMode = mode;
        
        // Update legacy compatibility fields
        inventoryNavigationMode = (mode == NavigationMode.INVENTORY);
        fenceNavigationMode = (mode == NavigationMode.FENCE_BUILDING);
        
        // Handle mode transition cleanup
        if (previousMode != currentNavigationMode) {
            handleModeTransition(previousMode, currentNavigationMode);
        }
    }
    
    /**
     * Handle cleanup and initialization when transitioning between navigation modes.
     * 
     * @param fromMode The previous navigation mode
     * @param toMode The new navigation mode
     */
    private void handleModeTransition(NavigationMode fromMode, NavigationMode toMode) {
        System.out.println("[NavMode] Transitioning from " + fromMode.getDescription() + 
                         " to " + toMode.getDescription());
        
        // Cleanup for exiting modes
        switch (fromMode) {
            case INVENTORY:
                if (inventoryManager != null) {
                    inventoryManager.clearSelection();
                }
                break;
            case FENCE_BUILDING:
                wagemaker.uk.ui.FenceItemRenderer fenceItemRenderer = getFenceItemRenderer();
                if (fenceItemRenderer != null) {
                    fenceItemRenderer.setFenceSelectionActive(false);
                }
                // Deactivate targeting when exiting fence building mode
                if (targetingSystem != null && targetingSystem.isActive()) {
                    targetingSystem.deactivate();
                }
                // Re-enable direct mouse input when exiting fence building
                wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
                if (fenceBuildingManager != null) {
                    fenceBuildingManager.setDirectMouseInputDisabled(false);
                }
                // Reset fence navigation stability flags
                fenceNavigationStable = false;
                framesSinceFenceActivation = 0;
                buildingModeJustEntered = false;
                break;
            case TARGETING:
                if (targetingSystem != null && targetingSystem.isActive()) {
                    targetingSystem.deactivate();
                }
                // Re-enable direct mouse input when exiting targeting mode
                wagemaker.uk.fence.FenceBuildingManager fenceManager = getFenceBuildingManager();
                if (fenceManager != null) {
                    fenceManager.setDirectMouseInputDisabled(false);
                }
                break;
        }
        
        // Initialization for entering modes
        switch (toMode) {
            case INVENTORY:
                if (inventoryManager != null) {
                    inventoryManager.setSelectedSlot(0);
                    updateTargetingForSelection(0);
                }
                break;
            case FENCE_BUILDING:
                wagemaker.uk.ui.FenceItemRenderer fenceItemRenderer = getFenceItemRenderer();
                if (fenceItemRenderer != null) {
                    fenceItemRenderer.setFenceSelectionActive(true);
                    fenceItemRenderer.setSelectedFencePieceIndex(0);
                }
                activateFenceTargeting();
                break;
            case NORMAL:
                // Restore original planting validator when returning to normal mode
                updateTargetingValidator();
                break;
        }
    }
    
    /**
     * Check if the current navigation mode blocks player movement.
     * @return true if the current mode blocks player movement
     */
    public boolean currentModeBlocksMovement() {
        return currentNavigationMode.blocksPlayerMovement();
    }
    
    /**
     * Check if a specific navigation mode is currently active.
     * @param mode The navigation mode to check
     * @return true if the specified mode is currently active
     */
    public boolean isNavigationModeActive(NavigationMode mode) {
        return currentNavigationMode == mode;
    }
    
    /**
     * Check if any special navigation mode is currently active.
     * Special modes are any mode other than NORMAL.
     * @return true if a special navigation mode is active
     */
    public boolean isAnySpecialNavigationModeActive() {
        return currentNavigationMode != NavigationMode.NORMAL;
    }
    
    /**
     * Check if inventory navigation can be activated.
     * Inventory navigation is blocked when fence building or targeting is active.
     * @return true if inventory navigation can be activated
     */
    public boolean canActivateInventoryNavigation() {
        return currentNavigationMode == NavigationMode.NORMAL || currentNavigationMode == NavigationMode.INVENTORY;
    }
    
    /**
     * Check if fence building navigation can be activated.
     * Fence building navigation is blocked when inventory navigation or targeting is active.
     * @return true if fence building navigation can be activated
     */
    public boolean canActivateFenceNavigation() {
        return currentNavigationMode == NavigationMode.NORMAL || currentNavigationMode == NavigationMode.FENCE_BUILDING;
    }
    
    /**
     * Get a description of why a navigation mode change was rejected.
     * @param requestedMode The mode that was requested
     * @return A human-readable explanation of why the mode change was rejected
     */
    public String getNavigationModeRejectionReason(NavigationMode requestedMode) {
        if (requestedMode == currentNavigationMode) {
            return "Mode is already active";
        }
        
        if (!isNavigationModeChangeAllowed(requestedMode)) {
            return "Mode blocked by exclusivity rules - " + currentNavigationMode.getDescription() + " is active";
        }
        
        if (!requestedMode.hasHigherPriorityThan(currentNavigationMode) && requestedMode != NavigationMode.NORMAL) {
            return "Mode has lower priority than current mode (" + currentNavigationMode.getDescription() + ")";
        }
        
        return "Unknown reason";
    }
    
    public void setPosition(float newX, float newY) {
        this.x = newX;
        this.y = newY;
    }

    private void attackNearbyTargets(boolean treeOnly) {
        if (!treeOnly) {
            // Priority 1: Check for remote players in range
            RemotePlayer targetPlayer = findNearestRemotePlayerInRange();
            if (targetPlayer != null && gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                // TODO: Cooldown for player attacks (currently disabled for testing)
                // Check cooldown for player attacks only (0.5 seconds since last player attack)
                // float currentTime = System.currentTimeMillis() / 1000.0f; // Convert to seconds
                // if (currentTime - lastPlayerAttackTime < PLAYER_ATTACK_COOLDOWN) {
                //     // Still on cooldown for player attacks
                //     System.out.println("Player attack on cooldown");
                //     return;
                // }
                
                // Attack the remote player
                String targetPlayerId = targetPlayer.getPlayerId();
                gameClient.sendAttackAction(targetPlayerId);
                // lastPlayerAttackTime = currentTime; // Disabled with cooldown
                System.out.println("Attacking player: " + targetPlayerId);
                return; // Don't attack trees if we attacked a player
            }
        }
        
        // Priority 2: Attack trees if no players in range (no cooldown for trees)
        boolean attackedSomething = false;
        
        // Attack trees within range (individual collision)
        if (trees != null) {
            SmallTree targetTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, SmallTree> entry : trees.entrySet()) {
                SmallTree tree = entry.getValue();
                
                if (tree.isInAttackRange(x, y)) {
                    targetTree = tree;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetTree != null) {
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles tree destruction
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
                    float healthBefore = targetTree.getHealth();
                    boolean destroyed = targetTree.attack();
                    if (destroyed) {
                        System.out.println("Attacking tree, health before: " + healthBefore);
                        System.out.println("Tree health after attack: " + targetTree.getHealth() + ", destroyed: " + destroyed);
                    }
                    attackedSomething = true;
                    
                    if (destroyed) {
                        // Random drop selection (0, 1, or 2)
                        int dropType = random.nextInt(3);
                        float treeX = targetTree.getX();
                        float treeY = targetTree.getY();
                        
                        // Validate spawn locations and find alternatives if needed
                        float[] location1 = findValidItemSpawnLocation(treeX, treeY, targetKey + "-item1");
                        float[] location2 = findValidItemSpawnLocation(treeX + 8, treeY, targetKey + "-item2");
                        
                        switch (dropType) {
                            case 0: // 2x TreeSapling
                                if (location1 != null) {
                                    treeSaplings.put(targetKey + "-item1", new TreeSapling(location1[0], location1[1]));
                                    System.out.println("Dropped TreeSapling at: " + location1[0] + ", " + location1[1]);
                                }
                                if (location2 != null) {
                                    treeSaplings.put(targetKey + "-item2", new TreeSapling(location2[0], location2[1]));
                                    System.out.println("Dropped TreeSapling at: " + location2[0] + ", " + location2[1]);
                                }
                                break;
                            case 1: // 2x WoodStack
                                if (location1 != null) {
                                    woodStacks.put(targetKey + "-item1", new WoodStack(location1[0], location1[1]));
                                    System.out.println("Dropped WoodStack at: " + location1[0] + ", " + location1[1]);
                                }
                                if (location2 != null) {
                                    woodStacks.put(targetKey + "-item2", new WoodStack(location2[0], location2[1]));
                                    System.out.println("Dropped WoodStack at: " + location2[0] + ", " + location2[1]);
                                }
                                break;
                            case 2: // 1x TreeSapling + 1x WoodStack
                                if (location1 != null) {
                                    treeSaplings.put(targetKey + "-item1", new TreeSapling(location1[0], location1[1]));
                                    System.out.println("Dropped TreeSapling at: " + location1[0] + ", " + location1[1]);
                                }
                                if (location2 != null) {
                                    woodStacks.put(targetKey + "-item2", new WoodStack(location2[0], location2[1]));
                                    System.out.println("Dropped WoodStack at: " + location2[0] + ", " + location2[1]);
                                }
                                break;
                        }
                        
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.TREE,
                                    targetTree.getX(),
                                    targetTree.getY(),
                                    wagemaker.uk.network.TreeType.SMALL
                                );
                            }
                        }
                        
                        targetTree.dispose();
                        trees.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                        System.out.println("Tree removed from world");
                    }
                }
            }
        }
        
        // Attack apple trees within range (individual collision)
        if (appleTrees != null && !attackedSomething) {
            AppleTree targetAppleTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, AppleTree> entry : appleTrees.entrySet()) {
                AppleTree appleTree = entry.getValue();
                
                if (appleTree.isInAttackRange(x, y)) {
                    targetAppleTree = appleTree;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetAppleTree != null) {
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles tree destruction, healing, and item spawning
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
                    float healthBefore = targetAppleTree.getHealth();
                    boolean destroyed = targetAppleTree.attack();
                    if (destroyed) {
                        System.out.println("Attacking apple tree, health before: " + healthBefore);
                        System.out.println("Apple tree health after attack: " + targetAppleTree.getHealth() + ", destroyed: " + destroyed);
                    }
                    attackedSomething = true;
                    
                    if (destroyed) {
                        // Validate spawn locations and find alternatives if needed
                        float[] appleLocation = findValidItemSpawnLocation(targetAppleTree.getX(), targetAppleTree.getY(), targetKey);
                        float[] saplingLocation = findValidItemSpawnLocation(targetAppleTree.getX() + 8, targetAppleTree.getY(), targetKey + "-applesapling");
                        
                        // Spawn Apple at validated position
                        if (appleLocation != null) {
                            apples.put(targetKey, new Apple(appleLocation[0], appleLocation[1]));
                            System.out.println("Apple tree destroyed! Apple dropped at: " + appleLocation[0] + ", " + appleLocation[1]);
                        }
                        
                        // Spawn AppleSapling at validated position
                        if (saplingLocation != null) {
                            appleSaplings.put(targetKey + "-applesapling", 
                                new AppleSapling(saplingLocation[0], saplingLocation[1]));
                            System.out.println("AppleSapling dropped at: " + saplingLocation[0] + ", " + saplingLocation[1]);
                        }
                        
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.TREE,
                                    targetAppleTree.getX(),
                                    targetAppleTree.getY(),
                                    wagemaker.uk.network.TreeType.APPLE
                                );
                            }
                        }
                        
                        targetAppleTree.dispose();
                        appleTrees.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                    }
                }
            }
        }
        
        // Attack coconut trees within range (individual collision)
        if (coconutTrees != null && !attackedSomething) {
            CoconutTree targetCoconutTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, CoconutTree> entry : coconutTrees.entrySet()) {
                CoconutTree coconutTree = entry.getValue();
                
                if (coconutTree.isInAttackRange(x, y)) {
                    targetCoconutTree = coconutTree;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetCoconutTree != null) {
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles tree destruction
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
                    float healthBefore = targetCoconutTree.getHealth();
                    boolean destroyed = targetCoconutTree.attack();
                    if (destroyed) {
                        System.out.println("Attacking coconut tree, health before: " + healthBefore);
                        System.out.println("Coconut tree health after attack: " + targetCoconutTree.getHealth() + ", destroyed: " + destroyed);
                    }
                    attackedSomething = true;
                    
                    if (destroyed) {
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.TREE,
                                    targetCoconutTree.getX(),
                                    targetCoconutTree.getY(),
                                    wagemaker.uk.network.TreeType.COCONUT
                                );
                            }
                        }
                        
                        // Validate spawn location and find alternative if needed
                        float[] palmFiberLocation = findValidItemSpawnLocation(targetCoconutTree.getX(), targetCoconutTree.getY(), targetKey);
                        
                        // Spawn palm fiber at validated position
                        if (palmFiberLocation != null) {
                            palmFibers.put(targetKey, new PalmFiber(palmFiberLocation[0], palmFiberLocation[1]));
                            System.out.println("PalmFiber dropped at: " + palmFiberLocation[0] + ", " + palmFiberLocation[1]);
                        }
                        
                        targetCoconutTree.dispose();
                        coconutTrees.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                        System.out.println("Coconut tree removed from world");
                    }
                }
            }
        }
        
        // Attack bamboo trees within range (individual collision)
        if (bambooTrees != null && !attackedSomething) {
            BambooTree targetBambooTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, BambooTree> entry : bambooTrees.entrySet()) {
                BambooTree bambooTree = entry.getValue();
                
                if (bambooTree.isInAttackRange(x, y)) {
                    targetBambooTree = bambooTree;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetBambooTree != null) {
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles tree destruction
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
                    float healthBefore = targetBambooTree.getHealth();
                    boolean destroyed = targetBambooTree.attack();
                    if (destroyed) {
                        System.out.println("Attacking bamboo tree, health before: " + healthBefore);
                        System.out.println("Bamboo tree health after attack: " + targetBambooTree.getHealth() + ", destroyed: " + destroyed);
                    }
                    attackedSomething = true;
                    
                    if (destroyed) {
                        // Randomly choose drop pattern: 
                        // 33% chance: 1 BambooStack + 1 BambooSapling
                        // 33% chance: 2 BambooStack
                        // 33% chance: 2 BambooSapling
                        float dropRoll = (float) Math.random();
                        
                        // Validate spawn locations and find alternatives if needed
                        float[] location1 = findValidItemSpawnLocation(targetBambooTree.getX(), targetBambooTree.getY(), targetKey + "-item1");
                        float[] location2 = findValidItemSpawnLocation(targetBambooTree.getX() + 8, targetBambooTree.getY(), targetKey + "-item2");
                        
                        if (dropRoll < 0.33f) {
                            // Drop 1 BambooStack + 1 BambooSapling (original behavior)
                            if (location1 != null) {
                                bambooStacks.put(targetKey + "-bamboostack", 
                                    new BambooStack(location1[0], location1[1]));
                                System.out.println("BambooStack dropped at: " + location1[0] + ", " + location1[1]);
                            }
                            if (location2 != null) {
                                bambooSaplings.put(targetKey + "-babybamboo", 
                                    new BambooSapling(location2[0], location2[1]));
                                System.out.println("BambooSapling dropped at: " + location2[0] + ", " + location2[1]);
                            }
                        } else if (dropRoll < 0.66f) {
                            // Drop 2 BambooStack
                            if (location1 != null) {
                                bambooStacks.put(targetKey + "-bamboostack1", 
                                    new BambooStack(location1[0], location1[1]));
                                System.out.println("BambooStack dropped at: " + location1[0] + ", " + location1[1]);
                            }
                            if (location2 != null) {
                                bambooStacks.put(targetKey + "-bamboostack2", 
                                    new BambooStack(location2[0], location2[1]));
                                System.out.println("BambooStack dropped at: " + location2[0] + ", " + location2[1]);
                            }
                        } else {
                            // Drop 2 BambooSapling
                            if (location1 != null) {
                                bambooSaplings.put(targetKey + "-babybamboo1", 
                                    new BambooSapling(location1[0], location1[1]));
                                System.out.println("BambooSapling dropped at: " + location1[0] + ", " + location1[1]);
                            }
                            if (location2 != null) {
                                bambooSaplings.put(targetKey + "-babybamboo2", 
                                    new BambooSapling(location2[0], location2[1]));
                                System.out.println("BambooSapling dropped at: " + location2[0] + ", " + location2[1]);
                            }
                        }
                        
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.TREE,
                                    targetBambooTree.getX(),
                                    targetBambooTree.getY(),
                                    wagemaker.uk.network.TreeType.BAMBOO
                                );
                            }
                        }
                        
                        targetBambooTree.dispose();
                        bambooTrees.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                    }
                }
            }
        }
        
        // Attack banana trees within range (individual collision)
        if (bananaTrees != null && !attackedSomething) {
            BananaTree targetBananaTree = null;
            String targetKey = null;
            
            for (Map.Entry<String, BananaTree> entry : bananaTrees.entrySet()) {
                BananaTree bananaTree = entry.getValue();
                
                if (bananaTree.isInAttackRange(x, y)) {
                    targetBananaTree = bananaTree;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetBananaTree != null) {
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles tree destruction and item spawning
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
                    float healthBefore = targetBananaTree.getHealth();
                    boolean destroyed = targetBananaTree.attack();
                    if (destroyed) {
                        System.out.println("Attacking banana tree, health before: " + healthBefore);
                        System.out.println("Banana tree health after attack: " + targetBananaTree.getHealth() + ", destroyed: " + destroyed);
                    }
                    attackedSomething = true;
                    
                    if (destroyed) {
                        // Validate spawn locations and find alternatives if needed
                        float[] bananaLocation = findValidItemSpawnLocation(targetBananaTree.getX(), targetBananaTree.getY(), targetKey);
                        float[] saplingLocation = findValidItemSpawnLocation(targetBananaTree.getX() + 8, targetBananaTree.getY(), targetKey + "-bananasapling");
                        
                        // Spawn Banana at validated position
                        if (bananaLocation != null) {
                            bananas.put(targetKey, new Banana(bananaLocation[0], bananaLocation[1]));
                            System.out.println("Banana tree destroyed! Banana dropped at: " + bananaLocation[0] + ", " + bananaLocation[1]);
                        }
                        
                        // Spawn BananaSapling at validated position
                        if (saplingLocation != null) {
                            bananaSaplings.put(targetKey + "-bananasapling", 
                                new BananaSapling(saplingLocation[0], saplingLocation[1]));
                            System.out.println("BananaSapling dropped at: " + saplingLocation[0] + ", " + saplingLocation[1]);
                        }
                        
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.TREE,
                                    targetBananaTree.getX(),
                                    targetBananaTree.getY(),
                                    wagemaker.uk.network.TreeType.BANANA
                                );
                            }
                        }
                        
                        targetBananaTree.dispose();
                        bananaTrees.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                    }
                }
            }
        }
        
        // Attack cactus within range
        if (cactus != null && !attackedSomething) {
            if (cactus.isInAttackRange(x, y)) {
                float healthBefore = cactus.getHealth();
                boolean destroyed = cactus.attack();
                
                if (destroyed) {
                    System.out.println("Attacking cactus, health before: " + healthBefore);
                    System.out.println("Cactus health after attack: " + cactus.getHealth() + ", destroyed: " + destroyed);
                    System.out.println("Cactus destroyed! Will respawn after timer...");
                    
                    // Register for respawn before removing
                    if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                        wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                        wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                        if (respawnManager != null) {
                            // Use a fixed key for the single cactus
                            String cactusKey = "cactus-128,128";
                            respawnManager.registerDestruction(
                                cactusKey,
                                wagemaker.uk.respawn.ResourceType.TREE,
                                cactus.getX(),
                                cactus.getY(),
                                wagemaker.uk.network.TreeType.CACTUS
                            );
                        }
                        
                        // Dispose the cactus
                        try {
                            final wagemaker.uk.trees.Cactus cactusToDispose = cactus;
                            gameInstance.getClass().getMethod("deferOperation", Runnable.class)
                                .invoke(gameInstance, (Runnable) () -> cactusToDispose.dispose());
                        } catch (Exception e) {
                            cactus.dispose();
                        }
                        
                        // Set cactus to null so it's not rendered
                        setCactus(null);
                    }
                }
                attackedSomething = true;
            }
        }
        
        // Attack stones within range (individual collision)
        if (stones != null && !attackedSomething) {
            Stone targetStone = null;
            String targetKey = null;
            
            for (Map.Entry<String, Stone> entry : stones.entrySet()) {
                Stone stone = entry.getValue();
                
                if (stone.isInAttackRange(x, y)) {
                    targetStone = stone;
                    targetKey = entry.getKey();
                    break;
                }
            }
            
            if (targetStone != null) {
                // Send attack action to server in multiplayer mode
                if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                    gameClient.sendAttackAction(targetKey);
                    // In multiplayer, server handles stone destruction and pebble spawning
                    attackedSomething = true;
                } else {
                    // Single-player mode: handle locally
                    float healthBefore = targetStone.getHealth();
                    boolean destroyed = targetStone.attack();
                    if (destroyed) {
                        System.out.println("Attacking stone, health before: " + healthBefore);
                        System.out.println("Stone health after attack: " + targetStone.getHealth() + ", destroyed: " + destroyed);
                    }
                    attackedSomething = true;
                    
                    if (destroyed) {
                        // Validate spawn location and find alternative if needed
                        float[] pebbleLocation = findValidItemSpawnLocation(targetStone.getX(), targetStone.getY(), targetKey + "-pebble");
                        
                        // Spawn pebble at validated position
                        if (pebbleLocation != null) {
                            pebbles.put(targetKey + "-pebble", new Pebble(pebbleLocation[0], pebbleLocation[1]));
                            System.out.println("Pebble dropped at: " + pebbleLocation[0] + ", " + pebbleLocation[1]);
                        }
                        
                        // Register for respawn before removing
                        if (gameInstance != null && gameInstance instanceof wagemaker.uk.gdx.MyGdxGame) {
                            wagemaker.uk.gdx.MyGdxGame game = (wagemaker.uk.gdx.MyGdxGame) gameInstance;
                            wagemaker.uk.respawn.RespawnManager respawnManager = game.getRespawnManager();
                            if (respawnManager != null) {
                                respawnManager.registerDestruction(
                                    targetKey,
                                    wagemaker.uk.respawn.ResourceType.STONE,
                                    targetStone.getX(),
                                    targetStone.getY(),
                                    null  // stones don't have a tree type
                                );
                            }
                        }
                        
                        // Use deferred operation for texture disposal (threading safety)
                        if (gameInstance != null) {
                            try {
                                final Stone stoneToDispose = targetStone;
                                gameInstance.getClass().getMethod("deferOperation", Runnable.class)
                                    .invoke(gameInstance, (Runnable) () -> stoneToDispose.dispose());
                            } catch (Exception e) {
                                // Fallback: dispose immediately if deferOperation not available
                                targetStone.dispose();
                            }
                        } else {
                            targetStone.dispose();
                        }
                        
                        stones.remove(targetKey);
                        clearedPositions.put(targetKey, true);
                        System.out.println("Stone removed from world");
                    }
                }
            }
        }
        
        if (!attackedSomething) {
            System.out.println("No trees in range to attack");
        }
    }

    /**
     * Toggle inventory navigation mode on/off.
     * When ON, arrow keys navigate inventory slots instead of moving player.
     * When OFF, arrow keys move player normally.
     * Uses the new priority-based navigation mode system.
     */
    private void toggleInventoryNavigationMode() {
        if (isNavigationModeActive(NavigationMode.INVENTORY)) {
            // Currently in inventory mode - try to return to normal mode
            requestNavigationMode(NavigationMode.NORMAL);
        } else {
            // Not in inventory mode - try to enter inventory mode
            boolean accepted = requestNavigationMode(NavigationMode.INVENTORY);
            if (!accepted) {
                System.out.println("Cannot enter inventory navigation mode - higher priority mode is active");
            }
        }
    }
    
    /**
     * Handle arrow key navigation in inventory mode.
     * RIGHT/LEFT cycle through slots with wrap-around.
     * UP/DOWN reserved for future multi-row support.
     */
    private void handleInventoryNavigation() {
        if (inventoryManager == null) {
            return;
        }
        
        int currentSelection = inventoryManager.getSelectedSlot();
        int previousSelection = currentSelection;
        int newSelection = currentSelection;
        
        // RIGHT arrow: move to next slot (wrap around)
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (currentSelection == -1) {
                newSelection = 0; // Start at first slot
            } else {
                newSelection = (currentSelection + 1) % 14; // Wrap to 0 after slot 13 (14 total slots: 0-13)
            }
        }
        // LEFT arrow: move to previous slot (wrap around)
        else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (currentSelection == -1) {
                newSelection = 13; // Start at last slot
            } else {
                newSelection = (currentSelection - 1 + 14) % 14; // Wrap to 13 from slot 0
            }
        }
        // UP/DOWN: Reserved for future multi-row layout
        else if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            // No-op for now, reserved for future enhancement
            return;
        }
        
        // Apply selection change if different
        if (newSelection != previousSelection) {
            inventoryManager.setSelectedSlot(newSelection);
            
            // Update targeting system based on new selection
            updateTargetingForSelection(newSelection);
        }
    }
    
    /**
     * Handle arrow key navigation in fence selection mode.
     * LEFT/RIGHT cycle through fence pieces.
     * Integrates with FenceItemRenderer for selection and activates targeting.
     */
    private void handleFenceNavigation() {
        // Get the fence item renderer from the game instance
        wagemaker.uk.ui.FenceItemRenderer fenceItemRenderer = getFenceItemRenderer();
        if (fenceItemRenderer == null) {
            return;
        }
        
        boolean selectionChanged = false;
        
        // Handle LEFT/RIGHT arrow keys for fence piece selection
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            int currentIndex = fenceItemRenderer.getSelectedFencePieceIndex();
            int newIndex = (currentIndex + 1) % 8; // Wrap around after 7
            fenceItemRenderer.setSelectedFencePieceIndex(newIndex);
            selectionChanged = true;
            System.out.println("Selected fence piece: " + fenceItemRenderer.getSelectedFencePieceType().getDescription());
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            int currentIndex = fenceItemRenderer.getSelectedFencePieceIndex();
            int newIndex = (currentIndex - 1 + 8) % 8; // Wrap around before 0
            fenceItemRenderer.setSelectedFencePieceIndex(newIndex);
            selectionChanged = true;
            System.out.println("Selected fence piece: " + fenceItemRenderer.getSelectedFencePieceType().getDescription());
        }
        
        // Activate targeting for fence placement when first entering fence navigation
        // Don't reactivate on selection changes to prevent cancellation loops
        if (!targetingSystem.isActive()) {
            activateFenceTargeting();
        } else if (selectionChanged) {
            // Just update the validator when selection changes, don't reactivate targeting
            wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
            if (fenceBuildingManager != null) {
                wagemaker.uk.targeting.FenceTargetValidator fenceValidator = 
                    new wagemaker.uk.targeting.FenceTargetValidator(fenceBuildingManager);
                targetingSystem.setValidator(fenceValidator);
                System.out.println("[FenceNav] Updated fence validator for new selection");
            }
        }
    }
    
    /**
     * Activate targeting system for fence placement.
     */
    private void activateFenceTargeting() {
        if (targetingSystem != null) {
            // Set fence-specific validator and disable direct mouse input
            wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
            if (fenceBuildingManager != null) {
                wagemaker.uk.targeting.FenceTargetValidator fenceValidator = 
                    new wagemaker.uk.targeting.FenceTargetValidator(fenceBuildingManager);
                targetingSystem.setValidator(fenceValidator);
                
                // Disable direct mouse input to prevent double placement
                fenceBuildingManager.setDirectMouseInputDisabled(true);
            }
            
            // Only activate if not already active to prevent cancellation loops
            if (!targetingSystem.isActive()) {
                targetingSystem.activate(x, y, wagemaker.uk.targeting.TargetingMode.ADJACENT, new wagemaker.uk.targeting.TargetingCallback() {
                    @Override
                    public void onTargetConfirmed(float targetX, float targetY) {
                        // Check if targeting callback should be suppressed (e.g., after direct mouse removal)
                        wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
                        if (fenceBuildingManager != null && fenceBuildingManager.shouldSuppressTargetingCallback()) {
                            System.out.println("Suppressing targeting callback after direct mouse operation");
                            return;
                        }
                        handleFencePlacement(targetX, targetY);
                    }
                    
                    @Override
                    public void onTargetCancelled() {
                        System.out.println("Fence placement cancelled");
                        // Re-enable direct mouse input when targeting is cancelled
                        wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
                        if (fenceBuildingManager != null) {
                            fenceBuildingManager.setDirectMouseInputDisabled(false);
                        }
                        // Exit fence navigation mode when targeting is cancelled
                        requestNavigationMode(NavigationMode.NORMAL);
                    }
                });
                System.out.println("[FenceNav] Targeting system activated for fence placement");
            } else {
                System.out.println("[FenceNav] Targeting system already active, just updated validator");
            }
        }
    }
    
    /**
     * Handle fence placement at the confirmed target coordinates.
     * Called when the player confirms a target position for fence placement.
     * Integrates fence piece selection with fence placement system and provides proper error handling.
     */
    private void handleFencePlacement(float targetX, float targetY) {
        System.out.println("Fence placement at (" + targetX + ", " + targetY + ")");
        
        wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
        wagemaker.uk.ui.FenceItemRenderer fenceItemRenderer = getFenceItemRenderer();
        
        if (fenceBuildingManager == null) {
            System.err.println("Cannot place fence: FenceBuildingManager not available");
            return;
        }
        
        if (fenceItemRenderer == null) {
            System.err.println("Cannot place fence: FenceItemRenderer not available");
            return;
        }
        
        // Ensure building mode is still active
        if (!fenceBuildingManager.isBuildingModeActive()) {
            System.err.println("Cannot place fence: Building mode is not active");
            return;
        }
        
        try {
            // Convert world coordinates to grid coordinates
            wagemaker.uk.fence.FenceGrid fenceGrid = fenceBuildingManager.getStructureManager().getGrid();
            java.awt.Point gridPos = fenceGrid.worldToGrid(targetX, targetY);
            
            // Get the selected fence piece type (for logging/debugging)
            wagemaker.uk.fence.FencePieceType selectedPieceType = fenceItemRenderer.getSelectedFencePieceType();
            System.out.println("Attempting to place fence piece: " + selectedPieceType.getDescription() + 
                             " at grid position (" + gridPos.x + ", " + gridPos.y + ")");
            
            // Attempt to place the fence (FenceBuildingManager handles automatic piece type selection)
            boolean placed = fenceBuildingManager.placeFenceSegment(gridPos.x, gridPos.y);
            
            if (placed) {
                System.out.println("Fence piece placed successfully at (" + targetX + ", " + targetY + ")");
                // Keep targeting active for multiple placements
                activateFenceTargeting();
            } else {
                System.out.println("Failed to place fence piece - see error details above");
                // Targeting remains active so user can try a different position
            }
        } catch (Exception e) {
            System.err.println("Error during fence placement: " + e.getMessage());
            e.printStackTrace();
            // Targeting remains active so user can try again
        }
    }
    
    /**
     * Get the fence item renderer from the game instance.
     * @return The fence item renderer, or null if not available
     */
    private wagemaker.uk.ui.FenceItemRenderer getFenceItemRenderer() {
        if (gameInstance == null) {
            return null;
        }
        
        try {
            // Use reflection to get the fenceItemRenderer field from MyGdxGame
            java.lang.reflect.Field field = gameInstance.getClass().getDeclaredField("fenceItemRenderer");
            field.setAccessible(true);
            return (wagemaker.uk.ui.FenceItemRenderer) field.get(gameInstance);
        } catch (Exception e) {
            System.err.println("Could not access fence item renderer: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get the fence building manager from the game instance.
     * @return The fence building manager, or null if not available
     */
    private wagemaker.uk.fence.FenceBuildingManager getFenceBuildingManager() {
        if (gameInstance == null) {
            return null;
        }
        
        try {
            // Use reflection to get the fenceBuildingManager field from MyGdxGame
            java.lang.reflect.Field field = gameInstance.getClass().getDeclaredField("fenceBuildingManager");
            field.setAccessible(true);
            return (wagemaker.uk.fence.FenceBuildingManager) field.get(gameInstance);
        } catch (Exception e) {
            System.err.println("Could not access fence building manager: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Update targeting system when inventory selection changes.
     * Activates targeting for plantable items, deactivates for consumables.
     * Activates fence building mode for fence item.
     */
    private void updateTargetingForSelection(int slot) {
        System.out.println("[DEBUG] updateTargetingForSelection called with slot: " + slot);
        
        if (slot == -1) {
            // Item deselected - deactivate targeting and fence building
            if (targetingSystem.isActive()) {
                targetingSystem.deactivate();
            }
            // Exit fence building mode if active
            wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
            if (fenceBuildingManager != null && fenceBuildingManager.isBuildingModeActive()) {
                fenceBuildingManager.exitBuildingMode();
                System.out.println("Exited fence building mode due to item deselection");
            }
        } else {
            // Item selected - check item type
            wagemaker.uk.inventory.ItemType selectedItemType = inventoryManager.getSelectedItemType();
            System.out.println("[DEBUG] Selected item type: " + selectedItemType);
            
            // Handle fence item selection (slot 11 - BACK_FENCE)
            if (selectedItemType == wagemaker.uk.inventory.ItemType.BACK_FENCE) {
                System.out.println("[DEBUG] Back fence item selected - activating fence building mode");
                
                // Activate fence building mode
                wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
                if (fenceBuildingManager != null) {
                    if (!fenceBuildingManager.isBuildingModeActive()) {
                        fenceBuildingManager.setActivatedViaInventorySelection(true);
                        fenceBuildingManager.enterBuildingMode();
                        System.out.println("Entered fence building mode via back fence item selection");
                    }
                } else {
                    System.err.println("FenceBuildingManager not available");
                }
                
                // DON'T change navigation mode when fence item is selected from inventory
                // This keeps the inventory selection active and visible
                
                // Deactivate targeting system as fence building has its own targeting
                if (targetingSystem.isActive()) {
                    targetingSystem.deactivate();
                }
                return;
            } else {
                // Non-fence item selected - exit fence building mode if active
                wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
                if (fenceBuildingManager != null && fenceBuildingManager.isBuildingModeActive()) {
                    fenceBuildingManager.exitBuildingMode();
                    System.out.println("Exited fence building mode due to non-fence item selection");
                }
            }
            
            // Only activate targeting for plantable items (saplings and baby items)
            boolean isPlantable = selectedItemType != null && (
                selectedItemType == wagemaker.uk.inventory.ItemType.APPLE_SAPLING ||
                selectedItemType == wagemaker.uk.inventory.ItemType.BANANA_SAPLING ||
                selectedItemType == wagemaker.uk.inventory.ItemType.BABY_BAMBOO ||
                selectedItemType == wagemaker.uk.inventory.ItemType.BABY_TREE
            );
            System.out.println("[DEBUG] Is plantable: " + isPlantable + " for item: " + selectedItemType);
            
            if (isPlantable) {
                // Plantable item - activate targeting at player position
                if (!targetingSystem.isActive()) {
                    System.out.println("[DEBUG] Activating targeting system for slot " + slot);
                    targetingSystem.activate(x, y, TargetingMode.ADJACENT, new TargetingCallback() {
                        @Override
                        public void onTargetConfirmed(float targetX, float targetY) {
                            // Handle planting based on selected item
                            handleItemPlacement(targetX, targetY);
                        }
                        
                        @Override
                        public void onTargetCancelled() {
                            System.out.println("Item placement cancelled");
                        }
                    });
                } else {
                    System.out.println("[DEBUG] Targeting system already active");
                }
            } else {
                // Non-plantable item - deactivate targeting if active
                if (targetingSystem.isActive()) {
                    System.out.println("[DEBUG] Deactivating targeting system for non-plantable item: " + selectedItemType);
                    targetingSystem.deactivate();
                }
            }
        }
    }
    
    /**
     * Handle item placement at the confirmed target coordinates.
     * Called when the player confirms a target position (presses P).
     * Handles different item types based on the currently selected inventory slot.
     */
    private void handleItemPlacement(float targetX, float targetY) {
        System.out.println("[DEBUG] handleItemPlacement called at (" + targetX + ", " + targetY + ")");
        
        if (inventoryManager == null) {
            System.out.println("[DEBUG] inventoryManager is null");
            return;
        }
        
        int selectedSlot = inventoryManager.getSelectedSlot();
        System.out.println("[DEBUG] Selected slot: " + selectedSlot);
        
        // Handle baby bamboo planting (slot 2)
        if (selectedSlot == 2) {
            if (inventoryManager.getCurrentInventory().getBambooSaplingCount() > 0) {
                executePlanting(targetX, targetY);
            } else {
                System.out.println("No baby bamboo in inventory");
            }
        }
        // Handle baby tree planting (slot 4)
        else if (selectedSlot == 4) {
            if (inventoryManager.getCurrentInventory().getTreeSaplingCount() > 0) {
                executeTreePlanting(targetX, targetY);
            } else {
                System.out.println("No baby tree in inventory");
            }
        }
        // Handle apple tree planting (slot 8)
        else if (selectedSlot == 8) {
            if (inventoryManager.getCurrentInventory().getAppleSaplingCount() > 0) {
                executeAppleTreePlanting(targetX, targetY);
            } else {
                System.out.println("No apple sapling in inventory");
            }
        }
        // Handle banana tree planting (slot 9)
        else if (selectedSlot == 9) {
            if (inventoryManager.getCurrentInventory().getBananaSaplingCount() > 0) {
                executeBananaTreePlanting(targetX, targetY);
            } else {
                System.out.println("No banana sapling in inventory");
            }
        }
        // Add handling for other items here in the future
        else {
            System.out.println("Selected item cannot be placed");
        }
    }
    
    /**
     * Handle targeting input when targeting mode is active.
     * Processes A/W/D/S keys for directional movement and ESC for cancellation.
     * Also supports mouse movement and left-click for targeting.
     * Mouse movement updates target position even when just hovering (not clicking).
     * Requirements: 1.1, 1.2, 1.3, 1.4, 1.5, 4.1, 4.2, 4.3, 4.4
     */
    private void handleTargetingInput() {
        if (!targetingSystem.isActive()) {
            return;
        }
        
        // Handle keyboard directional input (A/W/D/S)
        if (Gdx.input.isKeyJustPressed(Input.Keys.A)) {
            targetingSystem.moveTarget(wagemaker.uk.targeting.Direction.LEFT);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            targetingSystem.moveTarget(wagemaker.uk.targeting.Direction.UP);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.D)) {
            targetingSystem.moveTarget(wagemaker.uk.targeting.Direction.RIGHT);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            targetingSystem.moveTarget(wagemaker.uk.targeting.Direction.DOWN);
        }
        
        // Handle mouse movement for targeting (updates on hover, not just when clicking)
        // This allows the target indicator to follow the mouse cursor in real-time
        int mouseX = Gdx.input.getX();
        int mouseY = Gdx.input.getY();
        targetingSystem.setTargetFromMouse(mouseX, mouseY);
        
        // Handle left mouse click for placement (context-sensitive)
        if (Gdx.input.justTouched()) {
            if (targetingSystem.isTargetValid()) {
                // Get current target coordinates
                float[] coords = targetingSystem.getTargetCoordinates();
                
                // Handle placement based on current navigation mode
                if (fenceNavigationMode) {
                    // Check if targeting callback should be suppressed
                    wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
                    if (fenceBuildingManager != null && fenceBuildingManager.shouldSuppressTargetingCallback()) {
                        System.out.println("Suppressing fence placement after direct mouse operation");
                        return;
                    }
                    handleFencePlacement(coords[0], coords[1]);
                } else {
                    handleItemPlacement(coords[0], coords[1]);
                }
                
                // Targeting remains active - don't deactivate
            }
        }
        
        // Handle cancellation (ESC key)
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            targetingSystem.cancel();
        }
    }
    
    /**
     * Handle planting action when "p" key is pressed.
     * Places item at current target position without deactivating targeting.
     * Targeting stays active as long as an item is selected.
     * Requirements: 1.1, 1.3, 4.1, 4.2
     */
    private void handlePlantingAction() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            // If targeting is active and target is valid, place the item
            if (targetingSystem.isActive() && targetingSystem.isTargetValid()) {
                // Get current target coordinates
                float[] coords = targetingSystem.getTargetCoordinates();
                
                // Place the item at target location
                handleItemPlacement(coords[0], coords[1]);
                
                // Targeting remains active - don't deactivate
                // It will only deactivate when the player deselects the item
            }
        }
    }
    
    /**
     * Handle placement action when spacebar is pressed while targeting is active.
     * This provides an alternative to the 'P' key for placement.
     * Spacebar is context-sensitive: places fence when in fence navigation mode, plants items otherwise.
     */
    private void handleSpacebarPlanting() {
        // If targeting is active and target is valid, place the item/fence
        if (targetingSystem.isActive() && targetingSystem.isTargetValid()) {
            // Get current target coordinates
            float[] coords = targetingSystem.getTargetCoordinates();
            
            // Handle placement based on current navigation mode
            if (fenceNavigationMode) {
                // Check if targeting callback should be suppressed
                wagemaker.uk.fence.FenceBuildingManager fenceBuildingManager = getFenceBuildingManager();
                if (fenceBuildingManager != null && fenceBuildingManager.shouldSuppressTargetingCallback()) {
                    System.out.println("Suppressing fence placement after direct mouse operation");
                    return;
                }
                handleFencePlacement(coords[0], coords[1]);
            } else {
                handleItemPlacement(coords[0], coords[1]);
            }
            
            // Targeting remains active - don't deactivate
            // It will only deactivate when the player deselects the item or exits fence navigation
        }
    }
    
    /**
     * Execute planting at the specified target coordinates.
     * Called by the targeting system callback when target is confirmed.
     * Includes error handling and state rollback on failure.
     */
    private void executePlanting(float targetX, float targetY) {
        // Store initial inventory state for potential rollback
        int initialBambooSaplingCount = inventoryManager.getCurrentInventory().getBambooSaplingCount();
        
        // Attempt to plant baby bamboo at target coordinates
        PlantedBamboo plantedBamboo = plantingSystem.attemptPlant(
            targetX, targetY, 
            inventoryManager, 
            biomeManager, 
            plantedBamboos, 
            bambooTrees
        );
        
        // Add planted bamboo to game world map if planting succeeds
        if (plantedBamboo != null) {
            // Generate unique key for the planted bamboo
            float tileX = (float) (Math.floor(targetX / 64.0) * 64.0);
            float tileY = (float) (Math.floor(targetY / 64.0) * 64.0);
            String key = "planted-bamboo-" + (int)tileX + "-" + (int)tileY;
            plantedBamboos.put(key, plantedBamboo);
            System.out.println("Planted bamboo added to game world at: " + key);
            
            // Send planting message to server in multiplayer
            if (gameClient != null && gameClient.isConnected()) {
                try {
                    gameClient.sendBambooPlant(key, tileX, tileY);
                    
                    // Send inventory update after planting (baby bamboo was deducted)
                    inventoryManager.sendInventoryUpdateToServer();
                } catch (Exception e) {
                    // Network error - rollback state
                    System.err.println("Failed to send planting message to server: " + e.getMessage());
                    
                    // Remove planted bamboo from local state
                    plantedBamboos.remove(key);
                    plantedBamboo.dispose();
                    
                    // Restore inventory (add baby bamboo back)
                    inventoryManager.getCurrentInventory().addBambooSapling(1);
                    
                    System.out.println("Planting rolled back due to network error");
                }
            } else {
                // Single-player mode: check for auto-deselection
                inventoryManager.checkAndAutoDeselect();
            }
        } else {
            System.out.println("Planting failed: invalid location or no baby bamboo in inventory");
        }
    }
    
    /**
     * Execute tree planting at the specified target coordinates.
     * Called by the targeting system callback when target is confirmed for baby trees.
     * Includes error handling and state rollback on failure.
     */
    private void executeTreePlanting(float targetX, float targetY) {
        // Store initial inventory state for potential rollback
        int initialTreeSaplingCount = inventoryManager.getCurrentInventory().getTreeSaplingCount();
        
        // Validate grass biome for tree planting
        if (!plantingSystem.canPlantTree(targetX, targetY, biomeManager)) {
            System.out.println("Tree planting failed: can only plant trees on grass biomes");
            return;
        }
        
        // Attempt to plant baby tree at target coordinates
        String plantedTreeId = plantingSystem.plantTree(targetX, targetY, plantedTrees);
        
        if (plantedTreeId != null) {
            // Deduct baby tree from inventory
            boolean removed = inventoryManager.getCurrentInventory().removeTreeSapling(1);
            if (!removed) {
                // Failed to remove item - rollback planting
                plantedTrees.remove(plantedTreeId);
                System.out.println("Tree planting failed: could not deduct baby tree from inventory");
                return;
            }
            
            System.out.println("Baby tree planted successfully at: " + plantedTreeId);
            
            // Send planting message to server in multiplayer
            if (gameClient != null && gameClient.isConnected()) {
                try {
                    // Extract coordinates from planted tree
                    PlantedTree plantedTree = plantedTrees.get(plantedTreeId);
                    if (plantedTree != null) {
                        gameClient.sendTreePlant(plantedTreeId, plantedTree.getX(), plantedTree.getY());
                        
                        // Send inventory update after planting (baby tree was deducted)
                        inventoryManager.sendInventoryUpdateToServer();
                    }
                } catch (Exception e) {
                    // Network error - rollback state
                    System.err.println("Failed to send tree planting message to server: " + e.getMessage());
                    
                    // Remove planted tree from local state
                    PlantedTree plantedTree = plantedTrees.remove(plantedTreeId);
                    if (plantedTree != null) {
                        plantedTree.dispose();
                    }
                    
                    // Restore inventory (add baby tree back)
                    inventoryManager.getCurrentInventory().addTreeSapling(1);
                    
                    System.out.println("Tree planting rolled back due to network error");
                }
            } else {
                // Single-player mode: check for auto-deselection
                inventoryManager.checkAndAutoDeselect();
            }
        } else {
            System.out.println("Tree planting failed: invalid location or tile already occupied");
        }
    }
    
    /**
     * Execute banana tree planting at the specified target coordinates.
     * Called by the targeting system callback when target is confirmed for banana saplings.
     * Includes error handling and state rollback on failure.
     */
    private void executeBananaTreePlanting(float targetX, float targetY) {
        System.out.println("[DEBUG] executeBananaTreePlanting called at (" + targetX + ", " + targetY + ")");
        
        // Store initial inventory state for potential rollback
        int initialBananaSaplingCount = inventoryManager.getCurrentInventory().getBananaSaplingCount();
        System.out.println("[DEBUG] BananaSapling count: " + initialBananaSaplingCount);
        
        // Validate grass biome for banana tree planting
        if (!plantingSystem.canPlantBananaTree(targetX, targetY, biomeManager)) {
            System.out.println("[DEBUG] Banana tree planting failed: can only plant banana trees on grass biomes");
            wagemaker.uk.biome.BiomeType biome = biomeManager.getBiomeAtPosition(targetX, targetY);
            System.out.println("[DEBUG] Current biome at position: " + biome);
            return;
        }
        System.out.println("[DEBUG] Biome check passed - grass biome confirmed");
        
        // Attempt to plant banana tree at target coordinates
        String plantedBananaTreeId = plantingSystem.plantBananaTree(targetX, targetY, plantedBananaTrees);
        
        if (plantedBananaTreeId != null) {
            // Deduct banana sapling from inventory
            boolean removed = inventoryManager.getCurrentInventory().removeBananaSapling(1);
            if (!removed) {
                // Failed to remove item - rollback planting
                plantedBananaTrees.remove(plantedBananaTreeId);
                System.out.println("Banana tree planting failed: could not deduct banana sapling from inventory");
                return;
            }
            
            System.out.println("Banana tree planted successfully at: " + plantedBananaTreeId);
            
            // Send planting message to server in multiplayer
            if (gameClient != null && gameClient.isConnected()) {
                try {
                    // Extract coordinates from planted banana tree
                    wagemaker.uk.planting.PlantedBananaTree plantedBananaTree = plantedBananaTrees.get(plantedBananaTreeId);
                    if (plantedBananaTree != null) {
                        gameClient.sendBananaTreePlant(plantedBananaTreeId, plantedBananaTree.getX(), plantedBananaTree.getY());
                        
                        // Send inventory update after planting (banana sapling was deducted)
                        inventoryManager.sendInventoryUpdateToServer();
                    }
                } catch (Exception e) {
                    // Network error - rollback state
                    System.err.println("Failed to send banana tree planting message to server: " + e.getMessage());
                    
                    // Remove planted banana tree from local state
                    wagemaker.uk.planting.PlantedBananaTree plantedBananaTree = plantedBananaTrees.remove(plantedBananaTreeId);
                    if (plantedBananaTree != null) {
                        plantedBananaTree.dispose();
                    }
                    
                    // Restore inventory (add banana sapling back)
                    inventoryManager.getCurrentInventory().addBananaSapling(1);
                    
                    System.out.println("Banana tree planting rolled back due to network error");
                }
            } else {
                // Single-player mode: check for auto-deselection
                inventoryManager.checkAndAutoDeselect();
            }
        } else {
            System.out.println("Banana tree planting failed: invalid location or tile already occupied");
        }
    }
    
    /**
     * Execute apple tree planting at the specified target coordinates.
     * Called by the targeting system callback when target is confirmed for apple saplings.
     * Includes error handling and state rollback on failure.
     */
    private void executeAppleTreePlanting(float targetX, float targetY) {
        System.out.println("[DEBUG] executeAppleTreePlanting called at (" + targetX + ", " + targetY + ")");
        
        // Store initial inventory state for potential rollback
        int initialAppleSaplingCount = inventoryManager.getCurrentInventory().getAppleSaplingCount();
        System.out.println("[DEBUG] AppleSapling count: " + initialAppleSaplingCount);
        
        // Validate grass biome for apple tree planting
        if (!plantingSystem.canPlantAppleTree(targetX, targetY, biomeManager)) {
            System.out.println("[DEBUG] Apple tree planting failed: can only plant apple trees on grass biomes");
            wagemaker.uk.biome.BiomeType biome = biomeManager.getBiomeAtPosition(targetX, targetY);
            System.out.println("[DEBUG] Current biome at position: " + biome);
            return;
        }
        System.out.println("[DEBUG] Biome check passed - grass biome confirmed");
        
        // Attempt to plant apple tree at target coordinates
        String plantedAppleTreeId = plantingSystem.plantAppleTree(targetX, targetY, plantedAppleTrees);
        
        if (plantedAppleTreeId != null) {
            // Deduct apple sapling from inventory
            boolean removed = inventoryManager.getCurrentInventory().removeAppleSapling(1);
            if (!removed) {
                // Failed to remove item - rollback planting
                plantedAppleTrees.remove(plantedAppleTreeId);
                System.out.println("Apple tree planting failed: could not deduct apple sapling from inventory");
                return;
            }
            
            System.out.println("Apple tree planted successfully at: " + plantedAppleTreeId);
            
            // Send planting message to server in multiplayer
            if (gameClient != null && gameClient.isConnected()) {
                try {
                    // Extract coordinates from planted apple tree
                    wagemaker.uk.planting.PlantedAppleTree plantedAppleTree = plantedAppleTrees.get(plantedAppleTreeId);
                    if (plantedAppleTree != null) {
                        gameClient.sendAppleTreePlant(plantedAppleTreeId, plantedAppleTree.getX(), plantedAppleTree.getY());
                        
                        // Send inventory update after planting (apple sapling was deducted)
                        inventoryManager.sendInventoryUpdateToServer();
                    }
                } catch (Exception e) {
                    // Network error - rollback state
                    System.err.println("Failed to send apple tree planting message to server: " + e.getMessage());
                    
                    // Remove planted apple tree from local state
                    wagemaker.uk.planting.PlantedAppleTree plantedAppleTree = plantedAppleTrees.remove(plantedAppleTreeId);
                    if (plantedAppleTree != null) {
                        plantedAppleTree.dispose();
                    }
                    
                    // Restore inventory (add apple sapling back)
                    inventoryManager.getCurrentInventory().addAppleSapling(1);
                    
                    System.out.println("Apple tree planting rolled back due to network error");
                }
            } else {
                // Single-player mode: check for auto-deselection
                inventoryManager.checkAndAutoDeselect();
            }
        } else {
            System.out.println("Apple tree planting failed: invalid location or tile already occupied");
        }
    }

    private void checkCactusDamage(float deltaTime) {
        if (cactus != null) {
            // Use the same logic as attack range for consistency
            float cactusCenterX = cactus.getX() + 32;
            float cactusCenterY = cactus.getY() + 64;
            float playerCenterX = x + 32;
            float playerCenterY = y + 32;
            
            float dx = Math.abs(cactusCenterX - playerCenterX);
            float dy = Math.abs(cactusCenterY - playerCenterY);
            
            // Use same range as attack range: 64px left/right, 96px up/down
            boolean inDamageRange = dx <= 64 && dy <= 96;
            
            if (inDamageRange) {

                lastCactusDamageTime += deltaTime;
                
                // Take damage every 0.5 seconds while in range
                if (lastCactusDamageTime >= 0.5f) {
                    health -= 10; // 10 damage per half second
                    lastCactusDamageTime = 0;
                    
                    System.out.println("Player taking cactus damage! Health: " + health);
                    
                    // Check if player died
                    if (health <= 0) {
                        respawnPlayer();
                    }
                }
            } else {
                // Reset damage timer when not in range
                lastCactusDamageTime = 0;
            }
        }
    }
    
    private void respawnPlayer() {
        System.out.println("Player died! Respawning...");
        
        // Reset health and hunger
        health = 100;
        hunger = 0;
        
        // Generate random respawn position far from cactus
        float newX, newY;
        if (cactus != null) {
            do {
                // Random position in a large area
                newX = (float)(Math.random() - 0.5) * 2000; // ±1000px
                newY = (float)(Math.random() - 0.5) * 2000; // ±1000px
                
                float distance = (float)Math.sqrt((newX - cactus.getX()) * (newX - cactus.getX()) + 
                                                 (newY - cactus.getY()) * (newY - cactus.getY()));
                
                // Ensure respawn is at least 500px away from cactus
                if (distance >= 500) {
                    break;
                }
            } while (true);
        } else {
            // If no cactus, respawn at origin
            newX = 0;
            newY = 0;
        }
        
        // Set new position
        x = newX;
        y = newY;
        
        System.out.println("Player respawned!");
    }
    
    /**
     * Trigger the fall sequence when player steps in a puddle.
     * Sets falling flag, starts animation, marks puddle as triggered, and applies damage.
     * Requirements: 1.2, 4.1
     */
    private void triggerFall(wagemaker.uk.weather.WaterPuddle puddle) {
        // Set falling flag
        isFalling = true;
        
        // Start fall animation sequence
        fallAnimationSystem.startFallSequence();
        
        // Mark puddle as triggered
        puddleCollisionSystem.markPuddleTriggered(puddle);
        
        // Apply 10% damage to health
        float damage = 10.0f;
        health = Math.max(0, health - damage);
        
        System.out.println("Player fell in puddle! Health: " + health);
        
        // Send fall event to server in multiplayer
        if (gameClient != null && gameClient.isConnected()) {
            gameClient.sendPlayerFall(puddle.getId());
        }
        
        // Send health update in multiplayer
        checkAndSendHealthUpdate();
    }
    
    /**
     * Complete the fall sequence and restore normal player state.
     * Requirements: 1.4, 2.6
     */
    private void completeFall() {
        // Set falling flag to false
        isFalling = false;
        
        // Reset fall animation system
        fallAnimationSystem.reset();
        
        System.out.println("Fall sequence complete, player can move again");
    }
    
    public float getHealth() {
        return health;
    }
    
    public void setHealth(float health) {
        this.health = Math.max(0, Math.min(100, health)); // Clamp between 0 and 100
    }
    
    public float getHealthPercentage() {
        return health / 100.0f;
    }
    
    /**
     * Get the current hunger level (0-100%).
     * @return Current hunger level
     */
    public float getHunger() {
        return hunger;
    }
    
    /**
     * Set the hunger level (clamped between 0 and 100).
     * @param hunger New hunger level
     */
    public void setHunger(float hunger) {
        this.hunger = Math.max(0, Math.min(100, hunger)); // Clamp between 0 and 100
    }
    
    /**
     * Update hunger system - increases hunger by 1% every 60 seconds.
     * When hunger reaches 100%, player dies and respawns.
     * Sends hunger updates to server in multiplayer mode.
     * Requirements: 4.1, 4.2, 4.3, 5.1, 5.2, 5.3, 5.4
     * 
     * @param deltaTime Time elapsed since last frame in seconds
     */
    private void updateHunger(float deltaTime) {
        // Check for hunger death first (handles cases where hunger was set to 100 externally)
        if (hunger >= 100) {
            handleHungerDeath();
            return; // Exit early after respawn
        }
        
        // Accumulate time
        hungerTimer += deltaTime;
        
        // Check if 60 seconds have passed
        if (hungerTimer >= HUNGER_INTERVAL) {
            // Increase hunger by 1%
            hunger = Math.min(100, hunger + 1);
            
            // Subtract interval from timer (preserve remainder for precision)
            hungerTimer -= HUNGER_INTERVAL;
            
            System.out.println("Hunger increased to: " + hunger + "%");
            
            // Send hunger update to server in multiplayer mode
            checkAndSendHungerUpdate();
            
            // Check for hunger death
            if (hunger >= 100) {
                handleHungerDeath();
            }
        }
    }
    
    /**
     * Handle player death from hunger.
     * Respawns player at origin (0, 0) with full health and no hunger.
     * Requirements: 5.1, 5.2, 5.3, 5.4
     */
    private void handleHungerDeath() {
        System.out.println("Player died from hunger! Respawning...");
        
        // Reset health and hunger
        health = 100;
        hunger = 0;
        hungerTimer = 0; // Reset timer as well
        
        // Respawn at origin (0, 0)
        x = 0;
        y = 0;
        
        // Send respawn message in multiplayer with hunger reset
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendPlayerRespawn(x, y, health, hunger);
            System.out.println("Sent respawn message to server with hunger reset");
        }
        
        System.out.println("Player respawned at origin (0, 0)!");
    }
    
    /**
     * Consume the currently selected item from inventory.
     * Handles apple consumption (restore 10% health) and banana consumption (reduce 5% hunger).
     * Called when space bar is pressed with a consumable item selected.
     * Requirements: 1.2, 2.1
     */
    private void consumeSelectedItem() {
        if (inventoryManager == null) {
            return;
        }
        
        // Get the selected item type
        wagemaker.uk.inventory.ItemType selectedItemType = inventoryManager.getSelectedItemType();
        
        if (selectedItemType == null) {
            return;
        }
        
        // Check if the item is consumable
        if (!selectedItemType.restoresHealth() && !selectedItemType.reducesHunger()) {
            System.out.println("Selected item (" + selectedItemType.name() + ") cannot be consumed");
            return;
        }
        
        // Try to consume the item via inventory manager
        boolean consumed = inventoryManager.tryConsumeSelectedItem(this);
        
        if (consumed) {
            // Log successful consumption
            if (selectedItemType.restoresHealth()) {
                System.out.println("Consumed " + selectedItemType.name() + " - restored " + 
                                 selectedItemType.getHealthRestore() + "% health");
            } else if (selectedItemType.reducesHunger()) {
                System.out.println("Consumed " + selectedItemType.name() + " - reduced 5% hunger");
            }
            
            // Send consumption to server in multiplayer mode
            if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
                // Convert inventory ItemType to network ItemType
                wagemaker.uk.network.ItemType networkItemType = convertToNetworkItemType(selectedItemType);
                if (networkItemType != null) {
                    gameClient.sendItemConsumption(networkItemType);
                }
            }
        } else {
            System.out.println("Failed to consume " + selectedItemType.name() + " - none in inventory");
        }
    }
    
    /**
     * Convert inventory ItemType to network ItemType.
     * @param inventoryItemType The inventory item type
     * @return The corresponding network item type, or null if not found
     */
    private wagemaker.uk.network.ItemType convertToNetworkItemType(wagemaker.uk.inventory.ItemType inventoryItemType) {
        if (inventoryItemType == null) {
            return null;
        }
        
        switch (inventoryItemType) {
            case APPLE:
                return wagemaker.uk.network.ItemType.APPLE;
            case BANANA:
                return wagemaker.uk.network.ItemType.BANANA;
            case BABY_BAMBOO:
                return wagemaker.uk.network.ItemType.BABY_BAMBOO;
            case BAMBOO_STACK:
                return wagemaker.uk.network.ItemType.BAMBOO_STACK;
            case BABY_TREE:
                return wagemaker.uk.network.ItemType.BABY_TREE;
            case WOOD_STACK:
                return wagemaker.uk.network.ItemType.WOOD_STACK;
            case PEBBLE:
                return wagemaker.uk.network.ItemType.PEBBLE;
            case PALM_FIBER:
                return wagemaker.uk.network.ItemType.PALM_FIBER;
            default:
                return null;
        }
    }
    
    private void checkApplePickups() {
        if (apples != null) {
            // Check all apples for pickup
            for (Map.Entry<String, Apple> entry : apples.entrySet()) {
                Apple apple = entry.getValue();
                String appleKey = entry.getKey();
                
                // Check if player is close enough to pick up apple (32px range)
                float dx = Math.abs((x + 32) - (apple.getX() + 12)); // Player center to apple center
                float dy = Math.abs((y + 32) - (apple.getY() + 12)); // Apple is 24x24, so center is +12
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the apple
                    pickupApple(appleKey);
                    break; // Only pick up one apple per frame
                }
            }
        }
    }
    
    private void pickupApple(String appleKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(appleKey);
            // In multiplayer, server handles item removal and health restoration
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.APPLE);
            }
            
            // Remove apple from game
            if (apples.containsKey(appleKey)) {
                Apple apple = apples.get(appleKey);
                apple.dispose();
                apples.remove(appleKey);
                System.out.println("Apple removed from game");
            }
        }
    }
    
    private void checkAppleSaplingPickups() {
        if (appleSaplings != null) {
            // Check all apple saplings for pickup
            for (Map.Entry<String, AppleSapling> entry : appleSaplings.entrySet()) {
                AppleSapling appleSapling = entry.getValue();
                String appleSaplingKey = entry.getKey();
                
                // Check if player is close enough to pick up apple sapling (32px range)
                float dx = Math.abs((x + 32) - (appleSapling.getX() + 16)); // Player center to apple sapling center
                float dy = Math.abs((y + 32) - (appleSapling.getY() + 16)); // AppleSapling is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the apple sapling
                    pickupAppleSapling(appleSaplingKey);
                    break; // Only pick up one apple sapling per frame
                }
            }
        }
    }
    
    private void pickupAppleSapling(String appleSaplingKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(appleSaplingKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.APPLE_SAPLING);
            }
            
            // Remove apple sapling from game
            if (appleSaplings.containsKey(appleSaplingKey)) {
                AppleSapling appleSapling = appleSaplings.get(appleSaplingKey);
                appleSapling.dispose();
                appleSaplings.remove(appleSaplingKey);
                System.out.println("AppleSapling removed from game");
            }
        }
    }
    
    private void checkBananaPickups() {
        if (bananas != null) {
            // Check all bananas for pickup
            for (Map.Entry<String, Banana> entry : bananas.entrySet()) {
                Banana banana = entry.getValue();
                String bananaKey = entry.getKey();
                
                // Check if player is close enough to pick up banana (32px range)
                float dx = Math.abs((x + 32) - (banana.getX() + 16)); // Player center to banana center
                float dy = Math.abs((y + 32) - (banana.getY() + 16)); // Banana is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the banana
                    pickupBanana(bananaKey);
                    break; // Only pick up one banana per frame
                }
            }
        }
    }
    
    private void checkBananaSaplingPickups() {
        if (bananaSaplings != null) {
            for (Map.Entry<String, BananaSapling> entry : bananaSaplings.entrySet()) {
                BananaSapling bananaSapling = entry.getValue();
                String bananaSaplingKey = entry.getKey();
                
                // Check if player is close enough to pick up (32px range)
                float dx = Math.abs((x + 32) - (bananaSapling.getX() + 16)); // BananaSapling is 32x32, so center is +16
                float dy = Math.abs((y + 32) - (bananaSapling.getY() + 16));
                
                if (dx <= 32 && dy <= 32) {
                    pickupBananaSapling(bananaSaplingKey);
                    break;
                }
            }
        }
    }
    
    private void pickupBananaSapling(String bananaSaplingKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(bananaSaplingKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.BANANA_SAPLING);
            }
            
            // Remove banana sapling from game
            if (bananaSaplings.containsKey(bananaSaplingKey)) {
                BananaSapling bananaSapling = bananaSaplings.get(bananaSaplingKey);
                bananaSapling.dispose();
                bananaSaplings.remove(bananaSaplingKey);
                System.out.println("BananaSapling removed from game");
            }
        }
    }
    
    private void pickupBanana(String bananaKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(bananaKey);
            // In multiplayer, server handles item removal and health restoration
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.BANANA);
            }
            
            // Remove banana from game
            if (bananas.containsKey(bananaKey)) {
                Banana banana = bananas.get(bananaKey);
                banana.dispose();
                bananas.remove(bananaKey);
                System.out.println("Banana removed from game");
            }
        }
    }
    
    private void checkBambooStackPickups() {
        if (bambooStacks != null) {
            // Check all bamboo stacks for pickup
            for (Map.Entry<String, BambooStack> entry : bambooStacks.entrySet()) {
                BambooStack bambooStack = entry.getValue();
                String bambooStackKey = entry.getKey();
                
                // Check if player is close enough to pick up bamboo stack (32px range)
                float dx = Math.abs((x + 32) - (bambooStack.getX() + 16)); // Player center to bamboo stack center
                float dy = Math.abs((y + 32) - (bambooStack.getY() + 16)); // BambooStack is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the bamboo stack
                    pickupBambooStack(bambooStackKey);
                    break; // Only pick up one bamboo stack per frame
                }
            }
        }
    }
    
    private void pickupBambooStack(String bambooStackKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(bambooStackKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.BAMBOO_STACK);
                // Also collect bamboo fence material when harvesting bamboo
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.BAMBOO_FENCE_MATERIAL);
            }
            
            // Remove bamboo stack from game
            if (bambooStacks.containsKey(bambooStackKey)) {
                BambooStack bambooStack = bambooStacks.get(bambooStackKey);
                bambooStack.dispose();
                bambooStacks.remove(bambooStackKey);
                System.out.println("BambooStack removed from game");
            }
        }
    }
    
    private void checkBambooSaplingPickups() {
        if (bambooSaplings != null) {
            // Check all baby bamboos for pickup
            for (Map.Entry<String, BambooSapling> entry : bambooSaplings.entrySet()) {
                BambooSapling bambooSapling = entry.getValue();
                String bambooSaplingKey = entry.getKey();
                
                // Check if player is close enough to pick up baby bamboo (32px range)
                float dx = Math.abs((x + 32) - (bambooSapling.getX() + 16)); // Player center to baby bamboo center
                float dy = Math.abs((y + 32) - (bambooSapling.getY() + 16)); // BambooSapling is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the baby bamboo
                    pickupBambooSapling(bambooSaplingKey);
                    break; // Only pick up one baby bamboo per frame
                }
            }
        }
    }
    
    private void pickupBambooSapling(String bambooSaplingKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(bambooSaplingKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.BABY_BAMBOO);
            }
            
            // Remove baby bamboo from game
            if (bambooSaplings.containsKey(bambooSaplingKey)) {
                BambooSapling bambooSapling = bambooSaplings.get(bambooSaplingKey);
                bambooSapling.dispose();
                bambooSaplings.remove(bambooSaplingKey);
                System.out.println("BambooSapling removed from game");
            }
        }
    }
    
    private void checkTreeSaplingPickups() {
        if (treeSaplings != null) {
            // Check all baby trees for pickup
            for (Map.Entry<String, TreeSapling> entry : treeSaplings.entrySet()) {
                TreeSapling treeSapling = entry.getValue();
                String treeSaplingKey = entry.getKey();
                
                // Check if player is close enough to pick up baby tree (32px range)
                float dx = Math.abs((x + 32) - (treeSapling.getX() + 16)); // Player center to baby tree center
                float dy = Math.abs((y + 32) - (treeSapling.getY() + 16)); // TreeSapling is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the baby tree
                    pickupTreeSapling(treeSaplingKey);
                    break; // Only pick up one baby tree per frame
                }
            }
        }
    }
    
    private void pickupTreeSapling(String treeSaplingKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(treeSaplingKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.BABY_TREE);
            }
            
            // Remove baby tree from game
            if (treeSaplings.containsKey(treeSaplingKey)) {
                TreeSapling treeSapling = treeSaplings.get(treeSaplingKey);
                treeSapling.dispose();
                treeSaplings.remove(treeSaplingKey);
                System.out.println("TreeSapling removed from game");
            }
        }
    }
    
    private void checkWoodStackPickups() {
        if (woodStacks != null) {
            // Check all wood stacks for pickup
            for (Map.Entry<String, WoodStack> entry : woodStacks.entrySet()) {
                WoodStack woodStack = entry.getValue();
                String woodStackKey = entry.getKey();
                
                // Check if player is close enough to pick up wood stack (32px range)
                float dx = Math.abs((x + 32) - (woodStack.getX() + 16)); // Player center to wood stack center
                float dy = Math.abs((y + 32) - (woodStack.getY() + 16)); // WoodStack is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the wood stack
                    pickupWoodStack(woodStackKey);
                    break; // Only pick up one wood stack per frame
                }
            }
        }
    }
    
    private void pickupWoodStack(String woodStackKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(woodStackKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.WOOD_STACK);
                // Also collect wood fence material when harvesting wood
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.WOOD_FENCE_MATERIAL);
            }
            
            // Remove wood stack from game
            if (woodStacks.containsKey(woodStackKey)) {
                WoodStack woodStack = woodStacks.get(woodStackKey);
                woodStack.dispose();
                woodStacks.remove(woodStackKey);
                System.out.println("WoodStack removed from game");
            }
        }
    }

    private void checkPebblePickups() {
        if (pebbles != null) {
            // Check all pebbles for pickup
            for (Map.Entry<String, Pebble> entry : pebbles.entrySet()) {
                Pebble pebble = entry.getValue();
                String pebbleKey = entry.getKey();
                
                // Check if player is close enough to pick up pebble (32px range)
                float dx = Math.abs((x + 32) - (pebble.getX() + 16)); // Player center to pebble center
                float dy = Math.abs((y + 32) - (pebble.getY() + 16)); // Pebble is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the pebble
                    pickupPebble(pebbleKey);
                    break; // Only pick up one pebble per frame
                }
            }
        }
    }
    
    private void pickupPebble(String pebbleKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(pebbleKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.PEBBLE);
            }
            
            // Remove pebble from game
            if (pebbles.containsKey(pebbleKey)) {
                Pebble pebble = pebbles.get(pebbleKey);
                pebble.dispose();
                pebbles.remove(pebbleKey);
                System.out.println("Pebble removed from game");
            }
        }
    }
    
    private void checkPalmFiberPickups() {
        if (palmFibers != null) {
            // Check all palm fibers for pickup
            for (Map.Entry<String, PalmFiber> entry : palmFibers.entrySet()) {
                PalmFiber palmFiber = entry.getValue();
                String palmFiberKey = entry.getKey();
                
                // Check if player is close enough to pick up palm fiber (32px range)
                float dx = Math.abs((x + 32) - (palmFiber.getX() + 16)); // Player center to palm fiber center
                float dy = Math.abs((y + 32) - (palmFiber.getY() + 16)); // PalmFiber is 32x32, so center is +16
                
                if (dx <= 32 && dy <= 32) {
                    // Pick up the palm fiber
                    pickupPalmFiber(palmFiberKey);
                    break; // Only pick up one palm fiber per frame
                }
            }
        }
    }
    
    private void pickupPalmFiber(String palmFiberKey) {
        // Send pickup request to server in multiplayer mode
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            gameClient.sendItemPickup(palmFiberKey);
            // In multiplayer, server handles item removal
            // The server will broadcast the pickup to all clients
        } else {
            // Single-player mode: handle locally via inventory manager
            if (inventoryManager != null) {
                inventoryManager.collectItem(wagemaker.uk.inventory.ItemType.PALM_FIBER);
            }
            
            // Remove palm fiber from game
            if (palmFibers.containsKey(palmFiberKey)) {
                PalmFiber palmFiber = palmFibers.get(palmFiberKey);
                palmFiber.dispose();
                palmFibers.remove(palmFiberKey);
                System.out.println("PalmFiber removed from game");
            }
        }
    }

    public boolean shouldShowHealthBar() {
        return health < 100;
    }
    
    /**
     * Convert Player's internal Direction enum to network Direction enum.
     */
    private wagemaker.uk.network.Direction convertToNetworkDirection(Direction direction) {
        switch (direction) {
            case UP:
                return wagemaker.uk.network.Direction.UP;
            case DOWN:
                return wagemaker.uk.network.Direction.DOWN;
            case LEFT:
                return wagemaker.uk.network.Direction.LEFT;
            case RIGHT:
                return wagemaker.uk.network.Direction.RIGHT;
            default:
                return wagemaker.uk.network.Direction.DOWN;
        }
    }
    
    /**
     * Check if health has changed and send update to server in multiplayer mode.
     */
    private void checkAndSendHealthUpdate() {
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            // Only send update if health has changed
            if (health != previousHealth) {
                gameClient.sendPlayerHealth(health);
                previousHealth = health;
            }
        }
    }
    
    /**
     * Check if hunger has changed and send update to server in multiplayer mode.
     */
    private void checkAndSendHungerUpdate() {
        if (gameClient != null && gameClient.isConnected() && isLocalPlayer) {
            // Only send update if hunger has changed
            if (hunger != previousHunger) {
                gameClient.sendPlayerHunger(hunger);
                previousHunger = hunger;
            }
        }
    }
    
    /**
     * Get the targeting system instance.
     * Used by MyGdxGame for rendering the target indicator.
     * 
     * @return The targeting system instance
     */
    public TargetingSystem getTargetingSystem() {
        return targetingSystem;
    }
    
    /**
     * Get the target indicator renderer instance.
     * Used by MyGdxGame for rendering the target indicator.
     * 
     * @return The target indicator renderer instance
     */
    public TargetIndicatorRenderer getTargetIndicatorRenderer() {
        return targetIndicatorRenderer;
    }

    public void dispose() {
        if (spriteSheet != null) spriteSheet.dispose();
        if (targetIndicatorRenderer != null) targetIndicatorRenderer.dispose();
    }
}