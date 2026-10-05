package com.smarturban.backend.service;

import com.smarturban.backend.entity.Complaint;
import com.smarturban.backend.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AIDuplicateDetector {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintMLClassifier mlClassifier;

    public static class DuplicateDetectionResult {
        private final boolean possibleDuplicate;
        private final double similarityScore;
        private final Complaint relatedComplaint;
        private final String warningReason;

        public DuplicateDetectionResult(boolean possibleDuplicate, double similarityScore, Complaint relatedComplaint, String warningReason) {
            this.possibleDuplicate = possibleDuplicate;
            this.similarityScore = similarityScore;
            this.relatedComplaint = relatedComplaint;
            this.warningReason = warningReason;
        }

        public boolean isPossibleDuplicate() { return possibleDuplicate; }
        public double getSimilarityScore() { return similarityScore; }
        public Complaint getRelatedComplaint() { return relatedComplaint; }
        public String getWarningReason() { return warningReason; }
    }

    /**
     * Semantic text similarity using term frequency vector space model & geographic proximity calculation.
     */
    public DuplicateDetectionResult checkForDuplicates(String title, String description, Long categoryId, Double latitude, Double longitude) {
        List<Complaint> candidates = complaintRepository.findByCategoryId(categoryId);
        if (candidates.isEmpty()) {
            candidates = complaintRepository.findAll();
        }

        String newText = (title != null ? title : "") + " " + (description != null ? description : "");
        List<String> newTokens = mlClassifier.tokenizeAndClean(newText);

        Complaint highestMatchComplaint = null;
        double maxSimilarity = 0.0;
        String matchReason = "No duplicate detected";

        for (Complaint existing : candidates) {
            // Exclude resolved or rejected complaints from duplicate matching
            if ("Resolved".equalsIgnoreCase(existing.getStatus()) || "Rejected".equalsIgnoreCase(existing.getStatus())) {
                continue;
            }

            double distanceKm = -1.0;
            boolean isGeographicallyNearby = false;
            if (latitude != null && longitude != null && existing.getLatitude() != null && existing.getLongitude() != null) {
                distanceKm = LocationRoutingEngine.calculateHaversineDistance(
                        latitude, longitude, existing.getLatitude(), existing.getLongitude());
                if (distanceKm <= 0.5) { // Within 500 meters
                    isGeographicallyNearby = true;
                } else if (distanceKm > 10.0) { // If > 10km away, do not flag as duplicate
                    continue;
                }
            }

            // Compute Vector Space Cosine Semantic Text Similarity
            String existingText = (existing.getTitle() != null ? existing.getTitle() : "") + " " + (existing.getDescription() != null ? existing.getDescription() : "");
            List<String> existingTokens = mlClassifier.tokenizeAndClean(existingText);

            double textSimilarity = computeVectorCosineSimilarity(newTokens, existingTokens);

            // Composite Weighted Similarity Score incorporating text semantics & geo-proximity
            double compositeSimilarity;
            String reasonSignal;

            if (isGeographicallyNearby) {
                compositeSimilarity = (textSimilarity * 0.6) + 0.4;
                reasonSignal = String.format(Locale.ROOT, "High text similarity (%.0f%%) and close GPS proximity (%.2f km)", textSimilarity * 100, distanceKm);
            } else {
                compositeSimilarity = textSimilarity;
                reasonSignal = String.format(Locale.ROOT, "High semantic text similarity (%.0f%%)", textSimilarity * 100);
            }

            if (compositeSimilarity > maxSimilarity) {
                maxSimilarity = compositeSimilarity;
                highestMatchComplaint = existing;
                matchReason = reasonSignal;
            }
        }

        // Clean round to 2 decimal places
        maxSimilarity = Math.round(maxSimilarity * 100.0) / 100.0;

        boolean isPossibleDuplicate = maxSimilarity >= 0.50 && highestMatchComplaint != null;

        return new DuplicateDetectionResult(isPossibleDuplicate, maxSimilarity, highestMatchComplaint, matchReason);
    }

    private double computeVectorCosineSimilarity(List<String> tokensA, List<String> tokensB) {
        if (tokensA.isEmpty() || tokensB.isEmpty()) return 0.0;

        Map<String, Integer> tfA = new HashMap<>();
        Map<String, Integer> tfB = new HashMap<>();

        for (String t : tokensA) tfA.put(t, tfA.getOrDefault(t, 0) + 1);
        for (String t : tokensB) tfB.put(t, tfB.getOrDefault(t, 0) + 1);

        Set<String> allWords = new HashSet<>(tfA.keySet());
        allWords.addAll(tfB.keySet());

        double dotProduct = 0.0;
        double magA = 0.0;
        double magB = 0.0;

        for (String word : allWords) {
            int countA = tfA.getOrDefault(word, 0);
            int countB = tfB.getOrDefault(word, 0);

            dotProduct += countA * countB;
            magA += countA * countA;
            magB += countB * countB;
        }

        if (magA == 0.0 || magB == 0.0) return 0.0;

        return dotProduct / (Math.sqrt(magA) * Math.sqrt(magB));
    }
}
