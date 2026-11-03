# Smart EV Station Finder, Geofenced Pre-Booking & Billing Management System
> **A Centralized Web Application for Urban & Highway Electric Vehicle Infrastructure**  
> *Final-Year B.Tech Computer Science & Engineering Capstone Project*

---

## 1. Project Overview & Abstract

The **Smart EV Station Finder, Geofenced Pre-Booking & Billing Management System** is an enterprise-grade, multi-tenant web application engineered to solve critical bottlenecks in contemporary electric vehicle (EV) charging ecosystems: charger discovery latency, range anxiety, queuing bottlenecks, unauthorized slot hogging, and opaque billing.

Built with **Java 21 LTS**, **Spring Boot 3.3.5**, **MySQL 8.x**, and a responsive frontend powered by **Bootstrap 5**, **Leaflet.js**, and **Chart.js**, the platform centralizes EV charging management across multiple independent Charging Point Operators (CPOs such as Tata Power and Zeon Charging) under strict multi-tenant data isolation.

### Key Architectural Highlights
1. **Server-Side Haversine 30 km Geofencing:** Prevents speculative hoarding of charging slots by enforcing an algorithmic mathematical distance boundary computed strictly on the backend.
2. **Conflict-Safe Concurrency Slot Pre-Booking:** Employs interval overlap detection (`existingStart < reqEnd && existingEnd > reqStart`) with transactional locking to eliminate double-booking race conditions.
3. **Simulated Demo Token Payments:** Provides an interactive financial transaction simulator supporting success and failure demo paths with token deductions and status updates.
4. **Deterministic Simulated Charging Engine:** Accurately computes energy delivered (kWh) and battery State of Charge (% SoC) using charger power ratings (kW) and elapsed time, explicitly avoiding fabricated IoT hardware claims.
5. **Transparent Dynamic Tariff Calculation:** Calculates base energy charges, parking/service fees, and 18% GST using Java `BigDecimal` with half-up rounding to prevent floating-point financial drift.
6. **Downloadable OpenPDF Invoices:** Generates standardized, professional PDF tax invoices on-the-fly containing complete breakdown, token adjustment, and unique invoice numbers.
7. **Four Role-Based Dashboards:** Dedicated role-gated interfaces for Platform Admins, Company Admins, Station Managers, and EV Drivers.

---

## 2. Technology Stack

| Layer | Technologies & Libraries | Rationale |
| :--- | :--- | :--- |
| **Backend Framework** | **Java 21 LTS**, **Spring Boot 3.3.5** | High-performance enterprise backend with modern virtual threads support and Spring ecosystem. |
| **Persistence & ORM** | **Spring Data JPA**, **Hibernate 6.5**, **MySQL 8.x** | Automated mapping, relational constraints, foreign keys, UTF-8 collation, and transactional guarantees. |
| **Fallback / Test DB**| **H2 In-Memory Database** (`dev-h2` profile) | Enables zero-configuration standalone evaluation without requiring local MySQL daemon running. |
| **Security & Auth** | **Spring Security 6**, **JJWT 0.12.6**, **BCrypt** | Stateless JWT authentication, role-based authorization filters, multi-tenant company isolation. |
| **PDF Generation** | **OpenPDF 1.3.40** (`com.github.librepdf:openpdf`) | Robust, LGPL/MPL compliant server-side PDF invoice generation with tables, headers, and metadata. |
| **Frontend UI** | **HTML5**, **CSS3**, **Bootstrap 5.3**, **Bootstrap Icons** | Responsive, modern, clean user interface with zero external build-tool dependencies. |
| **Interactive Maps** | **Leaflet.js 1.9.4** with **OpenStreetMap** | Open-source tile rendering, dynamic station pin markers, custom popup cards, distance badges. |
| **Data Visualizations**| **Chart.js 4.4.1** | Real-time doughnut charts for charger occupancy and revenue distribution. |
| **Build & Testing** | **Apache Maven 3.9.9**, **JUnit 5**, **Mockito**, **MockMvc** | Standardized lifecycle build, repeatable offline dependency caching, automated regression test suite. |

---

## 3. System Architecture & Multi-Tenant Design

