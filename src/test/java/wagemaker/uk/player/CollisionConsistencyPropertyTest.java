package wagemaker.uk.player;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import wagemaker.uk.biome.BiomeManager;
import wagemaker.uk.biome.BiomeType;
import wagemaker.uk.objects.Stone;
import wagemaker.uk.trees.SmallTree;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Property-based test for collision consistency across different terrain types.
 * 
 * Feature: water-lake-biome, Property 6: Collision consistency
 * Validates: Requirements 2.4
 */
@RunWith(JUnitQuickcheck.class)
public class CollisionConsistencyPropertyTest {
    
    private static HeadlessApplication application;
    private static boolean graphicsAvailable = true;
    
    @BeforeClass
    public static void setUpGdx() {
        try {
            // Initialize headless LibGDX application for testing
            HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
            application = new HeadlessApplication(new ApplicationAdapter() {}, config);
            
            // Mock GL20 to prevent null pointer exceptions
            Gdx.gl = Mockito.mock(GL20.class);
            Gdx.gl20 = Mockito.mock(GL20.class);
        } catch (Exception e) {
            graphicsAvailable = false;
        }
    }
    
    @AfterClass
    public static void tearDownGdx() {
        if (application != null) {
            application.exit();
            application = null;
        }
    }
    
    /**
     * Property 6: Collision consistency
     * For any impassable terrain type (water, tree, stone), the collision response 
     * should be identical: player position remains unchanged and wouldCollide returns true.
     * 
     * Validates: Requirements 2.4
     */
    @Property(trials = 100)
    public void allImpassableTerrainBlocksMovementConsistently(float worldX, float worldY, int terrainType) {
        // Skip test if graphics context is not available (headless mode)
        Assume.assumeTrue("Skipping test - graphics context not available", graphicsAvailable);
        
        // Constrain coordinates to reasonable range
        worldX = Math.abs(worldX % 10000.0f);
        worldY = Math.abs(worldY % 10000.0f);
        
        // Constrain terrain type to 0-2 (water, tree, stone)
        terrainType = Math.abs(terrainType % 3);
        
        // Create BiomeManager and initialize
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        // Create a player at a safe position
        OrthographicCamera camera = new OrthographicCamera();
        Player player = new Player(0, 0, camera);
        player.setBiomeManager(biomeManager);
        
        try {
            // Set up the impassable terrain based on type
            boolean shouldCollide = false;
            float playerX = worldX - 32; // Player center offset
            float playerY = worldY - 32;
            
            switch (terrainType) {
                case 0: // Water
                    BiomeType biomeType = biomeManager.getBiomeAtPosition(worldX, worldY);
                    if (biomeType == BiomeType.WATER) {
                        shouldCollide = true;
                    }
                    break;
                    
                case 1: // Tree
                    Map<String, SmallTree> trees = new HashMap<>();
                    SmallTree tree = new SmallTree(worldX, worldY);
                    trees.put("test-tree", tree);
                    player.setTrees(trees);
                    shouldCollide = true;
                    break;
                    
                case 2: // Stone
                    Map<String, Stone> stones = new HashMap<>();
                    Stone stone = new Stone(worldX, worldY);
                    stones.put("test-stone", stone);
                    player.setStones(stones);
                    shouldCollide = true;
                    break;
            }
            
            // Only test if we have an impassable terrain
            if (!shouldCollide && terrainType == 0) {
                return; // Skip if not water
            }
            
            // Access private wouldCollide method using reflection
            Method wouldCollideMethod = Player.class.getDeclaredMethod("wouldCollide", float.class, float.class);
            wouldCollideMethod.setAccessible(true);
            
            // Test collision detection
            boolean collides = (boolean) wouldCollideMethod.invoke(player, playerX, playerY);
            
            // Verify that all impassable terrain blocks movement consistently
            assertTrue(collides, 
                String.format("Impassable terrain type %d at (%.2f, %.2f) should block player movement", 
                    terrainType, worldX, worldY));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to test collision consistency at (" + worldX + ", " + worldY + ")", e);
        } finally {
            player.dispose();
            biomeManager.dispose();
        }
    }
}
