package com.chatbot.nlp;

import java.util.*;

/**
 * Computes Term Frequency - Inverse Document Frequency (TF-IDF) feature vectors.
 */
public class TFIDFVectorizer {

    private final List<String> vocabulary = new ArrayList<>();
    private final Map<String, Integer> termIndexMap = new HashMap<>();
    private final Map<String, Double> idfMap = new HashMap<>();

    public void fit(List<List<String>> corpus) {
        vocabulary.clear();
        termIndexMap.clear();
        idfMap.clear();

        Set<String> vocabSet = new LinkedHashSet<>();
        int totalDocuments = corpus.size();

        for (List<String> doc : corpus) {
            vocabSet.addAll(doc);
        }

        int index = 0;
        for (String term : vocabSet) {
            vocabulary.add(term);
            termIndexMap.put(term, index++);
        }

        // Calculate IDF for each term in vocabulary
        for (String term : vocabulary) {
            int docCountWithTerm = 0;
            for (List<String> doc : corpus) {
                if (doc.contains(term)) {
                    docCountWithTerm++;
                }
            }
            // Standard smooth IDF formula: log((1 + totalDocuments) / (1 + docCountWithTerm)) + 1
            double idf = Math.log((1.0 + totalDocuments) / (1.0 + docCountWithTerm)) + 1.0;
            idfMap.put(term, idf);
        }
    }

    public double[] transform(List<String> document) {
        double[] vector = new double[vocabulary.size()];
        if (vocabulary.isEmpty() || document.isEmpty()) {
            return vector;
        }

        // Count Term Frequency (TF)
        Map<String, Integer> termFreq = new HashMap<>();
        for (String term : document) {
            termFreq.put(term, termFreq.getOrDefault(term, 0) + 1);
        }

        int docSize = document.size();
        for (Map.Entry<String, Integer> entry : termFreq.entrySet()) {
            String term = entry.getKey();
            if (termIndexMap.containsKey(term)) {
                int idx = termIndexMap.get(term);
                double tf = (double) entry.getValue() / docSize;
                double idf = idfMap.getOrDefault(term, 1.0);
                vector[idx] = tf * idf;
            }
        }
        return vector;
    }

    public List<String> getVocabulary() {
        return vocabulary;
    }
}
