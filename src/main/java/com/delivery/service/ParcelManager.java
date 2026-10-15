package com.delivery.service;

import com.delivery.exception.DuplicateTrackingNumberException;
import com.delivery.exception.ParcelNotFoundException;
import com.delivery.model.Parcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton repository and manager for Parcel entities in the system.
 * Uses the Bill Pugh Singleton Pattern (Initialization-on-demand holder).
 * Eliminates nested if-else checks while guaranteeing thread safety.
 */
public class ParcelManager {
    private final Map<String, Parcel> parcels;

    /**
     * Private constructor to enforce Singleton pattern.
     */
    private ParcelManager() {
        this.parcels = new LinkedHashMap<>();
    }

    /**
     * Lazy holder class ensuring thread-safe, robust initialization.
     */
    private static class InstanceHolder {
        private static final ParcelManager INSTANCE = new ParcelManager();
    }

    /**
     * Retrieves the single instance of ParcelManager.
     *
     * @return the ParcelManager instance
     */
    public static ParcelManager getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * Adds a new parcel to the system.
     *
     * @param parcel the parcel to add
     * @throws DuplicateTrackingNumberException if a parcel with the same tracking number already exists
     */
    public synchronized void addParcel(Parcel parcel) throws DuplicateTrackingNumberException {
        if (parcel == null) {
            throw new IllegalArgumentException("Parcel cannot be null");
        }
        String key = parcel.getTrackingNumber().toUpperCase();
        if (parcels.containsKey(key)) {
            throw new DuplicateTrackingNumberException(parcel.getTrackingNumber());
        }
        parcels.put(key, parcel);
    }

    /**
     * Finds a parcel by tracking number (case-insensitive).
     *
     * @param trackingNumber the tracking number to search
     * @return the found Parcel
     * @throws ParcelNotFoundException if no parcel matches the tracking number
     */
    public synchronized Parcel findParcel(String trackingNumber) throws ParcelNotFoundException {
        if (trackingNumber == null) {
            throw new ParcelNotFoundException("null");
        }
        String key = trackingNumber.trim().toUpperCase();
        Parcel parcel = parcels.get(key);
        if (parcel == null) {
            throw new ParcelNotFoundException(trackingNumber.trim());
        }
        return parcel;
    }

    /**
     * Checks if a tracking number is already registered in the system.
     *
     * @param trackingNumber the tracking number to check
     * @return true if already exists, false otherwise
     */
    public synchronized boolean hasTrackingNumber(String trackingNumber) {
        if (trackingNumber == null) {
            return false;
        }
        return parcels.containsKey(trackingNumber.trim().toUpperCase());
    }

    /**
     * Returns an unmodifiable list of all registered parcels in insertion order.
     *
     * @return list of parcels
     */
    public synchronized List<Parcel> getAllParcels() {
        return Collections.unmodifiableList(new ArrayList<>(parcels.values()));
    }

    /**
     * Returns the total count of registered parcels.
     */
    public synchronized int getParcelCount() {
        return parcels.size();
    }

    /**
     * Clears all registered parcels (useful for testing).
     */
    public synchronized void clear() {
        parcels.clear();
    }
}
