package com.mrsuffix.fishmating.utils;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

/**
 * Utility class for particle effects with maximum compatibility
 */
public class ParticleUtils {

    /**
     * Shows heart particles above a location (breeding ready state)
     * @param location The location to show particles
     * @param count The number of particles
     */
    public static void showHeartParticles(Location location, int count) {
        try {
            World world = location.getWorld();
            if (world != null) {
                Location particleLocation = location.clone().add(0, 1.5, 0);
                spawnParticleSafely(world, "HEART", particleLocation, count, 0.3, 0.3, 0.3, 0);
            }
        } catch (Exception e) {
            // Silently handle particle errors to avoid console spam
        }
    }

    /**
     * Shows particles when a fish consumes a seed
     * @param location The location to show particles
     */
    public static void showConsumptionParticles(Location location) {
        try {
            World world = location.getWorld();
            if (world != null) {
                spawnParticleSafely(world, "VILLAGER_HAPPY", location, 5, 0.2, 0.2, 0.2, 0);
            }
        } catch (Exception e) {
            // Silently handle particle errors
        }
    }

    /**
     * Shows particles during the breeding process
     * @param location The location to show particles
     */
    public static void showBreedingParticles(Location location) {
        try {
            World world = location.getWorld();
            if (world != null) {
                Location particleLocation = location.clone().add(0, 1, 0);
                spawnParticleSafely(world, "HEART", particleLocation, 10, 0.5, 0.5, 0.5, 0);
            }
        } catch (Exception e) {
            // Silently handle particle errors
        }
    }

    /**
     * Shows particles when a baby fish is born
     * @param location The location to show particles
     */
    public static void showBirthParticles(Location location) {
        try {
            World world = location.getWorld();
            if (world != null) {
                spawnParticleSafely(world, "SPELL_WITCH", location, 15, 0.5, 0.5, 0.5, 0.1);
                spawnParticleSafely(world, "VILLAGER_HAPPY", location, 8, 0.3, 0.3, 0.3, 0);
            }
        } catch (Exception e) {
            // Silently handle particle errors
        }
    }

    /**
     * Safely spawns particles with fallback handling
     * @param world The world to spawn particles in
     * @param particleName The particle name
     * @param location The location to spawn at
     * @param count The number of particles
     * @param offsetX X offset
     * @param offsetY Y offset
     * @param offsetZ Z offset
     * @param extra Extra data
     */
    private static void spawnParticleSafely(World world, String particleName, Location location,
                                            int count, double offsetX, double offsetY, double offsetZ, double extra) {
        try {
            Particle particle = Particle.valueOf(particleName);
            world.spawnParticle(particle, location, count, offsetX, offsetY, offsetZ, extra);
        } catch (IllegalArgumentException e) {
            // Fallback to basic particles if the specified one doesn't exist
            try {
                if (particleName.contains("HEART")) {
                    world.spawnParticle(Particle.HEART, location, count, offsetX, offsetY, offsetZ, extra);
                } else {
                    world.spawnParticle(Particle.DRIPPING_HONEY, location, count, offsetX, offsetY, offsetZ, extra);
                }
            } catch (Exception fallbackException) {
                // If even fallback fails, just ignore
            }
        } catch (Exception e) {
            // Ignore any other particle-related errors
        }
    }
}
