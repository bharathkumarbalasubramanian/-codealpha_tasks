package com.hotel;

import com.hotel.api.HttpWebServer;
import com.hotel.repository.HotelRepository;
import com.hotel.service.BookingService;
import com.hotel.service.PaymentService;
import com.hotel.service.RoomService;
import com.hotel.ui.ConsoleUI;
import com.hotel.ui.SwingGUI;

import javax.swing.*;
import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.URI;

/**
 * Main application entry point supporting Web REST Server, Swing Desktop GUI, and Console CLI modes.
 */
public class Main {
    public static void main(String[] args) {
        String dataDir = "data";
        String webDir = "web";
        int port = 8080;

        HotelRepository repository = new HotelRepository(dataDir);
        RoomService roomService = new RoomService(repository);
        PaymentService paymentService = new PaymentService(repository);
        BookingService bookingService = new BookingService(repository, paymentService);

        boolean launchCli = false;
        boolean launchGui = false;
        boolean launchWeb = false;

        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                launchCli = true;
            } else if ("--gui".equalsIgnoreCase(arg) || "-g".equalsIgnoreCase(arg)) {
                launchGui = true;
            } else if ("--web".equalsIgnoreCase(arg) || "-w".equalsIgnoreCase(arg)) {
                launchWeb = true;
            }
        }

        // Default mode is Web application unless CLI/GUI explicitly specified
        if (!launchCli && !launchGui) {
            launchWeb = true;
        }

        if (launchWeb) {
            try {
                HttpWebServer webServer = new HttpWebServer(port, roomService, bookingService, paymentService, webDir);
                webServer.start();

                // Automatically attempt to open system browser
                openWebBrowser("http://localhost:" + port);

            } catch (Exception e) {
                System.err.println("Could not start Web Server: " + e.getMessage());
                launchCli = true;
            }
        }

        if (launchCli) {
            ConsoleUI cli = new ConsoleUI(roomService, bookingService, paymentService);
            cli.start();
        } else if (launchGui && !GraphicsEnvironment.isHeadless()) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            SwingUtilities.invokeLater(() -> {
                try {
                    SwingGUI gui = new SwingGUI(roomService, bookingService, paymentService);
                    gui.setVisible(true);
                } catch (Exception e) {
                    System.err.println("Could not launch Swing GUI: " + e.getMessage());
                }
            });
        }
    }

    private static void openWebBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                System.out.println("Please open your browser at: " + url);
            }
        } catch (Exception ignored) {
            System.out.println("Please open your browser at: " + url);
        }
    }
}
