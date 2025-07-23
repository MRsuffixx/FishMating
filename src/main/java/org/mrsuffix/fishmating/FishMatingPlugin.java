package com.mrsuffix.fishmating;

import com.mrsuffix.fishmating.listeners.EntityListener;
import com.mrsuffix.fishmating.listeners.ItemDropListener;
import com.mrsuffix.fishmating.managers.BreedingManager;
import com.mrsuffix.fishmating.managers.ConfigManager;
import com.mrsuffix.fishmating.managers.FishManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * FishMating Plugin - Simulates fish breeding triggered by throwing seeds into water
 *
 * @author mrsuffix
 * @version 1.0.0
 * @since 1.21.3
 */
public class FishMatingPlugin extends JavaPlugin {

    private static FishMatingPlugin instance;
    private ConfigManager configManager;
    private FishManager fishManager;
    private BreedingManager breedingManager;

    @Override
    public void onEnable() {
        instance = this;

        try {
            // Initialize managers
            this.configManager = new ConfigManager(this);
            this.fishManager = new FishManager(this);
            this.breedingManager = new BreedingManager(this);

            // Register event listeners
            getServer().getPluginManager().registerEvents(new ItemDropListener(this), this);
            getServer().getPluginManager().registerEvents(new EntityListener(this), this);

            getLogger().info("FishMating plugin has been enabled successfully!");
            getLogger().info("Version: " + getDescription().getVersion());
            getLogger().info("Author: " + getDescription().getAuthors().get(0));

        } catch (Exception e) {
            getLogger().severe("Failed to enable FishMating plugin: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        try {
            // Clean up managers
            if (breedingManager != null) {
                breedingManager.shutdown();
            }
            if (fishManager != null) {
                fishManager.shutdown();
            }

            getLogger().info("FishMating plugin has been disabled successfully!");

        } catch (Exception e) {
            getLogger().warning("Error during plugin disable: " + e.getMessage());
        }

        instance = null;
    }

    /**
     * Gets the plugin instance
     * @return The plugin instance
     */
    public static FishMatingPlugin getInstance() {
        return instance;
    }

    /**
     * Gets the configuration manager
     * @return The configuration manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gets the fish manager
     * @return The fish manager
     */
    public FishManager getFishManager() {
        return fishManager;
    }

    /**
     * Gets the breeding manager
     * @return The breeding manager
     */
    public BreedingManager getBreedingManager() {
        return breedingManager;
    }
}