```
+---------------------------------------------------------------------------------------+
|                                    PRESENTATION LAYER                                 |
|   Bootstrap 5 + Vanilla JavaScript (Fetch API + JWT Bearer) + Leaflet.js + Chart.js   |
|   Role Dashboards: Driver | Station Manager | Company Admin | Platform Admin           |
+---------------------------------------------------------------------------------------+
                                           |  HTTPS / JSON REST APIs
                                           v
+---------------------------------------------------------------------------------------+
|                                SPRING SECURITY 6 FILTER CHAIN                         |
|   - JwtAuthenticationFilter (Extracts Bearer Token & Validates JJWT Signature)        |
|   - Role-Based Endpoint Authorizations (/api/admin/**, /api/company/**, /api/driver)  |
+---------------------------------------------------------------------------------------+
                                           |
                                           v
+---------------------------------------------------------------------------------------+
|                               REST CONTROLLER LAYER (11 Controllers)                  |
|   AuthController | StationController | BookingController | PaymentController          |
|   ChargingSessionController | BillingController | VehicleController | EngagementController|
|   DashboardController | TariffController | PlatformAdminController                     |
+---------------------------------------------------------------------------------------+
                                           |
                                           v
+---------------------------------------------------------------------------------------+
|                                BUSINESS SERVICE LAYER (10 Services)                   |
|   - GeofenceService (Haversine 30km Spherical Boundary Enforcement)                   |
|   - BookingService (Interval Overlap Detection & Transactional Slot Reservation)     |
|   - DemoPaymentService (Simulated Token Payment & Idempotency)                        |
|   - ChargingSessionService (Deterministic kWh Calculation from Charger Power)         |
|   - BillingService (BigDecimal Currency & Tax Calculation)                           |
|   - PdfInvoiceService (Server-Side OpenPDF Document Generation)                       |
|   - EngagementService (Reviews, Complaints, Notifications, Maintenance Orders)        |
|   - DashboardService (Role-Scoped Aggregations & Metrics)                             |
+---------------------------------------------------------------------------------------+
                                           |
                                           v
+---------------------------------------------------------------------------------------+
|                        SPRING DATA JPA & REPOSITORY LAYER (17 Repositories)           |
+---------------------------------------------------------------------------------------+
                                           |
                   +-----------------------+-----------------------+
                   |                                               |
                   v                                               v
        [ MySQL 8.x Database ]                         [ H2 In-Memory Database ]
          (Production / Default)                           (Demo / dev-h2 Profile)
```

---

## 4. Database Design & Entity Relationships

The relational model consists of **17 normalized tables** with explicit foreign keys, indexes, and constraints:

1. `companies`: Charging point operators (Tata Power, Zeon Charging, etc.) with registration codes and tax IDs.
2. `users`: System users with BCrypt password hashes, email uniqueness, role enumeration, and company association.
3. `stations`: Physical station entities with geographic coordinates (`latitude`, `longitude`), address, city, and company ownership.
4. `chargers`: Hardware units with identifier (e.g. `TP-BLR-01-A`), type (`AC`/`DC`), connector standard (`CCS2`, `Type 2`, `CHAdeMO`, `GB/T`), power rating (`kW`), and operational status.
5. `station_manager_assignments`: Scoped M:N assignments linking Station Managers to specific stations.
6. `vehicles`: Driver electric vehicles with make, model, battery capacity (`kWh`), connector type, and license plate.
7. `tariffs`: Active rate structures per station/charger type with base rates, energy rates per kWh, and parking fees.
8. `bookings`: Pre-booked slots with unique booking codes (`BK-XXXXXXXX`), start/end timestamps, distance audit, and token deposit.
9. `charging_sessions`: Simulated charging records tracking start/end times, energy delivered (`kWh`), initial SoC, final SoC, and status.
10. `payments`: Payment audit ledger tracking token deductions, mock transaction references (`TXN-XXXXXXXX`), and payment methods.
11. `bills`: Detailed tax invoices tracking units consumed, energy charges, tax amounts (18% GST), token deductions, and net payable.
12. `reviews`: Driver ratings (1 to 5 stars) and feedback comments per station.
13. `favorites`: Driver bookmarked stations for rapid discovery.
14. `notifications`: In-app event alerts (booking confirmations, session updates, payment receipts).
15. `complaints`: Driver complaint tickets with category, description, and operational status (`OPEN`, `RESOLVED`).
16. `maintenance_records`: Station work orders tracking charger maintenance, technician notes, and scheduled downtime.
17. `activity_logs`: Audit trail tracking administrative actions with IP address and timestamps.

