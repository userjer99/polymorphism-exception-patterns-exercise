package com.delivery.model;

import com.delivery.exception.DeliveryServiceNotAllowedException;
import com.delivery.exception.InvalidParcelException;
import com.delivery.strategy.DeliveryStrategy;

/**
 * Represents a Document parcel.
 * - Maximum weight: 0.5 kg
 * - Additional field: numberOfPages (1 to 100)
 * - Chargeable weight: actual weight
 * - Base fee: Php 50 flat
 */
public class DocumentParcel extends Parcel {
    public static final double MAX_WEIGHT = 0.5;
    public static final double FLAT_BASE_FEE = 50.0;
    public static final int MIN_PAGES = 1;
    public static final int MAX_PAGES = 100;

    private final int numberOfPages;

    public DocumentParcel(String trackingNumber, String receiverName, double weight,
                          DeliveryStrategy deliveryStrategy, int numberOfPages)
            throws InvalidParcelException, DeliveryServiceNotAllowedException {
        super(trackingNumber, receiverName, weight, deliveryStrategy);

        if (weight > MAX_WEIGHT) {
            throw new InvalidParcelException("Document weight cannot exceed 0.5 kg! Provided: " + weight + " kg");
        }
        if (numberOfPages < MIN_PAGES || numberOfPages > MAX_PAGES) {
            throw new InvalidParcelException("Number of pages must be between 1 and 100! Provided: " + numberOfPages);
        }

        this.numberOfPages = numberOfPages;
        validateServiceEligibility();
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    @Override
    public double getChargeableWeight() {
        return weight;
    }

    @Override
    public double getBaseFee() {
        return FLAT_BASE_FEE;
    }

    @Override
    public String getParcelType() {
        return "Document";
    }
}
