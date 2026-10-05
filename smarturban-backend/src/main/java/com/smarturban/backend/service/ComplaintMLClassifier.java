package com.smarturban.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ComplaintMLClassifier {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintMLClassifier.class);

    @Value("${smarturban.ai.confidence-threshold:0.35}")
    private double confidenceThreshold;

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

    // Comprehensive Training Corpus for Multinomial Naive Bayes Model
    private static final Map<String, List<String>> TRAINING_CORPUS = new LinkedHashMap<>();

    static {
        TRAINING_CORPUS.put("Road Maintenance", Arrays.asList(
                "pothole on main road asphalt pavement damaged tar street crater broken road surface speed bump caved road edge highway crack maintenance uneven paving bitumen lane avenue drive way street hole",
                "deep pothole on main road causing traffic hazard and tire damage near junction asphalt peeling off completely",
                "broken pavement tiles and large crater in asphalt road surface dangerous for two wheelers and cars",
                "caved-in road surface tar erosion broken divider kerbstone damaged road pavement needs immediate repair patch work",
                "bad road condition huge potholes tar washed away dangerous road stretch asphalt repair required",
                "damaged pavement tiles unpaved road crater in asphalt street hole bitumen cracking"
        ));

        TRAINING_CORPUS.put("Streetlights", Arrays.asList(
                "streetlight not working dark bulb pole lamp flickering electricity power outage street light dark road junction evening night safety glow fixture lamp post led light wire broken fused",
                "multiple streetlights turned off whole street dark at night safety hazard fused bulb high mast light not working",
                "flickering street light pole leaning hanging electrical wire exposed near lamp post dark alleyway no light",
                "led street light broken dark street fixture burnt out electrical junction box open near light pole",
                "no light on street at night streetlight bulb fused dark road junction streetlight pole broken",
                "street light power failure light fixture not glowing dark lane safety issue"
        ));

        TRAINING_CORPUS.put("Sanitation/Garbage", Arrays.asList(
                "garbage trash waste bin clean dump overflow litter rubbish foul smell uncleaned dustbin waste pile stinking debris plastic waste collection sweeping dump yard uncleared garbage dump",
                "overflowing garbage bin on sidewalk trash scattered across street foul smell uncollected municipal waste dump",
                "garbage collector did not sweep street accumulated plastic waste food waste rotting in public bin stinking area",
                "illegal dumping of construction waste debris and domestic trash near residential park needs garbage clearing truck",
                "uncleaned dustbin area pile of trash smelling bad street sweeping garbage vehicle missing",
                "waste dumped on side of road littering plastic bags rotting waste foul odour garbage issue"
        ));

        TRAINING_CORPUS.put("Water Supply", Arrays.asList(
                "water pipe leak tap supply contamination drinking water pipeline burst low pressure dirty water muddy water no water flow municipal water meter leakage valve leak water pipeline broken",
                "main water supply pipe leaking clean drinking water wasted on street low water pressure in residential houses",
                "dirty sewage mixed contaminated drinking water coming out of home tap foul smelling muddy water supply pipeline",
                "no water supply in entire locality pipeline burst near water overhead tank pipe leakage needs immediate plumber attention",
                "drinking water pipeline broken tap water muddy contaminated low water pressure issue",
                "water valve leak supply pipe burst municipal water tank overflow no drinking water available"
        ));

        TRAINING_CORPUS.put("Drainage", Arrays.asList(
                "drain sewer overflow gutter flooding sludge blockage storm drain manhole cover open clogged drain sewage water stagnant water rain water logging blocked drain drain line manhole overflow",
                "clogged storm drain causing street flooding during rain stagnant sewage water overflowing from open manhole cover",
                "blocked sewer line foul water entering houses open gutter overflowing with sludge blockage in main drainage network",
                "broken concrete cover on deep manhole open drain hazard stagnant water breeding mosquitoes in stormwater drain",
                "sewage pipe blocked stormwater drain overflowing water logging in street open gutter blocked",
                "sludge accumulation in drainage line sewage water backflow open manhole cover broken"
        ));

        TRAINING_CORPUS.put("Other Urban Infrastructure", Arrays.asList(
                "park bench public toilet wall illegal hoarding encroachment tree branch fallen public park playground noise pollution stray animal issue public property maintenance general civic complaint",
                "damaged public park fence fallen tree branch blocking pedestrian walkway illegal advertisement board hoarding",
                "encroachment on public footpath illegal vendor booth stray dogs near playground damaged public toilet door",
                "general municipal infrastructure issue damaged sign board public garden maintenance city beautification request",
                "fallen tree on road blocking traffic stray cattle issue public toilet maintenance playground fence broken",
                "illegal banner hoarding on pole public park bench damaged public infrastructure repair request"
        ));
    }

    /**
     * Tokenizes text into unigrams and bigrams
     */
    public List<String> extractFeatures(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String cleaned = text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9\\s]", " ");
        String[] tokens = cleaned.split("\\s+");
        List<String> features = new ArrayList<>();

        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if (token.length() > 2) {
                features.add(token); // Unigram
            }
            if (i < tokens.length - 1 && tokens[i].length() > 2 && tokens[i + 1].length() > 2) {
                features.add(tokens[i] + "_" + tokens[i + 1]); // Bigram
            }
        }
        return features;
    }

    /**
     * Multinomial Naive Bayes Probabilistic Classifier with Softmax Posterior Probabilities
     */
    public ClassificationOutput classifyComplaint(String title, String description) {
        String combinedText = (title != null ? title : "") + " " + (description != null ? description : "");
        List<String> inputFeatures = extractFeatures(combinedText);

        if (inputFeatures.isEmpty()) {
            return new ClassificationOutput("Other Urban Infrastructure", 0.0, true);
        }

        // Build Global Vocabulary and Calculate Category Feature Frequencies
        Set<String> vocabulary = new HashSet<>(inputFeatures);
        Map<String, Map<String, Integer>> categoryFeatureFreq = new HashMap<>();
        Map<String, Integer> categoryTotalTokens = new HashMap<>();

        for (Map.Entry<String, List<String>> entry : TRAINING_CORPUS.entrySet()) {
            String category = entry.getKey();
            Map<String, Integer> featureFreq = new HashMap<>();
            int totalTokens = 0;

            for (String doc : entry.getValue()) {
                List<String> docFeatures = extractFeatures(doc);
                for (String feat : docFeatures) {
                    vocabulary.add(feat);
                    featureFreq.put(feat, featureFreq.getOrDefault(feat, 0) + 1);
                    totalTokens++;
                }
            }
            categoryFeatureFreq.put(category, featureFreq);
            categoryTotalTokens.put(category, totalTokens);
        }

        int vocabSize = vocabulary.size();
        double laplaceAlpha = 1.0;

        // Calculate Log Posterior Probabilities: log P(C) + sum f_i * log P(w_i | C)
        Map<String, Double> logLikelihoods = new HashMap<>();
        double priorLogProb = Math.log(1.0 / TRAINING_CORPUS.size()); // Uniform prior across categories

        for (String category : TRAINING_CORPUS.keySet()) {
            double logProb = priorLogProb;
            Map<String, Integer> featureFreq = categoryFeatureFreq.get(category);
            int totalTokens = categoryTotalTokens.get(category);

            for (String feat : inputFeatures) {
                int count = featureFreq.getOrDefault(feat, 0);
                // Laplace Smoothing Probability
                double wordProb = (count + laplaceAlpha) / (totalTokens + laplaceAlpha * vocabSize);
                logProb += Math.log(wordProb);
            }
            logLikelihoods.put(category, logProb);
        }

        // Softmax Normalization over Log Likelihoods to derive exact posterior probabilities
        double maxLog = Collections.max(logLikelihoods.values());
        double expSum = 0.0;
        Map<String, Double> posteriorProbabilities = new HashMap<>();

        for (Map.Entry<String, Double> entry : logLikelihoods.entrySet()) {
            double expVal = Math.exp(entry.getValue() - maxLog);
            posteriorProbabilities.put(entry.getKey(), expVal);
            expSum += expVal;
        }

        String bestCategory = "Other Urban Infrastructure";
        double maxConfidence = 0.0;

        for (Map.Entry<String, Double> entry : posteriorProbabilities.entrySet()) {
            double normProb = entry.getValue() / expSum;
            if (normProb > maxConfidence) {
                maxConfidence = normProb;
                bestCategory = entry.getKey();
            }
        }

        maxConfidence = Math.round(maxConfidence * 100.0) / 100.0;
        boolean lowConfidence = maxConfidence < confidenceThreshold;

        if (lowConfidence) {
            logger.info("Multinomial Naive Bayes confidence ({}) below threshold ({}). Assigning 'Other Urban Infrastructure'.",
                    maxConfidence, confidenceThreshold);
            return new ClassificationOutput("Other Urban Infrastructure", maxConfidence, true);
        }

        return new ClassificationOutput(bestCategory, maxConfidence, false);
    }
}