---

## 5. User Roles and Default Credentials

The system provides 5 pre-seeded user personas covering all four supported roles:

| Role | Name | Email Address | Password | Company / Scope |
| :--- | :--- | :--- | :--- | :--- |
| **`PLATFORM_ADMIN`** | System Administrator | `admin@smartev.com` | `Admin@123` | Platform-wide (all companies) |
| **`COMPANY_ADMIN`** | Rajesh Tata (CPO Admin) | `tatapower.admin@demo.com` | `Tata@123` | Tata Power EV Network |
| **`COMPANY_ADMIN`** | Vikram Zeon (CPO Admin) | `zeon.admin@demo.com` | `Zeon@123` | Zeon Charging Network |
| **`STATION_MANAGER`**| Suresh Kumar | `manager.blr@demo.com` | `Manager@123` | Tata Power Indiranagar Hub |
| **`DRIVER`** | Rahul Sharma | `driver@demo.com` | `Driver@123` | EV Driver (Nexon EV, 40.5 kWh) |

> **Evaluator Quick-Fill Feature:** On the `login.html` page, click any of the 1-click credential buttons to automatically populate the credentials and navigate directly to the appropriate dashboard!

---

## 6. Prerequisites & Installation Guide

### Prerequisites
- **Java Development Kit (JDK):** Version 17 or Version 21 LTS (`java -version`).
- **Maven:** Version 3.8+ (A portable Maven 3.9.9 wrapper `mvnw.cmd` is pre-configured in the repository).
- **MySQL (Optional for default profile):** MySQL 8.x running on port `3306`. (If MySQL is not installed, the in-memory `dev-h2` profile runs out-of-the-box with zero setup).

---

### Method A: Running with In-Memory Demo Database (Zero Setup)
To evaluate the project instantly without installing or configuring MySQL:

```powershell
# Open terminal in the project directory
cd c:\Users\venka\Downloads\Sup_Charged_EV_Project\ev-charging-finder

# Run Spring Boot with the dev-h2 profile
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev-h2
```

The system will start up on `http://localhost:8080`, generate the full in-memory schema, seed all sample CPOs, 5 stations, 17 chargers, tariffs, and test accounts, and serve the web interface.

---

### Method B: Running with MySQL 8.x Database

1. **Create the MySQL Database:**
   ```sql
   CREATE DATABASE smartev_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. **Execute Database Scripts (Optional - Hibernate auto-creates if `ddl-auto: update` is kept):**
   ```powershell
   mysql -u root -p smartev_db < src/main/resources/schema-mysql.sql
   mysql -u root -p smartev_db < src/main/resources/data-mysql.sql
   ```

3. **Configure Environment Variables / Properties:**
   Edit `src/main/resources/application.properties` or set environment variables:
   ```properties
   DB_URL=jdbc:mysql://localhost:3306/smartev_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   DB_USERNAME=root
   DB_PASSWORD=your_mysql_password
   JWT_SECRET=SmartEVSecretKeyForBTechProjectCapstoneMustBeAtLeast256BitsLongForHMACSHA256
   ```

4. **Launch the Application:**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

5. **Open Web Browser:**
   Navigate to: **`http://localhost:8080`**

---

### Method C: Running the Standalone Executable JAR

The application packages into a self-contained executable JAR containing an embedded Apache Tomcat server:

```powershell
# Build the production JAR (offline-capable)
.\mvnw.cmd clean package -DskipTests

# Run the standalone JAR with H2 demo profile
java -jar -Dspring.profiles.active=dev-h2 target/ev-charging-system-1.0.0.jar

# Or run with MySQL (default profile)
java -jar target/ev-charging-system-1.0.0.jar
```

---

## 7. Running Automated Test Suites

The project includes unit, service, integration, and security test suites using JUnit 5 and MockMvc:

```powershell
# Run the full automated test suite
.\mvnw.cmd test -o
```

