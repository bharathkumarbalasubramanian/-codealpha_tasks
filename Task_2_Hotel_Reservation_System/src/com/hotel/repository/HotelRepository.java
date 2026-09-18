package com.hotel.repository;

import com.hotel.model.*;
import com.hotel.util.SimpleJsonParser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Data Access Repository storing hotel rooms, guests, reservations, and payments.
 * Persists data to disk using File I/O JSON engine.
 */
public class HotelRepository {
    private static final String ROOMS_FILE = "rooms.json";
    private static final String GUESTS_FILE = "guests.json";
    private static final String RESERVATIONS_FILE = "reservations.json";
    private static final String PAYMENTS_FILE = "payments.json";

    private final FilePersistenceEngine persistence;

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final Map<String, Guest> guests = new ConcurrentHashMap<>();
    private final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    private final Map<String, Payment> payments = new ConcurrentHashMap<>();

    public HotelRepository(String dataDir) {
        this.persistence = new FilePersistenceEngine(dataDir);
        loadAllData();
    }

    private synchronized void loadAllData() {
        // Load Rooms
        String roomsJson = persistence.readFile(ROOMS_FILE);
        if (roomsJson != null && !roomsJson.trim().isEmpty()) {
            List<Room> loadedRooms = SimpleJsonParser.deserializeRooms(roomsJson);
            for (Room r : loadedRooms) {
                rooms.put(r.getRoomId(), r);
            }
        }

        // If no rooms exist, populate initial sample rooms
        if (rooms.isEmpty()) {
            seedSampleRooms();
        }

        // Load Guests
        String guestsJson = persistence.readFile(GUESTS_FILE);
        if (guestsJson != null && !guestsJson.trim().isEmpty()) {
            List<Guest> loadedGuests = SimpleJsonParser.deserializeGuests(guestsJson);
            for (Guest g : loadedGuests) {
                guests.put(g.getGuestId(), g);
            }
        }

        // Load Reservations
        String reservationsJson = persistence.readFile(RESERVATIONS_FILE);
        if (reservationsJson != null && !reservationsJson.trim().isEmpty()) {
            List<Reservation> loadedReservations = SimpleJsonParser.deserializeReservations(reservationsJson);
            for (Reservation res : loadedReservations) {
                reservations.put(res.getReservationId(), res);
            }
        }

        // Load Payments
        String paymentsJson = persistence.readFile(PAYMENTS_FILE);
        if (paymentsJson != null && !paymentsJson.trim().isEmpty()) {
            List<Payment> loadedPayments = SimpleJsonParser.deserializePayments(paymentsJson);
            for (Payment p : loadedPayments) {
                payments.put(p.getTransactionId(), p);
            }
        }
    }

    private void seedSampleRooms() {
        List<Room> initial = List.of(
            new Room("R101", "101", RoomCategory.STANDARD, 100.0, true, List.of("Free WiFi", "Queen Bed", "TV", "Air Conditioning")),
            new Room("R102", "102", RoomCategory.STANDARD, 105.0, true, List.of("Free WiFi", "Queen Bed", "TV", "Desk")),
            new Room("R201", "201", RoomCategory.DELUXE, 180.0, true, List.of("Free WiFi", "King Bed", "4K Smart TV", "Mini Fridge", "Balcony")),
            new Room("R202", "202", RoomCategory.DELUXE, 195.0, true, List.of("Free WiFi", "King Bed", "4K Smart TV", "Mini Fridge", "City View")),
            new Room("R301", "301", RoomCategory.SUITE, 320.0, true, List.of("Free WiFi", "King Bed", "Living Room", "Jacuzzi", "Ocean View", "Breakfast Included")),
            new Room("R302", "302", RoomCategory.SUITE, 350.0, true, List.of("Free WiFi", "King Bed", "Living Room", "Jacuzzi", "Mountain View", "Breakfast Included")),
            new Room("R401", "401", RoomCategory.EXECUTIVE_SUITE, 500.0, true, List.of("Free WiFi", "2 King Beds", "Private Lounge Access", "Jacuzzi", "Panoramic View", "24/7 Butler Service"))
        );

        for (Room r : initial) {
            rooms.put(r.getRoomId(), r);
        }
        saveRooms();
    }

    // --- SAVE METHODS ---

    public synchronized void saveRooms() {
        String json = SimpleJsonParser.serializeRooms(getAllRooms());
        persistence.writeFile(ROOMS_FILE, json);
    }

    public synchronized void saveGuests() {
        String json = SimpleJsonParser.serializeGuests(getAllGuests());
        persistence.writeFile(GUESTS_FILE, json);
    }

    public synchronized void saveReservations() {
        String json = SimpleJsonParser.serializeReservations(getAllReservations());
        persistence.writeFile(RESERVATIONS_FILE, json);
    }

    public synchronized void savePayments() {
        String json = SimpleJsonParser.serializePayments(getAllPayments());
        persistence.writeFile(PAYMENTS_FILE, json);
    }

    // --- ROOM OPERATIONS ---

    public List<Room> getAllRooms() {
        return new ArrayList<>(rooms.values());
    }

    public Room getRoomById(String roomId) {
        return rooms.get(roomId);
    }

    public Room getRoomByNumber(String roomNumber) {
        return rooms.values().stream()
                .filter(r -> r.getRoomNumber().equalsIgnoreCase(roomNumber))
                .findFirst()
                .orElse(null);
    }

    public void saveRoom(Room room) {
        rooms.put(room.getRoomId(), room);
        saveRooms();
    }

    // --- GUEST OPERATIONS ---

    public List<Guest> getAllGuests() {
        return new ArrayList<>(guests.values());
    }

    public Guest getGuestById(String guestId) {
        return guests.get(guestId);
    }

    public Guest findGuestByEmail(String email) {
        return guests.values().stream()
                .filter(g -> g.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);
    }

    public void saveGuest(Guest guest) {
        guests.put(guest.getGuestId(), guest);
        saveGuests();
    }

    // --- RESERVATION OPERATIONS ---

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(reservations.values());
    }

    public Reservation getReservationById(String reservationId) {
        return reservations.get(reservationId);
    }

    public List<Reservation> getReservationsByGuest(String guestId) {
        return reservations.values().stream()
                .filter(r -> r.getGuest().getGuestId().equalsIgnoreCase(guestId))
                .collect(Collectors.toList());
    }

    public void saveReservation(Reservation reservation) {
        reservations.put(reservation.getReservationId(), reservation);
        saveReservations();
    }

    // --- PAYMENT OPERATIONS ---

    public List<Payment> getAllPayments() {
        return new ArrayList<>(payments.values());
    }

    public Payment getPaymentById(String transactionId) {
        return payments.get(transactionId);
    }

    public Payment getPaymentByReservationId(String reservationId) {
        return payments.values().stream()
                .filter(p -> p.getReservationId().equalsIgnoreCase(reservationId))
                .findFirst()
                .orElse(null);
    }

    public void savePayment(Payment payment) {
        payments.put(payment.getTransactionId(), payment);
        savePayments();
    }
}
