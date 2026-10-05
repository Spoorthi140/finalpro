package com.smarturban.backend.service;

import com.smarturban.backend.entity.Category;
import com.smarturban.backend.entity.Department;
import com.smarturban.backend.repository.CategoryRepository;
import com.smarturban.backend.repository.DepartmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ComplaintMLClassifier {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintMLClassifier.class);

    @Value("${smarturban.ai.confidence-threshold:0.35}")
    private double confidenceThreshold;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public static class ClassificationOutput {
        private final String categoryName;
        private final double confidenceScore;
        private final boolean lowConfidence;

        public ClassificationOutput(String categoryName, double confidenceScore, boolean lowConfidence) {
            this.categoryName = categoryName;
            this.confidenceScore = confidenceScore;
            this.lowConfidence = lowConfidence;
        }

        public String getCategoryName() { return categoryName; }
        public double getConfidenceScore() { return confidenceScore; }
        public boolean isLowConfidence() { return lowConfidence; }
    }

    // Ground Truth Dataset for Model Training (Feature Vectors -> Target Class)
    private static final Map<String, List<String>> TRAINING_CORPUS = new HashMap<>();

    static {
        TRAINING_CORPUS.put("Road Maintenance", Arrays.asList(
                "pothole on road asphalt pavement damaged tar street crater broken road surface speed bump caved road edge highway crack maintenance uneven paving bitumen lane avenue drive way street hole",
                "deep pothole on main road causing traffic hazard and tire damage near junction asphalt peeling off completely",
                "broken pavement tiles and large crater in asphalt road surface dangerous for two wheelers and cars",
                "caved-in road surface tar erosion broken divider kerbstone damaged road pavement needs immediate repair patch work"
        ));

        TRAINING_CORPUS.put("Streetlights", Arrays.asList(
                "streetlight not working dark bulb pole lamp flickering electricity power outage street light dark road junction evening night safety glow fixture lamp post led light wire broken fused",
                "multiple streetlights turned off whole street dark at night safety hazard fused bulb high mast light not working",
                "flickering street light pole leaning hanging electrical wire exposed near lamp post dark alleyway no light",
                "led street light broken dark street fixture burnt out electrical junction box open near light pole"
        ));

        TRAINING_CORPUS.put("Sanitation/Garbage", Arrays.asList(
                "garbage trash waste bin clean dump overflow litter rubbish foul smell uncleaned dustbin waste pile stinking debris plastic waste collection sweeping dump yard uncleared garbage dump",
                "overflowing garbage bin on sidewalk trash scattered across street foul smell uncollected municipal waste dump",
                "garbage collector did not sweep street accumulated plastic waste food waste rotting in public bin stinking area",
                "illegal dumping of construction waste debris and domestic trash near residential park needs garbage clearing truck"
        ));

        TRAINING_CORPUS.put("Water Supply", Arrays.asList(
                "water pipe leak tap supply contamination drinking water pipeline burst low pressure dirty water muddy water no water flow municipal water meter leakage valve leak water pipeline broken",
                "main water supply pipe leaking clean drinking water wasted on street low water pressure in residential houses",
                "dirty sewage mixed contaminated drinking water coming out of home tap foul smelling muddy water supply pipeline",
                "no water supply in entire locality pipeline burst near water overhead tank pipe leakage needs immediate plumber attention"
        ));

        TRAINING_CORPUS.put("Drainage", Arrays.asList(
                "drain sewer overflow gutter flooding sludge blockage storm drain manhole cover open clogged drain sewage water stagnant water rain water logging blocked drain drain line manhole overflow",
                "clogged storm drain causing street flooding during rain stagnant sewage water overflowing from open manhole cover",
                "blocked sewer line foul water entering houses open gutter overflowing with sludge blockage in main drainage network",
                "broken concrete cover on deep manhole open drain hazard stagnant water breeding mosquitoes in stormwater drain"
        ));

        TRAINING_CORPUS.put("Other Urban Infrastructure", Arrays.asList(
                "park bench public toilet wall illegal hoarding encroachment tree branch fallen public park playground noise pollution stray animal issue public property maintenance general civic complaint",
                "damaged public park fence fallen tree branch blocking pedestrian walkway illegal advertisement board hoarding",
                "encroachment on public footpath illegal vendor booth stray dogs near playground damaged public toilet door",
                "general municipal infrastructure issue damaged sign board public garden maintenance city beautification request"
        ));
    }

    private static final Pattern WORD_PATTERN = Pattern.compile("[^a-z0-9]+");

    /**
     * Preprocesses text into tokenized clean term bag
     */
    public List<String> tokenizeAndClean(String input) {
        if (input == null || input.isBlank()) {
            return Collections.emptyList();
        }
        String cleaned = input.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = cleaned.split("\\s+");
        List<String> list = new ArrayList<>();
        for (String t : tokens) {
            if (t.length() > 2) { // Filter out short stop words like "in", "on", "a", "is", "of"
                list.add(t);
            }
        }
        return list;
    }

    /**
     * Computes TF-IDF vector & Cosine Similarity / Naive Bayes Probability against category corpora
     */
    public ClassificationOutput classifyComplaint(String title, String description) {
        String combinedText = (title != null ? title : "") + " " + (description != null ? description : "");
        List<String> inputTokens = tokenizeAndClean(combinedText);

        if (inputTokens.isEmpty()) {
            return new ClassificationOutput("Other Urban Infrastructure", 0.0, true);
        }

        // Build Vocabulary across Corpus
        Set<String> vocabulary = new HashSet<>(inputTokens);
        for (List<String> docs : TRAINING_CORPUS.values()) {
            for (String doc : docs) {
                vocabulary.addAll(tokenizeAndClean(doc));
            }
        }

        // Calculate Term Frequencies for Input
        Map<String, Integer> inputTf = new HashMap<>();
        for (String token : inputTokens) {
            inputTf.put(token, inputTf.getOrDefault(token, 0) + 1);
        }

        String bestCategory = "Other Urban Infrastructure";
        double maxScore = 0.0;

        // Compare input vector against each category's aggregated trained TF-IDF model
        for (Map.Entry<String, List<String>> entry : TRAINING_CORPUS.entrySet()) {
            String category = entry.getKey();
            List<String> categoryDocs = entry.getValue();

            // Aggregated Category Term Frequencies
            Map<String, Integer> categoryTf = new HashMap<>();
            int totalCategoryTokens = 0;
            for (String doc : categoryDocs) {
                List<String> docTokens = tokenizeAndClean(doc);
                for (String token : docTokens) {
                    categoryTf.put(token, categoryTf.getOrDefault(token, 0) + 1);
                    totalCategoryTokens++;
                }
            }

            // Calculate Cosine Similarity / Dot product weighting
            double dotProduct = 0.0;
            double inputMagSq = 0.0;
            double catMagSq = 0.0;

            for (String term : inputTf.keySet()) {
                double tfIn = inputTf.get(term);
                inputMagSq += tfIn * tfIn;

                if (categoryTf.containsKey(term)) {
                    // Laplace Smoothed Weighting
                    double tfCat = (double) categoryTf.get(term) / (totalCategoryTokens + vocabulary.size());
                    dotProduct += tfIn * tfCat;
                }
            }

            for (String term : categoryTf.keySet()) {
                double tfCat = (double) categoryTf.get(term) / (totalCategoryTokens + vocabulary.size());
                catMagSq += tfCat * tfCat;
            }

            double similarity = 0.0;
            if (inputMagSq > 0 && catMagSq > 0) {
                similarity = dotProduct / (Math.sqrt(inputMagSq) * Math.sqrt(catMagSq));
            }

            if (similarity > maxScore) {
                maxScore = similarity;
                bestCategory = category;
            }
        }

        // Normalize Score to a 0.0 - 1.0 confidence index scale
        double normalizedConfidence = Math.min(1.0, maxScore * 10.0);
        // Clean round to 2 decimal places
        normalizedConfidence = Math.round(normalizedConfidence * 100.0) / 100.0;

        boolean lowConfidence = normalizedConfidence < confidenceThreshold;
        if (lowConfidence) {
            logger.info("Complaint classification confidence ({}) below threshold ({}). Defaulting to low confidence/Other.",
                    normalizedConfidence, confidenceThreshold);
            return new ClassificationOutput("Other Urban Infrastructure", normalizedConfidence, true);
        }

        return new ClassificationOutput(bestCategory, normalizedConfidence, false);
    }
}
