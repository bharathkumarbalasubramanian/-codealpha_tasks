package com.chatbot.engine;

import com.chatbot.model.*;
import com.chatbot.nlp.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Hybrid Machine Learning & Rule-based NLP Reasoning Engine.
 */
public class HybridChatEngine {

    private final FAQDataset faqDataset;
    private final TFIDFVectorizer vectorizer;
    private final List<PatternVector> patternVectors = new ArrayList<>();
    private String currentContext = null;
    private double confidenceThreshold = 0.18; // Minimum cosine similarity cutoff (18%)

    // Helper class linking a pattern to its intent and stemmed tokens
    private static class PatternVector {
        Intent intent;
        String rawPattern;
        List<String> stemmedTokens;
        double[] tfidfVector;

        PatternVector(Intent intent, String rawPattern, List<String> stemmedTokens) {
            this.intent = intent;
            this.rawPattern = rawPattern;
            this.stemmedTokens = stemmedTokens;
        }
    }

    public HybridChatEngine(FAQDataset faqDataset) {
        this.faqDataset = faqDataset;
        this.vectorizer = new TFIDFVectorizer();
        trainModel();
    }

    /**
     * Trains/retrains the TF-IDF feature space and pattern vectors.
     */
    public synchronized void trainModel() {
        patternVectors.clear();
        List<List<String>> corpus = new ArrayList<>();

        for (Intent intent : faqDataset.getIntents()) {
            for (String pattern : intent.getPatterns()) {
                List<String> rawTokens = Tokenizer.tokenize(pattern);
                List<String> filteredTokens = StopWords.removeStopWords(rawTokens);
                List<String> stemmed = PorterStemmer.stemTokens(filteredTokens);

                if (!stemmed.isEmpty()) {
                    corpus.add(stemmed);
                    patternVectors.add(new PatternVector(intent, pattern, stemmed));
                }
            }
        }

        if (!corpus.isEmpty()) {
            vectorizer.fit(corpus);
            for (PatternVector pv : patternVectors) {
                pv.tfidfVector = vectorizer.transform(pv.stemmedTokens);
            }
            System.out.println("[HybridChatEngine] Model trained successfully across " + patternVectors.size() + " pattern vectors.");
        }
    }

    /**
     * Processes user input message through NLP + Rule + ML Pipeline.
     */
    public ChatMessage processQuery(String userQuery) {
        if (userQuery == null || userQuery.trim().isEmpty()) {
            return new ChatMessage(
                userQuery,
                "Please type a question or command!",
                "empty_input",
                1.0,
                "RULE_REGEX",
                "NEUTRAL",
                Collections.emptyList(),
                Collections.emptyList()
            );
        }

        // 1. Tokenize & Clean
        List<String> rawTokens = Tokenizer.tokenize(userQuery);
        List<String> filteredTokens = StopWords.removeStopWords(rawTokens);
        List<String> stemmedTokens = PorterStemmer.stemTokens(filteredTokens);

        // 2. Sentiment Analysis
        SentimentAnalyzer.SentimentResult sentiment = SentimentAnalyzer.analyze(rawTokens);

        // 3. Phase A: Rule-Based / Regex Pattern Matcher
        String lowerQuery = userQuery.trim().toLowerCase();
        for (Intent intent : faqDataset.getIntents()) {
            if (intent.getRegexRule() != null && !intent.getRegexRule().trim().isEmpty()) {
                try {
                    Pattern regex = Pattern.compile(intent.getRegexRule(), Pattern.CASE_INSENSITIVE);
                    if (regex.matcher(lowerQuery).find()) {
                        updateContext(intent);
                        return new ChatMessage(
                            userQuery,
                            intent.getRandomResponse(),
                            intent.getTag(),
                            0.98, // 98% confidence for exact rule match
                            "RULE_REGEX",
                            sentiment.getSentiment(),
                            rawTokens,
                            stemmedTokens
                        );
                    }
                } catch (Exception e) {
                    // Ignore regex syntax errors
                }
            }
        }

        // 4. Phase B: ML TF-IDF + Cosine Similarity Vector Space Classification
        double[] queryVector = vectorizer.transform(stemmedTokens);
        
        Intent bestMatchIntent = null;
        double bestSimilarityScore = 0.0;
        String matchedPattern = null;

        for (PatternVector pv : patternVectors) {
            // Check context filter if applicable
            if (pv.intent.getContextFilter() != null && !pv.intent.getContextFilter().isEmpty()) {
                if (!pv.intent.getContextFilter().equalsIgnoreCase(currentContext)) {
                    continue; // Skip intent if current context doesn't match requirement
                }
            }

            // Calculate Cosine Similarity
            double similarity = SimilarityCalculator.cosineSimilarity(queryVector, pv.tfidfVector);
            
            // Secondary Jaccard boost for direct token overlaps
            double jaccard = SimilarityCalculator.jaccardSimilarity(stemmedTokens, pv.stemmedTokens);
            double hybridScore = (similarity * 0.75) + (jaccard * 0.25);

            if (hybridScore > bestSimilarityScore) {
                bestSimilarityScore = hybridScore;
                bestMatchIntent = pv.intent;
                matchedPattern = pv.rawPattern;
            }
        }

        // Normalize confidence score to percentage (0.0 to 1.0)
        double confidence = Math.min(1.0, bestSimilarityScore);

        // 5. Threshold Evaluation
        if (bestMatchIntent != null && confidence >= confidenceThreshold) {
            updateContext(bestMatchIntent);
            return new ChatMessage(
                userQuery,
                bestMatchIntent.getRandomResponse(),
                bestMatchIntent.getTag(),
                confidence,
                "ML_TFIDF",
                sentiment.getSentiment(),
                rawTokens,
                stemmedTokens
            );
        }

        // 6. Phase C: Smart Fallback Logic
        String fallbackResponse = generateFallbackResponse(sentiment.getSentiment());
        return new ChatMessage(
            userQuery,
            fallbackResponse,
            "fallback",
            confidence,
            "FALLBACK",
            sentiment.getSentiment(),
            rawTokens,
            stemmedTokens
        );
    }

    private void updateContext(Intent intent) {
        if (intent.getContextSet() != null && !intent.getContextSet().isEmpty()) {
            this.currentContext = intent.getContextSet();
        }
    }

    private String generateFallbackResponse(String sentiment) {
        if ("NEGATIVE".equals(sentiment)) {
            return "I apologize for any frustration! I'm still learning. Could you rephrase your question, or try asking about Java programming, NLP techniques, or support policies?";
        }
        return "I'm not completely sure I understood that question. You can ask me about Java, Natural Language Processing, customer support, or train me with new questions in the FAQ Trainer!";
    }

    public double getConfidenceThreshold() {
        return confidenceThreshold;
    }

    public void setConfidenceThreshold(double threshold) {
        this.confidenceThreshold = threshold;
    }

    public String getCurrentContext() {
        return currentContext;
    }
}
