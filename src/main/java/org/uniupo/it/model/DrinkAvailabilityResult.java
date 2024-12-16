package org.uniupo.it.model;

public class DrinkAvailabilityResult {
    private final boolean isAvailable;
    private final ConsumableType missingConsumable;
    private final Integer maxSugarAvailable;

    public DrinkAvailabilityResult(boolean isAvailable) {
        this(isAvailable, null, null);
    }

    public DrinkAvailabilityResult(boolean isAvailable, ConsumableType missingConsumable, Integer maxSugarAvailable) {
        this.isAvailable = isAvailable;
        this.missingConsumable = missingConsumable;
        this.maxSugarAvailable = maxSugarAvailable;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public ConsumableType getMissingConsumable() {
        return missingConsumable;
    }

    public Integer getMaxSugarAvailable() {
        return maxSugarAvailable;
    }

    public String toString() {
        return "DrinkAvailabilityResult{" +
                "isAvailable=" + isAvailable +
                ", missingConsumable=" + missingConsumable +
                ", maxSugarAvailable=" + maxSugarAvailable +
                '}';
    }
}