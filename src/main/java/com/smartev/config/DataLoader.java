package com.smartev.config;

import com.smartev.entity.*;
import com.smartev.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final StationRepository stationRepository;
    private final ChargerRepository chargerRepository;
    private final VehicleRepository vehicleRepository;
    private final TariffRepository tariffRepository;
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final ChargingSessionRepository sessionRepository;
    private final BillRepository billRepository;
    private final ReviewRepository reviewRepository;
    private final NotificationRepository notificationRepository;
    private final StationManagerAssignmentRepository assignmentRepository;
    private final ActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed-demo-data:true}")
    private boolean seedDemoData;

    public DataLoader(UserRepository userRepository,
                      CompanyRepository companyRepository,
                      StationRepository stationRepository,
                      ChargerRepository chargerRepository,
                      VehicleRepository vehicleRepository,
                      TariffRepository tariffRepository,
                      BookingRepository bookingRepository,
                      PaymentRepository paymentRepository,
                      ChargingSessionRepository sessionRepository,
                      BillRepository billRepository,
                      ReviewRepository reviewRepository,
                      NotificationRepository notificationRepository,
                      StationManagerAssignmentRepository assignmentRepository,
                      ActivityLogRepository activityLogRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
        this.stationRepository = stationRepository;
        this.chargerRepository = chargerRepository;
        this.vehicleRepository = vehicleRepository;
        this.tariffRepository = tariffRepository;
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.sessionRepository = sessionRepository;
        this.billRepository = billRepository;
        this.reviewRepository = reviewRepository;
        this.notificationRepository = notificationRepository;
        this.assignmentRepository = assignmentRepository;
        this.activityLogRepository = activityLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!seedDemoData || userRepository.existsByEmail("admin@smartev.com")) {
            return;
        }

        // 1. Companies (CPOs)
        Company tataPower = new Company(
                "Tata Power EZ Charge",
                "CPO-IND-2022-001",
                "support@tatapowerezcharge.com",
                "+91 1800 209 5161",
                "Corporate Centre, 34 Sant Tukaram Road, Mumbai, Maharashtra 400009"
        );
        companyRepository.save(tataPower);

        Company zeonCharging = new Company(
                "Zeon Electric Charging",
                "CPO-IND-2023-009",
                "care@zeoncharging.com",
                "+91 99400 99400",
                "Tech Park, Avinashi Road, Coimbatore, Tamil Nadu 641014"
        );
        companyRepository.save(zeonCharging);

        Company jioBp = new Company(
                "Jio-bp pulse",
                "CPO-IND-2024-042",
                "pulse@jiobp.com",
                "+91 1800 891 9000",
                "Maker Maxity, BKC, Bandra East, Mumbai, Maharashtra 400051"
        );
        companyRepository.save(jioBp);

        // 2. Users (Role-Based System Actors)
        // Platform Admin
        User platformAdmin = new User(
                "admin@smartev.com",
                passwordEncoder.encode("Admin@123"),
                "Chief Platform Administrator",
                "+91 98765 00001",
                Role.PLATFORM_ADMIN,
                null
        );
        userRepository.save(platformAdmin);

        // Company Admin for Tata Power
        User tataAdmin = new User(
                "tatapower@smartev.com",
                passwordEncoder.encode("Tata@123"),
                "Rajesh Sharma (Tata CPO Admin)",
                "+91 98765 00002",
                Role.COMPANY_ADMIN,
                tataPower
        );
        userRepository.save(tataAdmin);

        // Company Admin for Zeon Charging
        User zeonAdmin = new User(
                "zeon@smartev.com",
                passwordEncoder.encode("Zeon@123"),
                "Priya Natarajan (Zeon CPO Admin)",
                "+91 98765 00003",
                Role.COMPANY_ADMIN,
                zeonCharging
        );
        userRepository.save(zeonAdmin);

        // Station Manager
        User stationManager = new User(
                "manager.bangalore@smartev.com",
                passwordEncoder.encode("Manager@123"),
                "Suresh Kumar (Hub Manager)",
                "+91 98765 00004",
                Role.STATION_MANAGER,
                tataPower
        );
        userRepository.save(stationManager);

        // Primary Demo Driver
        User driver = new User(
                "driver@smartev.com",
                passwordEncoder.encode("Driver@123"),
                "Venkata Kiran",
                "+91 98765 43210",
                Role.DRIVER,
                null
        );
        userRepository.save(driver);

        // Second Demo Driver
        User driver2 = new User(
                "driver2@smartev.com",
                passwordEncoder.encode("Driver@123"),
                "Ananya Reddy",
                "+91 98765 43211",
                Role.DRIVER,
                null
        );
        userRepository.save(driver2);

        // 3. Registered Vehicles for Drivers
        Vehicle nexonEv = new Vehicle(
                driver,
                "Tata",
                "Nexon EV Long Range",
                2024,
                "KA-01-EV-4096",
                40.5,
                "CCS2"
        );
        vehicleRepository.save(nexonEv);

        Vehicle mgZs = new Vehicle(
                driver,
                "MG",
                "ZS EV Excite",
                2023,
                "KA-05-EV-8821",
                50.3,
                "CCS2"
        );
        vehicleRepository.save(mgZs);

        Vehicle ioniq5 = new Vehicle(
                driver2,
                "Hyundai",
                "Ioniq 5 EV",
                2024,
                "TS-09-EV-1008",
                72.6,
                "CCS2"
        );
        vehicleRepository.save(ioniq5);

        // 4. Charging Stations (Bangalore & Highway Corridors)
        // Station 1: Tata Power - Indiranagar Super Hub (Bangalore)
        Station station1 = new Station(
                tataPower,
                "Tata Power EV Hub - Indiranagar",
                "100 Feet Road, Near HAL 2nd Stage, Indiranagar",
                "Bangalore",
                "Karnataka",
                "560038",
                12.9784,
                77.6408,
                4
        );
        stationRepository.save(station1);

        // Explicitly assign Station Manager (Suresh Kumar) to Station 1 (Indiranagar Hub)
        StationManagerAssignment managerAssignment = new StationManagerAssignment(stationManager, station1);
        assignmentRepository.save(managerAssignment);

        // Station 2: Zeon Charging - Electronic City Expressway (Bangalore)
        Station station2 = new Station(
                zeonCharging,
                "Zeon Fast Charger - Electronic City Phase 1",
                "Velankani Tech Park, Hosur Road, Electronic City",
                "Bangalore",
                "Karnataka",
                "560100",
                12.8452,
                77.6602,
                3
        );
        stationRepository.save(station2);

        // Station 3: Tata Power - Whitefield ITPL Hub (Bangalore)
        Station station3 = new Station(
                tataPower,
                "Tata Power - ITPL Tech Hub",
                "International Tech Park, Whitefield Main Road",
                "Bangalore",
                "Karnataka",
                "560066",
                12.9863,
                77.7381,
                3
        );
        stationRepository.save(station3);

        // Station 4: Jio-bp pulse - Bangalore-Mysore Highway Expressway Hub
        Station station4 = new Station(
                jioBp,
                "Jio-bp pulse Highway Oasis - Bidadi",
                "NH 275 Bangalore-Mysore Expressway, Bidadi",
                "Ramanagara",
                "Karnataka",
                "562109",
                12.7950,
                77.3820,
                4
        );
        stationRepository.save(station4);

        // Station 5: Zeon Fast Charger - Koramangala 5th Block (Bangalore)
        Station station5 = new Station(
                zeonCharging,
                "Zeon Super Fast Hub - Koramangala",
                "80 Feet Road, 5th Block, Near Sony World Signal",
                "Bangalore",
                "Karnataka",
                "560095",
                12.9352,
                77.6245,
                3
        );
        stationRepository.save(station5);

        // 5. Chargers for Stations
        // Station 1 Chargers
        Charger chg1A = new Charger(station1, "TP-INDIRA-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        Charger chg1B = new Charger(station1, "TP-INDIRA-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        Charger chg1C = new Charger(station1, "TP-INDIRA-AC-22KW-01", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        Charger chg1D = new Charger(station1, "TP-INDIRA-AC-7.4KW", "AC", "Type 2", 7.4, "OUT_OF_SERVICE", "UNAVAILABLE");
        chargerRepository.save(chg1A);
        chargerRepository.save(chg1B);
        chargerRepository.save(chg1C);
        chargerRepository.save(chg1D);

        // Station 2 Chargers
        Charger chg2A = new Charger(station2, "ZN-ECITY-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        Charger chg2B = new Charger(station2, "ZN-ECITY-DC-60KW-02", "DC", "CHAdeMO", 60.0, "OPERATIONAL", "AVAILABLE");
        Charger chg2C = new Charger(station2, "ZN-ECITY-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        chargerRepository.save(chg2A);
        chargerRepository.save(chg2B);
        chargerRepository.save(chg2C);

        // Station 3 Chargers
        Charger chg3A = new Charger(station3, "TP-ITPL-DC-150KW-01", "DC", "CCS2", 150.0, "OPERATIONAL", "AVAILABLE");
        Charger chg3B = new Charger(station3, "TP-ITPL-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        Charger chg3C = new Charger(station3, "TP-ITPL-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        chargerRepository.save(chg3A);
        chargerRepository.save(chg3B);
        chargerRepository.save(chg3C);

        // Station 4 Chargers (Highway)
        Charger chg4A = new Charger(station4, "JBP-HWY-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        Charger chg4B = new Charger(station4, "JBP-HWY-DC-120KW-02", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        Charger chg4C = new Charger(station4, "JBP-HWY-DC-60KW-03", "DC", "GB/T", 60.0, "OPERATIONAL", "AVAILABLE");
        Charger chg4D = new Charger(station4, "JBP-HWY-AC-22KW-04", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        chargerRepository.save(chg4A);
        chargerRepository.save(chg4B);
        chargerRepository.save(chg4C);
        chargerRepository.save(chg4D);

        // Station 5 Chargers
        Charger chg5A = new Charger(station5, "ZN-KORA-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        Charger chg5B = new Charger(station5, "ZN-KORA-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        Charger chg5C = new Charger(station5, "ZN-KORA-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        chargerRepository.save(chg5A);
        chargerRepository.save(chg5B);
        chargerRepository.save(chg5C);

        // 6. Tariffs
        Tariff tariffTata = new Tariff(
                tataPower,
                null, // All stations
                "ALL",
                BigDecimal.valueOf(19.50), // Rs. 19.50 per kWh
                BigDecimal.valueOf(25.00), // Service fee Rs. 25
                BigDecimal.valueOf(18.00)  // 18% GST
        );
        tariffRepository.save(tariffTata);

        Tariff tariffZeon = new Tariff(
                zeonCharging,
                null,
                "ALL",
                BigDecimal.valueOf(21.00), // Rs. 21.00 per kWh
                BigDecimal.valueOf(30.00),
                BigDecimal.valueOf(18.00)
        );
        tariffRepository.save(tariffZeon);

        Tariff tariffJio = new Tariff(
                jioBp,
                null,
                "ALL",
                BigDecimal.valueOf(18.50), // Rs. 18.50 per kWh
                BigDecimal.valueOf(20.00),
                BigDecimal.valueOf(18.00)
        );
        tariffRepository.save(tariffJio);

        // 7. Seed Sample Reviews
        Review rev1 = new Review(station1, driver, 5, "Excellent high-speed 60 kW charging post! Charged my Nexon from 20% to 80% in 45 minutes.");
        Review rev2 = new Review(station1, driver2, 4, "Clean station, easy access near Indiranagar 100ft road.");
        Review rev3 = new Review(station2, driver, 5, "Ultra-fast 120 kW charger at Electronic City. Very helpful on the commute.");
        reviewRepository.save(rev1);
        reviewRepository.save(rev2);
        reviewRepository.save(rev3);

        // 8. Seed Initial Completed Booking and Invoice for visual demonstration
        LocalDateTime sampleStart = LocalDateTime.now().minusDays(1).withHour(14).withMinute(0);
        LocalDateTime sampleEnd = sampleStart.plusMinutes(45);
        Booking sampleBooking = new Booking(
                "BK-DEMO-001",
                driver,
                nexonEv,
                station1,
                chg1A,
                sampleStart,
                sampleEnd,
                4.2, // 4.2 km away
                BigDecimal.valueOf(150.00)
        );
        sampleBooking.setStatus("COMPLETED");
        bookingRepository.save(sampleBooking);

        Payment samplePayment = new Payment(
                sampleBooking,
                driver,
                "TXN-DEMO-INIT001",
                BigDecimal.valueOf(150.00),
                "DEMO_TOKEN",
                "SUCCESS",
                "Demo Token Wallet"
        );
        paymentRepository.save(samplePayment);

        ChargingSession sampleSession = new ChargingSession(
                sampleBooking,
                chg1A,
                driver,
                "SESS-DEMO-001",
                sampleStart,
                22
        );
        sampleSession.setEndTime(sampleEnd);
        sampleSession.setCurrentBatteryPct(82);
        sampleSession.setEnergyConsumedKwh(24.50);
        sampleSession.setStatus("COMPLETED");
        sessionRepository.save(sampleSession);

        // Calculated Bill for Sample Session
        // 24.50 kWh * 19.50 = 477.75
        // Subtotal = 477.75 + 25.00 = 502.75
        // Tax 18% = 90.50
        // Total = 593.25
        Bill sampleBill = new Bill(
                "INV-2026-0001",
                sampleSession,
                sampleBooking,
                driver,
                tataPower,
                station1,
                chg1A,
                24.50,
                BigDecimal.valueOf(19.50),
                BigDecimal.valueOf(477.75),
                BigDecimal.valueOf(25.00),
                BigDecimal.valueOf(18.00),
                BigDecimal.valueOf(90.50),
                BigDecimal.valueOf(593.25),
                "PAID"
        );
        billRepository.save(sampleBill);

        // Seed In-App Welcome Notification
        Notification welcomeNote = new Notification(
                driver,
                "Welcome to Smart EV Platform!",
                "Find nearby stations, verify 30 km geofencing, and test slot pre-booking with demo tokens.",
                "SYSTEM"
        );
        notificationRepository.save(welcomeNote);

        // Seed Initial System Activity and Security Audit Logs
        if (activityLogRepository.count() == 0) {
            ActivityLog startupLog = new ActivityLog(
                    1L,
                    "system@smartev.com",
                    "SYSTEM",
                    "Application Startup & Data Seeding Complete",
                    "Verified 3 CPOs, 5 stations, 17 chargers, and regulated tariffs.",
                    "127.0.0.1"
            );
            activityLogRepository.save(startupLog);

            ActivityLog driverRegLog = new ActivityLog(
                    5L,
                    "driver@smartev.com",
                    "DRIVER",
                    "Driver Account Onboarded",
                    "Registered verified driver profile with vehicle KA-01-EV-4096.",
                    "127.0.0.1"
            );
            activityLogRepository.save(driverRegLog);

            ActivityLog managerAssignLog = new ActivityLog(
                    4L,
                    "manager.bangalore@smartev.com",
                    "STATION_MANAGER",
                    "Hub Station Assignment Initialized",
                    "Assigned operational management for Tata Power EV Hub - Indiranagar.",
                    "127.0.0.1"
            );
            activityLogRepository.save(managerAssignLog);
        }
    }
}
