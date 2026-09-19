package com.chatbot;

import com.chatbot.engine.HybridChatEngine;
import com.chatbot.engine.TrainerEngine;
import com.chatbot.gui.SwingChatGUI;
import com.chatbot.model.ChatMessage;
import com.chatbot.model.FAQDataset;
import com.chatbot.server.ChatbotWebServer;

import java.io.File;
import java.util.Arrays;

/**
 * Main application launcher for the Java AI Chatbot.
 */
public class App {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   🤖 Artificial Intelligence Chatbot (Java)   ");
        System.out.println("=================================================");

        String dataFilePath = "data" + File.separator + "faqs.json";
        String webRootDir = "web";
        int port = 8080;
        boolean runWeb = true;
        boolean runSwing = false;
        boolean runTest = false;

        for (int i = 0; i < args.length; i++) {
            if ("--port".equalsIgnoreCase(args[i]) && i + 1 < args.length) {
                try {
                    port = Integer.parseInt(args[++i]);
                } catch (NumberFormatException ignored) {}
            } else if ("--swing".equalsIgnoreCase(args[i])) {
                runSwing = true;
                runWeb = false;
            } else if ("--both".equalsIgnoreCase(args[i])) {
                runSwing = true;
                runWeb = true;
            } else if ("--test".equalsIgnoreCase(args[i])) {
                runTest = true;
                runWeb = false;
            }
        }

        // Initialize Core Engine Architecture
        FAQDataset faqDataset = new FAQDataset(dataFilePath);
        HybridChatEngine chatEngine = new HybridChatEngine(faqDataset);
        TrainerEngine trainerEngine = new TrainerEngine(faqDataset, chatEngine);

        if (runTest) {
            runSelfTests(chatEngine);
            return;
        }

        if (runSwing) {
            System.out.println("🖥️ Launching Desktop Java Swing GUI...");
            SwingChatGUI gui = new SwingChatGUI(chatEngine, trainerEngine, faqDataset);
            gui.display();
        }

        if (runWeb) {
            try {
                ChatbotWebServer webServer = new ChatbotWebServer(port, chatEngine, trainerEngine, faqDataset, webRootDir);
                webServer.start();
            } catch (Exception e) {
                System.err.println("❌ Failed to start web server: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void runSelfTests(HybridChatEngine engine) {
        System.out.println("\n🧪 Running Chatbot NLP & Intent Engine Verification Tests...\n");
        String[] testQueries = {
            "Hello there!",
            "What is Java programming language?",
            "Can you explain how NLP tokenization and tfidf work?",
            "Where is my shipping order?",
            "How do I apply for university scholarships?",
            "This chatbot is terrible and useless", // Negative sentiment test
            "I love this awesome AI chatbot!",       // Positive sentiment test
            "Random nonsense xyz123"                // Fallback test
        };

        for (String q : testQueries) {
            ChatMessage res = engine.processQuery(q);
            System.out.println("Query:      \"" + q + "\"");
            System.out.println("Intent:     " + res.getIntentTag() + " (Match: " + res.getMatchType() + ")");
            System.out.println("Confidence: " + Math.round(res.getConfidenceScore() * 100.0) + "%");
            System.out.println("Sentiment:  " + res.getSentiment());
            System.out.println("Tokens:     " + res.getTokens());
            System.out.println("Stemmed:    " + res.getStemmedTokens());
            System.out.println("Response:   " + res.getBotResponse());
            System.out.println("-------------------------------------------------");
        }
        System.out.println("✅ Verification tests completed successfully!");
    }
}
