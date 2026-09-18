package com.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Domain entity representing a Room Reservation.
 */
public class Reservation {
    private String reservationId; // e.g. BK-9821A
    private Guest guest;
    private Room room;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int totalNights;
    private double totalPrice;
    private ReservationStatus status;
    private String paymentTransactionId;
    private LocalDateTime createdAt;

    public Reservation() {
        this.status = ReservationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Reservation(String reservationId, Guest guest, Room room, LocalDate checkInDate, LocalDate checkOutDate, int totalNights, double totalPrice, ReservationStatus status, String paymentTransactionId, LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.guest = guest;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.totalNights = totalNights;
        this.totalPrice = totalPrice;
        this.status = status;
        this.paymentTransactionId = paymentTransactionId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public Guest getGuest() {
        return guest;
    }

    public void setGuest(Guest guest) {
        this.guest = guest;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public int getTotalNights() {
        return totalNights;
    }

    public void setTotalNights(int totalNights) {
        this.totalNights = totalNights;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void setStatus(ReservationStatus status) {
        this.status = status;
    }

    public String getPaymentTransactionId() {
        return paymentTransactionId;
    }

    public void setPaymentTransactionId(String paymentTransactionId) {
        this.paymentTransactionId = paymentTransactionId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Checks if this reservation overlaps with a given date range.
     */
    public boolean overlapsWith(LocalDate newCheckIn, LocalDate newCheckOut) {
        if (status == ReservationStatus.CANCELLED) {
            return false;
        }
        // Overlap logic: newCheckIn < existingCheckOut AND newCheckOut > existingCheckIn
        return newCheckIn.isBefore(checkOutDate) && newCheckOut.isAfter(checkInDate);
    }

    @Override
    public String toString() {
        return String.format("Booking ID: %s | Room #%s (%s) | Guest: %s | Dates: %s to %s (%d nights) | Total: $%.2f | Status: %s",
                reservationId, room.getRoomNumber(), room.getCategory().getDisplayName(),
                guest.getName(), checkInDate, checkOutDate, totalNights, totalPrice, status.getLabel());
    }
}
