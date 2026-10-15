package com.delivery.strategy;

import com.delivery.exception.DeliveryServiceNotAllowedException;

/**
 * Concrete Strategy: Express Delivery.
 * Delivery fee = Base fee * 1.5.
 */
public class ExpressDeliveryStrategy implements DeliveryStrategy {

    @Override
    public String getServiceName() {
        return "Express";
    }

    @Override
    public void validateEligibility(double chargeableWeight) throws DeliveryServiceNotAllowedException {
        // Express delivery has no additional weight restrictions
    }

    @Override
    public double calculateFee(double baseFee) {
        return baseFee * 1.5;
    }

    @Override
    public String toString() {
        return getServiceName();
    }
}
