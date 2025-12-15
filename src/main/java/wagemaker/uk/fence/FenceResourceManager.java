package wagemaker.uk.fence;

import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.Pool;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resource manager for fence system that handles memory management, resource
 * cleanup,
 * and object pooling for frequently used fence components.
 */
public class FenceResourceManager implements Disposable {

    /** Singleton instance */
    private static FenceResourceManager instance;

    /** Shared texture atlas for all fence renderers */
    private FenceTextureAtlas sharedTextureAtlas;

    /** Reference count for the shared texture atlas */
    private int atlasReferenceCount = 0;

    /** Object pools for frequently created objects */
    private final Map<Class<?>, Pool<?>> objectPools;

    /** Active fence structure managers for cleanup tracking */
    private final Map<String, FenceStructureManager> activeStructureManagers;

    /** Active fence renderers for cleanup tracking */
    private final Map<String, FenceRenderer> activeRenderers;

    /** Flag indicating if the resource manager has been disposed */
    private boolean disposed = false;

    /**
     * Private constructor for singleton pattern.
     */
    private FenceResourceManager() {
        this.objectPools = new ConcurrentHashMap<>();
        this.activeStructureManagers = new ConcurrentHashMap<>();
        this.activeRenderers = new ConcurrentHashMap<>();
        initializeObjectPools();
    }

    /**
     * Gets the singleton instance of the fence resource manager.
     * 
     * @return The resource manager instance
     */
    public static synchronized FenceResourceManager getInstance() {
        if (instance == null) {
            instance = new FenceResourceManager();
        }
        return instance;
    }

    /**
     * Initializes object pools for frequently used objects.
     */
    private void initializeObjectPools() {
        // Pool for Point objects used in grid calculations
        objectPools.put(java.awt.Point.class, new Pool<java.awt.Point>() {
            @Override
            protected java.awt.Point newObject() {
                return new java.awt.Point();
            }
        });

        // Pool for Rectangle objects used in collision detection
        objectPools.put(com.badlogic.gdx.math.Rectangle.class, new Pool<com.badlogic.gdx.math.Rectangle>() {
            @Override
            protected com.badlogic.gdx.math.Rectangle newObject() {
                return new com.badlogic.gdx.math.Rectangle();
            }
        });

        // Pool for Vector2 objects used in coordinate conversion
        objectPools.put(com.badlogic.gdx.math.Vector2.class, new Pool<com.badlogic.gdx.math.Vector2>() {
            @Override
            protected com.badlogic.gdx.math.Vector2 newObject() {
                return new com.badlogic.gdx.math.Vector2();
            }
        });
    }

    /**
     * Gets a shared texture atlas instance, creating it if necessary.
     * Uses reference counting to manage the atlas lifecycle.
     * 
     * @return The shared texture atlas
     */
    public synchronized FenceTextureAtlas getSharedTextureAtlas() {
        if (disposed) {
            throw new IllegalStateException("Resource manager has been disposed");
        }

        if (sharedTextureAtlas == null) {
            sharedTextureAtlas = new FenceTextureAtlas();
        }

        atlasReferenceCount++;
        return sharedTextureAtlas;
    }

    /**
     * Releases a reference to the shared texture atlas.
     * Disposes the atlas when no more references exist.
     */
    public synchronized void releaseSharedTextureAtlas() {
        if (atlasReferenceCount > 0) {
            atlasReferenceCount--;

            if (atlasReferenceCount == 0 && sharedTextureAtlas != null) {
                sharedTextureAtlas.dispose();
                sharedTextureAtlas = null;
            }
        }
    }

