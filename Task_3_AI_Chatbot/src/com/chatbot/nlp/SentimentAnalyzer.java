package com.chatbot.nlp;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Multi-class Sentiment Analyzer detecting Positive, Negative, or Neutral sentiment.
 */
public class SentimentAnalyzer {

    private static final Set<String> POSITIVE_WORDS = new HashSet<>(Arrays.asList(
        "good", "great", "awesome", "excellent", "amazing", "wonderful", "fantastic", "love", "like",
        "happy", "best", "brilliant", "super", "cool", "helpful", "thanks", "thank", "nice", "perfect",
        "pleased", "delighted", "appreciate", "outstanding", "spectacular", "glad", "enjoy", "sweet"
    ));

    private static final Set<String> NEGATIVE_WORDS = new HashSet<>(Arrays.asList(
        "bad", "terrible", "horrible", "awful", "worst", "hate", "dislike", "sad", "angry", "upset",
        "annoyed", "useless", "broken", "bug", "issue", "problem", "error", "fail", "failed", "failure",
        "slow", "waste", "stupid", "disappointed", "frustrated", "unhelpful", "poor", "wrong", "diff"
    ));

    public static class SentimentResult {
        private final String sentiment; // "POSITIVE", "NEGATIVE", "NEUTRAL"
        private final double score;     // Score range -1.0 to +1.0

        public SentimentResult(String sentiment, double score) {
            this.sentiment = sentiment;
            this.score = score;
        }

        public String getSentiment() { return sentiment; }
        public double getScore() { return score; }
    }

    public static SentimentResult analyze(List<String> tokens) {
        if (tokens == null || tokens.isEmpty()) {
            return new SentimentResult("NEUTRAL", 0.0);
        }

        int posCount = 0;
        int negCount = 0;

        for (String token : tokens) {
            String word = token.toLowerCase();
            if (POSITIVE_WORDS.contains(word)) posCount++;
            if (NEGATIVE_WORDS.contains(word)) negCount++;
        }

        int totalScore = posCount - negCount;
        double normalizedScore = (double) totalScore / Math.max(1, tokens.size());

        if (totalScore > 0) {
            return new SentimentResult("POSITIVE", Math.min(1.0, normalizedScore * 2.5));
        } else if (totalScore < 0) {
            return new SentimentResult("NEGATIVE", Math.max(-1.0, normalizedScore * 2.5));
        } else {
            return new SentimentResult("NEUTRAL", 0.0);
        }
    }
}
