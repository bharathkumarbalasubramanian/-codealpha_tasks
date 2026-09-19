package com.chatbot.nlp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Handles text cleaning, normalization, and tokenization.
 */
public class Tokenizer {

    public static List<String> tokenize(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        
        // Lowercase normalization
        String normalized = text.toLowerCase(Locale.ENGLISH);

        // Replace punctuation with spaces except contractions/hyphens if needed
        normalized = normalized.replaceAll("[^a-z0-9\\s]", " ");

        // Split by whitespace
        String[] rawTokens = normalized.split("\\s+");
        List<String> tokens = new ArrayList<>();

        for (String t : rawTokens) {
            String trimmed = t.trim();
            if (!trimmed.isEmpty()) {
                tokens.add(trimmed);
            }
        }
        return tokens;
    }

    public static String normalizeText(String text) {
        List<String> tokens = tokenize(text);
        return String.join(" ", tokens);
    }
}
