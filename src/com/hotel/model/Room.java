package com.hotel.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a Hotel Room.
 */
public class Room {
    private String roomId;
    private String roomNumber;
    private RoomCategory category;
    private double pricePerNight;
    private boolean isAvailable;
    private List<String> amenities;

    public Room() {
        this.amenities = new ArrayList<>();
    }

    public Room(String roomId, String roomNumber, RoomCategory category, double pricePerNight, boolean isAvailable, List<String> amenities) {
        this.roomId = roomId;
        this.roomNumber = roomNumber;
        this.category = category;
        this.pricePerNight = pricePerNight;
        this.isAvailable = isAvailable;
        this.amenities = amenities != null ? new ArrayList<>(amenities) : new ArrayList<>(category.getDefaultAmenities());
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public RoomCategory getCategory() {
        return category;
    }

    public void setCategory(RoomCategory category) {
        this.category = category;
    }

    public double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public List<String> getAmenities() {
        return amenities;
    }

    public void setAmenities(List<String> amenities) {
        this.amenities = amenities;
    }

    @Override
    public String toString() {
        return String.format("Room #%s [%s] - $%.2f/night (Max Occupancy: %d)",
                roomNumber, category.getDisplayName(), pricePerNight, category.getMaxOccupancy());
    }
}
