package com.mrsuffix.fishmating.models;

import org.bukkit.entity.Entity;

/**
 * Represents a pair of fish that are breeding
 */
public class BreedingPair {

    private final Entity fish1;
    private final Entity fish2;
    private final long creationTime;

    public BreedingPair(Entity fish1, Entity fish2) {
        this.fish1 = fish1;
        this.fish2 = fish2;
        this.creationTime = System.currentTimeMillis();
    }

    /**
     * Gets the first fish in the pair
     * @return The first fish entity
     */
    public Entity getFish1() {
        return fish1;
    }

    /**
     * Gets the second fish in the pair
     * @return The second fish entity
     */
    public Entity getFish2() {
        return fish2;
    }

    /**
     * Gets the creation time of the pair
     * @return The creation time in milliseconds
     */
    public long getCreationTime() {
        return creationTime;
    }

    /**
     * Checks if the pair contains a specific fish
     * @param fish The fish to check
     * @return True if the pair contains the fish
     */
    public boolean contains(Entity fish) {
        return fish1.equals(fish) || fish2.equals(fish);
    }

    /**
     * Checks if both fish in the pair are still valid
     * @return True if both fish are valid and alive
     */
    public boolean isValid() {
        return fish1.isValid() && !fish1.isDead() &&
                fish2.isValid() && !fish2.isDead();
    }
}
