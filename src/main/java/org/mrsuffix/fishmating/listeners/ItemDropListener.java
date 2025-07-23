package com.mrsuffix.fishmating.listeners;

import com.mrsuffix.fishmating.FishMatingPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

/**
 * Handles item drop events to track breeding seeds
 */
public class ItemDropListener implements Listener {

    private final FishMatingPlugin plugin;

    public ItemDropListener(FishMatingPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles item spawn events to identify breeding seeds
     * @param event The item spawn event
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        try {
            Item item = event.getEntity();
            Material itemType = item.getItemStack().getType();

            // Check if this is a breeding seed
            if (plugin.getConfigManager().isBreedingSeed(itemType)) {
                // Check if item is in water
                if (isInWater(item)) {
                    plugin.getLogger().fine("Breeding seed dropped in water: " + itemType);
                    // The FishManager will handle fish attraction in its update cycle
                }
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Error handling item spawn: " + e.getMessage());
        }
    }

    /**
     * Checks if an item is in water
     * @param item The item to check
     * @return True if the item is in water
     */
    private boolean isInWater(Item item) {
        Material blockType = item.getLocation().getBlock().getType();
        return blockType == Material.WATER;
    }
}
