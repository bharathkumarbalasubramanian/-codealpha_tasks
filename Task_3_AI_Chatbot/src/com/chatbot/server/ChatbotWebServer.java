package com.chatbot.server;

import com.chatbot.engine.HybridChatEngine;
import com.chatbot.engine.TrainerEngine;
import com.chatbot.model.ChatMessage;
import com.chatbot.model.FAQDataset;
import com.chatbot.model.Intent;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Embedded HTTP REST API and Web Server for the AI Chatbot.
 */
public class ChatbotWebServer {

    private final int port;
    private final HybridChatEngine chatEngine;
    private final TrainerEngine trainerEngine;
    private final FAQDataset faqDataset;
    private final String webRootDirectory;
    private HttpServer server;

    public ChatbotWebServer(int port, HybridChatEngine chatEngine, TrainerEngine trainerEngine, FAQDataset faqDataset, String webRootDirectory) {
        this.port = port;
        this.chatEngine = chatEngine;
        this.trainerEngine = trainerEngine;
        this.faqDataset = faqDataset;
        this.webRootDirectory = webRootDirectory;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoints
        server.createContext("/api/chat", this::handleChatRequest);
        server.createContext("/api/faqs", this::handleFaqsRequest);
        server.createContext("/api/train", this::handleTrainRequest);
        server.createContext("/api/delete-faq", this::handleDeleteFaqRequest);
        server.createContext("/api/analytics", this::handleAnalyticsRequest);

        // Static Files Handler
        server.createContext("/", new StaticFileHandler(webRootDirectory));

        server.setExecutor(Executors.newFixedThreadPool(10));
        server.start();

        System.out.println("=================================================");
        System.out.println("🚀 AI Chatbot Web Server Started!");
        System.out.println("🌐 URL: http://localhost:" + port);
        System.out.println("=================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[ChatbotWebServer] Web server stopped.");
        }
    }

    private void handleChatRequest(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);
            String userQuery = parseJsonStringField(body, "message");

            ChatMessage message = chatEngine.processQuery(userQuery);
            trainerEngine.recordQuery(message);

            String jsonResponse = buildChatMessageJson(message);
            sendJsonResponse(exchange, 200, jsonResponse);
        } else {
            sendJsonResponse(exchange, 45, "{\"error\": \"Method not allowed\"}");
        }
    }

    private void handleFaqsRequest(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            String json = faqDataset.toJsonString();
            sendJsonResponse(exchange, 200, json);
        } else {
            sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
        }
    }

    private void handleTrainRequest(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);

            String tag = parseJsonStringField(body, "tag");
            String category = parseJsonStringField(body, "category");
            String regexRule = parseJsonStringField(body, "regexRule");
            List<String> patterns = parseJsonStringArray(body, "patterns");
            List<String> responses = parseJsonStringArray(body, "responses");

            if (tag == null || tag.trim().isEmpty() || patterns.isEmpty() || responses.isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Tag, patterns, and responses are required.\"}");
                return;
            }

            trainerEngine.addOrUpdateFAQ(tag.trim(), category != null ? category.trim() : "General", patterns, responses, regexRule);

            sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Intent trained and persisted successfully!\"}");
        } else {
            sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
        }
    }

    private void handleDeleteFaqRequest(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String body = readRequestBody(exchange);
            String tag = parseJsonStringField(body, "tag");

            if (tag == null || tag.trim().isEmpty()) {
                sendJsonResponse(exchange, 400, "{\"success\": false, \"message\": \"Tag is required.\"}");
                return;
            }

            boolean deleted = trainerEngine.deleteFAQ(tag.trim());
            if (deleted) {
                sendJsonResponse(exchange, 200, "{\"success\": true, \"message\": \"Intent deleted successfully!\"}");
            } else {
                sendJsonResponse(exchange, 404, "{\"success\": false, \"message\": \"Intent tag not found.\"}");
            }
        } else {
            sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
        }
    }

    private void handleAnalyticsRequest(HttpExchange exchange) throws IOException {
        if ("GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, Object> stats = trainerEngine.getAnalyticsSummary();
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"totalQueries\": ").append(stats.get("totalQueries")).append(",");
            sb.append("\"successfulMatches\": ").append(stats.get("successfulMatches")).append(",");
            sb.append("\"fallbackCount\": ").append(stats.get("fallbackCount")).append(",");
            sb.append("\"matchRatePercent\": ").append(stats.get("matchRatePercent")).append(",");
            sb.append("\"totalIntents\": ").append(stats.get("totalIntents"));
            sb.append("}");
            sendJsonResponse(exchange, 200, sb.toString());
        } else {
            sendJsonResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
        }
    }

    private String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private void sendJsonResponse(HttpExchange exchange, int statusCode, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String buildChatMessageJson(ChatMessage msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"userQuery\": \"").append(escapeJson(msg.getUserQuery())).append("\",");
        sb.append("\"botResponse\": \"").append(escapeJson(msg.getBotResponse())).append("\",");
        sb.append("\"intentTag\": \"").append(escapeJson(msg.getIntentTag())).append("\",");
        sb.append("\"confidenceScore\": ").append(Math.round(msg.getConfidenceScore() * 1000.0) / 10.0).append(",");
        sb.append("\"matchType\": \"").append(escapeJson(msg.getMatchType())).append("\",");
        sb.append("\"sentiment\": \"").append(escapeJson(msg.getSentiment())).append("\",");
        sb.append("\"tokens\": ").append(listToJsonArray(msg.getTokens())).append(",");
        sb.append("\"stemmedTokens\": ").append(listToJsonArray(msg.getStemmedTokens())).append(",");
        sb.append("\"timestamp\": ").append(msg.getTimestamp());
        sb.append("}");
        return sb.toString();
    }

    private String listToJsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                sb.append("\"").append(escapeJson(list.get(i))).append("\"");
                if (i < list.size() - 1) sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String parseJsonStringField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        int colonIdx = json.indexOf(":", idx + key.length());
        if (colonIdx == -1) return null;
        int startVal = colonIdx + 1;
        while (startVal < json.length() && Character.isWhitespace(json.charAt(startVal))) {
            startVal++;
        }
        if (startVal >= json.length() || json.charAt(startVal) != '"') return null;
        int endVal = startVal + 1;
        while (endVal < json.length()) {
            if (json.charAt(endVal) == '"' && json.charAt(endVal - 1) != '\\') {
                break;
            }
            endVal++;
        }
        if (endVal < json.length()) {
            return unescapeJson(json.substring(startVal + 1, endVal));
        }
        return null;
    }

    private List<String> parseJsonStringArray(String json, String fieldName) {
        List<String> list = new ArrayList<>();
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return list;
        int arrayStart = json.indexOf("[", idx);
        if (arrayStart == -1) return list;
        int arrayEnd = json.indexOf("]", arrayStart);
        if (arrayEnd == -1) return list;

        String content = json.substring(arrayStart + 1, arrayEnd).trim();
        if (content.isEmpty()) return list;

        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                if (!inQuotes) {
                    list.add(unescapeJson(sb.toString()));
                    sb.setLength(0);
                }
            } else if (inQuotes) {
                sb.append(c);
            }
        }
        return list;
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private String unescapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n").replace("\\r", "\r");
    }
}
