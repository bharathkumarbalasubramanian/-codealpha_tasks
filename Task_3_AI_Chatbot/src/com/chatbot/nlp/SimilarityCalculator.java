package com.chatbot.nlp;

import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Calculates Cosine Similarity and Jaccard Similarity between vectors and token lists.
 */
public class SimilarityCalculator {

    /**
     * Calculates Cosine Similarity between two TF-IDF double vectors.
     * Score range: 0.0 to 1.0.
     */
    public static double cosineSimilarity(double[] vectorA, double[] vectorB) {
        if (vectorA == null || vectorB == null || vectorA.length != vectorB.length || vectorA.length == 0) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += vectorA[i] * vectorA[i];
            normB += vectorB[i] * vectorB[i];
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Calculates Jaccard Similarity index between two token lists.
     */
    public static double jaccardSimilarity(List<String> listA, List<String> listB) {
        if (listA == null || listB == null || listA.isEmpty() || listB.isEmpty()) {
            return 0.0;
        }

        Set<String> setA = new HashSet<>(listA);
        Set<String> setB = new HashSet<>(listB);

        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);

        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }
}
