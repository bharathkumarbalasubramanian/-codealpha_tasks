package com.chatbot.engine;

import com.chatbot.model.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manages FAQ training operations, model updates, and runtime analytics.
 */
public class TrainerEngine {

    private final FAQDataset faqDataset;
    private final HybridChatEngine chatEngine;

    // Analytics counters
    private final AtomicInteger totalQueries = new AtomicInteger(0);
    private final AtomicInteger successfulMatches = new AtomicInteger(0);
    private final AtomicInteger fallbackCount = new AtomicInteger(0);
    private final Map<String, AtomicInteger> intentHitMap = new HashMap<>();

    public TrainerEngine(FAQDataset faqDataset, HybridChatEngine chatEngine) {
        this.faqDataset = faqDataset;
        this.chatEngine = chatEngine;
    }

    public synchronized void retrainModel() {
        chatEngine.trainModel();
    }

    public synchronized void addOrUpdateFAQ(String tag, String category, List<String> patterns, List<String> responses, String regexRule) {
        Intent intent = new Intent(tag, category, patterns, responses);
        if (regexRule != null && !regexRule.trim().isEmpty()) {
            intent.setRegexRule(regexRule.trim());
        }
        faqDataset.addOrUpdateIntent(intent);
        retrainModel();
    }

    public synchronized boolean deleteFAQ(String tag) {
        boolean deleted = faqDataset.deleteIntent(tag);
        if (deleted) {
            retrainModel();
        }
        return deleted;
    }

    public void recordQuery(ChatMessage message) {
        totalQueries.incrementAndGet();
        if ("FALLBACK".equals(message.getMatchType())) {
            fallbackCount.incrementAndGet();
        } else {
            successfulMatches.incrementAndGet();
            intentHitMap.computeIfAbsent(message.getIntentTag(), k -> new AtomicInteger(0)).incrementAndGet();
        }
    }

    public Map<String, Object> getAnalyticsSummary() {
        Map<String, Object> stats = new HashMap<>();
        int total = totalQueries.get();
        int matched = successfulMatches.get();
        int fallback = fallbackCount.get();
        double matchRate = total > 0 ? ((double) matched / total) * 100.0 : 0.0;

        stats.put("totalQueries", total);
        stats.put("successfulMatches", matched);
        stats.put("fallbackCount", fallback);
        stats.put("matchRatePercent", Math.round(matchRate * 10.0) / 10.0);
        stats.put("totalIntents", faqDataset.getIntents().size());

        Map<String, Integer> hits = new HashMap<>();
        intentHitMap.forEach((tag, count) -> hits.put(tag, count.get()));
        stats.put("intentHits", hits);

        return stats;
    }
}
