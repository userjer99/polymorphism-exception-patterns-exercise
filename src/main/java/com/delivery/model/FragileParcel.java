package com.delivery.model;

import com.delivery.exception.DeliveryServiceNotAllowedException;
import com.delivery.exception.InvalidParcelException;
import com.delivery.strategy.DeliveryStrategy;

/**
 * Represents a Fragile parcel.
 * - MUST extend BoxParcel (as specified in requirements)
 * - Maximum weight: 30 kg
 * - Additional field: handlingNote (string, non-empty)
 * - Chargeable weight: same rule as Box
 * - Base fee: Box base fee + Php 100 handling fee
 */
public class FragileParcel extends BoxParcel {
    public static final double MAX_FRAGILE_WEIGHT = 30.0;
    public static final double HANDLING_FEE = 100.0;

    private final String handlingNote;

    public FragileParcel(String trackingNumber, String receiverName, double weight,
                         DeliveryStrategy deliveryStrategy, int length, int width, int height,
                         String handlingNote)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        super(trackingNumber, receiverName, weight, deliveryStrategy, length, width, height);

        if (weight > MAX_FRAGILE_WEIGHT) {
            throw new InvalidParcelException("Fragile parcel weight cannot exceed 30 kg! Provided: " + weight + " kg");
        }
        if (handlingNote == null || handlingNote.trim().isEmpty()) {
            throw new InvalidParcelException("Handling note cannot be empty!");
        }

        this.handlingNote = handlingNote.trim();
        validateServiceEligibility();
    }

    public String getHandlingNote() {
        return handlingNote;
    }

    @Override
    public double getBaseFee() {
        return super.getBaseFee() + HANDLING_FEE;
    }

    @Override
    public String getParcelType() {
        return "Fragile";
    }
}
