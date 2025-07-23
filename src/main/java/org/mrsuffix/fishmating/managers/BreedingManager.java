package com.mrsuffix.fishmating.managers;

import com.mrsuffix.fishmating.FishMatingPlugin;
import com.mrsuffix.fishmating.models.BreedingPair;
import com.mrsuffix.fishmating.models.FishData;
import com.mrsuffix.fishmating.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

/**
 * Manages fish breeding pairs and breeding logic
 */
public class BreedingManager {

    private final FishMatingPlugin plugin;
    private final Set<BreedingPair> activeBreedingPairs;
    private BukkitTask breedingCheckTask;

    public BreedingManager(FishMatingPlugin plugin) {
        this.plugin = plugin;
        this.activeBreedingPairs = ConcurrentHashMap.newKeySet();
        startBreedingCheckTask();
    }

    /**
     * Starts the periodic task that checks for breeding opportunities
     */
    private void startBreedingCheckTask() {
        breedingCheckTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            try {
                checkForBreedingOpportunities();
                cleanupInvalidPairs();
            } catch (Exception e) {
                plugin.getLogger().warning("Error in breeding check task: " + e.getMessage());
            }
        }, 20L, 20L); // Run every second
    }

    /**
     * Checks for breeding opportunities among all breeding-ready fish
     */
    /**
     * Checks for breeding opportunities among all breeding-ready fish
     */
    private void checkForBreedingOpportunities() {
        ConfigManager config = plugin.getConfigManager();
        FishManager fishManager = plugin.getFishManager();

        try {
            // Get all breeding-ready fish grouped by type
            for (EntityType fishType : config.getFishSeedMappings().keySet()) {
                List<FishData> breedingReadyFish = new ArrayList<>();

                // Collect all breeding-ready fish of this type from all worlds
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity.getType() == fishType && entity.isValid() && !entity.isDead()) {
                            FishData fishData = fishManager.getFishData(entity);
                            if (fishData.isBreedingReady() && !isInBreedingPair(entity)) {
                                breedingReadyFish.add(fishData);
                            }
                        }
                    }
                }

                // Check for breeding pairs
                if (breedingReadyFish.size() >= 2) {
                    checkBreedingPairs(breedingReadyFish, config);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking breeding opportunities: " + e.getMessage());
        }
    }


    /**
     * Checks for breeding pairs among breeding-ready fish of the same type
     * @param breedingReadyFish List of breeding-ready fish
     * @param config Configuration manager
     */
    private void checkBreedingPairs(List<FishData> breedingReadyFish, ConfigManager config) {
        double radius = config.getDetectionRadius();

        for (int i = 0; i < breedingReadyFish.size(); i++) {
            FishData fish1Data = breedingReadyFish.get(i);
            Entity fish1 = fish1Data.getEntity();

            for (int j = i + 1; j < breedingReadyFish.size(); j++) {
                FishData fish2Data = breedingReadyFish.get(j);
                Entity fish2 = fish2Data.getEntity();

                // Check if fish are within breeding range
                if (fish1.getLocation().distance(fish2.getLocation()) <= radius) {
                    // Create breeding pair and initiate breeding
                    BreedingPair pair = new BreedingPair(fish1, fish2);
                    activeBreedingPairs.add(pair);

                    // Start breeding process
                    initiateBreeding(fish1Data, fish2Data, pair);

                    // Remove from consideration for this cycle
                    breedingReadyFish.remove(fish2Data);
                    break;
                }
            }
        }
    }

    /**
     * Initiates the breeding process for a pair of fish
     * @param fish1Data First fish data
     * @param fish2Data Second fish data
     * @param pair The breeding pair
     */
    private void initiateBreeding(FishData fish1Data, FishData fish2Data, BreedingPair pair) {
        Entity fish1 = fish1Data.getEntity();
        Entity fish2 = fish2Data.getEntity();

        // Show breeding particles
        if (plugin.getConfigManager().isParticlesEnabled()) {
            ParticleUtils.showBreedingParticles(fish1.getLocation());
            ParticleUtils.showBreedingParticles(fish2.getLocation());
        }

        // Schedule baby spawning after a short delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pair.isValid()) {
                spawnBabyFish(fish1, fish2);
                completeBreeding(fish1Data, fish2Data, pair);
            }
        }, 40L); // 2 seconds delay
    }

    /**
     * Spawns a baby fish between two parent fish
     * @param parent1 First parent fish
     * @param parent2 Second parent fish
     */
    private void spawnBabyFish(Entity parent1, Entity parent2) {
        try {
            // Calculate spawn location (midpoint between parents)
            Location spawnLocation = parent1.getLocation().clone()
                    .add(parent2.getLocation().toVector())
                    .multiply(0.5);

            // Spawn baby fish
            Entity baby = parent1.getWorld().spawnEntity(spawnLocation, parent1.getType());

            // Set baby properties if supported
            if (baby instanceof org.bukkit.entity.Ageable) {
                ((org.bukkit.entity.Ageable) baby).setBaby();
            }

            // Show birth particles
            if (plugin.getConfigManager().isParticlesEnabled()) {
                ParticleUtils.showBirthParticles(spawnLocation);
            }

            plugin.getLogger().fine("Baby fish spawned at " + spawnLocation);

        } catch (Exception e) {
            plugin.getLogger().warning("Error spawning baby fish: " + e.getMessage());
        }
    }

    /**
     * Completes the breeding process for a pair of fish
     * @param fish1Data First fish data
     * @param fish2Data Second fish data
     * @param pair The breeding pair
     */
    private void completeBreeding(FishData fish1Data, FishData fish2Data, BreedingPair pair) {
        // Set breeding cooldown for both fish
        fish1Data.setLastBreedingTime();
        fish2Data.setLastBreedingTime();

        // Remove from active breeding pairs
        activeBreedingPairs.remove(pair);

        plugin.getLogger().fine("Breeding completed for pair");
    }

    /**
     * Checks if a fish is currently in a breeding pair
     * @param fish The fish entity to check
     * @return True if the fish is in an active breeding pair
     */
    private boolean isInBreedingPair(Entity fish) {
        return activeBreedingPairs.stream()
                .anyMatch(pair -> pair.contains(fish));
    }

    /**
     * Removes invalid breeding pairs from the active set
     */
    private void cleanupInvalidPairs() {
        activeBreedingPairs.removeIf(pair -> !pair.isValid());
    }

    /**
     * Shuts down the breeding manager
     */
    public void shutdown() {
        if (breedingCheckTask != null && !breedingCheckTask.isCancelled()) {
            breedingCheckTask.cancel();
        }
        activeBreedingPairs.clear();
    }

    /**
     * Gets the number of active breeding pairs
     * @return The number of active breeding pairs
     */
    public int getActiveBreedingPairCount() {
        return activeBreedingPairs.size();
    }
}
