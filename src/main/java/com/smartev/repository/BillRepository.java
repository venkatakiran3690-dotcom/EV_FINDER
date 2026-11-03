package com.smartev.repository;

import com.smartev.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByInvoiceNumber(String invoiceNumber);

    Optional<Bill> findBySessionId(Long sessionId);

    boolean existsBySessionId(Long sessionId);

    List<Bill> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<Bill> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<Bill> findByStationIdOrderByCreatedAtDesc(Long stationId);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.paymentStatus = 'PAID'")
    BigDecimal calculatePlatformTotalRevenue();

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.company.id = :companyId AND b.paymentStatus = 'PAID'")
    BigDecimal calculateCompanyTotalRevenue(@Param("companyId") Long companyId);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.station.id = :stationId AND b.paymentStatus = 'PAID'")
    BigDecimal calculateStationTotalRevenue(@Param("stationId") Long stationId);

    @Query("SELECT COALESCE(SUM(b.energyConsumedKwh), 0) FROM Bill b WHERE b.driver.id = :driverId")
    Double calculateDriverTotalEnergyConsumed(@Param("driverId") Long driverId);

    @Query("SELECT COALESCE(SUM(b.totalAmount), 0) FROM Bill b WHERE b.driver.id = :driverId AND b.paymentStatus = 'PAID'")
    BigDecimal calculateDriverTotalExpenditure(@Param("driverId") Long driverId);
}
