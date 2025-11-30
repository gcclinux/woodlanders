package wagemaker.uk.network;

import java.io.Serializable;

/**
 * Represents the state of a planted banana tree in the multiplayer world.
 * Used for synchronizing planted banana trees across clients and for world persistence.
 */
public class PlantedBananaTreeState implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String plantedBananaTreeId;
    private float x;
    private float y;
    private float growthTimer;
    
    public PlantedBananaTreeState() {
    }
    
    public PlantedBananaTreeState(String plantedBananaTreeId, float x, float y, float growthTimer) {
        this.plantedBananaTreeId = plantedBananaTreeId;
        this.x = x;
        this.y = y;
        this.growthTimer = growthTimer;
    }
    
    public String getPlantedBananaTreeId() {
        return plantedBananaTreeId;
    }
    
    public void setPlantedBananaTreeId(String plantedBananaTreeId) {
        this.plantedBananaTreeId = plantedBananaTreeId;
    }
    
    public float getX() {
        return x;
    }
    
    public void setX(float x) {
        this.x = x;
    }
    
    public float getY() {
        return y;
    }
    
    public void setY(float y) {
        this.y = y;
    }
    
    public float getGrowthTimer() {
        return growthTimer;
    }
    
    public void setGrowthTimer(float growthTimer) {
        this.growthTimer = growthTimer;
    }
}
