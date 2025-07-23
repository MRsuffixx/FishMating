package com.mrsuffix.fishmating.models;

import org.bukkit.entity.Entity;

/**
 * Represents data associated with a fish entity for breeding purposes
 */
public class FishData {

    private final Entity entity;
    private boolean breedingReady;
    private long breedingReadyTime;
    private long lastBreedingTime;
    private Entity targetSeed;

    public FishData(Entity entity) {
        this.entity = entity;
        this.breedingReady = false;
        this.breedingReadyTime = 0;
        this.lastBreedingTime = 0;
        this.targetSeed = null;
    }

    /**
     * Gets the fish entity
     * @return The fish entity
     */
    public Entity getEntity() {
        return entity;
    }

    /**
     * Checks if the fish is ready for breeding
     * @return True if ready for breeding
     */
    public boolean isBreedingReady() {
        return breedingReady;
    }

    /**
     * Sets the breeding ready state
     * @param breedingReady The breeding ready state
     */
    public void setBreedingReady(boolean breedingReady) {
        this.breedingReady = breedingReady;
        if (breedingReady) {
            this.breedingReadyTime = System.currentTimeMillis();
        } else {
            this.breedingReadyTime = 0;
        }
    }

    /**
     * Gets the time when the fish became breeding ready
     * @return The breeding ready time in milliseconds
     */
    public long getBreedingReadyTime() {
        return breedingReadyTime;
    }

    /**
     * Gets the last breeding time
     * @return The last breeding time in milliseconds
     */
    public long getLastBreedingTime() {
        return lastBreedingTime;
    }

    /**
     * Sets the last breeding time to current time
     */
    public void setLastBreedingTime() {
        this.lastBreedingTime = System.currentTimeMillis();
        this.breedingReady = false;
        this.breedingReadyTime = 0;
    }

    /**
     * Checks if the fish can breed (not in cooldown)
     * @param cooldownMinutes The cooldown period in minutes
     * @return True if the fish can breed
     */
    public boolean canBreed(int cooldownMinutes) {
        if (lastBreedingTime == 0) return true;
        long cooldownMillis = cooldownMinutes * 60 * 1000L;
        return System.currentTimeMillis() - lastBreedingTime >= cooldownMillis;
    }

    /**
     * Gets the target seed entity
     * @return The target seed entity
     */
    public Entity getTargetSeed() {
        return targetSeed;
    }

    /**
     * Sets the target seed entity
     * @param targetSeed The target seed entity
     */
    public void setTargetSeed(Entity targetSeed) {
        this.targetSeed = targetSeed;
    }

    /**
     * Checks if the breeding ready state has timed out
     * @param timeoutSeconds The timeout period in seconds
     * @return True if timed out
     */
    public boolean hasBreedingTimedOut(int timeoutSeconds) {
        if (!breedingReady || breedingReadyTime == 0) return false;
        long timeoutMillis = timeoutSeconds * 1000L;
        return System.currentTimeMillis() - breedingReadyTime >= timeoutMillis;
    }
}
