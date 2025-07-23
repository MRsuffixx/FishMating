package com.mrsuffix.fishmating.listeners;

import com.mrsuffix.fishmating.FishMatingPlugin;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;

/**
 * Handles entity-related events for fish tracking
 */
public class EntityListener implements Listener {

    private final FishMatingPlugin plugin;

    public EntityListener(FishMatingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles entity spawn events to track new fish
     * @param event The entity spawn event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        try {
            EntityType entityType = event.getEntity().getType();

            // Check if this is a trackable fish type
            if (plugin.getConfigManager().getSeedForFish(entityType) != null) {
                // Fish data will be created automatically when needed by FishManager
                plugin.getLogger().fine("New fish spawned: " + entityType);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error handling entity spawn: " + e.getMessage());
        }
    }

    /**
     * Handles entity death events to clean up fish data
     * @param event The entity death event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        try {
            EntityType entityType = event.getEntity().getType();

            // Check if this was a trackable fish type
            if (plugin.getConfigManager().getSeedForFish(entityType) != null) {
                plugin.getFishManager().removeFishData(event.getEntity());
                plugin.getLogger().fine("Fish died, data cleaned up: " + entityType);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error handling entity death: " + e.getMessage());
        }
    }
}
