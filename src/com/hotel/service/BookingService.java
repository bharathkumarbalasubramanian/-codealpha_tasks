package com.hotel.service;

import com.hotel.model.*;
import com.hotel.repository.HotelRepository;
import com.hotel.util.DateUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service managing room reservations, date collision checks, and cancellations.
 */
public class BookingService {
    private final HotelRepository repository;
    private final PaymentService paymentService;

    public BookingService(HotelRepository repository, PaymentService paymentService) {
        this.repository = repository;
        this.paymentService = paymentService;
    }

    /**
     * Creates a new booking with automatic date overlap validation.
     */
    public Reservation createReservation(String guestName, String guestEmail, String guestPhone,
                                          String roomId, LocalDate checkIn, LocalDate checkOut,
                                          String paymentMethod, String paymentDetails) throws Exception {

        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }

        Room room = repository.getRoomById(roomId);
        if (room == null) {
            throw new IllegalArgumentException("Room with ID " + roomId + " does not exist.");
        }
        if (!room.isAvailable()) {
            throw new IllegalStateException("Room #" + room.getRoomNumber() + " is currently out of service.");
        }

        // Check date collisions across existing confirmed bookings
        List<Reservation> allReservations = repository.getAllReservations();
        for (Reservation res : allReservations) {
            if (res.getRoom().getRoomId().equalsIgnoreCase(roomId) && res.getStatus() == ReservationStatus.CONFIRMED) {
                if (res.overlapsWith(checkIn, checkOut)) {
                    throw new IllegalStateException(String.format(
                            "Room #%s is already reserved from %s to %s.",
                            room.getRoomNumber(), res.getCheckInDate(), res.getCheckOutDate()));
                }
            }
        }

        // Get or Create Guest
        Guest guest = repository.findGuestByEmail(guestEmail);
        if (guest == null) {
            String guestId = "G" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            guest = new Guest(guestId, guestName, guestEmail, guestPhone);
            repository.saveGuest(guest);
        }

        // Calculate nights and total price
        int nights = DateUtils.calculateNights(checkIn, checkOut);
        double totalPrice = room.getPricePerNight() * nights;

        // Generate Booking ID
        String reservationId = "BK-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();

        // Process Payment Simulation
        Payment payment = paymentService.processPayment(reservationId, totalPrice, paymentMethod, paymentDetails);

        ReservationStatus status = payment.getPaymentStatus().equalsIgnoreCase("SUCCESS")
                ? ReservationStatus.CONFIRMED
                : ReservationStatus.PENDING;

        Reservation reservation = new Reservation(
                reservationId, guest, room, checkIn, checkOut, nights, totalPrice, status, payment.getTransactionId(), LocalDateTime.now()
        );

        repository.saveReservation(reservation);
        return reservation;
    }

    /**
     * Cancels an existing reservation.
     */
    public boolean cancelReservation(String reservationId) throws Exception {
        Reservation reservation = repository.getReservationById(reservationId);
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation ID " + reservationId + " not found.");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation " + reservationId + " is already cancelled.");
        }

        reservation.setStatus(ReservationStatus.CANCELLED);
        repository.saveReservation(reservation);

        // Update payment status to REFUNDED if was paid
        Payment payment = paymentService.getPaymentByReservationId(reservationId);
        if (payment != null && payment.getPaymentStatus().equalsIgnoreCase("SUCCESS")) {
            payment.setPaymentStatus("REFUNDED");
            repository.savePayment(payment);
        }

        return true;
    }

    public Reservation getReservationDetails(String reservationId) {
        return repository.getReservationById(reservationId);
    }

    public List<Reservation> getAllReservations() {
        return repository.getAllReservations();
    }

    public List<Reservation> getReservationsByGuestEmail(String email) {
        Guest guest = repository.findGuestByEmail(email);
        if (guest == null) return List.of();
        return repository.getReservationsByGuest(guest.getGuestId());
    }
}
