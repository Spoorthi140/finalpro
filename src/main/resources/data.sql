-- Clear existing data
DELETE FROM sales;
DELETE FROM medications;
DELETE FROM categories;
DELETE FROM suppliers;
DELETE FROM users;

-- Insert 15 sample categories
INSERT INTO categories (id, name, description, created_at, updated_at) VALUES
(1, 'Analgesics & Pain Relief', 'Pain relief and anti-inflammatory medications', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Fever & Cold', 'Antipyretics, cold, cough, and decongestants', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Antibiotics & Antifungals', 'Anti-bacterial and anti-fungal treatments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Antacids & Gastrointestinal', 'Medications for acidity, GERD, and digestion', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Antihistamines & Allergy', 'Allergy relief and anti-histamine drugs', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Vitamins & Supplements', 'Nutritional supplements and mineral formulas', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Antidiabetics', 'Blood sugar control and diabetes management', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Cardiovascular & BP', 'Hypertension, heart, and cholesterol medications', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Skin Care & Topical', 'Creams, ointments, and dermatological remedies', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Eye & Ear Care', 'Ophthalmic and otic drops', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'First Aid & Antiseptics', 'Disinfectants, bandages, and wound care', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Respiratory & Asthma', 'Inhalers, bronchodilators, and lung care', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'Hormones & Steroids', 'Endocrine treatments and anti-inflammatory steroids', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'CNS & Psychiatric', 'Neurological, anti-anxiety, and sleep aids', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Muscle Relaxants', 'Treatments for muscle spasms and stiffness', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert 10 Indian pharma suppliers
INSERT INTO suppliers (id, name, contact_person, phone, email, address, active, created_at, updated_at) VALUES
(1, 'Apollo Pharmacy Distributors', 'Rajesh Sharma', '+91 98765 43210', 'rajesh@apollodist.in', '12, Link Road, Andheri West, Mumbai, Maharashtra 400053', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'MedPlus Logistics', 'Sunil Verma', '+91 91234 56789', 'sunil@medplus.co.in', '45, MG Road, Indiranagar, Bengaluru, Karnataka 560038', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Cipla Supply Chain Ltd', 'Priya Patel', '+91 99887 66554', 'priya.patel@cipla-supply.in', '78, CG Road, Navrangpura, Ahmedabad, Gujarat 380009', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Sun Pharma Wholesalers', 'Amitabh Roy', '+91 98112 23344', 'amitabh@sunwholesalers.in', '102, Connaught Place, New Delhi, Delhi 110001', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Dr Reddys Pharma Depot', 'Kavita Reddy', '+91 97001 12233', 'kavita@drreddysdepot.in', '33, Banjara Hills, Road No 12, Hyderabad, Telangana 500034', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Mankind Pharma Supply', 'Vikram Singh', '+91 98450 98450', 'vikram@mankindsupply.in', '56, Park Street, Kolkata, West Bengal 700016', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Lupin Medical Agencies', 'Sanjay Kulkarni', '+91 98220 12345', 'sanjay@lupinagencies.in', '88, FC Road, Shivajinagar, Pune, Maharashtra 411005', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Zydus Health Distributors', 'Anil Mehta', '+91 99000 55443', 'anil@zydusdist.in', '21, SG Highway, Thaltej, Ahmedabad, Gujarat 380054', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Torrent Pharma Depot', 'Meena Iyer', '+91 94440 11223', 'meena@torrentdepot.in', '14, Anna Salai, T Nagar, Chennai, Tamil Nadu 600017', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Alkem Pharma Services', 'Rohan Gupta', '+91 98300 77889', 'rohan@alkemservices.in', '67, Boring Road, Patna, Bihar 800001', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert 52 realistic Indian medicines
INSERT INTO medications (id, name, manufacturer, price, quantity, low_stock_threshold, expiration_date, type, description, batch_number, category_id, supplier_id, is_deleted, created_at, updated_at) VALUES
(1, 'Dolo 650mg Tablet', 'Micro Labs Ltd', 30.50, 150, 20, DATEADD('DAY', -5, CURRENT_DATE), 'TABLET', 'Paracetamol 650mg used for fever and mild to moderate pain relief.', 'BATCH-IN-001', 2, 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Crocin 500mg Tablet', 'GlaxoSmithKline', 24.00, 120, 15, DATEADD('DAY', -2, CURRENT_DATE), 'TABLET', 'Fast acting analgesic and antipyretic for fever and headache.', 'BATCH-IN-002', 2, 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Combiflam Tablet', 'Sanofi India', 45.00, 5, 15, DATEADD('DAY', 3, CURRENT_DATE), 'TABLET', 'Ibuprofen 400mg + Paracetamol 325mg combination for joint and muscle pain.', 'BATCH-IN-003', 1, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Zerodol-SP Tablet', 'Ipca Laboratories', 115.00, 80, 15, DATEADD('DAY', 5, CURRENT_DATE), 'TABLET', 'Aceclofenac 100mg + Paracetamol 325mg + Serratiopeptidase 15mg for swelling and pain.', 'BATCH-IN-004', 1, 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Sumo 100mg/325mg Tablet', 'Alkem Laboratories', 88.00, 4, 10, DATEADD('DAY', 12, CURRENT_DATE), 'TABLET', 'Nimesulide + Paracetamol combination for acute pain and inflammatory fever.', 'BATCH-IN-005', 1, 10, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Pan 40mg Tablet', 'Alkem Laboratories', 155.00, 90, 15, DATEADD('DAY', 18, CURRENT_DATE), 'TABLET', 'Pantoprazole gastro-resistant tablet for hyperacidity, GERD, and stomach ulcers.', 'BATCH-IN-006', 4, 10, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Pantocid D SR Capsule', 'Sun Pharma', 198.50, 60, 10, DATEADD('DAY', 25, CURRENT_DATE), 'CAPSULE', 'Pantoprazole 40mg + Domperidone 30mg SR for acidity with nausea.', 'BATCH-IN-007', 4, 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Omez 20mg Capsule', 'Dr Reddys Laboratories', 62.00, 110, 20, DATEADD('DAY', 28, CURRENT_DATE), 'CAPSULE', 'Omeprazole 20mg for peptic ulcer disease and heartburn relief.', 'BATCH-IN-008', 4, 5, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Digene Gel Syrup 200ml', 'Abbott India', 142.00, 35, 8, DATEADD('DAY', 45, CURRENT_DATE), 'SYRUP', 'Antacid liquid providing quick relief from acidity, gas, and bloat.', 'BATCH-IN-009', 4, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Gelusil MPS Syrup 200ml', 'Pfizer India', 130.00, 40, 10, DATEADD('DAY', 60, CURRENT_DATE), 'SYRUP', 'Antacid and anti-flatulent liquid syrup for stomach acidity.', 'BATCH-IN-010', 4, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Azithral 500mg Tablet', 'Alembic Pharmaceuticals', 121.50, 45, 10, DATEADD('DAY', 90, CURRENT_DATE), 'TABLET', 'Azithromycin 500mg antibiotic for throat, chest, and skin infections.', 'BATCH-IN-011', 3, 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Moxikind-CV 625 Tablet', 'Mankind Pharma', 204.00, 70, 15, DATEADD('DAY', 120, CURRENT_DATE), 'TABLET', 'Amoxicillin 500mg + Clavulanic Acid 125mg broad spectrum antibiotic.', 'BATCH-IN-012', 3, 6, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'Augmentin 625 Duo Tablet', 'GlaxoSmithKline', 223.00, 65, 12, DATEADD('DAY', 150, CURRENT_DATE), 'TABLET', 'Potent antibacterial for severe respiratory, urinary, and dental infections.', 'BATCH-IN-013', 3, 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Ciplox 500mg Tablet', 'Cipla Ltd', 42.00, 85, 20, DATEADD('DAY', 180, CURRENT_DATE), 'TABLET', 'Ciprofloxacin 500mg fluoroquinolone antibiotic.', 'BATCH-IN-014', 3, 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Taxim-O 200mg Tablet', 'Alkem Laboratories', 112.00, 50, 10, DATEADD('DAY', 200, CURRENT_DATE), 'TABLET', 'Cefixime 200mg oral cephalosporin antibiotic.', 'BATCH-IN-015', 3, 10, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'Azee 500mg Tablet', 'Cipla Ltd', 118.00, 55, 12, DATEADD('DAY', 210, CURRENT_DATE), 'TABLET', 'Azithromycin tablet for bacterial respiratory tract infections.', 'BATCH-IN-016', 3, 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'Allegra 120mg Tablet', 'Sanofi India', 210.00, 40, 10, DATEADD('DAY', 240, CURRENT_DATE), 'TABLET', 'Fexofenadine 120mg non-drowsy anti-allergy medication.', 'BATCH-IN-017', 5, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'Cetzine 10mg Tablet', 'Dr Reddys Laboratories', 22.00, 140, 25, DATEADD('DAY', 270, CURRENT_DATE), 'TABLET', 'Cetirizine 10mg for allergic rhinitis, sneezing, and hives.', 'BATCH-IN-018', 5, 5, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'Levocet 5mg Tablet', 'Cipla Ltd', 48.00, 100, 20, DATEADD('DAY', 300, CURRENT_DATE), 'TABLET', 'Levocetirizine 5mg for allergic cold and skin allergies.', 'BATCH-IN-019', 5, 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'Avil 25mg Tablet', 'Sanofi India', 12.50, 200, 30, DATEADD('DAY', 330, CURRENT_DATE), 'TABLET', 'Pheniramine maleate anti-allergy tablet for itching and motion sickness.', 'BATCH-IN-020', 5, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(21, 'Ascoril LS Syrup 100ml', 'Glenmark Pharmaceuticals', 118.00, 50, 12, DATEADD('DAY', 360, CURRENT_DATE), 'SYRUP', 'Levosalbutamol + Ambroxol + Guaiphenesin mucolytic cough syrup for wet cough.', 'BATCH-IN-021', 2, 7, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(22, 'Benadryl Cough Syrup 150ml', 'Johnson & Johnson', 135.00, 60, 15, DATEADD('DAY', 380, CURRENT_DATE), 'SYRUP', 'Soothes dry cough and throat irritation.', 'BATCH-IN-022', 2, 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(23, 'Alex Cough Syrup 100ml', 'Glenmark Pharmaceuticals', 125.00, 45, 10, DATEADD('DAY', 400, CURRENT_DATE), 'SYRUP', 'Dextromethorphan + Chlorpheniramine dry cough formula.', 'BATCH-IN-023', 2, 7, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(24, 'Glycomet 500mg Tablet', 'USV Private Ltd', 32.00, 180, 30, DATEADD('DAY', 420, CURRENT_DATE), 'TABLET', 'Metformin 500mg for type-2 diabetes blood sugar management.', 'BATCH-IN-024', 7, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(25, 'Glycomet GP 2 Tablet', 'USV Private Ltd', 145.00, 90, 15, DATEADD('DAY', 450, CURRENT_DATE), 'TABLET', 'Metformin 500mg + Glimepiride 2mg dual diabetes control.', 'BATCH-IN-025', 7, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(26, 'Janumet 50mg/500mg Tablet', 'MSD India', 580.00, 30, 8, DATEADD('DAY', 480, CURRENT_DATE), 'TABLET', 'Sitagliptin + Metformin advanced oral antidiabetic.', 'BATCH-IN-026', 7, 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(27, 'Amlokind 5mg Tablet', 'Mankind Pharma', 18.50, 220, 30, DATEADD('DAY', 500, CURRENT_DATE), 'TABLET', 'Amlodipine 5mg calcium channel blocker for hypertension.', 'BATCH-IN-027', 8, 6, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(28, 'Telmikind 40mg Tablet', 'Mankind Pharma', 52.00, 130, 20, DATEADD('DAY', 520, CURRENT_DATE), 'TABLET', 'Telmisartan 40mg angiotensin receptor blocker for BP control.', 'BATCH-IN-028', 8, 6, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(29, 'Losar 50mg Tablet', 'Torrent Pharmaceuticals', 86.00, 95, 15, DATEADD('DAY', 540, CURRENT_DATE), 'TABLET', 'Losartan potassium 50mg for high blood pressure.', 'BATCH-IN-029', 8, 9, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(30, 'Ciplar 10mg Tablet', 'Cipla Ltd', 24.50, 110, 20, DATEADD('DAY', 560, CURRENT_DATE), 'TABLET', 'Propranolol 10mg beta-blocker for BP and anxiety tremors.', 'BATCH-IN-030', 8, 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(31, 'Atorva 10mg Tablet', 'Zydus Cadila', 98.00, 85, 15, DATEADD('DAY', 580, CURRENT_DATE), 'TABLET', 'Atorvastatin 10mg for cholesterol management.', 'BATCH-IN-031', 8, 8, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(32, 'Ecosprin 75mg Tablet', 'USV Private Ltd', 9.50, 300, 50, DATEADD('DAY', 600, CURRENT_DATE), 'TABLET', 'Low-dose Aspirin 75mg blood thinner for heart health.', 'BATCH-IN-032', 8, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(33, 'Becosules Capsules (20s)', 'Pfizer India', 50.00, 160, 25, DATEADD('DAY', 620, CURRENT_DATE), 'CAPSULE', 'B-Complex with Vitamin C for immunity, mouth ulcers, and energy.', 'BATCH-IN-033', 6, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(34, 'Limcee 500mg Chewable Tablet', 'Abbott India', 25.50, 250, 40, DATEADD('DAY', 640, CURRENT_DATE), 'TABLET', 'Vitamin C 500mg chewable tablets for immunity.', 'BATCH-IN-034', 6, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(35, 'Shelcal 500mg Tablet', 'Torrent Pharmaceuticals', 132.00, 110, 20, DATEADD('DAY', 660, CURRENT_DATE), 'TABLET', 'Calcium 500mg + Vitamin D3 250IU for bone density.', 'BATCH-IN-035', 6, 9, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(36, 'Urisept 100mg Capsule', 'Lupin Ltd', 165.00, 40, 10, DATEADD('DAY', 680, CURRENT_DATE), 'CAPSULE', 'Nitrofurantoin for urinary tract infections.', 'BATCH-IN-036', 3, 7, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(37, 'Electral ORS Sachet 21.8g', 'FDC Ltd', 22.00, 400, 50, DATEADD('DAY', 700, CURRENT_DATE), 'POWDER', 'WHO formula Oral Rehydration Salts for dehydration and diarrhea.', 'BATCH-IN-037', 6, 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(38, 'Betadine 10% Ointment 15g', 'Win-Medicare', 72.00, 80, 15, DATEADD('DAY', 720, CURRENT_DATE), 'CREAM', 'Povidone-Iodine antiseptic ointment for wounds and cuts.', 'BATCH-IN-038', 11, 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(39, 'Volini Pain Relief Gel 30g', 'Sun Pharma', 145.00, 65, 12, DATEADD('DAY', 740, CURRENT_DATE), 'GEL', 'Diclofenac gel for quick joint, neck, and back pain relief.', 'BATCH-IN-039', 1, 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(40, 'Omnigel 30g Tube', 'Cipla Ltd', 110.00, 75, 15, DATEADD('DAY', 760, CURRENT_DATE), 'GEL', 'Topical pain relief gel for sprains and strains.', 'BATCH-IN-040', 1, 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(41, 'Candid B Cream 20g', 'Glenmark Pharmaceuticals', 185.00, 50, 10, DATEADD('DAY', 780, CURRENT_DATE), 'CREAM', 'Clotrimazole + Beclomethasone for fungal skin infections.', 'BATCH-IN-041', 9, 7, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(42, 'Boroline Antiseptic Cream 20g', 'G D Pharmaceuticals', 42.00, 120, 20, DATEADD('DAY', 800, CURRENT_DATE), 'CREAM', 'Ayurvedic antiseptic cream for dry skin and minor cuts.', 'BATCH-IN-042', 9, 6, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(43, 'Otrivin Adult Nasal Drops 10ml', 'GlaxoSmithKline', 108.00, 70, 15, DATEADD('DAY', 820, CURRENT_DATE), 'DROPS', 'Xylometazoline nasal decongestant for blocked nose.', 'BATCH-IN-043', 2, 1, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(44, 'Ciplox Eye/Ear Drops 10ml', 'Cipla Ltd', 21.00, 150, 25, DATEADD('DAY', 840, CURRENT_DATE), 'DROPS', 'Ciprofloxacin eye and ear antibacterial drops.', 'BATCH-IN-044', 10, 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(45, 'Asthalin Inhaler 100mcg', 'Cipla Ltd', 162.00, 45, 10, DATEADD('DAY', 860, CURRENT_DATE), 'INHALER', 'Salbutamol inhaler for asthma and bronchospasm relief.', 'BATCH-IN-045', 12, 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(46, 'Foracort 200 Synchrobreathe', 'Cipla Ltd', 525.00, 25, 5, DATEADD('DAY', 880, CURRENT_DATE), 'INHALER', 'Formoterol + Budesonide breath-actuated inhaler for asthma.', 'BATCH-IN-046', 12, 3, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(47, 'Deriphyllin Retard 150mg', 'Zydus Healthcare', 34.00, 110, 20, DATEADD('DAY', 900, CURRENT_DATE), 'TABLET', 'Etofylline + Theophylline for asthma and chronic bronchitis.', 'BATCH-IN-047', 12, 8, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(48, 'Thyronorm 50mcg Tablet', 'Abbott India', 185.00, 100, 15, DATEADD('DAY', 920, CURRENT_DATE), 'TABLET', 'Levothyroxine sodium for hypothyroidism management.', 'BATCH-IN-048', 13, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(49, 'Dexamethasone 0.5mg Tablet', 'Zydus Cadila', 8.50, 300, 50, DATEADD('DAY', 940, CURRENT_DATE), 'TABLET', 'Corticosteroid for severe allergic and inflammatory conditions.', 'BATCH-IN-049', 13, 8, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(50, 'Myospaz Tablet', 'Win-Medicare', 168.00, 60, 10, DATEADD('DAY', 960, CURRENT_DATE), 'TABLET', 'Paracetamol + Chlorzoxazone muscle relaxant for back pain.', 'BATCH-IN-050', 15, 4, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(51, 'Alprax 0.25mg Tablet', 'Torrent Pharmaceuticals', 29.00, 90, 15, DATEADD('DAY', 980, CURRENT_DATE), 'TABLET', 'Alprazolam 0.25mg for anxiety and panic disorders.', 'BATCH-IN-051', 14, 9, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(52, 'Evion 400mg Capsule', 'Procter & Gamble Health', 38.50, 180, 25, DATEADD('DAY', 1000, CURRENT_DATE), 'CAPSULE', 'Vitamin E 400mg capsule for skin, hair, and antioxidant protection.', 'BATCH-IN-052', 6, 2, false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample sales records in INR
INSERT INTO sales (sale_date, quantity, unit_price, cost_price, total_price, medication_id, created_at) VALUES
(CURRENT_DATE, 5, 30.50, 21.35, 152.50, 1, CURRENT_TIMESTAMP),
(CURRENT_DATE, 3, 155.00, 108.50, 465.00, 6, CURRENT_TIMESTAMP),
(DATEADD('DAY', -1, CURRENT_DATE), 2, 204.00, 142.80, 408.00, 12, CURRENT_TIMESTAMP),
(DATEADD('DAY', -2, CURRENT_DATE), 4, 132.00, 92.40, 528.00, 35, CURRENT_TIMESTAMP),
(DATEADD('DAY', -3, CURRENT_DATE), 10, 24.00, 16.80, 240.00, 2, CURRENT_TIMESTAMP),
(DATEADD('DAY', -4, CURRENT_DATE), 1, 525.00, 367.50, 525.00, 46, CURRENT_TIMESTAMP),
(DATEADD('DAY', -5, CURRENT_DATE), 6, 25.50, 17.85, 153.00, 34, CURRENT_TIMESTAMP),
(DATEADD('DAY', -6, CURRENT_DATE), 2, 198.50, 138.95, 397.00, 7, CURRENT_TIMESTAMP),
(DATEADD('MONTH', -1, CURRENT_DATE), 15, 30.50, 21.35, 457.50, 1, CURRENT_TIMESTAMP),
(DATEADD('MONTH', -2, CURRENT_DATE), 20, 45.00, 31.50, 900.00, 3, CURRENT_TIMESTAMP);
