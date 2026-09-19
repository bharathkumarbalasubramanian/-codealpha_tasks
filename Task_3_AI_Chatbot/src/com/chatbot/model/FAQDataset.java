package com.chatbot.model;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/**
 * Manages the intent training dataset and FAQ repository with disk persistence.
 */
public class FAQDataset {

    private final List<Intent> intents = new ArrayList<>();
    private final String datasetFilePath;

    public FAQDataset(String datasetFilePath) {
        this.datasetFilePath = datasetFilePath;
        loadOrInitializeDefaults();
    }

    public List<Intent> getIntents() {
        return intents;
    }

    public synchronized void addOrUpdateIntent(Intent intent) {
        for (int i = 0; i < intents.size(); i++) {
            if (intents.get(i).getTag().equalsIgnoreCase(intent.getTag())) {
                intents.set(i, intent);
                saveToFile();
                return;
            }
        }
        intents.add(intent);
        saveToFile();
    }

    public synchronized boolean deleteIntent(String tag) {
        boolean removed = intents.removeIf(intent -> intent.getTag().equalsIgnoreCase(tag));
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    public Intent getIntentByTag(String tag) {
        for (Intent intent : intents) {
            if (intent.getTag().equalsIgnoreCase(tag)) {
                return intent;
            }
        }
        return null;
    }

    public synchronized void loadOrInitializeDefaults() {
        intents.clear();
        File file = new File(datasetFilePath);
        if (file.exists()) {
            try {
                String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
                parseJsonIntents(content);
                if (!intents.isEmpty()) {
                    System.out.println("[FAQDataset] Loaded " + intents.size() + " intents from " + datasetFilePath);
                    return;
                }
            } catch (Exception e) {
                System.err.println("[FAQDataset] Error reading dataset file: " + e.getMessage() + ". Loading default dataset.");
            }
        }
        
        loadDefaultIntents();
        saveToFile();
    }

    private void loadDefaultIntents() {
        // 1. Greeting Intent
        Intent greeting = new Intent("greeting", "General",
            Arrays.asList("hello", "hi", "hey", "good morning", "good evening", "greetings", "whats up", "howdy"),
            Arrays.asList(
                "Hello! How can I help you today?",
                "Hi there! What can I assist you with?",
                "Greetings! Feel free to ask me any question!"
            ));
        greeting.setRegexRule("^(hi|hello|hey|greetings|howdy)(!|\\s|$)");
        intents.add(greeting);

        // 2. Goodbye Intent
        Intent goodbye = new Intent("goodbye", "General",
            Arrays.asList("bye", "goodbye", "see you later", "exit", "quit", "talk to you later", "have a nice day"),
            Arrays.asList(
                "Goodbye! Have a great day ahead!",
                "Bye! Let me know if you need anything else.",
                "See you later! Take care!"
            ));
        goodbye.setRegexRule("^(bye|goodbye|exit|quit)(!|\\s|$)");
        intents.add(goodbye);

        // 3. Bot Capability / Help
        Intent help = new Intent("bot_capabilities", "General",
            Arrays.asList("what can you do", "help me", "who are you", "what is your purpose", "features", "capabilities"),
            Arrays.asList(
                "I am an AI-powered Chatbot built with Natural Language Processing (NLP) in Java! I can answer FAQs, process natural language questions using TF-IDF and Cosine Similarity, analyze sentiment, and learn new intents dynamically.",
                "I assist with FAQs, technical queries, university information, and e-commerce orders. You can also train me live via the FAQ Trainer interface!"
            ));
        intents.add(help);

        // 4. Java Programming FAQ
        Intent javaFaq = new Intent("java_programming", "Technical",
            Arrays.asList("what is java", "explain java jdk", "jvm vs jre", "why use java", "java features", "object oriented programming"),
            Arrays.asList(
                "Java is a popular, class-based, object-oriented programming language designed for platform independence via the Java Virtual Machine (JVM). Key features include Write Once, Run Anywhere (WORA), robust memory management, and multi-threading.",
                "JDK is the Development Kit needed to build Java programs, JRE is the Runtime Environment needed to run them, and JVM (Java Virtual Machine) executes compiled bytecode."
            ));
        intents.add(javaFaq);

        // 5. Natural Language Processing (NLP) FAQ
        Intent nlpFaq = new Intent("nlp_explanation", "AI/ML",
            Arrays.asList("what is nlp", "how does nlp work", "explain tfidf", "cosine similarity in nlp", "stemming vs lemmatization"),
            Arrays.asList(
                "Natural Language Processing (NLP) is a branch of Artificial Intelligence that enables computers to understand, interpret, and manipulate human language.",
                "In this chatbot: 1) Tokenization splits text, 2) StopWords filter noise, 3) Porter Stemmer reduces root words, 4) TF-IDF computes term importance, and 5) Cosine Similarity computes vector similarity to match your intent!"
            ));
        intents.add(nlpFaq);

        // 6. E-Commerce / Order FAQ
        Intent orderFaq = new Intent("order_shipping", "E-Commerce",
            Arrays.asList("where is my order", "track order", "shipping policy", "delivery status", "refund policy", "cancel order"),
            Arrays.asList(
                "You can track your order in real time by logging into your account under 'My Orders'. Standard shipping takes 3-5 business days.",
                "Our return policy allows hassle-free returns within 30 days of receipt. Refunds are processed back to your original payment method within 5 days."
            ));
        intents.add(orderFaq);

        // 7. University / Admission FAQ
        Intent admissionFaq = new Intent("university_admission", "Academic",
            Arrays.asList("how to apply", "admission deadline", "tuition fees", "courses offered", "scholarships available"),
            Arrays.asList(
                "Admissions for the upcoming academic year are open! Applications can be submitted online via our admissions portal before July 31st.",
                "We offer merit-based scholarships covering up to 100% of tuition fees for high-performing applicants."
            ));
        intents.add(admissionFaq);

        // 8. Contact Support Intent
        Intent support = new Intent("contact_support", "Support",
            Arrays.asList("contact human", "talk to agent", "customer service phone", "support email", "speak to representative"),
            Arrays.asList(
                "You can reach our human support team 24/7 via email at support@aichatbot.org or call our hotline at 1-800-555-CHAT."
            ));
        intents.add(support);
    }

    public synchronized void saveToFile() {
        try {
            File file = new File(datasetFilePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            String json = toJsonString();
            Files.writeString(file.toPath(), json, StandardCharsets.UTF_8);
            System.out.println("[FAQDataset] Persisted " + intents.size() + " intents to " + datasetFilePath);
        } catch (Exception e) {
            System.err.println("[FAQDataset] Failed to save dataset to file: " + e.getMessage());
        }
    }

    public String toJsonString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < intents.size(); i++) {
            Intent intent = intents.get(i);
            sb.append("  {\n");
            sb.append("    \"tag\": \"").append(escapeJson(intent.getTag())).append("\",\n");
            sb.append("    \"category\": \"").append(escapeJson(intent.getCategory() != null ? intent.getCategory() : "General")).append("\",\n");
            sb.append("    \"patterns\": ").append(listToJsonArray(intent.getPatterns())).append(",\n");
            sb.append("    \"responses\": ").append(listToJsonArray(intent.getResponses())).append(",\n");
            sb.append("    \"regexRule\": ").append(intent.getRegexRule() != null ? "\"" + escapeJson(intent.getRegexRule()) + "\"" : "null").append("\n");
            sb.append("  }").append(i < intents.size() - 1 ? ",\n" : "\n");
        }
        sb.append("]");
        return sb.toString();
    }

