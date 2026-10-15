package com.delivery.strategy;

/**
 * Enumeration of available delivery services with factory methods to create their strategies.
 */
public enum DeliveryServiceType {
    STANDARD("Standard", new StandardDeliveryStrategy()),
    EXPRESS("Express", new ExpressDeliveryStrategy()),
    SAME_DAY("Same Day", new SameDayDeliveryStrategy());

    private final String displayName;
    private final DeliveryStrategy strategy;

    DeliveryServiceType(String displayName, DeliveryStrategy strategy) {
        this.displayName = displayName;
        this.strategy = strategy;
    }

    public String getDisplayName() {
        return displayName;
    }

    public DeliveryStrategy getStrategy() {
        return strategy;
    }

    public static DeliveryServiceType fromOption(int option) {
        switch (option) {
            case 1:
                return STANDARD;
            case 2:
                return EXPRESS;
            case 3:
                return SAME_DAY;
            default:
                throw new IllegalArgumentException("Invalid delivery service option: " + option);
        }
    }

    @Override
    public String toString() {
        return displayName;
    }
}