### Test Coverage Highlights
1. **`GeofenceServiceTest`:**
   - Accurate distance calculation for known coordinate pairs (e.g., Bengaluru Indiranagar to Whitefield = 11.75 km).
   - Same-coordinate zero-distance test.
   - 30 km boundary validation (distance <= 30 km allowed; > 30 km throws `GeofenceException`).
   - Coordinate validation (lat -90 to +90, lng -180 to +180).
2. **`BookingServiceTest`:**
   - Successful slot booking creation and booking code generation.
   - Slot conflict detection on overlapping time intervals.
   - Rejection of out-of-service chargers.
   - Rejection of bookings exceeding 30 km geofence.
   - 30-minute booking cancellation cutoff rule.
3. **`BillingServiceTest`:**
   - Accurate dynamic tariff computation using `BigDecimal` (base fee + energy fee + 18% GST - token deposit).
   - Prevention of duplicate bill generation for the same charging session.
4. **`DemoPaymentServiceTest`:**
   - Successful simulated token payment and status transition.
   - Controlled demo payment failure simulation for edge-case evaluation.
   - Idempotent payment processing preventing double-deduction.
5. **`SecurityAndIsolationTest`:**
   - Multi-tenant company isolation: Company Admin cannot access another company's stations.
   - Station Manager scope validation: Station Manager cannot manage unassigned stations.
6. **`StationAndAuthControllerTest` (MockMvc):**
   - End-to-end HTTP integration testing for public station discovery, driver registration, and JWT login.

---

## 8. Core Workflows & System Walkthrough

### 8.1 Station Discovery & Geolocation Filtering
1. Open `http://localhost:8080/station-search.html`.
2. The user's simulated GPS location defaults to Bengaluru City Center (`12.9716° N, 77.5946° E`).
3. Evaluators can select preset locations (Indiranagar, Electronic City, Whitefield, or Out-of-Range Mysore) from the dropdown or click **"Use My GPS"**.
4. Real-time Haversine distance is displayed on each station card.
5. Filter by AC / DC charger type, connector standard (CCS2, Type 2), power rating, and availability.
6. Map pins indicate live occupancy status (Green = Available, Red = Occupied, Grey = Maintenance).

### 8.2 Geofenced Slot Pre-Booking
1. Click **"Book Slot"** on any station within 30 km.
2. Select your registered EV, charger connector, date, start time, and reservation duration (e.g., 60 mins).
3. The server validates that:
   - The charger is in `AVAILABLE` status.
   - The user's coordinates are strictly $\le 30.00\text{ km}$ from the station.
   - No conflicting booking overlaps the requested interval.
4. The system calculates a token pre-authorization deposit (default ₹150.00) and creates a booking in `PENDING_PAYMENT` status.

### 8.3 Demo Token Payment
1. Navigate to **"My Bookings"** (`bookings.html`).
2. Click **"Pay Token Deposit"**.
3. Choose **"Simulate Successful Payment"** to transition booking to `CONFIRMED`.
4. (Optional) Choose **"Simulate Payment Failure"** to test graceful error handling and retry workflows.

### 8.4 Deterministic Simulated Charging Session
1. Once confirmed and within the reservation window, click **"Start Charging"**.
2. The charging monitor (`charging-session.html`) displays:
   - Live battery State of Charge (% SoC) animated gauge.
   - Energy delivered counter (kWh) progressing smoothly based on charger kW rating.
   - Time elapsed ticker.
3. Click **"Complete Charging"** to finalize the session.

### 8.5 Billing & OpenPDF Downloadable Invoices
1. Upon session completion, `BillingService` automatically generates a consolidated bill:
   $$\text{Subtotal} = \text{Base Fee} + (\text{kWh} \times \text{Tariff Rate}) + \text{Parking Fee}$$
   $$\text{GST} = \text{Subtotal} \times 0.18$$
   $$\text{Total Payable} = \text{Subtotal} + \text{GST} - \text{Token Deposit}$$
2. Navigate to **"Billing & Invoices"** (`billing.html`).
3. Click **"Download Tax Invoice (PDF)"** to download a styled PDF invoice generated server-side via OpenPDF.

