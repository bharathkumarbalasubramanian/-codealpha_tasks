package com.hotel.ui;

import com.hotel.model.*;
import com.hotel.service.BookingService;
import com.hotel.service.PaymentService;
import com.hotel.service.RoomService;
import com.hotel.util.DateUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Modern Graphical User Interface (GUI) for Hotel Reservation System using Java Swing.
 */
public class SwingGUI extends JFrame {
    private final RoomService roomService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    private JTable roomTable;
    private DefaultTableModel roomTableModel;

    private JTable bookingsTable;
    private DefaultTableModel bookingsTableModel;

    public SwingGUI(RoomService roomService, BookingService bookingService, PaymentService paymentService) {
        this.roomService = roomService;
        this.bookingService = bookingService;
        this.paymentService = paymentService;

        initUI();
    }

    private void initUI() {
        setTitle("Luxury Hotel & Suites - Reservation & Management System");
        setSize(1024, 720);
        setMinimumSize(new Dimension(800, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(24, 43, 73));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("LUXURY HOTEL RESERVATION SYSTEM");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Room Search • Booking Engine • Payment Simulation • Booking Management");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(180, 200, 230));

        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabbedPane.addTab("  Search & Book Rooms  ", createBrowseAndBookPanel());
        tabbedPane.addTab("  Manage Bookings & Cancellations  ", createManageBookingsPanel());
        tabbedPane.addTab("  Admin Dashboard  ", createAdminPanel());

        getContentPane().add(headerPanel, BorderLayout.NORTH);
        getContentPane().add(tabbedPane, BorderLayout.CENTER);
    }

    // --- TAB 1: BROWSE & BOOK ROOMS ---

    private JPanel createBrowseAndBookPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Search Bar
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("Search Available Rooms"));

        filterPanel.add(new JLabel("Check-In (YYYY-MM-DD):"));
        JTextField checkInTxt = new JTextField(LocalDate.now().toString(), 9);
        filterPanel.add(checkInTxt);

        filterPanel.add(new JLabel("Check-Out (YYYY-MM-DD):"));
        JTextField checkOutTxt = new JTextField(LocalDate.now().plusDays(2).toString(), 9);
        filterPanel.add(checkOutTxt);

