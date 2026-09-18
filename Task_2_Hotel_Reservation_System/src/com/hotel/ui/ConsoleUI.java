package com.hotel.ui;

import com.hotel.model.*;
import com.hotel.service.BookingService;
import com.hotel.service.PaymentService;
import com.hotel.service.RoomService;
import com.hotel.util.DateUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive Console CLI User Interface for the Hotel Reservation System.
 */
public class ConsoleUI {
    private final RoomService roomService;
    private final BookingService bookingService;
    private final PaymentService paymentService;
    private final Scanner scanner;

    public ConsoleUI(RoomService roomService, BookingService bookingService, PaymentService paymentService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            printHeader();
            System.out.println(" 1. View All Rooms & Categories");
            System.out.println(" 2. Search Available Rooms");
            System.out.println(" 3. Book a Room");
            System.out.println(" 4. View Booking Details & Print Receipt");
            System.out.println(" 5. Cancel a Reservation");
            System.out.println(" 6. Admin Dashboard (Add/Manage Rooms)");
            System.out.println(" 7. Exit System");
            System.out.println("==================================================");
            System.out.print("Select an option (1-7): ");

            String input = scanner.nextLine().trim();
            System.out.println();

            switch (input) {
                case "1" -> viewAllRooms();
                case "2" -> searchAvailableRooms();
                case "3" -> bookRoom();
                case "4" -> viewBookingDetails();
                case "5" -> cancelReservation();
                case "6" -> adminMenu();
                case "7" -> {
                    System.out.println("Thank you for using Luxury Hotel Reservation System. Goodbye!");
                    running = false;
                }
                default -> System.out.println("Invalid option! Please enter a number between 1 and 7.\n");
            }
        }
    }

    private void printHeader() {
        System.out.println("\n==================================================");
        System.out.println("         HOTEL RESERVATION MANAGEMENT SYSTEM       ");
        System.out.println("==================================================");
    }

    private void viewAllRooms() {
        System.out.println("--- ALL HOTEL ROOMS & CATEGORIES ---");
        List<Room> rooms = roomService.getAllRooms();
        if (rooms.isEmpty()) {
            System.out.println("No rooms available in inventory.");
            return;
        }

        printRoomTable(rooms);
    }

    private void searchAvailableRooms() {
        System.out.println("--- SEARCH AVAILABLE ROOMS ---");

        LocalDate checkIn = readDate("Enter Check-In Date (YYYY-MM-DD): ");
        if (checkIn == null) return;

        LocalDate checkOut = readDate("Enter Check-Out Date (YYYY-MM-DD): ");
        if (checkOut == null) return;

        if (!checkOut.isAfter(checkIn)) {
            System.out.println("Error: Check-Out date must be after Check-In date.");
            return;
        }

        System.out.println("\nSelect Room Category Filter:");
        System.out.println(" 0. Any Category");
        System.out.println(" 1. Standard Room");
        System.out.println(" 2. Deluxe Room");
        System.out.println(" 3. Luxury Suite");
        System.out.println(" 4. Executive Suite");
        System.out.print("Choice (0-4): ");
        String catChoice = scanner.nextLine().trim();

        RoomCategory category = switch (catChoice) {
            case "1" -> RoomCategory.STANDARD;
            case "2" -> RoomCategory.DELUXE;
            case "3" -> RoomCategory.SUITE;
            case "4" -> RoomCategory.EXECUTIVE_SUITE;
            default -> null;
        };

        System.out.print("Enter Maximum Price per night ($) [press Enter for any]: ");
        String priceStr = scanner.nextLine().trim();
        Double maxPrice = null;
        if (!priceStr.isEmpty()) {
            try {
                maxPrice = Double.parseDouble(priceStr);
            } catch (NumberFormatException ignored) {}
        }

        List<Room> available = roomService.searchAvailableRooms(checkIn, checkOut, category, maxPrice, null);

        int nights = DateUtils.calculateNights(checkIn, checkOut);
        System.out.printf("\nFound %d available room(s) for stay duration of %d night(s):\n", available.size(), nights);

        if (!available.isEmpty()) {
            printRoomTable(available);
        }
    }

    private void bookRoom() {
        System.out.println("--- MAKE A RESERVATION ---");

        LocalDate checkIn = readDate("Enter Check-In Date (YYYY-MM-DD): ");
        if (checkIn == null) return;

        LocalDate checkOut = readDate("Enter Check-Out Date (YYYY-MM-DD): ");
        if (checkOut == null) return;

        List<Room> available = roomService.searchAvailableRooms(checkIn, checkOut, null, null, null);
        if (available.isEmpty()) {
            System.out.println("Sorry, no rooms are available for the selected dates.");
            return;
        }

        printRoomTable(available);

        System.out.print("\nEnter Room Number to book (e.g. 101): ");
        String roomNum = scanner.nextLine().trim();
        Room selectedRoom = roomService.getRoomByNumber(roomNum);

        if (selectedRoom == null) {
            System.out.println("Invalid Room Number!");
            return;
        }

        System.out.print("Enter Guest Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Guest Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter Guest Phone Number: ");
        String phone = scanner.nextLine().trim();

        int nights = DateUtils.calculateNights(checkIn, checkOut);
        double totalPrice = selectedRoom.getPricePerNight() * nights;
        double tax = totalPrice * 0.12;
        double grandTotal = totalPrice + tax;

        System.out.printf("\nSUMMARY: Room #%s (%s) for %d night(s)\n", selectedRoom.getRoomNumber(), selectedRoom.getCategory().getDisplayName(), nights);
        System.out.printf("Subtotal: $%.2f | Tax (12%%): $%.2f | Total: $%.2f\n", totalPrice, tax, grandTotal);

        System.out.println("\nSelect Payment Method:");
        System.out.println(" 1. Credit Card");
        System.out.println(" 2. Debit Card");
        System.out.println(" 3. UPI / Digital Wallet");
        System.out.println(" 4. PayPal");
        System.out.print("Choice (1-4): ");
        String payChoice = scanner.nextLine().trim();

        String paymentMethod = switch (payChoice) {
            case "2" -> "Debit Card";
            case "3" -> "UPI";
            case "4" -> "PayPal";
            default -> "Credit Card";
        };

        System.out.print("Enter Payment Details (Card # / VPA / Account): ");
        String payDetails = scanner.nextLine().trim();

        System.out.println("\nProcessing payment simulation...");
        try {
            Reservation reservation = bookingService.createReservation(
                    name, email, phone, selectedRoom.getRoomId(), checkIn, checkOut, paymentMethod, payDetails
            );

            System.out.println("\n SUCCESS! Reservation Confirmed!");
            System.out.println("Booking Reference ID: " + reservation.getReservationId());
            System.out.println();

            Payment payment = paymentService.getPaymentByReservationId(reservation.getReservationId());
            System.out.println(paymentService.generateReceipt(reservation, payment));

        } catch (Exception e) {
            System.out.println(" Booking Failed: " + e.getMessage());
        }
    }

    private void viewBookingDetails() {
        System.out.println("--- VIEW BOOKING DETAILS & RECEIPT ---");
        System.out.print("Enter Booking Reference ID (e.g. BK-XXXXXX): ");
        String resId = scanner.nextLine().trim();

        Reservation reservation = bookingService.getReservationDetails(resId);
        if (reservation == null) {
            System.out.println("Reservation not found for ID: " + resId);
            return;
        }

        Payment payment = paymentService.getPaymentByReservationId(reservation.getReservationId());
        System.out.println(paymentService.generateReceipt(reservation, payment));
    }

    private void cancelReservation() {
        System.out.println("--- CANCEL A RESERVATION ---");
        System.out.print("Enter Booking Reference ID to Cancel: ");
        String resId = scanner.nextLine().trim();

        Reservation reservation = bookingService.getReservationDetails(resId);
        if (reservation == null) {
            System.out.println("Reservation not found!");
            return;
        }

        System.out.println("\nReservation Details:");
        System.out.println(reservation);
        System.out.print("\nAre you sure you want to CANCEL this reservation? (y/N): ");
        String confirm = scanner.nextLine().trim();

        if (confirm.equalsIgnoreCase("y") || confirm.equalsIgnoreCase("yes")) {
            try {
                bookingService.cancelReservation(resId);
                System.out.println("\n Reservation " + resId + " has been successfully CANCELLED.");
                System.out.println("Refund processed if payment was completed.");
            } catch (Exception e) {
                System.out.println("Cancellation Failed: " + e.getMessage());
            }
        } else {
            System.out.println("Cancellation aborted.");
        }
    }

    private void adminMenu() {
        System.out.println("--- ADMIN MANAGEMENT DASHBOARD ---");
        System.out.println(" 1. View All Bookings");
        System.out.println(" 2. Add New Room");
        System.out.println(" 3. Toggle Room Service Status");
        System.out.print("Choice (1-3): ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                List<Reservation> list = bookingService.getAllReservations();
                if (list.isEmpty()) {
                    System.out.println("No reservations recorded yet.");
                } else {
                    System.out.println("\n--- ALL RESERVATIONS ---");
                    for (Reservation r : list) {
                        System.out.println(r);
                    }
                }
            }
            case "2" -> {
                System.out.print("Enter Room Number (e.g. 501): ");
                String rNum = scanner.nextLine().trim();

                System.out.println("Select Category:");
                System.out.println(" 1. STANDARD ($100)");
                System.out.println(" 2. DELUXE ($180)");
                System.out.println(" 3. SUITE ($320)");
                System.out.println(" 4. EXECUTIVE SUITE ($500)");
                System.out.print("Choice: ");
                String catChoice = scanner.nextLine().trim();

                RoomCategory cat = switch (catChoice) {
                    case "2" -> RoomCategory.DELUXE;
                    case "3" -> RoomCategory.SUITE;
                    case "4" -> RoomCategory.EXECUTIVE_SUITE;
                    default -> RoomCategory.STANDARD;
                };

                System.out.print("Enter Price per night ($): ");
                double price = Double.parseDouble(scanner.nextLine().trim());

                Room r = roomService.addRoom(rNum, cat, price, cat.getDefaultAmenities());
                System.out.println(" Added Room: " + r);
            }
            case "3" -> {
                System.out.print("Enter Room ID (e.g. R101): ");
                String roomId = scanner.nextLine().trim();
                System.out.print("Is Room Available for Service? (true/false): ");
                boolean avail = Boolean.parseBoolean(scanner.nextLine().trim());

                if (roomService.updateRoomAvailability(roomId, avail)) {
                    System.out.println(" Updated room availability status.");
                } else {
                    System.out.println(" Room ID not found.");
                }
            }
        }
    }

    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        LocalDate date = DateUtils.parseDate(input);
        if (date == null) {
            System.out.println("Invalid date format! Use YYYY-MM-DD (e.g. 2026-10-15)");
        }
        return date;
    }

    private void printRoomTable(List<Room> rooms) {
        System.out.println("+------+-----------------+--------------------+------------+-----------------------------------+");
        System.out.println("| Room | Category        | Max Occupancy      | Price/Night| Amenities                         |");
        System.out.println("+------+-----------------+--------------------+------------+-----------------------------------+");
        for (Room r : rooms) {
            String amenitiesStr = String.join(", ", r.getAmenities());
            if (amenitiesStr.length() > 33) amenitiesStr = amenitiesStr.substring(0, 30) + "...";
            System.out.printf("| %-4s | %-15s | %-18s | $%-9.2f | %-33s |\n",
                    r.getRoomNumber(), r.getCategory().getDisplayName(), r.getCategory().getMaxOccupancy() + " Guests",
                    r.getPricePerNight(), amenitiesStr);
        }
        System.out.println("+------+-----------------+--------------------+------------+-----------------------------------+");
    }
}
