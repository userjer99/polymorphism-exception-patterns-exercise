package com.delivery.strategy;

import com.delivery.exception.DeliveryServiceNotAllowedException;

/**
 * Strategy interface for calculating parcel delivery fees.
 * Demonstrates the Strategy Design Pattern.
 */
public interface DeliveryStrategy {
    /**
     * Gets the display name of the delivery service.
     */
    String getServiceName();

    /**
     * Validates whether the delivery service can be used for the given chargeable weight.
     *
     * @param chargeableWeight the chargeable weight in kg
     * @throws DeliveryServiceNotAllowedException if the parcel exceeds eligibility constraints
     */
    void validateEligibility(double chargeableWeight) throws DeliveryServiceNotAllowedException;

    /**
     * Calculates the final delivery fee based on the parcel's base fee.
     *
     * @param baseFee the base fee of the parcel
     * @return the total delivery fee in Philippine Pesos (Php)
     */
    double calculateFee(double baseFee);
}
