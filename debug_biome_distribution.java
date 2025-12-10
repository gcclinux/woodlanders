import wagemaker.uk.biome.BiomeManager;
import wagemaker.uk.biome.BiomeType;

class DebugBiomeDistribution {
    public static void main(String[] args) {
        BiomeManager biomeManager = new BiomeManager();
        biomeManager.initialize();
        
        int grassCount = 0;
        int sandCount = 0;
        int waterCount = 0;
        int sampleSize = 1000;
        
        System.out.println("Sampling biome distribution...");
        
        for (int i = 0; i < sampleSize; i++) {
            float worldX = -5000 + (i % 100) * 100;
            float worldY = -5000 + (i / 100) * 100;
            
            BiomeType biomeType = biomeManager.getBiomeAtPosition(worldX, worldY);
            BiomeType baseBiome = biomeManager.getBaseBiomeAtPosition(worldX, worldY);
            
            if (i < 10) {
                System.out.printf("Position (%.0f, %.0f): Base=%s, Final=%s%n", 
                    worldX, worldY, baseBiome, biomeType);
            }
            
            switch (biomeType) {
                case GRASS:
                    grassCount++;
                    break;
                case SAND:
                    sandCount++;
                    break;
                case WATER:
                    waterCount++;
                    break;
            }
        }
        
        double grassPercent = (grassCount * 100.0) / sampleSize;
        double sandPercent = (sandCount * 100.0) / sampleSize;
        double waterPercent = (waterCount * 100.0) / sampleSize;
        
        System.out.printf("Distribution: Grass=%.1f%%, Sand=%.1f%%, Water=%.1f%%\n", 
            grassPercent, sandPercent, waterPercent);
        
        biomeManager.dispose();
    }
}