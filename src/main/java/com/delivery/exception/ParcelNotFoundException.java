package com.delivery.exception;

/**
 * Thrown when a parcel with the specified tracking number cannot be found.
 */
public class ParcelNotFoundException extends ParcelException {
    private final String trackingNumber;

    public ParcelNotFoundException(String trackingNumber) {
        super("Parcel not found!");
        this.trackingNumber = trackingNumber;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
}
