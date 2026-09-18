package com.hotel.util;

import com.hotel.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Lightweight JSON parser and serializer built using core Java standard libraries.
 */
public class SimpleJsonParser {

    // --- ROOM SERIALIZATION / DESERIALIZATION ---

    public static String serializeRooms(List<Room> rooms) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < rooms.size(); i++) {
            Room r = rooms.get(i);
            sb.append("  {\n");
            sb.append("    \"roomId\": \"").append(escape(r.getRoomId())).append("\",\n");
            sb.append("    \"roomNumber\": \"").append(escape(r.getRoomNumber())).append("\",\n");
            sb.append("    \"category\": \"").append(r.getCategory().name()).append("\",\n");
            sb.append("    \"pricePerNight\": ").append(r.getPricePerNight()).append(",\n");
            sb.append("    \"isAvailable\": ").append(r.isAvailable()).append(",\n");
            sb.append("    \"amenities\": [")
              .append(r.getAmenities().stream().map(a -> "\"" + escape(a) + "\"").collect(Collectors.joining(", ")))
              .append("]\n");
            sb.append("  }").append(i < rooms.size() - 1 ? ",\n" : "\n");
        }
        sb.append("]");
        return sb.toString();
    }

    public static List<Room> deserializeRooms(String json) {
        List<Room> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return list;

        List<String> objects = extractJsonObjects(json);
        for (String obj : objects) {
            String roomId = extractString(obj, "roomId");
            String roomNumber = extractString(obj, "roomNumber");
            String categoryStr = extractString(obj, "category");
            double price = extractDouble(obj, "pricePerNight");
            boolean isAvailable = extractBoolean(obj, "isAvailable");
            List<String> amenities = extractStringList(obj, "amenities");

            RoomCategory category = RoomCategory.STANDARD;
            try {
                if (categoryStr != null) category = RoomCategory.valueOf(categoryStr);
            } catch (Exception ignored) {}

            if (roomId != null && roomNumber != null) {
                list.add(new Room(roomId, roomNumber, category, price, isAvailable, amenities));
            }
        }
        return list;
    }

    // --- GUEST SERIALIZATION / DESERIALIZATION ---

    public static String serializeGuests(List<Guest> guests) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < guests.size(); i++) {
            Guest g = guests.get(i);
            sb.append("  {\n");
            sb.append("    \"guestId\": \"").append(escape(g.getGuestId())).append("\",\n");
            sb.append("    \"name\": \"").append(escape(g.getName())).append("\",\n");
            sb.append("    \"email\": \"").append(escape(g.getEmail())).append("\",\n");
            sb.append("    \"phone\": \"").append(escape(g.getPhone())).append("\"\n");
            sb.append("  }").append(i < guests.size() - 1 ? ",\n" : "\n");
        }
        sb.append("]");
        return sb.toString();
    }

    public static List<Guest> deserializeGuests(String json) {
        List<Guest> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return list;

        List<String> objects = extractJsonObjects(json);
        for (String obj : objects) {
            String guestId = extractString(obj, "guestId");
            String name = extractString(obj, "name");
            String email = extractString(obj, "email");
            String phone = extractString(obj, "phone");

            if (guestId != null && name != null) {
                list.add(new Guest(guestId, name, email, phone));
            }
        }
        return list;
    }

    // --- RESERVATION SERIALIZATION / DESERIALIZATION ---

    public static String serializeReservations(List<Reservation> reservations) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < reservations.size(); i++) {
            Reservation r = reservations.get(i);
            sb.append("  {\n");
            sb.append("    \"reservationId\": \"").append(escape(r.getReservationId())).append("\",\n");
            sb.append("    \"guest\": {\n");
            sb.append("      \"guestId\": \"").append(escape(r.getGuest().getGuestId())).append("\",\n");
            sb.append("      \"name\": \"").append(escape(r.getGuest().getName())).append("\",\n");
            sb.append("      \"email\": \"").append(escape(r.getGuest().getEmail())).append("\",\n");
            sb.append("      \"phone\": \"").append(escape(r.getGuest().getPhone())).append("\"\n");
            sb.append("    },\n");
            sb.append("    \"room\": {\n");
            sb.append("      \"roomId\": \"").append(escape(r.getRoom().getRoomId())).append("\",\n");
            sb.append("      \"roomNumber\": \"").append(escape(r.getRoom().getRoomNumber())).append("\",\n");
            sb.append("      \"category\": \"").append(r.getRoom().getCategory().name()).append("\",\n");
            sb.append("      \"pricePerNight\": ").append(r.getRoom().getPricePerNight()).append(",\n");
            sb.append("      \"isAvailable\": ").append(r.getRoom().isAvailable()).append("\n");
            sb.append("    },\n");
            sb.append("    \"checkInDate\": \"").append(DateUtils.formatDate(r.getCheckInDate())).append("\",\n");
            sb.append("    \"checkOutDate\": \"").append(DateUtils.formatDate(r.getCheckOutDate())).append("\",\n");
            sb.append("    \"totalNights\": ").append(r.getTotalNights()).append(",\n");
            sb.append("    \"totalPrice\": ").append(r.getTotalPrice()).append(",\n");
            sb.append("    \"status\": \"").append(r.getStatus().name()).append("\",\n");
            sb.append("    \"paymentTransactionId\": \"").append(escape(r.getPaymentTransactionId())).append("\",\n");
            sb.append("    \"createdAt\": \"").append(DateUtils.formatDateTime(r.getCreatedAt())).append("\"\n");
            sb.append("  }").append(i < reservations.size() - 1 ? ",\n" : "\n");
        }
        sb.append("]");
        return sb.toString();
    }

    public static List<Reservation> deserializeReservations(String json) {
        List<Reservation> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return list;

        List<String> objects = extractJsonObjects(json);
        for (String obj : objects) {
            String resId = extractString(obj, "reservationId");

            // Extract embedded guest
            String guestSub = extractSubObject(obj, "guest");
            Guest guest = new Guest(
                    extractString(guestSub, "guestId"),
                    extractString(guestSub, "name"),
                    extractString(guestSub, "email"),
                    extractString(guestSub, "phone")
            );

            // Extract embedded room
            String roomSub = extractSubObject(obj, "room");
            RoomCategory cat = RoomCategory.STANDARD;
            try {
                String cStr = extractString(roomSub, "category");
                if (cStr != null) cat = RoomCategory.valueOf(cStr);
            } catch (Exception ignored) {}
            Room room = new Room(
                    extractString(roomSub, "roomId"),
                    extractString(roomSub, "roomNumber"),
                    cat,
                    extractDouble(roomSub, "pricePerNight"),
                    extractBoolean(roomSub, "isAvailable"),
                    null
            );

            LocalDate checkIn = DateUtils.parseDate(extractString(obj, "checkInDate"));
            LocalDate checkOut = DateUtils.parseDate(extractString(obj, "checkOutDate"));
            int nights = (int) extractDouble(obj, "totalNights");
            double totalPrice = extractDouble(obj, "totalPrice");
            String statusStr = extractString(obj, "status");
            ReservationStatus status = ReservationStatus.CONFIRMED;
            try {
                if (statusStr != null) status = ReservationStatus.valueOf(statusStr);
            } catch (Exception ignored) {}
            String txnId = extractString(obj, "paymentTransactionId");
            LocalDateTime createdAt = DateUtils.parseDateTime(extractString(obj, "createdAt"));

            if (resId != null && guest.getGuestId() != null && room.getRoomId() != null) {
                list.add(new Reservation(resId, guest, room, checkIn, checkOut, nights, totalPrice, status, txnId, createdAt));
            }
        }
        return list;
    }

    // --- PAYMENT SERIALIZATION / DESERIALIZATION ---

    public static String serializePayments(List<Payment> payments) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < payments.size(); i++) {
            Payment p = payments.get(i);
            sb.append("  {\n");
            sb.append("    \"transactionId\": \"").append(escape(p.getTransactionId())).append("\",\n");
            sb.append("    \"reservationId\": \"").append(escape(p.getReservationId())).append("\",\n");
            sb.append("    \"amount\": ").append(p.getAmount()).append(",\n");
            sb.append("    \"paymentMethod\": \"").append(escape(p.getPaymentMethod())).append("\",\n");
            sb.append("    \"paymentStatus\": \"").append(escape(p.getPaymentStatus())).append("\",\n");
            sb.append("    \"timestamp\": \"").append(DateUtils.formatDateTime(p.getTimestamp())).append("\"\n");
            sb.append("  }").append(i < payments.size() - 1 ? ",\n" : "\n");
        }
        sb.append("]");
        return sb.toString();
    }

    public static List<Payment> deserializePayments(String json) {
        List<Payment> list = new ArrayList<>();
        if (json == null || json.trim().isEmpty()) return list;

        List<String> objects = extractJsonObjects(json);
        for (String obj : objects) {
            String txnId = extractString(obj, "transactionId");
            String resId = extractString(obj, "reservationId");
            double amount = extractDouble(obj, "amount");
            String method = extractString(obj, "paymentMethod");
            String status = extractString(obj, "paymentStatus");
            LocalDateTime ts = DateUtils.parseDateTime(extractString(obj, "timestamp"));

            if (txnId != null) {
                list.add(new Payment(txnId, resId, amount, method, status, ts));
            }
        }
        return list;
    }

    // --- HELPER REGEX PARSING UTILITIES ---

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static List<String> extractJsonObjects(String json) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) {
                inString = !inString;
            }
            if (!inString) {
                if (c == '{') {
                    if (depth == 0) start = i;
                    depth++;
                } else if (c == '}') {
                    depth--;
                    if (depth == 0 && start != -1) {
                        result.add(json.substring(start, i + 1));
                        start = -1;
                    }
                }
            }
        }
        return result;
    }

    private static String extractString(String obj, String key) {
        if (obj == null) return null;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(obj);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private static double extractDouble(String obj, String key) {
        if (obj == null) return 0.0;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([-+]?[0-9]*\\.?[0-9]+)");
        Matcher matcher = pattern.matcher(obj);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    private static boolean extractBoolean(String obj, String key) {
        if (obj == null) return false;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false)");
        Matcher matcher = pattern.matcher(obj);
        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }
        return false;
    }

    private static List<String> extractStringList(String obj, String key) {
        List<String> list = new ArrayList<>();
        if (obj == null) return list;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\\[([^\\]]*)\\]");
        Matcher matcher = pattern.matcher(obj);
        if (matcher.find()) {
            String arrayContent = matcher.group(1);
            Pattern strPattern = Pattern.compile("\"([^\"]*)\"");
            Matcher strMatcher = strPattern.matcher(arrayContent);
            while (strMatcher.find()) {
                list.add(strMatcher.group(1));
            }
        }
        return list;
    }

    private static String extractSubObject(String obj, String key) {
        if (obj == null) return "";
        int keyIndex = obj.indexOf("\"" + key + "\"");
        if (keyIndex == -1) return "";
        int openBrace = obj.indexOf('{', keyIndex);
        if (openBrace == -1) return "";

        int depth = 0;
        for (int i = openBrace; i < obj.length(); i++) {
            char c = obj.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') depth--;

            if (depth == 0) {
                return obj.substring(openBrace, i + 1);
            }
        }
        return "";
    }
}