### 8.6 Multi-Tenant Company Management
1. Log in as `tatapower.admin@demo.com`.
2. Notice the dashboard displays **only Tata Power stations, chargers, and revenue**.
3. Log in as `zeon.admin@demo.com`.
4. Notice Zeon Charging sees only Zeon stations; cross-company access via URL tampering is rejected with `403 Forbidden`.

---

## 9. Viva Voce & Technical Defense Guide

This section is curated specifically to assist final-year engineering students during project defense, external viva evaluations, and technical interviews.

---

### Q1: Why did you use the Haversine formula instead of the Euclidean distance formula, and why is it calculated server-side?
**Answer:**
1. **Spherical Geometry:** The Earth is an oblate spheroid, not a flat 2D plane. Euclidean distance ($d = \sqrt{\Delta x^2 + \Delta y^2}$) assumes flat Cartesian coordinates and causes significant distortion over geographic distances.
2. **Haversine Accuracy:** The Haversine formula computes the great-circle distance between two points on a sphere given their latitudes and longitudes:
   $$a = \sin^2\left(\frac{\Delta \phi}{2}\right) + \cos(\phi_1)\cos(\phi_2)\sin^2\left(\frac{\Delta \lambda}{2}\right)$$
   $$c = 2 \cdot \text{atan2}\left(\sqrt{a}, \sqrt{1-a}\right)$$
   $$d = R \cdot c \quad (\text{where } R = 6,371\text{ km})$$
3. **Server-Side Enforcement:** Calculating distance in the browser using client-side JavaScript is vulnerable to client tampering (e.g. modifying browser variables or bypassing client scripts). To maintain absolute data integrity and prevent fraudulent slot reservations beyond the 30 km radius, our `GeofenceService` re-computes and enforces the Haversine distance on the backend using validated coordinates.

---

### Q2: How does your system prevent double-booking race conditions when multiple users try to book the same charger at the same time?
**Answer:**
We implement a two-tier concurrency and overlap prevention mechanism:
1. **Mathematical Interval Overlap Condition:**
   Two time intervals $[S_1, E_1)$ and $[S_2, E_2)$ overlap if and only if:
   $$S_1 < E_2 \quad \text{AND} \quad E_1 > S_2$$
   In `BookingRepository.java`, we execute this JPQL query:
   ```sql
   SELECT COUNT(b) FROM Booking b
   WHERE b.charger.id = :chargerId
     AND b.status IN ('PENDING_PAYMENT', 'CONFIRMED')
     AND b.startTime < :endTime
     AND b.endTime > :startTime
   ```
2. **Transactional Isolation:** The reservation method is marked `@Transactional`. If the count is greater than zero, a `SlotConflictException` is thrown before any booking record is written to the database.

---

### Q3: How do you enforce multi-tenant company isolation and prevent unauthorized data access?
**Answer:**
1. **Authenticated Context Resolution:** Rather than accepting a `companyId` from client URL query parameters or request bodies (which can be easily spoofed), the backend resolves the authenticated user's company identity directly from the validated Spring Security `UserPrincipal` extracted from the cryptographically signed JWT token.
2. **Repository-Level Scoping:** All company administration endpoints (`/api/company/**`) enforce:
   ```java
   Long companyId = currentUser.getCompanyId();
   List<Station> stations = stationRepository.findByCompanyId(companyId);
   ```
   If a user attempts to view or update a station or charger that belongs to a different `companyId`, the server immediately throws an `UnauthorizedException` and returns HTTP `403 Forbidden`.

---

### Q4: Why did you use `BigDecimal` instead of `float` or `double` for billing and financial arithmetic?
**Answer:**
1. **Binary Floating-Point Imprecision:** Primitive floating-point types (`float` and `double`) represent numbers using binary fractions (IEEE 754 standard). Certain decimal fractions like `0.1` or `0.05` cannot be represented exactly in binary floating-point, leading to compounding rounding errors (e.g., `0.1 + 0.2 = 0.30000000000000004`).
2. **Financial Exactness:** In commercial billing and tax calculations (e.g. 18% GST), every fraction of a paisa must be accounted for accurately. `BigDecimal` provides arbitrary-precision signed decimal numbers and explicit rounding modes (`RoundingMode.HALF_UP`).
3. **Scale Enforcement:** In our `BillingService`, every calculation enforces `.setScale(2, RoundingMode.HALF_UP)` to guarantee exact rupee and paisa accounting without rounding drift.

