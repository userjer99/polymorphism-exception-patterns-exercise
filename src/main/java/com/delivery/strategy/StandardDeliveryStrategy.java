package com.delivery.strategy;

import com.delivery.exception.DeliveryServiceNotAllowedException;

/**
 * Concrete Strategy: Standard Delivery.
 * Delivery fee = Base fee.
 */
public class StandardDeliveryStrategy implements DeliveryStrategy {

    @Override
    public String getServiceName() {
        return "Standard";
    }

    @Override
    public void validateEligibility(double chargeableWeight) throws DeliveryServiceNotAllowedException {
        // Standard delivery has no additional weight restrictions
    }

    @Override
    public double calculateFee(double baseFee) {
        return baseFee;
    }

    @Override
    public String toString() {
        return getServiceName();
    }
}
