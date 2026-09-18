package com.hotel.api;

import com.hotel.service.BookingService;
import com.hotel.service.PaymentService;
import com.hotel.service.RoomService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Embedded HTTP Server serving both Web API endpoints and Web Static Assets.
 */
public class HttpWebServer {
    private final int port;
    private final ApiHandler apiHandler;
    private final String webDir;
    private HttpServer server;

    public HttpWebServer(int port, RoomService roomService, BookingService bookingService, PaymentService paymentService, String webDir) {
        this.port = port;
        this.apiHandler = new ApiHandler(roomService, bookingService, paymentService);
        this.webDir = webDir;
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);

        // Mount API Handler
        server.createContext("/api", apiHandler);

        // Mount Static File Handler
        server.createContext("/", new StaticFileHandler(webDir));

        server.setExecutor(null); // default executor
        server.start();

        System.out.println("==================================================");
        System.out.println("  HOTEL RESERVATION SYSTEM - WEB SERVER ACTIVE    ");
        System.out.println("  Web Application UI: http://localhost:" + port + "        ");
        System.out.println("  REST API Endpoint:  http://localhost:" + port + "/api   ");
        System.out.println("==================================================");
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    private static class StaticFileHandler implements HttpHandler {
        private final String rootDir;

        public StaticFileHandler(String rootDir) {
            this.rootDir = rootDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String uriPath = exchange.getRequestURI().getPath();
            if ("/".equals(uriPath) || uriPath.trim().isEmpty()) {
                uriPath = "/index.html";
            }

            Path filePath = Paths.get(rootDir, uriPath);
            File file = filePath.toFile();

            if (!file.exists() || file.isDirectory()) {
                // Fallback to index.html for single-page app routing
                filePath = Paths.get(rootDir, "index.html");
                file = filePath.toFile();
            }

            if (!file.exists()) {
                String response = "404 Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            String contentType = probeContentType(file.getName());
            exchange.getResponseHeaders().set("Content-Type", contentType);

            byte[] fileBytes = Files.readAllBytes(filePath);
            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
            }
        }

        private String probeContentType(String filename) {
            if (filename.endsWith(".html") || filename.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (filename.endsWith(".css")) return "text/css; charset=UTF-8";
            if (filename.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (filename.endsWith(".json")) return "application/json; charset=UTF-8";
            if (filename.endsWith(".png")) return "image/png";
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return "image/jpeg";
            if (filename.endsWith(".svg")) return "image/svg+xml";
            if (filename.endsWith(".ico")) return "image/x-icon";
            return "text/plain; charset=UTF-8";
        }
    }
}
