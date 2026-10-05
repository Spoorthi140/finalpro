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

        public DuplicateDetectionResult(boolean possibleDuplicate, double similarityScore, Complaint relatedComplaint) {
            this.possibleDuplicate = possibleDuplicate;
            this.similarityScore = similarityScore;
            this.relatedComplaint = relatedComplaint;
        }

        public boolean isPossibleDuplicate() { return possibleDuplicate; }
        public double getSimilarityScore() { return similarityScore; }
        public Complaint getRelatedComplaint() { return relatedComplaint; }
    }

    /**
     * Checks if a new complaint submission is a possible duplicate of existing active complaints.
     * Evaluates Cosine Text Similarity and Geo-Proximity (< 0.5 km).
     */
    public DuplicateDetectionResult checkForDuplicates(String title, String description, Long categoryId, Double latitude, Double longitude) {
        List<Complaint> candidates = complaintRepository.findByCategoryId(categoryId);
        if (candidates.isEmpty()) {
            candidates = complaintRepository.findAll();
        }

        String newText = ((title != null ? title : "") + " " + (description != null ? description : "")).toLowerCase(Locale.ROOT);
        List<String> newTokens = mlClassifier.tokenizeAndClean(newText);

        Complaint highestMatchComplaint = null;
        double maxSimilarity = 0.0;

        for (Complaint existing : candidates) {
            // Exclude resolved or rejected complaints from duplicate matching
            if ("Resolved".equalsIgnoreCase(existing.getStatus()) || "Rejected".equalsIgnoreCase(existing.getStatus())) {
                continue;
            }

            // Check Geographic Proximity
            boolean isGeographicallyNearby = false;
            if (latitude != null && longitude != null && existing.getLatitude() != null && existing.getLongitude() != null) {
                double distanceKm = LocationRoutingEngine.calculateHaversineDistance(
                        latitude, longitude, existing.getLatitude(), existing.getLongitude());
                if (distanceKm <= 0.5) { // Within 500 meters
                    isGeographicallyNearby = true;
                } else if (distanceKm > 10.0) { // If > 10km away, do not flag as duplicate
                    continue;
                }
            }

            // Compute Text Jaccard & Token Similarity
            String existingText = ((existing.getTitle() != null ? existing.getTitle() : "") + " " + (existing.getDescription() != null ? existing.getDescription() : "")).toLowerCase(Locale.ROOT);
            List<String> existingTokens = mlClassifier.tokenizeAndClean(existingText);

            double textSimilarity = computeJaccardSimilarity(newTokens, existingTokens);

            // Total Weighted Similarity Score
            double compositeSimilarity = isGeographicallyNearby ? (textSimilarity * 0.6 + 0.4) : textSimilarity;

            if (compositeSimilarity > maxSimilarity) {
                maxSimilarity = compositeSimilarity;
                highestMatchComplaint = existing;
            }
        }

        // Clean round to 2 decimal places
        maxSimilarity = Math.round(maxSimilarity * 100.0) / 100.0;

        boolean isPossibleDuplicate = maxSimilarity >= 0.55 && highestMatchComplaint != null;

        return new DuplicateDetectionResult(isPossibleDuplicate, maxSimilarity, highestMatchComplaint);
    }

    private double computeJaccardSimilarity(List<String> listA, List<String> listB) {
        if (listA.isEmpty() || listB.isEmpty()) return 0.0;

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
