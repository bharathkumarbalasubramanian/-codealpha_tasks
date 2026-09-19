package com.chatbot.gui;

import com.chatbot.engine.HybridChatEngine;
import com.chatbot.engine.TrainerEngine;
import com.chatbot.model.ChatMessage;
import com.chatbot.model.FAQDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Modern Java Swing Desktop GUI for real-time chatbot interaction.
 */
public class SwingChatGUI extends JFrame {

    private final HybridChatEngine chatEngine;
    private final TrainerEngine trainerEngine;
    private final FAQDataset faqDataset;

    private JPanel chatPanel;
    private JScrollPane scrollPane;
    private JTextField inputField;
    private JButton sendButton;
    private JLabel statusLabel;

    public SwingChatGUI(HybridChatEngine chatEngine, TrainerEngine trainerEngine, FAQDataset faqDataset) {
        this.chatEngine = chatEngine;
        this.trainerEngine = trainerEngine;
        this.faqDataset = faqDataset;

        initUI();
    }

    private void initUI() {
        setTitle("🤖 Java AI Chatbot - Desktop GUI");
        setSize(750, 700);
        setMinimumSize(new Dimension(500, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Dark Theme Colors
        Color bgColor = new Color(24, 26, 38);
        Color cardColor = new Color(33, 36, 52);
        Color accentColor = new Color(99, 102, 241);
        Color textColor = new Color(241, 245, 249);
        Color subTextColor = new Color(148, 163, 184);

        getContentPane().setBackground(bgColor);
        setLayout(new BorderLayout());

        // Header Bar
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(cardColor);
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("🤖 Java AI Chatbot");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(textColor);

        statusLabel = new JLabel("Engine: Hybrid NLP (TF-IDF + Cosine Sim)");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(subTextColor);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(statusLabel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Chat Container
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        chatPanel.setBackground(bgColor);
        chatPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        scrollPane = new JScrollPane(chatPanel);
        scrollPane.setBorder(null);
        scrollPane.setBackground(bgColor);
        scrollPane.getViewport().setBackground(bgColor);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(cardColor);
        inputPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        inputField = new JTextField();
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        inputField.setBackground(new Color(24, 26, 38));
        inputField.setForeground(textColor);
        inputField.setCaretColor(textColor);
        inputField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85), 1),
            BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        sendButton = new JButton("Send ➔");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(accentColor);
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        sendButton.addActionListener((ActionEvent e) -> sendMessage());
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    sendMessage();
                }
            }
        });

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        // Welcome message
        addBotBubble("Hello! I am your Java AI Chatbot. Ask me any question or test my NLP capabilities!", "greeting", 1.0, "RULE_REGEX");
    }

    private void sendMessage() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;

        addUserBubble(text);
        inputField.setText("");

        // Process query
        ChatMessage response = chatEngine.processQuery(text);
        trainerEngine.recordQuery(response);

        addBotBubble(
            response.getBotResponse(),
            response.getIntentTag(),
            response.getConfidenceScore(),
            response.getMatchType()
        );

        // Auto scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    private void addUserBubble(String text) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        wrapper.setOpaque(false);

        JPanel bubble = new JPanel(new BorderLayout());
        bubble.setBackground(new Color(99, 102, 241));
        bubble.setBorder(new EmptyBorder(10, 14, 10, 14));

        JLabel label = new JLabel("<html><p style='width: 320px; color: #ffffff; font-family: Segoe UI; font-size: 13px;'>" + escapeHtml(text) + "</p></html>");
        bubble.add(label);

        wrapper.add(bubble);
        chatPanel.add(wrapper);
        chatPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        chatPanel.revalidate();
    }

    private void addBotBubble(String text, String intent, double confidence, String matchType) {
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wrapper.setOpaque(false);

        JPanel bubble = new JPanel(new BorderLayout(0, 5));
        bubble.setBackground(new Color(33, 36, 52));
        bubble.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel label = new JLabel("<html><p style='width: 360px; color: #f1f5f9; font-family: Segoe UI; font-size: 13px;'>" + escapeHtml(text) + "</p></html>");
        
        int confPercent = (int) Math.round(confidence * 100);
        String metaText = String.format("<font color='#94a3b8' size='2'>Intent: <b>%s</b> | Conf: <b>%d%%</b> | Match: %s</font>", intent, confPercent, matchType);
        JLabel metaLabel = new JLabel("<html>" + metaText + "</html>");

        bubble.add(label, BorderLayout.CENTER);
        bubble.add(metaLabel, BorderLayout.SOUTH);

        wrapper.add(bubble);
        chatPanel.add(wrapper);
        chatPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        chatPanel.revalidate();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    public void display() {
        SwingUtilities.invokeLater(() -> setVisible(true));
    }
}
