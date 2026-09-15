import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

/**
 * StudentGradeTracker
 * 
 * A Graphical User Interface (GUI) application built with Java Swing
 * to dynamically track, calculate, and summarize student grades.
 * 
 * Backend Data Management: ArrayList<Double>
 */
public class StudentGradeTracker extends JFrame {

    // =========================================================================
    // BACKEND DATA STORAGE
    // =========================================================================
    /*
     * ArrayList<Double> Explanation:
     * - ArrayList is a dynamic data structure that resizes automatically as elements are added.
     * - Uses wrapper class Double (reference type) to store numeric grades.
     * - Manages grade records behind the scenes separately from GUI rendering.
     */
    private final ArrayList<Double> grades = new ArrayList<>();

    // =========================================================================
    // GUI COMPONENTS
    // =========================================================================
    private JTextField gradeInputField;
    private JButton addGradeButton;
    private JButton clearButton;
    private JTextArea reportTextArea;

    // Statistics Labels (dynamically updated)
    private JLabel totalStudentsLabel;
    private JLabel averageScoreLabel;
    private JLabel highestScoreLabel;
    private JLabel lowestScoreLabel;

    /**
     * Constructor: Configures main JFrame properties and builds the GUI layout.
     */
    public StudentGradeTracker() {
        // Window Configuration
        setTitle("Student Grade Tracker - GUI");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(720, 560);
        setMinimumSize(new Dimension(640, 480));
        setLocationRelativeTo(null); // Center on screen

        // Apply System Look and Feel for native UI appearance
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Fall back to default Swing look and feel if system L&F unavailable
        }

