package com.hotel.model;

import java.util.List;

/**
 * Categorization of Hotel Rooms (Standard, Deluxe, Suite, Executive Suite)
 */
public enum RoomCategory {
    STANDARD("Standard Room", 100.0, 2, List.of("Free WiFi", "Queen Bed", "TV", "Air Conditioning")),
    DELUXE("Deluxe Room", 180.0, 3, List.of("Free WiFi", "King Bed", "4K Smart TV", "Mini Fridge", "Balcony")),
    SUITE("Luxury Suite", 320.0, 4, List.of("Free WiFi", "King Bed", "Living Room", "Jacuzzi", "Ocean View", "Breakfast Included")),
    EXECUTIVE_SUITE("Executive Suite", 500.0, 5, List.of("Free WiFi", "2 King Beds", "Private Lounge Access", "Jacuzzi", "Panoramic View", "24/7 Butler Service", "Complimentary Breakfast & Dinner"));

    private final String displayName;
    private final double basePricePerNight;
    private final int maxOccupancy;
    private final List<String> defaultAmenities;

    RoomCategory(String displayName, double basePricePerNight, int maxOccupancy, List<String> defaultAmenities) {
        this.displayName = displayName;
        this.basePricePerNight = basePricePerNight;
        this.maxOccupancy = maxOccupancy;
        this.defaultAmenities = defaultAmenities;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBasePricePerNight() {
        return basePricePerNight;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }

    public List<String> getDefaultAmenities() {
        return defaultAmenities;
    }
}