        filterPanel.add(new JLabel("Category:"));
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{"All Categories", "Standard", "Deluxe", "Suite", "Executive Suite"});
        filterPanel.add(categoryCombo);

        JButton searchBtn = new JButton("Search Rooms");
        searchBtn.setBackground(new Color(30, 144, 255));
        searchBtn.setForeground(Color.WHITE);
        filterPanel.add(searchBtn);

        // Center Table
        String[] cols = {"Room #", "Category", "Base Price/Night", "Max Occupants", "Status", "Amenities"};
        roomTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        roomTable = new JTable(roomTableModel);
        roomTable.setRowHeight(26);
        roomTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        refreshRoomTable(roomService.getAllRooms());

        JScrollPane scrollPane = new JScrollPane(roomTable);

        // Bottom Booking Bar
        JPanel bookActionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bookActionPanel.setBorder(BorderFactory.createEtchedBorder());

        JButton bookSelectedBtn = new JButton("Book Selected Room");
        bookSelectedBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        bookSelectedBtn.setBackground(new Color(40, 167, 69));
        bookSelectedBtn.setForeground(Color.WHITE);

        bookActionPanel.add(new JLabel("Select a room from the table above and click: "));
        bookActionPanel.add(bookSelectedBtn);

        mainPanel.add(filterPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bookActionPanel, BorderLayout.SOUTH);

        // Event Handlers
        searchBtn.addActionListener(e -> {
            LocalDate in = DateUtils.parseDate(checkInTxt.getText());
            LocalDate out = DateUtils.parseDate(checkOutTxt.getText());
            if (in == null || out == null) {
                JOptionPane.showMessageDialog(this, "Please enter valid dates in format YYYY-MM-DD", "Invalid Date", JOptionPane.ERROR_MESSAGE);
                return;
            }

            RoomCategory cat = switch (categoryCombo.getSelectedIndex()) {
                case 1 -> RoomCategory.STANDARD;
                case 2 -> RoomCategory.DELUXE;
                case 3 -> RoomCategory.SUITE;
                case 4 -> RoomCategory.EXECUTIVE_SUITE;
                default -> null;
            };

            List<Room> available = roomService.searchAvailableRooms(in, out, cat, null, null);
            refreshRoomTable(available);
        });

        bookSelectedBtn.addActionListener(e -> {
            int selectedRow = roomTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a room from the table first.", "No Room Selected", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String roomNum = (String) roomTableModel.getValueAt(selectedRow, 0);
            Room room = roomService.getRoomByNumber(roomNum);
            LocalDate in = DateUtils.parseDate(checkInTxt.getText());
            LocalDate out = DateUtils.parseDate(checkOutTxt.getText());

            if (in == null || out == null || !out.isAfter(in)) {
                JOptionPane.showMessageDialog(this, "Please specify valid Check-In and Check-Out dates.", "Invalid Dates", JOptionPane.ERROR_MESSAGE);
                return;
            }

            showBookingModal(room, in, out);
        });

        return mainPanel;
    }

    private void refreshRoomTable(List<Room> rooms) {
        roomTableModel.setRowCount(0);
        for (Room r : rooms) {
            roomTableModel.addRow(new Object[]{
                    r.getRoomNumber(),
                    r.getCategory().getDisplayName(),
                    String.format("$%.2f", r.getPricePerNight()),
                    r.getCategory().getMaxOccupancy() + " Guests",
                    r.isAvailable() ? "Available" : "Maintenance",
                    String.join(", ", r.getAmenities())
            });
        }
    }

    private void showBookingModal(Room room, LocalDate checkIn, LocalDate checkOut) {
        int nights = DateUtils.calculateNights(checkIn, checkOut);
        double subtotal = room.getPricePerNight() * nights;
        double tax = subtotal * 0.12;
        double grandTotal = subtotal + tax;

        JDialog dialog = new JDialog(this, "Confirm Reservation - Room #" + room.getRoomNumber(), true);
        dialog.setSize(480, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JPanel form = new JPanel(new GridLayout(9, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JComboBox<String> paymentMethodCombo = new JComboBox<>(new String[]{"Credit Card", "Debit Card", "UPI", "PayPal"});
        JTextField cardDetailsField = new JTextField("4111-2222-3333-4444");

        form.add(new JLabel("Room Selected:"));
        form.add(new JLabel("#" + room.getRoomNumber() + " (" + room.getCategory().getDisplayName() + ")"));

        form.add(new JLabel("Stay Dates:"));
        form.add(new JLabel(checkIn + " to " + checkOut + " (" + nights + " nights)"));

        form.add(new JLabel("Total Price (inc. Tax):"));
        form.add(new JLabel(String.format("$%.2f", grandTotal)));

        form.add(new JLabel("Guest Full Name:"));
        form.add(nameField);

        form.add(new JLabel("Guest Email:"));
        form.add(emailField);

        form.add(new JLabel("Guest Phone:"));
        form.add(phoneField);

        form.add(new JLabel("Payment Method:"));
        form.add(paymentMethodCombo);

        form.add(new JLabel("Payment Account/Card #:"));
        form.add(cardDetailsField);

        JButton confirmBtn = new JButton("Process Payment & Book");
        confirmBtn.setBackground(new Color(40, 167, 69));
        confirmBtn.setForeground(Color.WHITE);
        confirmBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        confirmBtn.addActionListener(ev -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String phone = phoneField.getText().trim();
            String method = (String) paymentMethodCombo.getSelectedItem();
            String details = cardDetailsField.getText().trim();

            if (name.isEmpty() || email.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in Guest Name and Email.", "Missing Data", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Reservation res = bookingService.createReservation(name, email, phone, room.getRoomId(), checkIn, checkOut, method, details);
                Payment payment = paymentService.getPaymentByReservationId(res.getReservationId());
                dialog.dispose();

                String receiptText = paymentService.generateReceipt(res, payment);
                showReceiptDialog(receiptText);
                refreshRoomTable(roomService.getAllRooms());
                refreshBookingsTable();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Booking Failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(confirmBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void showReceiptDialog(String receiptText) {
        JDialog dialog = new JDialog(this, "Booking Receipt & Confirmation", true);
        dialog.setSize(550, 600);
        dialog.setLocationRelativeTo(this);

        JTextArea area = new JTextArea(receiptText);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setEditable(false);

        JButton okBtn = new JButton("Close Receipt");
        okBtn.addActionListener(e -> dialog.dispose());

        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.add(okBtn, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // --- TAB 2: MANAGE BOOKINGS & CANCELLATIONS ---

    private JPanel createManageBookingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top Search Bar for Bookings
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.setBorder(BorderFactory.createTitledBorder("Search Reservation"));

        searchBar.add(new JLabel("Booking ID / Reference:"));
        JTextField resIdTxt = new JTextField(12);
        searchBar.add(resIdTxt);

        JButton searchResBtn = new JButton("Find Reservation");
        searchBar.add(searchResBtn);

        // Center Table of All Bookings
        String[] cols = {"Booking ID", "Guest Name", "Room #", "Check-In", "Check-Out", "Nights", "Total Price", "Status"};
        bookingsTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        bookingsTable = new JTable(bookingsTableModel);
        bookingsTable.setRowHeight(25);
        refreshBookingsTable();

        JScrollPane scrollPane = new JScrollPane(bookingsTable);

        // Action Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));

        JButton viewReceiptBtn = new JButton("View Receipt / Invoice");
        JButton cancelBtn = new JButton("Cancel Selected Reservation");
        cancelBtn.setBackground(new Color(220, 53, 69));
        cancelBtn.setForeground(Color.WHITE);

        actions.add(viewReceiptBtn);
        actions.add(cancelBtn);

        panel.add(searchBar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);

        // Handlers
        searchResBtn.addActionListener(e -> {
            String ref = resIdTxt.getText().trim();
            if (ref.isEmpty()) {
                refreshBookingsTable();
                return;
            }
            Reservation res = bookingService.getReservationDetails(ref);
            if (res != null) {
                bookingsTableModel.setRowCount(0);
                bookingsTableModel.addRow(new Object[]{
                        res.getReservationId(), res.getGuest().getName(), res.getRoom().getRoomNumber(),
                        res.getCheckInDate(), res.getCheckOutDate(), res.getTotalNights(),
                        String.format("$%.2f", res.getTotalPrice()), res.getStatus().getLabel()
                });
            } else {
                JOptionPane.showMessageDialog(this, "No reservation found for ID: " + ref, "Not Found", JOptionPane.WARNING_MESSAGE);
            }
        });

        viewReceiptBtn.addActionListener(e -> {
            int row = bookingsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a booking from table.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String bId = (String) bookingsTableModel.getValueAt(row, 0);
            Reservation res = bookingService.getReservationDetails(bId);
            if (res != null) {
                Payment pay = paymentService.getPaymentByReservationId(bId);
                showReceiptDialog(paymentService.generateReceipt(res, pay));
            }
        });

        cancelBtn.addActionListener(e -> {
            int row = bookingsTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Please select a booking to cancel.", "Notice", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            String bId = (String) bookingsTableModel.getValueAt(row, 0);
            int opt = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel booking " + bId + "?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
            if (opt == JOptionPane.YES_OPTION) {
                try {
                    bookingService.cancelReservation(bId);
                    JOptionPane.showMessageDialog(this, "Reservation cancelled successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshBookingsTable();
                    refreshRoomTable(roomService.getAllRooms());
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Cancellation Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    private void refreshBookingsTable() {
        bookingsTableModel.setRowCount(0);
        for (Reservation res : bookingService.getAllReservations()) {
            bookingsTableModel.addRow(new Object[]{
                    res.getReservationId(), res.getGuest().getName(), res.getRoom().getRoomNumber(),
                    res.getCheckInDate(), res.getCheckOutDate(), res.getTotalNights(),
                    String.format("$%.2f", res.getTotalPrice()), res.getStatus().getLabel()
            });
        }
    }

    // --- TAB 3: ADMIN DASHBOARD ---

    private JPanel createAdminPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel addRoomPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        addRoomPanel.setBorder(BorderFactory.createTitledBorder("Add New Room to Inventory"));

        JTextField roomNumTxt = new JTextField();
        JComboBox<RoomCategory> categoryCombo = new JComboBox<>(RoomCategory.values());
        JTextField priceTxt = new JTextField();

        addRoomPanel.add(new JLabel("Room Number (e.g. 501):"));
        addRoomPanel.add(roomNumTxt);

        addRoomPanel.add(new JLabel("Room Category:"));
        addRoomPanel.add(categoryCombo);

        addRoomPanel.add(new JLabel("Price Per Night ($):"));
        addRoomPanel.add(priceTxt);

        JButton addBtn = new JButton("Add Room");
        addBtn.setBackground(new Color(24, 43, 73));
        addBtn.setForeground(Color.WHITE);

        addRoomPanel.add(new JLabel(""));
        addRoomPanel.add(addBtn);

        addBtn.addActionListener(e -> {
            try {
                String num = roomNumTxt.getText().trim();
                RoomCategory cat = (RoomCategory) categoryCombo.getSelectedItem();
                double p = Double.parseDouble(priceTxt.getText().trim());

                roomService.addRoom(num, cat, p, cat.getDefaultAmenities());
                JOptionPane.showMessageDialog(this, "Room #" + num + " added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshRoomTable(roomService.getAllRooms());
                roomNumTxt.setText("");
                priceTxt.setText("");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid Room Inputs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(addRoomPanel, BorderLayout.NORTH);
        return panel;
    }
}
