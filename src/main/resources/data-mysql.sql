-- ============================================================================
-- SMART EV STATION FINDER - SAMPLE DEMONSTRATION SEED DATA
-- Default Password for all demo accounts: 'Admin@123', 'Tata@123', 'Zeon@123', 'Manager@123', 'Driver@123'
-- Hashes generated via Spring Security BCryptPasswordEncoder (strength 10)
-- ============================================================================

USE smartev_db;

-- 1. COMPANIES (CPOs)
INSERT INTO companies (id, name, registration_number, contact_email, contact_phone, address, status) VALUES
(1, 'Tata Power EZ Charge', 'CPO-IND-2022-001', 'support@tatapowerezcharge.com', '+91 1800 209 5161', 'Corporate Centre, 34 Sant Tukaram Road, Mumbai, Maharashtra 400009', 'ACTIVE'),
(2, 'Zeon Electric Charging', 'CPO-IND-2023-009', 'care@zeoncharging.com', '+91 99400 99400', 'Tech Park, Avinashi Road, Coimbatore, Tamil Nadu 641014', 'ACTIVE'),
(3, 'Jio-bp pulse', 'CPO-IND-2024-042', 'pulse@jiobp.com', '+91 1800 891 9000', 'Maker Maxity, BKC, Bandra East, Mumbai, Maharashtra 400051', 'ACTIVE')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. USERS
INSERT INTO users (id, email, password_hash, full_name, phone, role, company_id, status) VALUES
(1, 'admin@smartev.com', '$2a$10$dTlw2LLYg2t3jGO12o.GLun/Oz4mh9cCXu.t8hKEshhqHjUPHy0TS', 'Chief Platform Administrator', '+91 98765 00001', 'PLATFORM_ADMIN', NULL, 'ACTIVE'),
(2, 'tatapower@smartev.com', '$2a$10$K0pW3.fCLbYHNGeslh7GHOcUHRRkrb6ge7W5EomTwK/6g.XjPU7ae', 'Rajesh Sharma (Tata CPO Admin)', '+91 98765 00002', 'COMPANY_ADMIN', 1, 'ACTIVE'),
(3, 'zeon@smartev.com', '$2a$10$retgfgTehY/NWtR/jnMV6O7OlTXHnezYUEYiX8svvSVMQIOv6ChvO', 'Priya Natarajan (Zeon CPO Admin)', '+91 98765 00003', 'COMPANY_ADMIN', 2, 'ACTIVE'),
(4, 'manager.bangalore@smartev.com', '$2a$10$El37MjYvknbRgcXOnysDKO73ZtbL5lnsO4tedWtC5JdhqVz/a.ru6', 'Suresh Kumar (Hub Manager)', '+91 98765 00004', 'STATION_MANAGER', 1, 'ACTIVE'),
(5, 'driver@smartev.com', '$2a$10$hQ3abG0tvyE.Bow4srWzM.MwaWPT4QJve8Fg7ROVMKt1yy59y932K', 'Venkata Kiran', '+91 98765 43210', 'DRIVER', NULL, 'ACTIVE'),
(6, 'driver2@smartev.com', '$2a$10$hQ3abG0tvyE.Bow4srWzM.MwaWPT4QJve8Fg7ROVMKt1yy59y932K', 'Ananya Reddy', '+91 98765 43211', 'DRIVER', NULL, 'ACTIVE')
ON DUPLICATE KEY UPDATE email=VALUES(email), password_hash=VALUES(password_hash);

-- 3. VEHICLES
INSERT INTO vehicles (id, driver_id, make, model, model_year, license_plate, battery_capacity_kwh, compatible_connector) VALUES
(1, 5, 'Tata', 'Nexon EV Long Range', 2024, 'KA-01-EV-4096', 40.5, 'CCS2'),
(2, 5, 'MG', 'ZS EV Excite', 2023, 'KA-05-EV-8821', 50.3, 'CCS2'),
(3, 6, 'Hyundai', 'Ioniq 5 EV', 2024, 'TS-09-EV-1008', 72.6, 'CCS2')
ON DUPLICATE KEY UPDATE license_plate=VALUES(license_plate);

