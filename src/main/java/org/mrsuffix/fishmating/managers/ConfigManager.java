package com.mrsuffix.fishmating.managers;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages plugin configuration and provides easy access to settings
 */
public class ConfigManager {

    private final JavaPlugin plugin;
    private final Map<EntityType, Material> fishSeedMappings;

    // Configuration values
    private double detectionRadius;
    private int breedingTimeoutSeconds;
    private int breedingCooldownMinutes;
    private boolean enableParticles;
    private int particleCount;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.fishSeedMappings = new HashMap<>();
        loadConfiguration();
    }

    /**
     * Loads configuration from config.yml
     */
    public void loadConfiguration() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        try {
            // Load basic settings
            detectionRadius = plugin.getConfig().getDouble("settings.detection-radius", 5.0);
            breedingTimeoutSeconds = plugin.getConfig().getInt("settings.breeding-timeout-seconds", 30);
            breedingCooldownMinutes = plugin.getConfig().getInt("settings.breeding-cooldown-minutes", 3);
            enableParticles = plugin.getConfig().getBoolean("settings.enable-particles", true);
            particleCount = plugin.getConfig().getInt("settings.particle-count", 5);

            // Load fish-seed mappings
            fishSeedMappings.clear();
            for (String fishType : plugin.getConfig().getConfigurationSection("fish-mappings").getKeys(false)) {
                try {
                    EntityType entityType = EntityType.valueOf(fishType.toUpperCase());
                    String seedName = plugin.getConfig().getString("fish-mappings." + fishType);
                    Material seedMaterial = Material.valueOf(seedName.toUpperCase());

                    fishSeedMappings.put(entityType, seedMaterial);
                    plugin.getLogger().info("Loaded mapping: " + entityType + " -> " + seedMaterial);

                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid fish type or seed material: " + fishType);
                }
            }

            plugin.getLogger().info("Configuration loaded successfully!");

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Gets the seed material for a specific fish type
     * @param fishType The fish entity type
     * @return The corresponding seed material, or null if not found
     */
    public Material getSeedForFish(EntityType fishType) {
        return fishSeedMappings.get(fishType);
    }

    /**
     * Gets the fish type for a specific seed material
     * @param seedMaterial The seed material
     * @return The corresponding fish entity type, or null if not found
     */
    public EntityType getFishForSeed(Material seedMaterial) {
        return fishSeedMappings.entrySet().stream()
                .filter(entry -> entry.getValue() == seedMaterial)
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if a material is a valid breeding seed
     * @param material The material to check
     * @return True if the material is a breeding seed
     */
    public boolean isBreedingSeed(Material material) {
        return fishSeedMappings.containsValue(material);
    }

    // Getters for configuration values
    public double getDetectionRadius() { return detectionRadius; }
    public int getBreedingTimeoutSeconds() { return breedingTimeoutSeconds; }
    public int getBreedingCooldownMinutes() { return breedingCooldownMinutes; }
    public boolean isParticlesEnabled() { return enableParticles; }
    public int getParticleCount() { return particleCount; }
    public Map<EntityType, Material> getFishSeedMappings() { return new HashMap<>(fishSeedMappings); }
}