        // Initialize and layout GUI components
        setupUI();
    }

    /**
     * UI Setup: Modular method connecting layout panels and controls.
     */
    private void setupUI() {
        // Main container panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // 1. Header Banner Panel (Top)
        JPanel headerPanel = createHeaderPanel();

        // 2. Input Panel (North - directly below header)
        JPanel inputPanel = createInputPanel();

        // Combine Header & Input Panel into Top Section
        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(inputPanel, BorderLayout.SOUTH);
        mainPanel.add(topContainer, BorderLayout.NORTH);

        // 3. Grade Report Display Area (Center)
        JPanel reportPanel = createReportPanel();
        mainPanel.add(reportPanel, BorderLayout.CENTER);

        // 4. Statistics Summary Dashboard (South)
        JPanel statsPanel = createStatsPanel();
        mainPanel.add(statsPanel, BorderLayout.SOUTH);

        // Add main container to JFrame
        add(mainPanel);

        // Initial UI state reset
        resetStatisticsDisplay();
    }

    /**
     * Creates the Header Title Banner.
     */
    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(new Color(41, 128, 185)); // Sleek Blue Header
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 12));

        JLabel titleLabel = new JLabel("STUDENT GRADE TRACKER");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        panel.add(titleLabel);

        return panel;
    }

    /**
     * Creates the Input Panel with JTextField and JButtons.
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Grade Input Panel", TitledBorder.LEFT, TitledBorder.TOP));

        JLabel inputPromptLabel = new JLabel("Enter Student Grade (0 - 100):");
        inputPromptLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        gradeInputField = new JTextField(10);
        gradeInputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        addGradeButton = new JButton("Add Grade");
        addGradeButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addGradeButton.setBackground(new Color(46, 204, 113)); // Emerald Green
        addGradeButton.setForeground(Color.BLACK);

        clearButton = new JButton("Reset All");
        clearButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Register ActionListeners (Event Handlers)
        addGradeButton.addActionListener(e -> processGradeInput());
        
        // Also allow pressing 'Enter' in text field to submit grade
        gradeInputField.addActionListener(e -> processGradeInput());

        clearButton.addActionListener(e -> resetTracker());

        // Add controls to panel
        panel.add(inputPromptLabel);
        panel.add(gradeInputField);
        panel.add(addGradeButton);
        panel.add(clearButton);

        return panel;
    }

    /**
     * Creates the Central Grade Summary Display Area using JTextArea inside JScrollPane.
     */
    private JPanel createReportPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Entered Grades Log", TitledBorder.LEFT, TitledBorder.TOP));

        reportTextArea = new JTextArea();
        reportTextArea.setEditable(false);
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 13)); // Monospaced for neat column formatting
        reportTextArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * Creates the Statistics Summary Dashboard Panel containing dynamic JLabels.
     */
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 15, 10));
        panel.setBorder(new CompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createEtchedBorder(), "Live Class Statistics", TitledBorder.LEFT, TitledBorder.TOP),
                new EmptyBorder(8, 12, 8, 12)
        ));

        // Initialize dynamic JLabels
        totalStudentsLabel = createStatLabel("Total Students: 0");
        averageScoreLabel   = createStatLabel("Class Average: N/A");
        highestScoreLabel   = createStatLabel("Highest Score: N/A");
        lowestScoreLabel    = createStatLabel("Lowest Score: N/A");

        panel.add(totalStudentsLabel);
        panel.add(averageScoreLabel);
        panel.add(highestScoreLabel);
        panel.add(lowestScoreLabel);

        return panel;
    }

    /**
     * Helper to instantiate styled stat labels.
     */
    private JLabel createStatLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(44, 62, 80));
        return label;
    }

    // =========================================================================
    // EVENT PROCESSING & INPUT VALIDATION
    // =========================================================================
    /**
     * Validates input from JTextField and triggers backend update upon success.
     * Uses JOptionPane to display error alerts for invalid/out-of-range input.
     */
    private void processGradeInput() {
        String inputText = gradeInputField.getText().trim();

        // 1. Check for empty input
        if (inputText.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a numeric grade value before clicking 'Add Grade'.",
                    "Empty Input Warning",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        double grade;

        // 2. Parse input and handle non-numeric text
        try {
            grade = Double.parseDouble(inputText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid input '" + inputText + "'!\nPlease enter a valid number (e.g., 85.5 or 90).",
                    "Input Error - Non-numeric Value",
                    JOptionPane.ERROR_MESSAGE
            );
            gradeInputField.selectAll();
            gradeInputField.requestFocus();
            return;
        }

        // 3. Range Validation (0.0 to 100.0)
        if (grade < 0.0 || grade > 100.0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Grade must be between 0.0 and 100.0.\nYou entered: " + grade,
                    "Input Error - Out of Range",
                    JOptionPane.ERROR_MESSAGE
            );
            gradeInputField.selectAll();
            gradeInputField.requestFocus();
            return;
        }

        // Add validated grade to ArrayList backend
        grades.add(grade);

        // Update GUI display
        updateReportLog();
        updateStatisticsDashboard();

        // Clear input text field and restore focus for quick consecutive entry
        gradeInputField.setText("");
        gradeInputField.requestFocus();
    }

    // =========================================================================
    // BACKEND MATHEMATICAL LOGIC & GUI SYNCHRONIZATION
    // =========================================================================
    /**
     * Formats and updates the JTextArea showing the complete list of entered grades.
     */
    private void updateReportLog() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%-12s | %-12s\n", "Student Index", "Grade Score"));
        sb.append("---------------------------------\n");

        for (int i = 0; i < grades.size(); i++) {
            sb.append(String.format("Student #%-5d | %6.2f%%\n", (i + 1), grades.get(i)));
        }

        reportTextArea.setText(sb.toString());
    }

    /**
     * Calculates mathematical statistics from ArrayList<Double> and updates dynamic JLabels.
     */
    private void updateStatisticsDashboard() {
        if (grades.isEmpty()) {
            resetStatisticsDisplay();
            return;
        }

        /*
         * Statistical Calculations:
         * - Iterates over ArrayList to compute Sum, Min, Max.
         * - Calculates Average = Sum / count.
         */
        double sum = 0.0;
        double highest = grades.get(0);
        double lowest = grades.get(0);

        for (double grade : grades) {
            sum += grade;
            if (grade > highest) {
                highest = grade;
            }
            if (grade < lowest) {
                lowest = grade;
            }
        }

        double average = sum / grades.size();

        // Dynamically update JLabel text properties
        totalStudentsLabel.setText("Total Students: " + grades.size());
        averageScoreLabel.setText(String.format("Class Average: %.2f%%", average));
        highestScoreLabel.setText(String.format("Highest Score: %.2f%%", highest));
        lowestScoreLabel.setText(String.format("Lowest Score: %.2f%%", lowest));
    }

    /**
     * Resets statistics labels when no data exists.
     */
    private void resetStatisticsDisplay() {
        totalStudentsLabel.setText("Total Students: 0");
        averageScoreLabel.setText("Class Average: N/A");
        highestScoreLabel.setText("Highest Score: N/A");
        lowestScoreLabel.setText("Lowest Score: N/A");
        reportTextArea.setText("No grades entered yet. Enter a score above to begin.");
    }

    /**
     * Clears all stored grades and resets GUI components.
     */
    private void resetTracker() {
        if (grades.isEmpty()) {
            return;
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to clear all entered grade records?",
                "Confirm Reset",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (choice == JOptionPane.YES_OPTION) {
            grades.clear(); // Clear backend ArrayList
            resetStatisticsDisplay();
            gradeInputField.setText("");
            gradeInputField.requestFocus();
        }
    }

    // =========================================================================
    // APPLICATION ENTRY POINT
    // =========================================================================
    public static void main(String[] args) {
        // Run GUI construction on Event Dispatch Thread (EDT) for thread safety
        SwingUtilities.invokeLater(() -> {
            StudentGradeTracker frame = new StudentGradeTracker();
            frame.setVisible(true);
        });
    }
}
