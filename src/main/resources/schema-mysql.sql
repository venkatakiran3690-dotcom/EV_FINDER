-- ============================================================================
-- SMART EV STATION FINDER, GEOFENCED PRE-BOOKING & BILLING MANAGEMENT SYSTEM
-- Relational Database Schema for MySQL 8.x
-- ============================================================================

CREATE DATABASE IF NOT EXISTS smartev_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smartev_db;

-- 1. COMPANIES TABLE
CREATE TABLE IF NOT EXISTS companies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL UNIQUE,
    registration_number VARCHAR(100) UNIQUE,
    contact_email VARCHAR(120),
    contact_phone VARCHAR(30),
    address VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. USERS TABLE
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    role VARCHAR(30) NOT NULL,
    company_id BIGINT,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_users_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE SET NULL,
    INDEX idx_users_email (email),
    INDEX idx_users_role (role)
) ENGINE=InnoDB;

-- 3. STATIONS TABLE
CREATE TABLE IF NOT EXISTS stations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    postal_code VARCHAR(20),
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    total_chargers INT NOT NULL DEFAULT 0,
    operational_status VARCHAR(30) NOT NULL DEFAULT 'OPERATIONAL',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_stations_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    INDEX idx_stations_city (city),
    INDEX idx_stations_coords (latitude, longitude)
) ENGINE=InnoDB;

-- 4. CHARGERS TABLE
CREATE TABLE IF NOT EXISTS chargers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_id BIGINT NOT NULL,
    charger_identifier VARCHAR(60) NOT NULL,
    charger_type VARCHAR(20) NOT NULL, -- AC, DC
    connector_type VARCHAR(50) NOT NULL, -- CCS2, Type 2, CHAdeMO, GB/T
    power_rating_kw DOUBLE NOT NULL,
    operational_status VARCHAR(30) NOT NULL DEFAULT 'OPERATIONAL', -- OPERATIONAL, OUT_OF_SERVICE
    availability_status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, OCCUPIED, RESERVED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chargers_station FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE,
    INDEX idx_chargers_status (station_id, availability_status)
) ENGINE=InnoDB;

-- 5. STATION MANAGER ASSIGNMENTS TABLE
CREATE TABLE IF NOT EXISTS station_manager_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    assigned_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sma_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_sma_station FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE,
    UNIQUE KEY uq_manager_station (user_id, station_id)
) ENGINE=InnoDB;

-- 6. VEHICLES TABLE
CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    make VARCHAR(60) NOT NULL,
    model VARCHAR(60) NOT NULL,
    model_year INT NOT NULL,
    license_plate VARCHAR(30) NOT NULL UNIQUE,
    battery_capacity_kwh DOUBLE NOT NULL,
    compatible_connector VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_vehicles_driver FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_vehicles_driver (driver_id)
) ENGINE=InnoDB;

-- 7. BOOKINGS TABLE
CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_code VARCHAR(40) NOT NULL UNIQUE,
    driver_id BIGINT NOT NULL,
    vehicle_id BIGINT,
    station_id BIGINT NOT NULL,
    charger_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    calculated_distance_km DOUBLE NOT NULL,
    token_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAYMENT', -- PENDING_PAYMENT, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED, EXPIRED
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bookings_driver FOREIGN KEY (driver_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL,
    CONSTRAINT fk_bookings_station FOREIGN KEY (station_id) REFERENCES stations(id),
    CONSTRAINT fk_bookings_charger FOREIGN KEY (charger_id) REFERENCES chargers(id),
    INDEX idx_booking_charger_time (charger_id, start_time, end_time),
    INDEX idx_booking_code (booking_code)
) ENGINE=InnoDB;

-- 8. CHARGING SESSIONS TABLE
CREATE TABLE IF NOT EXISTS charging_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL UNIQUE,
    charger_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    session_code VARCHAR(40) NOT NULL UNIQUE,
    start_time DATETIME NOT NULL,
    end_time DATETIME,
    energy_consumed_kwh DOUBLE NOT NULL DEFAULT 0.0,
    initial_battery_pct INT NOT NULL DEFAULT 20,
    current_battery_pct INT NOT NULL DEFAULT 20,
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, COMPLETED, STOPPED_EARLY
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sessions_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_sessions_charger FOREIGN KEY (charger_id) REFERENCES chargers(id),
    CONSTRAINT fk_sessions_driver FOREIGN KEY (driver_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- 9. PAYMENTS TABLE
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    transaction_reference VARCHAR(60) NOT NULL UNIQUE,
    amount DECIMAL(10, 2) NOT NULL,
    payment_type VARCHAR(30) NOT NULL DEFAULT 'DEMO_TOKEN',
    status VARCHAR(30) NOT NULL, -- SUCCESS, FAILED, REFUNDED
    payment_method_note VARCHAR(200),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_payments_driver FOREIGN KEY (driver_id) REFERENCES users(id),
    INDEX idx_payments_ref (transaction_reference)
) ENGINE=InnoDB;

