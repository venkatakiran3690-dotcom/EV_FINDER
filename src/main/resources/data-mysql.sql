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
(1, 'admin@smartev.com', '$2a$10$vG.e/N97gH5u2tBskU6/G.bWbK2V/J/8eC77mYQOvy59gq9w0m4o2', 'Chief Platform Administrator', '+91 98765 00001', 'PLATFORM_ADMIN', NULL, 'ACTIVE'),
(2, 'tatapower@smartev.com', '$2a$10$0zX7g25Hj8Q0mPqJ8zLwDeM9q47r2EwM4jE5yG9p1xK3r5t8y7u6i', 'Rajesh Sharma (Tata CPO Admin)', '+91 98765 00002', 'COMPANY_ADMIN', 1, 'ACTIVE'),
(3, 'zeon@smartev.com', '$2a$10$9vB7g25Hj8Q0mPqJ8zLwDeM9q47r2EwM4jE5yG9p1xK3r5t8y7u6i', 'Priya Natarajan (Zeon CPO Admin)', '+91 98765 00003', 'COMPANY_ADMIN', 2, 'ACTIVE'),
(4, 'manager.bangalore@smartev.com', '$2a$10$8wA7g25Hj8Q0mPqJ8zLwDeM9q47r2EwM4jE5yG9p1xK3r5t8y7u6i', 'Suresh Kumar (Hub Manager)', '+91 98765 00004', 'STATION_MANAGER', 1, 'ACTIVE'),
(5, 'driver@smartev.com', '$2a$10$7vC7g25Hj8Q0mPqJ8zLwDeM9q47r2EwM4jE5yG9p1xK3r5t8y7u6i', 'Venkata Kiran', '+91 98765 43210', 'DRIVER', NULL, 'ACTIVE'),
(6, 'driver2@smartev.com', '$2a$10$7vC7g25Hj8Q0mPqJ8zLwDeM9q47r2EwM4jE5yG9p1xK3r5t8y7u6i', 'Ananya Reddy', '+91 98765 43211', 'DRIVER', NULL, 'ACTIVE')
ON DUPLICATE KEY UPDATE email=VALUES(email);

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
(5, 2, 'Zeon Super Fast Hub - Koramangala', '80 Feet Road, 5th Block, Near Sony World Signal', 'Bangalore', 'Karnataka', '560095', 12.9352, 77.6245, 3, 'OPERATIONAL')
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
(17, 5, 'ZN-KORA-AC-22KW-03', 'AC', 'Type 2', 22.0, 'OPERATIONAL', 'AVAILABLE')
ON DUPLICATE KEY UPDATE charger_identifier=VALUES(charger_identifier);

-- 6. TARIFFS
INSERT INTO tariffs (id, company_id, station_id, charger_type, price_per_kwh, service_charge, tax_percentage, effective_from, status) VALUES
(1, 1, NULL, 'ALL', 19.50, 25.00, 18.00, NOW(), 'ACTIVE'),
(2, 2, NULL, 'ALL', 21.00, 30.00, 18.00, NOW(), 'ACTIVE'),
(3, 3, NULL, 'ALL', 18.50, 20.00, 18.00, NOW(), 'ACTIVE')
ON DUPLICATE KEY UPDATE price_per_kwh=VALUES(price_per_kwh);
