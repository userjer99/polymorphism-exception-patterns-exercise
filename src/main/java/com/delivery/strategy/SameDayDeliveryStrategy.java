package com.delivery.strategy;

import com.delivery.exception.DeliveryServiceNotAllowedException;

/**
 * Concrete Strategy: Same Day Delivery.
 * Delivery fee = (Base fee * 2) + Php 100.
 * Allowed only for parcels with a chargeable weight <= 10.0 kg.
 */
public class SameDayDeliveryStrategy implements DeliveryStrategy {

    public static final double MAX_CHARGEABLE_WEIGHT = 10.0;
    public static final double SURCHARGE = 100.0;

    @Override
    public String getServiceName() {
        return "Same Day";
    }

    @Override
    public void validateEligibility(double chargeableWeight) throws DeliveryServiceNotAllowedException {
        if (chargeableWeight > MAX_CHARGEABLE_WEIGHT) {
            throw new DeliveryServiceNotAllowedException("Same Day delivery is only for parcels up to 10 kg!");
        }
    }

    @Override
    public double calculateFee(double baseFee) {
        return (baseFee * 2.0) + SURCHARGE;
    }

    @Override
    public String toString() {
        return getServiceName();
    }
}