-- 10. TARIFFS TABLE
CREATE TABLE IF NOT EXISTS tariffs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT NOT NULL,
    station_id BIGINT,
    charger_type VARCHAR(20) NOT NULL DEFAULT 'ALL',
    price_per_kwh DECIMAL(10, 2) NOT NULL,
    service_charge DECIMAL(10, 2) NOT NULL,
    tax_percentage DECIMAL(5, 2) NOT NULL DEFAULT 18.00,
    effective_from DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tariffs_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_tariffs_station FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 11. BILLS TABLE
CREATE TABLE IF NOT EXISTS bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    session_id BIGINT NOT NULL UNIQUE,
    booking_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    charger_id BIGINT NOT NULL,
    energy_consumed_kwh DOUBLE NOT NULL,
    price_per_kwh DECIMAL(10, 2) NOT NULL,
    energy_cost DECIMAL(10, 2) NOT NULL,
    service_charge DECIMAL(10, 2) NOT NULL,
    tax_rate_pct DECIMAL(5, 2) NOT NULL,
    tax_amount DECIMAL(10, 2) NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_status VARCHAR(30) NOT NULL DEFAULT 'PAID',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_bills_session FOREIGN KEY (session_id) REFERENCES charging_sessions(id),
    CONSTRAINT fk_bills_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_bills_driver FOREIGN KEY (driver_id) REFERENCES users(id),
    CONSTRAINT fk_bills_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_bills_station FOREIGN KEY (station_id) REFERENCES stations(id),
    CONSTRAINT fk_bills_charger FOREIGN KEY (charger_id) REFERENCES chargers(id),
    INDEX idx_bills_invoice (invoice_number)
) ENGINE=InnoDB;

-- 12. REVIEWS TABLE
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_id BIGINT NOT NULL,
    driver_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_station FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_driver FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 13. FAVORITES TABLE
CREATE TABLE IF NOT EXISTS favorites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    station_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_favorites_driver FOREIGN KEY (driver_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_favorites_station FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE,
    UNIQUE KEY uq_driver_station_fav (driver_id, station_id)
) ENGINE=InnoDB;

-- 14. NOTIFICATIONS TABLE
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    message VARCHAR(1000) NOT NULL,
    type VARCHAR(40) NOT NULL DEFAULT 'STATUS_UPDATE',
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 15. COMPLAINTS TABLE
CREATE TABLE IF NOT EXISTS complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    driver_id BIGINT NOT NULL,
    company_id BIGINT NOT NULL,
    station_id BIGINT,
    subject VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    resolution_notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME,
    CONSTRAINT fk_complaints_driver FOREIGN KEY (driver_id) REFERENCES users(id),
    CONSTRAINT fk_complaints_company FOREIGN KEY (company_id) REFERENCES companies(id),
    CONSTRAINT fk_complaints_station FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 16. MAINTENANCE RECORDS TABLE
CREATE TABLE IF NOT EXISTS maintenance_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    station_id BIGINT NOT NULL,
    charger_id BIGINT,
    company_id BIGINT NOT NULL,
    reported_by_user_id BIGINT,
    issue_description TEXT NOT NULL,
    priority VARCHAR(30) NOT NULL DEFAULT 'MEDIUM',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    scheduled_date DATETIME,
    completed_date DATETIME,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_maint_station FOREIGN KEY (station_id) REFERENCES stations(id) ON DELETE CASCADE,
    CONSTRAINT fk_maint_charger FOREIGN KEY (charger_id) REFERENCES chargers(id) ON DELETE SET NULL,
    CONSTRAINT fk_maint_company FOREIGN KEY (company_id) REFERENCES companies(id) ON DELETE CASCADE,
    CONSTRAINT fk_maint_reporter FOREIGN KEY (reported_by_user_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 17. ACTIVITY LOGS TABLE
CREATE TABLE IF NOT EXISTS activity_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    user_email VARCHAR(120),
    user_role VARCHAR(30),
    action VARCHAR(100) NOT NULL,
    details TEXT,
    ip_address VARCHAR(60),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_activity_timestamp (timestamp)
) ENGINE=InnoDB;
