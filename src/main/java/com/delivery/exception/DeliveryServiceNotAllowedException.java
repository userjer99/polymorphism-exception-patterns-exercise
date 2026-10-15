package com.delivery.exception;

/**
 * Thrown when a delivery service strategy cannot be applied to a parcel
 * (e.g., Same Day delivery requested for a parcel weighing over 10 kg).
 */
public class DeliveryServiceNotAllowedException extends ParcelException {
    public DeliveryServiceNotAllowedException(String message) {
        super(message);
    }
}
