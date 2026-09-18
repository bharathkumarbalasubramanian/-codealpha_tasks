package com.hotel.service;

import com.hotel.model.Reservation;
import com.hotel.model.Room;
import com.hotel.model.RoomCategory;
import com.hotel.repository.HotelRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service managing Room searching, categorization, and inventory management.
 */
public class RoomService {
    private final HotelRepository repository;

    public RoomService(HotelRepository repository) {
        this.repository = repository;
    }

    public List<Room> getAllRooms() {
        return repository.getAllRooms();
    }

    public Room getRoomById(String roomId) {
        return repository.getRoomById(roomId);
    }

    public Room getRoomByNumber(String roomNumber) {
        return repository.getRoomByNumber(roomNumber);
    }

    /**
     * Searches available rooms for a given check-in and check-out date range,
     * optional category filter, max price per night, and minimum required guests.
     */
    public List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, RoomCategory category, Double maxPrice, Integer guestsCount) {
        List<Room> allRooms = repository.getAllRooms();
        List<Reservation> allReservations = repository.getAllReservations();

        return allRooms.stream()
                // Must be enabled for booking
                .filter(Room::isAvailable)
                // Category filter
                .filter(r -> category == null || r.getCategory() == category)
                // Price filter
                .filter(r -> maxPrice == null || r.getPricePerNight() <= maxPrice)
                // Occupancy filter
                .filter(r -> guestsCount == null || r.getCategory().getMaxOccupancy() >= guestsCount)
                // Date overlap check
                .filter(room -> isRoomAvailableForDates(room, checkIn, checkOut, allReservations))
                .collect(Collectors.toList());
    }

    private boolean isRoomAvailableForDates(Room room, LocalDate checkIn, LocalDate checkOut, List<Reservation> reservations) {
        if (checkIn == null || checkOut == null) return true;

        for (Reservation res : reservations) {
            if (res.getRoom().getRoomId().equalsIgnoreCase(room.getRoomId())) {
                if (res.overlapsWith(checkIn, checkOut)) {
                    return false; // Room is already reserved for this period
                }
            }
        }
        return true;
    }

    public Room addRoom(String roomNumber, RoomCategory category, double pricePerNight, List<String> amenities) {
        String roomId = "R" + UUID.randomUUID().toString().substring(0, 5).toUpperCase();
        Room room = new Room(roomId, roomNumber, category, pricePerNight, true, amenities);
        repository.saveRoom(room);
        return room;
    }

    public boolean updateRoomAvailability(String roomId, boolean isAvailable) {
        Room room = repository.getRoomById(roomId);
        if (room != null) {
            room.setAvailable(isAvailable);
            repository.saveRoom(room);
            return true;
        }
        return false;
    }
}
