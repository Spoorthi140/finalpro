-- Clear existing data (optional)
DELETE FROM medications;
DELETE FROM categories;
DELETE FROM suppliers;

-- Insert 20 sample categories
INSERT INTO categories (id, name, description, created_at, updated_at) VALUES
(1, 'Analgesics', 'Pain relief medications used to treat headaches, muscle aches, arthritis, and other pains', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Antibiotics', 'Anti-bacterial medications used to treat bacterial infections', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Antihypertensives', 'Blood pressure medications used to control high blood pressure', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Vitamins', 'Nutritional supplements and vitamins for overall health', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Antidiabetics', 'Diabetes medications used to control blood sugar levels', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Antidepressants', 'Medications used to treat depression and anxiety disorders', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Antacids', 'Medications that neutralize stomach acidity', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Antihistamines', 'Allergy medications that block histamine effects', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Anticoagulants', 'Blood thinners that prevent blood clots', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Bronchodilators', 'Medications that open up airways in the lungs', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Corticosteroids', 'Anti-inflammatory medications for various conditions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Diuretics', 'Medications that increase urine production', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'Anticonvulsants', 'Medications to control seizures and epilepsy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Antipsychotics', 'Medications for psychiatric conditions like schizophrenia', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Hormones', 'Hormone replacement therapies and endocrine medications', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'Muscle Relaxants', 'Medications that relieve muscle spasms and pain', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'Ophthalmic', 'Eye drops and medications for eye conditions', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'Dermatological', 'Skin creams, ointments, and topical treatments', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'Cardiovascular', 'Medications for heart conditions and circulation', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'Gastrointestinal', 'Medications for digestive system disorders', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert 20 sample suppliers
INSERT INTO suppliers (id, name, contact_person, phone, email, address, active, created_at, updated_at) VALUES
(1, 'MediCorp Pharmaceuticals', 'John Smith', '+1-555-0101', 'john@medicorp.com', '123 Pharma Street, New York, NY 10001, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'HealthPlus Suppliers', 'Sarah Johnson', '+1-555-0102', 'sarah@healthplus.com', '456 Medical Avenue, Los Angeles, CA 90001, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'PrimeMed Distributors', 'Mike Wilson', '+1-555-0103', 'mike@primemed.com', '789 Health Boulevard, Chicago, IL 60007, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Global Pharma Solutions', 'Emily Chen', '+1-555-0104', 'emily@globalpharma.com', '321 Wellness Drive, Houston, TX 77001, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'BioCare Laboratories', 'David Brown', '+1-555-0105', 'david@biocare.com', '654 Care Circle, Phoenix, AZ 85001, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'PharmaTech International', 'Robert Davis', '+1-555-0106', 'robert@pharmatech.com', '987 Medicine Lane, Philadelphia, PA 19102, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'MediSupply Co.', 'Jennifer Lee', '+1-555-0107', 'jennifer@medisupply.com', '654 Pharmacy Road, San Antonio, TX 78201, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Quality Drugs Ltd.', 'James Miller', '+1-555-0108', 'james@qualitydrugs.com', '321 Healthcare Ave, San Diego, CA 92101, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Prime Pharmaceuticals', 'Lisa Garcia', '+1-555-0109', 'lisa@primepharma.com', '159 Treatment Street, Dallas, TX 75201, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Elite Medical Supplies', 'Thomas Martinez', '+1-555-0110', 'thomas@elitemedical.com', '753 Cure Boulevard, San Jose, CA 95101, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Advanced Pharma Group', 'Maria Rodriguez', '+1-555-0111', 'maria@advancedpharma.com', '852 Wellness Way, Austin, TX 73301, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'National Drug Distributors', 'Kevin Wilson', '+1-555-0112', 'kevin@nationaldrugs.com', '963 Health Street, Jacksonville, FL 32201, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'MediWorld Suppliers', 'Amanda Taylor', '+1-555-0113', 'amanda@mediworld.com', '741 Care Avenue, Fort Worth, TX 76101, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'PharmaCare Solutions', 'Christopher Anderson', '+1-555-0114', 'chris@pharmacare.com', '852 Medicine Circle, Columbus, OH 43201, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Global Health Partners', 'Michelle Thomas', '+1-555-0115', 'michelle@globalhealth.com', '963 Treatment Road, Charlotte, NC 28201, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'Premium Drug Co.', 'Daniel White', '+1-555-0116', 'daniel@premiumdrugs.com', '159 Health Lane, Indianapolis, IN 46201, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'MediExpress Distributors', 'Laura Harris', '+1-555-0117', 'laura@mediexpress.com', '753 Pharma Street, Seattle, WA 98101, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'BioPharma Innovations', 'Steven Clark', '+1-555-0118', 'steven@biopharma.com', '852 Medical Drive, Denver, CO 80201, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'CarePlus Suppliers', 'Nancy Lewis', '+1-555-0119', 'nancy@careplus.com', '963 Wellness Avenue, Washington, DC 20001, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'PharmaExpert Group', 'Paul Walker', '+1-555-0120', 'paul@pharmaexpert.com', '741 Cure Road, Boston, MA 02101, USA', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert 20 sample medications
INSERT INTO medications (name, manufacturer, price, quantity, low_stock_threshold, expiration_date, type, description, batch_number, category_id, supplier_id, created_at, updated_at) VALUES
-- Analgesics
('Paracetamol 500mg', 'MediCorp Pharmaceuticals', 5.99, 100, 20, '2025-12-31', 'TABLET', 'Pain reliever and fever reducer. Used for headaches, muscle aches, arthritis, backaches, toothaches, colds, and fevers.', 'BATCH001', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ibuprofen 400mg', 'HealthPlus Suppliers', 8.50, 75, 15, '2025-11-30', 'TABLET', 'Nonsteroidal anti-inflammatory drug (NSAID) used to treat pain, fever, and inflammation.', 'BATCH002', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Aspirin 325mg', 'PrimeMed Distributors', 4.25, 150, 25, '2026-01-31', 'TABLET', 'Used to treat pain, fever, or inflammation. Also used as blood thinner.', 'BATCH003', 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Naproxen 250mg', 'Global Pharma Solutions', 12.75, 60, 12, '2025-10-31', 'TABLET', 'NSAID for pain relief, inflammation, and fever reduction.', 'BATCH004', 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Diclofenac 50mg', 'BioCare Laboratories', 9.25, 80, 18, '2025-09-30', 'TABLET', 'NSAID used for pain and inflammatory diseases like arthritis.', 'BATCH005', 1, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Antibiotics
('Amoxicillin 250mg', 'MediCorp Pharmaceuticals', 12.50, 50, 15, '2025-06-30', 'CAPSULE', 'Broad-spectrum antibiotic used to treat bacterial infections including ear infections and strep throat.', 'BATCH006', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Azithromycin 500mg', 'HealthPlus Suppliers', 18.75, 40, 10, '2025-08-15', 'TABLET', 'Macrolide antibiotic used for bacterial infections such as respiratory infections and skin infections.', 'BATCH007', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Ciprofloxacin 500mg', 'Global Pharma Solutions', 15.99, 35, 12, '2025-09-20', 'TABLET', 'Fluoroquinolone antibiotic used for urinary tract infections and respiratory infections.', 'BATCH008', 2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Doxycycline 100mg', 'PrimeMed Distributors', 14.25, 45, 15, '2025-07-31', 'CAPSULE', 'Tetracycline antibiotic used for various bacterial infections.', 'BATCH009', 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Cephalexin 500mg', 'BioCare Laboratories', 16.50, 38, 12, '2025-08-31', 'CAPSULE', 'Cephalosporin antibiotic for respiratory and skin infections.', 'BATCH010', 2, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Antihypertensives
('Lisinopril 10mg', 'PrimeMed Distributors', 22.50, 60, 20, '2026-03-31', 'TABLET', 'ACE inhibitor used to treat high blood pressure and heart failure.', 'BATCH011', 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Amlodipine 5mg', 'BioCare Laboratories', 19.99, 55, 18, '2026-02-28', 'TABLET', 'Calcium channel blocker used to treat high blood pressure and coronary artery disease.', 'BATCH012', 3, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Metoprolol 50mg', 'MediCorp Pharmaceuticals', 17.25, 45, 15, '2025-12-15', 'TABLET', 'Beta blocker used for high blood pressure, chest pain, and heart failure.', 'BATCH013', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Losartan 50mg', 'HealthPlus Suppliers', 21.75, 50, 16, '2026-01-31', 'TABLET', 'Angiotensin II receptor blocker for hypertension.', 'BATCH014', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Hydrochlorothiazide 25mg', 'Global Pharma Solutions', 8.99, 65, 20, '2025-11-30', 'TABLET', 'Diuretic used to treat high blood pressure and fluid retention.', 'BATCH015', 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),

-- Vitamins
('Vitamin C 1000mg', 'HealthPlus Suppliers', 8.75, 200, 30, '2026-03-31', 'TABLET', 'Essential vitamin for immune system support and antioxidant protection.', 'BATCH016', 4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Vitamin D3 2000IU', 'PrimeMed Distributors', 12.99, 180, 25, '2026-04-30', 'CAPSULE', 'Vitamin D supplement for bone health and immune function.', 'BATCH017', 4, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Multivitamin Complex', 'Global Pharma Solutions', 15.50, 120, 20, '2026-05-31', 'TABLET', 'Comprehensive multivitamin with essential vitamins and minerals for daily nutrition.', 'BATCH018', 4, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Vitamin B Complex', 'MediCorp Pharmaceuticals', 11.25, 150, 25, '2026-02-28', 'TABLET', 'B vitamin complex for energy production and nervous system health.', 'BATCH019', 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Iron Supplement 65mg', 'BioCare Laboratories', 6.99, 110, 20, '2025-12-31', 'TABLET', 'Iron supplement for treating and preventing iron deficiency anemia.', 'BATCH020', 4, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);