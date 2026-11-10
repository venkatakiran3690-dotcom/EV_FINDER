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

        // =========================================================================
        // EXPANDED STATIONS: HYDERABAD (6 STATIONS)
        // =========================================================================
        Station stHyd1 = createStation(tataPower, "Tata Power EV Hub - Cyber Towers", "Building 1, Hitec City Main Road, Madhapur", "Hyderabad", "Telangana", "500081", 17.4504, 78.3808, 4);
        Station stHyd2 = createStation(zeonCharging, "Zeon Electric Charging - Financial District Hub", "WaveRock Tech Park, Nanakramguda, Gachibowli", "Hyderabad", "Telangana", "500032", 17.4156, 78.3427, 4);
        Station stHyd3 = createStation(jioBp, "Jio-bp pulse - Jubilee Hills Road 36", "Plot 784, Road Number 36, CBI Colony, Jubilee Hills", "Hyderabad", "Telangana", "500033", 17.4319, 78.4073, 3);
        Station stHyd4 = createStation(tataPower, "Tata Power EV Hub - Banjara Hills", "Near MLA Colony, Road No. 12, Banjara Hills", "Hyderabad", "Telangana", "500034", 17.4150, 78.4410, 4);
        Station stHyd5 = createStation(zeonCharging, "Zeon Fast Charger - Begumpet Main Road", "Prakash Nagar Metro Pillar 1380, Begumpet", "Hyderabad", "Telangana", "500016", 17.4442, 78.4735, 3);
        Station stHyd6 = createStation(jioBp, "Jio-bp pulse Express Hub - Uppal Metro", "Survey 214, Inner Ring Road, Uppal Junction", "Hyderabad", "Telangana", "500039", 17.3986, 78.5583, 4);

        // =========================================================================
        // EXPANDED STATIONS: CHENNAI (5 STATIONS)
        // =========================================================================
        Station stChn1 = createStation(tataPower, "Tata Power EV Hub - OMR IT Expressway", "Rajiv Gandhi Salai, Near Infosys Gate, Sholinganallur", "Chennai", "Tamil Nadu", "600119", 12.9010, 80.2279, 4);
        Station stChn2 = createStation(zeonCharging, "Zeon Electric Charging - Guindy Olympia Hub", "1 SIDCO Industrial Estate, Olympia Tech Park, Guindy", "Chennai", "Tamil Nadu", "600032", 13.0102, 80.2038, 3);
        Station stChn3 = createStation(jioBp, "Jio-bp pulse - Anna Nagar 2nd Avenue", "Roundtana Circle, 2nd Avenue, Anna Nagar", "Chennai", "Tamil Nadu", "600040", 13.0850, 80.2101, 4);
        Station stChn4 = createStation(tataPower, "Tata Power EV - T. Nagar Pondy Bazaar", "Sir Thyagaraya Road, Near Panagal Park, T. Nagar", "Chennai", "Tamil Nadu", "600017", 13.0418, 80.2341, 3);
        Station stChn5 = createStation(zeonCharging, "Zeon Fast Charger - Adyar Besant Avenue", "Near Theosophical Society, Besant Avenue, Adyar", "Chennai", "Tamil Nadu", "600020", 13.0012, 80.2565, 3);

        // =========================================================================
        // EXPANDED STATIONS: VIJAYAWADA (3 STATIONS)
        // =========================================================================
        Station stVja1 = createStation(jioBp, "Jio-bp pulse Highway Oasis - Gannavaram Airport", "NH 16 Vijayawada-Kolkata Highway, Gannavaram", "Vijayawada", "Andhra Pradesh", "521102", 16.5350, 80.7920, 4);
        Station stVja2 = createStation(tataPower, "Tata Power EV Hub - Benz Circle", "Near Jyothi Convention Centre, MG Road, Benz Circle", "Vijayawada", "Andhra Pradesh", "520010", 16.5002, 80.6480, 3);
        Station stVja3 = createStation(zeonCharging, "Zeon Fast Charger - Governorpet Commercial", "Besant Road Cross, Governorpet", "Vijayawada", "Andhra Pradesh", "520002", 16.5125, 80.6275, 3);

        // =========================================================================
        // EXPANDED STATIONS: VISAKHAPATNAM (3 STATIONS)
        // =========================================================================
        Station stViz1 = createStation(tataPower, "Tata Power EV Hub - Siripuram VIP Road", "VIP Road, Siripuram, Waltair Uplands", "Visakhapatnam", "Andhra Pradesh", "530003", 17.7215, 83.3150, 4);
        Station stViz2 = createStation(zeonCharging, "Zeon Electric Charging - RK Beach Road", "Beach Road Promenade, Pandurangapuram", "Visakhapatnam", "Andhra Pradesh", "530002", 17.7120, 83.3240, 3);
        Station stViz3 = createStation(jioBp, "Jio-bp pulse - Gajuwaka Steel Plant Corridor", "Old Gajuwaka Junction, NH 16 Bypass", "Visakhapatnam", "Andhra Pradesh", "530026", 17.6912, 83.2104, 3);

        // =========================================================================
        // EXPANDED STATIONS: KURNOOL (3 STATIONS)
        // =========================================================================
        Station stKrn1 = createStation(jioBp, "Jio-bp pulse Highway Oasis - NH 44 Kurnool", "NH 44 Hyderabad-Bangalore Highway, Kurnool Bypass", "Kurnool", "Andhra Pradesh", "518002", 15.8050, 78.0210, 4);
        Station stKrn2 = createStation(tataPower, "Tata Power EV Hub - Raj Vihar Centre", "Raj Vihar Circle, Main Road", "Kurnool", "Andhra Pradesh", "518001", 15.8281, 78.0373, 3);
        Station stKrn3 = createStation(zeonCharging, "Zeon Fast Charger - Bellary Road", "Bellary Road, Near Collector Office", "Kurnool", "Andhra Pradesh", "518004", 15.8190, 78.0160, 3);

        // =========================================================================
        // EXPANDED STATIONS: SELECTED TIER-2 CITIES (11 STATIONS)
        // =========================================================================
        Station stTpt1 = createStation(tataPower, "Tata Power EV Hub - Alipiri Toll Gate", "Alipiri Bypass Road, Near Garuda Statue", "Tirupati", "Andhra Pradesh", "517502", 13.6350, 79.4020, 3);
        Station stTpt2 = createStation(zeonCharging, "Zeon Fast Charger - Renigunta Airport Road", "NH 71 Airport Highway, Renigunta", "Tirupati", "Andhra Pradesh", "517520", 13.6420, 79.5130, 3);
        Station stNlr1 = createStation(jioBp, "Jio-bp pulse - Grand Trunk Road", "Mini Bypass Road, Ramji Nagar", "Nellore", "Andhra Pradesh", "524003", 14.4426, 79.9865, 3);
        Station stWgl1 = createStation(tataPower, "Tata Power EV Hub - Kazipet Main Road", "Warangal-Hyderabad Road, Kazipet", "Warangal", "Telangana", "506001", 17.9784, 79.5630, 3);
        Station stWgl2 = createStation(zeonCharging, "Zeon Fast Charger - Subedari Hanamkonda", "Subedari, Near Nayeem Nagar, Hanamkonda", "Warangal", "Telangana", "506002", 18.0020, 79.5780, 3);
        Station stGnt1 = createStation(jioBp, "Jio-bp pulse - Arundelpet Amaravathi Road", "Arundelpet 5th Line, Near Old Bus Stand", "Guntur", "Andhra Pradesh", "522002", 16.3067, 80.4365, 3);
        Station stRjy1 = createStation(tataPower, "Tata Power EV Hub - Godavari Bund", "Morampudi Junction, NH 16 Bypass", "Rajahmundry", "Andhra Pradesh", "533103", 17.0005, 81.7820, 3);
        Station stMys1 = createStation(zeonCharging, "Zeon Super Fast Hub - Mysore Palace Ring Road", "Ring Road Junction, Near Bannur Road", "Mysuru", "Karnataka", "570010", 12.3052, 76.6552, 3);
        Station stMys2 = createStation(jioBp, "Jio-bp pulse - Hebbal Industrial Area", "KRS Road, Hebbal Electronic City", "Mysuru", "Karnataka", "570016", 12.3580, 76.6020, 3);
        Station stCbe1 = createStation(tataPower, "Tata Power EV Hub - Gandhipuram Central", "Cross Cut Road, Near Central Bus Stand, Gandhipuram", "Coimbatore", "Tamil Nadu", "641012", 11.0168, 76.9672, 4);
        Station stCbe2 = createStation(zeonCharging, "Zeon Electric Charging - Avinashi Road", "Avinashi Road, Peelamedu", "Coimbatore", "Tamil Nadu", "641004", 11.0280, 77.0060, 4);

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

        // =========================================================================
        // CHARGERS FOR HYDERABAD STATIONS (6 STATIONS)
        // =========================================================================
        // stHyd1: Cyber Towers (4 chargers)
        createCharger(stHyd1, "TP-HYD-HITEC-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd1, "TP-HYD-HITEC-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd1, "TP-HYD-HITEC-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd1, "TP-HYD-HITEC-AC-7.4KW-04", "AC", "Type 2", 7.4, "OPERATIONAL", "AVAILABLE");

        // stHyd2: Financial District Hub (4 chargers)
        createCharger(stHyd2, "ZN-HYD-GACH-DC-150KW-01", "DC", "CCS2", 150.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd2, "ZN-HYD-GACH-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd2, "ZN-HYD-GACH-DC-60KW-03", "DC", "CHAdeMO", 60.0, "OPERATIONAL", "OCCUPIED");
        createCharger(stHyd2, "ZN-HYD-GACH-AC-22KW-04", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // stHyd3: Jubilee Hills (3 chargers)
        createCharger(stHyd3, "JBP-HYD-JUB-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd3, "JBP-HYD-JUB-DC-30KW-02", "DC", "CCS2", 30.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd3, "JBP-HYD-JUB-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // stHyd4: Banjara Hills (4 chargers)
        createCharger(stHyd4, "TP-HYD-BANJ-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd4, "TP-HYD-BANJ-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "OCCUPIED");
        createCharger(stHyd4, "TP-HYD-BANJ-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd4, "TP-HYD-BANJ-AC-7.4KW-04", "AC", "Type 2", 7.4, "OUT_OF_SERVICE", "UNAVAILABLE");

        // stHyd5: Begumpet (3 chargers)
        createCharger(stHyd5, "ZN-HYD-BEG-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd5, "ZN-HYD-BEG-AC-22KW-02", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd5, "ZN-HYD-BEG-AC-7.4KW-03", "AC", "Type 2", 7.4, "OPERATIONAL", "AVAILABLE");

        // stHyd6: Uppal Metro Hub (4 chargers)
        createCharger(stHyd6, "JBP-HYD-UPP-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd6, "JBP-HYD-UPP-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd6, "JBP-HYD-UPP-DC-60KW-03", "DC", "GB/T", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stHyd6, "JBP-HYD-UPP-AC-22KW-04", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // =========================================================================
        // CHARGERS FOR CHENNAI STATIONS (5 STATIONS)
        // =========================================================================
        // stChn1: OMR Expressway (4 chargers)
        createCharger(stChn1, "TP-CHN-OMR-DC-150KW-01", "DC", "CCS2", 150.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stChn1, "TP-CHN-OMR-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stChn1, "TP-CHN-OMR-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stChn1, "TP-CHN-OMR-AC-7.4KW-04", "AC", "Type 2", 7.4, "OPERATIONAL", "AVAILABLE");

        // stChn2: Guindy Olympia Hub (3 chargers)
        createCharger(stChn2, "ZN-CHN-GUIN-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stChn2, "ZN-CHN-GUIN-DC-60KW-02", "DC", "CHAdeMO", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stChn2, "ZN-CHN-GUIN-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // stChn3: Anna Nagar (4 chargers)
        createCharger(stChn3, "JBP-CHN-ANNA-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stChn3, "JBP-CHN-ANNA-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "OCCUPIED");
        createCharger(stChn3, "JBP-CHN-ANNA-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stChn3, "JBP-CHN-ANNA-AC-7.4KW-04", "AC", "Type 2", 7.4, "OPERATIONAL", "AVAILABLE");

        // stChn4: T. Nagar Pondy Bazaar (3 chargers)
        createCharger(stChn4, "TP-CHN-TNAG-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stChn4, "TP-CHN-TNAG-DC-30KW-02", "DC", "CCS2", 30.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stChn4, "TP-CHN-TNAG-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // stChn5: Adyar Besant Avenue (3 chargers)
        createCharger(stChn5, "ZN-CHN-ADY-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stChn5, "ZN-CHN-ADY-AC-22KW-02", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stChn5, "ZN-CHN-ADY-AC-7.4KW-03", "AC", "Type 2", 7.4, "OPERATIONAL", "AVAILABLE");

        // =========================================================================
        // CHARGERS FOR VIJAYAWADA STATIONS (3 STATIONS)
        // =========================================================================
        // stVja1: Gannavaram Airport NH16 (4 chargers)
        createCharger(stVja1, "JBP-VJA-GAN-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stVja1, "JBP-VJA-GAN-DC-120KW-02", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stVja1, "JBP-VJA-GAN-DC-60KW-03", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stVja1, "JBP-VJA-GAN-AC-22KW-04", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // stVja2: Benz Circle MG Road (3 chargers)
        createCharger(stVja2, "TP-VJA-BENZ-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stVja2, "TP-VJA-BENZ-DC-30KW-02", "DC", "CCS2", 30.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stVja2, "TP-VJA-BENZ-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // stVja3: Governorpet Commercial (3 chargers)
        createCharger(stVja3, "ZN-VJA-GOV-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stVja3, "ZN-VJA-GOV-AC-22KW-02", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stVja3, "ZN-VJA-GOV-AC-7.4KW-03", "AC", "Type 2", 7.4, "OPERATIONAL", "AVAILABLE");

        // =========================================================================
        // CHARGERS FOR VISAKHAPATNAM STATIONS (3 STATIONS)
        // =========================================================================
        // stViz1: Siripuram VIP Road (4 chargers)
        createCharger(stViz1, "TP-VIZ-SIRI-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stViz1, "TP-VIZ-SIRI-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stViz1, "TP-VIZ-SIRI-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stViz1, "TP-VIZ-SIRI-AC-7.4KW-04", "AC", "Type 2", 7.4, "OPERATIONAL", "AVAILABLE");

        // stViz2: RK Beach Road (3 chargers)
        createCharger(stViz2, "ZN-VIZ-BEACH-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stViz2, "ZN-VIZ-BEACH-DC-60KW-02", "DC", "CHAdeMO", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stViz2, "ZN-VIZ-BEACH-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // stViz3: Gajuwaka Steel Plant Corridor (3 chargers)
        createCharger(stViz3, "JBP-VIZ-GAJU-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stViz3, "JBP-VIZ-GAJU-DC-60KW-02", "DC", "GB/T", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stViz3, "JBP-VIZ-GAJU-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // =========================================================================
        // CHARGERS FOR KURNOOL STATIONS (3 STATIONS)
        // =========================================================================
        // stKrn1: NH 44 Bypass Kurnool (4 chargers)
        createCharger(stKrn1, "JBP-KRN-NH44-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stKrn1, "JBP-KRN-NH44-DC-120KW-02", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stKrn1, "JBP-KRN-NH44-DC-60KW-03", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stKrn1, "JBP-KRN-NH44-AC-22KW-04", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // stKrn2: Raj Vihar Centre (3 chargers)
        createCharger(stKrn2, "TP-KRN-RAJ-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stKrn2, "TP-KRN-RAJ-DC-30KW-02", "DC", "CCS2", 30.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stKrn2, "TP-KRN-RAJ-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // stKrn3: Bellary Road (3 chargers)
        createCharger(stKrn3, "ZN-KRN-BEL-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stKrn3, "ZN-KRN-BEL-AC-22KW-02", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stKrn3, "ZN-KRN-BEL-AC-7.4KW-03", "AC", "Type 2", 7.4, "OPERATIONAL", "AVAILABLE");

        // =========================================================================
        // CHARGERS FOR TIER-2 CITIES (11 STATIONS)
        // =========================================================================
        // Tirupati: Alipiri Toll Gate (3 chargers)
        createCharger(stTpt1, "TP-TPT-ALI-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stTpt1, "TP-TPT-ALI-DC-30KW-02", "DC", "CCS2", 30.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stTpt1, "TP-TPT-ALI-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // Tirupati: Renigunta Airport Road (3 chargers)
        createCharger(stTpt2, "ZN-TPT-RENI-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stTpt2, "ZN-TPT-RENI-AC-22KW-02", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stTpt2, "ZN-TPT-RENI-AC-7.4KW-03", "AC", "Type 2", 7.4, "OPERATIONAL", "AVAILABLE");

        // Nellore: Grand Trunk Road (3 chargers)
        createCharger(stNlr1, "JBP-NLR-GT-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stNlr1, "JBP-NLR-GT-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stNlr1, "JBP-NLR-GT-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // Warangal: Kazipet Main Road (3 chargers)
        createCharger(stWgl1, "TP-WGL-KZP-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stWgl1, "TP-WGL-KZP-DC-30KW-02", "DC", "CCS2", 30.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stWgl1, "TP-WGL-KZP-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // Warangal: Subedari Hanamkonda (3 chargers)
        createCharger(stWgl2, "ZN-WGL-SUB-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stWgl2, "ZN-WGL-SUB-AC-22KW-02", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stWgl2, "ZN-WGL-SUB-AC-7.4KW-03", "AC", "Type 2", 7.4, "OPERATIONAL", "AVAILABLE");

        // Guntur: Arundelpet Amaravathi Road (3 chargers)
        createCharger(stGnt1, "JBP-GNT-ARUN-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stGnt1, "JBP-GNT-ARUN-DC-30KW-02", "DC", "CCS2", 30.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stGnt1, "JBP-GNT-ARUN-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // Rajahmundry: Godavari Bund (3 chargers)
        createCharger(stRjy1, "TP-RJY-MOR-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stRjy1, "TP-RJY-MOR-DC-30KW-02", "DC", "CCS2", 30.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stRjy1, "TP-RJY-MOR-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // Mysuru: Palace Ring Road (3 chargers)
        createCharger(stMys1, "ZN-MYS-ORR-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stMys1, "ZN-MYS-ORR-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stMys1, "ZN-MYS-ORR-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // Mysuru: Hebbal Industrial Area (3 chargers)
        createCharger(stMys2, "JBP-MYS-HEB-DC-60KW-01", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stMys2, "JBP-MYS-HEB-DC-30KW-02", "DC", "CCS2", 30.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stMys2, "JBP-MYS-HEB-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

        // Coimbatore: Gandhipuram Central (4 chargers)
        createCharger(stCbe1, "TP-CBE-GAND-DC-120KW-01", "DC", "CCS2", 120.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stCbe1, "TP-CBE-GAND-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stCbe1, "TP-CBE-GAND-AC-22KW-03", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stCbe1, "TP-CBE-GAND-AC-7.4KW-04", "AC", "Type 2", 7.4, "OPERATIONAL", "AVAILABLE");

        // Coimbatore: Avinashi Road Peelamedu (4 chargers)
        createCharger(stCbe2, "ZN-CBE-PEEL-DC-150KW-01", "DC", "CCS2", 150.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stCbe2, "ZN-CBE-PEEL-DC-60KW-02", "DC", "CCS2", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stCbe2, "ZN-CBE-PEEL-DC-60KW-03", "DC", "CHAdeMO", 60.0, "OPERATIONAL", "AVAILABLE");
        createCharger(stCbe2, "ZN-CBE-PEEL-AC-22KW-04", "AC", "Type 2", 22.0, "OPERATIONAL", "AVAILABLE");

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
        Review rev4 = new Review(stHyd1, driver2, 5, "Cyber Towers hub is spotless! 120 kW fast gun reached full rate instantly on Ioniq 5.");
        Review rev5 = new Review(stChn1, driver, 5, "Reliable charging on OMR expressway near Sholinganallur. Great amenities nearby.");
        Review rev6 = new Review(stVja1, driver, 5, "NH 16 airport corridor stop has fantastic high-speed chargers and clean washrooms.");
        Review rev7 = new Review(stViz1, driver2, 4, "Convenient VIP road location in Vizag. Easy pre-booking through the geofenced app.");
        Review rev8 = new Review(stKrn1, driver, 5, "Lifesaver on the Hyderabad-Bangalore NH 44 highway run. 120 kW DC charged smoothly.");
        reviewRepository.save(rev1);
        reviewRepository.save(rev2);
        reviewRepository.save(rev3);
        reviewRepository.save(rev4);
        reviewRepository.save(rev5);
        reviewRepository.save(rev6);
        reviewRepository.save(rev7);
        reviewRepository.save(rev8);

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
                    "Verified 3 CPOs, 36 stations, 121 chargers across 14 cities, and regulated tariffs.",
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

    private Station createStation(Company company, String name, String address, String city, String state, String postalCode,
                                  Double latitude, Double longitude, Integer totalChargers) {
        Station s = new Station(company, name, address, city, state, postalCode, latitude, longitude, totalChargers);
        return stationRepository.save(s);
    }

    private Charger createCharger(Station station, String identifier, String type, String connector,
                                  Double powerRatingKw, String operationalStatus, String availabilityStatus) {
        Charger c = new Charger(station, identifier, type, connector, powerRatingKw, operationalStatus, availabilityStatus);
        return chargerRepository.save(c);
    }
}
