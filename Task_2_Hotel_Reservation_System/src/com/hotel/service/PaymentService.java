package com.hotel.service;

import com.hotel.model.Payment;
import com.hotel.model.Reservation;
import com.hotel.repository.HotelRepository;
import com.hotel.util.DateUtils;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service managing payment processing simulation and invoice/receipt generation.
 */
public class PaymentService {
    private final HotelRepository repository;

    public PaymentService(HotelRepository repository) {
        this.repository = repository;
    }

    /**
     * Simulates payment processing for a booking.
     */
    public Payment processPayment(String reservationId, double amount, String paymentMethod, String accountOrCardDetails) {
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Payment Simulation Logic (99% success rate simulation)
        boolean isSuccess = simulateBankProcessing(paymentMethod, accountOrCardDetails);
        String status = isSuccess ? "SUCCESS" : "FAILED";

        Payment payment = new Payment(txnId, reservationId, amount, paymentMethod, status, LocalDateTime.now());
        repository.savePayment(payment);
        return payment;
    }

    private boolean simulateBankProcessing(String method, String details) {
        // Basic simulation validation
        if (details != null && details.trim().toLowerCase().contains("fail")) {
            return false;
        }
        return true;
    }

    public Payment getPaymentByReservationId(String reservationId) {
        return repository.getPaymentByReservationId(reservationId);
    }

    /**
     * Generates a beautifully formatted receipt string.
     */
    public String generateReceipt(Reservation reservation, Payment payment) {
        if (reservation == null) return "No reservation details available.";

        double subtotal = reservation.getTotalPrice();
        double tax = subtotal * 0.12; // 12% Hotel Occupancy Tax
        double grandTotal = subtotal + tax;

        StringBuilder sb = new StringBuilder();
        sb.append("=====================================================\n");
        sb.append("               LUXURY HOTEL & SUITES                \n");
        sb.append("                OFFICIAL PAYMENT RECEIPT            \n");
        sb.append("=====================================================\n");
        sb.append(String.format("Receipt Date:   %s\n", payment != null ? DateUtils.formatDateTime(payment.getTimestamp()) : DateUtils.formatDateTime(LocalDateTime.now())));
        sb.append(String.format("Transaction ID: %s\n", payment != null ? payment.getTransactionId() : "N/A"));
        sb.append(String.format("Booking ID:     %s\n", reservation.getReservationId()));
        sb.append(String.format("Payment Method: %s\n", payment != null ? payment.getPaymentMethod() : "N/A"));
        sb.append(String.format("Payment Status: %s\n", payment != null ? payment.getPaymentStatus() : "N/A"));
        sb.append("-----------------------------------------------------\n");
        sb.append("GUEST INFORMATION:\n");
        sb.append(String.format("  Name:  %s\n", reservation.getGuest().getName()));
        sb.append(String.format("  Email: %s\n", reservation.getGuest().getEmail()));
        sb.append(String.format("  Phone: %s\n", reservation.getGuest().getPhone()));
        sb.append("-----------------------------------------------------\n");
        sb.append("STAY DETAILS:\n");
        sb.append(String.format("  Room Number:   #%s (%s)\n", reservation.getRoom().getRoomNumber(), reservation.getRoom().getCategory().getDisplayName()));
        sb.append(String.format("  Check-In:      %s\n", DateUtils.formatDate(reservation.getCheckInDate())));
        sb.append(String.format("  Check-Out:     %s\n", DateUtils.formatDate(reservation.getCheckOutDate())));
        sb.append(String.format("  Total Nights:  %d night(s)\n", reservation.getTotalNights()));
        sb.append(String.format("  Rate/Night:    $%.2f\n", reservation.getRoom().getPricePerNight()));
        sb.append("-----------------------------------------------------\n");
        sb.append("BILLING SUMMARY:\n");
        sb.append(String.format("  Room Subtotal: $%.2f\n", subtotal));
        sb.append(String.format("  Taxes & Fees:  $%.2f (12%%)\n", tax));
        sb.append(String.format("  GRAND TOTAL:   $%.2f\n", grandTotal));
        sb.append("=====================================================\n");
        sb.append("         Thank you for staying with us!              \n");
        sb.append("=====================================================\n");

        return sb.toString();
    }
}
