package com.mrsuffix.fishmating.managers;

import com.mrsuffix.fishmating.FishMatingPlugin;
import com.mrsuffix.fishmating.models.FishData;
import com.mrsuffix.fishmating.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages fish behavior, movement, and seed interaction
 */
public class FishManager {

    private final FishMatingPlugin plugin;
    private final Map<UUID, FishData> fishDataMap;
    private BukkitTask fishUpdateTask;

    public FishManager(FishMatingPlugin plugin) {
        this.plugin = plugin;
        this.fishDataMap = new ConcurrentHashMap<>();
        startFishUpdateTask();
    }

    /**
     * Starts the periodic task that updates fish behavior
     */
    private void startFishUpdateTask() {
        fishUpdateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                updateAllFish();
            } catch (Exception e) {
                plugin.getLogger().warning("Error in fish update task: " + e.getMessage());
            }
        }, 20L, 10L); // Run every 0.5 seconds
    }

    /**
     * Updates all tracked fish
     */
    private void updateAllFish() {
        ConfigManager config = plugin.getConfigManager();

        // Clean up invalid fish data
        fishDataMap.entrySet().removeIf(entry -> {
            Entity entity = entry.getValue().getEntity();
            return !entity.isValid() || entity.isDead();
        });

        // Update each fish
        for (FishData fishData : fishDataMap.values()) {
            try {
                updateFish(fishData, config);
            } catch (Exception e) {
                plugin.getLogger().warning("Error updating fish: " + e.getMessage());
            }
        }
    }

    /**
     * Updates a single fish's behavior
     * @param fishData The fish data to update
     * @param config The configuration manager
     */
    private void updateFish(FishData fishData, ConfigManager config) {
        Entity fish = fishData.getEntity();

        // Check for breeding timeout
        if (fishData.hasBreedingTimedOut(config.getBreedingTimeoutSeconds())) {
            fishData.setBreedingReady(false);
            fishData.setTargetSeed(null);
        }

        // Show particles if breeding ready
        if (fishData.isBreedingReady() && config.isParticlesEnabled()) {
            ParticleUtils.showHeartParticles(fish.getLocation(), config.getParticleCount());
        }

        // Find and move toward seeds if not breeding ready
        if (!fishData.isBreedingReady() && fishData.canBreed(config.getBreedingCooldownMinutes())) {
            handleSeedSeeking(fishData, config);
        }
    }

    /**
     * Handles fish seeking behavior for seeds
     * @param fishData The fish data
     * @param config The configuration manager
     */
    private void handleSeedSeeking(FishData fishData, ConfigManager config) {
        Entity fish = fishData.getEntity();
        Material requiredSeed = config.getSeedForFish(fish.getType());

        if (requiredSeed == null) return;

        // Check if current target is still valid
        Entity currentTarget = fishData.getTargetSeed();
        if (currentTarget != null && (!currentTarget.isValid() || currentTarget.isDead())) {
            fishData.setTargetSeed(null);
            currentTarget = null;
        }

        // Find nearest matching seed if no current target
        if (currentTarget == null) {
            Item nearestSeed = findNearestSeed(fish.getLocation(), requiredSeed, config.getDetectionRadius());
            if (nearestSeed != null) {
                fishData.setTargetSeed(nearestSeed);
                currentTarget = nearestSeed;
            }
        }

        // Move toward target seed
        if (currentTarget instanceof Item) {
            Item seedItem = (Item) currentTarget;
            moveTowardSeed(fish, seedItem);

            // Check if fish reached the seed
            if (fish.getLocation().distance(seedItem.getLocation()) <= 1.5) {
                consumeSeed(fishData, seedItem);
            }
        }
    }

    /**
     * Finds the nearest seed of the specified type within radius
     * @param location The center location
     * @param seedType The seed material type
     * @param radius The search radius
     * @return The nearest seed item, or null if none found
     */
    private Item findNearestSeed(Location location, Material seedType, double radius) {
        return location.getWorld().getNearbyEntities(location, radius, radius, radius)
                .stream()
                .filter(entity -> entity instanceof Item)
                .map(entity -> (Item) entity)
                .filter(item -> item.getItemStack().getType() == seedType)
                .filter(item -> isInWater(item.getLocation()))
                .min((item1, item2) -> Double.compare(
                        location.distance(item1.getLocation()),
                        location.distance(item2.getLocation())
                ))
                .orElse(null);
    }

    /**
     * Moves a fish toward a seed item
     * @param fish The fish entity
     * @param seed The seed item
     */
    private void moveTowardSeed(Entity fish, Item seed) {
        Location fishLoc = fish.getLocation();
        Location seedLoc = seed.getLocation();

        Vector direction = seedLoc.toVector().subtract(fishLoc.toVector()).normalize();
        Vector velocity = direction.multiply(0.3); // Moderate speed

        // Ensure fish stays in water
        velocity.setY(Math.max(velocity.getY(), -0.1));

        fish.setVelocity(velocity);
    }

    /**
     * Handles fish consuming a seed
     * @param fishData The fish data
     * @param seedItem The seed item
     */
    private void consumeSeed(FishData fishData, Item seedItem) {
        try {
            // Reduce seed stack size
            int currentAmount = seedItem.getItemStack().getAmount();
            if (currentAmount > 1) {
                seedItem.getItemStack().setAmount(currentAmount - 1);
            } else {
                seedItem.remove();
            }

            // Set fish as breeding ready
            fishData.setBreedingReady(true);
            fishData.setTargetSeed(null);

            // Show consumption particles
            if (plugin.getConfigManager().isParticlesEnabled()) {
                ParticleUtils.showConsumptionParticles(fishData.getEntity().getLocation());
            }

            plugin.getLogger().fine("Fish consumed seed and is now breeding ready");

        } catch (Exception e) {
            plugin.getLogger().warning("Error during seed consumption: " + e.getMessage());
        }
    }

    /**
     * Checks if a location is in water
     * @param location The location to check
     * @return True if the location is in water
     */
    private boolean isInWater(Location location) {
        Material blockType = location.getBlock().getType();
        return blockType == Material.WATER;
    }

    /**
     * Gets or creates fish data for an entity
     * @param fish The fish entity
     * @return The fish data
     */
    public FishData getFishData(Entity fish) {
        return fishDataMap.computeIfAbsent(fish.getUniqueId(), uuid -> new FishData(fish));
    }

    /**
     * Removes fish data for an entity
     * @param fish The fish entity
     */
    public void removeFishData(Entity fish) {
        fishDataMap.remove(fish.getUniqueId());
    }

    /**
     * Gets all breeding-ready fish of a specific type within radius of a location
     * @param location The center location
     * @param fishType The fish type
     * @param radius The search radius
     * @return List of breeding-ready fish
     */
    public List<FishData> getBreedingReadyFish(Location location, EntityType fishType, double radius) {
        return fishDataMap.values().stream()
                .filter(fishData -> fishData.getEntity().getType() == fishType)
                .filter(FishData::isBreedingReady)
                .filter(fishData -> fishData.getEntity().getLocation().distance(location) <= radius)
                .collect(Collectors.toList());
    }

    /**
     * Shuts down the fish manager
     */
    public void shutdown() {
        if (fishUpdateTask != null && !fishUpdateTask.isCancelled()) {
            fishUpdateTask.cancel();
        }
        fishDataMap.clear();
    }
}
