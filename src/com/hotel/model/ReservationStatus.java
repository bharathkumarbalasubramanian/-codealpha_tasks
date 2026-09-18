package com.hotel.model;

/**
 * Status of a Hotel Reservation.
 */
public enum ReservationStatus {
    PENDING("Pending Payment"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled"),
    COMPLETED("Completed");

    private final String label;

    ReservationStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