    /**
     * Gets an object from the specified pool.
     * 
     * @param clazz The class of object to obtain
     * @param <T>   The type of object
     * @return A pooled object instance
     */
    @SuppressWarnings("unchecked")
    public <T> T obtain(Class<T> clazz) {
        Pool<T> pool = (Pool<T>) objectPools.get(clazz);
        if (pool != null) {
            return pool.obtain();
        }

        // If no pool exists, create object directly
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getSimpleName(), e);
        }
    }

    /**
     * Returns an object to its pool for reuse.
     * 
     * @param object The object to return to the pool
     * @param <T>    The type of object
     */
    @SuppressWarnings("unchecked")
    public <T> void free(T object) {
        if (object == null) {
            return;
        }

        Pool<T> pool = (Pool<T>) objectPools.get(object.getClass());
        if (pool != null) {
            pool.free(object);
        }
        // If no pool exists, object will be garbage collected normally
    }

    /**
     * Registers a fence structure manager for cleanup tracking.
     * 
     * @param id      Unique identifier for the manager
     * @param manager The structure manager to register
     */
    public void registerStructureManager(String id, FenceStructureManager manager) {
        if (!disposed) {
            activeStructureManagers.put(id, manager);
        }
    }

    /**
     * Unregisters a fence structure manager.
     * 
     * @param id The identifier of the manager to unregister
     */
    public void unregisterStructureManager(String id) {
        activeStructureManagers.remove(id);
    }

    /**
     * Registers a fence renderer for cleanup tracking.
     * 
     * @param id       Unique identifier for the renderer
     * @param renderer The renderer to register
     */
    public void registerRenderer(String id, FenceRenderer renderer) {
        if (!disposed) {
            activeRenderers.put(id, renderer);
        }
    }

    /**
     * Unregisters a fence renderer.
     * 
     * @param id The identifier of the renderer to unregister
     */
    public void unregisterRenderer(String id) {
        activeRenderers.remove(id);
    }

    /**
     * Cleans up all fence structures on world unload.
     * Disposes of all fence pieces and clears structure data.
     */
    public void cleanupOnWorldUnload() {
        System.out.println("Cleaning up fence resources on world unload...");

        // Clean up all registered structure managers
        for (Map.Entry<String, FenceStructureManager> entry : activeStructureManagers.entrySet()) {
            FenceStructureManager manager = entry.getValue();
            if (manager != null) {
                manager.clear(); // This disposes all fence pieces
                System.out.println("Cleaned up structure manager: " + entry.getKey());
            }
        }

        // Clear object pools to free memory
        clearObjectPools();

        System.out.println("Fence resource cleanup completed");
    }

    /**
     * Clears all object pools to free pooled objects.
     */
    private void clearObjectPools() {
        for (Map.Entry<Class<?>, Pool<?>> entry : objectPools.entrySet()) {
            Pool<?> pool = entry.getValue();
            pool.clear();
        }
    }

    /**
     * Gets memory usage statistics for the fence system.
     * 
     * @return Memory usage information
     */
    public MemoryStats getMemoryStats() {
        int totalPooledObjects = 0;
        Map<String, Integer> poolSizes = new HashMap<>();

        for (Map.Entry<Class<?>, Pool<?>> entry : objectPools.entrySet()) {
            String className = entry.getKey().getSimpleName();
            int poolSize = entry.getValue().getFree();
            poolSizes.put(className, poolSize);
            totalPooledObjects += poolSize;
        }

        return new MemoryStats(
                activeStructureManagers.size(),
                activeRenderers.size(),
                atlasReferenceCount,
                totalPooledObjects,
                poolSizes);
    }

    /**
     * Forces garbage collection and clears unused resources.
     * Should be used sparingly as it can impact performance.
     */
    public void forceCleanup() {
        clearObjectPools();
        System.gc(); // Suggest garbage collection
        System.out.println("Forced fence resource cleanup completed");
    }

    /**
     * Checks if the resource manager has been disposed.
     * 
     * @return true if disposed, false otherwise
     */
    public boolean isDisposed() {
        return disposed;
    }

    @Override
    public void dispose() {
        if (disposed) {
            return;
        }

        System.out.println("Disposing fence resource manager...");

        // Dispose all registered renderers
        for (Map.Entry<String, FenceRenderer> entry : activeRenderers.entrySet()) {
            FenceRenderer renderer = entry.getValue();
            if (renderer != null) {
                renderer.dispose();
            }
        }
        activeRenderers.clear();

        // Dispose all registered structure managers
        for (Map.Entry<String, FenceStructureManager> entry : activeStructureManagers.entrySet()) {
            FenceStructureManager manager = entry.getValue();
            if (manager != null) {
                manager.dispose();
            }
        }
        activeStructureManagers.clear();

        // Dispose shared texture atlas
        if (sharedTextureAtlas != null) {
            sharedTextureAtlas.dispose();
            sharedTextureAtlas = null;
        }
        atlasReferenceCount = 0;

        // Clear object pools
        clearObjectPools();
        objectPools.clear();

        disposed = true;
        instance = null;

        System.out.println("Fence resource manager disposed");
    }

    /**
     * Memory usage statistics for the fence system.
     */
    public static class MemoryStats {
        private final int activeStructureManagers;
        private final int activeRenderers;
        private final int atlasReferences;
        private final int totalPooledObjects;
        private final Map<String, Integer> poolSizes;

        public MemoryStats(int activeStructureManagers, int activeRenderers,
                int atlasReferences, int totalPooledObjects,
                Map<String, Integer> poolSizes) {
            this.activeStructureManagers = activeStructureManagers;
            this.activeRenderers = activeRenderers;
            this.atlasReferences = atlasReferences;
            this.totalPooledObjects = totalPooledObjects;
            this.poolSizes = new HashMap<>(poolSizes);
        }

        public int getActiveStructureManagers() {
            return activeStructureManagers;
        }

        public int getActiveRenderers() {
            return activeRenderers;
        }

        public int getAtlasReferences() {
            return atlasReferences;
        }

        public int getTotalPooledObjects() {
            return totalPooledObjects;
        }

        public Map<String, Integer> getPoolSizes() {
            return new HashMap<>(poolSizes);
        }

        @Override
        public String toString() {
            return String.format("MemoryStats[managers=%d, renderers=%d, atlasRefs=%d, pooled=%d]",
                    activeStructureManagers, activeRenderers, atlasReferences, totalPooledObjects);
        }
    }
}