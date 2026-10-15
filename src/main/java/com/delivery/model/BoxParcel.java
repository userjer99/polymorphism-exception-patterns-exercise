package com.delivery.model;

import com.delivery.exception.DeliveryServiceNotAllowedException;
import com.delivery.exception.InvalidParcelException;
import com.delivery.strategy.DeliveryStrategy;

/**
 * Represents a standard Box parcel.
 * - Maximum weight: 50 kg
 * - Additional fields: Length, Width, Height (cm, integer > 0)
 * - Volumetric weight = (Length × Width × Height) / 3500.0
 * - Chargeable weight = max(actual weight, volumetric weight) rounded up to whole kg
 * - Base fee = Php 40 × chargeable weight
 */
public class BoxParcel extends Parcel {
    public static final double MAX_WEIGHT = 50.0;
    public static final double RATE_PER_KG = 40.0;
    public static final double VOLUMETRIC_DIVISOR = 3500.0;

    protected final int length;
    protected final int width;
    protected final int height;

    public BoxParcel(String trackingNumber, String receiverName, double weight,
                     DeliveryStrategy deliveryStrategy, int length, int width, int height)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        super(trackingNumber, receiverName, weight, deliveryStrategy);

        if (weight > getMaxWeight()) {
            throw new InvalidParcelException(getParcelType() + " weight cannot exceed " + getMaxWeight() + " kg! Provided: " + weight + " kg");
        }
        if (length <= 0 || width <= 0 || height <= 0) {
            throw new InvalidParcelException("Dimensions must be positive integers! (Length, width, and height > 0)");
        }

        this.length = length;
        this.width = width;
        this.height = height;

        validateServiceEligibility();
    }

    public int getLength() {
        return length;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    protected double getMaxWeight() {
        return MAX_WEIGHT;
    }

    public double getVolumetricWeight() {
        return ((double) length * width * height) / VOLUMETRIC_DIVISOR;
    }

    @Override
    public double getChargeableWeight() {
        double maxWeight = Math.max(weight, getVolumetricWeight());
        return Math.ceil(maxWeight);
    }

    @Override
    public double getBaseFee() {
        return RATE_PER_KG * getChargeableWeight();
    }

    @Override
    public String getParcelType() {
        return "Box";
    }
}
