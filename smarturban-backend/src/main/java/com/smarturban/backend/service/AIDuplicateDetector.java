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
     * Evaluates semantic text vector similarity and geographic distance to flag potential duplicates.
     */
    public DuplicateDetectionResult checkForDuplicates(String title, String description, Long categoryId, Double latitude, Double longitude) {
        List<Complaint> candidates = complaintRepository.findByCategoryId(categoryId);
        if (candidates.isEmpty()) {
            candidates = complaintRepository.findAll();
        }

        String newText = (title != null ? title : "") + " " + (description != null ? description : "");
        List<String> newFeatures = mlClassifier.extractFeatures(newText);

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
            if (latitude != null && longitude != null && existing.getLatitude() != null && existing.getLongitude() != null
                    && LocationRoutingEngine.isValidCoordinate(latitude, longitude)
                    && LocationRoutingEngine.isValidCoordinate(existing.getLatitude(), existing.getLongitude())) {

                distanceKm = LocationRoutingEngine.calculateHaversineDistance(
                        latitude, longitude, existing.getLatitude(), existing.getLongitude());

                if (distanceKm <= 0.5) { // Within 500 meters
                    isGeographicallyNearby = true;
                } else if (distanceKm > 10.0) { // If > 10km away, isolate from duplicate flagging
                    continue;
                }
            }

            // Compute Sublinear TF Vector Space Cosine Semantic Similarity
            String existingText = (existing.getTitle() != null ? existing.getTitle() : "") + " " + (existing.getDescription() != null ? existing.getDescription() : "");
            List<String> existingFeatures = mlClassifier.extractFeatures(existingText);

            double semanticSimilarity = computeSublinearCosineSimilarity(newFeatures, existingFeatures);

            // Composite Weighted Similarity Score incorporating semantic text features & geo-proximity
            double compositeSimilarity;
            String reasonSignal;

            if (isGeographicallyNearby) {
                compositeSimilarity = (semanticSimilarity * 0.6) + 0.4;
                reasonSignal = String.format(Locale.ROOT, "High semantic text similarity (%.0f%%) and close GPS proximity (%.2f km)", semanticSimilarity * 100, distanceKm);
            } else {
                compositeSimilarity = semanticSimilarity;
                reasonSignal = String.format(Locale.ROOT, "High semantic text similarity (%.0f%%)", semanticSimilarity * 100);
            }

            if (compositeSimilarity > maxSimilarity) {
                maxSimilarity = compositeSimilarity;
                highestMatchComplaint = existing;
                matchReason = reasonSignal;
            }
        }

        maxSimilarity = Math.round(maxSimilarity * 100.0) / 100.0;
        boolean isPossibleDuplicate = maxSimilarity >= 0.45 && highestMatchComplaint != null;

        return new DuplicateDetectionResult(isPossibleDuplicate, maxSimilarity, highestMatchComplaint, matchReason);
    }

    /**
     * Sublinear Term Frequency Cosine Similarity
     */
    private double computeSublinearCosineSimilarity(List<String> featuresA, List<String> featuresB) {
        if (featuresA.isEmpty() || featuresB.isEmpty()) return 0.0;

        Map<String, Integer> countA = new HashMap<>();
        Map<String, Integer> countB = new HashMap<>();

        for (String f : featuresA) countA.put(f, countA.getOrDefault(f, 0) + 1);
        for (String f : featuresB) countB.put(f, countB.getOrDefault(f, 0) + 1);

        Set<String> allFeatures = new HashSet<>(countA.keySet());
        allFeatures.addAll(countB.keySet());

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (String feat : allFeatures) {
            double tfA = countA.containsKey(feat) ? (1.0 + Math.log(countA.get(feat))) : 0.0;
            double tfB = countB.containsKey(feat) ? (1.0 + Math.log(countB.get(feat))) : 0.0;

            dotProduct += tfA * tfB;
            normA += tfA * tfA;
            normB += tfB * tfB;
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