---

### Q5: Why did you implement a deterministic simulation model instead of connecting to real IoT charging hardware?
**Answer:**
1. **Academic Honesty:** Connecting to physical EV charging stations in a college capstone project is unfeasible because commercial chargers use proprietary industrial protocols (such as OCPP 1.6J/2.0.1) connected to secure private enterprise networks.
2. **Deterministic Physics-Based Model:** Instead of faking hardware connections, we engineered an accurate mathematical simulation model:
   $$\text{Energy Delivered (kWh)} = \text{Charger Power (kW)} \times \text{Duration (Hours)} \times \eta \quad (\eta = 0.92\text{ efficiency})$$
   $$\Delta \text{SoC (\%)} = \left(\frac{\text{Energy Delivered}}{\text{Battery Capacity (kWh)}}\right) \times 100$$
   This simulates real-world battery charging curves and power delivery deterministically and transparently without misleading claims.

---

### Q6: What is the purpose of the 30 km geofence restriction?
**Answer:**
In urban and highway EV infrastructure, charging slots are scarce resources. Allowing drivers to pre-book chargers hundreds of kilometres away leads to high no-show rates, charger under-utilization, and frustration for drivers stranded near the station with empty batteries. The 30 km software geofence ensures that reservations are only made by drivers who are genuinely in the operational proximity of the station (typically 30–45 minutes driving time).

---

### Q7: How does JWT authentication work in this application?
**Answer:**
1. When a user submits credentials to `POST /api/auth/login`, Spring Security authenticates the email and BCrypt-hashed password.
2. Upon successful authentication, `JwtTokenProvider` generates a cryptographically signed JSON Web Token using the HMAC-SHA256 algorithm with a server-side secret key.
3. The token contains claims including subject (`email`), `userId`, `role`, and expiration time (24 hours).
4. The client saves this token in `localStorage` and includes it in the `Authorization: Bearer <token>` header for subsequent requests.
5. `JwtAuthenticationFilter` intercepts each request, validates the cryptographic signature and expiration, constructs a `UserPrincipal`, and populates Spring's `SecurityContextHolder`.

---

## 10. Troubleshooting Guide

| Issue | Root Cause | Solution |
| :--- | :--- | :--- |
| **`Connection refused: connect` to MySQL on port 3306** | Local MySQL service is stopped or not installed. | Either start your local MySQL server (`net start mysql80`) or run with H2 in-memory mode: `.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev-h2`. |
| **`Port 8080 already in use`** | Another process is listening on port 8080. | Stop the conflicting process or change port in `application.properties`: `server.port=8081`. |
| **Leaflet map tiles not rendering** | No active internet connection or tile server timeout. | Ensure your machine has internet access to fetch OpenStreetMap tiles (`tile.openstreetmap.org`), or verify firewall settings. |
| **PDF Invoice download blocked by browser** | Pop-up blocker preventing automatic blob download. | Allow pop-ups for `localhost:8080` in your browser settings. |
| **`AccessDeniedException` on Company Admin endpoints** | Attempting to access resources belonging to another company. | Ensure you are logged in with the credentials of the company that owns the station. |

---

## 11. Academic Integrity Statement

- **Demo Currency:** The token payment and billing modules simulate financial workflows using mock tokens and mathematical transactions. No real currency is charged or processed.
- **Maps & Open Data:** Interactive maps leverage OpenStreetMap tiles via Leaflet.js in compliance with OpenStreetMap's acceptable use policy.
- **Simulation Disclaimer:** Charging session power delivery, battery SoC progression, and waiting time calculations are deterministic mathematical simulations designed for educational demonstration.

---

## 12. Authors & Acknowledgements
- **Project:** Smart EV Station Finder, Geofenced Pre-Booking & Billing Management System
- **Degree:** Final Year B.Tech Computer Science & Engineering
- **Technologies:** Java 21, Spring Boot 3.3.5, MySQL 8.x, Leaflet.js, OpenStreetMap, Chart.js, OpenPDF
