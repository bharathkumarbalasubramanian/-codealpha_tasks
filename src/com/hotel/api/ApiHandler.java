package com.hotel.api;

import com.hotel.model.*;
import com.hotel.service.BookingService;
import com.hotel.service.PaymentService;
import com.hotel.service.RoomService;
import com.hotel.util.DateUtils;
import com.hotel.util.SimpleJsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST API Router & Controller handling HTTP requests for the Web Application.
 */
public class ApiHandler implements HttpHandler {
    private final RoomService roomService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    public ApiHandler(RoomService roomService, BookingService bookingService, PaymentService paymentService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Enable CORS
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        String method = exchange.getRequestMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            sendResponse(exchange, 204, "");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());

        try {
            if ("GET".equalsIgnoreCase(method) && "/api/rooms".equals(path)) {
                handleGetRooms(exchange, queryParams);
            } else if ("POST".equalsIgnoreCase(method) && "/api/bookings".equals(path)) {
                handleCreateBooking(exchange);
            } else if ("GET".equalsIgnoreCase(method) && "/api/bookings/lookup".equals(path)) {
                handleLookupBooking(exchange, queryParams);
            } else if ("POST".equalsIgnoreCase(method) && "/api/bookings/cancel".equals(path)) {
                handleCancelBooking(exchange);
            } else if ("GET".equalsIgnoreCase(method) && "/api/admin/stats".equals(path)) {
                handleAdminStats(exchange);
            } else if ("POST".equalsIgnoreCase(method) && "/api/admin/rooms".equals(path)) {
                handleAddRoom(exchange);
            } else {
                sendResponse(exchange, 404, "{\"error\": \"Endpoint not found\"}");
            }
        } catch (Exception e) {
            System.err.println("[ApiHandler] Error handling request " + path + ": " + e.getMessage());
            sendResponse(exchange, 500, "{\"error\": \"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleGetRooms(HttpExchange exchange, Map<String, String> query) throws IOException {
        String checkInStr = query.get("checkIn");
        String checkOutStr = query.get("checkOut");
        String categoryStr = query.get("category");
        String maxPriceStr = query.get("maxPrice");

        LocalDate checkIn = DateUtils.parseDate(checkInStr);
        LocalDate checkOut = DateUtils.parseDate(checkOutStr);

        RoomCategory cat = null;
        if (categoryStr != null && !categoryStr.isEmpty() && !"ALL".equalsIgnoreCase(categoryStr)) {
            try { cat = RoomCategory.valueOf(categoryStr.toUpperCase()); } catch (Exception ignored) {}
        }

        Double maxPrice = null;
        if (maxPriceStr != null && !maxPriceStr.isEmpty()) {
            try { maxPrice = Double.parseDouble(maxPriceStr); } catch (Exception ignored) {}
        }

        List<Room> rooms;
        if (checkIn != null && checkOut != null) {
            rooms = roomService.searchAvailableRooms(checkIn, checkOut, cat, maxPrice, null);
        } else {
            rooms = roomService.getAllRooms();
        }

        String json = SimpleJsonParser.serializeRooms(rooms);
        sendJsonResponse(exchange, 200, json);
    }

    private void handleCreateBooking(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);

        String guestName = extractJsonValue(body, "guestName");
        String guestEmail = extractJsonValue(body, "guestEmail");
        String guestPhone = extractJsonValue(body, "guestPhone");
        String roomId = extractJsonValue(body, "roomId");
        String checkInStr = extractJsonValue(body, "checkIn");
        String checkOutStr = extractJsonValue(body, "checkOut");
        String paymentMethod = extractJsonValue(body, "paymentMethod");
        String paymentDetails = extractJsonValue(body, "paymentDetails");

        LocalDate checkIn = DateUtils.parseDate(checkInStr);
        LocalDate checkOut = DateUtils.parseDate(checkOutStr);

        try {
            Reservation res = bookingService.createReservation(
                    guestName, guestEmail, guestPhone, roomId, checkIn, checkOut, paymentMethod, paymentDetails
            );
            Payment payment = paymentService.getPaymentByReservationId(res.getReservationId());
            String receipt = paymentService.generateReceipt(res, payment);

            StringBuilder json = new StringBuilder("{");
            json.append("\"success\": true,");
            json.append("\"reservationId\": \"").append(res.getReservationId()).append("\",");
            json.append("\"status\": \"").append(res.getStatus().name()).append("\",");
            json.append("\"totalPrice\": ").append(res.getTotalPrice()).append(",");
            json.append("\"receipt\": \"").append(escapeJson(receipt)).append("\"");
            json.append("}");

            sendJsonResponse(exchange, 200, json.toString());

        } catch (Exception e) {
            sendJsonResponse(exchange, 400, "{\"success\": false, \"error\": \"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleLookupBooking(HttpExchange exchange, Map<String, String> query) throws IOException {
        String resId = query.get("id");
        if (resId == null || resId.trim().isEmpty()) {
            sendJsonResponse(exchange, 400, "{\"error\": \"Missing reservation ID\"}");
            return;
        }

        Reservation res = bookingService.getReservationDetails(resId.trim());
        if (res == null) {
            sendJsonResponse(exchange, 404, "{\"error\": \"Reservation not found\"}");
            return;
        }

        Payment payment = paymentService.getPaymentByReservationId(res.getReservationId());
        String receipt = paymentService.generateReceipt(res, payment);

        StringBuilder json = new StringBuilder("{");
        json.append("\"reservationId\": \"").append(res.getReservationId()).append("\",");
        json.append("\"guestName\": \"").append(escapeJson(res.getGuest().getName())).append("\",");
        json.append("\"guestEmail\": \"").append(escapeJson(res.getGuest().getEmail())).append("\",");
        json.append("\"roomNumber\": \"").append(res.getRoom().getRoomNumber()).append("\",");
        json.append("\"category\": \"").append(res.getRoom().getCategory().getDisplayName()).append("\",");
        json.append("\"checkIn\": \"").append(res.getCheckInDate()).append("\",");
        json.append("\"checkOut\": \"").append(res.getCheckOutDate()).append("\",");
        json.append("\"nights\": ").append(res.getTotalNights()).append(",");
        json.append("\"totalPrice\": ").append(res.getTotalPrice()).append(",");
        json.append("\"status\": \"").append(res.getStatus().getLabel()).append("\",");
        json.append("\"receipt\": \"").append(escapeJson(receipt)).append("\"");
        json.append("}");

        sendJsonResponse(exchange, 200, json.toString());
    }

    private void handleCancelBooking(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        String resId = extractJsonValue(body, "reservationId");

        try {
            bookingService.cancelReservation(resId);
            sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Reservation cancelled successfully.\"}");
        } catch (Exception e) {
            sendJsonResponse(exchange, 400, "{\"success\": false, \"error\": \"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private void handleAdminStats(HttpExchange exchange) throws IOException {
        List<Room> rooms = roomService.getAllRooms();
        List<Reservation> reservations = bookingService.getAllReservations();

        double totalRevenue = reservations.stream()
                .filter(r -> r.getStatus() == ReservationStatus.CONFIRMED || r.getStatus() == ReservationStatus.COMPLETED)
                .mapToDouble(Reservation::getTotalPrice)
                .sum();

        long confirmedCount = reservations.stream().filter(r -> r.getStatus() == ReservationStatus.CONFIRMED).count();

        StringBuilder json = new StringBuilder("{");
        json.append("\"totalRooms\": ").append(rooms.size()).append(",");
        json.append("\"totalReservations\": ").append(reservations.size()).append(",");
        json.append("\"confirmedReservations\": ").append(confirmedCount).append(",");
        json.append("\"totalRevenue\": ").append(totalRevenue).append(",");
        json.append("\"reservations\": ").append(SimpleJsonParser.serializeReservations(reservations));
        json.append("}");

        sendJsonResponse(exchange, 200, json.toString());
    }

    private void handleAddRoom(HttpExchange exchange) throws IOException {
        String body = readRequestBody(exchange);
        String roomNumber = extractJsonValue(body, "roomNumber");
        String categoryStr = extractJsonValue(body, "category");
        String priceStr = extractJsonValue(body, "pricePerNight");

        try {
            RoomCategory cat = RoomCategory.valueOf(categoryStr.toUpperCase());
            double price = Double.parseDouble(priceStr);
            Room r = roomService.addRoom(roomNumber, cat, price, cat.getDefaultAmenities());

            sendJsonResponse(exchange, 200, "{\"success\": true, \"roomId\": \"" + r.getRoomId() + "\"}");
        } catch (Exception e) {
            sendJsonResponse(exchange, 400, "{\"success\": false, \"error\": \"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    // --- UTILITY METHODS ---

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        sendResponse(exchange, statusCode, json);
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        for (String param : query.split("&")) {
            String[] pair = param.split("=");
            if (pair.length > 1) {
                map.put(pair[0], java.net.URLDecoder.decode(pair[1], StandardCharsets.UTF_8));
            } else if (pair.length == 1) {
                map.put(pair[0], "");
            }
        }
        return map;
    }

    private String extractJsonValue(String json, String key) {
        if (json == null) return null;
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        java.util.regex.Matcher matcher = pattern.matcher(json);
        if (matcher.find()) return matcher.group(1);

        pattern = java.util.regex.Pattern.compile("\"" + java.util.regex.Pattern.quote(key) + "\"\\s*:\\s*([^,\\}\\s]+)");
        matcher = pattern.matcher(json);
        if (matcher.find()) return matcher.group(1);

        return null;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
