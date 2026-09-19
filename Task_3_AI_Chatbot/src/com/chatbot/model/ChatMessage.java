package com.chatbot.model;

import java.util.List;

/**
 * Encapsulates user input, bot output, confidence score, NLP tokens, and sentiment.
 */
public class ChatMessage {
    private String userQuery;
    private String botResponse;
    private String intentTag;
    private double confidenceScore;
    private String matchType; // "ML_TFIDF", "RULE_REGEX", "FALLBACK"
    private String sentiment; // "POSITIVE", "NEGATIVE", "NEUTRAL"
    private List<String> tokens;
    private List<String> stemmedTokens;
    private long timestamp;

    public ChatMessage(String userQuery, String botResponse, String intentTag, double confidenceScore, 
                       String matchType, String sentiment, List<String> tokens, List<String> stemmedTokens) {
        this.userQuery = userQuery;
        this.botResponse = botResponse;
        this.intentTag = intentTag;
        this.confidenceScore = confidenceScore;
        this.matchType = matchType;
        this.sentiment = sentiment;
        this.tokens = tokens;
        this.stemmedTokens = stemmedTokens;
        this.timestamp = System.currentTimeMillis();
    }

    public String getUserQuery() { return userQuery; }
    public String getBotResponse() { return botResponse; }
    public String getIntentTag() { return intentTag; }
    public double getConfidenceScore() { return confidenceScore; }
    public String getMatchType() { return matchType; }
    public String getSentiment() { return sentiment; }
    public List<String> getTokens() { return tokens; }
    public List<String> getStemmedTokens() { return stemmedTokens; }
    public long getTimestamp() { return timestamp; }
}
