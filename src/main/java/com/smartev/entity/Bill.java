package com.smartev.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills", indexes = {
    @Index(name = "idx_bill_invoice_no", columnList = "invoice_number", unique = true),
    @Index(name = "idx_bill_session", columnList = "session_id", unique = true)
})
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false, unique = true)
    private ChargingSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charger_id", nullable = false)
    private Charger charger;

    @Column(name = "energy_consumed_kwh", nullable = false)
    private Double energyConsumedKwh;

    @Column(name = "price_per_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerKwh;

    @Column(name = "energy_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal energyCost;

    @Column(name = "service_charge", nullable = false, precision = 10, scale = 2)
    private BigDecimal serviceCharge;

    @Column(name = "tax_rate_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal taxRatePct;

    @Column(name = "tax_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus = "PAID";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Bill() {}

    public Bill(String invoiceNumber, ChargingSession session, Booking booking, User driver,
                Company company, Station station, Charger charger, Double energyConsumedKwh,
                BigDecimal pricePerKwh, BigDecimal energyCost, BigDecimal serviceCharge,
                BigDecimal taxRatePct, BigDecimal taxAmount, BigDecimal totalAmount, String paymentStatus) {
        this.invoiceNumber = invoiceNumber;
        this.session = session;
        this.booking = booking;
        this.driver = driver;
        this.company = company;
        this.station = station;
        this.charger = charger;
        this.energyConsumedKwh = energyConsumedKwh;
        this.pricePerKwh = pricePerKwh;
        this.energyCost = energyCost;
        this.serviceCharge = serviceCharge;
        this.taxRatePct = taxRatePct;
        this.taxAmount = taxAmount;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public ChargingSession getSession() { return session; }
    public void setSession(ChargingSession session) { this.session = session; }

    public Booking getBooking() { return booking; }
    public void setBooking(Booking booking) { this.booking = booking; }

    public User getDriver() { return driver; }
    public void setDriver(User driver) { this.driver = driver; }

    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public Station getStation() { return station; }
    public void setStation(Station station) { this.station = station; }

    public Charger getCharger() { return charger; }
    public void setCharger(Charger charger) { this.charger = charger; }

    public Double getEnergyConsumedKwh() { return energyConsumedKwh; }
    public void setEnergyConsumedKwh(Double energyConsumedKwh) { this.energyConsumedKwh = energyConsumedKwh; }

    public BigDecimal getPricePerKwh() { return pricePerKwh; }
    public void setPricePerKwh(BigDecimal pricePerKwh) { this.pricePerKwh = pricePerKwh; }

    public BigDecimal getEnergyCost() { return energyCost; }
    public void setEnergyCost(BigDecimal energyCost) { this.energyCost = energyCost; }

    public BigDecimal getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(BigDecimal serviceCharge) { this.serviceCharge = serviceCharge; }

    public BigDecimal getTaxRatePct() { return taxRatePct; }
    public void setTaxRatePct(BigDecimal taxRatePct) { this.taxRatePct = taxRatePct; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
