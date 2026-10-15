package com.delivery.exception;

/**
 * Base checked exception for the Parcel Delivery System.
 */
public class ParcelException extends Exception {
    public ParcelException(String message) {
        super(message);
    }

    public ParcelException(String message, Throwable cause) {
        super(message, cause);
    }
}
