package wagemaker.uk.planting;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

public class PlantedBananaTree {
    private float x, y;
    private float growthTimer;
    private static final float GROWTH_DURATION = 120.0f;
    
    private static Texture sharedTexture = null;
    private static int instanceCount = 0;

    public PlantedBananaTree(float x, float y) {
        this.x = snapToTileGrid(x);
        this.y = snapToTileGrid(y);
        this.growthTimer = 0.0f;
        
        if (sharedTexture == null) {
            createSharedTexture();
        }
        instanceCount++;
    }

    private float snapToTileGrid(float coordinate) {
        return (float) (Math.floor(coordinate / 64.0) * 64.0);
    }

    private static synchronized void createSharedTexture() {
        if (sharedTexture != null) {
            return;
        }
        
        try {
            Texture spriteSheet = new Texture("sprites/assets.png");
            Pixmap pixmap = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
            spriteSheet.getTextureData().prepare();
            Pixmap sheetPixmap = spriteSheet.getTextureData().consumePixmap();
            
            // BananaSapling coordinates: 192 from left, 192 from top, 64x64 size
            pixmap.drawPixmap(sheetPixmap, 0, 0, 192, 192, 64, 64);
            
            sharedTexture = new Texture(pixmap);
            pixmap.dispose();
            sheetPixmap.dispose();
            spriteSheet.dispose();
            
            System.out.println("[PlantedBananaTree] Shared texture created successfully");
        } catch (Exception e) {
            System.err.println("[PlantedBananaTree] Failed to create shared texture: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean update(float deltaTime) {
        growthTimer += deltaTime;
        return growthTimer >= GROWTH_DURATION;
    }

    public boolean isReadyToTransform() {
        return growthTimer >= GROWTH_DURATION;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getGrowthTimer() {
        return growthTimer;
    }

    public void setGrowthTimer(float growthTimer) {
        this.growthTimer = growthTimer;
    }

    public Texture getTexture() {
        return sharedTexture;
    }

    public void dispose() {
        instanceCount--;
        
        if (instanceCount <= 0 && sharedTexture != null) {
            System.out.println("[PlantedBananaTree] All instances disposed, keeping shared texture for reuse");
        }
    }
    
    public static void disposeSharedTexture() {
        if (sharedTexture != null) {
            sharedTexture.dispose();
            sharedTexture = null;
            instanceCount = 0;
            System.out.println("[PlantedBananaTree] Shared texture disposed");
        }
    }
}