    private void parseJsonIntents(String json) {
        try {
            // Light custom JSON parser for Intent list array
            String trimmed = json.trim();
            if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) return;
            
            // Split object entries by top-level "},"
            trimmed = trimmed.substring(1, trimmed.length() - 1).trim();
            if (trimmed.isEmpty()) return;

            List<String> rawObjects = splitJsonObjects(trimmed);
            for (String rawObj : rawObjects) {
                Intent intent = parseSingleIntent(rawObj);
                if (intent != null && intent.getTag() != null) {
                    intents.add(intent);
                }
            }
        } catch (Exception e) {
            System.err.println("[FAQDataset] JSON parsing error: " + e.getMessage());
        }
    }

    private List<String> splitJsonObjects(String str) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) {
                    result.add(str.substring(start, i + 1).trim());
                    // Skip comma and whitespace
                    while (i + 1 < str.length() && (str.charAt(i + 1) == ',' || Character.isWhitespace(str.charAt(i + 1)))) {
                        i++;
                    }
                    start = i + 1;
                }
            }
        }
        return result;
    }

    private Intent parseSingleIntent(String jsonObj) {
        String tag = extractJsonField(jsonObj, "tag");
        String category = extractJsonField(jsonObj, "category");
        String regexRule = extractJsonField(jsonObj, "regexRule");
        List<String> patterns = extractJsonArray(jsonObj, "patterns");
        List<String> responses = extractJsonArray(jsonObj, "responses");

        if (tag == null) return null;
        Intent intent = new Intent(tag, category != null ? category : "General", patterns, responses);
        if (regexRule != null && !regexRule.equals("null")) {
            intent.setRegexRule(regexRule);
        }
        return intent;
    }

    private String extractJsonField(String json, String fieldName) {
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return null;
        int colonIdx = json.indexOf(":", idx + key.length());
        if (colonIdx == -1) return null;
        int startVal = colonIdx + 1;
        while (startVal < json.length() && Character.isWhitespace(json.charAt(startVal))) {
            startVal++;
        }
        if (startVal >= json.length()) return null;

        if (json.charAt(startVal) == '"') {
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
        }
        return null;
    }

    private List<String> extractJsonArray(String json, String fieldName) {
        List<String> list = new ArrayList<>();
        String key = "\"" + fieldName + "\"";
        int idx = json.indexOf(key);
        if (idx == -1) return list;
        int arrayStart = json.indexOf("[", idx);
        if (arrayStart == -1) return list;
        int arrayEnd = json.indexOf("]", arrayStart);
        if (arrayEnd == -1) return list;

        String arrayContent = json.substring(arrayStart + 1, arrayEnd).trim();
        if (arrayContent.isEmpty()) return list;

        boolean inQuotes = false;
        StringBuilder currentToken = new StringBuilder();
        for (int i = 0; i < arrayContent.length(); i++) {
            char c = arrayContent.charAt(i);
            if (c == '"' && (i == 0 || arrayContent.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
                if (!inQuotes) {
                    list.add(unescapeJson(currentToken.toString()));
                    currentToken.setLength(0);
                }
            } else if (inQuotes) {
                currentToken.append(c);
            }
        }
        return list;
    }

    private String listToJsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                sb.append("\"").append(escapeJson(list.get(i))).append("\"");
                if (i < list.size() - 1) sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private String unescapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t");
    }
}
