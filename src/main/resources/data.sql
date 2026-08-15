-- Clear existing data
DELETE FROM sales;
DELETE FROM medications;
DELETE FROM categories;
DELETE FROM suppliers;
DELETE FROM users;

-- Insert seed Admin user (password: Admin123! hashed with BCrypt)
INSERT INTO users (id, full_name, email, password, role, created_at) VALUES
(1, 'Dr. Sarah Johnson', 'admin@pharmacare.com', '$2a$10$wT5UfT7Y12mN/Q0Zl1C7UeM.J4P9Jk2X7n9/p8R7a7s8t9u0v1w2x', 'ADMIN', CURRENT_TIMESTAMP);

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

-- Insert sample medications with realistic expiry dates
-- Note: Dates configured to test Expiry Alerts (Expired: < CURRENT_DATE, Expiring in 7 days, Expiring in 30 days, Safe: > 30 days)
INSERT INTO medications (id, name, manufacturer, price, quantity, low_stock_threshold, expiration_date, type, description, batch_number, category_id, supplier_id, created_at, updated_at) VALUES
(1, 'Paracetamol 500mg', 'MediCorp Pharmaceuticals', 5.99, 100, 20, DATEADD('DAY', -10, CURRENT_DATE), 'TABLET', 'Pain reliever and fever reducer. Used for headaches, muscle aches, arthritis, backaches, toothaches, colds, and fevers.', 'BATCH001', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Amoxicillin 250mg', 'MediCorp Pharmaceuticals', 12.50, 50, 15, DATEADD('DAY', -2, CURRENT_DATE), 'CAPSULE', 'Broad-spectrum antibiotic used to treat bacterial infections including ear infections and strep throat.', 'BATCH006', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Ibuprofen 400mg', 'HealthPlus Suppliers', 8.50, 75, 15, DATEADD('DAY', 3, CURRENT_DATE), 'TABLET', 'Nonsteroidal anti-inflammatory drug (NSAID) used to treat pain, fever, and inflammation.', 'BATCH002', 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Cough Syrup 100ml', 'CureAll', 18.99, 25, 8, DATEADD('DAY', 5, CURRENT_DATE), 'SYRUP', 'Cough suppressant and soothing throat syrup.', 'CS2024005', 20, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Azithromycin 500mg', 'HealthPlus Suppliers', 18.75, 40, 10, DATEADD('DAY', 12, CURRENT_DATE), 'TABLET', 'Macrolide antibiotic used for bacterial infections such as respiratory infections and skin infections.', 'BATCH007', 2, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Ciprofloxacin 500mg', 'Global Pharma Solutions', 15.99, 35, 12, DATEADD('DAY', 18, CURRENT_DATE), 'TABLET', 'Fluoroquinolone antibiotic used for urinary tract infections and respiratory infections.', 'BATCH008', 2, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Doxycycline 100mg', 'PrimeMed Distributors', 14.25, 45, 15, DATEADD('DAY', 25, CURRENT_DATE), 'CAPSULE', 'Tetracycline antibiotic used for various bacterial infections.', 'BATCH009', 2, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'Cephalexin 500mg', 'BioCare Laboratories', 16.50, 38, 12, DATEADD('DAY', 29, CURRENT_DATE), 'CAPSULE', 'Cephalosporin antibiotic for respiratory and skin infections.', 'BATCH010', 2, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Aspirin 325mg', 'PrimeMed Distributors', 4.25, 150, 25, DATEADD('DAY', 90, CURRENT_DATE), 'TABLET', 'Used to treat pain, fever, or inflammation. Also used as blood thinner.', 'BATCH003', 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Naproxen 250mg', 'Global Pharma Solutions', 12.75, 60, 12, DATEADD('DAY', 120, CURRENT_DATE), 'TABLET', 'NSAID for pain relief, inflammation, and fever reduction.', 'BATCH004', 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Diclofenac 50mg', 'BioCare Laboratories', 9.25, 80, 18, DATEADD('DAY', 150, CURRENT_DATE), 'TABLET', 'NSAID used for pain and inflammatory diseases like arthritis.', 'BATCH005', 1, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Lisinopril 10mg', 'PrimeMed Distributors', 22.50, 60, 20, DATEADD('DAY', 180, CURRENT_DATE), 'TABLET', 'ACE inhibitor used to treat high blood pressure and heart failure.', 'BATCH011', 3, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(13, 'Amlodipine 5mg', 'BioCare Laboratories', 19.99, 55, 18, DATEADD('DAY', 200, CURRENT_DATE), 'TABLET', 'Calcium channel blocker used to treat high blood pressure and coronary artery disease.', 'BATCH012', 3, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(14, 'Metoprolol 50mg', 'MediCorp Pharmaceuticals', 17.25, 45, 15, DATEADD('DAY', 220, CURRENT_DATE), 'TABLET', 'Beta blocker used for high blood pressure, chest pain, and heart failure.', 'BATCH013', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(15, 'Losartan 50mg', 'HealthPlus Suppliers', 21.75, 50, 16, DATEADD('DAY', 250, CURRENT_DATE), 'TABLET', 'Angiotensin II receptor blocker for hypertension.', 'BATCH014', 3, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(16, 'Hydrochlorothiazide 25mg', 'Global Pharma Solutions', 8.99, 65, 20, DATEADD('DAY', 280, CURRENT_DATE), 'TABLET', 'Diuretic used to treat high blood pressure and fluid retention.', 'BATCH015', 3, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(17, 'Vitamin C 1000mg', 'HealthPlus Suppliers', 8.75, 200, 30, DATEADD('DAY', 300, CURRENT_DATE), 'TABLET', 'Essential vitamin for immune system support and antioxidant protection.', 'BATCH016', 4, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(18, 'Vitamin D3 2000IU', 'PrimeMed Distributors', 12.99, 180, 25, DATEADD('DAY', 320, CURRENT_DATE), 'CAPSULE', 'Vitamin D supplement for bone health and immune function.', 'BATCH017', 4, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(19, 'Multivitamin Complex', 'Global Pharma Solutions', 15.50, 120, 20, DATEADD('DAY', 350, CURRENT_DATE), 'TABLET', 'Comprehensive multivitamin with essential vitamins and minerals for daily nutrition.', 'BATCH018', 4, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(20, 'Vitamin B Complex', 'MediCorp Pharmaceuticals', 11.25, 150, 25, DATEADD('DAY', 365, CURRENT_DATE), 'TABLET', 'B vitamin complex for energy production and nervous system health.', 'BATCH019', 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Insert sample sales records
INSERT INTO sales (sale_date, quantity, unit_price, cost_price, total_price, medication_id, created_at) VALUES
(CURRENT_DATE, 5, 5.99, 4.19, 29.95, 1, CURRENT_TIMESTAMP),
(CURRENT_DATE, 2, 12.50, 8.75, 25.00, 2, CURRENT_TIMESTAMP),
(DATEADD('DAY', -1, CURRENT_DATE), 3, 8.50, 5.95, 25.50, 3, CURRENT_TIMESTAMP),
(DATEADD('DAY', -2, CURRENT_DATE), 4, 18.75, 13.12, 75.00, 5, CURRENT_TIMESTAMP),
(DATEADD('DAY', -3, CURRENT_DATE), 6, 5.99, 4.19, 35.94, 1, CURRENT_TIMESTAMP),
(DATEADD('DAY', -4, CURRENT_DATE), 2, 22.50, 15.75, 45.00, 12, CURRENT_TIMESTAMP),
(DATEADD('DAY', -5, CURRENT_DATE), 10, 8.75, 6.12, 87.50, 17, CURRENT_TIMESTAMP),
(DATEADD('DAY', -6, CURRENT_DATE), 1, 15.99, 11.19, 15.99, 6, CURRENT_TIMESTAMP),
(DATEADD('MONTH', -1, CURRENT_DATE), 15, 5.99, 4.19, 89.85, 1, CURRENT_TIMESTAMP),
(DATEADD('MONTH', -2, CURRENT_DATE), 20, 8.50, 5.95, 170.00, 3, CURRENT_TIMESTAMP);
