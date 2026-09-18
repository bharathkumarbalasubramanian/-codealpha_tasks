package com.hotel.test;

import com.hotel.model.*;
import com.hotel.repository.HotelRepository;
import com.hotel.service.BookingService;
import com.hotel.service.PaymentService;
import com.hotel.service.RoomService;

import java.time.LocalDate;
import java.util.List;

/**
 * Automated Verification Suite for the Hotel Reservation System.
 */
public class SystemTest {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("      RUNNING HOTEL RESERVATION SYSTEM TESTS      ");
        System.out.println("==================================================");

        try {
            // Setup test repository in test_data directory
            String testDataDir = "test_data";
            HotelRepository repository = new HotelRepository(testDataDir);
            RoomService roomService = new RoomService(repository);
            PaymentService paymentService = new PaymentService(repository);
            BookingService bookingService = new BookingService(repository, paymentService);

            // Test 1: Search available rooms
            LocalDate checkIn = LocalDate.now().plusDays(5);
            LocalDate checkOut = LocalDate.now().plusDays(8);

            List<Room> deluxeRooms = roomService.searchAvailableRooms(checkIn, checkOut, RoomCategory.DELUXE, null, null);
            assertFalse(deluxeRooms.isEmpty(), "Test 1 Failed: Deluxe rooms should be available.");
            System.out.println(" TEST 1 PASSED: Found " + deluxeRooms.size() + " available Deluxe rooms.");

            // Test 2: Create Reservation & Verify Payment
            Room roomToBook = deluxeRooms.get(0);
            Reservation reservation = bookingService.createReservation(
                    "John Doe", "john@example.com", "+1-555-0192",
                    roomToBook.getRoomId(), checkIn, checkOut, "Credit Card", "4111-2222-3333-4444"
            );

            assertEquals(ReservationStatus.CONFIRMED, reservation.getStatus(), "Test 2 Failed: Booking should be CONFIRMED.");
            assertEquals(3, reservation.getTotalNights(), "Test 2 Failed: Should be 3 nights stay.");
            System.out.println(" TEST 2 PASSED: Created reservation " + reservation.getReservationId() + " for 3 nights.");

            // Test 3: Date Overlap Prevention (Double Booking Check)
            boolean collisionCaught = false;
            try {
                bookingService.createReservation(
                        "Jane Smith", "jane@example.com", "+1-555-0199",
                        roomToBook.getRoomId(), checkIn.plusDays(1), checkOut.plusDays(1), "UPI", "jane@upi"
                );
            } catch (IllegalStateException e) {
                collisionCaught = true;
            }
            assertTrue(collisionCaught, "Test 3 Failed: Overlapping date reservation should be rejected!");
            System.out.println(" TEST 3 PASSED: Overlapping reservation successfully blocked!");

            // Test 4: Receipt Generation
            Payment payment = paymentService.getPaymentByReservationId(reservation.getReservationId());
            String receipt = paymentService.generateReceipt(reservation, payment);
            assertTrue(receipt.contains("OFFICIAL PAYMENT RECEIPT"), "Test 4 Failed: Receipt header missing.");
            assertTrue(receipt.contains("John Doe"), "Test 4 Failed: Guest name missing from receipt.");
            System.out.println(" TEST 4 PASSED: Payment receipt generated successfully.");

            // Test 5: Reservation Cancellation & Payment Refund
            bookingService.cancelReservation(reservation.getReservationId());
            Reservation updatedRes = bookingService.getReservationDetails(reservation.getReservationId());
            assertEquals(ReservationStatus.CANCELLED, updatedRes.getStatus(), "Test 5 Failed: Status should be CANCELLED.");

            Payment updatedPayment = paymentService.getPaymentByReservationId(reservation.getReservationId());
            assertEquals("REFUNDED", updatedPayment.getPaymentStatus(), "Test 5 Failed: Payment status should be REFUNDED.");
            System.out.println(" TEST 5 PASSED: Reservation cancelled and payment marked as REFUNDED.");

            System.out.println("==================================================");
            System.out.println("         ALL AUTOMATED TESTS PASSED!              ");
            System.out.println("==================================================");

        } catch (Exception e) {
            System.err.println("Test Suite Failed with Exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " Expected: " + expected + ", Actual: " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message);
        }
    }
}
