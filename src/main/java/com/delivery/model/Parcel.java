package com.delivery.model;

import com.delivery.exception.DeliveryServiceNotAllowedException;
import com.delivery.exception.InvalidParcelException;
import com.delivery.strategy.DeliveryStrategy;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Abstract base class representing a generic Parcel.
 * Demonstrates Polymorphism and encapsulates common parcel attributes and behaviors.
 */
public abstract class Parcel {
    public static final Pattern TRACKING_NUMBER_PATTERN = Pattern.compile("^ABC\\d{4}$");

    protected final String trackingNumber;
    protected final String receiverName;
    protected final double weight;
    protected DeliveryStrategy deliveryStrategy;
    protected ParcelStatus status;

    public Parcel(String trackingNumber, String receiverName, double weight, DeliveryStrategy deliveryStrategy)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        this.trackingNumber = validateTrackingNumber(trackingNumber);
        this.receiverName = validateReceiverName(receiverName);
        this.weight = validateWeight(weight);
        this.status = ParcelStatus.PENDING;

        Objects.requireNonNull(deliveryStrategy, "Delivery strategy cannot be null");
        this.deliveryStrategy = deliveryStrategy;
    }

    /**
     * Completes post-construction validation, including strategy eligibility on calculated chargeable weight.
     */
    protected void validateServiceEligibility() throws DeliveryServiceNotAllowedException {
        this.deliveryStrategy.validateEligibility(getChargeableWeight());
    }

    private static String validateTrackingNumber(String trackingNumber) throws InvalidParcelException {
        if (trackingNumber == null || !TRACKING_NUMBER_PATTERN.matcher(trackingNumber.trim()).matches()) {
            throw new InvalidParcelException("Tracking number must start with 'ABC' followed by 4 digits (e.g., ABC0001)!");
        }
        return trackingNumber.trim();
    }

    private static String validateReceiverName(String receiverName) throws InvalidParcelException {
        if (receiverName == null || receiverName.trim().isEmpty()) {
            throw new InvalidParcelException("Receiver name cannot be empty!");
        }
        return receiverName.trim();
    }

    private static double validateWeight(double weight) throws InvalidParcelException {
        if (Double.isNaN(weight) || Double.isInfinite(weight) || weight <= 0) {
            throw new InvalidParcelException("Weight must be greater than 0 kg!");
        }
        return weight;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public double getWeight() {
        return weight;
    }

    public DeliveryStrategy getDeliveryStrategy() {
        return deliveryStrategy;
    }

    public ParcelStatus getStatus() {
        return status;
    }

    public boolean isDelivered() {
        return this.status == ParcelStatus.DELIVERED;
    }

    public void markAsDelivered() {
        this.status = ParcelStatus.DELIVERED;
    }

    /**
     * Changes the delivery service strategy for this parcel.
     *
     * @param newStrategy the new delivery strategy to apply
     * @throws DeliveryServiceNotAllowedException if the parcel exceeds the new service's weight threshold
     */
    public void changeDeliveryService(DeliveryStrategy newStrategy) throws DeliveryServiceNotAllowedException {
        Objects.requireNonNull(newStrategy, "Delivery strategy cannot be null");
        newStrategy.validateEligibility(getChargeableWeight());
        this.deliveryStrategy = newStrategy;
    }

    /**
     * Polymorphic method to calculate the parcel's chargeable weight.
     *
     * @return the chargeable weight in kg
     */
    public abstract double getChargeableWeight();

    /**
     * Polymorphic method to calculate the parcel's base fee in Php.
     *
     * @return base fee
     */
    public abstract double getBaseFee();

    /**
     * Calculates the final delivery fee using the Strategy pattern.
     *
     * @return total delivery fee in Php
     */
    public double calculateDeliveryFee() {
        return deliveryStrategy.calculateFee(getBaseFee());
    }

    /**
     * Returns the parcel type name (e.g. "Document", "Box", "Fragile").
     */
    public abstract String getParcelType();
}
