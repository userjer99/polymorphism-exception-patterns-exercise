package com.delivery.exception;

/**
 * Thrown when parcel input data violates domain validation rules.
 */
public class InvalidParcelException extends ParcelException {
    public InvalidParcelException(String message) {
        super(message);
    }
}
