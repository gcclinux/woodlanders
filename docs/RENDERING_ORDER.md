# Rendering Order Documentation

## Current Rendering Order (MyGdxGame.java, render() method)

**Location**: `src/main/java/wagemaker/uk/gdx/MyGdxGame.java` - lines ~830-950

```java
batch.begin();
// 1. BACKGROUND LAYER
drawInfiniteGrass();
batch.end();

// 2. PUDDLES LAYER (between batch operations)
rainSystem.renderPuddles(camera);

batch.begin();
// 3. TARGET INDICATOR
if (player.getTargetingSystem().isActive()) {
    player.getTargetIndicatorRenderer().render(batch, targetCoords[0], targetCoords[1], isValid);
}

// 4. PLANTED ITEMS LAYER
drawPlantedBamboos();
drawPlantedTrees();
drawPlantedBananaTrees();
drawPlantedAppleTrees();

// 5. RESPAWN INDICATORS
if (respawnManager != null) {
    respawnManager.renderIndicators(batch, deltaTime, ...);
}

// 6. FENCE STRUCTURES (MOVED TO BEFORE WORLD OBJECTS)
if (fenceBuildingManager != null) {
    fenceBuildingManager.renderFences(batch);
}

// 7. WORLD OBJECTS LAYER
drawTrees();           // Small trees
drawCoconutTrees();    // Coconut trees
drawBambooTrees();     // Bamboo trees
drawStones();          // Stone objects
drawApples();          // Apple items
drawAppleSaplings();   // Apple saplings
drawBananas();         // Banana items
drawBananaSaplings();  // Banana saplings
drawBambooStacks();    // Bamboo stack items
drawBambooSaplings();  // Bamboo saplings
drawTreeSaplings();    // Tree saplings
drawWoodStacks();      // Wood stack items
drawPebbles();         // Pebble items
drawPalmFibers();      // Palm fiber items
drawCactus();          // Cactus object

// 8. PLAYER LAYER
batch.draw(player.getCurrentFrame(), player.getX(), player.getY(), 100, 100);
renderRemotePlayers();

// 9. FOREGROUND TREES (above player)
drawAppleTrees();      // Apple trees (foliage above player)
drawBananaTrees();     // Banana trees (foliage above player)

batch.end();

// 10. EFFECTS LAYER (after batch.end())
fenceBuildingManager.renderVisualEffects();
rainSystem.render(camera);
birdFormationManager.render(batch);

// 11. UI LAYER
// Player name tags, health bars, compass, inventory, menus, etc.
```

## Rendering Layer Summary

1. **Background Layer**:
   - Infinite grass background (`drawInfiniteGrass()`)
   - Puddles (`rainSystem.renderPuddles()`)

2. **Ground Objects Layer**:
   - Target indicator (white/red dots) - `player.getTargetIndicatorRenderer().render()`
   - Planted bamboos (`drawPlantedBamboos()`)
   - Planted trees (`drawPlantedTrees()`)
   - Planted banana trees (`drawPlantedBananaTrees()`)
   - Planted apple trees (`drawPlantedAppleTrees()`)
   - Respawn indicators

3. **Structures Layer**:
   - **Fences** (`fenceBuildingManager.renderFences(batch)`) - **BEFORE WORLD OBJECTS**

4. **World Objects Layer**:
   - Trees (`drawTrees()`)
   - Coconut trees (`drawCoconutTrees()`)
   - Bamboo trees (`drawBambooTrees()`)
   - Stones (`drawStones()`)
   - Items (apples, bananas, bamboo stacks, saplings, wood stacks, pebbles, palm fibers)
   - Cactus (`drawCactus()`)

5. **Characters Layer**:
   - **Player** (`batch.draw(player.getCurrentFrame(), ...)`) - **AFTER WORLD OBJECTS**
   - Remote players (`renderRemotePlayers()`)

6. **Foreground Objects Layer**:
   - Apple trees (`drawAppleTrees()`) - renders above player for foliage effect
   - Banana trees (`drawBananaTrees()`)

7. **Effects Layer**:
   - Fence visual effects (`fenceBuildingManager.renderVisualEffects()`)
   - Rain effects (`rainSystem.render()`)
   - Birds (`birdFormationManager.render()`)

8. **UI Layer**:
   - Player name tags
   - Health bars
   - Connection quality indicator
   - Compass
   - **Inventory UI** (`inventoryRenderer.render()`)
   - **Fence inventory UI** (`fenceItemRenderer.render()`)
   - Game menu
   - Notifications

## Recent Changes

**Fence Rendering Position**: Moved fence structures from rendering after world objects to rendering **before** world objects. This means:

- **Fences now appear behind trees and cactus** (trees/cactus render on top of fences)
- **Player still appears above fences** (unchanged)
- **All world objects (trees, stones, items, cactus) now render above fences**

The fence structures are now part of the background/terrain layer rather than appearing above world objects.