-- 4. STATIONS
INSERT INTO stations (id, company_id, name, address, city, state, postal_code, latitude, longitude, total_chargers, operational_status) VALUES
(1, 1, 'Tata Power EV Hub - Indiranagar', '100 Feet Road, Near HAL 2nd Stage, Indiranagar', 'Bangalore', 'Karnataka', '560038', 12.9784, 77.6408, 4, 'OPERATIONAL'),
(2, 2, 'Zeon Fast Charger - Electronic City Phase 1', 'Velankani Tech Park, Hosur Road, Electronic City', 'Bangalore', 'Karnataka', '560100', 12.8452, 77.6602, 3, 'OPERATIONAL'),
(3, 1, 'Tata Power - ITPL Tech Hub', 'International Tech Park, Whitefield Main Road', 'Bangalore', 'Karnataka', '560066', 12.9863, 77.7381, 3, 'OPERATIONAL'),
(4, 3, 'Jio-bp pulse Highway Oasis - Bidadi', 'NH 275 Bangalore-Mysore Expressway, Bidadi', 'Ramanagara', 'Karnataka', '562109', 12.7950, 77.3820, 4, 'OPERATIONAL'),
(5, 2, 'Zeon Super Fast Hub - Koramangala', '80 Feet Road, 5th Block, Near Sony World Signal', 'Bangalore', 'Karnataka', '560095', 12.9352, 77.6245, 3, 'OPERATIONAL'),
-- Hyderabad (6 Stations)
(6, 1, 'Tata Power EV Hub - Cyber Towers', 'Building 1, Hitec City Main Road, Madhapur', 'Hyderabad', 'Telangana', '500081', 17.4504, 78.3808, 4, 'OPERATIONAL'),
(7, 2, 'Zeon Electric Charging - Financial District Hub', 'WaveRock Tech Park, Nanakramguda, Gachibowli', 'Hyderabad', 'Telangana', '500032', 17.4156, 78.3427, 4, 'OPERATIONAL'),
(8, 3, 'Jio-bp pulse - Jubilee Hills Road 36', 'Plot 784, Road Number 36, CBI Colony, Jubilee Hills', 'Hyderabad', 'Telangana', '500033', 17.4319, 78.4073, 3, 'OPERATIONAL'),
(9, 1, 'Tata Power EV Hub - Banjara Hills', 'Near MLA Colony, Road No. 12, Banjara Hills', 'Hyderabad', 'Telangana', '500034', 17.4150, 78.4410, 4, 'OPERATIONAL'),
(10, 2, 'Zeon Fast Charger - Begumpet Main Road', 'Prakash Nagar Metro Pillar 1380, Begumpet', 'Hyderabad', 'Telangana', '500016', 17.4442, 78.4735, 3, 'OPERATIONAL'),
(11, 3, 'Jio-bp pulse Express Hub - Uppal Metro', 'Survey 214, Inner Ring Road, Uppal Junction', 'Hyderabad', 'Telangana', '500039', 17.3986, 78.5583, 4, 'OPERATIONAL'),
-- Chennai (5 Stations)
(12, 1, 'Tata Power EV Hub - OMR IT Expressway', 'Rajiv Gandhi Salai, Near Infosys Gate, Sholinganallur', 'Chennai', 'Tamil Nadu', '600119', 12.9010, 80.2279, 4, 'OPERATIONAL'),
(13, 2, 'Zeon Electric Charging - Guindy Olympia Hub', '1 SIDCO Industrial Estate, Olympia Tech Park, Guindy', 'Chennai', 'Tamil Nadu', '600032', 13.0102, 80.2038, 3, 'OPERATIONAL'),
(14, 3, 'Jio-bp pulse - Anna Nagar 2nd Avenue', 'Roundtana Circle, 2nd Avenue, Anna Nagar', 'Chennai', 'Tamil Nadu', '600040', 13.0850, 80.2101, 4, 'OPERATIONAL'),
(15, 1, 'Tata Power EV - T. Nagar Pondy Bazaar', 'Sir Thyagaraya Road, Near Panagal Park, T. Nagar', 'Chennai', 'Tamil Nadu', '600017', 13.0418, 80.2341, 3, 'OPERATIONAL'),
(16, 2, 'Zeon Fast Charger - Adyar Besant Avenue', 'Near Theosophical Society, Besant Avenue, Adyar', 'Chennai', 'Tamil Nadu', '600020', 13.0012, 80.2565, 3, 'OPERATIONAL'),
-- Vijayawada (3 Stations)
(17, 3, 'Jio-bp pulse Highway Oasis - Gannavaram Airport', 'NH 16 Vijayawada-Kolkata Highway, Gannavaram', 'Vijayawada', 'Andhra Pradesh', '521102', 16.5350, 80.7920, 4, 'OPERATIONAL'),
(18, 1, 'Tata Power EV Hub - Benz Circle', 'Near Jyothi Convention Centre, MG Road, Benz Circle', 'Vijayawada', 'Andhra Pradesh', '520010', 16.5002, 80.6480, 3, 'OPERATIONAL'),
(19, 2, 'Zeon Fast Charger - Governorpet Commercial', 'Besant Road Cross, Governorpet', 'Vijayawada', 'Andhra Pradesh', '520002', 16.5125, 80.6275, 3, 'OPERATIONAL'),
-- Visakhapatnam (3 Stations)
(20, 1, 'Tata Power EV Hub - Siripuram VIP Road', 'VIP Road, Siripuram, Waltair Uplands', 'Visakhapatnam', 'Andhra Pradesh', '530003', 17.7215, 83.3150, 4, 'OPERATIONAL'),
(21, 2, 'Zeon Electric Charging - RK Beach Road', 'Beach Road Promenade, Pandurangapuram', 'Visakhapatnam', 'Andhra Pradesh', '530002', 17.7120, 83.3240, 3, 'OPERATIONAL'),
(22, 3, 'Jio-bp pulse - Gajuwaka Steel Plant Corridor', 'Old Gajuwaka Junction, NH 16 Bypass', 'Visakhapatnam', 'Andhra Pradesh', '530026', 17.6912, 83.2104, 3, 'OPERATIONAL'),
-- Kurnool (3 Stations)
(23, 3, 'Jio-bp pulse Highway Oasis - NH 44 Kurnool', 'NH 44 Hyderabad-Bangalore Highway, Kurnool Bypass', 'Kurnool', 'Andhra Pradesh', '518002', 15.8050, 78.0210, 4, 'OPERATIONAL'),
(24, 1, 'Tata Power EV Hub - Raj Vihar Centre', 'Raj Vihar Circle, Main Road', 'Kurnool', 'Andhra Pradesh', '518001', 15.8281, 78.0373, 3, 'OPERATIONAL'),
(25, 2, 'Zeon Fast Charger - Bellary Road', 'Bellary Road, Near Collector Office', 'Kurnool', 'Andhra Pradesh', '518004', 15.8190, 78.0160, 3, 'OPERATIONAL'),
-- Tier-2 Cities (11 Stations)
(26, 1, 'Tata Power EV Hub - Alipiri Toll Gate', 'Alipiri Bypass Road, Near Garuda Statue', 'Tirupati', 'Andhra Pradesh', '517502', 13.6350, 79.4020, 3, 'OPERATIONAL'),
(27, 2, 'Zeon Fast Charger - Renigunta Airport Road', 'NH 71 Airport Highway, Renigunta', 'Tirupati', 'Andhra Pradesh', '517520', 13.6420, 79.5130, 3, 'OPERATIONAL'),
(28, 3, 'Jio-bp pulse - Grand Trunk Road', 'Mini Bypass Road, Ramji Nagar', 'Nellore', 'Andhra Pradesh', '524003', 14.4426, 79.9865, 3, 'OPERATIONAL'),
(29, 1, 'Tata Power EV Hub - Kazipet Main Road', 'Warangal-Hyderabad Road, Kazipet', 'Warangal', 'Telangana', '506001', 17.9784, 79.5630, 3, 'OPERATIONAL'),
(30, 2, 'Zeon Fast Charger - Subedari Hanamkonda', 'Subedari, Near Nayeem Nagar, Hanamkonda', 'Warangal', 'Telangana', '506002', 18.0020, 79.5780, 3, 'OPERATIONAL'),
(31, 3, 'Jio-bp pulse - Arundelpet Amaravathi Road', 'Arundelpet 5th Line, Near Old Bus Stand', 'Guntur', 'Andhra Pradesh', '522002', 16.3067, 80.4365, 3, 'OPERATIONAL'),
(32, 1, 'Tata Power EV Hub - Godavari Bund', 'Morampudi Junction, NH 16 Bypass', 'Rajahmundry', 'Andhra Pradesh', '533103', 17.0005, 81.7820, 3, 'OPERATIONAL'),
(33, 2, 'Zeon Super Fast Hub - Mysore Palace Ring Road', 'Ring Road Junction, Near Bannur Road', 'Mysuru', 'Karnataka', '570010', 12.3052, 76.6552, 3, 'OPERATIONAL'),
(34, 3, 'Jio-bp pulse - Hebbal Industrial Area', 'KRS Road, Hebbal Electronic City', 'Mysuru', 'Karnataka', '570016', 12.3580, 76.6020, 3, 'OPERATIONAL'),
(35, 1, 'Tata Power EV Hub - Gandhipuram Central', 'Cross Cut Road, Near Central Bus Stand, Gandhipuram', 'Coimbatore', 'Tamil Nadu', '641012', 11.0168, 76.9672, 4, 'OPERATIONAL'),
(36, 2, 'Zeon Electric Charging - Avinashi Road', 'Avinashi Road, Peelamedu', 'Coimbatore', 'Tamil Nadu', '641004', 11.0280, 77.0060, 4, 'OPERATIONAL')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 5. CHARGERS
INSERT INTO chargers (id, station_id, charger_identifier, charger_type, connector_type, power_rating_kw, operational_status, availability_status) VALUES
(1, 1, 'TP-INDIRA-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(2, 1, 'TP-INDIRA-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(3, 1, 'TP-INDIRA-AC-22KW-01', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(4, 1, 'TP-INDIRA-AC-7.4KW', 'AC', 'Type 2', 7.4, 'OUT_OF_SERVICE', 'UNAVAILABLE'),
(5, 2, 'ZN-ECITY-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(6, 2, 'ZN-ECITY-DC-60KW-02', 'DC', 'CHAdeMO', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(7, 2, 'ZN-ECITY-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(8, 3, 'TP-ITPL-DC-150KW-01', 'DC', 'CCS2', 150.0, 'OPERATIONAL', 'AVAILABLE'),
(9, 3, 'TP-ITPL-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(10, 3, 'TP-ITPL-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(11, 4, 'JBP-HWY-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(12, 4, 'JBP-HWY-DC-120KW-02', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(13, 4, 'JBP-HWY-DC-60KW-03', 'DC', 'GB/T', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(14, 4, 'JBP-HWY-AC-22KW-04', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(15, 5, 'ZN-KORA-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(16, 5, 'ZN-KORA-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(17, 5, 'ZN-KORA-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Hyderabad Station 6 (Cyber Towers)
(18, 6, 'TP-HYD-HITEC-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(19, 6, 'TP-HYD-HITEC-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(20, 6, 'TP-HYD-HITEC-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(21, 6, 'TP-HYD-HITEC-AC-7.4KW-04', 'AC', 'Type 2', 7.4, 'OPERATIONAL', 'AVAILABLE'),
-- Hyderabad Station 7 (Financial District)
(22, 7, 'ZN-HYD-GACH-DC-150KW-01', 'DC', 'CCS2', 150.0, 'OPERATIONAL', 'AVAILABLE'),
(23, 7, 'ZN-HYD-GACH-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(24, 7, 'ZN-HYD-GACH-DC-60KW-03', 'DC', 'CHAdeMO', 60.0, 'OPERATIONAL', 'OCCUPIED'),
(25, 7, 'ZN-HYD-GACH-AC-22KW-04', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Hyderabad Station 8 (Jubilee Hills)
(26, 8, 'JBP-HYD-JUB-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(27, 8, 'JBP-HYD-JUB-DC-30KW-02', 'DC', 'CCS2', 30.0, 'OPERATIONAL', 'AVAILABLE'),
(28, 8, 'JBP-HYD-JUB-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Hyderabad Station 9 (Banjara Hills)
(29, 9, 'TP-HYD-BANJ-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(30, 9, 'TP-HYD-BANJ-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'OCCUPIED'),
(31, 9, 'TP-HYD-BANJ-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(32, 9, 'TP-HYD-BANJ-AC-7.4KW-04', 'AC', 'Type 2', 7.4, 'OUT_OF_SERVICE', 'UNAVAILABLE'),
-- Hyderabad Station 10 (Begumpet)
(33, 10, 'ZN-HYD-BEG-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(34, 10, 'ZN-HYD-BEG-AC-22KW-02', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(35, 10, 'ZN-HYD-BEG-AC-7.4KW-03', 'AC', 'Type 2', 7.4, 'OPERATIONAL', 'AVAILABLE'),
-- Hyderabad Station 11 (Uppal)
(36, 11, 'JBP-HYD-UPP-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(37, 11, 'JBP-HYD-UPP-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(38, 11, 'JBP-HYD-UPP-DC-60KW-03', 'DC', 'GB/T', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(39, 11, 'JBP-HYD-UPP-AC-22KW-04', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Chennai Station 12 (OMR Expressway)
(40, 12, 'TP-CHN-OMR-DC-150KW-01', 'DC', 'CCS2', 150.0, 'OPERATIONAL', 'AVAILABLE'),
(41, 12, 'TP-CHN-OMR-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(42, 12, 'TP-CHN-OMR-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(43, 12, 'TP-CHN-OMR-AC-7.4KW-04', 'AC', 'Type 2', 7.4, 'OPERATIONAL', 'AVAILABLE'),
-- Chennai Station 13 (Guindy)
(44, 13, 'ZN-CHN-GUIN-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(45, 13, 'ZN-CHN-GUIN-DC-60KW-02', 'DC', 'CHAdeMO', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(46, 13, 'ZN-CHN-GUIN-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Chennai Station 14 (Anna Nagar)
(47, 14, 'JBP-CHN-ANNA-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(48, 14, 'JBP-CHN-ANNA-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'OCCUPIED'),
(49, 14, 'JBP-CHN-ANNA-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(50, 14, 'JBP-CHN-ANNA-AC-7.4KW-04', 'AC', 'Type 2', 7.4, 'OPERATIONAL', 'AVAILABLE'),
-- Chennai Station 15 (T. Nagar)
(51, 15, 'TP-CHN-TNAG-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(52, 15, 'TP-CHN-TNAG-DC-30KW-02', 'DC', 'CCS2', 30.0, 'OPERATIONAL', 'AVAILABLE'),
(53, 15, 'TP-CHN-TNAG-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Chennai Station 16 (Adyar)
(54, 16, 'ZN-CHN-ADY-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(55, 16, 'ZN-CHN-ADY-AC-22KW-02', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(56, 16, 'ZN-CHN-ADY-AC-7.4KW-03', 'AC', 'Type 2', 7.4, 'OPERATIONAL', 'AVAILABLE'),
-- Vijayawada Station 17 (Gannavaram)
(57, 17, 'JBP-VJA-GAN-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(58, 17, 'JBP-VJA-GAN-DC-120KW-02', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(59, 17, 'JBP-VJA-GAN-DC-60KW-03', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(60, 17, 'JBP-VJA-GAN-AC-22KW-04', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Vijayawada Station 18 (Benz Circle)
(61, 18, 'TP-VJA-BENZ-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(62, 18, 'TP-VJA-BENZ-DC-30KW-02', 'DC', 'CCS2', 30.0, 'OPERATIONAL', 'AVAILABLE'),
(63, 18, 'TP-VJA-BENZ-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Vijayawada Station 19 (Governorpet)
(64, 19, 'ZN-VJA-GOV-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(65, 19, 'ZN-VJA-GOV-AC-22KW-02', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(66, 19, 'ZN-VJA-GOV-AC-7.4KW-03', 'AC', 'Type 2', 7.4, 'OPERATIONAL', 'AVAILABLE'),
-- Visakhapatnam Station 20 (Siripuram)
(67, 20, 'TP-VIZ-SIRI-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(68, 20, 'TP-VIZ-SIRI-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(69, 20, 'TP-VIZ-SIRI-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(70, 20, 'TP-VIZ-SIRI-AC-7.4KW-04', 'AC', 'Type 2', 7.4, 'OPERATIONAL', 'AVAILABLE'),
-- Visakhapatnam Station 21 (RK Beach)
(71, 21, 'ZN-VIZ-BEACH-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(72, 21, 'ZN-VIZ-BEACH-DC-60KW-02', 'DC', 'CHAdeMO', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(73, 21, 'ZN-VIZ-BEACH-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Visakhapatnam Station 22 (Gajuwaka)
(74, 22, 'JBP-VIZ-GAJU-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(75, 22, 'JBP-VIZ-GAJU-DC-60KW-02', 'DC', 'GB/T', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(76, 22, 'JBP-VIZ-GAJU-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Kurnool Station 23 (NH 44 Bypass)
(77, 23, 'JBP-KRN-NH44-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(78, 23, 'JBP-KRN-NH44-DC-120KW-02', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(79, 23, 'JBP-KRN-NH44-DC-60KW-03', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(80, 23, 'JBP-KRN-NH44-AC-22KW-04', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Kurnool Station 24 (Raj Vihar)
(81, 24, 'TP-KRN-RAJ-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(82, 24, 'TP-KRN-RAJ-DC-30KW-02', 'DC', 'CCS2', 30.0, 'OPERATIONAL', 'AVAILABLE'),
(83, 24, 'TP-KRN-RAJ-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Kurnool Station 25 (Bellary Road)
(84, 25, 'ZN-KRN-BEL-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(85, 25, 'ZN-KRN-BEL-AC-22KW-02', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(86, 25, 'ZN-KRN-BEL-AC-7.4KW-03', 'AC', 'Type 2', 7.4, 'OPERATIONAL', 'AVAILABLE'),
-- Tirupati Station 26 (Alipiri Toll)
(87, 26, 'TP-TPT-ALI-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(88, 26, 'TP-TPT-ALI-DC-30KW-02', 'DC', 'CCS2', 30.0, 'OPERATIONAL', 'AVAILABLE'),
(89, 26, 'TP-TPT-ALI-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Tirupati Station 27 (Renigunta)
(90, 27, 'ZN-TPT-RENI-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(91, 27, 'ZN-TPT-RENI-AC-22KW-02', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(92, 27, 'ZN-TPT-RENI-AC-7.4KW-03', 'AC', 'Type 2', 7.4, 'OPERATIONAL', 'AVAILABLE'),
-- Nellore Station 28 (Grand Trunk Road)
(93, 28, 'JBP-NLR-GT-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(94, 28, 'JBP-NLR-GT-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(95, 28, 'JBP-NLR-GT-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Warangal Station 29 (Kazipet)
(96, 29, 'TP-WGL-KZP-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(97, 29, 'TP-WGL-KZP-DC-30KW-02', 'DC', 'CCS2', 30.0, 'OPERATIONAL', 'AVAILABLE'),
(98, 29, 'TP-WGL-KZP-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Warangal Station 30 (Subedari)
(99, 30, 'ZN-WGL-SUB-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(100, 30, 'ZN-WGL-SUB-AC-22KW-02', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(101, 30, 'ZN-WGL-SUB-AC-7.4KW-03', 'AC', 'Type 2', 7.4, 'OPERATIONAL', 'AVAILABLE'),
-- Guntur Station 31 (Arundelpet)
(102, 31, 'JBP-GNT-ARUN-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(103, 31, 'JBP-GNT-ARUN-DC-30KW-02', 'DC', 'CCS2', 30.0, 'OPERATIONAL', 'AVAILABLE'),
(104, 31, 'JBP-GNT-ARUN-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Rajahmundry Station 32 (Godavari Bund)
(105, 32, 'TP-RJY-MOR-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(106, 32, 'TP-RJY-MOR-DC-30KW-02', 'DC', 'CCS2', 30.0, 'OPERATIONAL', 'AVAILABLE'),
(107, 32, 'TP-RJY-MOR-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Mysuru Station 33 (Palace Ring Road)
(108, 33, 'ZN-MYS-ORR-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(109, 33, 'ZN-MYS-ORR-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(110, 33, 'ZN-MYS-ORR-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Mysuru Station 34 (Hebbal Industrial Area)
(111, 34, 'JBP-MYS-HEB-DC-60KW-01', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(112, 34, 'JBP-MYS-HEB-DC-30KW-02', 'DC', 'CCS2', 30.0, 'OPERATIONAL', 'AVAILABLE'),
(113, 34, 'JBP-MYS-HEB-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
-- Coimbatore Station 35 (Gandhipuram)
(114, 35, 'TP-CBE-GAND-DC-120KW-01', 'DC', 'CCS2', 120.0, 'OPERATIONAL', 'AVAILABLE'),
(115, 35, 'TP-CBE-GAND-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(116, 35, 'TP-CBE-GAND-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE'),
(117, 35, 'TP-CBE-GAND-AC-7.4KW-04', 'AC', 'Type 2', 7.4, 'OPERATIONAL', 'AVAILABLE'),
-- Coimbatore Station 36 (Avinashi Road)
(118, 36, 'ZN-CBE-PEEL-DC-150KW-01', 'DC', 'CCS2', 150.0, 'OPERATIONAL', 'AVAILABLE'),
(119, 36, 'ZN-CBE-PEEL-DC-60KW-02', 'DC', 'CCS2', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(120, 36, 'ZN-CBE-PEEL-DC-60KW-03', 'DC', 'CHAdeMO', 60.0, 'OPERATIONAL', 'AVAILABLE'),
(121, 36, 'ZN-CBE-PEEL-AC-22KW-04', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE')
ON DUPLICATE KEY UPDATE charger_identifier=VALUES(charger_identifier);

-- 6. TARIFFS
INSERT INTO tariffs (id, company_id, station_id, charger_type, price_per_kwh, service_charge, tax_percentage, effective_from, status) VALUES
(1, 1, NULL, 'ALL', 19.50, 25.00, 18.00, NOW(), 'ACTIVE'),
(2, 2, NULL, 'ALL', 21.00, 30.00, 18.00, NOW(), 'ACTIVE'),
(3, 3, NULL, 'ALL', 18.50, 20.00, 18.00, NOW(), 'ACTIVE')
ON DUPLICATE KEY UPDATE price_per_kwh=VALUES(price_per_kwh);

-- 7. STATION MANAGER ASSIGNMENTS
INSERT INTO station_manager_assignments (id, user_id, station_id, assigned_at) VALUES
(1, 4, 1, NOW())
ON DUPLICATE KEY UPDATE station_id=VALUES(station_id);

-- 8. INITIAL ACTIVITY LOGS
INSERT INTO activity_logs (id, user_id, user_email, user_role, action, details, ip_address, timestamp) VALUES
(1, 1, 'system@smartev.com', 'SYSTEM', 'Application Startup & Data Seeding Complete', 'Verified 3 CPOs, 36 stations, 121 chargers across 14 cities, and regulated tariffs.', '127.0.0.1', '2026-11-07 10:00:00'),
(2, 5, 'driver@smartev.com', 'DRIVER', 'Driver Account Onboarded', 'Registered verified driver profile with vehicle KA-01-EV-4096.', '127.0.0.1', '2026-11-07 10:05:00'),
(3, 4, 'manager.bangalore@smartev.com', 'STATION_MANAGER', 'Hub Station Assignment Initialized', 'Assigned operational management for Tata Power EV Hub - Indiranagar.', '127.0.0.1', '2026-11-07 10:10:00')
ON DUPLICATE KEY UPDATE action=VALUES(action);
