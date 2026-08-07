package com.carebeforecare.prehospitalcare_platform.config;

import com.carebeforecare.prehospitalcare_platform.model.entity.*;
import com.carebeforecare.prehospitalcare_platform.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final SymptomRepository symptomRepository;
    private final FirstAidGuideRepository firstAidGuideRepository;
    private final MedicationRepository medicationRepository;

    public DataInitializer(SymptomRepository symptomRepository,
                          FirstAidGuideRepository firstAidGuideRepository,
                          MedicationRepository medicationRepository) {
        this.symptomRepository = symptomRepository;
        this.firstAidGuideRepository = firstAidGuideRepository;
        this.medicationRepository = medicationRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Clear existing data
        symptomRepository.deleteAll();
        firstAidGuideRepository.deleteAll();
        medicationRepository.deleteAll();

        // Insert symptoms
        initializeSymptoms();

        // Insert first aid guides
        initializeFirstAidGuides();

        // Insert medications
        initializeMedications();

        System.out.println("✅ Sample data initialized successfully!");
        System.out.println("📊 Statistics: " + symptomRepository.count() + " Symptoms, " +
                          firstAidGuideRepository.count() + " First Aid Guides, " +
                          medicationRepository.count() + " Medications");
    }

    private void initializeSymptoms() {
        List<Symptom> symptoms = Arrays.asList(
            createSymptom("Headache", "Neurological", "Mild",
                "Pain or discomfort in the head or upper neck",
                "Rest in a quiet room, Apply cold compress, Stay hydrated",
                "Severe pain, Vision changes, Fever with stiff neck",
                "If headache is severe or accompanied by other neurological symptoms"),

            createSymptom("Fever", "General", "Moderate",
                "Elevated body temperature above normal range",
                "Rest, Stay hydrated, Use fever reducers, Cool compress",
                "High fever (104°F+), Rash, Difficulty breathing, Seizures",
                "If fever is above 103°F or lasts more than 3 days"),

            createSymptom("Chest Pain", "Cardiovascular", "High",
                "Sharp, burning, or squeezing pain in chest area",
                "Sit down and rest, Loosen tight clothing, Call emergency services",
                "Pain radiating to arm/jaw, Shortness of breath, Nausea, Sweating",
                "Seek immediate medical attention for any chest pain"),

            createSymptom("Shortness of Breath", "Respiratory", "High",
                "Difficulty breathing or feeling of suffocation",
                "Sit upright, Loosen clothing, Use inhaler if available",
                "Blue lips/fingernails, Chest pain, Confusion, Rapid breathing",
                "Emergency care needed for sudden or severe breathing difficulty"),

            createSymptom("Abdominal Pain", "Gastrointestinal", "Moderate",
                "Pain or discomfort in stomach area",
                "Rest, Sip clear fluids, Avoid solid foods",
                "Severe pain, Fever, Vomiting blood, Black stools",
                "If pain is severe, persistent, or accompanied by fever"),

            createSymptom("Dizziness", "Neurological", "Mild",
                "Feeling lightheaded, unsteady, or spinning sensation",
                "Sit or lie down immediately, Hydrate, Move slowly",
                "Fainting, Chest pain, Irregular heartbeat, Severe headache",
                "If recurrent or accompanied by other concerning symptoms"),

            createSymptom("Nausea", "Gastrointestinal", "Mild",
                "Feeling of sickness with urge to vomit",
                "Sip clear fluids, Eat bland foods, Rest in comfortable position",
                "Severe vomiting, Blood in vomit, Abdominal pain, Dehydration",
                "If persistent or preventing fluid intake"),

            createSymptom("Cough", "Respiratory", "Mild",
                "Sudden expulsion of air from lungs",
                "Stay hydrated, Use cough drops, Humidify air",
                "Coughing blood, Shortness of breath, Chest pain, Fever",
                "If cough persists more than 3 weeks or is severe"),

            createSymptom("Fatigue", "General", "Mild",
                "Persistent feeling of tiredness or weakness",
                "Ensure adequate sleep, Balanced diet, Regular exercise",
                "Unexplained weight loss, Fever, Night sweats, Severe weakness",
                "If fatigue is severe, persistent, or affecting daily life"),

            createSymptom("Back Pain", "Musculoskeletal", "Moderate",
                "Pain in upper, middle, or lower back region",
                "Rest, Apply ice/heat, Gentle stretching, Over-the-counter pain relief",
                "Leg weakness, Loss of bladder control, Fever, Trauma history",
                "If accompanied by neurological symptoms or severe pain"),

            createSymptom("Sore Throat", "ENT", "Mild",
                "Pain, scratchiness or irritation of the throat",
                "Warm salt water gargle, Stay hydrated, Lozenges",
                "Difficulty breathing, Difficulty swallowing, High fever, Rash",
                "If severe, persistent, or accompanied by fever"),

            createSymptom("Rash", "Dermatological", "Mild",
                "Change in skin color or texture",
                "Keep area clean and dry, Avoid scratching, Cool compress",
                "Rapid spreading, Fever, Difficulty breathing, Blisters",
                "If widespread, painful, or accompanied by systemic symptoms"),

            createSymptom("Joint Pain", "Musculoskeletal", "Moderate",
                "Discomfort, inflammation in joints",
                "Rest affected joint, Ice application, Over-the-counter anti-inflammatories",
                "Severe swelling, Redness, Fever, Inability to move joint",
                "If severe, persistent, or affecting mobility"),

            createSymptom("Eye Pain", "Ophthalmological", "Moderate",
                "Discomfort in or around the eye",
                "Rest eyes, Avoid bright lights, Use artificial tears",
                "Vision loss, Eye trauma, Chemical exposure, Severe pain",
                "Immediate care for vision changes or severe pain"),

            createSymptom("Ear Pain", "ENT", "Mild",
                "Pain or discomfort in the ear",
                "Over-the-counter pain relievers, Warm compress",
                "Fever, Hearing loss, Drainage from ear, Severe pain",
                "If accompanied by fever or hearing changes"),

            createSymptom("Nasal Congestion", "Respiratory", "Mild",
                "Blocked or stuffy nose",
                "Saline nasal spray, Steam inhalation, Hydration",
                "Difficulty breathing, High fever, Severe headache",
                "If affecting breathing or accompanied by high fever"),

            createSymptom("Heart Palpitations", "Cardiovascular", "Moderate",
                "Awareness of heartbeat - pounding, fluttering",
                "Sit down, Deep breathing, Avoid stimulants",
                "Chest pain, Fainting, Shortness of breath, Rapid heartbeat",
                "If accompanied by chest pain or fainting"),

            createSymptom("Swelling", "General", "Mild",
                "Enlargement of body parts from fluid accumulation",
                "Elevate affected area, Compression, Rest",
                "Sudden swelling, Difficulty breathing, Pain, Redness",
                "If sudden, severe, or affecting breathing"),

            createSymptom("Bruising", "Hematological", "Mild",
                "Skin discoloration from blood vessel damage",
                "Ice pack, Elevation, Rest affected area",
                "Unexplained bruising, Frequent bruising, Bleeding problems",
                "If unexplained or excessive bruising occurs"),

            createSymptom("Muscle Pain", "Musculoskeletal", "Mild",
                "Pain or discomfort in muscles",
                "Rest, Stretching, Over-the-counter pain relievers, Heat therapy",
                "Severe pain, Swelling, Fever, Dark urine",
                "If severe, persistent, or accompanied by systemic symptoms"),

            createSymptom("Insomnia", "Neurological", "Mild",
                "Difficulty falling or staying asleep",
                "Establish routine, Limit screen time, Relaxation techniques",
                "Daytime fatigue, Mood changes, Poor concentration",
                "If chronic or affecting daily functioning"),

            createSymptom("Anxiety", "Psychological", "Moderate",
                "Feelings of worry, nervousness, or unease",
                "Deep breathing, Meditation, Physical activity, Limit caffeine",
                "Panic attacks, Chest pain, Difficulty breathing, Severe distress",
                "If severe, persistent, or affecting daily life"),

            createSymptom("Depression", "Psychological", "Moderate",
                "Persistent feelings of sadness and loss of interest",
                "Regular exercise, Social connection, Professional support",
                "Suicidal thoughts, Severe functional impairment, Weight changes",
                "Immediate help for suicidal thoughts or severe symptoms"),

            createSymptom("Memory Loss", "Neurological", "Moderate",
                "Difficulty remembering information or events",
                "Mental exercises, Organized routines, Adequate sleep",
                "Rapid progression, Personality changes, Disorientation",
                "If sudden, worsening, or affecting safety"),

            createSymptom("Weight Loss", "General", "Moderate",
                "Unintentional reduction in body weight",
                "Balanced nutrition, Regular meals, Medical evaluation",
                "Rapid weight loss, Loss of appetite, Fatigue, Fever",
                "If unexplained or excessive weight loss occurs"),

            createSymptom("Constipation", "Gastrointestinal", "Mild",
                "Infrequent bowel movements or difficulty passing stools",
                "Increase fiber intake, Hydration, Physical activity",
                "Severe pain, Vomiting, Blood in stool, No bowel movements",
                "If severe, persistent, or accompanied by pain"),

            createSymptom("Diarrhea", "Gastrointestinal", "Moderate",
                "Loose, watery stools occurring frequently",
                "Hydration, BRAT diet, Rest, Avoid dairy",
                "Severe dehydration, Blood in stool, High fever, Persistent vomiting",
                "If severe, bloody, or with dehydration signs"),

            createSymptom("Blurred Vision", "Ophthalmological", "Moderate",
                "Lack of sharpness in vision",
                "Rest eyes, Proper lighting, Eye exams",
                "Sudden vision loss, Eye pain, Headache, Double vision",
                "Immediate care for sudden vision changes"),

            createSymptom("Tinnitus", "ENT", "Mild",
                "Ringing or buzzing in ears",
                "Background noise, Stress management, Hearing protection",
                "Sudden onset, Hearing loss, Dizziness, Pain",
                "If sudden, unilateral, or accompanied by hearing loss"),

            createSymptom("Loss of Appetite", "General", "Mild",
                "Reduced desire to eat",
                "Small frequent meals, Pleasant eating environment",
                "Rapid weight loss, Fever, Pain, Persistent nausea",
                "If prolonged or accompanied by weight loss"),

            createSymptom("Excessive Thirst", "Endocrine", "Moderate",
                "Unusual persistent thirst",
                "Hydrate with water, Monitor urine output",
                "Frequent urination, Fatigue, Weight loss, Blurred vision",
                "If persistent or accompanied by other diabetes symptoms"),

            createSymptom("Frequent Urination", "Urological", "Moderate",
                "Need to urinate more often than usual",
                "Monitor fluid intake, Limit caffeine and alcohol",
                "Painful urination, Blood in urine, Fever, Incontinence",
                "If accompanied by pain, fever, or other symptoms"),

            createSymptom("Night Sweats", "General", "Moderate",
                "Excessive sweating during sleep",
                "Light bedding, Cool room, Moisture-wicking sleepwear",
                "Fever, Weight loss, Fatigue, Chills",
                "If persistent or accompanied by other symptoms"),

            createSymptom("Chills", "General", "Mild",
                "Feeling of coldness with shivering",
                "Warm clothing, Warm fluids, Rest",
                "High fever, Severe shaking, Confusion, Difficulty breathing",
                "If severe or with high fever"),

            createSymptom("Numbness", "Neurological", "Moderate",
                "Loss of sensation or tingling",
                "Change position, Gentle movement, Warm compress",
                "Sudden onset, Weakness, Speech difficulties, Vision changes",
                "Immediate care for sudden numbness or weakness"),

            createSymptom("Tremors", "Neurological", "Moderate",
                "Involuntary shaking movements",
                "Rest, Stress reduction, Avoid stimulants",
                "Worsening tremors, Difficulty with tasks, Other neurological symptoms",
                "If interfering with daily activities"),

            createSymptom("Hair Loss", "Dermatological", "Mild",
                "Excessive shedding or thinning of hair",
                "Gentle hair care, Balanced nutrition, Stress management",
                "Rapid hair loss, Bald patches, Scalp irritation, Other symptoms",
                "If sudden or accompanied by other health issues"),

            createSymptom("Dry Mouth", "General", "Mild",
                "Insufficient saliva production",
                "Frequent sips of water, Sugar-free gum, Humidifier",
                "Difficulty swallowing, Speaking problems, Dental issues",
                "If persistent or affecting eating/speaking"),

            createSymptom("Bad Breath", "Oral", "Mild",
                "Unpleasant odor from mouth",
                "Good oral hygiene, Hydration, Regular dental care",
                "Persistent despite hygiene, Pain, Swelling, Other symptoms",
                "If persistent or accompanied by other symptoms"),

            createSymptom("Snoring", "Respiratory", "Mild",
                "Noisy breathing during sleep",
                "Sleep position change, Weight management, Avoid alcohol",
                "Gasping sounds, Daytime sleepiness, Breathing pauses",
                "If accompanied by sleep apnea symptoms"),

            createSymptom("Hiccups", "General", "Mild",
                "Involuntary diaphragm contractions",
                "Hold breath, Sip cold water, Breathe into paper bag",
                "Persistent hiccups (over 48 hours), Pain, Difficulty eating",
                "If persistent or accompanied by pain"),

            createSymptom("Gas", "Gastrointestinal", "Mild",
                "Excess air in digestive tract",
                "Diet modification, Slow eating, Physical activity",
                "Severe pain, Weight loss, Blood in stool, Vomiting",
                "If severe or accompanied by other symptoms"),

            createSymptom("Heartburn", "Gastrointestinal", "Mild",
                "Burning sensation in chest",
                "Avoid trigger foods, Smaller meals, Don't lie down after eating",
                "Severe pain, Difficulty swallowing, Weight loss, Vomiting",
                "If frequent, severe, or accompanied by alarming symptoms"),

            createSymptom("Motion Sickness", "Neurological", "Mild",
                "Nausea from motion",
                "Look at horizon, Fresh air, Avoid reading while moving",
                "Severe vomiting, Dehydration, Inability to function",
                "If severe or preventing necessary travel"),

            createSymptom("Sunburn", "Dermatological", "Moderate",
                "Skin damage from UV exposure",
                "Cool compresses, Aloe vera, Hydration, Pain relievers",
                "Severe pain, Blisters, Fever, Chills, Nausea",
                "If severe, widespread, or with systemic symptoms"),

            createSymptom("Dehydration", "General", "Moderate",
                "Insufficient body fluids",
                "Oral rehydration solutions, Water, Rest in cool place",
                "Extreme thirst, No urination, Dizziness, Confusion",
                "Emergency care for severe dehydration signs"),

            createSymptom("Allergic Reaction", "Immunological", "High",
                "Immune response to allergen",
                "Avoid allergen, Antihistamines, Cool compress",
                "Difficulty breathing, Swelling of face/throat, Hives, Dizziness",
                "Emergency care for breathing difficulties or severe reactions"),

            createSymptom("Stress", "Psychological", "Mild",
                "Mental or emotional strain",
                "Relaxation techniques, Exercise, Time management, Social support",
                "Severe anxiety, Depression, Physical symptoms, Functional impairment",
                "If severe or affecting daily functioning")
        );

        symptomRepository.saveAll(symptoms);
    }

    private void initializeFirstAidGuides() {
        List<FirstAidGuide> guides = Arrays.asList(
            createFirstAidGuide("CPR - Adult", "Cardiopulmonary resuscitation for adults",
                "1. Check responsiveness\n2. Call emergency services\n3. Open airway\n4. Check breathing\n5. Begin chest compressions\n6. Give rescue breaths\n7. Continue cycles until help arrives",
                "Emergency", "Critical", "Immediate", "Advanced", "Continuous", "None", "heart"),

            createFirstAidGuide("Choking - Adult", "First aid for choking conscious adult",
                "1. Ask 'Are you choking?'\n2. Stand behind person\n3. Make fist above navel\n4. Perform abdominal thrusts\n5. Continue until object is expelled\n6. Seek medical attention after",
                "Emergency", "High", "Immediate", "Basic", "5-10 minutes", "None", "alert"),

            createFirstAidGuide("Severe Bleeding", "Control of severe external bleeding",
                "1. Wear gloves if available\n2. Apply direct pressure\n3. Elevate injured area\n4. Apply bandage\n5. Don't remove soaked bandages\n6. Seek medical help",
                "Injury", "High", "Immediate", "Basic", "10-15 minutes", "Gloves, Bandages", "droplet"),

            createFirstAidGuide("Burn Treatment", "First aid for thermal burns",
                "1. Cool burn with running water\n2. Remove jewelry/clothing\n3. Cover with sterile dressing\n4. Don't apply ice\n5. Don't break blisters\n6. Pain relief if needed",
                "Injury", "Moderate", "Urgent", "Basic", "20 minutes", "Cool water, Sterile dressing", "thermometer"),

            createFirstAidGuide("Fracture Management", "First aid for suspected fractures",
                "1. Keep injured area still\n2. Support above and below injury\n3. Apply ice pack\n4. Check circulation\n5. Don't try to realign\n6. Seek medical attention",
                "Injury", "High", "Urgent", "Intermediate", "15 minutes", "Splint, Bandages, Ice", "bone"),

            createFirstAidGuide("Sprain Care", "Treatment for joint sprains",
                "1. Rest injured area\n2. Ice application\n3. Compression bandage\n4. Elevation\n5. Pain relief\n6. Gradual return to activity",
                "Injury", "Moderate", "Standard", "Basic", "20 minutes", "Ice pack, Bandage", "activity"),

            createFirstAidGuide("Heat Stroke", "Emergency cooling for heat stroke",
                "1. Move to cool place\n2. Remove excess clothing\n3. Cool with water/wet cloths\n4. Fan the person\n5. Monitor consciousness\n6. Seek emergency care",
                "Environmental", "Critical", "Immediate", "Basic", "15-30 minutes", "Water, Fan, Thermometer", "sun"),

            createFirstAidGuide("Hypothermia", "Warming for hypothermia",
                "1. Move to warm place\n2. Remove wet clothing\n3. Warm center of body first\n4. Warm beverages if conscious\n5. Don't rub extremities\n6. Seek medical help",
                "Environmental", "High", "Urgent", "Basic", "30+ minutes", "Blankets, Warm fluids", "snowflake"),

            createFirstAidGuide("Seizure First Aid", "Care during and after seizures",
                "1. Clear area of hazards\n2. Don't restrain person\n3. Place on side after seizure\n4. Don't put anything in mouth\n5. Time the seizure\n6. Stay until fully alert",
                "Neurological", "High", "Immediate", "Basic", "Varies", "Cushion, Timer", "brain"),

            createFirstAidGuide("Stroke Recognition", "FAST assessment for stroke",
                "1. F - Face drooping\n2. A - Arm weakness\n3. S - Speech difficulty\n4. T - Time to call emergency\n5. Note time of onset\n6. Don't give food/drink",
                "Neurological", "Critical", "Immediate", "Basic", "5 minutes", "None", "clock"),

            createFirstAidGuide("Allergic Reaction", "Treatment for severe allergic reaction",
                "1. Check for epinephrine auto-injector\n2. Administer if available\n3. Call emergency services\n4. Help person use inhaler if needed\n5. Monitor breathing\n6. Loosen tight clothing",
                "Allergy", "Critical", "Immediate", "Intermediate", "10 minutes", "Epinephrine, Antihistamines", "allergy"),

            createFirstAidGuide("Asthma Attack", "Assistance during asthma attack",
                "1. Help person sit upright\n2. Assist with inhaler\n3. Loosen tight clothing\n4. Stay calm and reassuring\n5. Monitor breathing\n6. Call emergency if no improvement",
                "Respiratory", "High", "Immediate", "Basic", "15 minutes", "Inhaler, Spacer", "lungs"),

            createFirstAidGuide("Diabetic Emergency", "Treatment for hypoglycemia",
                "1. Check if conscious\n2. If conscious, give sugar\n3. If unconscious, don't give oral\n4. Administer glucagon if trained\n5. Monitor response\n6. Seek medical attention",
                "Metabolic", "High", "Immediate", "Intermediate", "15 minutes", "Sugar, Glucagon", "activity"),

            createFirstAidGuide("Heart Attack", "First aid for suspected heart attack",
                "1. Help person rest comfortably\n2. Call emergency services\n3. Give aspirin if recommended\n4. Loosen tight clothing\n5. Monitor breathing\n6. Be prepared for CPR",
                "Cardiac", "Critical", "Immediate", "Basic", "Continuous", "Aspirin", "heart"),

            createFirstAidGuide("Poisoning", "First aid for poisoning cases",
                "1. Check scene safety\n2. Call poison control\n3. Don't induce vomiting\n4. Identify poison if possible\n5. Follow instructions\n6. Save container for identification",
                "Toxicology", "High", "Immediate", "Intermediate", "Varies", "Poison control number", "skull"),

            createFirstAidGuide("Eye Injury", "First aid for eye trauma",
                "1. Don't rub eye\n2. Flush with clean water\n3. Cover both eyes\n4. Don't try to remove object\n5. Seek immediate care\n6. Protect from further injury",
                "Ophthalmological", "High", "Urgent", "Basic", "10 minutes", "Eye wash, Eye shield", "eye"),

            createFirstAidGuide("Nosebleed", "Control of epistaxis",
                "1. Sit upright, lean forward\n2. Pinch soft part of nose\n3. Breathe through mouth\n4. Maintain pressure 10-15 min\n5. Apply ice pack\n6. Seek help if bleeding continues",
                "ENT", "Mild", "Standard", "Basic", "15 minutes", "Tissues, Ice pack", "droplet"),

            createFirstAidGuide("Tooth Injury", "First aid for dental emergencies",
                "1. Save tooth/parts\n2. Handle by crown only\n3. Rinse with milk/saline\n4. Control bleeding\n5. See dentist immediately\n6. Use cold compress for pain",
                "Dental", "Moderate", "Urgent", "Basic", "15 minutes", "Milk, Gauze", "tooth"),

            createFirstAidGuide("Animal Bite", "First aid for animal bites",
                "1. Control bleeding\n2. Clean with soap and water\n3. Apply antibiotic ointment\n4. Cover with clean dressing\n5. Seek medical attention\n6. Report to animal control",
                "Injury", "Moderate", "Urgent", "Basic", "20 minutes", "Soap, Antibiotic ointment", "alert"),

            createFirstAidGuide("Insect Sting", "Treatment for insect stings",
                "1. Remove stinger if present\n2. Clean area\n3. Apply cold compress\n4. Watch for allergic reaction\n5. Use antihistamine if needed\n6. Seek help for severe reaction",
                "Allergy", "Mild", "Standard", "Basic", "15 minutes", "Cold pack, Antihistamine", "bug"),

            createFirstAidGuide("Jellyfish Sting", "First aid for jellyfish stings",
                "1. Rinse with vinegar\n2. Remove tentacles\n3. Apply hot water\n4. Don't rub area\n5. Pain relief if needed\n6. Watch for allergic reaction",
                "Marine", "Moderate", "Standard", "Basic", "20 minutes", "Vinegar, Hot water", "water"),

            createFirstAidGuide("Snake Bite", "First aid for venomous snake bites",
                "1. Keep person calm\n2. Keep bite below heart level\n3. Remove jewelry\n4. Don't cut bite or use tourniquet\n5. Seek immediate medical help\n6. Note snake appearance",
                "Toxicology", "Critical", "Immediate", "Advanced", "Continuous", "Bandage, Splint", "skull"),

            createFirstAidGuide("Spider Bite", "First aid for spider bites",
                "1. Clean with soap and water\n2. Apply cold compress\n3. Elevate if possible\n4. Watch for severe reaction\n5. Seek medical attention\n6. Save spider if possible",
                "Toxicology", "Moderate", "Urgent", "Basic", "15 minutes", "Soap, Cold pack", "bug"),

            createFirstAidGuide("Chemical Burn", "First aid for chemical exposure",
                "1. Remove contaminated clothing\n2. Brush off dry chemicals\n3. Flush with running water\n4. Continue flushing 15-20 minutes\n5. Cover loosely\n6. Seek medical help",
                "Toxicology", "High", "Immediate", "Intermediate", "20+ minutes", "Water source, Gloves", "flask"),

            createFirstAidGuide("Electrical Injury", "First aid for electrical shocks",
                "1. Ensure scene is safe\n2. Don't touch person if connected\n3. Check breathing\n4. Treat for shock\n5. Check for entry/exit wounds\n6. Seek medical attention",
                "Environmental", "Critical", "Immediate", "Advanced", "15 minutes", "None", "zap"),

            createFirstAidGuide("Drowning Rescue", "First aid for drowning victims",
                "1. Ensure your safety first\n2. Remove from water\n3. Check breathing\n4. Begin CPR if needed\n5. Treat for hypothermia\n6. Seek emergency care",
                "Emergency", "Critical", "Immediate", "Advanced", "Continuous", "Rescue equipment", "water"),

            createFirstAidGuide("Head Injury", "First aid for head trauma",
                "1. Keep person still\n2. Control bleeding\n3. Watch for consciousness changes\n4. Don't remove helmet if present\n5. Monitor for concussion signs\n6. Seek medical evaluation",
                "Neurological", "High", "Urgent", "Intermediate", "30 minutes", "Ice pack, Bandages", "brain"),

            createFirstAidGuide("Neck Injury", "First aid for suspected neck injury",
                "1. Don't move person\n2. Stabilize head and neck\n3. Call emergency services\n4. Monitor breathing\n5. Keep person warm\n6. Wait for professional help",
                "Neurological", "Critical", "Immediate", "Advanced", "Continuous", "Blankets", "alert"),

            createFirstAidGuide("Shock Treatment", "First aid for physiological shock",
                "1. Lay person down\n2. Elevate legs\n3. Keep warm\n4. Loosen tight clothing\n5. Don't give food/drink\n6. Monitor until help arrives",
                "Emergency", "High", "Immediate", "Basic", "Continuous", "Blankets", "activity"),

            createFirstAidGuide("Fainting", "First aid for syncope",
                "1. Lay person flat\n2. Elevate legs\n3. Loosen tight clothing\n4. Ensure fresh air\n5. Don't crowd around\n6. Seek help if doesn't recover quickly",
                "Cardiac", "Moderate", "Standard", "Basic", "10 minutes", "None", "activity"),

            createFirstAidGuide("Heat Exhaustion", "Cooling for heat exhaustion",
                "1. Move to cool place\n2. Lie down and elevate legs\n3. Remove excess clothing\n4. Cool with wet cloths\n5. Hydrate with electrolytes\n6. Seek help if no improvement",
                "Environmental", "Moderate", "Urgent", "Basic", "30 minutes", "Water, Electrolytes", "sun"),

            createFirstAidGuide("Frostbite", "First aid for frostbite",
                "1. Move to warm area\n2. Don't rub affected area\n3. Warm in lukewarm water\n4. Don't walk on frostbitten feet\n5. Protect from refreezing\n6. Seek medical attention",
                "Environmental", "High", "Urgent", "Intermediate", "30+ minutes", "Warm water, Blankets", "snowflake"),

            createFirstAidGuide("Motion Sickness", "Relief for motion sickness",
                "1. Look at horizon\n2. Get fresh air\n3. Avoid reading\n4. Eat light meals before travel\n5. Use acupressure bands\n6. Medication if prescribed",
                "Neurological", "Mild", "Standard", "Basic", "Varies", "Acupressure bands", "car"),

            createFirstAidGuide("Anaphylaxis", "Emergency treatment for anaphylaxis",
                "1. Use epinephrine auto-injector\n2. Call emergency services\n3. Lay person flat\n4. Monitor breathing\n5. Administer second dose if needed\n6. Don't stand or walk",
                "Allergy", "Critical", "Immediate", "Intermediate", "10 minutes", "Epinephrine", "allergy"),

            createFirstAidGuide("Concussion", "Management of head injury",
                "1. Rest physically and mentally\n2. Monitor for worsening symptoms\n3. Avoid screens and bright lights\n4. Gradual return to activity\n5. Follow medical advice\n6. No sports until cleared",
                "Neurological", "Moderate", "Urgent", "Basic", "Days to weeks", "Ice pack", "brain"),

            createFirstAidGuide("Dislocation", "First aid for joint dislocation",
                "1. Don't try to relocate\n2. Immobilize joint\n3. Apply ice pack\n4. Check circulation\n5. Seek immediate care\n6. Pain management",
                "Musculoskeletal", "High", "Urgent", "Intermediate", "20 minutes", "Splint, Ice", "bone"),

            createFirstAidGuide("Spinal Injury", "First aid for suspected spinal injury",
                "1. Don't move person\n2. Stabilize head and neck\n3. Call emergency services\n4. Monitor breathing\n5. Keep person still\n6. Wait for professional help",
                "Neurological", "Critical", "Immediate", "Advanced", "Continuous", "Blankets", "alert"),

            createFirstAidGuide("Chest Injury", "First aid for chest trauma",
                "1. Help person find comfortable position\n2. Don't remove impaled objects\n3. Seal sucking chest wounds\n4. Monitor breathing\n5. Treat for shock\n6. Seek emergency care",
                "Trauma", "Critical", "Immediate", "Advanced", "Continuous", "Occlusive dressing", "heart"),

            createFirstAidGuide("Abdominal Injury", "First aid for abdominal trauma",
                "1. Lay person flat with knees bent\n2. Don't give food or drink\n3. Cover protruding organs with moist dressing\n4. Treat for shock\n5. Monitor closely\n6. Seek emergency care",
                "Trauma", "High", "Immediate", "Intermediate", "Continuous", "Moist dressing", "activity"),

            createFirstAidGuide("Amputation", "First aid for traumatic amputation",
                "1. Control bleeding with direct pressure\n2. Treat for shock\n3. Save amputated part\n4. Wrap in clean moist cloth\n5. Place in plastic bag on ice\n6. Seek immediate surgical care",
                "Trauma", "Critical", "Immediate", "Advanced", "Continuous", "Clean cloth, Ice", "alert"),

            createFirstAidGuide("Eye Chemical Burn", "First aid for chemical eye exposure",
                "1. Flush with clean water 15-20 minutes\n2. Hold eyelid open\n3. Continue during transport\n4. Don't use eye drops\n5. Cover both eyes\n6. Seek immediate care",
                "Ophthalmological", "High", "Immediate", "Intermediate", "20+ minutes", "Water source", "eye"),

            createFirstAidGuide("Foreign Body Eye", "Removing objects from eye",
                "1. Wash hands first\n2. Don't rub eye\n3. Pull upper lid over lower lid\n4. Use clean water to flush\n5. Don't use tweezers\n6. Seek help if unable to remove",
                "Ophthalmological", "Mild", "Standard", "Basic", "10 minutes", "Water, Mirror", "eye"),

            createFirstAidGuide("Foreign Body Ear", "Removing objects from ear",
                "1. Don't insert objects in ear\n2. Try gravity - tilt head\n3. Use oil for insects\n4. Don't use for other objects\n5. Seek medical help if stuck\n6. Don't attempt removal if painful",
                "ENT", "Mild", "Standard", "Basic", "15 minutes", "Mineral oil", "ear"),

            createFirstAidGuide("Foreign Body Nose", "Removing objects from nose",
                "1. Have person blow gently\n2. Don't insert objects\n3. Close opposite nostril and blow\n4. Seek help if unable to remove\n5. Don't use tweezers\n6. Watch for breathing difficulty",
                "ENT", "Mild", "Standard", "Basic", "10 minutes", "Tissue", "nose"),

            createFirstAidGuide("Bee Sting", "Treatment for bee stings",
                "1. Remove stinger by scraping\n2. Don't use tweezers\n3. Wash area with soap\n4. Apply cold compress\n5. Watch for allergic reaction\n6. Use antihistamine for itching",
                "Allergy", "Mild", "Standard", "Basic", "15 minutes", "Cold pack, Antihistamine", "bug"),

            createFirstAidGuide("Tick Removal", "Safe tick removal",
                "1. Use fine-tipped tweezers\n2. Grasp close to skin\n3. Pull upward steadily\n4. Don't twist or jerk\n5. Clean area thoroughly\n6. Save tick for identification",
                "Dermatological", "Mild", "Standard", "Basic", "10 minutes", "Tweezers, Antiseptic", "bug"),

            createFirstAidGuide("Blister Care", "Treatment for blisters",
                "1. Don't pop blister\n2. Clean area gently\n3. Cover with bandage\n4. Use moleskin for friction\n5. Change dressing daily\n6. Seek help if infected",
                "Dermatological", "Mild", "Standard", "Basic", "5 minutes", "Bandage, Moleskin", "activity"),

            createFirstAidGuide("Splinter Removal", "Removing embedded splinters",
                "1. Clean area with soap\n2. Use sterilized needle\n3. Lift splinter out\n4. Use tweezers if protruding\n5. Clean area again\n6. Apply antibiotic ointment",
                "Dermatological", "Mild", "Standard", "Basic", "10 minutes", "Needle, Tweezers, Antiseptic", "activity"),

            createFirstAidGuide("Sunburn Relief", "Treatment for sunburn",
                "1. Cool compresses or baths\n2. Moisturize with aloe vera\n3. Stay hydrated\n4. Use pain relievers if needed\n5. Don't break blisters\n6. Protect from further sun",
                "Dermatological", "Mild", "Standard", "Basic", "Multiple applications", "Aloe vera, Pain relievers", "sun"),

            createFirstAidGuide("Poison Ivy", "Treatment for plant dermatitis",
                "1. Wash skin immediately\n2. Use calamine lotion\n3. Cool compresses\n4. Oral antihistamines for itching\n5. Don't scratch\n6. Seek help for severe reaction",
                "Dermatological", "Mild", "Standard", "Basic", "Multiple days", "Calamine, Antihistamines", "leaf")
        );

        firstAidGuideRepository.saveAll(guides);
    }

    private void initializeMedications() {
        List<Medication> medications = Arrays.asList(
            createMedication("Paracetamol", "Acetaminophen", "Analgesic",
                "Tablets, Caplets, Liquid, Suppositories", false,
                "Pain relief, Fever reduction",
                "Take 500-1000mg every 4-6 hours as needed for pain or fever",
                "Liver damage with overdose, Rare allergic reactions",
                "Do not exceed 4000mg per day, Avoid with alcohol, Consult doctor for liver disease",
                "Store at room temperature away from moisture"),

            createMedication("Ibuprofen", "Ibuprofen", "NSAID",
                "Tablets, Capsules, Liquid, Gel", false,
                "Pain, Inflammation, Fever",
                "Take 200-400mg every 4-6 hours with food or milk",
                "Stomach pain, Heartburn, Dizziness, Increased bleeding risk",
                "Take with food, Avoid if pregnant, Caution with kidney disease",
                "Room temperature in dry place"),

            createMedication("Aspirin", "Acetylsalicylic acid", "NSAID",
                "Tablets, Chewable tablets, Suppositories", false,
                "Pain relief, Fever reduction, Heart attack prevention",
                "Take with food or full glass of water as directed",
                "Stomach upset, Bleeding risk, Ringing in ears, Allergic reactions",
                "Avoid if allergic, Don't give to children with viral infections",
                "Store in cool dry place"),

            createMedication("Amoxicillin", "Amoxicillin", "Antibiotic",
                "Capsules, Tablets, Liquid", true,
                "Bacterial infections",
                "Take exactly as prescribed, usually every 8-12 hours",
                "Diarrhea, Nausea, Rash, Allergic reactions",
                "Complete full course, Take with food if stomach upset occurs",
                "Refrigerate liquid form"),

            createMedication("Loratadine", "Loratadine", "Antihistamine",
                "Tablets, Syrup, Rapidly-disintegrating tablets", false,
                "Allergy relief, Hay fever, Hives",
                "Take once daily with or without food",
                "Headache, Dry mouth, Drowsiness (rare)",
                "May take with food if stomach upset occurs",
                "Store at room temperature"),

            createMedication("Omeprazole", "Omeprazole", "Proton Pump Inhibitor",
                "Capsules, Tablets, Powder", true,
                "Heartburn, GERD, Stomach ulcers",
                "Take before meals, usually once daily",
                "Headache, Diarrhea, Abdominal pain",
                "Take on empty stomach, Don't crush or chew capsules",
                "Store in original container"),

            createMedication("Simvastatin", "Simvastatin", "Statin",
                "Tablets", true,
                "High cholesterol",
                "Take once daily in the evening",
                "Muscle pain, Headache, Nausea, Liver problems",
                "Avoid grapefruit, Report muscle pain to doctor",
                "Store at room temperature"),

            createMedication("Metformin", "Metformin", "Biguanide",
                "Tablets, Extended-release tablets", true,
                "Type 2 diabetes",
                "Take with meals to reduce stomach side effects",
                "Diarrhea, Nausea, Gas, Metallic taste",
                "Take with food, Avoid excessive alcohol",
                "Store in tight container"),

            createMedication("Levothyroxine", "Levothyroxine", "Thyroid Hormone",
                "Tablets", true,
                "Hypothyroidism",
                "Take on empty stomach 30-60 minutes before breakfast",
                "Palpitations, Weight loss, Headache, Insomnia",
                "Take consistently, Don't take with calcium or iron supplements",
                "Store in original bottle"),

            createMedication("Albuterol", "Albuterol", "Bronchodilator",
                "Inhaler, Nebulizer solution, Tablets", true,
                "Asthma, Bronchospasm",
                "Use as needed for breathing difficulty, 1-2 puffs every 4-6 hours",
                "Rapid heartbeat, Tremors, Headache, Nervousness",
                "Don't exceed recommended dose, Rinse mouth after use",
                "Store at room temperature"),

            createMedication("Warfarin", "Warfarin", "Anticoagulant",
                "Tablets", true,
                "Blood clot prevention",
                "Take exactly as prescribed at same time each day",
                "Bleeding, Bruising, Hair loss, Skin necrosis",
                "Regular blood tests needed, Consistent vitamin K intake",
                "Store in original container"),

            createMedication("Atorvastatin", "Atorvastatin", "Statin",
                "Tablets", true,
                "High cholesterol, Cardiovascular prevention",
                "Take once daily with or without food",
                "Muscle pain, Headache, Diarrhea, Liver enzyme changes",
                "Avoid grapefruit, Report unexplained muscle pain",
                "Room temperature storage"),

            createMedication("Losartan", "Losartan", "ARB",
                "Tablets", true,
                "High blood pressure, Heart failure",
                "Take once or twice daily as prescribed",
                "Dizziness, Back pain, Low blood pressure, Cough",
                "May cause dizziness, Rise slowly from sitting position",
                "Store in dry place"),

            createMedication("Sertraline", "Sertraline", "SSRI",
                "Tablets, Liquid", true,
                "Depression, Anxiety, OCD",
                "Take once daily with or without food",
                "Nausea, Insomnia, Sexual dysfunction, Weight changes",
                "Don't stop abruptly, May take 4-6 weeks for full effect",
                "Store at room temperature"),

            createMedication("Metoprolol", "Metoprolol", "Beta Blocker",
                "Tablets, Extended-release tablets", true,
                "High blood pressure, Angina, Heart failure",
                "Take with or immediately after meals",
                "Fatigue, Dizziness, Slow heartbeat, Cold hands/feet",
                "Don't stop suddenly, Monitor pulse rate",
                "Store in tight container"),

            createMedication("Citalopram", "Citalopram", "SSRI",
                "Tablets, Liquid", true,
                "Depression, Anxiety disorders",
                "Take once daily with or without food",
                "Nausea, Dry mouth, Drowsiness, Insomnia",
                "Avoid alcohol, Don't stop abruptly",
                "Store at room temperature"),

            createMedication("Pantoprazole", "Pantoprazole", "PPI",
                "Tablets, IV solution", true,
                "GERD, Erosive esophagitis",
                "Take before meals, usually once daily",
                "Headache, Diarrhea, Nausea, Abdominal pain",
                "Take on empty stomach, Don't crush or chew",
                "Store in original package"),

            createMedication("Amlodipine", "Amlodipine", "Calcium Channel Blocker",
                "Tablets", true,
                "High blood pressure, Angina",
                "Take once daily with or without food",
                "Swelling of ankles/feet, Dizziness, Flushing, Headache",
                "Rise slowly from sitting position",
                "Store in dry place"),

            createMedication("Hydrochlorothiazide", "Hydrochlorothiazide", "Diuretic",
                "Tablets, Capsules", true,
                "High blood pressure, Edema",
                "Take in morning to avoid nighttime urination",
                "Increased urination, Dizziness, Low potassium, Sun sensitivity",
                "Take with food, Avoid excessive sun exposure",
                "Store in tight container"),

            createMedication("Gabapentin", "Gabapentin", "Anticonvulsant",
                "Capsules, Tablets, Solution", true,
                "Seizures, Nerve pain, Restless legs",
                "Take as directed, usually three times daily",
                "Dizziness, Drowsiness, Water retention, Weight gain",
                "May cause drowsiness, Don't stop abruptly",
                "Store at room temperature"),

            createMedication("Tramadol", "Tramadol", "Opioid Analgesic",
                "Tablets, Capsules, Liquid", true,
                "Moderate to severe pain",
                "Take as needed for pain, every 4-6 hours",
                "Nausea, Dizziness, Constipation, Drowsiness",
                "May be habit-forming, Avoid alcohol",
                "Store in secure location"),

            createMedication("Diazepam", "Diazepam", "Benzodiazepine",
                "Tablets, Liquid, Injection", true,
                "Anxiety, Muscle spasms, Seizures",
                "Take exactly as prescribed",
                "Drowsiness, Fatigue, Muscle weakness, Dependence",
                "Avoid alcohol, Don't stop abruptly, May be habit-forming",
                "Store in secure place"),

            createMedication("Prednisone", "Prednisone", "Corticosteroid",
                "Tablets, Liquid", true,
                "Inflammation, Allergies, Autoimmune disorders",
                "Take with food, follow tapering schedule exactly",
                "Increased appetite, Weight gain, Mood changes, Insomnia",
                "Don't stop suddenly, Take with food",
                "Store at room temperature"),

            createMedication("Cephalexin", "Cephalexin", "Antibiotic",
                "Capsules, Tablets, Liquid", true,
                "Bacterial infections",
                "Take every 6-12 hours as directed, with food",
                "Diarrhea, Nausea, Stomach pain, Rash",
                "Complete full course, Take with food",
                "Refrigerate liquid form"),

            createMedication("Fluoxetine", "Fluoxetine", "SSRI",
                "Capsules, Tablets, Liquid", true,
                "Depression, OCD, Bulimia",
                "Take once daily, usually in morning",
                "Nausea, Headache, Insomnia, Anxiety",
                "May take 4-6 weeks for full effect, Don't stop abruptly",
                "Store at room temperature"),

            createMedication("Clopidogrel", "Clopidogrel", "Anti-platelet",
                "Tablets", true,
                "Heart attack prevention, Stroke prevention",
                "Take once daily with or without food",
                "Bleeding, Bruising, Stomach pain, Rash",
                "Don't stop without doctor advice, Report unusual bleeding",
                "Store in original container"),

            createMedication("Insulin Glargine", "Insulin Glargine", "Long-acting Insulin",
                "Injection", true,
                "Diabetes mellitus",
                "Inject subcutaneously once daily at same time",
                "Low blood sugar, Weight gain, Injection site reactions",
                "Rotate injection sites, Monitor blood sugar regularly",
                "Refrigerate, don't freeze"),

            createMedication("Montelukast", "Montelukast", "Leukotriene Receptor Antagonist",
                "Tablets, Chewable tablets", true,
                "Asthma, Allergy prevention",
                "Take once daily in evening",
                "Headache, Upset stomach, Dizziness, Behavior changes",
                "Take regularly for best effect, Not for acute asthma attacks",
                "Store in original package"),

            createMedication("Furosemide", "Furosemide", "Loop Diuretic",
                "Tablets, Liquid, Injection", true,
                "Edema, High blood pressure",
                "Take in morning to avoid nighttime urination",
                "Increased urination, Dizziness, Dehydration, Low potassium",
                "Take with food, Rise slowly, Monitor weight",
                "Store in tight container"),

            createMedication("Tamsulosin", "Tamsulosin", "Alpha Blocker",
                "Capsules", true,
                "Benign prostatic hyperplasia",
                "Take once daily 30 minutes after same meal",
                "Dizziness, Headache, Abnormal ejaculation, Runny nose",
                "Take after same meal daily, Rise slowly",
                "Store at room temperature"),

            createMedication("Duloxetine", "Duloxetine", "SNRI",
                "Capsules, Delayed-release capsules", true,
                "Depression, Anxiety, Nerve pain",
                "Take with or without food, usually once or twice daily",
                "Nausea, Dry mouth, Drowsiness, Constipation",
                "Don't stop abruptly, May take weeks for full effect",
                "Store in original container"),

            createMedication("Venlafaxine", "Venlafaxine", "SNRI",
                "Tablets, Extended-release capsules", true,
                "Depression, Anxiety, Panic disorder",
                "Take with food, usually once or twice daily",
                "Nausea, Dizziness, Insomnia, High blood pressure",
                "Take with food, Don't stop abruptly",
                "Store at room temperature"),

            createMedication("Quetiapine", "Quetiapine", "Atypical Antipsychotic",
                "Tablets, Extended-release tablets", true,
                "Bipolar disorder, Schizophrenia, Depression",
                "Take as directed, usually once or twice daily",
                "Drowsiness, Dizziness, Dry mouth, Weight gain",
                "May cause drowsiness, Rise slowly",
                "Store in tight container"),

            createMedication("Zolpidem", "Zolpidem", "Sedative-Hypnotic",
                "Tablets, Sublingual tablets", true,
                "Insomnia",
                "Take immediately before bedtime on empty stomach",
                "Drowsiness, Dizziness, Headache, Memory problems",
                "Take only when able to get 7-8 hours sleep",
                "Store in secure location"),

            createMedication("Allopurinol", "Allopurinol", "Xanthine Oxidase Inhibitor",
                "Tablets", true,
                "Gout, Kidney stones",
                "Take once daily with plenty of fluids",
                "Rash, Nausea, Liver problems, Drowsiness",
                "Drink plenty of fluids, May initially increase gout attacks",
                "Store in dry place"),

            createMedication("Metronidazole", "Metronidazole", "Antibiotic",
                "Tablets, Capsules, Gel, Cream", true,
                "Bacterial infections, Parasitic infections",
                "Take exactly as prescribed, usually with food",
                "Nausea, Metallic taste, Dark urine, Headache",
                "Avoid alcohol during and 3 days after treatment",
                "Store at room temperature"),

            createMedication("Spironolactone", "Spironolactone", "Potassium-sparing Diuretic",
                "Tablets", true,
                "High blood pressure, Heart failure, Acne",
                "Take with food, usually once or twice daily",
                "Increased potassium, Breast tenderness, Dizziness",
                "Avoid potassium supplements, Take with food",
                "Store in tight container"),

            createMedication("Carvedilol", "Carvedilol", "Beta Blocker",
                "Tablets, Extended-release capsules", true,
                "High blood pressure, Heart failure",
                "Take with food, usually twice daily",
                "Dizziness, Fatigue, Slow heartbeat, Weight gain",
                "Take with food, Don't stop suddenly",
                "Store at room temperature"),

            createMedication("Pregabalin", "Pregabalin", "Anticonvulsant",
                "Capsules, Solution", true,
                "Nerve pain, Seizures, Anxiety",
                "Take as directed, usually two or three times daily",
                "Dizziness, Drowsiness, Dry mouth, Weight gain",
                "May cause drowsiness, Don't stop abruptly",
                "Store in original container"),

            createMedication("Esomeprazole", "Esomeprazole", "PPI",
                "Capsules, Packets", true,
                "GERD, Erosive esophagitis",
                "Take at least 1 hour before meals",
                "Headache, Diarrhea, Nausea, Abdominal pain",
                "Take before meals, Don't crush or chew capsules",
                "Store in original package"),

            createMedication("Atenolol", "Atenolol", "Beta Blocker",
                "Tablets", true,
                "High blood pressure, Angina",
                "Take once or twice daily with or without food",
                "Fatigue, Dizziness, Cold hands/feet, Depression",
                "Don't stop suddenly, Monitor pulse rate",
                "Store in dry place"),

            createMedication("Lisinopril", "Lisinopril", "ACE Inhibitor",
                "Tablets", true,
                "High blood pressure, Heart failure",
                "Take once daily with or without food",
                "Cough, Dizziness, Headache, High potassium",
                "Rise slowly, Report persistent cough",
                "Store in tight container"),

            createMedication("Ranitidine", "Ranitidine", "H2 Blocker",
                "Tablets, Liquid, Injection", false,
                "Heartburn, Ulcers, GERD",
                "Take with or without food as directed",
                "Headache, Constipation, Diarrhea, Drowsiness",
                "May take with food if stomach upset occurs",
                "Store at room temperature"),

            createMedication("Diphenhydramine", "Diphenhydramine", "Antihistamine",
                "Capsules, Tablets, Liquid, Cream", false,
                "Allergies, Itching, Sleep aid",
                "Take as needed for symptoms, usually every 4-6 hours",
                "Drowsiness, Dry mouth, Dizziness, Urinary retention",
                "May cause drowsiness, Avoid alcohol",
                "Store in dry place"),

            createMedication("Pepto-Bismol", "Bismuth subsalicylate", "Antidiarrheal",
                "Liquid, Tablets, Caplets", false,
                "Diarrhea, Heartburn, Nausea",
                "Take as needed, usually every 30-60 minutes",
                "Darkened stools, Constipation, Ringing in ears",
                "Don't use with aspirin, May darken stools",
                "Store at room temperature"),

            createMedication("Loperamide", "Loperamide", "Antidiarrheal",
                "Capsules, Tablets, Liquid", false,
                "Diarrhea",
                "Take after each loose stool, not to exceed maximum dose",
                "Constipation, Dizziness, Stomach pain",
                "Don't use if bloody diarrhea or fever present",
                "Store in original container"),

            createMedication("Guaifenesin", "Guaifenesin", "Expectorant",
                "Tablets, Syrup, Capsules", false,
                "Chest congestion",
                "Take every 4 hours with full glass of water",
                "Nausea, Vomiting, Dizziness, Headache",
                "Drink plenty of fluids, Don't use for persistent cough",
                "Store at room temperature"),

            createMedication("Dextromethorphan", "Dextromethorphan", "Antitussive",
                "Syrup, Lozenges, Gelcaps", false,
                "Cough suppression",
                "Take every 4-8 hours as needed for cough",
                "Drowsiness, Dizziness, Nausea",
                "Don't use for persistent chronic cough",
                "Store in original container"),

            createMedication("Pseudoephedrine", "Pseudoephedrine", "Decongestant",
                "Tablets, Liquid", true,
                "Nasal congestion",
                "Take every 4-6 hours as needed",
                "Nervousness, Restlessness, Sleeplessness, Increased blood pressure",
                "May cause insomnia, Don't use if high blood pressure",
                "Store at room temperature"),

            createMedication("Chlorpheniramine", "Chlorpheniramine", "Antihistamine",
                "Tablets, Syrup, Injection", false,
                "Allergy relief, Hay fever",
                "Take every 4-6 hours as needed for symptoms",
                "Drowsiness, Dry mouth, Dizziness, Blurred vision",
                "May cause drowsiness, Avoid alcohol",
                "Store in dry place"),

            createMedication("Hydrocortisone", "Hydrocortisone", "Corticosteroid",
                "Cream, Ointment, Lotion", false,
                "Skin inflammation, Itching, Rashes",
                "Apply thin layer to affected area 1-4 times daily",
                "Skin thinning, Burning, Itching, Irritation",
                "Don't use on face or broken skin long-term",
                "Store at room temperature"),

            createMedication("Neosporin", "Bacitracin/Neomycin/Polymyxin", "Antibiotic Ointment",
                "Ointment, Cream", false,
                "Minor cuts, Burns, Scrapes prevention",
                "Apply to cleaned area 1-3 times daily",
                "Itching, Redness, Swelling, Allergic reactions",
                "Stop use if rash develops, For external use only",
                "Store at room temperature"),

            createMedication("Calamine", "Calamine", "Topical Antipruritic",
                "Lotion, Cream", false,
                "Itching, Poison ivy, Insect bites",
                "Apply to affected area as needed",
                "Skin irritation, Dryness",
                "Shake well before use, For external use only",
                "Store at room temperature")
        );

        medicationRepository.saveAll(medications);
    }

    private Symptom createSymptom(String name, String category, String severity,
                                 String description, String immediateActions,
                                 String warningSigns, String whenToSeekHelp) {
        Symptom symptom = new Symptom();
        symptom.setName(name);
        symptom.setCategory(category);
        symptom.setSeverity(severity);
        symptom.setDescription(description);
        symptom.setImmediateActions(immediateActions);
        symptom.setWarningSigns(warningSigns);
        symptom.setWhenToSeekHelp(whenToSeekHelp);
        symptom.setCreatedAt(LocalDateTime.now());
        symptom.setUpdatedAt(LocalDateTime.now());
        return symptom;
    }

    private FirstAidGuide createFirstAidGuide(String title, String description, String steps,
                                             String category, String severity, String priority,
                                             String difficulty, String estimatedTime,
                                             String equipmentNeeded, String icon) {
        FirstAidGuide guide = new FirstAidGuide();
        guide.setTitle(title);
        guide.setDescription(description);
        guide.setSteps(steps);
        guide.setCategory(category);
        guide.setSeverity(severity);
        guide.setPriority(priority);
        guide.setDifficulty(difficulty);
        guide.setEstimatedTime(estimatedTime);
        guide.setEquipmentNeeded(equipmentNeeded);
        guide.setIcon(icon);
        guide.setCreatedAt(LocalDateTime.now());
        guide.setUpdatedAt(LocalDateTime.now());
        return guide;
    }

    private Medication createMedication(String name, String genericName, String drugClass,
                                      String dosageForms, boolean prescriptionRequired,
                                      String commonUses, String usageInstructions,
                                      String sideEffects, String precautions,
                                      String storageInstructions) {
        Medication medication = new Medication();
        medication.setName(name);
        medication.setGenericName(genericName);
        medication.setDrugClass(drugClass);
        medication.setDosageForms(dosageForms);
        medication.setPrescriptionRequired(prescriptionRequired);
        medication.setCommonUses(commonUses);
        medication.setUsageInstructions(usageInstructions);
        medication.setSideEffects(sideEffects);
        medication.setPrecautions(precautions);
        medication.setStorageInstructions(storageInstructions);
        medication.setCreatedAt(LocalDateTime.now());
        medication.setUpdatedAt(LocalDateTime.now());
        return medication;
    }
}