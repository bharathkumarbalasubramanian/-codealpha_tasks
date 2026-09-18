package com.hotel.model;

import java.time.LocalDateTime;

/**
 * Domain entity representing a Payment transaction.
 */
public class Payment {
    private String transactionId;
    private String reservationId;
    private double amount;
    private String paymentMethod; // e.g. Credit Card, Debit Card, UPI, PayPal, Cash
    private String paymentStatus; // SUCCESS, FAILED, REFUNDED
    private LocalDateTime timestamp;

    public Payment() {}

    public Payment(String transactionId, String reservationId, double amount, String paymentMethod, String paymentStatus, LocalDateTime timestamp) {
        this.transactionId = transactionId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentStatus = paymentStatus;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("Transaction %s | Booking: %s | Amount: $%.2f | Method: %s | Status: %s",
                transactionId, reservationId, amount, paymentMethod, paymentStatus);
    }
}
